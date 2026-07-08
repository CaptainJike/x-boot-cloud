package io.github.module.ai.service.embedding;

import io.github.framework.core.exception.BusinessException;
import io.github.module.ai.service.model.AiKnowledgeEmbeddingContext;
import io.github.module.ai.service.model.AiKnowledgeEmbeddingRequest;
import io.github.module.ai.service.model.AiKnowledgeEmbeddingResponse;

/**
 * 知识库向量化供应商适配器.
 */
public interface AiKnowledgeEmbeddingProvider {

    /**
     * 是否支持当前向量化上下文.
     */
    boolean supports(AiKnowledgeEmbeddingContext context);

    /**
     * 将文本转换为向量.
     */
    AiKnowledgeEmbeddingResponse embed(AiKnowledgeEmbeddingRequest request) throws BusinessException;
}
