package io.github.module.learning.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.module.learning.entity.LearningEventEntity;
import io.github.module.learning.mapper.LearningEventMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 统一学习事件读侧投影服务.
 */
@RequiredArgsConstructor
@Service
public class LearningEventReadService {

    private final LearningEventMapper learningEventMapper;

    public GoalEventSnapshot loadGoalSnapshot(Long goalId, Long userId) {
        List<LearningEventEntity> events = learningEventMapper.selectList(
                new QueryWrapper<LearningEventEntity>()
                        .lambda()
                        .eq(LearningEventEntity::getGoalId, goalId)
                        .eq(LearningEventEntity::getUserId, userId)
                        .orderByAsc(LearningEventEntity::getEventAt)
                        .orderByAsc(LearningEventEntity::getCreatedAt)
        );
        return projectGoalEvents(events);
    }

    public List<LearningEventEntity> loadUserEvents(Long userId) {
        return learningEventMapper.selectList(
                new QueryWrapper<LearningEventEntity>()
                        .lambda()
                        .eq(LearningEventEntity::getUserId, userId)
                        .orderByDesc(LearningEventEntity::getEventAt)
                        .orderByDesc(LearningEventEntity::getCreatedAt)
        );
    }

    public GoalEventSnapshot projectGoalEvents(List<LearningEventEntity> events) {
        return buildSnapshot(events);
    }

    private GoalEventSnapshot buildSnapshot(List<LearningEventEntity> events) {
        Map<Long, TutorEventProjection> latestTutorByNodeId = CollUtil.emptyIfNull(events).stream()
                .filter(event -> CharSequenceUtil.equals(event.getEventSource(), "TUTOR"))
                .map(this::toTutorEvent)
                .filter(Objects::nonNull)
                .filter(event -> event.nodeId() != null)
                .collect(Collectors.toMap(
                        TutorEventProjection::nodeId,
                        Function.identity(),
                        this::newerTutorEvent));

        Map<Long, TutorEventProjection> latestTutorBySessionId = CollUtil.emptyIfNull(events).stream()
                .filter(event -> CharSequenceUtil.equals(event.getEventSource(), "TUTOR"))
                .map(this::toTutorEvent)
                .filter(Objects::nonNull)
                .filter(event -> event.sessionId() != null)
                .collect(Collectors.toMap(
                        TutorEventProjection::sessionId,
                        Function.identity(),
                        this::newerTutorEvent));

        Map<Long, PracticeAttemptEventProjection> latestPracticeByAttemptId = CollUtil.emptyIfNull(events).stream()
                .filter(event -> CharSequenceUtil.equals(event.getEventSource(), "PRACTICE"))
                .map(this::toPracticeAttemptEvent)
                .filter(Objects::nonNull)
                .filter(event -> event.attemptId() != null)
                .collect(Collectors.toMap(
                        PracticeAttemptEventProjection::attemptId,
                        Function.identity(),
                        this::newerPracticeEvent));

        Map<Long, ReviewAttemptEventProjection> latestReviewByAttemptId = CollUtil.emptyIfNull(events).stream()
                .filter(event -> CharSequenceUtil.equals(event.getEventSource(), "REVIEW"))
                .map(this::toReviewAttemptEvent)
                .filter(Objects::nonNull)
                .filter(event -> event.attemptId() != null)
                .collect(Collectors.toMap(
                        ReviewAttemptEventProjection::attemptId,
                        Function.identity(),
                        this::newerReviewEvent));

        Map<Long, ReflectionEventProjection> latestReflectionByEntryId = CollUtil.emptyIfNull(events).stream()
                .filter(event -> CharSequenceUtil.equals(event.getEventSource(), "REFLECTION"))
                .map(this::toReflectionEvent)
                .filter(Objects::nonNull)
                .filter(event -> event.reflectionEntryId() != null)
                .collect(Collectors.toMap(
                        ReflectionEventProjection::reflectionEntryId,
                        Function.identity(),
                        this::newerReflectionEvent));

        return new GoalEventSnapshot(
                CollUtil.emptyIfNull(events),
                latestTutorByNodeId,
                latestTutorBySessionId,
                latestPracticeByAttemptId,
                latestReviewByAttemptId,
                latestReflectionByEntryId);
    }

    private TutorEventProjection toTutorEvent(LearningEventEntity event) {
        JSONObject payload = parsePayload(event.getPayloadJson());
        Long sessionId = payload == null ? null : payload.getLong("sessionId");
        Integer turnNo = payload == null ? null : payload.getInt("turnNo");
        String diagnosis = payload == null ? null : payload.getStr("diagnosis");
        String actionType = payload == null ? null : payload.getStr("actionType");
        Long recommendedNodeId = payload == null ? null : payload.getLong("recommendedNodeId");
        Boolean nodeCompleted = payload == null ? null : toBoolean(payload.get("nodeCompleted"));
        List<String> nextStepSuggestions = parseStringArray(payload, "nextStepSuggestions");
        return new TutorEventProjection(
                event.getMapNodeId(),
                sessionId,
                diagnosis,
                actionType,
                recommendedNodeId,
                Boolean.TRUE.equals(nodeCompleted),
                turnNo,
                resolveEventAt(event),
                nextStepSuggestions);
    }

    private PracticeAttemptEventProjection toPracticeAttemptEvent(LearningEventEntity event) {
        JSONObject payload = parsePayload(event.getPayloadJson());
        String rating = payload == null ? null : CharSequenceUtil.blankToDefault(
                payload.getStr("normalizedRating"),
                payload.getStr("selfRating"));
        Integer minutes = payload == null ? null : payload.getInt("estimatedMinutes");
        Boolean handoffValidation = payload == null ? null : toBoolean(payload.get("handoffValidation"));
        return new PracticeAttemptEventProjection(
                event.getRelatedEntityId(),
                event.getMapNodeId(),
                rating,
                payload == null ? null : payload.getStr("taskType"),
                payload == null ? null : payload.getStr("evidenceKind"),
                minutes,
                Boolean.TRUE.equals(handoffValidation),
                CharSequenceUtil.equals(event.getEventStatus(), "COMPLETED"),
                resolveEventAt(event));
    }

    private ReflectionEventProjection toReflectionEvent(LearningEventEntity event) {
        JSONObject payload = parsePayload(event.getPayloadJson());
        return new ReflectionEventProjection(
                event.getRelatedEntityId(),
                event.getSummary(),
                payload == null ? null : payload.getStr("nextAction"),
                parseLocalDate(payload == null ? null : payload.getStr("reflectionDate")),
                resolveEventAt(event));
    }

    private ReviewAttemptEventProjection toReviewAttemptEvent(LearningEventEntity event) {
        JSONObject payload = parsePayload(event.getPayloadJson());
        return new ReviewAttemptEventProjection(
                event.getRelatedEntityId(),
                event.getMapNodeId(),
                payload == null ? null : payload.getStr("selfRating"),
                payload == null ? null : payload.getStr("taskType"),
                payload == null ? null : payload.getInt("estimatedMinutes"),
                CharSequenceUtil.equals(event.getEventStatus(), "COMPLETED"),
                parseLocalDateTime(payload == null ? null : payload.getStr("scheduledDueAt")),
                payload == null ? null : payload.getInt("intervalDays"),
                payload == null ? null : payload.getInt("masteryScoreAtAttempt"),
                resolveEventAt(event));
    }

    private TutorEventProjection newerTutorEvent(TutorEventProjection left, TutorEventProjection right) {
        int timeCompare = compareDateTime(left.eventAt(), right.eventAt());
        if (timeCompare != 0) {
            return timeCompare >= 0 ? left : right;
        }
        return compareInt(left.turnNo(), right.turnNo()) >= 0 ? left : right;
    }

    private PracticeAttemptEventProjection newerPracticeEvent(PracticeAttemptEventProjection left,
                                                              PracticeAttemptEventProjection right) {
        return compareDateTime(left.eventAt(), right.eventAt()) >= 0 ? left : right;
    }

    private ReflectionEventProjection newerReflectionEvent(ReflectionEventProjection left,
                                                           ReflectionEventProjection right) {
        return compareDateTime(left.eventAt(), right.eventAt()) >= 0 ? left : right;
    }

    private ReviewAttemptEventProjection newerReviewEvent(ReviewAttemptEventProjection left,
                                                          ReviewAttemptEventProjection right) {
        return compareDateTime(left.eventAt(), right.eventAt()) >= 0 ? left : right;
    }

    private JSONObject parsePayload(String payloadJson) {
        if (CharSequenceUtil.isBlank(payloadJson)) {
            return null;
        }
        try {
            return JSONUtil.parseObj(payloadJson);
        } catch (Exception ignored) {
            return null;
        }
    }

    private List<String> parseStringArray(JSONObject payload, String key) {
        if (payload == null) {
            return Collections.emptyList();
        }
        try {
            JSONArray values = payload.getJSONArray(key);
            if (values == null) {
                return Collections.emptyList();
            }
            return JSONUtil.toList(values, String.class);
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    private Boolean toBoolean(Object rawValue) {
        if (rawValue == null) {
            return null;
        }
        if (rawValue instanceof Boolean value) {
            return value;
        }
        if (rawValue instanceof Number value) {
            return value.intValue() != 0;
        }
        String normalized = String.valueOf(rawValue).trim();
        if (CharSequenceUtil.isBlank(normalized)) {
            return null;
        }
        return CharSequenceUtil.equalsAnyIgnoreCase(normalized, "true", "1", "yes");
    }

    private LocalDate parseLocalDate(String rawValue) {
        if (CharSequenceUtil.isBlank(rawValue)) {
            return null;
        }
        try {
            return LocalDate.parse(rawValue);
        } catch (Exception ignored) {
            return null;
        }
    }

    private LocalDateTime parseLocalDateTime(String rawValue) {
        if (CharSequenceUtil.isBlank(rawValue)) {
            return null;
        }
        try {
            return LocalDateTime.parse(rawValue.replace(" ", "T"));
        } catch (Exception ignored) {
            return null;
        }
    }

    private LocalDateTime resolveEventAt(LearningEventEntity event) {
        if (event.getEventAt() != null) {
            return event.getEventAt();
        }
        if (event.getCreatedAt() != null) {
            return event.getCreatedAt();
        }
        return event.getUpdatedAt();
    }

    private int compareDateTime(LocalDateTime left, LocalDateTime right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return -1;
        }
        if (right == null) {
            return 1;
        }
        return left.compareTo(right);
    }

    private int compareInt(Integer left, Integer right) {
        return Integer.compare(left == null ? 0 : left, right == null ? 0 : right);
    }

    public record GoalEventSnapshot(List<LearningEventEntity> events,
                                    Map<Long, TutorEventProjection> latestTutorByNodeId,
                                    Map<Long, TutorEventProjection> latestTutorBySessionId,
                                    Map<Long, PracticeAttemptEventProjection> latestPracticeByAttemptId,
                                    Map<Long, ReviewAttemptEventProjection> latestReviewByAttemptId,
                                    Map<Long, ReflectionEventProjection> latestReflectionByEntryId) {
    }

    public record TutorEventProjection(Long nodeId,
                                       Long sessionId,
                                       String diagnosis,
                                       String actionType,
                                       Long recommendedNodeId,
                                       boolean nodeCompleted,
                                       Integer turnNo,
                                       LocalDateTime eventAt,
                                       List<String> nextStepSuggestions) {
    }

    public record PracticeAttemptEventProjection(Long attemptId,
                                                 Long nodeId,
                                                 String rating,
                                                 String taskType,
                                                 String evidenceKind,
                                                 Integer minutes,
                                                 boolean handoffValidation,
                                                 boolean completed,
                                                 LocalDateTime eventAt) {
    }

    public record ReviewAttemptEventProjection(Long attemptId,
                                               Long nodeId,
                                               String rating,
                                               String taskType,
                                               Integer minutes,
                                               boolean completed,
                                               LocalDateTime scheduledDueAt,
                                               Integer intervalDays,
                                               Integer masteryScoreAtAttempt,
                                               LocalDateTime eventAt) {
    }

    public record ReflectionEventProjection(Long reflectionEntryId,
                                            String summary,
                                            String nextAction,
                                            LocalDate reflectionDate,
                                            LocalDateTime eventAt) {
    }
}
