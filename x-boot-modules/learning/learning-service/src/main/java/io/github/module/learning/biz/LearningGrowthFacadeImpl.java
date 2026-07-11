package io.github.module.learning.biz;

import io.github.framework.core.constant.BaseConstant;
import io.github.module.learning.facade.LearningGrowthFacade;
import io.github.module.learning.model.response.GrowthTimelineBO;
import io.github.module.learning.service.LearningGrowthService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 成长 Facade 实现.
 */
@RequiredArgsConstructor
@DubboService(version = BaseConstant.Version.DUBBO_VERSION_V1,
        validation = BaseConstant.Dubbo.ENABLE_VALIDATION,
        timeout = BaseConstant.Dubbo.TIMEOUT)
public class LearningGrowthFacadeImpl implements LearningGrowthFacade {

    private final LearningGrowthService learningGrowthService;

    @Override
    public GrowthTimelineBO getTimeline() {
        return learningGrowthService.getTimeline();
    }
}
