package io.github.module.ai.model.request;

import io.github.framework.core.context.TenantContext;
import io.github.framework.core.context.UserContext;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 后台 AI 流式对话内部请求.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminAiChatStreamRequestDTO implements Serializable {

    @Schema(description = "对话请求", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotNull(message = "对话请求不能为空")
    private AdminAiChatDTO chat;

    @Schema(description = "当前后台用户上下文")
    private UserContext userContext;

    @Schema(description = "当前租户上下文")
    private TenantContext tenantContext;
}
