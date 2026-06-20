package io.github.module.ai.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * APP-AI 对话请求.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AppAiChatDTO implements Serializable {

    @Schema(description = "会话ID")
    @Size(max = 64, message = "【会话ID】最长64位")
    private String conversationId;

    @Schema(description = "AI模型配置编码")
    @Size(max = 64, message = "【AI模型配置编码】最长64位")
    private String modelConfigCode;

    @Schema(description = "对话内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 8000, message = "【对话内容】最长8000位")
    @NotBlank(message = "对话内容不能为空")
    private String content;
}
