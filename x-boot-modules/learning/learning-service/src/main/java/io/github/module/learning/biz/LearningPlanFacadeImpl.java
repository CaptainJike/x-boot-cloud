package io.github.module.learning.biz;

import io.github.framework.core.constant.BaseConstant;
import io.github.module.learning.facade.LearningPlanFacade;
import io.github.module.learning.model.response.LearningPlanBO;
import io.github.module.learning.model.response.ReplanTimelineBO;
import io.github.module.learning.service.LearningPlanService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 学习计划 Facade 实现.
 */
@RequiredArgsConstructor
@DubboService(version = BaseConstant.Version.DUBBO_VERSION_V1,
        validation = BaseConstant.Dubbo.ENABLE_VALIDATION,
        timeout = BaseConstant.Dubbo.TIMEOUT)
public class LearningPlanFacadeImpl implements LearningPlanFacade {

    private final LearningPlanService learningPlanService;

    @Override
    public LearningPlanBO getCurrentPlan(Long goalId) {
        return learningPlanService.getCurrentPlan(goalId);
    }

    @Override
    public ReplanTimelineBO getReplanTimeline(Long goalId) {
        return learningPlanService.getReplanTimeline(goalId);
    }
}
