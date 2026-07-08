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
 * 后台 AI 对话响应.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminAiChatBO implements Serializable {

    @Schema(description = "会话ID")
    private String conversationId;

    @Schema(description = "消息ID")
    private String messageId;

    @Schema(description = "AI模型配置编码")
    private String modelConfigCode;

    @Schema(description = "供应商类型")
    private String providerType;

    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "响应内容")
    private String answer;

    @Schema(description = "知识库检索日志ID")
    private Long knowledgeRetrievalLogId;

    @Schema(description = "引用片段")
    private List<AiKnowledgeRetrievalHitBO> references;

    @Schema(description = "是否已联网核验")
    private Boolean realtimeVerified;

    @Schema(description = "联网核验类型")
    private String realtimeLookupType;

    @Schema(description = "联网核验时间戳")
    private Long realtimeLookupTimestamp;

    @Schema(description = "联网核验引用来源")
    private List<AdminAiRealtimeReferenceBO> realtimeReferences;
}
