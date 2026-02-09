package io.github.module.appapi.web.auth;


import io.github.framework.core.constant.ApiPrefixConstant;
import io.github.framework.web.model.response.ApiResult;
import io.github.module.appapi.util.AppStpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Tag(name = "APP 鉴权接口")
@RequiredArgsConstructor
@RequestMapping(ApiPrefixConstant.API_PREFIX_APP + ApiPrefixConstant.VERSION)
public class AppAuthController {

    @Operation(summary = "登录")
    @PostMapping("/auth/login")
    public ApiResult<String> login() {
        AppStpUtil.login(123);
        return ApiResult.success(AppStpUtil.getTokenValue());
    }
}
