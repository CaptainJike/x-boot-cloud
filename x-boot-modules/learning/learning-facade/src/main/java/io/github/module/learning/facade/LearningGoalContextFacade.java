package io.github.module.learning.facade;

import io.github.module.learning.model.request.AppGoalAdjustmentRecordDTO;
import io.github.module.learning.model.request.AppGoalBriefRecordDTO;
import io.github.module.learning.model.request.AppGoalCheckpointRecordDTO;
import io.github.module.learning.model.request.AppGoalExecutionHandoffDTO;
import io.github.module.learning.model.request.AppGoalTuningSnapshotDTO;
import io.github.module.learning.model.request.AppPortfolioCandidateValidationRecordDTO;
import io.github.module.learning.model.response.GoalAdjustmentRecordBO;
import io.github.module.learning.model.response.GoalBriefRecordBO;
import io.github.module.learning.model.response.GoalCheckpointRecordBO;
import io.github.module.learning.model.response.GoalContextBundleBO;
import io.github.module.learning.model.response.GoalExecutionHandoffBO;
import io.github.module.learning.model.response.GoalTuningSnapshotBO;
import io.github.module.learning.model.response.PortfolioCandidateValidationRecordBO;

/**
 * 目标上下文持久化 Facade.
 */
public interface LearningGoalContextFacade {

    /**
     * 获取当前用户的目标上下文.
     */
    GoalContextBundleBO getGoalContext();

    /**
     * 保存目标简报记录.
     */
    GoalBriefRecordBO saveGoalBriefRecord(AppGoalBriefRecordDTO dto);

    /**
     * 保存目标调整记录.
     */
    GoalAdjustmentRecordBO saveGoalAdjustmentRecord(AppGoalAdjustmentRecordDTO dto);

    /**
     * 保存当前激活的目标交接状态.
     */
    GoalExecutionHandoffBO saveActiveGoalExecutionHandoff(AppGoalExecutionHandoffDTO dto);

    /**
     * 清理当前激活的目标交接状态.
     */
    void clearActiveGoalExecutionHandoff();

    /**
     * 保存 Portfolio 候选目标验证记录.
     */
    PortfolioCandidateValidationRecordBO savePortfolioCandidateValidationRecord(
            AppPortfolioCandidateValidationRecordDTO dto);

    /**
     * 保存阶段复盘记录.
     */
    GoalCheckpointRecordBO saveGoalCheckpointRecord(AppGoalCheckpointRecordDTO dto);

    /**
     * 保存当前目标调参快照.
     */
    GoalTuningSnapshotBO saveGoalTuningSnapshot(AppGoalTuningSnapshotDTO dto);

    /**
     * 清理当前目标调参快照.
     */
    void clearGoalTuningSnapshot();
}
