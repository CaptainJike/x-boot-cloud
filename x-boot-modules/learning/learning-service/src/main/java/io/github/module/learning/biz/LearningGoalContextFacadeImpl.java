package io.github.module.learning.biz;

import io.github.framework.core.constant.BaseConstant;
import io.github.module.learning.facade.LearningGoalContextFacade;
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
import io.github.module.learning.service.LearningGoalContextService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 目标上下文持久化 Facade 实现.
 */
@RequiredArgsConstructor
@DubboService(version = BaseConstant.Version.DUBBO_VERSION_V1,
        validation = BaseConstant.Dubbo.ENABLE_VALIDATION,
        timeout = BaseConstant.Dubbo.TIMEOUT)
public class LearningGoalContextFacadeImpl implements LearningGoalContextFacade {

    private final LearningGoalContextService learningGoalContextService;

    @Override
    public GoalContextBundleBO getGoalContext() {
        return learningGoalContextService.getGoalContext();
    }

    @Override
    public GoalBriefRecordBO saveGoalBriefRecord(AppGoalBriefRecordDTO dto) {
        return learningGoalContextService.saveGoalBriefRecord(dto);
    }

    @Override
    public GoalAdjustmentRecordBO saveGoalAdjustmentRecord(AppGoalAdjustmentRecordDTO dto) {
        return learningGoalContextService.saveGoalAdjustmentRecord(dto);
    }

    @Override
    public GoalExecutionHandoffBO saveActiveGoalExecutionHandoff(AppGoalExecutionHandoffDTO dto) {
        return learningGoalContextService.saveActiveGoalExecutionHandoff(dto);
    }

    @Override
    public void clearActiveGoalExecutionHandoff() {
        learningGoalContextService.clearActiveGoalExecutionHandoff();
    }

    @Override
    public PortfolioCandidateValidationRecordBO savePortfolioCandidateValidationRecord(
            AppPortfolioCandidateValidationRecordDTO dto) {
        return learningGoalContextService.savePortfolioCandidateValidationRecord(dto);
    }

    @Override
    public GoalCheckpointRecordBO saveGoalCheckpointRecord(AppGoalCheckpointRecordDTO dto) {
        return learningGoalContextService.saveGoalCheckpointRecord(dto);
    }

    @Override
    public GoalTuningSnapshotBO saveGoalTuningSnapshot(AppGoalTuningSnapshotDTO dto) {
        return learningGoalContextService.saveGoalTuningSnapshot(dto);
    }

    @Override
    public void clearGoalTuningSnapshot() {
        learningGoalContextService.clearGoalTuningSnapshot();
    }
}
