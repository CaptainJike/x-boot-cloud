package io.github.module.ai.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import io.github.framework.core.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI 模块错误枚举类.
 */
@AllArgsConstructor
@Getter
public enum AiErrorEnum implements BaseEnum<Integer> {

    INVALID_ID(400, "无效ID"),
    INVALID_CODE(400, "无效AI模型配置编码"),
    NO_ENABLED_MODEL_CONFIG(400, "当前租户没有可用AI模型配置"),
    INVALID_PROVIDER_TYPE(400, "无效AI模型供应商类型"),
    DISABLED_MODEL_CONFIG(400, "AI模型配置已禁用"),
    MISSING_API_KEY(400, "AI模型配置缺少可用API Key"),
    DUPLICATE_MODEL_CONFIG(400, "已存在相同AI模型配置，请重新输入"),;

    @EnumValue
    private final Integer value;
    private final String label;
}
