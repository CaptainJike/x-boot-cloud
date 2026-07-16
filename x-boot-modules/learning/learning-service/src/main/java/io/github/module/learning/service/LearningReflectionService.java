package io.github.module.learning.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.framework.core.context.UserContextHolder;
import io.github.module.learning.entity.DailyDigestEntity;
import io.github.module.learning.entity.GrowthSnapshotEntity;
import io.github.module.learning.entity.LearningGoalEntity;
import io.github.module.learning.entity.ReflectionEntryEntity;
import io.github.module.learning.enums.LearningErrorEnum;
import io.github.module.learning.mapper.DailyDigestMapper;
import io.github.module.learning.mapper.GrowthSnapshotMapper;
import io.github.module.learning.mapper.ReflectionEntryMapper;
import io.github.module.learning.model.request.AppSubmitDailyReflectionDTO;
import io.github.module.learning.model.response.DailyReflectionBO;
import io.github.module.learning.service.model.ReflectionSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 每日反思服务.
 */
@RequiredArgsConstructor
@Service
public class LearningReflectionService {

    private final ReflectionEntryMapper reflectionEntryMapper;
    private final DailyDigestMapper dailyDigestMapper;
    private final GrowthSnapshotMapper growthSnapshotMapper;
    private final LearningGoalService learningGoalService;
    private final LearningAiService learningAiService;
    private final LearningEventService learningEventService;
    private final LearningAssembler learningAssembler;

    @Transactional(rollbackFor = Exception.class)
    public DailyReflectionBO submitDailyReflection(AppSubmitDailyReflectionDTO dto) {
        Long userId = requireUserId();
        ReflectionEntryEntity existing = reflectionEntryMapper.selectOne(
                new QueryWrapper<ReflectionEntryEntity>()
                        .lambda()
                        .eq(ReflectionEntryEntity::getUserId, userId)
                        .eq(ReflectionEntryEntity::getReflectionDate, LocalDate.now())
                        .last(" LIMIT 1")
        );
        if (existing != null) {
            throw new io.github.framework.core.exception.BusinessException(LearningErrorEnum.REFLECTION_ALREADY_SUBMITTED);
        }

        LearningGoalEntity goalEntity = learningGoalService.getLatestActiveGoalForCurrentUser();

        ReflectionEntryEntity reflectionEntity = ReflectionEntryEntity.builder()
                .userId(userId)
                .goalId(goalEntity == null ? null : goalEntity.getId())
                .reflectionDate(LocalDate.now())
                .learnedToday(dto.getLearnedToday())
                .biggestInsight(dto.getBiggestInsight())
                .newAwareness(dto.getNewAwareness())
                .unresolvedQuestion(dto.getUnresolvedQuestion())
                .whyStuck(dto.getWhyStuck())
                .build();
        reflectionEntryMapper.insert(reflectionEntity);

        ReflectionSummary summary = learningAiService.summarizeReflection(
                dto.getLearnedToday(),
                dto.getBiggestInsight(),
                dto.getNewAwareness(),
                dto.getUnresolvedQuestion(),
                dto.getWhyStuck()
        );

        DailyDigestEntity digestEntity = DailyDigestEntity.builder()
                .userId(userId)
                .goalId(goalEntity == null ? null : goalEntity.getId())
                .reflectionEntryId(reflectionEntity.getId())
                .digestDate(LocalDate.now())
                .summary(summary.getSummary())
                .nextAction(summary.getNextAction())
                .build();
        dailyDigestMapper.insert(digestEntity);

        learningEventService.recordReflectionSubmitted(goalEntity, reflectionEntity, digestEntity);
        persistGrowthSnapshots(userId, goalEntity == null ? null : goalEntity.getId(), summary);
        return learningAssembler.toReflectionBO(reflectionEntity, digestEntity);
    }

    private void persistGrowthSnapshots(Long userId, Long goalId, ReflectionSummary summary) {
        List<GrowthSnapshotEntity> snapshots = new ArrayList<>();
        snapshots.add(GrowthSnapshotEntity.builder()
                .userId(userId)
                .goalId(goalId)
                .snapshotDate(LocalDate.now())
                .eventType("REFLECTION")
                .title("每日反思")
                .summary(summary.getSummary())
                .build());
        snapshots.addAll(CollUtil.emptyIfNull(summary.getKeyCognitiveChanges()).stream()
                .map(item -> GrowthSnapshotEntity.builder()
                        .userId(userId)
                        .goalId(goalId)
                        .snapshotDate(LocalDate.now())
                        .eventType("COGNITION")
                        .title("认知变化")
                        .summary(item)
                        .build())
                .toList());
        snapshots.addAll(CollUtil.emptyIfNull(summary.getCommonStickingPoints()).stream()
                .map(item -> GrowthSnapshotEntity.builder()
                        .userId(userId)
                        .goalId(goalId)
                        .snapshotDate(LocalDate.now())
                        .eventType("STUCK")
                        .title("常见卡点")
                        .summary(item)
                        .build())
                .toList());
        snapshots.forEach(growthSnapshotMapper::insert);
    }

    private Long requireUserId() {
        Long userId = UserContextHolder.getUserId();
        LearningErrorEnum.USER_NOT_LOGGED_IN.assertNotNull(userId);
        return userId;
    }
}
