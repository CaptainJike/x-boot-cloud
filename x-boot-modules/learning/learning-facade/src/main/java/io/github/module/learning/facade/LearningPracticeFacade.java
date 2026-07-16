package io.github.module.learning.facade;

import io.github.module.learning.model.request.AppSavePracticeAttemptDTO;
import io.github.module.learning.model.response.PracticeAttemptBO;
import io.github.module.learning.model.response.PracticeWorkspaceBO;

/**
 * 练习 Facade.
 */
public interface LearningPracticeFacade {

    /**
     * 获取当前目标的权威练习工作区.
     */
    PracticeWorkspaceBO getWorkspace(Long goalId);

    /**
     * 保存练习草稿或完成记录.
     */
    PracticeAttemptBO saveAttempt(String taskKey, AppSavePracticeAttemptDTO dto);
}
