package io.github.module.learning.service;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.framework.core.context.UserContextHolder;
import io.github.module.learning.entity.LearningGoalContextRecordEntity;
import io.github.module.learning.enums.LearningErrorEnum;
import io.github.module.learning.mapper.LearningGoalContextRecordMapper;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Goal Builder / Dashboard 上下文持久化服务.
 */
@RequiredArgsConstructor
@Service
public class LearningGoalContextService {

    private static final String RECORD_TYPE_GOAL_BRIEF = "GOAL_BRIEF";
    private static final String RECORD_TYPE_GOAL_ADJUSTMENT = "GOAL_ADJUSTMENT";
    private static final String RECORD_TYPE_ACTIVE_HANDOFF = "ACTIVE_HANDOFF";
    private static final String RECORD_TYPE_PORTFOLIO_VALIDATION = "PORTFOLIO_VALIDATION";
    private static final String RECORD_TYPE_GOAL_CHECKPOINT = "GOAL_CHECKPOINT";
    private static final String RECORD_TYPE_GOAL_TUNING = "GOAL_TUNING";
    private static final String ACTIVE_SCOPE_KEY = "active";

    private final LearningGoalContextRecordMapper learningGoalContextRecordMapper;

    public GoalContextBundleBO getGoalContext() {
        Long userId = requireUserId();
        List<LearningGoalContextRecordEntity> records = learningGoalContextRecordMapper.selectList(
                new QueryWrapper<LearningGoalContextRecordEntity>()
                        .lambda()
                        .eq(LearningGoalContextRecordEntity::getUserId, userId)
                        .in(LearningGoalContextRecordEntity::getRecordType, supportedRecordTypes())
                        .orderByDesc(LearningGoalContextRecordEntity::getUpdatedAt)
        );
        return GoalContextBundleBO.builder()
                .goalBriefRecords(resolveRecords(records, RECORD_TYPE_GOAL_BRIEF, GoalBriefRecordBO.class))
                .goalAdjustmentRecords(resolveRecords(records, RECORD_TYPE_GOAL_ADJUSTMENT, GoalAdjustmentRecordBO.class))
                .activeGoalExecutionHandoff(resolveSingleton(records, RECORD_TYPE_ACTIVE_HANDOFF, GoalExecutionHandoffBO.class))
                .portfolioCandidateValidations(resolveRecords(records, RECORD_TYPE_PORTFOLIO_VALIDATION,
                        PortfolioCandidateValidationRecordBO.class))
                .goalCheckpointRecords(resolveRecords(records, RECORD_TYPE_GOAL_CHECKPOINT, GoalCheckpointRecordBO.class))
                .goalTuningSnapshot(resolveSingleton(records, RECORD_TYPE_GOAL_TUNING, GoalTuningSnapshotBO.class))
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public GoalBriefRecordBO saveGoalBriefRecord(AppGoalBriefRecordDTO dto) {
        upsertRecord(RECORD_TYPE_GOAL_BRIEF,
                buildGoalScopeKey(dto.getGoalId()),
                dto.getGoalId(),
                null,
                dto.getGoalTitle(),
                dto);
        return convert(dto, GoalBriefRecordBO.class);
    }

    @Transactional(rollbackFor = Exception.class)
    public GoalAdjustmentRecordBO saveGoalAdjustmentRecord(AppGoalAdjustmentRecordDTO dto) {
        upsertRecord(RECORD_TYPE_GOAL_ADJUSTMENT,
                dto.getId(),
                dto.getSourceGoalId(),
                dto.getNextGoalId(),
                dto.getCheckpointTitle(),
                dto);
        return convert(dto, GoalAdjustmentRecordBO.class);
    }

    @Transactional(rollbackFor = Exception.class)
    public GoalExecutionHandoffBO saveActiveGoalExecutionHandoff(AppGoalExecutionHandoffDTO dto) {
        upsertRecord(RECORD_TYPE_ACTIVE_HANDOFF,
                ACTIVE_SCOPE_KEY,
                dto.getSourceGoalId(),
                dto.getNextGoalId(),
                dto.getHandoffTitle(),
                dto);
        return convert(dto, GoalExecutionHandoffBO.class);
    }

    @Transactional(rollbackFor = Exception.class)
    public void clearActiveGoalExecutionHandoff() {
        deleteRecord(RECORD_TYPE_ACTIVE_HANDOFF, ACTIVE_SCOPE_KEY);
    }

    @Transactional(rollbackFor = Exception.class)
    public PortfolioCandidateValidationRecordBO savePortfolioCandidateValidationRecord(
            AppPortfolioCandidateValidationRecordDTO dto) {
        upsertRecord(RECORD_TYPE_PORTFOLIO_VALIDATION,
                buildGoalScopeKey(dto.getGoalId()),
                dto.getGoalId(),
                null,
                dto.getGoalTitle(),
                dto);
        return convert(dto, PortfolioCandidateValidationRecordBO.class);
    }

    @Transactional(rollbackFor = Exception.class)
    public GoalCheckpointRecordBO saveGoalCheckpointRecord(AppGoalCheckpointRecordDTO dto) {
        upsertRecord(RECORD_TYPE_GOAL_CHECKPOINT,
                dto.getId(),
                null,
                null,
                dto.getDecisionTitle(),
                dto);
        return convert(dto, GoalCheckpointRecordBO.class);
    }

    @Transactional(rollbackFor = Exception.class)
    public GoalTuningSnapshotBO saveGoalTuningSnapshot(AppGoalTuningSnapshotDTO dto) {
        upsertRecord(RECORD_TYPE_GOAL_TUNING,
                ACTIVE_SCOPE_KEY,
                dto.getSourceGoalId(),
                dto.getCandidateGoalId(),
                dto.getCheckpointTitle(),
                dto);
        return convert(dto, GoalTuningSnapshotBO.class);
    }

    @Transactional(rollbackFor = Exception.class)
    public void clearGoalTuningSnapshot() {
        deleteRecord(RECORD_TYPE_GOAL_TUNING, ACTIVE_SCOPE_KEY);
    }

    private LearningGoalContextRecordEntity upsertRecord(String recordType,
                                                         String scopeKey,
                                                         Long goalId,
                                                         Long relatedGoalId,
                                                         String title,
                                                         Object payload) {
        Long userId = requireUserId();
        LearningGoalContextRecordEntity entity = findRecord(userId, recordType, scopeKey);
        if (entity == null) {
            entity = LearningGoalContextRecordEntity.builder()
                    .userId(userId)
                    .recordType(recordType)
                    .scopeKey(scopeKey)
                    .build();
        }
        entity.setGoalId(goalId);
        entity.setRelatedGoalId(relatedGoalId);
        entity.setTitle(CharSequenceUtil.blankToDefault(title, scopeKey));
        entity.setPayloadJson(JSONUtil.toJsonStr(payload));
        if (entity.getId() == null) {
            learningGoalContextRecordMapper.insert(entity);
        } else {
            learningGoalContextRecordMapper.updateById(entity);
        }
        return entity;
    }

    private void deleteRecord(String recordType, String scopeKey) {
        Long userId = requireUserId();
        LearningGoalContextRecordEntity entity = findRecord(userId, recordType, scopeKey);
        if (entity == null) {
            return;
        }
        learningGoalContextRecordMapper.deleteById(entity.getId());
    }

    private LearningGoalContextRecordEntity findRecord(Long userId, String recordType, String scopeKey) {
        return learningGoalContextRecordMapper.selectOne(
                new QueryWrapper<LearningGoalContextRecordEntity>()
                        .lambda()
                        .eq(LearningGoalContextRecordEntity::getUserId, userId)
                        .eq(LearningGoalContextRecordEntity::getRecordType, recordType)
                        .eq(LearningGoalContextRecordEntity::getScopeKey, scopeKey)
                        .last(" LIMIT 1")
        );
    }

    private List<String> supportedRecordTypes() {
        return List.of(
                RECORD_TYPE_GOAL_BRIEF,
                RECORD_TYPE_GOAL_ADJUSTMENT,
                RECORD_TYPE_ACTIVE_HANDOFF,
                RECORD_TYPE_PORTFOLIO_VALIDATION,
                RECORD_TYPE_GOAL_CHECKPOINT,
                RECORD_TYPE_GOAL_TUNING
        );
    }

    private String buildGoalScopeKey(Long goalId) {
        return "goal:" + goalId;
    }

    private <T> List<T> resolveRecords(List<LearningGoalContextRecordEntity> records, String recordType, Class<T> clazz) {
        return records.stream()
                .filter(record -> Objects.equals(record.getRecordType(), recordType))
                .map(record -> convertFromJson(record.getPayloadJson(), clazz))
                .filter(Objects::nonNull)
                .toList();
    }

    private <T> T resolveSingleton(List<LearningGoalContextRecordEntity> records, String recordType, Class<T> clazz) {
        return records.stream()
                .filter(record -> Objects.equals(record.getRecordType(), recordType))
                .map(record -> convertFromJson(record.getPayloadJson(), clazz))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private <T> T convert(Object source, Class<T> clazz) {
        if (source == null) {
            return null;
        }
        return JSONUtil.toBean(JSONUtil.toJsonStr(source), clazz);
    }

    private <T> T convertFromJson(String json, Class<T> clazz) {
        if (CharSequenceUtil.isBlank(json)) {
            return null;
        }
        try {
            return JSONUtil.toBean(json, clazz);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Long requireUserId() {
        Long userId = UserContextHolder.getUserId();
        LearningErrorEnum.USER_NOT_LOGGED_IN.assertNotNull(userId);
        return userId;
    }
}
