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
 * 学习地图.
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("learning_map")
public class LearningMapEntity extends BaseEntity<Long> {

    @TableField("goal_id")
    private Long goalId;

    @TableField("user_id")
    private Long userId;

    @TableField("generation_version")
    private Integer generationVersion;

    @TableField("generation_summary")
    private String generationSummary;
}
