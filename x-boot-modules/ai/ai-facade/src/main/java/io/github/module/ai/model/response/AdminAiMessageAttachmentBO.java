package io.github.module.ai.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 后台 AI 消息附件.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminAiMessageAttachmentBO implements Serializable {

    @Schema(description = "OSS文件ID")
    private Long ossFileId;

    @Schema(description = "附件类型(image/file)")
    private String attachmentType;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "MIME类型")
    private String mimeType;

    @Schema(description = "文件大小")
    private Long fileSize;

    @Schema(description = "排序号")
    private Integer sortNo;
}
