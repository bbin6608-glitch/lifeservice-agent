package com.lifeservice.agent.knowledge.retrieve;

import com.lifeservice.agent.knowledge.model.SearchCandidate;
import com.lifeservice.agent.knowledge.model.SearchOptions;

import java.util.List;

/**
 * 知识检索统一接口
 */
public interface KnowledgeSearchService {

    /**
     * 根据搜索选项检索最相关的知识片段
     * @param options 搜索配置
     * @return 排序后的候选列表
     */
    List<SearchCandidate> search(SearchOptions options);
    
    /**
     * 默认检索逻辑
     */
    default List<SearchCandidate> search(String query, int topK) {
        return search(SearchOptions.defaultFor(query, topK));
    }
}
