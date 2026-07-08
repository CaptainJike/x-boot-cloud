package io.github.module.ai.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.framework.core.constant.BaseConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 工作流执行记录 BO.
 */
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminAiWorkflowExecutionBO implements Serializable {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "创建时刻")
    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime createdAt;

    @Schema(description = "业务执行ID")
    private String executionId;

    @Schema(description = "工作流定义ID")
    private Long workflowDefinitionId;

    @Schema(description = "工作流编码")
    private String workflowCode;

    @Schema(description = "工作流名称")
    private String workflowName;

    @Schema(description = "版本号")
    private Integer versionNo;

    @Schema(description = "关联Agent ID")
    private Long agentId;

    @Schema(description = "后台用户ID")
    private Long userId;

    @Schema(description = "触发来源")
    private String triggerSource;

    @Schema(description = "触发业务ID")
    private String triggerId;

    @Schema(description = "状态(0=失败 1=成功 2=执行中 3=取消)")
    private Integer status;

    @Schema(description = "当前节点Key")
    private String currentNodeKey;

    @Schema(description = "失败节点Key")
    private String failedNodeKey;

    @Schema(description = "耗时，单位毫秒")
    private Long durationMs;

    @Schema(description = "错误编码")
    private String errorCode;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "链路追踪ID")
    private String traceId;

    @Schema(description = "执行开始时刻")
    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime startedAt;

    @Schema(description = "执行结束时刻")
    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime finishedAt;
}
