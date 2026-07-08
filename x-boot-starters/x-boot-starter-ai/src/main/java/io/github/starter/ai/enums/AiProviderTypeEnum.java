package io.github.starter.ai.enums;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Spring AI 对话模型供应商类型.
 */
@Getter
@AllArgsConstructor
public enum AiProviderTypeEnum {

    /**
     * OpenAI 官方接口.
     */
    OPENAI("OPENAI", "OpenAI"),

    /**
     * OpenAI 兼容接口，例如阿里云百炼通义千问兼容模式.
     */
    OPENAI_COMPATIBLE("OPENAI_COMPATIBLE", "OpenAI兼容接口"),

    /**
     * DeepSeek 官方接口.
     */
    DEEPSEEK("DEEPSEEK", "DeepSeek"),

    /**
     * 智谱 AI OpenAI 兼容接口.
     */
    ZHIPU("ZHIPU", "智谱AI"),

    /**
     * Ollama 本地模型.
     */
    OLLAMA("OLLAMA", "Ollama");

    /**
     * 枚举值.
     */
    private final String value;

    /**
     * 展示名称.
     */
    private final String label;

    /**
     * 安全解析 provider 类型，失败时返回 Ollama.
     *
     * @param input provider 类型字符串
     * @return provider 类型枚举
     */
    public static AiProviderTypeEnum safeOf(final String input) {
        if (StrUtil.isBlank(input)) {
            return OLLAMA;
        }
        String upper = input.trim().toUpperCase();
        if (StrUtil.equalsAny(upper,
                "DASHSCOPE", "DASH_SCOPE", "QWEN", "TONGYI", "TONG_YI")) {
            return OPENAI_COMPATIBLE;
        }
        if (StrUtil.equalsAny(upper, "ZHI_PU", "BIGMODEL", "BIG_MODEL")) {
            return ZHIPU;
        }
        try {
            return AiProviderTypeEnum.valueOf(upper);
        } catch (IllegalArgumentException e) {
            return OLLAMA;
        }
    }
}
