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

@RestController
@Tag(name = "AI 对话相关")
@RequiredArgsConstructor
@RequestMapping(ApiPrefixConstant.API_PREFIX_APP + ApiPrefixConstant.VERSION)
@SaCheckLogin(type = AppStpUtil.TYPE)
public class AppAiChatController {

    private final AppAiChatService appAiChatService;

    @Operation(summary = "普通对话")
    @PostMapping(value = "/ai/chat")
    public ApiResult<AppAiChatBO> chat(@RequestBody @Valid AppAiChatDTO dto) {
        return ApiResult.data(appAiChatService.chat(dto));
    }

    @Operation(summary = "流式对话")
    @PostMapping(value = "/ai/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<AppAiChatStreamChunkBO>> stream(@RequestBody @Valid AppAiChatDTO dto) {
        return appAiChatService.stream(dto)
                .map(chunk -> ServerSentEvent.builder(chunk)
                        .id(chunk.getMessageId())
                        .event(chunk.getEvent())
                        .build());
    }
}
