package io.github.module.ai.service.embedding;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import io.github.framework.core.exception.BusinessException;
import io.github.module.ai.enums.AiErrorEnum;
import io.github.module.ai.service.model.AiKnowledgeEmbeddingContext;
import io.github.module.ai.service.model.AiKnowledgeEmbeddingRequest;
import io.github.module.ai.service.model.AiKnowledgeEmbeddingResponse;
import io.github.starter.ai.enums.AiProviderTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

/**
 * OpenAI-compatible embeddings 供应商.
 */
@Slf4j
@Component
public class OpenAiCompatibleAiKnowledgeEmbeddingProvider implements AiKnowledgeEmbeddingProvider {

    private static final String DEFAULT_OPENAI_BASE_URL = "https://api.openai.com/v1";

    private static final String DEFAULT_OPENAI_COMPATIBLE_BASE_URL =
            "https://dashscope.aliyuncs.com/compatible-mode/v1";

    private static final String DEFAULT_DEEPSEEK_BASE_URL = "https://api.deepseek.com";

    private static final String DEFAULT_ZHIPU_BASE_URL = "https://open.bigmodel.cn/api/paas/v4";

    @Override
    public boolean supports(AiKnowledgeEmbeddingContext context) {
        if (context == null) {
            return false;
        }
        AiProviderTypeEnum providerType = AiProviderTypeEnum.safeOf(context.getProviderType());
        return providerType == AiProviderTypeEnum.OPENAI
                || providerType == AiProviderTypeEnum.OPENAI_COMPATIBLE
                || providerType == AiProviderTypeEnum.DEEPSEEK
                || providerType == AiProviderTypeEnum.ZHIPU;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AiKnowledgeEmbeddingResponse embed(AiKnowledgeEmbeddingRequest request) throws BusinessException {
        AiKnowledgeEmbeddingContext context = request.getContext();
        String baseUrl = resolveBaseUrl(context);
        String apiKey = cleanApiKey(context.getApiKey());
        AiErrorEnum.MISSING_API_KEY.assertNotBlank(apiKey);

        try {
            Map<String, Object> body = RestClient.builder()
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .build()
                    .post()
                    .uri(baseUrl + "/embeddings")
                    .body(Map.of("model", context.getModelName(), "input", request.getText()))
                    .retrieve()
                    .body(Map.class);
            List<Double> vector = parseEmbedding(body);

            return AiKnowledgeEmbeddingResponse.builder()
                    .knowledgeBaseId(request.getKnowledgeBaseId())
                    .documentId(request.getDocumentId())
                    .chunkId(request.getChunkId())
                    .dimensions(vector.size())
                    .vector(vector)
                    .providerType(context.getProviderType())
                    .modelName(context.getModelName())
                    .build();
        } catch (RestClientException | IllegalArgumentException e) {
            log.warn("[AI知识库OpenAI-compatible向量化失败] >> providerType={}, modelName={}",
                    context.getProviderType(),
                    context.getModelName(),
                    e);
            throw new BusinessException(
                    AiErrorEnum.INVALID_KNOWLEDGE_EMBEDDING_CONFIG.getValue(),
                    "知识库向量化失败：" + rootMessage(e));
        }
    }

    @SuppressWarnings("unchecked")
    private List<Double> parseEmbedding(Map<String, Object> body) {
        Object data = body == null ? null : body.get("data");
        if (!(data instanceof List<?> dataList) || CollUtil.isEmpty(dataList)) {
            throw new IllegalArgumentException("embeddings response data is empty");
        }
        Object first = dataList.getFirst();
        if (!(first instanceof Map<?, ?> firstMap)) {
            throw new IllegalArgumentException("embeddings response item is invalid");
        }
        Object embedding = firstMap.get("embedding");
        if (!(embedding instanceof List<?> embeddingList) || CollUtil.isEmpty(embeddingList)) {
            throw new IllegalArgumentException("embeddings vector is empty");
        }

        return embeddingList.stream()
                .map(this::toDouble)
                .toList();
    }

    private Double toDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String stringValue) {
            return Double.valueOf(stringValue);
        }
        throw new IllegalArgumentException("invalid embedding value type");
    }

    private String resolveBaseUrl(AiKnowledgeEmbeddingContext context) {
        AiProviderTypeEnum providerType = AiProviderTypeEnum.safeOf(context.getProviderType());
        String configuredBaseUrl = CharSequenceUtil.cleanBlank(context.getBaseUrl());
        String resolvedBaseUrl = switch (providerType) {
            case OPENAI -> StrUtil.blankToDefault(configuredBaseUrl, DEFAULT_OPENAI_BASE_URL);
            case OPENAI_COMPATIBLE -> StrUtil.blankToDefault(
                    configuredBaseUrl,
                    DEFAULT_OPENAI_COMPATIBLE_BASE_URL);
            case DEEPSEEK -> StrUtil.blankToDefault(configuredBaseUrl, DEFAULT_DEEPSEEK_BASE_URL);
            case ZHIPU -> StrUtil.blankToDefault(configuredBaseUrl, DEFAULT_ZHIPU_BASE_URL);
            case OLLAMA -> configuredBaseUrl;
        };
        AiErrorEnum.INVALID_KNOWLEDGE_EMBEDDING_CONFIG.assertNotBlank(resolvedBaseUrl);
        return removeTrailingSlash(resolvedBaseUrl);
    }

    private String removeTrailingSlash(String value) {
        String result = value;
        while (StrUtil.endWith(result, "/")) {
            result = StrUtil.removeSuffix(result, "/");
        }
        return result;
    }

    private String cleanApiKey(String apiKey) {
        return StrUtil.trimToEmpty(apiKey);
    }

    private String rootMessage(Throwable e) {
        Throwable root = e;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return StrUtil.blankToDefault(root.getMessage(), root.getClass().getSimpleName());
    }
}
