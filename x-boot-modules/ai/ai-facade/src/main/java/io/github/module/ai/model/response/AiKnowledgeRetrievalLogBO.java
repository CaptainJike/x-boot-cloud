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
import java.util.List;

/**
 * AI 知识库检索日志 BO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AiKnowledgeRetrievalLogBO implements Serializable {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "创建时刻")
    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime createdAt;

    @Schema(description = "后台用户ID")
    private Long userId;

    @Schema(description = "知识库ID列表")
    private List<Long> knowledgeBaseIds;

    @Schema(description = "业务会话ID")
    private String conversationId;

    @Schema(description = "业务消息ID")
    private String messageId;

    @Schema(description = "查询内容")
    private String query;

    @Schema(description = "召回数量")
    private Integer topK;

    @Schema(description = "相似度阈值")
    private Double similarityThreshold;

    @Schema(description = "命中数量")
    private Integer hitCount;

    @Schema(description = "召回摘要")
    private String hitsSummary;

    @Schema(description = "耗时毫秒")
    private Long elapsedMillis;

    @Schema(description = "状态(0=失败 1=成功)")
    private Integer status;

    @Schema(description = "错误编码")
    private String errorCode;

    @Schema(description = "错误信息")
    private String errorMessage;
}
