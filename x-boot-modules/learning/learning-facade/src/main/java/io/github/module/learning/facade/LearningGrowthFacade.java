package io.github.module.learning.facade;

import io.github.module.learning.model.response.GrowthTimelineBO;

/**
 * 成长 Facade.
 */
public interface LearningGrowthFacade {

    /**
     * 获取成长时间线.
     */
    GrowthTimelineBO getTimeline();
}
