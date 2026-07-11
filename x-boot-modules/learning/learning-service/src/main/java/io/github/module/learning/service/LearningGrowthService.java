package io.github.module.learning.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.framework.core.context.UserContextHolder;
import io.github.module.learning.entity.GrowthSnapshotEntity;
import io.github.module.learning.enums.LearningErrorEnum;
import io.github.module.learning.mapper.GrowthSnapshotMapper;
import io.github.module.learning.model.response.GrowthTimelineBO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 成长分析服务.
 */
@RequiredArgsConstructor
@Service
public class LearningGrowthService {

    private final GrowthSnapshotMapper growthSnapshotMapper;
    private final LearningAssembler learningAssembler;

    public GrowthTimelineBO getTimeline() {
        Long userId = UserContextHolder.getUserId();
        LearningErrorEnum.USER_NOT_LOGGED_IN.assertNotNull(userId);
        List<GrowthSnapshotEntity> snapshots = growthSnapshotMapper.selectList(
                new QueryWrapper<GrowthSnapshotEntity>()
                        .lambda()
                        .eq(GrowthSnapshotEntity::getUserId, userId)
                        .orderByDesc(GrowthSnapshotEntity::getSnapshotDate)
                        .orderByDesc(GrowthSnapshotEntity::getCreatedAt)
        );
        return learningAssembler.toGrowthTimelineBO(snapshots);
    }
}
