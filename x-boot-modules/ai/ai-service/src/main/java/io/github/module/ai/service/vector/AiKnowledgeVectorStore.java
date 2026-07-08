package io.github.module.ai.service.vector;

import io.github.framework.core.exception.BusinessException;
import io.github.module.ai.service.model.AiKnowledgeVectorSearchHit;
import io.github.module.ai.service.model.AiKnowledgeVectorSearchRequest;
import io.github.module.ai.service.model.AiKnowledgeVectorUpsertRequest;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 知识库向量存储适配器.
 */
public interface AiKnowledgeVectorStore {

    /**
     * 向量库类型.
     */
    String storeType();

    /**
     * 写入或更新向量文档.
     */
    void upsert(AiKnowledgeVectorUpsertRequest request) throws BusinessException;

    /**
     * 按文档删除向量.
     */
    void deleteByDocumentId(Long documentId) throws BusinessException;

    /**
     * 按查询向量执行相似度检索.
     */
    List<AiKnowledgeVectorSearchHit> search(AiKnowledgeVectorSearchRequest request) throws BusinessException;
}
