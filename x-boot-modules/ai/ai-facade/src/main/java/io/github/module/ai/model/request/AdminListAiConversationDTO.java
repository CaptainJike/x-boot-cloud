package io.github.module.ai.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 后台管理-分页列表 AI 会话.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminListAiConversationDTO implements Serializable {

    @Schema(description = "业务会话ID")
    private String conversationId;

    @Schema(description = "后台用户ID")
    private Long userId;

    @Schema(description = "会话标题")
    private String title;

    @Schema(description = "AI模型配置编码")
    private String modelConfigCode;

    @Schema(description = "供应商类型")
    private String providerType;

    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "状态(0=归档 1=活跃)")
    private Integer status;
}
