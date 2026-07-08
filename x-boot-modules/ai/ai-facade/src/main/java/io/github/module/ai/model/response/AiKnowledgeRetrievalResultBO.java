package io.github.module.ai.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * AI 知识库检索结果 BO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AiKnowledgeRetrievalResultBO implements Serializable {

    @Schema(description = "检索日志ID")
    private Long logId;

    @Schema(description = "查询内容")
    private String query;

    @Schema(description = "召回数量")
    private Integer topK;

    @Schema(description = "相似度阈值")
    private Double similarityThreshold;

    @Schema(description = "命中数量")
    private Integer hitCount;

    @Schema(description = "耗时毫秒")
    private Long elapsedMillis;

    @Schema(description = "状态(0=失败 1=成功)")
    private Integer status;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "命中片段")
    private List<AiKnowledgeRetrievalHitBO> hits;
}
