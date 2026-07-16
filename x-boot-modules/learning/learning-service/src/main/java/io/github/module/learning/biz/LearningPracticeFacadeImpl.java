package io.github.module.learning.biz;

import io.github.framework.core.constant.BaseConstant;
import io.github.module.learning.facade.LearningPracticeFacade;
import io.github.module.learning.model.request.AppSavePracticeAttemptDTO;
import io.github.module.learning.model.response.PracticeAttemptBO;
import io.github.module.learning.model.response.PracticeWorkspaceBO;
import io.github.module.learning.service.LearningPracticeService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 练习 Facade 实现.
 */
@RequiredArgsConstructor
@DubboService(version = BaseConstant.Version.DUBBO_VERSION_V1,
        validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
public class LearningPracticeFacadeImpl implements LearningPracticeFacade {

    private final LearningPracticeService learningPracticeService;

    @Override
    public PracticeWorkspaceBO getWorkspace(Long goalId) {
        return learningPracticeService.getWorkspace(goalId);
    }

    @Override
    public PracticeAttemptBO saveAttempt(String taskKey, AppSavePracticeAttemptDTO dto) {
        return learningPracticeService.saveAttempt(taskKey, dto);
    }
}
