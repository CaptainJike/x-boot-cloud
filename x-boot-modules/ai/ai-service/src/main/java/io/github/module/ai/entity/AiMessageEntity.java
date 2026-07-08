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
 * AI 消息.
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName(value = "ai_message")
public class AiMessageEntity extends BaseEntity<Long> {

    @Schema(description = "业务消息ID")
    @TableField(value = "message_id")
    private String messageId;

    @Schema(description = "业务会话ID")
    @TableField(value = "conversation_id")
    private String conversationId;

    @Schema(description = "父消息ID")
    @TableField(value = "parent_message_id")
    private String parentMessageId;

    @Schema(description = "消息角色")
    @TableField(value = "role")
    private String role;

    @Schema(description = "消息内容")
    @TableField(value = "content")
    private String content;

    @Schema(description = "内容类型")
    @TableField(value = "content_type")
    private String contentType;

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

    @Schema(description = "状态(0=失败 1=成功 2=生成中)")
    @TableField(value = "status")
    private Integer status;

    @Schema(description = "会话内消息序号")
    @TableField(value = "sequence_no")
    private Integer sequenceNo;

    @Schema(description = "提示词Token数")
    @TableField(value = "prompt_tokens")
    private Integer promptTokens;

    @Schema(description = "回复Token数")
    @TableField(value = "completion_tokens")
    private Integer completionTokens;

    @Schema(description = "总Token数")
    @TableField(value = "total_tokens")
    private Integer totalTokens;

    @Schema(description = "结束原因")
    @TableField(value = "finish_reason")
    private String finishReason;

    @Schema(description = "错误编码")
    @TableField(value = "error_code")
    private String errorCode;

    @Schema(description = "错误信息")
    @TableField(value = "error_message")
    private String errorMessage;

    @Schema(description = "消息时刻")
    @TableField(value = "sent_at")
    private LocalDateTime sentAt;
}
