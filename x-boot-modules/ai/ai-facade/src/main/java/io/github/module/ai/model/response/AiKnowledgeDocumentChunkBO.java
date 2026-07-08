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
 * AI 知识库文档切片 BO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AiKnowledgeDocumentChunkBO implements Serializable {

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

    @Schema(description = "文档ID")
    private Long documentId;

    @Schema(description = "文档名称")
    private String documentName;

    @Schema(description = "切片序号")
    private Integer chunkNo;

    @Schema(description = "切片内容")
    private String content;

    @Schema(description = "切片内容预览")
    private String contentPreview;

    @Schema(description = "来源页码")
    private Integer sourcePage;

    @Schema(description = "来源定位")
    private String sourcePosition;

    @Schema(description = "预估Token数")
    private Integer tokenCount;

    @Schema(description = "切片状态(0=失败 1=成功 2=处理中)")
    private Integer status;

    @Schema(description = "向量化状态(0=失败 1=成功 2=处理中 3=待处理)")
    private Integer embeddingStatus;

    @Schema(description = "错误信息")
    private String errorMessage;
}
