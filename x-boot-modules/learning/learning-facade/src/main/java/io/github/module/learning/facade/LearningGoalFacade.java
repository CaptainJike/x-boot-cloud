package io.github.module.learning.facade;

import io.github.module.learning.model.request.AppCreateLearningGoalDTO;
import io.github.module.learning.model.request.AppGoalDraftAssistDTO;
import io.github.module.learning.model.response.GoalDraftAssistBO;
import io.github.module.learning.model.response.LearningMapBO;
import io.github.module.learning.model.response.TodayLearningBO;

/**
 * 学习目标 Facade.
 */
public interface LearningGoalFacade {

    /**
     * 创建学习目标并生成初始学习地图.
     */
    LearningMapBO createGoal(AppCreateLearningGoalDTO dto);

    /**
     * AI 补全 Goal Draft.
     */
    GoalDraftAssistBO assistGoalDraft(AppGoalDraftAssistDTO dto);

    /**
     * 获取今日学习概览.
     */
    TodayLearningBO getToday();

    /**
     * 确保当前用户存在学习者画像.
     */
    void ensureLearnerProfile();
}
