package io.github.module.ai.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 后台管理-分页列表知识库检索日志.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminListAiKnowledgeRetrievalLogDTO implements Serializable {

    @Schema(description = "知识库ID")
    private Long knowledgeBaseId;

    @Schema(description = "业务会话ID")
    private String conversationId;

    @Schema(description = "业务消息ID")
    private String messageId;

    @Schema(description = "查询内容关键词")
    private String queryKeyword;

    @Schema(description = "状态(0=失败 1=成功)")
    private Integer status;
}
