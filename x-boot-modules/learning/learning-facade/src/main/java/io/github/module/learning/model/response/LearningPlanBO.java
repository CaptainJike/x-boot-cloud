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
 * 服务端学习计划快照.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "服务端学习计划快照")
public class LearningPlanBO implements Serializable {

    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime generatedAt;

    private String mode;
    private String missionTitle;
    private String missionSummary;
    private String missionReason;
    private String templateValidationBadge;
    private String templateValidationSummary;
    private String handoffTitle;
    private String handoffSummary;
    private List<String> handoffActions;
    private Integer totalMinutes;
    private Integer weeklyTargetMinutes;
    private Integer loggedMinutes;
    private Integer suggestedTodayMinutes;
    private Integer catchUpMinutes;
    private Integer missedDays;
    private Integer backlogMinutes;
    private String paceStatus;
    private String completionStatus;
    private String completionTitle;
    private String completionSummary;
    private Integer completionConfidence;
    private Integer remainingMinutes;
    private Integer expectedCompletionDays;
    private Integer targetCompletionDays;
    private Integer scheduleDeltaDays;
    private Integer recoveryWindowDays;
    private List<String> recoveryStrategy;
    private Boolean recoveryMode;
    private String recoveryModeTitle;
    private String stageLabel;
    private String milestoneTitle;
    private String milestoneSummary;
    private String replanReason;
    private String streakRisk;
    private List<LearningPlanTaskBO> tasks;
    private List<String> reviewQueue;
    private List<String> carryOverQueue;
    private String recoveryNote;
}
