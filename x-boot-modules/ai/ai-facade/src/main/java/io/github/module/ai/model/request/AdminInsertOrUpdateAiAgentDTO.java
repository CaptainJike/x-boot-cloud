package io.github.module.ai.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
 * 后台管理-新增/编辑 Agent.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminInsertOrUpdateAiAgentDTO implements Serializable {

    @Schema(description = "主键ID", hidden = true, title = "仅更新时使用")
    private Long id;

    @Schema(description = "Agent编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 64, message = "【Agent编码】最长64位")
    @NotBlank(message = "Agent编码不能为空")
    private String agentCode;

    @Schema(description = "Agent名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 100, message = "【Agent名称】最长100位")
    @NotBlank(message = "Agent名称不能为空")
    private String name;

    @Schema(description = "Agent描述")
    @Size(max = 500, message = "【Agent描述】最长500位")
    private String description;

    @Schema(description = "Agent头像")
    @Size(max = 500, message = "【Agent头像】最长500位")
    private String avatar;

    @Schema(description = "系统提示词")
    @Size(max = 4000, message = "【系统提示词】最长4000位")
    private String systemPrompt;

    @Schema(description = "默认模型配置编码")
    @Size(max = 64, message = "【默认模型配置编码】最长64位")
    private String modelConfigCode;

    @Schema(description = "默认知识库ID列表")
    @Size(max = 2000, message = "【默认知识库ID列表】最长2000位")
    private String knowledgeBaseIds;

    @Schema(description = "温度参数")
    @DecimalMin(value = "0.0", message = "【温度参数】不能小于0")
    @DecimalMax(value = "2.0", message = "【温度参数】不能大于2")
    private Double temperature;

    @Schema(description = "最大回复Token数")
    @Min(value = 1, message = "【最大回复Token数】不能小于1")
    @Max(value = 32000, message = "【最大回复Token数】不能大于32000")
    private Integer maxTokens;

    @Schema(description = "执行参数JSON")
    @Size(max = 4000, message = "【执行参数JSON】最长4000位")
    private String executionConfig;

    @Schema(description = "状态(0=禁用 1=启用)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;
}
