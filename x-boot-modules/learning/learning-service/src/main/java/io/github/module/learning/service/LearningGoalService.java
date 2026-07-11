package io.github.module.learning.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.framework.core.context.UserContextHolder;
import io.github.framework.core.exception.BusinessException;
import io.github.module.learning.entity.DailyDigestEntity;
import io.github.module.learning.entity.LearnerProfileEntity;
import io.github.module.learning.entity.LearningGoalEntity;
import io.github.module.learning.entity.LearningMapEntity;
import io.github.module.learning.entity.LearningMapNodeEntity;
import io.github.module.learning.entity.LearningNodeProgressEntity;
import io.github.module.learning.entity.ReflectionEntryEntity;
import io.github.module.learning.enums.LearningErrorEnum;
import io.github.module.learning.mapper.DailyDigestMapper;
import io.github.module.learning.mapper.LearnerProfileMapper;
import io.github.module.learning.mapper.LearningGoalMapper;
import io.github.module.learning.mapper.LearningMapMapper;
import io.github.module.learning.mapper.LearningMapNodeMapper;
import io.github.module.learning.mapper.LearningNodeProgressMapper;
import io.github.module.learning.mapper.ReflectionEntryMapper;
import io.github.module.learning.model.request.AppCreateLearningGoalDTO;
import io.github.module.learning.model.response.LearningMapBO;
import io.github.module.learning.model.response.TodayLearningBO;
import io.github.module.learning.service.model.GeneratedLearningMap;
import io.github.module.learning.service.model.LearningTemplate;
import io.github.module.learning.service.model.LearningTemplateNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 学习目标服务.
 */
@RequiredArgsConstructor
@Service
public class LearningGoalService {

    private final LearnerProfileMapper learnerProfileMapper;
    private final LearningGoalMapper learningGoalMapper;
    private final LearningMapMapper learningMapMapper;
    private final LearningMapNodeMapper learningMapNodeMapper;
    private final LearningNodeProgressMapper learningNodeProgressMapper;
    private final ReflectionEntryMapper reflectionEntryMapper;
    private final DailyDigestMapper dailyDigestMapper;
    private final LearningTemplateService learningTemplateService;
    private final LearningAiService learningAiService;
    private final LearningAssembler learningAssembler;

    @Transactional(rollbackFor = Exception.class)
    public LearningMapBO createGoal(AppCreateLearningGoalDTO dto) {
        Long userId = requireUserId();
        LearningTemplate template = learningTemplateService.matchTemplate(dto.getTargetTopic());
        GeneratedLearningMap generatedMap = learningAiService.generateLearningMap(
                dto.getTargetTopic(),
                dto.getSelfAssessment(),
                dto.getWeeklyLearningMinutes(),
                dto.getPreferredLearningStyle(),
                template
        );

        upsertLearnerProfile(userId, dto);

        LearningGoalEntity goalEntity = LearningGoalEntity.builder()
                .userId(userId)
                .targetTopic(dto.getTargetTopic())
                .selfAssessment(dto.getSelfAssessment())
                .weeklyLearningMinutes(dto.getWeeklyLearningMinutes())
                .preferredLearningStyle(dto.getPreferredLearningStyle())
                .templateCode(template.getTemplateCode())
                .status("ACTIVE")
                .estimatedDays(generatedMap.getEstimatedDays())
                .build();
        learningGoalMapper.insert(goalEntity);

        LearningMapEntity mapEntity = LearningMapEntity.builder()
                .goalId(goalEntity.getId())
                .userId(userId)
                .generationVersion(1)
                .generationSummary(generatedMap.getGenerationSummary())
                .build();
        learningMapMapper.insert(mapEntity);

        List<LearningMapNodeEntity> nodeEntities = persistNodes(goalEntity, mapEntity, generatedMap.getNodes());
        if (CollUtil.isNotEmpty(nodeEntities)) {
            goalEntity.setActiveNodeId(nodeEntities.getFirst().getId());
            learningGoalMapper.updateById(goalEntity);
        }

        List<LearningNodeProgressEntity> progressEntities = initProgress(goalEntity, nodeEntities);
        return learningAssembler.toMapBO(goalEntity, mapEntity, nodeEntities, progressEntities);
    }

    public TodayLearningBO getToday() {
        Long userId = requireUserId();
        ensureLearnerProfile(userId);
        LearningGoalEntity goalEntity = getLatestActiveGoal(userId);
        if (goalEntity == null) {
            return TodayLearningBO.builder()
                    .recommendedActions(List.of("先创建一个学习目标，开始你的 Learning OS 闭环。"))
                    .reflectedToday(Boolean.FALSE)
                    .build();
        }

        LearningMapNodeEntity currentNode = getCurrentNode(goalEntity);
        ReflectionEntryEntity reflectionEntity = reflectionEntryMapper.selectOne(
                new QueryWrapper<ReflectionEntryEntity>()
                        .lambda()
                        .eq(ReflectionEntryEntity::getUserId, userId)
                        .eq(ReflectionEntryEntity::getReflectionDate, LocalDate.now())
                        .last(" LIMIT 1")
        );
        DailyDigestEntity digestEntity = reflectionEntity == null ? null : dailyDigestMapper.selectOne(
                new QueryWrapper<DailyDigestEntity>()
                        .lambda()
                        .eq(DailyDigestEntity::getReflectionEntryId, reflectionEntity.getId())
                        .last(" LIMIT 1")
        );
        List<String> actions = buildRecommendedActions(currentNode, reflectionEntity != null);
        return learningAssembler.toTodayBO(
                goalEntity,
                currentNode,
                learningAssembler.toReflectionBO(reflectionEntity, digestEntity),
                actions
        );
    }

    public LearningMapBO getMapByGoalId(Long goalId) {
        Long userId = requireUserId();
        LearningGoalEntity goalEntity = learningGoalMapper.selectOne(
                new QueryWrapper<LearningGoalEntity>()
                        .lambda()
                        .eq(LearningGoalEntity::getId, goalId)
                        .eq(LearningGoalEntity::getUserId, userId)
                        .last(" LIMIT 1")
        );
        LearningErrorEnum.INVALID_GOAL.assertNotNull(goalEntity);

        LearningMapEntity mapEntity = learningMapMapper.selectOne(
                new QueryWrapper<LearningMapEntity>()
                        .lambda()
                        .eq(LearningMapEntity::getGoalId, goalId)
                        .eq(LearningMapEntity::getUserId, userId)
                        .last(" LIMIT 1")
        );
        List<LearningMapNodeEntity> nodeEntities = learningMapNodeMapper.selectList(
                new QueryWrapper<LearningMapNodeEntity>()
                        .lambda()
                        .eq(LearningMapNodeEntity::getGoalId, goalId)
                        .eq(LearningMapNodeEntity::getUserId, userId)
                        .orderByAsc(LearningMapNodeEntity::getSortOrder)
        );
        List<LearningNodeProgressEntity> progressEntities = learningNodeProgressMapper.selectList(
                new QueryWrapper<LearningNodeProgressEntity>()
                        .lambda()
                        .eq(LearningNodeProgressEntity::getGoalId, goalId)
                        .eq(LearningNodeProgressEntity::getUserId, userId)
        );
        return learningAssembler.toMapBO(goalEntity, mapEntity, nodeEntities, progressEntities);
    }

    @Transactional(rollbackFor = Exception.class)
    public void ensureLearnerProfileForCurrentUser() {
        ensureLearnerProfile(requireUserId());
    }

    public LearningGoalEntity getLatestActiveGoalForCurrentUser() {
        return getLatestActiveGoal(requireUserId());
    }

    public LearningGoalEntity getGoalById(Long goalId) {
        return learningGoalMapper.selectById(goalId);
    }

    public LearningGoalEntity getOwnedGoalById(Long goalId, Long userId) {
        if (goalId == null || userId == null) {
            return null;
        }
        return learningGoalMapper.selectOne(
                new QueryWrapper<LearningGoalEntity>()
                        .lambda()
                        .eq(LearningGoalEntity::getId, goalId)
                        .eq(LearningGoalEntity::getUserId, userId)
                        .last(" LIMIT 1")
        );
    }

    public LearningMapNodeEntity getNodeById(Long nodeId) {
        return learningMapNodeMapper.selectById(nodeId);
    }

    public LearningMapNodeEntity getOwnedNodeById(Long nodeId, Long userId) {
        if (nodeId == null || userId == null) {
            return null;
        }
        return learningMapNodeMapper.selectOne(
                new QueryWrapper<LearningMapNodeEntity>()
                        .lambda()
                        .eq(LearningMapNodeEntity::getId, nodeId)
                        .eq(LearningMapNodeEntity::getUserId, userId)
                        .last(" LIMIT 1")
        );
    }

    public LearningMapNodeEntity getCurrentNode(LearningGoalEntity goalEntity) {
        if (goalEntity == null || goalEntity.getActiveNodeId() == null) {
            return null;
        }
        return learningMapNodeMapper.selectById(goalEntity.getActiveNodeId());
    }

    public void updateGoal(LearningGoalEntity goalEntity) {
        learningGoalMapper.updateById(goalEntity);
    }

    public LearningNodeProgressEntity getProgress(Long goalId, Long nodeId, Long userId) {
        return learningNodeProgressMapper.selectOne(
                new QueryWrapper<LearningNodeProgressEntity>()
                        .lambda()
                        .eq(LearningNodeProgressEntity::getGoalId, goalId)
                        .eq(LearningNodeProgressEntity::getMapNodeId, nodeId)
                        .eq(LearningNodeProgressEntity::getUserId, userId)
                        .last(" LIMIT 1")
        );
    }

    public void updateProgress(LearningNodeProgressEntity progressEntity) {
        learningNodeProgressMapper.updateById(progressEntity);
    }

    public List<LearningMapNodeEntity> listNodesByGoalId(Long goalId, Long userId) {
        return learningMapNodeMapper.selectList(
                new QueryWrapper<LearningMapNodeEntity>()
                        .lambda()
                        .eq(LearningMapNodeEntity::getGoalId, goalId)
                        .eq(LearningMapNodeEntity::getUserId, userId)
                        .orderByAsc(LearningMapNodeEntity::getSortOrder)
        );
    }

    private Long requireUserId() {
        Long userId = UserContextHolder.getUserId();
        LearningErrorEnum.USER_NOT_LOGGED_IN.assertNotNull(userId);
        return userId;
    }

    private void upsertLearnerProfile(Long userId, AppCreateLearningGoalDTO dto) {
        LearnerProfileEntity existing = learnerProfileMapper.selectOne(
                new QueryWrapper<LearnerProfileEntity>()
                        .lambda()
                        .eq(LearnerProfileEntity::getUserId, userId)
                        .last(" LIMIT 1")
        );
        if (existing == null) {
            learnerProfileMapper.insert(LearnerProfileEntity.builder()
                    .userId(userId)
                    .preferredLearningStyle(dto.getPreferredLearningStyle())
                    .latestSelfAssessment(dto.getSelfAssessment())
                    .focusArea(dto.getTargetTopic())
                    .build());
            return;
        }
        existing.setPreferredLearningStyle(dto.getPreferredLearningStyle());
        existing.setLatestSelfAssessment(dto.getSelfAssessment());
        existing.setFocusArea(dto.getTargetTopic());
        learnerProfileMapper.updateById(existing);
    }

    private void ensureLearnerProfile(Long userId) {
        LearnerProfileEntity existing = learnerProfileMapper.selectOne(
                new QueryWrapper<LearnerProfileEntity>()
                        .lambda()
                        .eq(LearnerProfileEntity::getUserId, userId)
                        .last(" LIMIT 1")
        );
        if (existing != null) {
            return;
        }
        learnerProfileMapper.insert(LearnerProfileEntity.builder()
                .userId(userId)
                .preferredLearningStyle("")
                .latestSelfAssessment("")
                .focusArea("")
                .build());
    }

    private List<LearningMapNodeEntity> persistNodes(LearningGoalEntity goalEntity,
                                                     LearningMapEntity mapEntity,
                                                     List<LearningTemplateNode> nodes) {
        List<LearningMapNodeEntity> nodeEntities = new ArrayList<>();
        int sort = 0;
        for (LearningTemplateNode node : CollUtil.emptyIfNull(nodes)) {
            LearningMapNodeEntity entity = LearningMapNodeEntity.builder()
                    .goalId(goalEntity.getId())
                    .mapId(mapEntity.getId())
                    .userId(goalEntity.getUserId())
                    .nodeCode(node.getNodeCode())
                    .title(node.getTitle())
                    .description(node.getDescription())
                    .learningObjective(node.getLearningObjective())
                    .whyItMatters(node.getWhyItMatters())
                    .estimatedMinutes(node.getEstimatedMinutes())
                    .difficultyLevel(node.getDifficultyLevel())
                    .verificationMethod(node.getVerificationMethod())
                    .completionCriteria(node.getCompletionCriteria())
                    .prerequisiteNodeCodes(String.join(",", CollUtil.emptyIfNull(node.getPrerequisiteNodeCodes())))
                    .sortOrder(sort++)
                    .build();
            learningMapNodeMapper.insert(entity);
            nodeEntities.add(entity);
        }
        nodeEntities.sort(Comparator.comparing(LearningMapNodeEntity::getSortOrder));
        return nodeEntities;
    }

    private List<LearningNodeProgressEntity> initProgress(LearningGoalEntity goalEntity, List<LearningMapNodeEntity> nodes) {
        List<LearningNodeProgressEntity> progressEntities = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            LearningMapNodeEntity node = nodes.get(i);
            LearningNodeProgressEntity progress = LearningNodeProgressEntity.builder()
                    .goalId(goalEntity.getId())
                    .mapNodeId(node.getId())
                    .userId(goalEntity.getUserId())
                    .status(i == 0 ? "READY" : "PENDING")
                    .masteryLevel(0)
                    .build();
            learningNodeProgressMapper.insert(progress);
            progressEntities.add(progress);
        }
        return progressEntities;
    }

    private LearningGoalEntity getLatestActiveGoal(Long userId) {
        return learningGoalMapper.selectOne(
                new QueryWrapper<LearningGoalEntity>()
                        .lambda()
                        .eq(LearningGoalEntity::getUserId, userId)
                        .eq(LearningGoalEntity::getStatus, "ACTIVE")
                        .orderByDesc(LearningGoalEntity::getUpdatedAt)
                        .last(" LIMIT 1")
        );
    }

    private List<String> buildRecommendedActions(LearningMapNodeEntity currentNode, boolean reflectedToday) {
        if (currentNode == null) {
            return List.of("先创建一个学习目标。");
        }
        List<String> actions = new ArrayList<>();
        actions.add("先围绕「" + currentNode.getTitle() + "」完成一轮学习。");
        if (CharSequenceUtil.isNotBlank(currentNode.getVerificationMethod())) {
            actions.add("完成后用以下方式验证：" + currentNode.getVerificationMethod());
        }
        actions.add(reflectedToday ? "今天的反思已完成，继续保持学习节奏。" : "今晚记得提交一次 Reflection，总结今天的认知变化。");
        return actions;
    }
}
