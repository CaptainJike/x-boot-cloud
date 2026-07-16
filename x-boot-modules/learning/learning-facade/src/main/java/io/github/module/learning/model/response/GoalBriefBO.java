package io.github.module.learning.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * Goal Brief BO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "Goal Brief")
public class GoalBriefBO implements Serializable {

    @Schema(description = "目标标题")
    private String title;

    @Schema(description = "学习方向")
    private String domain;

    @Schema(description = "学习动机")
    private String motivation;

    @Schema(description = "当前水平")
    private String currentLevel;

    @Schema(description = "期望产出")
    private String desiredOutcome;

    @Schema(description = "成功标准")
    private List<String> successCriteria;

    @Schema(description = "每周学习分钟数")
    private Integer weeklyLearningMinutes;

    @Schema(description = "目标周数")
    private Integer targetWeeks;

    @Schema(description = "偏好学习方式")
    private String preferredLearningStyle;

    @Schema(description = "约束条件")
    private List<String> constraints;

    @Schema(description = "标签")
    private List<String> tags;
}
