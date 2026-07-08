package io.github.module.ai.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * AI 知识库检索命中 BO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AiKnowledgeRetrievalHitBO implements Serializable {

    @Schema(description = "知识库ID")
    private Long knowledgeBaseId;

    @Schema(description = "知识库名称")
    private String knowledgeBaseName;

    @Schema(description = "文档ID")
    private Long documentId;

    @Schema(description = "文档名称")
    private String documentName;

    @Schema(description = "切片ID")
    private Long chunkId;

    @Schema(description = "切片序号")
    private Integer chunkNo;

    @Schema(description = "命中内容")
    private String content;

    @Schema(description = "来源页码")
    private Integer sourcePage;

    @Schema(description = "来源定位")
    private String sourcePosition;

    @Schema(description = "相似度分数")
    private Double similarityScore;
}
