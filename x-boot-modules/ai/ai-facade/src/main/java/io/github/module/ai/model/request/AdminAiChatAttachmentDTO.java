package io.github.module.ai.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 后台 AI 对话附件请求.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminAiChatAttachmentDTO implements Serializable {

    @Schema(description = "OSS文件ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "OSS文件ID不能为空")
    private Long ossFileId;

    @Schema(description = "附件类型(image/file)")
    @Size(max = 32, message = "【附件类型】最长32位")
    private String attachmentType;

    @Schema(description = "文件名")
    @Size(max = 255, message = "【文件名】最长255位")
    private String fileName;

    @Schema(description = "MIME类型")
    @Size(max = 100, message = "【MIME类型】最长100位")
    private String mimeType;

    @Schema(description = "文件大小")
    private Long fileSize;

    @Schema(description = "排序号")
    private Integer sortNo;
}
