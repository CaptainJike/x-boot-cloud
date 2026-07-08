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
 * 后台管理-新增/编辑知识库.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminInsertOrUpdateAiKnowledgeBaseDTO implements Serializable {

    @Schema(description = "主键ID", hidden = true, title = "仅更新时使用")
    private Long id;

    @Schema(description = "知识库名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 100, message = "【知识库名称】最长100位")
    @NotBlank(message = "知识库名称不能为空")
    private String name;

    @Schema(description = "知识库描述")
    @Size(max = 500, message = "【知识库描述】最长500位")
    private String description;

    @Schema(description = "向量化模型配置编码")
    @Size(max = 64, message = "【向量化模型配置编码】最长64位")
    private String embeddingModelConfigCode;

    @Schema(description = "默认召回数量")
    @Min(value = 1, message = "【默认召回数量】不能小于1")
    @Max(value = 50, message = "【默认召回数量】不能大于50")
    private Integer retrievalTopK;

    @Schema(description = "默认相似度阈值")
    @DecimalMin(value = "0.0", message = "【默认相似度阈值】不能小于0")
    @DecimalMax(value = "1.0", message = "【默认相似度阈值】不能大于1")
    private Double similarityThreshold;

    @Schema(description = "状态(0=禁用 1=启用)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;
}
