package io.github.module.learning.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.framework.crud.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

/**
 * 服务端生成的练习任务.
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("practice_task")
public class PracticeTaskEntity extends BaseEntity<Long> {

    @TableField("user_id")
    private Long userId;

    @TableField("goal_id")
    private Long goalId;

    @TableField("map_node_id")
    private Long mapNodeId;

    @TableField("task_key")
    private String taskKey;

    @TableField("task_type")
    private String taskType;

    @TableField("title")
    private String title;

    @TableField("prompt")
    private String prompt;

    @TableField("expected_outcome")
    private String expectedOutcome;

    @TableField("hint")
    private String hint;

    @TableField("estimated_minutes")
    private Integer estimatedMinutes;

    @TableField("node_title")
    private String nodeTitle;

    @TableField("source_diagnosis")
    private String sourceDiagnosis;

    @TableField("related_concepts_json")
    private String relatedConceptsJson;

    @TableField("evidence_kind")
    private String evidenceKind;

    @TableField("knowledge_focus")
    private String knowledgeFocus;

    @TableField("handoff_validation")
    private Integer handoffValidation;

    @TableField("handoff_title")
    private String handoffTitle;

    @TableField("task_version")
    private Integer taskVersion;

    @TableField("active")
    private Integer active;
}
