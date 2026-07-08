package io.github.module.ai.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 后台管理-新增/编辑 AI 模型配置.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminInsertOrUpdateAiModelConfigDTO implements Serializable {

    @Schema(description = "主键ID", hidden = true, title = "仅更新时使用")
    private Long id;

    @Schema(description = "配置编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 64, message = "【配置编码】最长64位")
    @NotBlank(message = "配置编码不能为空")
    private String code;

    @Schema(description = "配置名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 100, message = "【配置名称】最长100位")
    @NotBlank(message = "配置名称不能为空")
    private String name;

    @Schema(description = "供应商类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 50, message = "【供应商类型】最长50位")
    @NotBlank(message = "供应商类型不能为空")
    private String providerType;

    @Schema(description = "模型服务地址")
    @Size(max = 255, message = "【模型服务地址】最长255位")
    private String baseUrl;

    @Schema(description = "API Key")
    @Size(max = 2048, message = "【API Key】最长2048位")
    private String apiKey;

    @Schema(description = "模型名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 100, message = "【模型名称】最长100位")
    @NotBlank(message = "模型名称不能为空")
    private String modelName;

    @Schema(description = "支持的模态，逗号分隔，例如 text,image")
    @Size(max = 64, message = "【支持的模态】最长64位")
    private String supportedModalities;

    @Schema(description = "支持的能力，逗号分隔，例如 chat,embedding")
    @Size(max = 64, message = "【支持的能力】最长64位")
    private String supportedCapabilities;

    @Schema(description = "温度参数")
    @DecimalMin(value = "0.0", message = "【温度参数】不能小于0")
    @DecimalMax(value = "2.0", message = "【温度参数】不能大于2")
    private Double temperature;

    @Schema(description = "调用超时时间，单位秒")
    private Long timeoutSeconds;

    @Schema(description = "状态(0=禁用 1=启用)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "是否默认配置(0=否 1=是)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否默认配置不能为空")
    private Integer defaultFlag;

    @Schema(description = "描述")
    @Size(max = 255, message = "【描述】最长255位")
    private String description;
}
