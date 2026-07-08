package io.github.module.ai.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 知识库向量检索请求.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AiKnowledgeVectorSearchRequest {

    private List<Long> knowledgeBaseIds;

    private String query;

    private List<Double> queryVector;

    private Integer topK;

    private Double similarityThreshold;

    private AiKnowledgeEmbeddingContext context;
}
