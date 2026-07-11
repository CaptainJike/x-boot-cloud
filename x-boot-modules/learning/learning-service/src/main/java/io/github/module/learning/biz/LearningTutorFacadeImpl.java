package io.github.module.learning.biz;

import io.github.framework.core.constant.BaseConstant;
import io.github.module.learning.facade.LearningTutorFacade;
import io.github.module.learning.model.request.AppCreateTutorSessionDTO;
import io.github.module.learning.model.request.AppSubmitTutorTurnDTO;
import io.github.module.learning.model.response.TutorSessionBO;
import io.github.module.learning.model.response.TutorTurnBO;
import io.github.module.learning.service.LearningTutorService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * Tutor Facade 实现.
 */
@RequiredArgsConstructor
@DubboService(version = BaseConstant.Version.DUBBO_VERSION_V1,
        validation = BaseConstant.Dubbo.ENABLE_VALIDATION,
        timeout = BaseConstant.Dubbo.AI_TIMEOUT)
public class LearningTutorFacadeImpl implements LearningTutorFacade {

    private final LearningTutorService learningTutorService;

    @Override
    public TutorSessionBO createSession(AppCreateTutorSessionDTO dto) {
        return learningTutorService.createSession(dto);
    }

    @Override
    public TutorTurnBO submitTurn(Long sessionId, AppSubmitTutorTurnDTO dto) {
        return learningTutorService.submitTurn(sessionId, dto);
    }
}
