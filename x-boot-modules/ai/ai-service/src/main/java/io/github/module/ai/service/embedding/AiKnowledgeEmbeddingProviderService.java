package io.github.module.ai.service.embedding;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.digest.DigestUtil;
import io.github.framework.core.exception.BusinessException;
import io.github.module.ai.enums.AiErrorEnum;
import io.github.module.ai.service.model.AiKnowledgeEmbeddingContext;
import io.github.module.ai.service.model.AiKnowledgeEmbeddingRequest;
import io.github.module.ai.service.model.AiKnowledgeEmbeddingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 知识库向量化供应商选择服务.
 */
@RequiredArgsConstructor
@Service
public class AiKnowledgeEmbeddingProviderService {

    private final List<AiKnowledgeEmbeddingProvider> providers;

    /**
     * 执行向量化.
     */
    public AiKnowledgeEmbeddingResponse embed(AiKnowledgeEmbeddingRequest request) throws BusinessException {
        validateRequest(request);
        AiKnowledgeEmbeddingProvider provider = providers.stream()
                .filter(candidate -> candidate.supports(request.getContext()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(AiErrorEnum.UNSUPPORTED_KNOWLEDGE_EMBEDDING_PROVIDER));

        return completeResponse(request, provider.embed(request));
    }

    private void validateRequest(AiKnowledgeEmbeddingRequest request) {
        if (request == null
                || request.getContext() == null
                || CharSequenceUtil.isBlank(request.getText())) {
            throw new BusinessException(AiErrorEnum.INVALID_KNOWLEDGE_EMBEDDING_CONFIG);
        }
        AiKnowledgeEmbeddingContext context = request.getContext();
        if (CharSequenceUtil.isBlank(context.getProviderType())
                || CharSequenceUtil.isBlank(context.getModelName())) {
            throw new BusinessException(AiErrorEnum.INVALID_KNOWLEDGE_EMBEDDING_CONFIG);
        }
    }

    private AiKnowledgeEmbeddingResponse completeResponse(AiKnowledgeEmbeddingRequest request,
                                                          AiKnowledgeEmbeddingResponse response) {
        if (response == null || CollUtil.isEmpty(response.getVector())) {
            throw new BusinessException(AiErrorEnum.INVALID_KNOWLEDGE_EMBEDDING_CONFIG);
        }

        if (response.getKnowledgeBaseId() == null) {
            response.setKnowledgeBaseId(request.getKnowledgeBaseId());
        }
        if (response.getDocumentId() == null) {
            response.setDocumentId(request.getDocumentId());
        }
        if (response.getChunkId() == null) {
            response.setChunkId(request.getChunkId());
        }
        if (response.getProviderType() == null) {
            response.setProviderType(request.getContext().getProviderType());
        }
        if (response.getModelName() == null) {
            response.setModelName(request.getContext().getModelName());
        }
        if (response.getDimensions() == null) {
            response.setDimensions(response.getVector().size());
        }
        if (CharSequenceUtil.isBlank(response.getVectorHash())) {
            response.setVectorHash(DigestUtil.sha256Hex(response.getVector().toString()));
        }

        return response;
    }
}
