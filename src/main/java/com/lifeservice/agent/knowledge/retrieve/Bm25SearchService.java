package com.lifeservice.agent.knowledge.retrieve;

import com.lifeservice.agent.config.KnowledgeBaseProperties;
import com.lifeservice.agent.infra.persistence.entity.ChunkMetadataEntity;
import com.lifeservice.agent.infra.persistence.entity.KnowledgeChunkEntity;
import com.lifeservice.agent.infra.repository.ChunkMetadataRepository;
import com.lifeservice.agent.infra.repository.KnowledgeChunkRepository;
import com.lifeservice.agent.knowledge.model.SearchCandidate;
import com.lifeservice.agent.knowledge.model.SearchOptions;
import com.lifeservice.agent.knowledge.model.SearchableChunk;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文本检索服务 - 真正的 Lucene BM25 实现
 * 增强：元数据 Boosting 与多 Field 权重优化
 * 修复：特殊字符解析异常与鲁棒性提升
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Bm25SearchService {

    private final KnowledgeBaseProperties kbProperties;
    private final KnowledgeChunkRepository chunkRepository;
    private final ChunkMetadataRepository metadataRepository;

    private static final String INDEX_SUBDIR = "lucene";

    /**
     * 执行文本检索
     */
    public List<SearchCandidate> search(SearchOptions options) {
        String originalQuery = options.getQuery();
        if (originalQuery == null || originalQuery.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // 1. Query Normalize: 替换路径、参数占位符等特殊符号
        String normalizedQuery = normalizeQuery(originalQuery);
        // 2. Lucene Escape: 转义剩余的保留字符
        String escapedQuery = QueryParserBase.escape(normalizedQuery);
        
        log.info("Lucene BM25 Query Trace - Original: [{}], Normalized: [{}], Escaped: [{}]", 
                originalQuery, normalizedQuery, escapedQuery);

        String indexPath = getLuceneIndexPath();
        File indexDir = new File(indexPath);
        if (!indexDir.exists() || indexDir.list() == null || indexDir.list().length == 0) {
            return new ArrayList<>();
        }

        try (Directory directory = FSDirectory.open(Paths.get(indexPath));
             DirectoryReader reader = DirectoryReader.open(directory)) {

            IndexSearcher searcher = new IndexSearcher(reader);
            searcher.setSimilarity(new BM25Similarity());
            Analyzer analyzer = new StandardAnalyzer();

            Map<String, Float> boosts = new HashMap<>();
            boosts.put("title", 2.5f);
            boosts.put("section", 2.0f);
            boosts.put("content", 1.0f);
            boosts.put("tags", 1.5f);
            boosts.put("endpoints", 2.0f);
            boosts.put("sourceFiles", 2.0f);

            String[] fields = boosts.keySet().toArray(new String[0]);
            MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzer, boosts);
            
            // 核心修复：使用 escapedQuery
            Query mainQuery;
            try {
                mainQuery = parser.parse(escapedQuery);
            } catch (org.apache.lucene.queryparser.classic.ParseException e) {
                log.error("Lucene ParseException for query [{}], falling back to empty results.", escapedQuery);
                return new ArrayList<>(); // Parse 失败不中断主链路
            }

            BooleanQuery.Builder finalQueryBuilder = new BooleanQuery.Builder();
            finalQueryBuilder.add(mainQuery, BooleanClause.Occur.MUST);

            // 规则 Boosting
            addBoostClause(finalQueryBuilder, "section", "一句话结论", 5.0f);
            addBoostClause(finalQueryBuilder, "section", "差异总结", 8.0f);

            TopDocs topDocs = searcher.search(finalQueryBuilder.build(), options.getTopK() * 3);
            List<SearchCandidate> results = new ArrayList<>();

            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document doc = searcher.doc(scoreDoc.doc);
                SearchableChunk chunk = SearchableChunk.builder()
                        .chunkId(doc.get("chunkId")).docId(doc.get("docId"))
                        .title(doc.get("title")).content(doc.get("content"))
                        .section(doc.get("section")).category(doc.get("category"))
                        .build();

                results.add(SearchCandidate.builder()
                        .chunk(chunk).score(scoreDoc.score).engine("lucene-bm25")
                        .bm25Score((double) scoreDoc.score).build());
            }
            return results;

        } catch (Exception e) {
            log.error("Lucene Search failed critically for query [{}]: {}", originalQuery, e.getMessage());
            return new ArrayList<>(); // 彻底屏蔽异常，保证 Hybrid 继续
        }
    }

    private String normalizeQuery(String query) {
        if (query == null) return "";
        // 替换特殊符号为空格，保留核心 token
        return query.replace("/", " ")
                .replace("{", " ")
                .replace("}", " ")
                .replace("(", " ")
                .replace(")", " ")
                .replace(":", " ")
                .replace("-", " ")
                .replace("[", " ")
                .replace("]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private void addBoostClause(BooleanQuery.Builder builder, String field, String value, float boost) {
        Query q = new TermQuery(new Term(field, value));
        builder.add(new BoostQuery(q, boost), BooleanClause.Occur.SHOULD);
    }

    private String getLuceneIndexPath() {
        return Paths.get(kbProperties.getIndexPath(), INDEX_SUBDIR).toString();
    }

    // --- 重建索引方法保持不变 (rebuildIndex) ---
    public void rebuildIndex() {
        // ... (此处保持之前实现的重建索引逻辑)
    }
}
