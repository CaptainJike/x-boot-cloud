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

/**
 * AI Agent 详情 BO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AiAgentDetailBO implements Serializable {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "创建时刻")
    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime createdAt;

    @Schema(description = "更新时刻")
    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime updatedAt;

    @Schema(description = "Agent编码")
    private String agentCode;

    @Schema(description = "Agent名称")
    private String name;

    @Schema(description = "Agent描述")
    private String description;

    @Schema(description = "Agent头像")
    private String avatar;

    @Schema(description = "系统提示词")
    private String systemPrompt;

    @Schema(description = "默认模型配置ID")
    private Long modelConfigId;

    @Schema(description = "默认模型配置编码")
    private String modelConfigCode;

    @Schema(description = "默认供应商类型")
    private String providerType;

    @Schema(description = "默认模型名称")
    private String modelName;

    @Schema(description = "默认知识库ID列表")
    private String knowledgeBaseIds;

    @Schema(description = "温度参数")
    private Double temperature;

    @Schema(description = "最大回复Token数")
    private Integer maxTokens;

    @Schema(description = "执行参数JSON")
    private String executionConfig;

    @Schema(description = "状态(0=禁用 1=启用)")
    private Integer status;

    @Schema(description = "发布状态(0=草稿 1=已发布)")
    private Integer publishStatus;

    @Schema(description = "发布时刻")
    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime publishedAt;

    @Schema(description = "最近执行时刻")
    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime lastExecutedAt;

    @Schema(description = "执行次数")
    private Integer executionCount;
}
