package io.github.module.learning.facade;

import io.github.module.learning.model.request.AppGithubLoginDTO;
import io.github.module.learning.model.request.AppEmailLoginDTO;
import io.github.module.learning.model.request.AppEmailRegisterDTO;
import io.github.module.learning.model.response.AppLearnerLoginBO;

/**
 * 学习者账号 Facade.
 */
public interface LearningLearnerFacade {

    /**
     * APP 侧 GitHub 授权登录/注册.
     */
    AppLearnerLoginBO appGithubLogin(AppGithubLoginDTO dto);

    /**
     * APP 侧邮箱验证码登录.
     */
    AppLearnerLoginBO appEmailLogin(AppEmailLoginDTO dto);

    /**
     * APP 侧邮箱注册并登录.
     */
    AppLearnerLoginBO appEmailRegister(AppEmailRegisterDTO dto);
}
