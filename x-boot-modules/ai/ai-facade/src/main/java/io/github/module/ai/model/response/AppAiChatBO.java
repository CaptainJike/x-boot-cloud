package io.github.module.ai.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * APP-AI 对话响应.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AppAiChatBO implements Serializable {

    @Schema(description = "会话ID")
    private String conversationId;

    @Schema(description = "AI模型配置编码")
    private String modelConfigCode;

    @Schema(description = "供应商类型")
    private String providerType;

    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "响应内容")
    private String answer;
}
