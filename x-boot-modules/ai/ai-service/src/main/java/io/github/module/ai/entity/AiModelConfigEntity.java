package io.github.module.ai.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.framework.crud.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

/**
 * AI 模型配置.
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName(value = "ai_model_config")
public class AiModelConfigEntity extends BaseEntity<Long> {

    @Schema(description = "配置编码")
    @TableField(value = "code")
    private String code;

    @Schema(description = "配置名称")
    @TableField(value = "name")
    private String name;

    @Schema(description = "供应商类型")
    @TableField(value = "provider_type")
    private String providerType;

    @Schema(description = "模型服务地址")
    @TableField(value = "base_url")
    private String baseUrl;

    @Schema(description = "API Key")
    @TableField(value = "api_key")
    private String apiKey;

    @Schema(description = "模型名称")
    @TableField(value = "model_name")
    private String modelName;

    @Schema(description = "温度参数")
    @TableField(value = "temperature")
    private Double temperature;

    @Schema(description = "调用超时时间，单位秒")
    @TableField(value = "timeout_seconds")
    private Long timeoutSeconds;

    @Schema(description = "状态(0=禁用 1=启用)")
    @TableField(value = "status")
    private Integer status;

    @Schema(description = "是否默认配置(0=否 1=是)")
    @TableField(value = "default_flag")
    private Integer defaultFlag;

    @Schema(description = "描述")
    @TableField(value = "description")
    private String description;
}
