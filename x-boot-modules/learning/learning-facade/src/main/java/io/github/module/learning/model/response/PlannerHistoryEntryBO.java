package io.github.module.learning.model.response;

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
import java.util.List;

/**
 * 计划重排历史条目.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "计划重排历史条目")
public class PlannerHistoryEntryBO implements Serializable {

    @Schema(description = "历史条目标识")
    private String id;

    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @Schema(description = "记录时间")
    private LocalDateTime recordedAt;

    @Schema(description = "内容指纹")
    private String fingerprint;

    @Schema(description = "当前任务标题")
    private String missionTitle;

    @Schema(description = "当前任务摘要")
    private String missionSummary;

    @Schema(description = "阶段标签")
    private String stageLabel;

    @Schema(description = "模板验证标签")
    private String templateValidationBadge;

    @Schema(description = "模板验证摘要")
    private String templateValidationSummary;

    @Schema(description = "节奏状态")
    private String paceStatus;

    @Schema(description = "完成状态")
    private String completionStatus;

    @Schema(description = "完成置信度")
    private Integer completionConfidence;

    @Schema(description = "建议今日投入分钟数")
    private Integer suggestedTodayMinutes;

    @Schema(description = "当前堆积分钟数")
    private Integer backlogMinutes;

    @Schema(description = "连续学习风险")
    private String streakRisk;

    @Schema(description = "阶段里程碑标题")
    private String milestoneTitle;

    @Schema(description = "本轮重排原因")
    private String replanReason;

    @Schema(description = "复盘队列")
    private List<String> reviewQueue;

    @Schema(description = "延续队列")
    private List<String> carryOverQueue;

    @Schema(description = "本轮相对上一轮变化字段")
    private List<String> changedFields;

    @Schema(description = "触发重排的事件来源")
    private String triggerEventSource;

    @Schema(description = "触发重排的事件细分类型")
    private String triggerEventDetailType;

    @Schema(description = "触发重排的事件状态")
    private String triggerEventStatus;

    @Schema(description = "触发事件标题")
    private String triggerTitle;

    @Schema(description = "触发事件摘要")
    private String triggerSummary;
}
