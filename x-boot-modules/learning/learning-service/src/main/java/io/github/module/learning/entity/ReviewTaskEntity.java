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

import java.time.LocalDateTime;

/**
 * 服务端生成的复盘任务.
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("review_task")
public class ReviewTaskEntity extends BaseEntity<Long> {

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

    @TableField("source_reason")
    private String sourceReason;

    @TableField("knowledge_focus")
    private String knowledgeFocus;

    @TableField("due_at")
    private LocalDateTime dueAt;

    @TableField("interval_days")
    private Integer intervalDays;

    @TableField("priority")
    private String priority;

    @TableField("priority_score")
    private Integer priorityScore;

    @TableField("schedule_reason")
    private String scheduleReason;

    @TableField("mastery_score")
    private Integer masteryScore;

    @TableField("last_reviewed_at")
    private LocalDateTime lastReviewedAt;

    @TableField("task_version")
    private Integer taskVersion;

    @TableField("active")
    private Integer active;
}
