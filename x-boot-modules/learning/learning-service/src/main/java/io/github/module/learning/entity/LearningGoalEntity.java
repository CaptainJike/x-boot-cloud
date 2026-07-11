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
 * 学习目标.
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("learning_goal")
public class LearningGoalEntity extends BaseEntity<Long> {

    @TableField("user_id")
    private Long userId;

    @TableField("target_topic")
    private String targetTopic;

    @TableField("self_assessment")
    private String selfAssessment;

    @TableField("weekly_learning_minutes")
    private Integer weeklyLearningMinutes;

    @TableField("preferred_learning_style")
    private String preferredLearningStyle;

    @TableField("template_code")
    private String templateCode;

    @TableField("status")
    private String status;

    @TableField("active_node_id")
    private Long activeNodeId;

    @TableField("estimated_days")
    private Integer estimatedDays;
}
