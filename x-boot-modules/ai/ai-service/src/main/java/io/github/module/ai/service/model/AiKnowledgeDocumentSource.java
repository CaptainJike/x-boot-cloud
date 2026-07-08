package io.github.module.ai.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 知识库文档原始来源.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AiKnowledgeDocumentSource {

    private Long documentId;

    private Long knowledgeBaseId;

    private Long ossFileId;

    private String documentName;

    private String originalFilename;

    private String extendName;

    private Long fileSize;

    private String md5;

    private String storagePlatform;

    private String storageFilename;

    private boolean directUrlSource;

    private String directUrl;

    private byte[] fileBytes;
}
