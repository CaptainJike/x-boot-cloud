package io.github.module.ai.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 后台管理-分页列表知识库文档.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminListAiKnowledgeDocumentDTO implements Serializable {

    @Schema(description = "知识库ID")
    private Long knowledgeBaseId;

    @Schema(description = "OSS文件ID")
    private Long ossFileId;

    @Schema(description = "文档名称")
    private String documentName;

    @Schema(description = "原始文件名")
    private String originalFilename;

    @Schema(description = "解析状态(0=失败 1=成功 2=处理中 3=待处理)")
    private Integer parseStatus;

    @Schema(description = "切片状态(0=失败 1=成功 2=处理中 3=待处理)")
    private Integer chunkStatus;

    @Schema(description = "状态(0=禁用 1=启用)")
    private Integer status;
}
