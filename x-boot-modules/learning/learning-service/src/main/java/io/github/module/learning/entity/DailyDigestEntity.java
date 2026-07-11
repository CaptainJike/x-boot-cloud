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
 * 成长日报.
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("daily_digest")
public class DailyDigestEntity extends BaseEntity<Long> {

    @TableField("user_id")
    private Long userId;

    @TableField("goal_id")
    private Long goalId;

    @TableField("reflection_entry_id")
    private Long reflectionEntryId;

    @TableField("digest_date")
    private LocalDate digestDate;

    @TableField("summary")
    private String summary;

    @TableField("next_action")
    private String nextAction;
}
