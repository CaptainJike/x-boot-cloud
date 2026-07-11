package io.github.module.learning.facade;

import io.github.module.learning.model.request.AppCreateTutorSessionDTO;
import io.github.module.learning.model.request.AppSubmitTutorTurnDTO;
import io.github.module.learning.model.response.TutorSessionBO;
import io.github.module.learning.model.response.TutorTurnBO;

/**
 * Tutor Facade.
 */
public interface LearningTutorFacade {

    /**
     * 创建 Tutor 会话.
     */
    TutorSessionBO createSession(AppCreateTutorSessionDTO dto);

    /**
     * 提交 Tutor 轮次.
     */
    TutorTurnBO submitTurn(Long sessionId, AppSubmitTutorTurnDTO dto);
}
