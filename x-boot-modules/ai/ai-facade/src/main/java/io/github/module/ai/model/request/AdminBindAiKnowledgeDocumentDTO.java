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
 * 后台管理-关联 OSS 文件为知识库文档.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminBindAiKnowledgeDocumentDTO implements Serializable {

    @Schema(description = "知识库ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "知识库ID不能为空")
    private Long knowledgeBaseId;

    @Schema(description = "OSS文件ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "OSS文件ID不能为空")
    private Long ossFileId;

    @Schema(description = "文档名称")
    @Size(max = 200, message = "【文档名称】最长200位")
    private String documentName;

    @Schema(description = "文档描述")
    @Size(max = 500, message = "【文档描述】最长500位")
    private String description;

    @Schema(description = "是否绑定后自动解析")
    private Boolean autoParse;
}
