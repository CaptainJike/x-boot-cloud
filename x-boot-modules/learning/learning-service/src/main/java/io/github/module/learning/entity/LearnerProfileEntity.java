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
 * 学习者画像.
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("learner_profile")
public class LearnerProfileEntity extends BaseEntity<Long> {

    @TableField("user_id")
    private Long userId;

    @TableField("preferred_learning_style")
    private String preferredLearningStyle;

    @TableField("latest_self_assessment")
    private String latestSelfAssessment;

    @TableField("focus_area")
    private String focusArea;
}
