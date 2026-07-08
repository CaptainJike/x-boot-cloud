package io.github.module.ai.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 知识库向量化响应.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AiKnowledgeEmbeddingResponse {

    private Long knowledgeBaseId;

    private Long documentId;

    private Long chunkId;

    private Integer dimensions;

    private List<Double> vector;

    private String vectorHash;

    private String providerType;

    private String modelName;
}
