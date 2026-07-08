package io.github.module.ai.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import io.github.framework.core.exception.BusinessException;
import io.github.module.ai.enums.AiErrorEnum;
import io.github.module.ai.service.embedding.AiKnowledgeEmbeddingProviderService;
import io.github.module.ai.service.model.AiKnowledgeEmbeddingRequest;
import io.github.module.ai.service.model.AiKnowledgeEmbeddingResponse;
import io.github.module.ai.service.model.AiKnowledgeVectorSearchHit;
import io.github.module.ai.service.model.AiKnowledgeVectorSearchRequest;
import io.github.module.ai.service.vector.AiKnowledgeVectorStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI 知识库向量检索编排服务.
 */
@RequiredArgsConstructor
@Service
public class AiKnowledgeVectorRetrievalService {

    private static final int DEFAULT_TOP_K = 5;

    private static final double DEFAULT_SIMILARITY_THRESHOLD = 0.0D;

    private final AiKnowledgeEmbeddingProviderService embeddingProviderService;

    private final AiKnowledgeVectorStore vectorStore;

    /**
     * 生成查询向量并执行向量库检索.
     */
    public List<AiKnowledgeVectorSearchHit> search(AiKnowledgeVectorSearchRequest request)
            throws BusinessException {
        validateRequest(request);
        int topK = defaultIfNull(request.getTopK(), DEFAULT_TOP_K);
        double threshold = defaultIfNull(request.getSimilarityThreshold(), DEFAULT_SIMILARITY_THRESHOLD);

        AiKnowledgeEmbeddingResponse embedding = embeddingProviderService.embed(AiKnowledgeEmbeddingRequest.builder()
                .text(request.getQuery())
                .context(request.getContext())
                .build());

        return vectorStore.search(AiKnowledgeVectorSearchRequest.builder()
                .knowledgeBaseIds(request.getKnowledgeBaseIds())
                .query(request.getQuery())
                .queryVector(embedding.getVector())
                .topK(topK)
                .similarityThreshold(threshold)
                .context(request.getContext())
                .build());
    }

    private void validateRequest(AiKnowledgeVectorSearchRequest request) {
        if (request == null
                || CollUtil.isEmpty(request.getKnowledgeBaseIds())
                || CharSequenceUtil.isBlank(request.getQuery())
                || request.getContext() == null
                || request.getTopK() != null && request.getTopK() <= 0
                || request.getSimilarityThreshold() != null && request.getSimilarityThreshold() < 0) {
            throw new BusinessException(AiErrorEnum.INVALID_KNOWLEDGE_EMBEDDING_CONFIG);
        }
    }

    private int defaultIfNull(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private double defaultIfNull(Double value, double defaultValue) {
        return value == null ? defaultValue : value;
    }
}
