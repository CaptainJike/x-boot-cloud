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
 * 节点进度.
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("learning_node_progress")
public class LearningNodeProgressEntity extends BaseEntity<Long> {

    @TableField("goal_id")
    private Long goalId;

    @TableField("map_node_id")
    private Long mapNodeId;

    @TableField("user_id")
    private Long userId;

    @TableField("status")
    private String status;

    @TableField("mastery_level")
    private Integer masteryLevel;

    @TableField("last_diagnosis")
    private String lastDiagnosis;

    @TableField("last_studied_at")
    private LocalDateTime lastStudiedAt;

    @TableField("completed_at")
    private LocalDateTime completedAt;
}
