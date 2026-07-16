package io.github.module.learning.facade;

import io.github.module.learning.model.request.AppSaveReviewAttemptDTO;
import io.github.module.learning.model.response.ReviewAttemptBO;
import io.github.module.learning.model.response.ReviewWorkspaceBO;

/**
 * 复盘 Facade.
 */
public interface LearningReviewFacade {

    /**
     * 获取当前目标的权威复盘工作区.
     */
    ReviewWorkspaceBO getWorkspace(Long goalId);

    /**
     * 保存复盘草稿或完成记录.
     */
    ReviewAttemptBO saveAttempt(String taskKey, AppSaveReviewAttemptDTO dto);
}
