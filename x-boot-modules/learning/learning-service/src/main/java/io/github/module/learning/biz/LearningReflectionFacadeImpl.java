package io.github.module.learning.biz;

import io.github.framework.core.constant.BaseConstant;
import io.github.module.learning.facade.LearningReflectionFacade;
import io.github.module.learning.model.request.AppSubmitDailyReflectionDTO;
import io.github.module.learning.model.response.DailyReflectionBO;
import io.github.module.learning.service.LearningReflectionService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 反思 Facade 实现.
 */
@RequiredArgsConstructor
@DubboService(version = BaseConstant.Version.DUBBO_VERSION_V1,
        validation = BaseConstant.Dubbo.ENABLE_VALIDATION,
        timeout = BaseConstant.Dubbo.AI_TIMEOUT)
public class LearningReflectionFacadeImpl implements LearningReflectionFacade {

    private final LearningReflectionService learningReflectionService;

    @Override
    public DailyReflectionBO submitDailyReflection(AppSubmitDailyReflectionDTO dto) {
        return learningReflectionService.submitDailyReflection(dto);
    }
}
