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
 * Tutor 会话.
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("tutor_session")
public class TutorSessionEntity extends BaseEntity<Long> {

    @TableField("goal_id")
    private Long goalId;

    @TableField("map_node_id")
    private Long mapNodeId;

    @TableField("user_id")
    private Long userId;

    @TableField("status")
    private String status;

    @TableField("learner_question")
    private String learnerQuestion;
}
