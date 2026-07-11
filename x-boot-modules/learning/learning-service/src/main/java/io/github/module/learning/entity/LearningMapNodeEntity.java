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
 * 学习地图节点.
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("learning_map_node")
public class LearningMapNodeEntity extends BaseEntity<Long> {

    @TableField("goal_id")
    private Long goalId;

    @TableField("map_id")
    private Long mapId;

    @TableField("user_id")
    private Long userId;

    @TableField("node_code")
    private String nodeCode;

    @TableField("title")
    private String title;

    @TableField("description")
    private String description;

    @TableField("learning_objective")
    private String learningObjective;

    @TableField("why_it_matters")
    private String whyItMatters;

    @TableField("estimated_minutes")
    private Integer estimatedMinutes;

    @TableField("difficulty_level")
    private Integer difficultyLevel;

    @TableField("verification_method")
    private String verificationMethod;

    @TableField("completion_criteria")
    private String completionCriteria;

    @TableField("prerequisite_node_codes")
    private String prerequisiteNodeCodes;

    @TableField("sort_order")
    private Integer sortOrder;
}
