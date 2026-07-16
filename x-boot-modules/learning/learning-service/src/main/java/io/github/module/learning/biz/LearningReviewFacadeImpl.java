package io.github.module.learning.biz;

import io.github.framework.core.constant.BaseConstant;
import io.github.module.learning.facade.LearningReviewFacade;
import io.github.module.learning.model.request.AppSaveReviewAttemptDTO;
import io.github.module.learning.model.response.ReviewAttemptBO;
import io.github.module.learning.model.response.ReviewWorkspaceBO;
import io.github.module.learning.service.LearningReviewService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 复盘 Facade 实现.
 */
@RequiredArgsConstructor
@DubboService(version = BaseConstant.Version.DUBBO_VERSION_V1,
        validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
public class LearningReviewFacadeImpl implements LearningReviewFacade {

    private final LearningReviewService learningReviewService;

    @Override
    public ReviewWorkspaceBO getWorkspace(Long goalId) {
        return learningReviewService.getWorkspace(goalId);
    }

    @Override
    public ReviewAttemptBO saveAttempt(String taskKey, AppSaveReviewAttemptDTO dto) {
        return learningReviewService.saveAttempt(taskKey, dto);
    }
}
