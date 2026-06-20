package io.github.module.ai.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * APP-AI 流式对话片段.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AppAiChatStreamChunkBO implements Serializable {

    @Schema(description = "SSE事件类型")
    private String event;

    @Schema(description = "消息ID")
    private String messageId;

    @Schema(description = "会话ID")
    private String conversationId;

    @Schema(description = "AI模型配置编码")
    private String modelConfigCode;

    @Schema(description = "供应商类型")
    private String providerType;

    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "片段内容")
    private String content;

    @Schema(description = "是否结束")
    private Boolean finish;

    @Schema(description = "事件时间戳")
    private Long timestamp;
}
