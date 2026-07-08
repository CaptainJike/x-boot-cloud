package io.github.module.ai.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.framework.core.constant.BaseConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 知识库文档详情 BO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AiKnowledgeDocumentDetailBO implements Serializable {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "创建时刻")
    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime createdAt;

    @Schema(description = "更新时刻")
    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime updatedAt;

    @Schema(description = "知识库ID")
    private Long knowledgeBaseId;

    @Schema(description = "知识库名称")
    private String knowledgeBaseName;

    @Schema(description = "OSS文件ID")
    private Long ossFileId;

    @Schema(description = "文档名称")
    private String documentName;

    @Schema(description = "文档描述")
    private String description;

    @Schema(description = "原始文件名")
    private String originalFilename;

    @Schema(description = "扩展名")
    private String extendName;

    @Schema(description = "文件大小")
    private Long fileSize;

    @Schema(description = "MD5")
    private String md5;

    @Schema(description = "存储平台")
    private String storagePlatform;

    @Schema(description = "解析状态(0=失败 1=成功 2=处理中 3=待处理)")
    private Integer parseStatus;

    @Schema(description = "切片状态(0=失败 1=成功 2=处理中 3=待处理)")
    private Integer chunkStatus;

    @Schema(description = "向量化状态(0=失败 1=成功 2=处理中 3=待处理)")
    private Integer embeddingStatus;

    @Schema(description = "状态(0=禁用 1=启用)")
    private Integer status;

    @Schema(description = "切片数量")
    private Integer chunkCount;

    @Schema(description = "解析失败原因")
    private String parseErrorMessage;

    @Schema(description = "切片失败原因")
    private String chunkErrorMessage;

    @Schema(description = "重试次数")
    private Integer retryCount;

    @Schema(description = "最近解析时刻")
    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime parsedAt;

    @Schema(description = "最近切片时刻")
    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime chunkedAt;

    @Schema(description = "最近重试时刻")
    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime lastRetryAt;
}
