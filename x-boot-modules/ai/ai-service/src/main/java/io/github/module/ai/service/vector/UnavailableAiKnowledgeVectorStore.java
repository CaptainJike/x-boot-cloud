package io.github.module.ai.service.vector;

import io.github.framework.core.exception.BusinessException;
import io.github.module.ai.enums.AiErrorEnum;
import io.github.module.ai.service.model.AiKnowledgeVectorSearchHit;
import io.github.module.ai.service.model.AiKnowledgeVectorSearchRequest;
import io.github.module.ai.service.model.AiKnowledgeVectorUpsertRequest;

import java.util.List;

/**
 * 未配置向量库时的保护性默认实现.
 */
public class UnavailableAiKnowledgeVectorStore implements AiKnowledgeVectorStore {

    @Override
    public String storeType() {
        return "UNAVAILABLE";
    }

    @Override
    public void upsert(AiKnowledgeVectorUpsertRequest request) throws BusinessException {
        throw unavailable();
    }

    @Override
    public void deleteByDocumentId(Long documentId) throws BusinessException {
        throw unavailable();
    }

    @Override
    public List<AiKnowledgeVectorSearchHit> search(AiKnowledgeVectorSearchRequest request)
            throws BusinessException {
        throw unavailable();
    }

    private BusinessException unavailable() {
        return new BusinessException(AiErrorEnum.KNOWLEDGE_VECTOR_STORE_UNAVAILABLE);
    }
}
