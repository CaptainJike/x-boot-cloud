package io.github.module.learning.facade;

import io.github.module.learning.model.request.AppSubmitDailyReflectionDTO;
import io.github.module.learning.model.response.DailyReflectionBO;

/**
 * 反思 Facade.
 */
public interface LearningReflectionFacade {

    /**
     * 提交每日反思.
     */
    DailyReflectionBO submitDailyReflection(AppSubmitDailyReflectionDTO dto);
}
