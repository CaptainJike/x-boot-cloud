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
 * AI 反馈.
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName(value = "ai_feedback")
public class AiFeedbackEntity extends BaseEntity<Long> {

    @Schema(description = "业务反馈ID")
    @TableField(value = "feedback_id")
    private String feedbackId;

    @Schema(description = "业务会话ID")
    @TableField(value = "conversation_id")
    private String conversationId;

    @Schema(description = "业务消息ID")
    @TableField(value = "message_id")
    private String messageId;

    @Schema(description = "后台用户ID")
    @TableField(value = "user_id")
    private Long userId;

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

    @Schema(description = "反馈类型")
    @TableField(value = "feedback_type")
    private String feedbackType;

    @Schema(description = "评分")
    @TableField(value = "score")
    private Integer score;

    @Schema(description = "反馈原因编码")
    @TableField(value = "reason_code")
    private String reasonCode;

    @Schema(description = "反馈内容")
    @TableField(value = "content")
    private String content;

    @Schema(description = "处理状态(0=待处理 1=已处理 2=忽略)")
    @TableField(value = "status")
    private Integer status;

    @Schema(description = "处理人")
    @TableField(value = "handled_by")
    private String handledBy;

    @Schema(description = "处理时刻")
    @TableField(value = "handled_at")
    private LocalDateTime handledAt;

    @Schema(description = "处理备注")
    @TableField(value = "remark")
    private String remark;

    @Schema(description = "反馈提交时刻")
    @TableField(value = "submitted_at")
    private LocalDateTime submittedAt;
}
