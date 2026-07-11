package io.github.module.learning.biz;

import io.github.framework.core.constant.BaseConstant;
import io.github.module.learning.facade.LearningMapFacade;
import io.github.module.learning.model.response.LearningMapBO;
import io.github.module.learning.service.LearningGoalService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 学习地图 Facade 实现.
 */
@RequiredArgsConstructor
@DubboService(version = BaseConstant.Version.DUBBO_VERSION_V1,
        validation = BaseConstant.Dubbo.ENABLE_VALIDATION,
        timeout = BaseConstant.Dubbo.TIMEOUT)
public class LearningMapFacadeImpl implements LearningMapFacade {

    private final LearningGoalService learningGoalService;

    @Override
    public LearningMapBO getMapByGoalId(Long goalId) {
        return learningGoalService.getMapByGoalId(goalId);
    }
}
