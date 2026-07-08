package io.github.module.ai.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 后台管理-分页列表知识库.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminListAiKnowledgeBaseDTO implements Serializable {

    @Schema(description = "知识库名称")
    private String name;

    @Schema(description = "向量化模型配置编码")
    private String embeddingModelConfigCode;

    @Schema(description = "状态(0=禁用 1=启用)")
    private Integer status;
}
