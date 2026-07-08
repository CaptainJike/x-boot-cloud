package io.github.module.ai.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.framework.core.constant.BaseConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 后台 AI 消息.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminAiMessageBO implements Serializable {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "创建时刻")
    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime createdAt;

    @Schema(description = "业务消息ID")
    private String messageId;

    @Schema(description = "业务会话ID")
    private String conversationId;

    @Schema(description = "父消息ID")
    private String parentMessageId;

    @Schema(description = "消息角色")
    private String role;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "内容类型")
    private String contentType;

    @Schema(description = "消息附件列表")
    private List<AdminAiMessageAttachmentBO> attachments;

    @Schema(description = "模型配置ID")
    private Long modelConfigId;

    @Schema(description = "AI模型配置编码")
    private String modelConfigCode;

    @Schema(description = "供应商类型")
    private String providerType;

    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "状态(0=失败 1=成功 2=生成中)")
    private Integer status;

    @Schema(description = "会话内消息序号")
    private Integer sequenceNo;

    @Schema(description = "提示词Token数")
    private Integer promptTokens;

    @Schema(description = "回复Token数")
    private Integer completionTokens;

    @Schema(description = "总Token数")
    private Integer totalTokens;

    @Schema(description = "结束原因")
    private String finishReason;

    @Schema(description = "错误编码")
    private String errorCode;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "消息时刻")
    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime sentAt;
}
