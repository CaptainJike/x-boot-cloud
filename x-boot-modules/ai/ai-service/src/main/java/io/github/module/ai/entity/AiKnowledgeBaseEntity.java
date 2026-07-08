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

import java.time.LocalDateTime;

/**
 * AI 知识库.
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName(value = "ai_knowledge_base")
public class AiKnowledgeBaseEntity extends BaseEntity<Long> {

    @Schema(description = "知识库名称")
    @TableField(value = "name")
    private String name;

    @Schema(description = "知识库描述")
    @TableField(value = "description")
    private String description;

    @Schema(description = "向量化模型配置ID")
    @TableField(value = "embedding_model_config_id")
    private Long embeddingModelConfigId;

    @Schema(description = "向量化模型配置编码")
    @TableField(value = "embedding_model_config_code")
    private String embeddingModelConfigCode;

    @Schema(description = "向量化供应商类型")
    @TableField(value = "embedding_provider_type")
    private String embeddingProviderType;

    @Schema(description = "向量化模型名称")
    @TableField(value = "embedding_model_name")
    private String embeddingModelName;

    @Schema(description = "默认召回数量")
    @TableField(value = "retrieval_top_k")
    private Integer retrievalTopK;

    @Schema(description = "默认相似度阈值")
    @TableField(value = "similarity_threshold")
    private Double similarityThreshold;

    @Schema(description = "状态(0=禁用 1=启用)")
    @TableField(value = "status")
    private Integer status;

    @Schema(description = "文档数量")
    @TableField(value = "document_count")
    private Integer documentCount;

    @Schema(description = "切片数量")
    @TableField(value = "chunk_count")
    private Integer chunkCount;

    @Schema(description = "最近文档解析时刻")
    @TableField(value = "last_parsed_at")
    private LocalDateTime lastParsedAt;

    @Schema(description = "最近检索时刻")
    @TableField(value = "last_retrieved_at")
    private LocalDateTime lastRetrievedAt;
}
