package io.github.module.ai.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 后台管理-工作流执行记录分页查询.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminListAiWorkflowExecutionDTO implements Serializable {

    @Schema(description = "业务执行ID")
    private String executionId;

    @Schema(description = "工作流定义ID")
    private Long workflowDefinitionId;

    @Schema(description = "工作流编码")
    private String workflowCode;

    @Schema(description = "后台用户ID")
    private Long userId;

    @Schema(description = "触发来源")
    private String triggerSource;

    @Schema(description = "触发业务ID")
    private String triggerId;

    @Schema(description = "状态(0=失败 1=成功 2=执行中 3=取消)")
    private Integer status;

    @Schema(description = "失败节点Key")
    private String failedNodeKey;
}
