package io.github.module.ai.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 后台管理-工作流分页查询.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminListAiWorkflowDTO implements Serializable {

    @Schema(description = "工作流编码")
    private String workflowCode;

    @Schema(description = "工作流名称")
    private String name;

    @Schema(description = "关联Agent ID")
    private Long agentId;

    @Schema(description = "状态(0=禁用 1=启用)")
    private Integer status;

    @Schema(description = "发布状态(0=草稿 1=已发布)")
    private Integer publishStatus;
}
