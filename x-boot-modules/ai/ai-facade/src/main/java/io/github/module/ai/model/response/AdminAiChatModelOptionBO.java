package io.github.module.ai.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 后台 AI 对话可选模型配置.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminAiChatModelOptionBO implements Serializable {

    @Schema(description = "模型配置ID")
    private Long id;

    @Schema(description = "配置编码")
    private String code;

    @Schema(description = "配置名称")
    private String name;

    @Schema(description = "供应商类型")
    private String providerType;

    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "支持的模态，逗号分隔，例如 text,image")
    private String supportedModalities;

    @Schema(description = "支持的能力，逗号分隔，例如 chat,embedding")
    private String supportedCapabilities;

    @Schema(description = "是否默认配置(0=否 1=是)")
    private Integer defaultFlag;

    @Schema(description = "描述")
    private String description;
}
