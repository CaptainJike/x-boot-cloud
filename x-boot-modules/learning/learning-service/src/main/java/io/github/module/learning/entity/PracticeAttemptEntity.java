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
 * 练习草稿和完成记录.
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("practice_attempt")
public class PracticeAttemptEntity extends BaseEntity<Long> {

    @TableField("user_id")
    private Long userId;

    @TableField("goal_id")
    private Long goalId;

    @TableField("practice_task_id")
    private Long practiceTaskId;

    @TableField("map_node_id")
    private Long mapNodeId;

    @TableField("attempt_key")
    private String attemptKey;

    @TableField("response_content")
    private String responseContent;

    @TableField("self_rating")
    private String selfRating;

    @TableField("artifacts_json")
    private String artifactsJson;

    @TableField("assessment_json")
    private String assessmentJson;

    @TableField("completed")
    private Integer completed;

    @TableField("handoff_validation")
    private Integer handoffValidation;

    @TableField("client_updated_at")
    private String clientUpdatedAt;

    @TableField("last_mutation_id")
    private String lastMutationId;

    @TableField("sync_version")
    private Long syncVersion;
}
