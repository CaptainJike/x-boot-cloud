package io.github.module.learning.facade;

import io.github.module.learning.model.response.LearningPlanBO;
import io.github.module.learning.model.response.ReplanTimelineBO;

/**
 * 学习计划 Facade.
 */
public interface LearningPlanFacade {

    /**
     * 获取当前目标的服务端学习计划.
     */
    LearningPlanBO getCurrentPlan(Long goalId);

    /**
     * 获取当前目标的服务端重排时间线.
     */
    ReplanTimelineBO getReplanTimeline(Long goalId);
}
