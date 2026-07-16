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
 * Goal Builder / Dashboard 上下文记录.
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("learning_goal_context_record")
public class LearningGoalContextRecordEntity extends BaseEntity<Long> {

    @TableField("user_id")
    private Long userId;

    @TableField("record_type")
    private String recordType;

    @TableField("scope_key")
    private String scopeKey;

    @TableField("goal_id")
    private Long goalId;

    @TableField("related_goal_id")
    private Long relatedGoalId;

    @TableField("title")
    private String title;

    @TableField("payload_json")
    private String payloadJson;
}
