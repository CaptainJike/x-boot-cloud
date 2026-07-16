package io.github.module.learning.biz;

import io.github.framework.core.constant.BaseConstant;
import io.github.module.learning.facade.LearningLearnerFacade;
import io.github.module.learning.model.request.AppGithubLoginDTO;
import io.github.module.learning.model.request.AppEmailLoginDTO;
import io.github.module.learning.model.request.AppEmailRegisterDTO;
import io.github.module.learning.model.response.AppLearnerLoginBO;
import io.github.module.learning.service.LearningLearnerService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 学习者账号 Facade 实现.
 */
@RequiredArgsConstructor
@DubboService(version = BaseConstant.Version.DUBBO_VERSION_V1,
        validation = BaseConstant.Dubbo.ENABLE_VALIDATION,
        timeout = BaseConstant.Dubbo.TIMEOUT)
public class LearningLearnerFacadeImpl implements LearningLearnerFacade {

    private final LearningLearnerService learningLearnerService;

    @Override
    public AppLearnerLoginBO appGithubLogin(AppGithubLoginDTO dto) {
        return learningLearnerService.appGithubLogin(dto);
    }

    @Override
    public AppLearnerLoginBO appEmailLogin(AppEmailLoginDTO dto) {
        return learningLearnerService.appEmailLogin(dto);
    }

    @Override
    public AppLearnerLoginBO appEmailRegister(AppEmailRegisterDTO dto) {
        return learningLearnerService.appEmailRegister(dto);
    }
}
