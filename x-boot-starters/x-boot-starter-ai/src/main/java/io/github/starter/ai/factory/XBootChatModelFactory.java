package io.github.starter.ai.factory;

import cn.hutool.core.util.StrUtil;
import io.github.starter.ai.enums.AiProviderTypeEnum;
import io.github.starter.ai.vo.AiModelConfig;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Component;

@Component
public final class XBootChatModelFactory {

    /**
     * 阿里云百炼 OpenAI 兼容接口地址.
     */
    private static final String DEFAULT_DASHSCOPE_BASE_URL =
            "https://dashscope.aliyuncs.com/compatible-mode/v1";

    /**
     * 默认通义千问模型.
     */
    private static final String DEFAULT_QWEN_MODEL = "qwen-plus";

    /**
     * 默认 OpenAI 模型.
     */
    private static final String DEFAULT_OPENAI_MODEL = "gpt-4o-mini";

    /**
     * 默认 DeepSeek 接口地址.
     */
    private static final String DEFAULT_DEEPSEEK_BASE_URL =
            "https://api.deepseek.com";

    /**
     * 默认 DeepSeek 模型.
     */
    private static final String DEFAULT_DEEPSEEK_MODEL = "deepseek-chat";

    /**
     * 默认 Ollama 本地接口地址.
     */
    private static final String DEFAULT_OLLAMA_BASE_URL =
            "http://localhost:11434";

    /**
     * 默认 Ollama 模型.
     */
    private static final String DEFAULT_OLLAMA_MODEL = "llama3.2";

    /**
     * 根据模型配置创建 Spring AI ChatModel.
     *
     * @param modelConfig 模型配置
     * @return Spring AI ChatModel
     */
    public ChatModel create(final AiModelConfig modelConfig) {
        AiModelConfig effectiveConfig = modelConfig != null
                ? modelConfig
                : new AiModelConfig();
        if (Boolean.FALSE.equals(effectiveConfig.getEnabled())) {
            throw new IllegalArgumentException("模型配置已禁用");
        }
        AiProviderTypeEnum providerType =
                effectiveConfig.getProviderType() != null
                ? effectiveConfig.getProviderType()
                : AiProviderTypeEnum.OLLAMA;
        return switch (providerType) {
            case OPENAI -> createOpenAi(effectiveConfig);
            case OPENAI_COMPATIBLE -> createOpenAiCompatible(effectiveConfig);
            case DEEPSEEK -> createDeepSeek(effectiveConfig);
            case OLLAMA -> createOllama(effectiveConfig);
        };
    }

    private ChatModel createOpenAi(final AiModelConfig modelConfig) {
        OpenAiChatOptions.Builder options = OpenAiChatOptions.builder()
                .apiKey(resolveApiKey(modelConfig, AiProviderTypeEnum.OPENAI))
                .model(resolveModelName(modelConfig, DEFAULT_OPENAI_MODEL));
        if (StrUtil.isNotBlank(modelConfig.getBaseUrl())) {
            options.baseUrl(modelConfig.getBaseUrl());
        }
        applyCommonOptions(options, modelConfig);
        return OpenAiChatModel.builder()
                .options(options.build())
                .build();
    }

    private ChatModel createOpenAiCompatible(
            final AiModelConfig modelConfig) {
        OpenAiChatOptions.Builder options = OpenAiChatOptions.builder()
                .apiKey(resolveApiKey(modelConfig,
                        AiProviderTypeEnum.OPENAI_COMPATIBLE))
                .baseUrl(resolveBaseUrl(modelConfig,
                        DEFAULT_DASHSCOPE_BASE_URL))
                .model(resolveModelName(modelConfig, DEFAULT_QWEN_MODEL));
        applyCommonOptions(options, modelConfig);
        return OpenAiChatModel.builder()
                .options(options.build())
                .build();
    }

    private ChatModel createDeepSeek(final AiModelConfig modelConfig) {
        DeepSeekApi deepSeekApi = DeepSeekApi.builder()
                .apiKey(resolveApiKey(modelConfig, AiProviderTypeEnum.DEEPSEEK))
                .baseUrl(resolveBaseUrl(modelConfig, DEFAULT_DEEPSEEK_BASE_URL))
                .build();
        DeepSeekChatOptions.Builder options = DeepSeekChatOptions.builder()
                .model(resolveModelName(modelConfig, DEFAULT_DEEPSEEK_MODEL));
        if (modelConfig.getTemperature() != null) {
            options.temperature(modelConfig.getTemperature());
        }
        return DeepSeekChatModel.builder()
                .deepSeekApi(deepSeekApi)
                .options(options.build())
                .build();
    }

    private ChatModel createOllama(final AiModelConfig modelConfig) {
        OllamaApi ollamaApi = OllamaApi.builder()
                .baseUrl(resolveBaseUrl(modelConfig, DEFAULT_OLLAMA_BASE_URL))
                .build();
        OllamaChatOptions.Builder options = OllamaChatOptions.builder()
                .model(resolveModelName(modelConfig, DEFAULT_OLLAMA_MODEL));
        if (modelConfig.getTemperature() != null) {
            options.temperature(modelConfig.getTemperature());
        }
        return OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .options(options.build())
                .build();
    }

    private void applyCommonOptions(final OpenAiChatOptions.Builder options,
                                    final AiModelConfig modelConfig) {
        if (modelConfig.getTemperature() != null) {
            options.temperature(modelConfig.getTemperature());
        }
        if (modelConfig.getTimeout() != null) {
            options.timeout(modelConfig.getTimeout());
        }
    }

    private String resolveApiKey(final AiModelConfig modelConfig,
                                 final AiProviderTypeEnum providerType) {
        if (StrUtil.isNotBlank(modelConfig.getApiKey())) {
            return modelConfig.getApiKey();
        }
        String apiKey = providerType == AiProviderTypeEnum.OLLAMA
                ? StrUtil.EMPTY
                : null;
        if (StrUtil.isBlank(apiKey)
                && providerType != AiProviderTypeEnum.OLLAMA) {
            throw new IllegalArgumentException("模型配置缺少 apiKey");
        }
        return apiKey;
    }

    private String resolveBaseUrl(final AiModelConfig modelConfig,
                                  final String defaultBaseUrl) {
        return StrUtil.blankToDefault(modelConfig.getBaseUrl(), defaultBaseUrl);
    }

    private String resolveModelName(final AiModelConfig modelConfig,
                                    final String defaultModelName) {
        return StrUtil.blankToDefault(modelConfig.getModelName(),
                defaultModelName);
    }

}
