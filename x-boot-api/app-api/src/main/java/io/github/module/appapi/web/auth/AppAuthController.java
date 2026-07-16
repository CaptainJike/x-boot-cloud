package io.github.module.appapi.web.auth;


import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.context.TenantContext;
import io.github.framework.core.context.TenantContextHolder;
import io.github.framework.core.context.UserContext;
import io.github.framework.core.context.UserContextHolder;
import io.github.framework.core.constant.ApiPrefixConstant;
import io.github.framework.web.model.response.ApiResult;
import io.github.module.learning.facade.LearningLearnerFacade;
import io.github.module.appapi.model.interior.GithubUserProfile;
import io.github.module.appapi.model.request.AppEmailAuthDTO;
import io.github.module.appapi.model.request.AppEmailCodeScene;
import io.github.module.appapi.model.request.AppSendEmailCodeDTO;
import io.github.module.appapi.model.response.AppAuthLoginVO;
import io.github.module.appapi.service.AppEmailCodeService;
import io.github.module.appapi.service.AppGithubOAuthService;
import io.github.module.appapi.util.AppStpUtil;
import io.github.module.learning.model.request.AppGithubLoginDTO;
import io.github.module.learning.model.response.AppLearnerLoginBO;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;


@RestController
@Tag(name = "APP 鉴权接口")
@RequiredArgsConstructor
@RequestMapping({ApiPrefixConstant.API_PREFIX_APP + ApiPrefixConstant.VERSION, "/app/v1"})
public class AppAuthController {

    private final AppGithubOAuthService appGithubOAuthService;

    private final AppEmailCodeService appEmailCodeService;

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private LearningLearnerFacade learningLearnerFacade;

    @Operation(summary = "获取 GitHub 登录地址")
    @GetMapping("/auth/github/start")
    public ApiResult<String> githubStart() {
        return ApiResult.data(appGithubOAuthService.buildAuthorizeUrl());
    }

    @Operation(summary = "GitHub 登录回调")
    @GetMapping("/auth/github/callback")
    public ApiResult<AppAuthLoginVO> githubCallback(
            @Parameter(description = "GitHub OAuth code") @RequestParam("code") String code,
            @Parameter(description = "GitHub OAuth state") @RequestParam("state") String state) {
        GithubUserProfile profile = appGithubOAuthService.exchangeCodeForUser(code, state);
        AppLearnerLoginBO loginBO = learningLearnerFacade.appGithubLogin(AppGithubLoginDTO.builder()
                .githubUserId(String.valueOf(profile.getId()))
                .githubLogin(profile.getLogin())
                .githubName(profile.getName())
                .githubAvatarUrl(profile.getAvatarUrl())
                .githubEmail(profile.getEmail())
                .build());

        return ApiResult.data(login(loginBO));
    }

    @Operation(summary = "发送邮箱验证码")
    @PostMapping("/auth/email/code")
    public ApiResult<Void> sendEmailCode(@RequestBody @Valid AppSendEmailCodeDTO dto) {
        appEmailCodeService.sendCode(dto.getEmail(), dto.getScene());
        return ApiResult.success();
    }

    @Operation(summary = "邮箱验证码登录")
    @PostMapping("/auth/email/login")
    public ApiResult<AppAuthLoginVO> emailLogin(@RequestBody @Valid AppEmailAuthDTO dto) {
        appEmailCodeService.verifyCode(dto.getEmail(), dto.getCode(), AppEmailCodeScene.LOGIN);
        AppLearnerLoginBO loginBO = learningLearnerFacade.appEmailLogin(
                io.github.module.learning.model.request.AppEmailLoginDTO.builder()
                        .email(dto.getEmail())
                        .build());
        return ApiResult.data(login(loginBO));
    }

    @Operation(summary = "邮箱验证码注册")
    @PostMapping("/auth/email/register")
    public ApiResult<AppAuthLoginVO> emailRegister(@RequestBody @Valid AppEmailAuthDTO dto) {
        appEmailCodeService.verifyCode(dto.getEmail(), dto.getCode(), AppEmailCodeScene.REGISTER);
        AppLearnerLoginBO loginBO = learningLearnerFacade.appEmailRegister(
                io.github.module.learning.model.request.AppEmailRegisterDTO.builder()
                        .email(dto.getEmail())
                        .nickname(dto.getNickname())
                        .build());
        return ApiResult.data(login(loginBO));
    }

    private AppAuthLoginVO login(AppLearnerLoginBO loginBO) {
        UserContext userContext = UserContext.builder()
                .userId(loginBO.getUserId())
                .userName(loginBO.getUsername())
                .userPhoneNo(loginBO.getPhoneNo())
                .userTypeStr("LEARNER")
                .rolesIds(Set.of())
                .roles(List.of("LEARNER"))
                .build();
        TenantContext tenantContext = loginBO.getTenantContext() == null
                ? new TenantContext() : loginBO.getTenantContext();

        UserContextHolder.setUserContext(userContext);
        TenantContextHolder.setTenantContext(tenantContext);
        try {
            AppStpUtil.login(loginBO.getUserId());
            AppStpUtil.getSession().set(UserContext.CAMEL_NAME, userContext);
            AppStpUtil.getSession().set(TenantContext.CAMEL_NAME, tenantContext);
        } finally {
            UserContextHolder.clear();
            TenantContextHolder.clear();
        }

        return AppAuthLoginVO.builder()
                .tokenName(AppStpUtil.getTokenName())
                .tokenValue(AppStpUtil.getTokenValue())
                .userId(loginBO.getUserId())
                .username(loginBO.getUsername())
                .nickname(loginBO.getNickname())
                .email(loginBO.getEmail())
                .avatarUrl(loginBO.getAvatarUrl())
                .build();
    }

    @Operation(summary = "登出")
    @PostMapping("/auth/logout")
    public ApiResult<Void> logout() {
        AppStpUtil.logout();
        return ApiResult.success();
    }
}
