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
 * 学习节奏快照.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "学习节奏快照")
public class LearningRhythmBO implements Serializable {

    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @Schema(description = "生成时间")
    private LocalDateTime generatedAt;

    @Schema(description = "数据模式")
    private String mode;

    @Schema(description = "周目标分钟数")
    private Integer weeklyTargetMinutes;

    @Schema(description = "已记录分钟数")
    private Integer loggedMinutes;

    @Schema(description = "剩余分钟数")
    private Integer remainingMinutes;

    @Schema(description = "完成百分比")
    private Integer completionPercent;

    @Schema(description = "连续天数")
    private Integer streakDays;

    @Schema(description = "最近七天活跃天数")
    private Integer activeDays;

    @Schema(description = "今天是否已完成最小学习动作")
    private Boolean todayDone;

    @Schema(description = "节奏标题")
    private String rhythmTitle;

    @Schema(description = "节奏摘要")
    private String rhythmSummary;

    @Schema(description = "周状态说明")
    private String weeklyStatus;

    @Schema(description = "本周聚焦")
    private String weeklyFocus;

    @Schema(description = "周计划摘要")
    private String weeklyPlanSummary;

    @Schema(description = "恢复建议")
    private String recoveryPlan;

    @Schema(description = "节奏信号")
    private List<String> signals;

    @Schema(description = "下一步提醒")
    private List<String> nextNudges;

    @Schema(description = "本周计划")
    private List<WeeklyPlanBucketBO> weeklyPlan;

    @Schema(description = "最近七天节奏")
    private List<LearningRhythmDayBO> week;
}
