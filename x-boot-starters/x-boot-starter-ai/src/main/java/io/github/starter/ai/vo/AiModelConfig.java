package io.github.starter.ai.vo;

import io.github.starter.ai.enums.AiProviderTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.Duration;

@Data
@Accessors(chain = true)
@Schema(description = "AI模型配置")
public class AiModelConfig implements Serializable {

    /**
     * 供应商类型.
     */
    @Schema(description = "供应商类型")
    private AiProviderTypeEnum providerType;

    /**
     * 模型服务地址.
     */
    @Schema(description = "模型服务地址")
    private String baseUrl;

    /**
     * 模型服务密钥.
     */
    @Schema(description = "模型服务密钥")
    private String apiKey;

    /**
     * 模型名称.
     */
    @Schema(description = "模型名称")
    private String modelName;

    /**
     * 温度参数.
     */
    @Schema(description = "温度参数")
    private Double temperature;

    /**
     * 调用超时时间.
     */
    @Schema(description = "调用超时时间")
    private Duration timeout;

    /**
     * 是否启用.
     */
    @Schema(description = "是否启用")
    private Boolean enabled = Boolean.TRUE;
}
