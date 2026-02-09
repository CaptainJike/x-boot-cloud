package io.github.framework.core.enums;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 模型支持的类型枚举
 */
@Slf4j
@Getter
@AllArgsConstructor
public enum ModelTypeEnum implements BaseEnum<String>{
    /**
     * ollama本地模型
     */
    OLLAMA("OLLAMA", "ollama"),

    /**
     * OpenAi大模型
     */
    OPENAI("OPENAI", "OpenAi"),

    /**
     * 阿里云通义千问
     */
    DASHSCOPE("DASHSCOPE", "dashscope");

    @EnumValue
    private final String value;
    private final String label;


    /**
     * 安全解析字符串到枚举，失败时返回默认值 OLLAMA
     */
    public static ModelTypeEnum safeOf(String input) {
        if (StrUtil.isBlank(input)) {
            return OLLAMA;
        }
        String upper = input.trim().toUpperCase();
        try {
            return ModelTypeEnum.valueOf(upper);
        } catch (IllegalArgumentException e) {
            log.warn("无效的模型类型参数 '{}', 使用默认: OLLAMA", input);
            return OLLAMA;
        }
    }
}
