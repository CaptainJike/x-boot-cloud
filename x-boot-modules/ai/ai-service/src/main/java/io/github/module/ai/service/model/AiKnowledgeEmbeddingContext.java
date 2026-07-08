package io.github.module.ai.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 知识库向量化模型上下文.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AiKnowledgeEmbeddingContext {

    private Long modelConfigId;

    private String modelConfigCode;

    private String providerType;

    private String baseUrl;

    private String apiKey;

    private String modelName;

    private Long timeoutSeconds;
}
