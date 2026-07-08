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
 * AI 知识库文档切片.
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName(value = "ai_knowledge_document_chunk")
public class AiKnowledgeDocumentChunkEntity extends BaseEntity<Long> {

    @Schema(description = "知识库ID")
    @TableField(value = "knowledge_base_id")
    private Long knowledgeBaseId;

    @Schema(description = "文档ID")
    @TableField(value = "document_id")
    private Long documentId;

    @Schema(description = "切片序号")
    @TableField(value = "chunk_no")
    private Integer chunkNo;

    @Schema(description = "切片内容")
    @TableField(value = "content")
    private String content;

    @Schema(description = "切片内容预览")
    @TableField(value = "content_preview")
    private String contentPreview;

    @Schema(description = "来源页码")
    @TableField(value = "source_page")
    private Integer sourcePage;

    @Schema(description = "来源定位")
    @TableField(value = "source_position")
    private String sourcePosition;

    @Schema(description = "预估Token数")
    @TableField(value = "token_count")
    private Integer tokenCount;

    @Schema(description = "切片状态(0=失败 1=成功 2=处理中)")
    @TableField(value = "status")
    private Integer status;

    @Schema(description = "向量化状态(0=失败 1=成功 2=处理中 3=待处理)")
    @TableField(value = "embedding_status")
    private Integer embeddingStatus;

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

    @Schema(description = "向量ID")
    @TableField(value = "vector_id")
    private String vectorId;

    @Schema(description = "向量内容哈希")
    @TableField(value = "vector_hash")
    private String vectorHash;

    @Schema(description = "错误信息")
    @TableField(value = "error_message")
    private String errorMessage;
}
