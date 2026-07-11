package io.github.module.learning.facade;

import io.github.module.learning.model.response.LearningMapBO;

/**
 * 学习地图 Facade.
 */
public interface LearningMapFacade {

    /**
     * 获取指定目标的学习地图.
     */
    LearningMapBO getMapByGoalId(Long goalId);
}
