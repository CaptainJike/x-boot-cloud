package io.github.module.learning.facade;

import io.github.module.learning.model.response.LearningAgentBO;

/**
 * 学习 Agent Facade.
 */
public interface LearningAgentFacade {

    /**
     * 获取学习 Agent 快照.
     */
    LearningAgentBO getLearningAgent(Long goalId);
}
