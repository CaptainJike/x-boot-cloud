package io.github.module.learning.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.framework.core.context.UserContextHolder;
import io.github.module.learning.entity.LearningGoalEntity;
import io.github.module.learning.entity.LearningMapNodeEntity;
import io.github.module.learning.entity.LearningNodeProgressEntity;
import io.github.module.learning.entity.PracticeAttemptEntity;
import io.github.module.learning.entity.ReviewAttemptEntity;
import io.github.module.learning.entity.ReviewTaskEntity;
import io.github.module.learning.entity.TutorTurnEntity;
import io.github.module.learning.enums.LearningErrorEnum;
import io.github.module.learning.mapper.PracticeAttemptMapper;
import io.github.module.learning.mapper.ReviewAttemptMapper;
import io.github.module.learning.mapper.ReviewTaskMapper;
import io.github.module.learning.mapper.TutorTurnMapper;
import io.github.module.learning.model.request.AppSaveReviewAttemptDTO;
import io.github.module.learning.model.response.LearningKnowledgeGraphBO;
import io.github.module.learning.model.response.MasteryRecordBO;
import io.github.module.learning.model.response.ReviewAttemptBO;
import io.github.module.learning.model.response.ReviewTaskBO;
import io.github.module.learning.model.response.ReviewWorkspaceBO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 服务端权威复盘任务与提交记录服务.
 */
@RequiredArgsConstructor
@Service
public class LearningReviewService {

    private static final long DEFAULT_TENANT_ID = 0L;
    private static final int WORKSPACE_SCHEMA_VERSION = 1;
    private static final int DAY_MS = 24 * 60 * 60 * 1000;
    private static final DateTimeFormatter TASK_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final ReviewTaskMapper reviewTaskMapper;
    private final ReviewAttemptMapper reviewAttemptMapper;
    private final PracticeAttemptMapper practiceAttemptMapper;
    private final TutorTurnMapper tutorTurnMapper;
    private final LearningGoalService learningGoalService;
    private final LearningGrowthService learningGrowthService;
    private final LearningEventService learningEventService;

    @Transactional(rollbackFor = Exception.class)
    public ReviewWorkspaceBO getWorkspace(Long goalId) {
        Long userId = requireUserId();
        LearningGoalEntity goal = learningGoalService.getOwnedGoalById(goalId, userId);
        LearningErrorEnum.INVALID_GOAL.assertNotNull(goal);

        List<LearningMapNodeEntity> nodes = learningGoalService.listNodesByGoalId(goalId, userId);
        Map<Long, LearningNodeProgressEntity> progressByNodeId = learningGoalService.listProgressByGoalId(goalId, userId).stream()
                .collect(Collectors.toMap(LearningNodeProgressEntity::getMapNodeId, Function.identity(), (left, right) -> right));
        List<PracticeAttemptEntity> practiceAttempts = practiceAttemptMapper.selectList(
                new QueryWrapper<PracticeAttemptEntity>()
                        .lambda()
                        .eq(PracticeAttemptEntity::getGoalId, goalId)
                        .eq(PracticeAttemptEntity::getUserId, userId)
                        .orderByAsc(PracticeAttemptEntity::getUpdatedAt)
        );
        List<ReviewAttemptEntity> reviewAttempts = reviewAttemptMapper.selectList(
                new QueryWrapper<ReviewAttemptEntity>()
                        .lambda()
                        .eq(ReviewAttemptEntity::getGoalId, goalId)
                        .eq(ReviewAttemptEntity::getUserId, userId)
                        .orderByAsc(ReviewAttemptEntity::getUpdatedAt)
        );
        Map<Long, TutorTurnEntity> latestTutorByNodeId = latestTurnByNodeId(tutorTurnMapper.selectList(
                new QueryWrapper<TutorTurnEntity>()
                        .lambda()
                        .eq(TutorTurnEntity::getGoalId, goalId)
                        .eq(TutorTurnEntity::getUserId, userId)
                        .orderByAsc(TutorTurnEntity::getTurnNo)
                        .orderByAsc(TutorTurnEntity::getCreatedAt)
        ));

        Map<Long, MasteryRecordBO> masteryByNodeId = learningGrowthService.getMasteryRecords(goalId).stream()
                .filter(item -> item.getNodeId() != null)
                .collect(Collectors.toMap(MasteryRecordBO::getNodeId, Function.identity(), (left, right) -> left));
        LearningKnowledgeGraphBO knowledgeGraph = learningGrowthService.getLearningKnowledgeGraph(goalId);

        ReviewSnapshot snapshot = buildReviewSnapshot(
                goal,
                nodes,
                progressByNodeId,
                masteryByNodeId,
                latestTutorByNodeId,
                practiceAttempts,
                reviewAttempts,
                knowledgeGraph,
                LocalDateTime.now());

        List<ReviewTaskEntity> activeTasks = synchronizeCurrentTasks(
                userId,
                goal,
                snapshot.tasks());
        Map<Long, ReviewTaskEntity> taskById = activeTasks.stream()
                .filter(task -> task.getId() != null)
                .collect(Collectors.toMap(ReviewTaskEntity::getId, Function.identity(), (left, right) -> right));

        return ReviewWorkspaceBO.builder()
                .goalId(goalId)
                .schemaVersion(WORKSPACE_SCHEMA_VERSION)
                .mode("server")
                .generatedAt(snapshot.generatedAt())
                .summary("Review 已按到期时间、掌握度和真实遗忘信号生成服务端动态队列；完成一次后会自动进入下一轮间隔。")
                .reviewReason(snapshot.reviewReason())
                .focusAreas(snapshot.focusAreas())
                .overdueCount(snapshot.overdueCount())
                .dueCount(snapshot.dueCount())
                .upcomingCount(snapshot.upcomingCount())
                .nextDueAt(snapshot.nextDueAt())
                .tasks(activeTasks.stream().map(this::toTaskBO).toList())
                .attempts(CollUtil.emptyIfNull(reviewAttempts).stream()
                        .map(attempt -> toAttemptBO(attempt, taskById.get(attempt.getReviewTaskId())))
                        .toList())
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public ReviewAttemptBO saveAttempt(String taskKey, AppSaveReviewAttemptDTO dto) {
        Long userId = requireUserId();
        LearningGoalEntity goal = learningGoalService.getOwnedGoalById(dto.getGoalId(), userId);
        LearningErrorEnum.INVALID_GOAL.assertNotNull(goal);

        ReviewTaskEntity task = reviewTaskMapper.selectOne(
                new QueryWrapper<ReviewTaskEntity>()
                        .lambda()
                        .eq(ReviewTaskEntity::getTaskKey, taskKey)
                        .eq(ReviewTaskEntity::getGoalId, dto.getGoalId())
                        .eq(ReviewTaskEntity::getUserId, userId)
                        .eq(ReviewTaskEntity::getActive, 1)
                        .last(" LIMIT 1")
        );
        LearningErrorEnum.INVALID_REVIEW_TASK.assertNotNull(task);

        ReviewAttemptEntity existing = findAttempt(task.getId(), userId);
        if (existing != null && Objects.equals(existing.getLastMutationId(), dto.getMutationId())) {
            return toAttemptBO(existing, task);
        }

        validateCompletedAttempt(dto);
        if (existing == null) {
            LearningErrorEnum.REVIEW_ATTEMPT_CONFLICT.assertTrue(dto.getBaseVersion() == 0L);
            ReviewAttemptEntity inserted = buildAttemptEntity(userId, task, dto);
            inserted.setSyncVersion(1L);
            try {
                reviewAttemptMapper.insert(inserted);
                learningEventService.recordReviewAttempt(task, inserted);
                return toAttemptBO(inserted, task);
            } catch (DuplicateKeyException duplicateKeyException) {
                ReviewAttemptEntity concurrent = findAttempt(task.getId(), userId);
                if (concurrent != null && Objects.equals(concurrent.getLastMutationId(), dto.getMutationId())) {
                    return toAttemptBO(concurrent, task);
                }
                throw duplicateKeyException;
            }
        }

        LearningErrorEnum.REVIEW_ATTEMPT_CONFLICT.assertTrue(Objects.equals(existing.getSyncVersion(), dto.getBaseVersion()));
        Long expectedVersion = dto.getBaseVersion();
        applyAttempt(existing, task, dto);
        existing.setSyncVersion(expectedVersion + 1);

        LambdaUpdateWrapper<ReviewAttemptEntity> versionGuard = Wrappers.lambdaUpdate(ReviewAttemptEntity.class)
                .eq(ReviewAttemptEntity::getId, existing.getId())
                .eq(ReviewAttemptEntity::getUserId, userId)
                .eq(ReviewAttemptEntity::getSyncVersion, expectedVersion);
        int updated = reviewAttemptMapper.update(existing, versionGuard);
        if (updated != 1) {
            ReviewAttemptEntity concurrent = findAttempt(task.getId(), userId);
            if (concurrent != null && Objects.equals(concurrent.getLastMutationId(), dto.getMutationId())) {
                return toAttemptBO(concurrent, task);
            }
            LearningErrorEnum.REVIEW_ATTEMPT_CONFLICT.assertTrue(false);
        }
        learningEventService.recordReviewAttempt(task, existing);
        return toAttemptBO(existing, task);
    }

    private ReviewAttemptEntity findAttempt(Long taskId, Long userId) {
        return reviewAttemptMapper.selectOne(
                new QueryWrapper<ReviewAttemptEntity>()
                        .lambda()
                        .eq(ReviewAttemptEntity::getReviewTaskId, taskId)
                        .eq(ReviewAttemptEntity::getUserId, userId)
                        .last(" LIMIT 1")
        );
    }

    private List<ReviewTaskEntity> synchronizeCurrentTasks(Long userId,
                                                           LearningGoalEntity goal,
                                                           List<ReviewTaskSpec> specs) {
        List<ReviewTaskEntity> existingTasks = reviewTaskMapper.selectList(
                new QueryWrapper<ReviewTaskEntity>()
                        .lambda()
                        .eq(ReviewTaskEntity::getGoalId, goal.getId())
                        .eq(ReviewTaskEntity::getUserId, userId)
        );
        Map<String, ReviewTaskEntity> taskByKey = CollUtil.emptyIfNull(existingTasks).stream()
                .collect(Collectors.toMap(ReviewTaskEntity::getTaskKey, Function.identity(), (left, right) -> right));
        Set<String> currentKeys = specs.stream().map(ReviewTaskSpec::taskKey).collect(Collectors.toSet());

        for (ReviewTaskEntity oldTask : CollUtil.emptyIfNull(existingTasks)) {
            if (oldTask.getActive() != null && oldTask.getActive() == 1 && !currentKeys.contains(oldTask.getTaskKey())) {
                oldTask.setActive(0);
                reviewTaskMapper.updateById(oldTask);
            }
        }

        List<ReviewTaskEntity> currentTasks = new ArrayList<>();
        for (ReviewTaskSpec spec : specs) {
            ReviewTaskEntity task = taskByKey.get(spec.taskKey());
            if (task == null) {
                task = ReviewTaskEntity.builder()
                        .tenantId(DEFAULT_TENANT_ID)
                        .userId(userId)
                        .goalId(goal.getId())
                        .mapNodeId(spec.nodeId())
                        .taskKey(spec.taskKey())
                        .taskVersion(1)
                        .build();
                applyTaskSpec(task, spec);
                reviewTaskMapper.insert(task);
            } else {
                applyTaskSpec(task, spec);
                task.setTaskVersion(Math.max(1, task.getTaskVersion() == null ? 1 : task.getTaskVersion()));
                reviewTaskMapper.updateById(task);
            }
            currentTasks.add(task);
        }
        return currentTasks;
    }

    private ReviewSnapshot buildReviewSnapshot(LearningGoalEntity goal,
                                               List<LearningMapNodeEntity> nodes,
                                               Map<Long, LearningNodeProgressEntity> progressByNodeId,
                                               Map<Long, MasteryRecordBO> masteryByNodeId,
                                               Map<Long, TutorTurnEntity> latestTutorByNodeId,
                                               List<PracticeAttemptEntity> practiceAttempts,
                                               List<ReviewAttemptEntity> reviewAttempts,
                                               LearningKnowledgeGraphBO knowledgeGraph,
                                               LocalDateTime generatedAt) {
        if (CollUtil.isEmpty(nodes)) {
            return new ReviewSnapshot(
                    generatedAt,
                    "当前还没有可生成复盘队列的学习节点。",
                    Collections.emptyList(),
                    0,
                    0,
                    0,
                    null,
                    Collections.emptyList());
        }

        List<LearningMapNodeEntity> candidateNodes = nodes.stream()
                .filter(node -> isReviewCandidate(goal, node, progressByNodeId, practiceAttempts, reviewAttempts))
                .toList();
        if (candidateNodes.isEmpty()) {
            return new ReviewSnapshot(
                    generatedAt,
                    "当前还没有到期或进入复盘窗口的节点。",
                    Collections.emptyList(),
                    0,
                    0,
                    0,
                    null,
                    Collections.emptyList());
        }

        Map<String, Integer> priorityRank = Map.of("overdue", 0, "due", 1, "upcoming", 2);
        List<ReviewSchedule> schedules = candidateNodes.stream()
                .map(node -> buildReviewNodeSchedule(
                        goal,
                        node,
                        progressByNodeId.get(node.getId()),
                        masteryByNodeId.get(node.getId()),
                        practiceAttempts,
                        reviewAttempts,
                        generatedAt))
                .sorted((left, right) -> {
                    int priorityCompare = Integer.compare(
                            priorityRank.getOrDefault(left.priority(), 3),
                            priorityRank.getOrDefault(right.priority(), 3));
                    if (priorityCompare != 0) {
                        return priorityCompare;
                    }
                    int scoreCompare = Integer.compare(
                            defaultInt(right.priorityScore(), 0),
                            defaultInt(left.priorityScore(), 0));
                    if (scoreCompare != 0) {
                        return scoreCompare;
                    }
                    return compareUpdatedAt(left.dueAt(), right.dueAt());
                })
                .toList();

        List<ReviewSchedule> focusSchedules = schedules.stream().limit(4).toList();
        String knowledgeWeakPath = knowledgeGraph == null || CollUtil.isEmpty(knowledgeGraph.getWeakPaths())
                ? null
                : knowledgeGraph.getWeakPaths().getFirst();
        String knowledgeWeakArea = knowledgeGraph == null || CollUtil.isEmpty(knowledgeGraph.getWeakAreas())
                ? null
                : knowledgeGraph.getWeakAreas().getFirst();
        List<ReviewTaskSpec> tasks = focusSchedules.stream()
                .map(schedule -> buildTaskSpec(
                        schedule,
                        knowledgeWeakPath,
                        knowledgeWeakArea,
                        latestTutorByNodeId.get(schedule.node().getId())))
                .toList();

        int overdueCount = (int) schedules.stream().filter(item -> CharSequenceUtil.equals(item.priority(), "overdue")).count();
        int dueCount = (int) schedules.stream().filter(item -> CharSequenceUtil.equals(item.priority(), "due")).count();
        int upcomingCount = (int) schedules.stream().filter(item -> CharSequenceUtil.equals(item.priority(), "upcoming")).count();
        LocalDateTime nextDueAt = schedules.stream()
                .map(ReviewSchedule::dueAt)
                .filter(Objects::nonNull)
                .min(this::compareUpdatedAt)
                .orElse(null);
        List<String> focusAreas = uniqueList(new ArrayList<>(focusSchedules.stream()
                .map(item -> item.node().getTitle() + "："
                        + (CharSequenceUtil.equals(item.priority(), "overdue")
                        ? "已逾期"
                        : CharSequenceUtil.equals(item.priority(), "due")
                        ? "今天到期"
                        : formatDate(item.dueAt()) + " 到期")
                        + "，掌握度 " + defaultInt(item.masteryScore(), 0) + "/100")
                .toList()), 5);
        if (CharSequenceUtil.isNotBlank(knowledgeWeakPath)) {
            List<String> nextFocusAreas = new ArrayList<>(focusAreas);
            nextFocusAreas.add("优先回收最薄弱关系：" + knowledgeWeakPath);
            focusAreas = uniqueList(nextFocusAreas, 5);
        }
        String reviewReason = overdueCount > 0
                ? "队列里有 " + overdueCount + " 个节点已逾期，系统按遗忘风险和掌握度从高到低排序。"
                : dueCount > 0
                ? "今天有 " + dueCount + " 个节点到期；完成后会按本次结果自动计算下一次间隔。"
                : "当前没有到期警报，最近一次复习将在 " + (nextDueAt == null ? "后续学习窗口" : formatDate(nextDueAt)) + " 到来。";

        return new ReviewSnapshot(
                generatedAt,
                reviewReason,
                focusAreas,
                overdueCount,
                dueCount,
                upcomingCount,
                nextDueAt,
                tasks);
    }

    private boolean isReviewCandidate(LearningGoalEntity goal,
                                      LearningMapNodeEntity node,
                                      Map<Long, LearningNodeProgressEntity> progressByNodeId,
                                      List<PracticeAttemptEntity> practiceAttempts,
                                      List<ReviewAttemptEntity> reviewAttempts) {
        String progressStatus = resolveNodeProgressStatus(goal, node, progressByNodeId);
        if (CharSequenceUtil.equalsAny(progressStatus, "IN_PROGRESS", "REVIEWING", "COMPLETED")) {
            return true;
        }
        boolean hasCompletedPractice = practiceAttempts.stream()
                .anyMatch(item -> Objects.equals(item.getMapNodeId(), node.getId())
                        && item.getCompleted() != null
                        && item.getCompleted() == 1);
        boolean hasCompletedReview = reviewAttempts.stream()
                .anyMatch(item -> Objects.equals(item.getMapNodeId(), node.getId())
                        && item.getCompleted() != null
                        && item.getCompleted() == 1);
        return hasCompletedPractice || hasCompletedReview;
    }

    private ReviewSchedule buildReviewNodeSchedule(LearningGoalEntity goal,
                                                   LearningMapNodeEntity node,
                                                   LearningNodeProgressEntity progress,
                                                   MasteryRecordBO masteryRecord,
                                                   List<PracticeAttemptEntity> practiceAttempts,
                                                   List<ReviewAttemptEntity> reviewAttempts,
                                                   LocalDateTime now) {
        List<PracticeAttemptEntity> nodePracticeAttempts = practiceAttempts.stream()
                .filter(item -> Objects.equals(item.getMapNodeId(), node.getId()))
                .toList();
        List<ReviewAttemptEntity> nodeReviewAttempts = reviewAttempts.stream()
                .filter(item -> Objects.equals(item.getMapNodeId(), node.getId()))
                .toList();
        PracticeAttemptEntity latestPractice = latestCompletedPractice(nodePracticeAttempts);
        ReviewAttemptEntity latestReview = latestCompletedReview(nodeReviewAttempts);
        int masteryScore = masteryRecord == null || masteryRecord.getMasteryScore() == null
                ? fallbackMasteryScore(resolveNodeProgressStatus(goal, node, progress == null ? Collections.emptyMap() : Map.of(node.getId(), progress)))
                : masteryRecord.getMasteryScore();
        long latestPracticeAt = latestPractice == null ? 0L : toEpochMillis(defaultIfNull(latestPractice.getUpdatedAt(), now));
        long latestReviewAt = latestReview == null ? 0L : toEpochMillis(defaultIfNull(latestReview.getUpdatedAt(), now));
        boolean reviewIsLatest = latestReview != null && latestReviewAt >= latestPracticeAt;
        boolean practiceIsLatest = latestPractice != null && latestPracticeAt > latestReviewAt;
        boolean practiceGap = practiceIsLatest && CharSequenceUtil.equals(resolvePracticeRating(latestPractice), "stuck");

        int intervalDays = baseIntervalForMastery(masteryScore);
        if (reviewIsLatest && CharSequenceUtil.equals(latestReview.getSelfRating(), "solid")) {
            intervalDays = Math.min(14, Math.max(intervalDays, (int) Math.ceil(intervalDays * 1.75)));
        } else if (reviewIsLatest && CharSequenceUtil.equals(latestReview.getSelfRating(), "wobbly")) {
            intervalDays = Math.min(intervalDays, 2);
        } else if ((reviewIsLatest && CharSequenceUtil.equals(latestReview.getSelfRating(), "forgotten")) || practiceGap) {
            intervalDays = 1;
        }

        LocalDate today = now.toLocalDate();
        long nowStart = toEpochMillis(today.atStartOfDay());
        LocalDateTime anchorAt = reviewIsLatest
                ? latestReview == null ? null : latestReview.getUpdatedAt()
                : latestPractice == null ? null : latestPractice.getUpdatedAt();
        long dueAtMs = anchorAt == null ? nowStart : toEpochMillis(anchorAt) + (long) intervalDays * DAY_MS;
        if (CharSequenceUtil.equals(resolveNodeProgressStatus(goal, node, progress == null ? Collections.emptyMap() : Map.of(node.getId(), progress)), "REVIEWING")) {
            dueAtMs = Math.min(dueAtMs, nowStart);
        }
        LocalDateTime dueAt = fromEpochMillis(dueAtMs);
        long tomorrowStart = nowStart + DAY_MS;
        String priority = dueAtMs < nowStart
                ? "overdue"
                : dueAtMs < tomorrowStart
                ? "due"
                : "upcoming";
        int overdueDays = Math.max(0, (int) Math.floor((nowStart - dueAtMs) * 1.0 / DAY_MS));
        String scheduleReason = reviewIsLatest && CharSequenceUtil.equals(latestReview.getSelfRating(), "forgotten")
                ? "forgotten"
                : practiceGap
                ? "practice_gap"
                : masteryScore < 55
                ? "low_mastery"
                : latestReview != null || latestPractice != null
                ? "scheduled"
                : "node_status";
        int priorityScore = Math.min(200, Math.round(
                (100 - masteryScore)
                        + overdueDays * 12
                        + (CharSequenceUtil.equals(priority, "overdue") ? 28 : CharSequenceUtil.equals(priority, "due") ? 18 : 0)
                        + (CharSequenceUtil.equals(scheduleReason, "forgotten") ? 35 : CharSequenceUtil.equals(scheduleReason, "practice_gap") ? 25 : 0)));

        return new ReviewSchedule(
                node,
                dueAt,
                intervalDays,
                priority,
                priorityScore,
                scheduleReason,
                masteryScore,
                latestReview == null ? null : latestReview.getUpdatedAt(),
                (int) nodeReviewAttempts.stream().filter(item -> item.getCompleted() != null && item.getCompleted() == 1).count());
    }

    private ReviewTaskSpec buildTaskSpec(ReviewSchedule schedule,
                                         String knowledgeWeakPath,
                                         String knowledgeWeakArea,
                                         TutorTurnEntity latestTutorTurn) {
        LearningMapNodeEntity node = schedule.node();
        String taskType = resolveTaskType(schedule);
        String scheduleKey = formatDate(schedule.dueAt());
        String taskKey = "review-" + taskType + "-" + node.getId() + "-" + scheduleKey;
        int estimatedMinutes = capMinutes(node.getEstimatedMinutes(), 8, 12);
        String sourceReason = scheduleReasonCopy(schedule);
        String tutorSuggestion = latestTutorTurn == null
                ? null
                : parseFirstSuggestion(latestTutorTurn.getNextStepSuggestionsJson());

        if (CharSequenceUtil.equals(taskType, "compare")) {
            return new ReviewTaskSpec(
                    taskKey,
                    taskType,
                    node.getId(),
                    node.getTitle(),
                    "比较「" + node.getTitle() + "」最容易混淆的点",
                    CharSequenceUtil.isNotBlank(knowledgeWeakPath)
                            ? "围绕「" + knowledgeWeakPath + "」，写出最容易混淆的关系，并说明哪里像、哪里不同、为什么链路会在这里断开。"
                            : "写出你最容易把 " + node.getTitle() + " 和什么概念混淆，并说明哪里像、哪里不同、什么场景下该选哪个。",
                    "能说清 " + node.getTitle() + " 的关键边界，而不是只重复定义。",
                    CharSequenceUtil.blankToDefault(
                            knowledgeWeakArea,
                            CharSequenceUtil.blankToDefault(node.getCompletionCriteria(), "优先写边界和反例。")),
                    estimatedMinutes,
                    sourceReason,
                    knowledgeWeakPath,
                    schedule.dueAt(),
                    schedule.intervalDays(),
                    schedule.priority(),
                    schedule.priorityScore(),
                    schedule.scheduleReason(),
                    schedule.masteryScore(),
                    schedule.lastReviewedAt());
        }

        if (CharSequenceUtil.equals(taskType, "rebuild")) {
            return new ReviewTaskSpec(
                    taskKey,
                    taskType,
                    node.getId(),
                    node.getTitle(),
                    "重建「" + node.getTitle() + "」的使用流程",
                    CharSequenceUtil.isNotBlank(knowledgeWeakPath)
                            ? "把「" + knowledgeWeakPath + "」编进一个最小流程：先解释哪段关系，再验证什么，最后如何判断链路真的连上了。"
                            : "写出一个最小流程：先解释什么，再验证什么，最后如何判断自己真的会了。",
                    "能把 " + node.getTitle() + " 从散点理解重建成可复用流程。",
                    CharSequenceUtil.blankToDefault(tutorSuggestion, "把“讲清楚”和“做出来”的顺序都写进去。"),
                    estimatedMinutes,
                    sourceReason,
                    knowledgeWeakPath,
                    schedule.dueAt(),
                    schedule.intervalDays(),
                    schedule.priority(),
                    schedule.priorityScore(),
                    schedule.scheduleReason(),
                    schedule.masteryScore(),
                    schedule.lastReviewedAt());
        }

        return new ReviewTaskSpec(
                taskKey,
                taskType,
                node.getId(),
                node.getTitle(),
                "不看资料回忆「" + node.getTitle() + "」",
                CharSequenceUtil.isNotBlank(knowledgeWeakPath)
                        ? "直接写下 " + node.getTitle() + " 和「" + knowledgeWeakPath + "」之间的关系、链路为什么容易断，以及最可能忘掉哪里。先回忆，再检查遗漏。"
                        : "直接写下 " + node.getTitle() + " 的核心作用、关键边界，以及你为什么要学它。",
                "能主动调出 " + node.getTitle() + " 的核心作用和边界，而不是被动识别。",
                CharSequenceUtil.blankToDefault(
                        knowledgeWeakArea,
                        CharSequenceUtil.blankToDefault(node.getWhyItMatters(), "回忆优先于重读，先暴露遗忘点再补。")),
                estimatedMinutes,
                sourceReason,
                knowledgeWeakPath,
                schedule.dueAt(),
                schedule.intervalDays(),
                schedule.priority(),
                schedule.priorityScore(),
                schedule.scheduleReason(),
                schedule.masteryScore(),
                schedule.lastReviewedAt());
    }

    private String resolveTaskType(ReviewSchedule schedule) {
        if (CharSequenceUtil.equals(schedule.scheduleReason(), "forgotten")) {
            return "recall";
        }
        return switch (schedule.reviewCount() % 3) {
            case 1 -> "compare";
            case 2 -> "rebuild";
            default -> "recall";
        };
    }

    private String scheduleReasonCopy(ReviewSchedule schedule) {
        String mastery = "当前掌握度 " + schedule.masteryScore() + "/100";
        return switch (schedule.scheduleReason()) {
            case "forgotten" -> "上次复盘明确标记为“忘了”，已缩短到 " + schedule.intervalDays() + " 天间隔；" + mastery + "。";
            case "practice_gap" -> "最新 Practice 评测仍需补强，已缩短到 " + schedule.intervalDays() + " 天间隔；" + mastery + "。";
            case "low_mastery" -> "掌握度仍处于薄弱区，按 " + schedule.intervalDays() + " 天间隔安排主动回忆；" + mastery + "。";
            case "scheduled" -> "根据最新学习证据和掌握度，按 " + schedule.intervalDays() + " 天间隔到期；" + mastery + "。";
            default -> "节点已经进入可复习阶段，先建立第一条间隔复习记录；" + mastery + "。";
        };
    }

    private void applyTaskSpec(ReviewTaskEntity task, ReviewTaskSpec spec) {
        task.setMapNodeId(spec.nodeId());
        task.setTaskType(spec.taskType());
        task.setTitle(spec.title());
        task.setPrompt(spec.prompt());
        task.setExpectedOutcome(spec.expectedOutcome());
        task.setHint(spec.hint());
        task.setEstimatedMinutes(spec.estimatedMinutes());
        task.setNodeTitle(spec.nodeTitle());
        task.setSourceReason(spec.sourceReason());
        task.setKnowledgeFocus(spec.knowledgeFocus());
        task.setDueAt(spec.dueAt());
        task.setIntervalDays(spec.intervalDays());
        task.setPriority(spec.priority());
        task.setPriorityScore(spec.priorityScore());
        task.setScheduleReason(spec.scheduleReason());
        task.setMasteryScore(spec.masteryScore());
        task.setLastReviewedAt(spec.lastReviewedAt());
        task.setActive(1);
    }

    private ReviewAttemptEntity buildAttemptEntity(Long userId,
                                                   ReviewTaskEntity task,
                                                   AppSaveReviewAttemptDTO dto) {
        ReviewAttemptEntity entity = ReviewAttemptEntity.builder()
                .tenantId(DEFAULT_TENANT_ID)
                .userId(userId)
                .goalId(task.getGoalId())
                .reviewTaskId(task.getId())
                .mapNodeId(task.getMapNodeId())
                .attemptKey(task.getTaskKey())
                .build();
        applyAttempt(entity, task, dto);
        return entity;
    }

    private void applyAttempt(ReviewAttemptEntity entity,
                              ReviewTaskEntity task,
                              AppSaveReviewAttemptDTO dto) {
        entity.setResponseContent(CharSequenceUtil.nullToEmpty(dto.getResponse()).trim());
        entity.setSelfRating(dto.getSelfRating());
        entity.setScheduledDueAt(parseDateTime(dto.getScheduledDueAt(), task.getDueAt()));
        entity.setIntervalDays(dto.getIntervalDays() == null ? task.getIntervalDays() : dto.getIntervalDays());
        entity.setMasteryScoreAtAttempt(dto.getMasteryScoreAtAttempt() == null ? task.getMasteryScore() : dto.getMasteryScoreAtAttempt());
        entity.setCompleted(Boolean.TRUE.equals(dto.getCompleted()) ? 1 : 0);
        entity.setClientUpdatedAt(CharSequenceUtil.blankToDefault(dto.getClientUpdatedAt(), LocalDateTime.now().toString()));
        entity.setLastMutationId(dto.getMutationId());
    }

    private void validateCompletedAttempt(AppSaveReviewAttemptDTO dto) {
        if (!Boolean.TRUE.equals(dto.getCompleted())) {
            return;
        }
        LearningErrorEnum.INVALID_REVIEW_ATTEMPT.assertNotBlank(dto.getResponse());
    }

    private ReviewTaskBO toTaskBO(ReviewTaskEntity task) {
        return ReviewTaskBO.builder()
                .id(task.getTaskKey())
                .type(task.getTaskType())
                .title(task.getTitle())
                .prompt(task.getPrompt())
                .expectedOutcome(task.getExpectedOutcome())
                .hint(task.getHint())
                .estimatedMinutes(task.getEstimatedMinutes())
                .nodeId(task.getMapNodeId())
                .nodeTitle(task.getNodeTitle())
                .sourceReason(task.getSourceReason())
                .knowledgeFocus(task.getKnowledgeFocus())
                .dueAt(task.getDueAt())
                .intervalDays(task.getIntervalDays())
                .priority(task.getPriority())
                .priorityScore(task.getPriorityScore())
                .scheduleReason(task.getScheduleReason())
                .masteryScore(task.getMasteryScore())
                .lastReviewedAt(task.getLastReviewedAt())
                .build();
    }

    private ReviewAttemptBO toAttemptBO(ReviewAttemptEntity attempt, ReviewTaskEntity task) {
        return ReviewAttemptBO.builder()
                .taskId(task == null ? attempt.getAttemptKey() : task.getTaskKey())
                .nodeId(attempt.getMapNodeId())
                .taskType(task == null ? null : task.getTaskType())
                .response(attempt.getResponseContent())
                .selfRating(attempt.getSelfRating())
                .scheduledDueAt(attempt.getScheduledDueAt())
                .intervalDays(attempt.getIntervalDays())
                .masteryScoreAtAttempt(attempt.getMasteryScoreAtAttempt())
                .completed(attempt.getCompleted() != null && attempt.getCompleted() == 1)
                .updatedAt(attempt.getUpdatedAt())
                .serverId(attempt.getId())
                .serverVersion(attempt.getSyncVersion())
                .lastMutationId(attempt.getLastMutationId())
                .syncStatus("synced")
                .build();
    }

    private Map<Long, TutorTurnEntity> latestTurnByNodeId(List<TutorTurnEntity> tutorTurns) {
        return CollUtil.emptyIfNull(tutorTurns).stream()
                .filter(turn -> turn.getMapNodeId() != null)
                .collect(Collectors.toMap(
                        TutorTurnEntity::getMapNodeId,
                        Function.identity(),
                        (left, right) -> compareUpdatedAt(left.getCreatedAt(), right.getCreatedAt()) >= 0 ? left : right));
    }

    private PracticeAttemptEntity latestCompletedPractice(List<PracticeAttemptEntity> attempts) {
        return CollUtil.emptyIfNull(attempts).stream()
                .filter(item -> item.getCompleted() != null && item.getCompleted() == 1)
                .max((left, right) -> compareUpdatedAt(left.getUpdatedAt(), right.getUpdatedAt()))
                .orElse(null);
    }

    private ReviewAttemptEntity latestCompletedReview(List<ReviewAttemptEntity> attempts) {
        return CollUtil.emptyIfNull(attempts).stream()
                .filter(item -> item.getCompleted() != null && item.getCompleted() == 1)
                .max((left, right) -> compareUpdatedAt(left.getUpdatedAt(), right.getUpdatedAt()))
                .orElse(null);
    }

    private String resolveNodeProgressStatus(LearningGoalEntity goal,
                                             LearningMapNodeEntity node,
                                             Map<Long, LearningNodeProgressEntity> progressByNodeId) {
        LearningNodeProgressEntity progress = progressByNodeId.get(node.getId());
        if (progress != null && CharSequenceUtil.isNotBlank(progress.getStatus())) {
            return progress.getStatus();
        }
        if (goal != null && Objects.equals(goal.getActiveNodeId(), node.getId())) {
            return "IN_PROGRESS";
        }
        return "PENDING";
    }

    private int fallbackMasteryScore(String progressStatus) {
        return switch (progressStatus) {
            case "COMPLETED" -> 65;
            case "REVIEWING" -> 55;
            case "IN_PROGRESS" -> 42;
            default -> 25;
        };
    }

    private int baseIntervalForMastery(int score) {
        if (score >= 75) {
            return 7;
        }
        if (score >= 55) {
            return 4;
        }
        if (score >= 30) {
            return 2;
        }
        return 1;
    }

    private String resolvePracticeRating(PracticeAttemptEntity attempt) {
        if (attempt == null) {
            return "stretch";
        }
        if (CharSequenceUtil.isNotBlank(attempt.getAssessmentJson())) {
            try {
                String level = cn.hutool.json.JSONUtil.parseObj(attempt.getAssessmentJson()).getStr("level");
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
                // fall through
            }
        }
        return CharSequenceUtil.blankToDefault(attempt.getSelfRating(), "stretch");
    }

    private String parseFirstSuggestion(String json) {
        if (CharSequenceUtil.isBlank(json)) {
            return null;
        }
        try {
            List<String> suggestions = cn.hutool.json.JSONUtil.toList(cn.hutool.json.JSONUtil.parseArray(json), String.class);
            return CollUtil.isEmpty(suggestions) ? null : suggestions.getFirst();
        } catch (Exception ignored) {
            return null;
        }
    }

    private LocalDateTime parseDateTime(String value, LocalDateTime fallback) {
        if (CharSequenceUtil.isBlank(value)) {
            return fallback;
        }
        try {
            return LocalDateTime.parse(value.replace(" ", "T"));
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private long toEpochMillis(LocalDateTime value) {
        return value == null
                ? 0L
                : value.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private LocalDateTime fromEpochMillis(long epochMillis) {
        return LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(epochMillis),
                java.time.ZoneId.systemDefault());
    }

    private String formatDate(LocalDateTime value) {
        return value == null ? "" : TASK_DATE_FORMATTER.format(value.toLocalDate());
    }

    private int capMinutes(Integer value, int fallback, int maxValue) {
        return Math.max(6, Math.min(maxValue, value == null ? fallback : value));
    }

    private int defaultInt(Integer value, int fallback) {
        return value == null ? fallback : value;
    }

    private int compareUpdatedAt(LocalDateTime left, LocalDateTime right) {
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

    private LocalDateTime defaultIfNull(LocalDateTime value, LocalDateTime fallback) {
        return value == null ? fallback : value;
    }

    private List<String> uniqueList(List<String> input, int maxSize) {
        return new ArrayList<>(new LinkedHashSet<>(CollUtil.emptyIfNull(input))).stream()
                .filter(CharSequenceUtil::isNotBlank)
                .limit(maxSize)
                .toList();
    }

    private Long requireUserId() {
        Long userId = UserContextHolder.getUserId();
        LearningErrorEnum.USER_NOT_LOGGED_IN.assertNotNull(userId);
        return userId;
    }

    private record ReviewSchedule(LearningMapNodeEntity node,
                                  LocalDateTime dueAt,
                                  Integer intervalDays,
                                  String priority,
                                  Integer priorityScore,
                                  String scheduleReason,
                                  Integer masteryScore,
                                  LocalDateTime lastReviewedAt,
                                  Integer reviewCount) {
    }

    private record ReviewTaskSpec(String taskKey,
                                  String taskType,
                                  Long nodeId,
                                  String nodeTitle,
                                  String title,
                                  String prompt,
                                  String expectedOutcome,
                                  String hint,
                                  Integer estimatedMinutes,
                                  String sourceReason,
                                  String knowledgeFocus,
                                  LocalDateTime dueAt,
                                  Integer intervalDays,
                                  String priority,
                                  Integer priorityScore,
                                  String scheduleReason,
                                  Integer masteryScore,
                                  LocalDateTime lastReviewedAt) {
    }

    private record ReviewSnapshot(LocalDateTime generatedAt,
                                  String reviewReason,
                                  List<String> focusAreas,
                                  Integer overdueCount,
                                  Integer dueCount,
                                  Integer upcomingCount,
                                  LocalDateTime nextDueAt,
                                  List<ReviewTaskSpec> tasks) {
    }
}
