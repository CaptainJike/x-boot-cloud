package io.github.module.ai.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 知识库文档切片草稿.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AiKnowledgeDocumentChunkDraft {

    private Long knowledgeBaseId;

    private Long documentId;

    private Integer chunkNo;

    private String content;

    private String contentPreview;

    private Integer sourcePage;

    private String sourcePosition;

    private Integer tokenCount;
}
