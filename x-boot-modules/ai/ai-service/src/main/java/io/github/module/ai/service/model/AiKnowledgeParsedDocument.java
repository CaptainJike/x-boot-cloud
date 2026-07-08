package io.github.module.ai.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 知识库文档解析结果.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AiKnowledgeParsedDocument {

    private Long documentId;

    private Long knowledgeBaseId;

    private Long ossFileId;

    private String documentName;

    private String originalFilename;

    private String extendName;

    private String content;

    private List<AiKnowledgeParsedSection> sections;
}
