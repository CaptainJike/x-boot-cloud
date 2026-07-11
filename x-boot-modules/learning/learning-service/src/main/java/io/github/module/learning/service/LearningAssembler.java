package io.github.module.learning.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import io.github.module.learning.entity.DailyDigestEntity;
import io.github.module.learning.entity.GrowthSnapshotEntity;
import io.github.module.learning.entity.LearningGoalEntity;
import io.github.module.learning.entity.LearningMapEntity;
import io.github.module.learning.entity.LearningMapNodeEntity;
import io.github.module.learning.entity.LearningNodeProgressEntity;
import io.github.module.learning.entity.ReflectionEntryEntity;
import io.github.module.learning.entity.TutorSessionEntity;
import io.github.module.learning.entity.TutorTurnEntity;
import io.github.module.learning.model.response.DailyReflectionBO;
import io.github.module.learning.model.response.GrowthTimelineBO;
import io.github.module.learning.model.response.GrowthTimelineItemBO;
import io.github.module.learning.model.response.LearningGoalBO;
import io.github.module.learning.model.response.LearningMapBO;
import io.github.module.learning.model.response.LearningMapNodeBO;
import io.github.module.learning.model.response.TodayLearningBO;
import io.github.module.learning.model.response.TutorSessionBO;
import io.github.module.learning.model.response.TutorTurnBO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Learning OS 领域对象组装器.
 */
@Component
public class LearningAssembler {

    public LearningGoalBO toGoalBO(LearningGoalEntity entity) {
        if (entity == null) {
            return null;
        }
        LearningGoalBO bo = new LearningGoalBO();
        BeanUtil.copyProperties(entity, bo);
        return bo;
    }

    public LearningMapNodeBO toMapNodeBO(LearningMapNodeEntity entity, LearningNodeProgressEntity progress, Long activeNodeId) {
        if (entity == null) {
            return null;
        }
        LearningMapNodeBO bo = new LearningMapNodeBO();
        BeanUtil.copyProperties(entity, bo);
        bo.setPrerequisiteNodeCodes(parseStringList(entity.getPrerequisiteNodeCodes()));
        bo.setProgressStatus(progress == null ? "PENDING" : progress.getStatus());
        bo.setActive(Objects.equals(activeNodeId, entity.getId()));
        return bo;
    }

    public LearningMapBO toMapBO(LearningGoalEntity goal,
                                 LearningMapEntity map,
                                 List<LearningMapNodeEntity> nodes,
                                 List<LearningNodeProgressEntity> progressList) {
        Map<Long, LearningNodeProgressEntity> progressMap = CollUtil.emptyIfNull(progressList).stream()
                .collect(Collectors.toMap(LearningNodeProgressEntity::getMapNodeId, item -> item, (a, b) -> b));
        List<LearningMapNodeBO> nodeBOs = CollUtil.emptyIfNull(nodes).stream()
                .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                .map(node -> toMapNodeBO(node, progressMap.get(node.getId()), goal == null ? null : goal.getActiveNodeId()))
                .toList();

        return LearningMapBO.builder()
                .id(map == null ? null : map.getId())
                .goal(toGoalBO(goal))
                .generationVersion(map == null ? null : map.getGenerationVersion())
                .generationSummary(map == null ? null : map.getGenerationSummary())
                .nodes(nodeBOs)
                .build();
    }

    public TutorTurnBO toTutorTurnBO(TutorTurnEntity entity) {
        if (entity == null) {
            return null;
        }
        TutorTurnBO bo = new TutorTurnBO();
        BeanUtil.copyProperties(entity, bo);
        bo.setDiagnosticQuestions(parseStringList(entity.getDiagnosticQuestionsJson()));
        bo.setNextStepSuggestions(parseStringList(entity.getNextStepSuggestionsJson()));
        bo.setNodeCompleted(entity.getNodeCompleted() != null && entity.getNodeCompleted() == 1);
        return bo;
    }

    public TutorSessionBO toTutorSessionBO(TutorSessionEntity session,
                                           LearningMapNodeEntity node,
                                           List<TutorTurnEntity> turns) {
        List<TutorTurnBO> turnBOs = CollUtil.emptyIfNull(turns).stream()
                .sorted((a, b) -> Integer.compare(a.getTurnNo(), b.getTurnNo()))
                .map(this::toTutorTurnBO)
                .toList();
        return TutorSessionBO.builder()
                .id(session.getId())
                .mapNodeId(session.getMapNodeId())
                .nodeTitle(node == null ? null : node.getTitle())
                .status(session.getStatus())
                .turns(turnBOs)
                .latestTurn(turnBOs.isEmpty() ? null : turnBOs.get(turnBOs.size() - 1))
                .build();
    }

    public DailyReflectionBO toReflectionBO(ReflectionEntryEntity entry, DailyDigestEntity digest) {
        if (entry == null) {
            return null;
        }
        DailyReflectionBO bo = new DailyReflectionBO();
        BeanUtil.copyProperties(entry, bo);
        if (digest != null) {
            bo.setDailySummary(digest.getSummary());
            bo.setNextAction(digest.getNextAction());
        }
        return bo;
    }

    public GrowthTimelineBO toGrowthTimelineBO(List<GrowthSnapshotEntity> snapshots) {
        List<GrowthTimelineItemBO> items = CollUtil.emptyIfNull(snapshots).stream()
                .sorted((a, b) -> b.getSnapshotDate().compareTo(a.getSnapshotDate()))
                .map(snapshot -> GrowthTimelineItemBO.builder()
                        .snapshotDate(snapshot.getSnapshotDate())
                        .eventType(snapshot.getEventType())
                        .title(snapshot.getTitle())
                        .summary(snapshot.getSummary())
                        .goalId(snapshot.getGoalId())
                        .build())
                .toList();

        List<String> cognitiveChanges = items.stream()
                .filter(item -> CharSequenceUtil.equals(item.getEventType(), "COGNITION"))
                .map(GrowthTimelineItemBO::getSummary)
                .distinct()
                .limit(5)
                .toList();

        List<String> stickingPoints = items.stream()
                .filter(item -> CharSequenceUtil.equals(item.getEventType(), "STUCK"))
                .map(GrowthTimelineItemBO::getSummary)
                .distinct()
                .limit(5)
                .toList();

        String overview = items.isEmpty()
                ? "还没有成长记录。"
                : "你已经形成了 " + items.size() + " 条成长记录，最近的关注点是持续澄清知识盲区并推进当前学习主题。";

        return GrowthTimelineBO.builder()
                .overview(overview)
                .keyCognitiveChanges(cognitiveChanges)
                .commonStickingPoints(stickingPoints)
                .items(items)
                .build();
    }

    public TodayLearningBO toTodayBO(LearningGoalEntity goal,
                                     LearningMapNodeEntity currentNode,
                                     DailyReflectionBO reflectionBO,
                                     List<String> recommendedActions) {
        return TodayLearningBO.builder()
                .goal(toGoalBO(goal))
                .currentNode(toMapNodeBO(currentNode, null, goal == null ? null : goal.getActiveNodeId()))
                .recommendedActions(CollUtil.emptyIfNull(recommendedActions))
                .reflectedToday(reflectionBO != null)
                .todayReflection(reflectionBO)
                .build();
    }

    private List<String> parseStringList(String json) {
        if (CharSequenceUtil.isBlank(json)) {
            return Collections.emptyList();
        }
        try {
            return JSONUtil.toList(JSONUtil.parseArray(json), String.class);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
