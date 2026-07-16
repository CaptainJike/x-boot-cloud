package io.github.module.learning.biz;

import io.github.framework.core.constant.BaseConstant;
import io.github.module.learning.facade.LearningGrowthFacade;
import io.github.module.learning.model.response.GrowthTimelineBO;
import io.github.module.learning.model.response.LearnerMemoryBO;
import io.github.module.learning.model.response.LearningKnowledgeGraphBO;
import io.github.module.learning.model.response.LearningRhythmBO;
import io.github.module.learning.model.response.MasteryRecordBO;
import io.github.module.learning.service.LearningGrowthService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

/**
 * 成长 Facade 实现.
 */
@RequiredArgsConstructor
@DubboService(version = BaseConstant.Version.DUBBO_VERSION_V1,
        validation = BaseConstant.Dubbo.ENABLE_VALIDATION,
        timeout = BaseConstant.Dubbo.TIMEOUT)
public class LearningGrowthFacadeImpl implements LearningGrowthFacade {

    private final LearningGrowthService learningGrowthService;

    @Override
    public GrowthTimelineBO getTimeline() {
        return learningGrowthService.getTimeline();
    }

    @Override
    public List<MasteryRecordBO> getMasteryRecords(Long goalId) {
        return learningGrowthService.getMasteryRecords(goalId);
    }

    @Override
    public LearnerMemoryBO getLearnerMemory(Long goalId) {
        return learningGrowthService.getLearnerMemory(goalId);
    }

    @Override
    public LearningKnowledgeGraphBO getLearningKnowledgeGraph(Long goalId) {
        return learningGrowthService.getLearningKnowledgeGraph(goalId);
    }

    @Override
    public LearningRhythmBO getLearningRhythm(Long goalId) {
        return learningGrowthService.getLearningRhythm(goalId);
    }
}
