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
 * Goal 调参快照 BO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "Goal 调参快照")
public class GoalTuningSnapshotBO implements Serializable {

    private String createdAt;
    private String mode;
    private String goalTitle;
    private Long sourceGoalId;
    private Long candidateGoalId;
    private String checkpointDecision;
    private String checkpointTitle;
    private String checkpointReason;
    private String tuningSummary;
    private GoalBriefBO suggestedBrief;
    private List<GoalTuningSuggestionBO> changes;
    private List<String> carryOverQuestions;
}
