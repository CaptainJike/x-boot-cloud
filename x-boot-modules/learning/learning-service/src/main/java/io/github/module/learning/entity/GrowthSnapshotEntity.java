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
 * 成长快照.
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("growth_snapshot")
public class GrowthSnapshotEntity extends BaseEntity<Long> {

    @TableField("user_id")
    private Long userId;

    @TableField("goal_id")
    private Long goalId;

    @TableField("snapshot_date")
    private LocalDate snapshotDate;

    @TableField("event_type")
    private String eventType;

    @TableField("title")
    private String title;

    @TableField("summary")
    private String summary;
}
