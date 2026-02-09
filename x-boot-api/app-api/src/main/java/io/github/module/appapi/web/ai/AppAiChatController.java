package io.github.module.appapi.web.ai;

import cn.dev33.satoken.annotation.SaCheckLogin;
import io.github.framework.core.constant.ApiPrefixConstant;
import io.github.module.appapi.util.AppStpUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "AI 对话相关")
@RequiredArgsConstructor
@RequestMapping(ApiPrefixConstant.API_PREFIX_APP + ApiPrefixConstant.VERSION)
@SaCheckLogin(type = AppStpUtil.TYPE)
public class AppAiChatController {

}
