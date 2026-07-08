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
 * AI 会话.
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName(value = "ai_conversation")
public class AiConversationEntity extends BaseEntity<Long> {

    @Schema(description = "业务会话ID")
    @TableField(value = "conversation_id")
    private String conversationId;

    @Schema(description = "后台用户ID")
    @TableField(value = "user_id")
    private Long userId;

    @Schema(description = "会话标题")
    @TableField(value = "title")
    private String title;

    @Schema(description = "模型配置ID")
    @TableField(value = "model_config_id")
    private Long modelConfigId;

    @Schema(description = "模型配置编码")
    @TableField(value = "model_config_code")
    private String modelConfigCode;

    @Schema(description = "供应商类型")
    @TableField(value = "provider_type")
    private String providerType;

    @Schema(description = "模型名称")
    @TableField(value = "model_name")
    private String modelName;

    @Schema(description = "状态(0=归档 1=活跃)")
    @TableField(value = "status")
    private Integer status;

    @Schema(description = "消息数量")
    @TableField(value = "message_count")
    private Integer messageCount;

    @Schema(description = "最近消息时刻")
    @TableField(value = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Schema(description = "最近消息预览")
    @TableField(value = "last_message_preview")
    private String lastMessagePreview;
}
