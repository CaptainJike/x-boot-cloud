package io.github.starter.ai.factory;

import cn.hutool.core.util.StrUtil;
import io.github.framework.core.enums.ModelTypeEnum;
import io.github.starter.ai.enums.AiProviderTypeEnum;
import io.github.starter.ai.service.XBootAiService;
import io.github.starter.ai.vo.AiModelConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
public final class XBootAiFactory {

    /**
     * 阿里云百炼 OpenAI 兼容接口地址.
     */
    private static final String DASHSCOPE_BASE_URL =
            "https://dashscope.aliyuncs.com/compatible-mode/v1";

    /**
     * AI 对话服务.
     */
    private final XBootAiService aiService;

    /**
     * 同步对话调用.
     *
     * @param message 用户消息
     * @param modelConfig 模型配置
     * @return 模型响应内容
     */
    public String chat(final String message,
                       final AiModelConfig modelConfig) {
        if (StrUtil.isBlank(message)) {
            throw new IllegalArgumentException("问题不能为空");
        }
        logModelConfig(modelConfig);
        return aiService.chat(message, modelConfig);
    }

    /**
     * 流式对话调用.
     *
     * @param message 用户消息
     * @param modelConfig 模型配置
     * @return 模型响应流
     */
    public Flux<String> streamChat(final String message,
                                   final AiModelConfig modelConfig) {
        if (StrUtil.isBlank(message)) {
            return Flux.error(new IllegalArgumentException("问题不能为空"));
        }
        logModelConfig(modelConfig);
        return aiService.stream(message, modelConfig);
    }

    /**
     * 兼容旧模型枚举的同步对话调用.
     *
     * @param message 用户消息
     * @param modelTypeEnum 模型类型
     * @return 模型响应内容
     */
    public String chat(final String message,
                       final ModelTypeEnum modelTypeEnum) {
        return chat(message, toDefaultModelConfig(modelTypeEnum));
    }

    /**
     * 兼容旧模型枚举的流式对话调用.
     *
     * @param message 用户消息
     * @param modelTypeEnum 模型类型
     * @return 模型响应流
     */
    public Flux<String> streamChat(final String message,
                                   final ModelTypeEnum modelTypeEnum) {
        return streamChat(message, toDefaultModelConfig(modelTypeEnum));
    }

    private AiModelConfig toDefaultModelConfig(
            final ModelTypeEnum modelTypeEnum) {
        ModelTypeEnum effectiveType = modelTypeEnum != null
                ? modelTypeEnum
                : ModelTypeEnum.OLLAMA;
        return switch (effectiveType) {
            case OPENAI -> new AiModelConfig()
                    .setProviderType(AiProviderTypeEnum.OPENAI)
                    .setModelName("gpt-4o-mini");
            case DEEPSEEK -> new AiModelConfig()
                    .setProviderType(AiProviderTypeEnum.DEEPSEEK)
                    .setModelName("deepseek-chat");
            case OPENAI_COMPATIBLE, DASHSCOPE -> new AiModelConfig()
                    .setProviderType(AiProviderTypeEnum.OPENAI_COMPATIBLE)
                    .setBaseUrl(DASHSCOPE_BASE_URL)
                    .setModelName("qwen-plus");
            case OLLAMA -> new AiModelConfig()
                    .setProviderType(AiProviderTypeEnum.OLLAMA)
                    .setBaseUrl("http://localhost:11434")
                    .setModelName("llama3.2");
        };
    }

    private void logModelConfig(final AiModelConfig modelConfig) {
        if (modelConfig == null) {
            log.info("使用AI模型配置: 默认Ollama");
            return;
        }
        log.info("使用AI模型配置: providerType={}, baseUrl={}, modelName={}",
                modelConfig.getProviderType(),
                modelConfig.getBaseUrl(),
                modelConfig.getModelName());
    }
}
