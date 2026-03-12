package com.lifeservice.canal;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lifeservice.cache.CacheInvalidationPublisher;
import com.lifeservice.dto.CacheInvalidateMessage;
import com.lifeservice.properties.CanalProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Canal Binlog 监听器 (重构版：集成 MQ 广播失效)
 */
@Slf4j
@Component
public class ShopCanalListener {

    @Resource
    private CanalProperties canalProperties;

    @Resource
    private CacheInvalidationPublisher cacheInvalidationPublisher;

    @Resource(name = "canalExecutorService")
    private ExecutorService executorService;

    @Value("${app.canal.enabled:true}")
    private boolean enabled;

    private CanalConnector connector;

    private volatile boolean running = true;

    private static final String TARGET_SCHEMA = "lifeservice";
    private static final String TARGET_TABLE = "tb_shop";

    @PostConstruct
    public void start() {
        if (!enabled) {
            log.info("Canal 监听器已根据配置禁用。");
            return;
        }
        connector = CanalConnectors.newSingleConnector(
                new InetSocketAddress(canalProperties.getHost(), canalProperties.getPort()),
                canalProperties.getDestination(),
                canalProperties.getUsername(),
                canalProperties.getPassword()
        );

        executorService.submit(this::worker);
        log.info("Canal 客户端（MQ 模式）启动成功。");
    }

    private void worker() {
        while (running) {
            try {
                connector.connect();
                connector.subscribe("lifeservice\\.tb_shop");
                connector.rollback();

                while (running) {
                    Message message = connector.getWithoutAck(canalProperties.getBatchSize());
                    long batchId = message.getId();
                    int size = message.getEntries().size();

                    if (batchId == -1 || size == 0) {
                        if (!sleep(1000)) break;
                        continue;
                    }

                    try {
                        processEntries(batchId, message.getEntries());
                        connector.ack(batchId);
                    } catch (Exception e) {
                        log.error("处理 Canal 批次失败，准备回滚。batchId={}", batchId, e);
                        connector.rollback(batchId);
                    }
                }
            } catch (Exception e) {
                if (running) {
                    log.error("Canal 连接器错误，5秒后重试", e);
                    if (!sleep(5000)) break;
                }
            } finally {
                disconnectQuietly();
            }
        }
    }

    private void processEntries(long batchId, List<CanalEntry.Entry> entries) {
        for (CanalEntry.Entry entry : entries) {
            if (entry.getEntryType() != CanalEntry.EntryType.ROWDATA) {
                continue;
            }

            CanalEntry.Header header = entry.getHeader();
            String schemaName = header.getSchemaName();
            String tableName = header.getTableName();

            if (!TARGET_SCHEMA.equalsIgnoreCase(schemaName) || !TARGET_TABLE.equalsIgnoreCase(tableName)) {
                continue;
            }

            CanalEntry.RowChange rowChange;
            try {
                rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException("解析 RowChange 失败", e);
            }

            CanalEntry.EventType eventType = rowChange.getEventType();
            if (!isDmlEvent(eventType)) {
                continue;
            }

            List<Long> ids = new ArrayList<>();
            for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                List<CanalEntry.Column> columns = (eventType == CanalEntry.EventType.DELETE)
                        ? rowData.getBeforeColumnsList()
                        : rowData.getAfterColumnsList();
                
                String idStr = getColumnValue(columns, "id");
                if (idStr != null) {
                    ids.add(Long.parseLong(idStr));
                }
            }

            if (ids.isEmpty()) continue;

            CacheInvalidateMessage message = CacheInvalidateMessage.builder()
                    .cacheType("shop")
                    .table(tableName)
                    .operation(eventType.name())
                    .ids(ids)
                    .timestamp(System.currentTimeMillis())
                    .build();

            cacheInvalidationPublisher.publish(message);
        }
    }

    private boolean isDmlEvent(CanalEntry.EventType eventType) {
        return eventType == CanalEntry.EventType.INSERT
                || eventType == CanalEntry.EventType.UPDATE
                || eventType == CanalEntry.EventType.DELETE;
    }

    private String getColumnValue(List<CanalEntry.Column> columns, String name) {
        for (CanalEntry.Column column : columns) {
            if (column.getName().equalsIgnoreCase(name)) {
                return column.getValue();
            }
        }
        return null;
    }

    private boolean sleep(long ms) {
        try {
            Thread.sleep(ms);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private void disconnectQuietly() {
        if (connector != null) {
            try {
                connector.disconnect();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    @PreDestroy
    public void stop() {
        running = false;
        disconnectQuietly();
    }
}
