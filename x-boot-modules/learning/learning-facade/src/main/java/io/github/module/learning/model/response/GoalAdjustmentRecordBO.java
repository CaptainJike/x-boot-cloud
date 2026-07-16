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
 * Goal 调整记录 BO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "Goal 调整记录")
public class GoalAdjustmentRecordBO implements Serializable {

    private String id;
    private String createdAt;
    private Long sourceGoalId;
    private String sourceGoalTitle;
    private Long nextGoalId;
    private String nextGoalTitle;
    private String checkpointDecision;
    private String checkpointTitle;
    private String tuningSummary;
    private List<String> changedFields;
    private List<GoalTuningSuggestionBO> changes;
    private GoalBriefBO sourceBrief;
    private GoalBriefBO resultingBrief;
    private String candidateValidationSummary;
    private Double candidateValidationConfidence;
    private String candidateValidationProof;
    private String candidateValidationRisk;
}
