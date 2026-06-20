package io.github.module.ai.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 后台管理-分页列表 AI 模型配置.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminListAiModelConfigDTO implements Serializable {

    @Schema(description = "配置编码")
    private String code;

    @Schema(description = "配置名称")
    private String name;

    @Schema(description = "供应商类型")
    private String providerType;

    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "状态(0=禁用 1=启用)")
    private Integer status;
}
