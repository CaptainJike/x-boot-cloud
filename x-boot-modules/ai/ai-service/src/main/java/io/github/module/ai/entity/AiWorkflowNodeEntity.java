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

/**
 * AI 工作流节点.
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName(value = "ai_workflow_node")
public class AiWorkflowNodeEntity extends BaseEntity<Long> {

    @Schema(description = "工作流定义ID")
    @TableField(value = "workflow_definition_id")
    private Long workflowDefinitionId;

    @Schema(description = "工作流编码")
    @TableField(value = "workflow_code")
    private String workflowCode;

    @Schema(description = "版本号")
    @TableField(value = "version_no")
    private Integer versionNo;

    @Schema(description = "节点Key")
    @TableField(value = "node_key")
    private String nodeKey;

    @Schema(description = "节点名称")
    @TableField(value = "node_name")
    private String nodeName;

    @Schema(description = "节点类型")
    @TableField(value = "node_type")
    private String nodeType;

    @Schema(description = "节点描述")
    @TableField(value = "description")
    private String description;

    @Schema(description = "节点配置JSON")
    @TableField(value = "node_config")
    private String nodeConfig;

    @Schema(description = "输入映射JSON")
    @TableField(value = "input_mapping")
    private String inputMapping;

    @Schema(description = "输出映射JSON")
    @TableField(value = "output_mapping")
    private String outputMapping;

    @Schema(description = "下游节点Key列表")
    @TableField(value = "next_node_keys")
    private String nextNodeKeys;

    @Schema(description = "条件表达式")
    @TableField(value = "condition_expression")
    private String conditionExpression;

    @Schema(description = "错误策略")
    @TableField(value = "error_strategy")
    private String errorStrategy;

    @Schema(description = "重试次数")
    @TableField(value = "retry_count")
    private Integer retryCount;

    @Schema(description = "超时时间，单位秒")
    @TableField(value = "timeout_seconds")
    private Long timeoutSeconds;

    @Schema(description = "排序")
    @TableField(value = "sort_order")
    private Integer sortOrder;

    @Schema(description = "状态(0=禁用 1=启用)")
    @TableField(value = "status")
    private Integer status;
}
