package io.github.module.ai.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 知识库向量化请求.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AiKnowledgeEmbeddingRequest {

    private Long knowledgeBaseId;

    private Long documentId;

    private Long chunkId;

    private Integer chunkNo;

    private String text;

    private AiKnowledgeEmbeddingContext context;
}
