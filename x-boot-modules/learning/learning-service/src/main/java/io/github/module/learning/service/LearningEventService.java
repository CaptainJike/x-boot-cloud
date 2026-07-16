package io.github.module.learning.service;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import io.github.module.learning.entity.DailyDigestEntity;
import io.github.module.learning.entity.LearningEventEntity;
import io.github.module.learning.entity.LearningGoalEntity;
import io.github.module.learning.entity.LearningMapNodeEntity;
import io.github.module.learning.entity.PracticeAttemptEntity;
import io.github.module.learning.entity.PracticeTaskEntity;
import io.github.module.learning.entity.ReflectionEntryEntity;
import io.github.module.learning.entity.ReviewAttemptEntity;
import io.github.module.learning.entity.ReviewTaskEntity;
import io.github.module.learning.entity.TutorSessionEntity;
import io.github.module.learning.entity.TutorTurnEntity;
import io.github.module.learning.mapper.LearningEventMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 统一学习事件写入服务.
 */
@RequiredArgsConstructor
@Service
public class LearningEventService {

    private final LearningEventMapper learningEventMapper;

    public void recordTutorSessionStarted(LearningGoalEntity goal,
                                          LearningMapNodeEntity node,
                                          TutorSessionEntity session,
                                          TutorTurnEntity firstTurn) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sessionId", session.getId());
        payload.put("turnNo", firstTurn.getTurnNo());
        payload.put("diagnosis", firstTurn.getDiagnosis());
        payload.put("actionType", firstTurn.getActionType());
        payload.put("recommendedNodeId", firstTurn.getRecommendedNodeId());
        payload.put("nodeCompleted", firstTurn.getNodeCompleted());
        payload.put("nextStepSuggestions", parseStringArray(firstTurn.getNextStepSuggestionsJson()));
        payload.put("learnerQuestion", session.getLearnerQuestion());

        insertEvent(LearningEventEntity.builder()
                .userId(session.getUserId())
                .goalId(session.getGoalId())
                .mapNodeId(session.getMapNodeId())
                .eventSource("TUTOR")
                .eventType("SESSION_STARTED")
                .eventStatus(resolveTutorStatus(firstTurn))
                .title("开始 Tutor 诊断")
                .summary("围绕「" + node.getTitle() + "」发起 Tutor，首轮判断为「" + humanizeDiagnosis(firstTurn.getDiagnosis())
                        + "」，建议动作是「" + humanizeAction(firstTurn.getActionType()) + "」。")
                .relatedEntityType("tutor_session")
                .relatedEntityId(session.getId())
                .eventAt(resolveEventAt(firstTurn.getCreatedAt(), session.getCreatedAt()))
                .payloadJson(JSONUtil.toJsonStr(payload))
                .build());
    }

    public void recordTutorTurn(LearningGoalEntity goal,
                                LearningMapNodeEntity node,
                                TutorTurnEntity turn) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("goalTopic", goal == null ? null : goal.getTargetTopic());
        payload.put("sessionId", turn.getSessionId());
        payload.put("turnNo", turn.getTurnNo());
        payload.put("diagnosis", turn.getDiagnosis());
        payload.put("actionType", turn.getActionType());
        payload.put("recommendedNodeId", turn.getRecommendedNodeId());
        payload.put("nodeCompleted", turn.getNodeCompleted());
        payload.put("nextStepSuggestions", parseStringArray(turn.getNextStepSuggestionsJson()));

        insertEvent(LearningEventEntity.builder()
                .userId(turn.getUserId())
                .goalId(turn.getGoalId())
                .mapNodeId(turn.getMapNodeId())
                .eventSource("TUTOR")
                .eventType("TURN_RECORDED")
                .eventStatus(resolveTutorStatus(turn))
                .title(turn.getNodeCompleted() != null && turn.getNodeCompleted() == 1
                        ? "Tutor 判断当前节点已通过"
                        : "提交 Tutor 回答")
                .summary("围绕「" + node.getTitle() + "」记录了一次 Tutor 轮次，诊断结果是「"
                        + humanizeDiagnosis(turn.getDiagnosis()) + "」，下一步建议「"
                        + humanizeAction(turn.getActionType()) + "」。")
                .relatedEntityType("tutor_turn")
                .relatedEntityId(turn.getId())
                .eventAt(resolveEventAt(turn.getCreatedAt(), turn.getUpdatedAt()))
                .payloadJson(JSONUtil.toJsonStr(payload))
                .build());
    }

    public void recordPracticeAttempt(PracticeTaskEntity task, PracticeAttemptEntity attempt) {
        boolean completed = attempt.getCompleted() != null && attempt.getCompleted() == 1;
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("taskId", task.getId());
        payload.put("taskKey", task.getTaskKey());
        payload.put("taskType", task.getTaskType());
        payload.put("evidenceKind", task.getEvidenceKind());
        payload.put("estimatedMinutes", task.getEstimatedMinutes());
        payload.put("syncVersion", attempt.getSyncVersion());
        payload.put("handoffValidation", attempt.getHandoffValidation());
        payload.put("selfRating", attempt.getSelfRating());
        payload.put("normalizedRating", resolvePracticeRating(attempt));
        payload.put("mutationId", attempt.getLastMutationId());

        insertEvent(LearningEventEntity.builder()
                .userId(attempt.getUserId())
                .goalId(attempt.getGoalId())
                .mapNodeId(attempt.getMapNodeId())
                .eventSource("PRACTICE")
                .eventType("ATTEMPT_SAVED")
                .eventStatus(completed ? "COMPLETED" : "DRAFT")
                .title(completed ? "完成练习任务" : "保存练习草稿")
                .summary((completed ? "围绕「" : "已保存围绕「") + task.getNodeTitle() + "」的「" + task.getTitle()
                        + "」，当前自评为「" + humanizePracticeRating(attempt.getSelfRating()) + "」，版本 v"
                        + attempt.getSyncVersion() + "。")
                .relatedEntityType("practice_attempt")
                .relatedEntityId(attempt.getId())
                .eventAt(resolveEventAt(attempt.getUpdatedAt(), attempt.getCreatedAt()))
                .payloadJson(JSONUtil.toJsonStr(payload))
                .build());
    }

    public void recordReviewAttempt(ReviewTaskEntity task, ReviewAttemptEntity attempt) {
        boolean completed = attempt.getCompleted() != null && attempt.getCompleted() == 1;
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("taskId", task.getId());
        payload.put("taskKey", task.getTaskKey());
        payload.put("taskType", task.getTaskType());
        payload.put("estimatedMinutes", task.getEstimatedMinutes());
        payload.put("scheduledDueAt", attempt.getScheduledDueAt());
        payload.put("intervalDays", attempt.getIntervalDays());
        payload.put("masteryScoreAtAttempt", attempt.getMasteryScoreAtAttempt());
        payload.put("syncVersion", attempt.getSyncVersion());
        payload.put("selfRating", attempt.getSelfRating());
        payload.put("mutationId", attempt.getLastMutationId());

        insertEvent(LearningEventEntity.builder()
                .userId(attempt.getUserId())
                .goalId(attempt.getGoalId())
                .mapNodeId(attempt.getMapNodeId())
                .eventSource("REVIEW")
                .eventType("ATTEMPT_SAVED")
                .eventStatus(completed ? "COMPLETED" : "DRAFT")
                .title(completed ? "完成复盘任务" : "保存复盘草稿")
                .summary((completed ? "围绕「" : "已保存围绕「") + task.getNodeTitle() + "」的「" + task.getTitle()
                        + "」，当前自评为「" + humanizeReviewRating(attempt.getSelfRating()) + "」，版本 v"
                        + attempt.getSyncVersion() + "。")
                .relatedEntityType("review_attempt")
                .relatedEntityId(attempt.getId())
                .eventAt(resolveEventAt(attempt.getUpdatedAt(), attempt.getCreatedAt()))
                .payloadJson(JSONUtil.toJsonStr(payload))
                .build());
    }

    public void recordReflectionSubmitted(LearningGoalEntity goal,
                                          ReflectionEntryEntity reflection,
                                          DailyDigestEntity digest) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("goalTopic", goal == null ? null : goal.getTargetTopic());
        payload.put("reflectionDate", reflection.getReflectionDate());
        payload.put("nextAction", digest == null ? null : digest.getNextAction());

        insertEvent(LearningEventEntity.builder()
                .userId(reflection.getUserId())
                .goalId(reflection.getGoalId())
                .mapNodeId(null)
                .eventSource("REFLECTION")
                .eventType("REFLECTION_SUBMITTED")
                .eventStatus("COMPLETED")
                .title("完成每日 Reflection")
                .summary(CharSequenceUtil.blankToDefault(
                        digest == null ? null : digest.getSummary(),
                        "今天的学习已经完成一次反思封箱。"))
                .relatedEntityType("reflection_entry")
                .relatedEntityId(reflection.getId())
                .eventAt(resolveEventAt(reflection.getCreatedAt(), digest == null ? null : digest.getCreatedAt()))
                .payloadJson(JSONUtil.toJsonStr(payload))
                .build());
    }

    private void insertEvent(LearningEventEntity entity) {
        learningEventMapper.insert(entity);
    }

    private LocalDateTime resolveEventAt(LocalDateTime primary, LocalDateTime secondary) {
        if (primary != null) {
            return primary;
        }
        if (secondary != null) {
            return secondary;
        }
        return LocalDateTime.now();
    }

    private String resolveTutorStatus(TutorTurnEntity turn) {
        if (turn.getNodeCompleted() != null && turn.getNodeCompleted() == 1) {
            return "COMPLETED";
        }
        return CharSequenceUtil.blankToDefault(turn.getDiagnosis(), "ACTIVE").toUpperCase();
    }

    private String humanizeDiagnosis(String diagnosis) {
        return switch (diagnosis) {
            case "ready" -> "准备充分";
            case "needs_prereq" -> "需要补前置";
            case "misconception" -> "存在概念误解";
            default -> CharSequenceUtil.blankToDefault(diagnosis, "待诊断");
        };
    }

    private String humanizeAction(String actionType) {
        return switch (actionType) {
            case "diagnose" -> "继续诊断";
            case "explain" -> "进入讲解";
            case "redirect" -> "调整路径";
            default -> CharSequenceUtil.blankToDefault(actionType, "待执行");
        };
    }

    private String humanizePracticeRating(String selfRating) {
        return switch (selfRating) {
            case "clear" -> "已经比较清楚";
            case "stretch" -> "能做但还不稳";
            case "stuck" -> "这里还是卡住";
            default -> CharSequenceUtil.blankToDefault(selfRating, "待判断");
        };
    }

    private String humanizeReviewRating(String selfRating) {
        return switch (selfRating) {
            case "solid" -> "已经稳住了";
            case "wobbly" -> "想起来但不稳";
            case "forgotten" -> "这里忘了";
            default -> CharSequenceUtil.blankToDefault(selfRating, "待判断");
        };
    }

    private String resolvePracticeRating(PracticeAttemptEntity attempt) {
        if (CharSequenceUtil.isNotBlank(attempt.getAssessmentJson())) {
            try {
                String level = JSONUtil.parseObj(attempt.getAssessmentJson()).getStr("level");
                if (CharSequenceUtil.equals(level, "verified")) {
                    return "clear";
                }
                if (CharSequenceUtil.equals(level, "partial")) {
                    return "stretch";
                }
                if (CharSequenceUtil.equals(level, "needs_work")) {
                    return "stuck";
                }
            } catch (Exception ignored) {
                // fall through to self rating
            }
        }
        return CharSequenceUtil.blankToDefault(attempt.getSelfRating(), "stretch");
    }

    private java.util.List<String> parseStringArray(String json) {
        if (CharSequenceUtil.isBlank(json)) {
            return java.util.Collections.emptyList();
        }
        try {
            return JSONUtil.toList(JSONUtil.parseArray(json), String.class);
        } catch (Exception ignored) {
            return java.util.Collections.emptyList();
        }
    }
}
