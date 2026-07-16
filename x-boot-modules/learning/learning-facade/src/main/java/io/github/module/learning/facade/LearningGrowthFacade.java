package io.github.module.learning.facade;

import io.github.module.learning.model.response.GrowthTimelineBO;
import io.github.module.learning.model.response.LearnerMemoryBO;
import io.github.module.learning.model.response.LearningKnowledgeGraphBO;
import io.github.module.learning.model.response.LearningRhythmBO;
import io.github.module.learning.model.response.MasteryRecordBO;

import java.util.List;

/**
 * 成长 Facade.
 */
public interface LearningGrowthFacade {

    /**
     * 获取成长时间线.
     */
    GrowthTimelineBO getTimeline();

    /**
     * 获取节点掌握记录.
     */
    List<MasteryRecordBO> getMasteryRecords(Long goalId);

    /**
     * 获取学习者长期记忆快照.
     */
    LearnerMemoryBO getLearnerMemory(Long goalId);

    /**
     * 获取学习知识图谱快照.
     */
    LearningKnowledgeGraphBO getLearningKnowledgeGraph(Long goalId);

    /**
     * 获取学习节奏快照.
     */
    LearningRhythmBO getLearningRhythm(Long goalId);
}
