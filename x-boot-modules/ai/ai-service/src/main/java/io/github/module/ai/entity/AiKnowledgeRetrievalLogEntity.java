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
 * AI 知识库检索日志.
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName(value = "ai_knowledge_retrieval_log")
public class AiKnowledgeRetrievalLogEntity extends BaseEntity<Long> {

    @Schema(description = "业务检索ID")
    @TableField(value = "retrieval_id")
    private String retrievalId;

    @Schema(description = "后台用户ID")
    @TableField(value = "user_id")
    private Long userId;

    @Schema(description = "知识库ID列表")
    @TableField(value = "knowledge_base_ids")
    private String knowledgeBaseIds;

    @Schema(description = "业务会话ID")
    @TableField(value = "conversation_id")
    private String conversationId;

    @Schema(description = "业务消息ID")
    @TableField(value = "message_id")
    private String messageId;

    @Schema(description = "查询内容")
    @TableField(value = "query_text")
    private String queryText;

    @Schema(description = "召回数量")
    @TableField(value = "top_k")
    private Integer topK;

    @Schema(description = "相似度阈值")
    @TableField(value = "similarity_threshold")
    private Double similarityThreshold;

    @Schema(description = "命中数量")
    @TableField(value = "hit_count")
    private Integer hitCount;

    @Schema(description = "召回摘要")
    @TableField(value = "hits_summary")
    private String hitsSummary;

    @Schema(description = "耗时，单位毫秒")
    @TableField(value = "elapsed_ms")
    private Long elapsedMs;

    @Schema(description = "状态(0=失败 1=成功)")
    @TableField(value = "status")
    private Integer status;

    @Schema(description = "错误编码")
    @TableField(value = "error_code")
    private String errorCode;

    @Schema(description = "错误信息")
    @TableField(value = "error_message")
    private String errorMessage;

    @Schema(description = "检索时刻")
    @TableField(value = "retrieved_at")
    private LocalDateTime retrievedAt;
}
