package io.github.module.appapi.service;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.enums.EnabledStatusEnum;
import io.github.framework.core.exception.BusinessException;
import io.github.module.ai.enums.AiErrorEnum;
import io.github.module.ai.facade.AiModelConfigFacade;
import io.github.module.ai.model.request.AppAiChatDTO;
import io.github.module.ai.model.response.AiModelConfigBO;
import io.github.module.ai.model.response.AppAiChatBO;
import io.github.module.ai.model.response.AppAiChatStreamChunkBO;
import io.github.starter.ai.enums.AiProviderTypeEnum;
import io.github.starter.ai.factory.XBootAiFactory;
import io.github.starter.ai.vo.AiModelConfig;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.UUID;

/**
 * APP-AI 对话编排服务.
 */
@RequiredArgsConstructor
@Service
public class AppAiChatService {

    public static final String EVENT_MESSAGE = "message";
    public static final String EVENT_DONE = "done";
    public static final String EVENT_ERROR = "error";
    public static final String DONE_MARKER = "[DONE]";

    private final XBootAiFactory xBootAiFactory;

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private AiModelConfigFacade aiModelConfigFacade;

    /**
     * 普通对话.
     */
    public AppAiChatBO chat(AppAiChatDTO dto) {
        RuntimeChatContext context = buildContext(dto, resolveConversationId(dto.getConversationId()));
        String answer = xBootAiFactory.chat(dto.getContent(), context.runtimeConfig());

        return AppAiChatBO.builder()
                .conversationId(context.conversationId())
                .modelConfigCode(context.modelConfig().getCode())
                .providerType(context.modelConfig().getProviderType())
                .modelName(context.modelConfig().getModelName())
                .answer(answer)
                .build();
    }

    /**
     * 流式对话.
     */
    public Flux<AppAiChatStreamChunkBO> stream(AppAiChatDTO dto) {
        String conversationId = resolveConversationId(dto.getConversationId());
        String messageId = newId();
        return Flux.defer(() -> {
            RuntimeChatContext context = buildContext(dto, conversationId);
            return xBootAiFactory.streamChat(dto.getContent(), context.runtimeConfig())
                    .map(content -> messageChunk(context, messageId, content))
                    .concatWithValues(doneChunk(context, messageId));
        }).onErrorResume(ex -> Flux.just(errorChunk(conversationId, messageId, ex)));
    }

    private RuntimeChatContext buildContext(AppAiChatDTO dto, String conversationId) {
        AiModelConfigBO modelConfig = resolveModelConfig(dto.getModelConfigCode());
        return new RuntimeChatContext(conversationId, modelConfig, toRuntimeConfig(modelConfig));
    }

    private AiModelConfigBO resolveModelConfig(String modelConfigCode) {
        String cleanCode = clean(modelConfigCode);
        if (StrUtil.isNotBlank(cleanCode)) {
            return aiModelConfigFacade.getEnabledConfigByCode(cleanCode, true);
        }

        AiModelConfigBO defaultConfig = aiModelConfigFacade.getDefaultEnabledConfig();
        AiErrorEnum.NO_ENABLED_MODEL_CONFIG.assertNotNull(defaultConfig);
        return defaultConfig;
    }

    private AiModelConfig toRuntimeConfig(AiModelConfigBO bo) {
        AiErrorEnum.NO_ENABLED_MODEL_CONFIG.assertNotNull(bo);
        AiProviderTypeEnum providerType = parseProviderType(bo.getProviderType());
        String apiKey = resolveApiKey(bo);
        if (providerType != AiProviderTypeEnum.OLLAMA) {
            AiErrorEnum.MISSING_API_KEY.assertNotBlank(apiKey);
        }

        return new AiModelConfig()
                .setProviderType(providerType)
                .setBaseUrl(bo.getBaseUrl())
                .setApiKey(apiKey)
                .setModelName(bo.getModelName())
                .setTemperature(bo.getTemperature())
                .setTimeout(toDuration(bo.getTimeoutSeconds()))
                .setEnabled(EnabledStatusEnum.ENABLED.getValue().equals(bo.getStatus()));
    }

    private AiProviderTypeEnum parseProviderType(String providerType) {
        String cleanProviderType = clean(providerType);
        AiErrorEnum.INVALID_PROVIDER_TYPE.assertNotBlank(cleanProviderType);
        if (StrUtil.equalsAnyIgnoreCase(cleanProviderType,
                "DASHSCOPE", "DASH_SCOPE", "QWEN", "TONGYI", "TONG_YI")) {
            return AiProviderTypeEnum.OPENAI_COMPATIBLE;
        }
        try {
            return AiProviderTypeEnum.valueOf(cleanProviderType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(AiErrorEnum.INVALID_PROVIDER_TYPE);
        }
    }

    private AppAiChatStreamChunkBO messageChunk(RuntimeChatContext context, String messageId, String content) {
        return chunk(context, messageId, EVENT_MESSAGE, content, false);
    }

    private AppAiChatStreamChunkBO doneChunk(RuntimeChatContext context, String messageId) {
        return chunk(context, messageId, EVENT_DONE, DONE_MARKER, true);
    }

    private AppAiChatStreamChunkBO errorChunk(String conversationId, String messageId, Throwable ex) {
        return AppAiChatStreamChunkBO.builder()
                .event(EVENT_ERROR)
                .messageId(messageId)
                .conversationId(conversationId)
                .content(StrUtil.blankToDefault(ex.getMessage(), "AI对话调用失败"))
                .finish(true)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    private AppAiChatStreamChunkBO chunk(RuntimeChatContext context,
                                         String messageId,
                                         String event,
                                         String content,
                                         boolean finish) {
        return AppAiChatStreamChunkBO.builder()
                .event(event)
                .messageId(messageId)
                .conversationId(context.conversationId())
                .modelConfigCode(context.modelConfig().getCode())
                .providerType(context.modelConfig().getProviderType())
                .modelName(context.modelConfig().getModelName())
                .content(content)
                .finish(finish)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    private String resolveApiKey(AiModelConfigBO bo) {
        return bo.getApiKey();
    }

    private Duration toDuration(Long timeoutSeconds) {
        if (timeoutSeconds == null) {
            return null;
        }
        return Duration.ofSeconds(timeoutSeconds);
    }

    private String resolveConversationId(String conversationId) {
        String cleanConversationId = clean(conversationId);
        return StrUtil.blankToDefault(cleanConversationId, newId());
    }

    private String newId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String clean(String value) {
        return CharSequenceUtil.cleanBlank(value);
    }

    private record RuntimeChatContext(
            String conversationId,
            AiModelConfigBO modelConfig,
            AiModelConfig runtimeConfig
    ) {
    }
}
