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
 * AI 工作流定义.
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName(value = "ai_workflow_definition")
public class AiWorkflowDefinitionEntity extends BaseEntity<Long> {

    @Schema(description = "工作流编码")
    @TableField(value = "workflow_code")
    private String workflowCode;

    @Schema(description = "工作流名称")
    @TableField(value = "name")
    private String name;

    @Schema(description = "工作流描述")
    @TableField(value = "description")
    private String description;

    @Schema(description = "关联Agent ID")
    @TableField(value = "agent_id")
    private Long agentId;

    @Schema(description = "版本号")
    @TableField(value = "version_no")
    private Integer versionNo;

    @Schema(description = "入口节点Key")
    @TableField(value = "entry_node_key")
    private String entryNodeKey;

    @Schema(description = "定义快照JSON")
    @TableField(value = "definition_snapshot")
    private String definitionSnapshot;

    @Schema(description = "发布快照JSON")
    @TableField(value = "published_snapshot")
    private String publishedSnapshot;

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
