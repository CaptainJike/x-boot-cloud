package io.github.module.learning.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * Goal 交接状态 BO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "Goal 交接状态")
public class GoalExecutionHandoffBO implements Serializable {

    private String id;
    private String createdAt;
    private Long sourceGoalId;
    private String sourceGoalTitle;
    private Long nextGoalId;
    private String nextGoalTitle;
    private String checkpointDecision;
    private String checkpointTitle;
    private String handoffTitle;
    private String handoffSummary;
    private String firstMissionTitle;
    private String firstMissionSummary;
    private List<String> carryOverActions;
    private List<String> watchouts;
}
