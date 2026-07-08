package io.github.module.appapi.service;

import io.github.framework.core.constant.BaseConstant;
import io.github.module.ai.facade.AiChatFacade;
import io.github.module.ai.model.request.AdminAiChatDTO;
import io.github.module.ai.model.request.AppAiChatDTO;
import io.github.module.ai.model.response.AdminAiChatBO;
import io.github.module.ai.model.response.AdminAiChatStreamChunkBO;
import io.github.module.ai.model.response.AppAiChatBO;
import io.github.module.ai.model.response.AppAiChatStreamChunkBO;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * APP-AI 历史示例对话适配服务.
 *
 * @deprecated 仅用于兼容 app-api 历史示例入口，不属于 Pig AI 后台 MVP 验收链路；
 *             新能力统一从 admin-api -> ai-facade -> ai-service 链路接入。
 */
@Deprecated(since = "1.0.0", forRemoval = false)
@RequiredArgsConstructor
@Service
public class AppAiChatService {

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private AiChatFacade aiChatFacade;

    /**
     * 普通对话.
     */
    public AppAiChatBO chat(AppAiChatDTO dto) {
        return toAppChatBO(aiChatFacade.adminChat(toAdminDto(dto)));
    }

    /**
     * 流式对话.
     */
    public Flux<AppAiChatStreamChunkBO> stream(AppAiChatDTO dto) {
        List<AdminAiChatStreamChunkBO> chunks = aiChatFacade.adminStream(toAdminDto(dto));
        return Flux.fromIterable(chunks == null ? List.of() : chunks).map(this::toAppStreamChunkBO);
    }

    private AdminAiChatDTO toAdminDto(AppAiChatDTO dto) {
        return AdminAiChatDTO.builder()
                .conversationId(dto.getConversationId())
                .modelConfigCode(dto.getModelConfigCode())
                .content(dto.getContent())
                .build();
    }

    private AppAiChatBO toAppChatBO(AdminAiChatBO bo) {
        return AppAiChatBO.builder()
                .conversationId(bo.getConversationId())
                .messageId(bo.getMessageId())
                .modelConfigCode(bo.getModelConfigCode())
                .providerType(bo.getProviderType())
                .modelName(bo.getModelName())
                .answer(bo.getAnswer())
                .build();
    }

    private AppAiChatStreamChunkBO toAppStreamChunkBO(AdminAiChatStreamChunkBO bo) {
        return AppAiChatStreamChunkBO.builder()
                .event(bo.getEvent())
                .messageId(bo.getMessageId())
                .conversationId(bo.getConversationId())
                .modelConfigCode(bo.getModelConfigCode())
                .providerType(bo.getProviderType())
                .modelName(bo.getModelName())
                .content(bo.getContent())
                .finish(bo.getFinish())
                .timestamp(bo.getTimestamp())
                .build();
    }
}
