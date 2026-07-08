package io.github.module.ai.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.framework.crud.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * AI 工作流执行记录.
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName(value = "ai_workflow_execution")
public class AiWorkflowExecutionEntity extends BaseEntity<Long> {

    @Schema(description = "业务执行ID")
    @TableField(value = "execution_id")
    private String executionId;

    @Schema(description = "工作流定义ID")
    @TableField(value = "workflow_definition_id")
    private Long workflowDefinitionId;

    @Schema(description = "工作流编码")
    @TableField(value = "workflow_code")
    private String workflowCode;

    @Schema(description = "工作流名称")
    @TableField(value = "workflow_name")
    private String workflowName;

    @Schema(description = "版本号")
    @TableField(value = "version_no")
    private Integer versionNo;

    @Schema(description = "关联Agent ID")
    @TableField(value = "agent_id")
    private Long agentId;

    @Schema(description = "后台用户ID")
    @TableField(value = "user_id")
    private Long userId;

    @Schema(description = "触发来源")
    @TableField(value = "trigger_source")
    private String triggerSource;

    @Schema(description = "触发业务ID")
    @TableField(value = "trigger_id")
    private String triggerId;

    @Schema(description = "输入摘要JSON")
    @TableField(value = "input_summary")
    private String inputSummary;

    @Schema(description = "输出摘要JSON")
    @TableField(value = "output_summary")
    private String outputSummary;

    @Schema(description = "状态(0=失败 1=成功 2=执行中 3=取消)")
    @TableField(value = "status")
    private Integer status;

    @Schema(description = "当前节点Key")
    @TableField(value = "current_node_key")
    private String currentNodeKey;

    @Schema(description = "失败节点Key")
    @TableField(value = "failed_node_key")
    private String failedNodeKey;

    @Schema(description = "耗时，单位毫秒")
    @TableField(value = "duration_ms")
    private Long durationMs;

    @Schema(description = "错误编码")
    @TableField(value = "error_code")
    private String errorCode;

    @Schema(description = "错误信息")
    @TableField(value = "error_message")
    private String errorMessage;

    @Schema(description = "链路追踪ID")
    @TableField(value = "trace_id")
    private String traceId;

    @Schema(description = "执行开始时刻")
    @TableField(value = "started_at")
    private LocalDateTime startedAt;

    @Schema(description = "执行结束时刻")
    @TableField(value = "finished_at")
    private LocalDateTime finishedAt;
}
