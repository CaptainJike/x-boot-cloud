package io.github.framework.core.enums;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 模型支持的类型枚举.
 */
@Slf4j
@Getter
@AllArgsConstructor
public enum ModelTypeEnum implements BaseEnum<String> {
    /**
     * ollama本地模型.
     */
    OLLAMA("OLLAMA", "ollama"),

    /**
     * OpenAi大模型.
     */
    OPENAI("OPENAI", "OpenAi"),

    /**
     * DeepSeek大模型.
     */
    DEEPSEEK("DEEPSEEK", "DeepSeek"),

    /**
     * OpenAI兼容接口.
     */
    OPENAI_COMPATIBLE("OPENAI_COMPATIBLE", "OpenAI-compatible"),

    /**
     * 阿里云通义千问，兼容旧参数，实际按 OpenAI 兼容接口调用.
     */
    @Deprecated
    DASHSCOPE("DASHSCOPE", "dashscope");

    /**
     * 枚举值.
     */
    @EnumValue
    private final String value;

    /**
     * 展示名称.
     */
    private final String label;

    /**
     * 安全解析字符串到枚举，失败时返回默认值 OLLAMA.
     *
     * @param input 模型类型字符串
     * @return 模型类型枚举
     */
    public static ModelTypeEnum safeOf(final String input) {
        if (StrUtil.isBlank(input)) {
            return OLLAMA;
        }
        String upper = input.trim().toUpperCase();
        if (StrUtil.equalsAny(upper,
                "QWEN", "TONGYI", "TONG_YI", "DASH_SCOPE")) {
            return OPENAI_COMPATIBLE;
        }
        try {
            return ModelTypeEnum.valueOf(upper);
        } catch (final IllegalArgumentException e) {
            log.warn("无效的模型类型参数 '{}', 使用默认: OLLAMA", input);
            return OLLAMA;
        }
    }
}
