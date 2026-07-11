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

import java.time.LocalDate;

/**
 * 反思记录.
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("reflection_entry")
public class ReflectionEntryEntity extends BaseEntity<Long> {

    @TableField("user_id")
    private Long userId;

    @TableField("goal_id")
    private Long goalId;

    @TableField("reflection_date")
    private LocalDate reflectionDate;

    @TableField("learned_today")
    private String learnedToday;

    @TableField("biggest_insight")
    private String biggestInsight;

    @TableField("new_awareness")
    private String newAwareness;

    @TableField("unresolved_question")
    private String unresolvedQuestion;

    @TableField("why_stuck")
    private String whyStuck;
}
