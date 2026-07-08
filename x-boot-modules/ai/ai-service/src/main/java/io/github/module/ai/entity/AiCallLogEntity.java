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
 * AI 调用日志.
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName(value = "ai_call_log")
public class AiCallLogEntity extends BaseEntity<Long> {

    @Schema(description = "业务调用ID")
    @TableField(value = "call_id")
    private String callId;

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

    @Schema(description = "调用类型")
    @TableField(value = "request_type")
    private String requestType;

    @Schema(description = "是否流式调用")
    @TableField(value = "stream_flag")
    private Integer streamFlag;

    @Schema(description = "请求内容摘要")
    @TableField(value = "request_preview")
    private String requestPreview;

    @Schema(description = "响应内容摘要")
    @TableField(value = "response_preview")
    private String responsePreview;

    @Schema(description = "状态(0=失败 1=成功 2=调用中)")
    @TableField(value = "status")
    private Integer status;

    @Schema(description = "耗时，单位毫秒")
    @TableField(value = "duration_ms")
    private Long durationMs;

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

    @Schema(description = "供应商请求ID")
    @TableField(value = "provider_request_id")
    private String providerRequestId;

    @Schema(description = "链路追踪ID")
    @TableField(value = "trace_id")
    private String traceId;

    @Schema(description = "错误编码")
    @TableField(value = "error_code")
    private String errorCode;

    @Schema(description = "错误信息")
    @TableField(value = "error_message")
    private String errorMessage;

    @Schema(description = "调用开始时刻")
    @TableField(value = "started_at")
    private LocalDateTime startedAt;

    @Schema(description = "调用结束时刻")
    @TableField(value = "finished_at")
    private LocalDateTime finishedAt;
}
