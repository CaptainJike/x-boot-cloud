package io.github.module.learning.biz;

import io.github.framework.core.constant.BaseConstant;
import io.github.module.learning.facade.LearningGoalFacade;
import io.github.module.learning.model.request.AppCreateLearningGoalDTO;
import io.github.module.learning.model.request.AppGoalDraftAssistDTO;
import io.github.module.learning.model.response.GoalDraftAssistBO;
import io.github.module.learning.model.response.LearningMapBO;
import io.github.module.learning.model.response.TodayLearningBO;
import io.github.module.learning.service.LearningGoalService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 学习目标 Facade 实现.
 */
@RequiredArgsConstructor
@DubboService(version = BaseConstant.Version.DUBBO_VERSION_V1,
        validation = BaseConstant.Dubbo.ENABLE_VALIDATION,
        timeout = BaseConstant.Dubbo.AI_TIMEOUT)
public class LearningGoalFacadeImpl implements LearningGoalFacade {

    private final LearningGoalService learningGoalService;

    @Override
    public LearningMapBO createGoal(AppCreateLearningGoalDTO dto) {
        return learningGoalService.createGoal(dto);
    }

    @Override
    public GoalDraftAssistBO assistGoalDraft(AppGoalDraftAssistDTO dto) {
        return learningGoalService.assistGoalDraft(dto);
    }

    @Override
    public TodayLearningBO getToday() {
        return learningGoalService.getToday();
    }

    @Override
    public void ensureLearnerProfile() {
        learningGoalService.ensureLearnerProfileForCurrentUser();
    }
}
