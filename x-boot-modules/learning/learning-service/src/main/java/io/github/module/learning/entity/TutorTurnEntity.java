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
 * Tutor 轮次.
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("tutor_turn")
public class TutorTurnEntity extends BaseEntity<Long> {

    @TableField("session_id")
    private Long sessionId;

    @TableField("goal_id")
    private Long goalId;

    @TableField("map_node_id")
    private Long mapNodeId;

    @TableField("user_id")
    private Long userId;

    @TableField("turn_no")
    private Integer turnNo;

    @TableField("learner_answer")
    private String learnerAnswer;

    @TableField("diagnosis")
    private String diagnosis;

    @TableField("action_type")
    private String actionType;

    @TableField("diagnostic_questions_json")
    private String diagnosticQuestionsJson;

    @TableField("tutor_response")
    private String tutorResponse;

    @TableField("next_step_suggestions_json")
    private String nextStepSuggestionsJson;

    @TableField("recommended_node_id")
    private Long recommendedNodeId;

    @TableField("node_completed")
    private Integer nodeCompleted;
}
