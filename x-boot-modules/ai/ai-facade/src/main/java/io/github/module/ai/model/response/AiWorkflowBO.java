package io.github.module.ai.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.framework.core.constant.BaseConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 工作流 BO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AiWorkflowBO implements Serializable {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "创建时刻")
    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime createdAt;

    @Schema(description = "更新时刻")
    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime updatedAt;

    @Schema(description = "工作流编码")
    private String workflowCode;

    @Schema(description = "工作流名称")
    private String name;

    @Schema(description = "工作流描述")
    private String description;

    @Schema(description = "关联Agent ID")
    private Long agentId;

    @Schema(description = "版本号")
    private Integer versionNo;

    @Schema(description = "入口节点Key")
    private String entryNodeKey;

    @Schema(description = "状态(0=禁用 1=启用)")
    private Integer status;

    @Schema(description = "发布状态(0=草稿 1=已发布)")
    private Integer publishStatus;

    @Schema(description = "最近执行时刻")
    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime lastExecutedAt;

    @Schema(description = "执行次数")
    private Integer executionCount;
}
