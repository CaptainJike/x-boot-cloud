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
 * 目标上下文 Bundle BO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "目标上下文 Bundle")
public class GoalContextBundleBO implements Serializable {

    private List<GoalBriefRecordBO> goalBriefRecords;
    private List<GoalAdjustmentRecordBO> goalAdjustmentRecords;
    private GoalExecutionHandoffBO activeGoalExecutionHandoff;
    private List<PortfolioCandidateValidationRecordBO> portfolioCandidateValidations;
    private List<GoalCheckpointRecordBO> goalCheckpointRecords;
    private GoalTuningSnapshotBO goalTuningSnapshot;
}
