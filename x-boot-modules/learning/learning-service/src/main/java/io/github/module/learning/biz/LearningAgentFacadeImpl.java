package io.github.module.learning.biz;

import io.github.framework.core.constant.BaseConstant;
import io.github.module.learning.facade.LearningAgentFacade;
import io.github.module.learning.model.response.LearningAgentBO;
import io.github.module.learning.service.LearningAgentService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 学习 Agent Facade 实现.
 */
@RequiredArgsConstructor
@DubboService(version = BaseConstant.Version.DUBBO_VERSION_V1,
        validation = BaseConstant.Dubbo.ENABLE_VALIDATION,
        timeout = BaseConstant.Dubbo.TIMEOUT)
public class LearningAgentFacadeImpl implements LearningAgentFacade {

    private final LearningAgentService learningAgentService;

    @Override
    public LearningAgentBO getLearningAgent(Long goalId) {
        return learningAgentService.getLearningAgent(goalId);
    }
}
