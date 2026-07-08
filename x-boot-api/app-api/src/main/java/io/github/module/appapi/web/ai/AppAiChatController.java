package io.github.module.appapi.web.ai;

import cn.dev33.satoken.annotation.SaCheckLogin;
import io.github.framework.core.constant.ApiPrefixConstant;
import io.github.framework.web.model.response.ApiResult;
import io.github.module.appapi.util.AppStpUtil;
import io.github.module.appapi.service.AppAiChatService;
import io.github.module.ai.model.request.AppAiChatDTO;
import io.github.module.ai.model.response.AppAiChatBO;
import io.github.module.ai.model.response.AppAiChatStreamChunkBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * APP-AI 历史示例对话接口.
 *
 * @deprecated 仅用于兼容 app-api 历史示例入口，不属于 Pig AI 后台 MVP 验收链路；
 *             新能力统一从后台 AI 对话接口接入。
 */
@Deprecated(since = "1.0.0", forRemoval = false)
@RestController
@Tag(name = "APP 历史示例-AI 对话", description = "非 Pig AI 后台 MVP 入口，仅用于兼容 app-api 历史示例")
@RequiredArgsConstructor
@RequestMapping(ApiPrefixConstant.API_PREFIX_APP + ApiPrefixConstant.VERSION)
@SaCheckLogin(type = AppStpUtil.TYPE)
public class AppAiChatController {

    private final AppAiChatService appAiChatService;

    @Operation(summary = "历史示例普通对话（非 MVP）", deprecated = true)
    @PostMapping(value = "/ai/chat")
    public ApiResult<AppAiChatBO> chat(@RequestBody @Valid AppAiChatDTO dto) {
        return ApiResult.data(appAiChatService.chat(dto));
    }

    @Operation(summary = "历史示例流式对话（非 MVP）", deprecated = true)
    @PostMapping(value = "/ai/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<AppAiChatStreamChunkBO>> stream(@RequestBody @Valid AppAiChatDTO dto) {
        return appAiChatService.stream(dto)
                .map(chunk -> ServerSentEvent.builder(chunk)
                        .id(chunk.getMessageId())
                        .event(chunk.getEvent())
                        .build());
    }
}
