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
 * AI Agent 配置.
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName(value = "ai_agent")
public class AiAgentEntity extends BaseEntity<Long> {

    @Schema(description = "Agent编码")
    @TableField(value = "agent_code")
    private String agentCode;

    @Schema(description = "Agent名称")
    @TableField(value = "name")
    private String name;

    @Schema(description = "Agent描述")
    @TableField(value = "description")
    private String description;

    @Schema(description = "Agent头像")
    @TableField(value = "avatar")
    private String avatar;

    @Schema(description = "系统提示词")
    @TableField(value = "system_prompt")
    private String systemPrompt;

    @Schema(description = "默认模型配置ID")
    @TableField(value = "model_config_id")
    private Long modelConfigId;

    @Schema(description = "默认模型配置编码")
    @TableField(value = "model_config_code")
    private String modelConfigCode;

    @Schema(description = "默认供应商类型")
    @TableField(value = "provider_type")
    private String providerType;

    @Schema(description = "默认模型名称")
    @TableField(value = "model_name")
    private String modelName;

    @Schema(description = "默认知识库ID列表")
    @TableField(value = "knowledge_base_ids")
    private String knowledgeBaseIds;

    @Schema(description = "温度参数")
    @TableField(value = "temperature")
    private Double temperature;

    @Schema(description = "最大回复Token数")
    @TableField(value = "max_tokens")
    private Integer maxTokens;

    @Schema(description = "执行参数JSON")
    @TableField(value = "execution_config")
    private String executionConfig;

    @Schema(description = "状态(0=禁用 1=启用)")
    @TableField(value = "status")
    private Integer status;

    @Schema(description = "发布状态(0=草稿 1=已发布)")
    @TableField(value = "publish_status")
    private Integer publishStatus;

    @Schema(description = "发布时刻")
    @TableField(value = "published_at")
    private LocalDateTime publishedAt;

    @Schema(description = "最近执行时刻")
    @TableField(value = "last_executed_at")
    private LocalDateTime lastExecutedAt;

    @Schema(description = "执行次数")
    @TableField(value = "execution_count")
    private Integer executionCount;
}
