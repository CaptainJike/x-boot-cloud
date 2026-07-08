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
 * AI 知识库文档.
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName(value = "ai_knowledge_document")
public class AiKnowledgeDocumentEntity extends BaseEntity<Long> {

    @Schema(description = "知识库ID")
    @TableField(value = "knowledge_base_id")
    private Long knowledgeBaseId;

    @Schema(description = "OSS文件ID")
    @TableField(value = "oss_file_id")
    private Long ossFileId;

    @Schema(description = "文档名称")
    @TableField(value = "document_name")
    private String documentName;

    @Schema(description = "文档描述")
    @TableField(value = "description")
    private String description;

    @Schema(description = "原始文件名")
    @TableField(value = "original_filename")
    private String originalFilename;

    @Schema(description = "扩展名")
    @TableField(value = "extend_name")
    private String extendName;

    @Schema(description = "文件大小")
    @TableField(value = "file_size")
    private Long fileSize;

    @Schema(description = "MD5")
    @TableField(value = "md5")
    private String md5;

    @Schema(description = "存储平台")
    @TableField(value = "storage_platform")
    private String storagePlatform;

    @Schema(description = "解析状态(0=失败 1=成功 2=处理中 3=待处理)")
    @TableField(value = "parse_status")
    private Integer parseStatus;

    @Schema(description = "切片状态(0=失败 1=成功 2=处理中 3=待处理)")
    @TableField(value = "chunk_status")
    private Integer chunkStatus;

    @Schema(description = "向量化状态(0=失败 1=成功 2=处理中 3=待处理)")
    @TableField(value = "embedding_status")
    private Integer embeddingStatus;

    @Schema(description = "状态(0=禁用 1=启用)")
    @TableField(value = "status")
    private Integer status;

    @Schema(description = "切片数量")
    @TableField(value = "chunk_count")
    private Integer chunkCount;

    @Schema(description = "解析失败原因")
    @TableField(value = "parse_error_message")
    private String parseErrorMessage;

    @Schema(description = "切片失败原因")
    @TableField(value = "chunk_error_message")
    private String chunkErrorMessage;

    @Schema(description = "重试次数")
    @TableField(value = "retry_count")
    private Integer retryCount;

    @Schema(description = "最近解析时刻")
    @TableField(value = "parsed_at")
    private LocalDateTime parsedAt;

    @Schema(description = "最近切片时刻")
    @TableField(value = "chunked_at")
    private LocalDateTime chunkedAt;

    @Schema(description = "最近重试时刻")
    @TableField(value = "last_retry_at")
    private LocalDateTime lastRetryAt;
}
