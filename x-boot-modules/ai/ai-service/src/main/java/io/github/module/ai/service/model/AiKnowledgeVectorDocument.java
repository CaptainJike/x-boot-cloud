package io.github.module.ai.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 知识库向量文档.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AiKnowledgeVectorDocument {

    private Long knowledgeBaseId;

    private String knowledgeBaseName;

    private Long documentId;

    private String documentName;

    private Long chunkId;

    private Integer chunkNo;

    private String content;

    private Integer sourcePage;

    private String sourcePosition;

    private String vectorId;

    private String vectorHash;

    private List<Double> vector;
}
