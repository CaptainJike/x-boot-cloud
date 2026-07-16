package io.github.module.learning.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * Goal Brief DTO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "Goal Brief")
public class AppGoalBriefDTO implements Serializable {

    @Schema(description = "目标标题")
    @Size(max = 255, message = "【目标标题】最长255位")
    private String title;

    @Schema(description = "学习方向")
    @Size(max = 255, message = "【学习方向】最长255位")
    private String domain;

    @Schema(description = "学习动机")
    @Size(max = 2000, message = "【学习动机】最长2000位")
    private String motivation;

    @Schema(description = "当前水平")
    @Size(max = 2000, message = "【当前水平】最长2000位")
    private String currentLevel;

    @Schema(description = "期望产出")
    @Size(max = 2000, message = "【期望产出】最长2000位")
    private String desiredOutcome;

    @Schema(description = "成功标准")
    @Size(max = 20, message = "【成功标准】最多20条")
    private List<@Size(max = 500, message = "【成功标准项】最长500位") String> successCriteria;

    @Schema(description = "每周学习分钟数")
    @Min(value = 30, message = "【每周学习分钟数】最少30分钟")
    @Max(value = 10080, message = "【每周学习分钟数】最多10080分钟")
    private Integer weeklyLearningMinutes;

    @Schema(description = "目标周数")
    @Min(value = 1, message = "【目标周数】最少1周")
    @Max(value = 156, message = "【目标周数】最多156周")
    private Integer targetWeeks;

    @Schema(description = "偏好学习方式")
    @Size(max = 1000, message = "【偏好学习方式】最长1000位")
    private String preferredLearningStyle;

    @Schema(description = "约束条件")
    @Size(max = 20, message = "【约束条件】最多20条")
    private List<@Size(max = 500, message = "【约束条件项】最长500位") String> constraints;

    @Schema(description = "标签")
    @Size(max = 20, message = "【标签】最多20条")
    private List<@Size(max = 64, message = "【标签项】最长64位") String> tags;
}
