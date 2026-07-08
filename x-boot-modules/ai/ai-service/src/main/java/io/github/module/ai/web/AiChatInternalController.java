package io.github.module.ai.web;

import cn.hutool.core.util.StrUtil;
import io.github.framework.core.context.TenantContextHolder;
import io.github.framework.core.context.UserContextHolder;
import io.github.framework.core.exception.BusinessException;
import io.github.module.ai.model.request.AdminAiChatStreamRequestDTO;
import io.github.module.ai.model.response.AdminAiChatStreamChunkBO;
import io.github.module.ai.service.AiChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * AI 服务内部流式对话入口，供 API 层代理 SSE，不走 Dubbo 序列化.
 */
@RequiredArgsConstructor
@RequestMapping("/internal-api/v1")
@RestController
public class AiChatInternalController {

    public static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

    private final AiChatService aiChatService;

    @Value("${x.ai.stream.internal-token:}")
    private String internalToken;

    @PostMapping(value = "/ai/chats/stream",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<AdminAiChatStreamChunkBO>> stream(
            @RequestHeader(value = INTERNAL_TOKEN_HEADER, required = false) String requestToken,
            @RequestBody @Valid AdminAiChatStreamRequestDTO request) {
        checkInternalToken(requestToken);
        UserContextHolder.setUserContext(request.getUserContext());
        TenantContextHolder.setTenantContext(request.getTenantContext());
        try {
            return aiChatService.adminStream(request.getChat())
                    .map(this::toServerSentEvent);
        } finally {
            UserContextHolder.clear();
            TenantContextHolder.clear();
        }
    }

    private void checkInternalToken(String requestToken) {
        if (StrUtil.isBlank(internalToken)) {
            return;
        }
        if (!StrUtil.equals(internalToken, requestToken)) {
            throw new BusinessException(401, "AI流式内部接口认证失败");
        }
    }

    private ServerSentEvent<AdminAiChatStreamChunkBO> toServerSentEvent(AdminAiChatStreamChunkBO chunk) {
        ServerSentEvent.Builder<AdminAiChatStreamChunkBO> builder = ServerSentEvent.builder(chunk);
        if (chunk == null) {
            return builder.build();
        }
        if (chunk.getMessageId() != null) {
            builder.id(chunk.getMessageId());
        }
        if (chunk.getEvent() != null) {
            builder.event(chunk.getEvent());
        }
        return builder.build();
    }
}
