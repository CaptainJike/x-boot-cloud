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
 * 今日学习概览 BO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "今日学习概览")
public class TodayLearningBO implements Serializable {

    @Schema(description = "目标信息")
    private LearningGoalBO goal;

    @Schema(description = "当前学习节点")
    private LearningMapNodeBO currentNode;

    @Schema(description = "今日推荐动作")
    private List<String> recommendedActions;

    @Schema(description = "今天是否已提交反思")
    private Boolean reflectedToday;

    @Schema(description = "今天的反思内容")
    private DailyReflectionBO todayReflection;
}
