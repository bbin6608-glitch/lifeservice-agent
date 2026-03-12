package com.lifeservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池配置
 */
@Slf4j
@Configuration
public class ThreadPoolConfig {

    /**
     * 缓存重建线程池
     */
    @Bean
    public ExecutorService cacheRebuildExecutor() {
        return new ThreadPoolExecutor(
                4,
                8,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                new ThreadFactory() {
                    private final AtomicInteger threadNum = new AtomicInteger(1);
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r);
                        t.setName("cache-rebuild-" + threadNum.getAndIncrement());
                        return t;
                    }
                },
                (r, executor) -> {
                    log.error("Cache rebuild thread pool is full! Task rejected.");
                    throw new RejectedExecutionException("Cache rebuild task rejected");
                }
        );
    }

    /**
     * Canal 消费专用线程池 (Spring 托管)
     * 
     * 配置说明：
     * - 单线程顺序消费：coreSize=1, maxSize=1
     * - 队列：100 容量的 LinkedBlockingQueue
     * - 线程特性：daemon = true，名称格式为 canal-shop-worker-x
     */
    @Bean
    public ExecutorService canalExecutorService() {
        return new ThreadPoolExecutor(
                1,
                1,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                new ThreadFactory() {
                    private final AtomicInteger threadNum = new AtomicInteger(1);
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r);
                        t.setName("canal-shop-worker-" + threadNum.getAndIncrement());
                        t.setDaemon(true);
                        return t;
                    }
                },
                (r, executor) -> {
                    log.error("Canal shop worker thread pool is full! Task rejected.");
                    throw new RejectedExecutionException("Canal shop worker task rejected");
                }
        );
    }
}
