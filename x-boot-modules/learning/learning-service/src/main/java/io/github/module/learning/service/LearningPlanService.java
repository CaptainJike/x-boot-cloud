package io.github.module.learning.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.framework.core.context.UserContextHolder;
import io.github.module.learning.entity.DailyDigestEntity;
import io.github.module.learning.entity.GrowthSnapshotEntity;
import io.github.module.learning.entity.LearningEventEntity;
import io.github.module.learning.entity.LearningGoalEntity;
import io.github.module.learning.entity.LearningMapNodeEntity;
import io.github.module.learning.entity.LearningNodeProgressEntity;
import io.github.module.learning.entity.PracticeAttemptEntity;
import io.github.module.learning.entity.PracticeTaskEntity;
import io.github.module.learning.entity.ReviewAttemptEntity;
import io.github.module.learning.entity.TutorTurnEntity;
import io.github.module.learning.enums.LearningErrorEnum;
import io.github.module.learning.mapper.DailyDigestMapper;
import io.github.module.learning.mapper.GrowthSnapshotMapper;
import io.github.module.learning.mapper.PracticeAttemptMapper;
import io.github.module.learning.mapper.PracticeTaskMapper;
import io.github.module.learning.mapper.ReviewAttemptMapper;
import io.github.module.learning.mapper.TutorTurnMapper;
import io.github.module.learning.model.response.LearningPlanBO;
import io.github.module.learning.model.response.LearningPlanTaskBO;
import io.github.module.learning.model.response.PlannerHistoryEntryBO;
import io.github.module.learning.model.response.ReplanTimelineBO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
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
 * 服务端学习计划读模型.
 */
@RequiredArgsConstructor
@Service
public class LearningPlanService {

    private static final int DEFAULT_WEEKLY_MINUTES = 60;
    private static final int DEFAULT_REFLECTION_MINUTES = 10;
    private static final int DEFAULT_TUTOR_MINUTES = 15;
    private static final int DEFAULT_PRACTICE_MINUTES = 15;
    private static final int DEFAULT_REVIEW_MINUTES = 15;
    private static final int DEFAULT_REPAIR_MINUTES = 18;
    private static final int SERVER_ACTIVITY_WINDOW_DAYS = 7;
    private static final int MAX_REPLAN_HISTORY = 24;

    private final LearningGoalService learningGoalService;
    private final TutorTurnMapper tutorTurnMapper;
    private final PracticeTaskMapper practiceTaskMapper;
    private final PracticeAttemptMapper practiceAttemptMapper;
    private final ReviewAttemptMapper reviewAttemptMapper;
    private final GrowthSnapshotMapper growthSnapshotMapper;
    private final DailyDigestMapper dailyDigestMapper;
    private final LearningEventReadService learningEventReadService;

    public LearningPlanBO getCurrentPlan(Long goalId) {
        return buildPlan(loadPlanningContext(goalId), LocalDateTime.now());
    }

    public ReplanTimelineBO getReplanTimeline(Long goalId) {
        PlanningReplayData replayData = loadPlanningReplayData(goalId);
        LearningPlanBO currentPlan = buildPlan(buildPlanningContext(replayData, null), LocalDateTime.now());
        List<PlannerHistoryEntryBO> allHistory = buildReplanHistory(replayData);
        List<PlannerHistoryEntryBO> visibleItems = allHistory.stream()
                .limit(MAX_REPLAN_HISTORY)
                .toList();

        if (visibleItems.isEmpty()) {
            visibleItems = List.of(toPlannerHistoryEntry(currentPlan, null, null));
        }

        int totalReplans = Math.max(0, allHistory.isEmpty() ? visibleItems.size() - 1 : allHistory.size() - 1);
        String title = totalReplans > 0
                ? "最近已经发生 " + totalReplans + " 次计划重排"
                : "当前计划还处在第一轮排布中";
        String summary = totalReplans > 0
                ? "系统已经开始根据统一学习事件回放重建今天该做什么。"
                : "随着你留下更多 Tutor、Practice、Reflection 事件，这里会开始展示计划是怎么一步步被改写出来的。";
        String stabilityLabel = CharSequenceUtil.equals(currentPlan.getPaceStatus(), "behind")
                ? "计划正在追赶"
                : CharSequenceUtil.equals(currentPlan.getPaceStatus(), "ahead")
                ? "计划处于领先稳态"
                : "计划相对稳定";
        String currentDrift = CharSequenceUtil.equals(currentPlan.getPaceStatus(), "behind")
                ? "当前仍有约 " + defaultInt(currentPlan.getBacklogMinutes(), 0) + " 分钟的堆积，需要先收拢再继续推进。"
                : CharSequenceUtil.equals(currentPlan.getPaceStatus(), "ahead")
                ? "当前没有明显堆积，系统主要在维持主线连续。"
                : "当前节奏和堆积都在可控范围内，适合稳定推进并保留复盘。";
        String nextAdjustment = CollUtil.isNotEmpty(currentPlan.getCarryOverQueue())
                ? currentPlan.getCarryOverQueue().getFirst()
                : CollUtil.isNotEmpty(currentPlan.getReviewQueue())
                ? currentPlan.getReviewQueue().getFirst()
                : currentPlan.getRecoveryNote();

        return ReplanTimelineBO.builder()
                .generatedAt(LocalDateTime.now())
                .mode("server")
                .title(title)
                .summary(summary)
                .totalReplans(totalReplans)
                .stabilityLabel(stabilityLabel)
                .latestReason(currentPlan.getReplanReason())
                .currentDrift(currentDrift)
                .nextAdjustment(nextAdjustment)
                .items(visibleItems)
                .build();
    }

    private LearningPlanBO buildPlan(PlanningContext context, LocalDateTime generatedAt) {
        LearningGoalEntity goal = context.goal();
        LearningMapNodeEntity currentNode = context.currentNode();
        List<LearningMapNodeEntity> nodes = context.nodes();
        LocalDate anchorDate = generatedAt == null ? LocalDate.now() : generatedAt.toLocalDate();

        List<LearningMapNodeEntity> reviewingNodes = nodes.stream()
                .filter(node -> progressStatusOf(node.getId(), context.progressByNodeId()).equals("REVIEWING"))
                .toList();
        List<LearningMapNodeEntity> inProgressNodes = nodes.stream()
                .filter(node -> progressStatusOf(node.getId(), context.progressByNodeId()).equals("IN_PROGRESS"))
                .filter(node -> currentNode == null || !Objects.equals(node.getId(), currentNode.getId()))
                .toList();
        List<LearningMapNodeEntity> readyNodes = nodes.stream()
                .filter(node -> progressStatusOf(node.getId(), context.progressByNodeId()).equals("READY"))
                .filter(node -> currentNode == null || !Objects.equals(node.getId(), currentNode.getId()))
                .toList();
        Map<Long, ReviewSignal> latestCompletedReviewByNodeId = latestCompletedReviewByNodeId(context.reviewSignals());
        List<LearningMapNodeEntity> prioritizedReviewNodes = buildPrioritizedReviewNodes(
                reviewingNodes,
                latestCompletedReviewByNodeId,
                context.nodeById());
        int forgottenReviewCount = countReviewSignals(context.reviewSignals(), "forgotten");

        TutorSignal latestCurrentTurn = currentNode == null ? null : context.latestTurnByNodeId().get(currentNode.getId());
        LearningMapNodeEntity recommendedNode = latestCurrentTurn == null || latestCurrentTurn.recommendedNodeId() == null
                ? null
                : context.nodeById().get(latestCurrentTurn.recommendedNodeId());

        GrowthHighlights growthHighlights = buildGrowthHighlights(context.growthSnapshots());
        ActivitySummary activitySummary = buildActivitySummary(context, anchorDate);

        int completedNodes = (int) context.progressByNodeId().values().stream()
                .filter(progress -> CharSequenceUtil.equals(progress.getStatus(), "COMPLETED"))
                .count();
        int currentNodePracticeCompleted = currentNode == null
                ? 0
                : (int) context.completedPracticeSignals().stream()
                .filter(signal -> Objects.equals(signal.nodeId(), currentNode.getId()))
                .count();

        int backlogMinutes =
                sumEstimatedMinutes(prioritizedReviewNodes, DEFAULT_REVIEW_MINUTES, DEFAULT_REPAIR_MINUTES) +
                        forgottenReviewCount * 6 +
                        (inProgressNodes.isEmpty() ? 0 : capMinutes(inProgressNodes.getFirst().getEstimatedMinutes(), DEFAULT_REVIEW_MINUTES, 20)) +
                        (activitySummary.reflectedToday() ? 0 : DEFAULT_REFLECTION_MINUTES);

        List<LearningPlanTaskBO> tasks = new ArrayList<>();
        if (currentNode != null && latestCurrentTurn == null) {
            tasks.add(taskBuilder()
                    .id("learn-" + currentNode.getId())
                    .kind("learn")
                    .title("先诊断「" + currentNode.getTitle() + "」")
                    .summary("先用 Tutor 判断你懂到了哪一步，再决定今天该讲解、追问还是补前置。")
                    .reason(CharSequenceUtil.blankToDefault(currentNode.getWhyItMatters(), "这是当前路径上最值得先推进的节点。"))
                    .estimatedMinutes(capMinutes(currentNode.getEstimatedMinutes(), 20, 25))
                    .nodeId(currentNode.getId())
                    .nodeTitle(currentNode.getTitle())
                    .targetSection("tutor")
                    .priority(100)
                    .recoveryMode(Boolean.FALSE)
                    .build());
        }

        if (currentNode != null && !CharSequenceUtil.equals(latestDiagnosis(latestCurrentTurn), "needs_prereq")) {
            tasks.add(taskBuilder()
                    .id("continue-" + currentNode.getId())
                    .kind("learn")
                    .title("继续推进「" + currentNode.getTitle() + "」")
                    .summary(firstNonBlank(nextStepSuggestions(latestCurrentTurn), "把这轮理解往前推一步，争取说清楚概念、边界和验证方式。"))
                    .reason(CharSequenceUtil.equals(latestDiagnosis(latestCurrentTurn), "misconception")
                            ? "Tutor 判断这里还存在概念误解，趁热纠偏最划算。"
                            : "这是当前学习路径上的主推进点。")
                    .estimatedMinutes(capMinutes(currentNode.getEstimatedMinutes(), 25, 30))
                    .nodeId(currentNode.getId())
                    .nodeTitle(currentNode.getTitle())
                    .targetSection("tutor")
                    .priority(activitySummary.reflectedToday() ? 92 : 88)
                    .recoveryMode(Boolean.FALSE)
                    .build());
        }

        if (currentNode != null && latestCurrentTurn != null
                && !CharSequenceUtil.equals(latestDiagnosis(latestCurrentTurn), "needs_prereq")
                && currentNodePracticeCompleted == 0) {
            tasks.add(taskBuilder()
                    .id("practice-" + currentNode.getId())
                    .kind("practice")
                    .title("练一轮「" + currentNode.getTitle() + "」")
                    .summary("别停在理解，马上用一轮解释或应用练习把这次 Tutor 变成可输出能力。")
                    .reason(CharSequenceUtil.equals(latestDiagnosis(latestCurrentTurn), "misconception")
                            ? "当前节点刚暴露了概念偏差，趁热做练习，最容易真正纠偏。"
                            : "完成 Tutor 后立刻练一轮，能显著降低“我好像懂了其实不会用”的风险。")
                    .estimatedMinutes(DEFAULT_PRACTICE_MINUTES)
                    .nodeId(currentNode.getId())
                    .nodeTitle(currentNode.getTitle())
                    .targetSection("practice")
                    .priority(CharSequenceUtil.equals(latestDiagnosis(latestCurrentTurn), "misconception") ? 90 : 84)
                    .recoveryMode(Boolean.FALSE)
                    .build());
        }

        if (CharSequenceUtil.equals(latestDiagnosis(latestCurrentTurn), "needs_prereq") && recommendedNode != null) {
            tasks.add(taskBuilder()
                    .id("repair-" + recommendedNode.getId())
                    .kind("repair")
                    .title("先回补「" + recommendedNode.getTitle() + "」")
                    .summary("当前节点直接深挖会卡住，先补前置，再回来继续会更省时间。")
                    .reason("Tutor 已识别出明确的前置缺口，今天更适合先补短板。")
                    .estimatedMinutes(capMinutes(recommendedNode.getEstimatedMinutes(), DEFAULT_REPAIR_MINUTES, 24))
                    .nodeId(recommendedNode.getId())
                    .nodeTitle(recommendedNode.getTitle())
                    .targetSection("tutor")
                    .priority(96)
                    .recoveryMode(Boolean.TRUE)
                    .build());
        }

        for (int index = 0; index < Math.min(2, prioritizedReviewNodes.size()); index++) {
            LearningMapNodeEntity node = prioritizedReviewNodes.get(index);
            ReviewSignal latestReviewSignal = latestCompletedReviewByNodeId.get(node.getId());
            tasks.add(taskBuilder()
                    .id("review-" + node.getId())
                    .kind("review")
                    .title(buildReviewTaskTitle(node, latestReviewSignal))
                    .summary(buildReviewTaskSummary(node, latestReviewSignal))
                    .reason(buildReviewTaskReason(node, latestReviewSignal))
                    .estimatedMinutes(capMinutes(node.getEstimatedMinutes(), DEFAULT_REVIEW_MINUTES, DEFAULT_REPAIR_MINUTES))
                    .nodeId(node.getId())
                    .nodeTitle(node.getTitle())
                    .targetSection("review")
                    .priority(reviewPriorityForSignal(latestReviewSignal, index))
                    .recoveryMode(Boolean.FALSE)
                    .build());
        }

        if (!activitySummary.reflectedToday()) {
            tasks.add(taskBuilder()
                    .id("reflect-today")
                    .kind("reflect")
                    .title("给今天做一次 Reflection 封箱")
                    .summary(latestCurrentTurn != null
                            ? "把今天的理解、误解和下一步动作写下来，Planner 才能持续调度。"
                            : "哪怕今天只推进了一小步，也要留下认知痕迹，避免学习只剩浏览。")
                    .reason("没有 Reflection，今天的学习无法稳定写回长期记忆。")
                    .estimatedMinutes(DEFAULT_REFLECTION_MINUTES)
                    .nodeId(currentNode == null ? null : currentNode.getId())
                    .nodeTitle(currentNode == null ? null : currentNode.getTitle())
                    .targetSection("reflection")
                    .priority(latestCurrentTurn != null ? 86 : 72)
                    .recoveryMode(Boolean.FALSE)
                    .build());
        }

        if (prioritizedReviewNodes.isEmpty() && !growthHighlights.commonStickingPoints().isEmpty()) {
            tasks.add(taskBuilder()
                    .id("repair-pattern")
                    .kind("repair")
                    .title("先处理最近最常见的卡点")
                    .summary(growthHighlights.commonStickingPoints().getFirst())
                    .reason("成长时间线显示这个问题在重复出现，今天值得优先修正。")
                    .estimatedMinutes(DEFAULT_REPAIR_MINUTES)
                    .nodeId(currentNode == null ? null : currentNode.getId())
                    .nodeTitle(currentNode == null ? null : currentNode.getTitle())
                    .targetSection(currentNode == null ? "growth" : "tutor")
                    .priority(74)
                    .recoveryMode(Boolean.TRUE)
                    .build());
        }

        if (!inProgressNodes.isEmpty()) {
            LearningMapNodeEntity staleNode = inProgressNodes.getFirst();
            tasks.add(taskBuilder()
                    .id("stale-" + staleNode.getId())
                    .kind("repair")
                    .title("收拢挂起的节点「" + staleNode.getTitle() + "」")
                    .summary("这条路径已经开始但没有收住，适合安排一次短回看，避免形成长期挂起。")
                    .reason("系统检测到存在进行中但不活跃的节点，容易分散学习注意力。")
                    .estimatedMinutes(capMinutes(staleNode.getEstimatedMinutes(), DEFAULT_REVIEW_MINUTES, 20))
                    .nodeId(staleNode.getId())
                    .nodeTitle(staleNode.getTitle())
                    .targetSection("map")
                    .priority(68)
                    .recoveryMode(Boolean.TRUE)
                    .build());
        }

        if (tasks.isEmpty() && !readyNodes.isEmpty()) {
            LearningMapNodeEntity nextNode = readyNodes.getFirst();
            tasks.add(taskBuilder()
                    .id("ready-" + nextNode.getId())
                    .kind("learn")
                    .title("开始下一个节点「" + nextNode.getTitle() + "」")
                    .summary("当前没有明显挂起任务，可以自然进入下一个可开始节点。")
                    .reason("学习路径已经准备好新的推进点。")
                    .estimatedMinutes(capMinutes(nextNode.getEstimatedMinutes(), 20, 25))
                    .nodeId(nextNode.getId())
                    .nodeTitle(nextNode.getTitle())
                    .targetSection("tutor")
                    .priority(80)
                    .recoveryMode(Boolean.FALSE)
                    .build());
        }

        CompletionForecast completionForecast = buildCompletionForecast(
                goal,
                nodes,
                context.progressByNodeId(),
                backlogMinutes,
                activitySummary,
                anchorDate);
        RecoveryModeMeta recoveryModeMeta = buildRecoveryModeMeta(
                completionForecast.completionStatus(),
                completionForecast.scheduleDeltaDays(),
                backlogMinutes,
                latestCurrentTurn,
                prioritizedReviewNodes.size(),
                forgottenReviewCount,
                activitySummary);
        List<LearningPlanTaskBO> orderedTasks = uniqueTasks(applyRecoveryMode(tasks, recoveryModeMeta.recoveryMode(), latestCurrentTurn))
                .stream()
                .sorted((left, right) -> Integer.compare(
                        defaultInt(right.getPriority(), 0),
                        defaultInt(left.getPriority(), 0)))
                .limit(4)
                .toList();

        LearningPlanTaskBO mission = orderedTasks.isEmpty() ? fallbackMission(currentNode) : orderedTasks.getFirst();
        int basePlanMinutes = orderedTasks.isEmpty()
                ? defaultInt(mission.getEstimatedMinutes(), DEFAULT_TUTOR_MINUTES)
                : orderedTasks.stream().mapToInt(task -> defaultInt(task.getEstimatedMinutes(), 0)).sum();
        int catchUpMinutes = activitySummary.missingMinutes() > 0
                ? Math.min(45, Math.max(15, (int) Math.ceil((double) activitySummary.missingMinutes()
                / Math.max(1, SERVER_ACTIVITY_WINDOW_DAYS - activitySummary.activeDays()))))
                : 0;
        int suggestedTodayMinutes = Math.min(
                60,
                Math.max(
                        basePlanMinutes,
                        Math.max(
                                completionForecast.paceStatus().equals("behind") ? catchUpMinutes : 0,
                                completionForecast.paceStatus().equals("ahead") ? 15 : 20)));
        StageMeta stageMeta = buildStageMeta(completedNodes, nodes.size(), prioritizedReviewNodes.size(), currentNode, latestCurrentTurn, currentNodePracticeCompleted);
        List<String> carryOverQueue = buildCarryOverQueue(orderedTasks, inProgressNodes, prioritizedReviewNodes, activitySummary.reflectedToday());

        return LearningPlanBO.builder()
                .generatedAt(generatedAt == null ? LocalDateTime.now() : generatedAt)
                .mode("server")
                .missionTitle(mission.getTitle())
                .missionSummary(mission.getSummary())
                .missionReason(mission.getReason())
                .templateValidationBadge(null)
                .templateValidationSummary(null)
                .handoffTitle(null)
                .handoffSummary(null)
                .handoffActions(Collections.emptyList())
                .totalMinutes(basePlanMinutes)
                .weeklyTargetMinutes(activitySummary.weeklyTargetMinutes())
                .loggedMinutes(activitySummary.loggedMinutes())
                .suggestedTodayMinutes(suggestedTodayMinutes)
                .catchUpMinutes(catchUpMinutes)
                .missedDays(activitySummary.missedDays())
                .backlogMinutes(backlogMinutes)
                .paceStatus(completionForecast.paceStatus())
                .completionStatus(completionForecast.completionStatus())
                .completionTitle(completionForecast.completionTitle())
                .completionSummary(completionForecast.completionSummary())
                .completionConfidence(completionForecast.completionConfidence())
                .remainingMinutes(completionForecast.remainingMinutes())
                .expectedCompletionDays(completionForecast.expectedCompletionDays())
                .targetCompletionDays(completionForecast.targetCompletionDays())
                .scheduleDeltaDays(completionForecast.scheduleDeltaDays())
                .recoveryWindowDays(completionForecast.recoveryWindowDays())
                .recoveryStrategy(completionForecast.recoveryStrategy())
                .recoveryMode(recoveryModeMeta.recoveryMode())
                .recoveryModeTitle(recoveryModeMeta.recoveryModeTitle())
                .stageLabel(stageMeta.stageLabel())
                .milestoneTitle(stageMeta.milestoneTitle())
                .milestoneSummary(stageMeta.milestoneSummary())
                .replanReason(buildReplanReason(completionForecast.paceStatus(), latestCurrentTurn, growthHighlights, backlogMinutes, forgottenReviewCount))
                .streakRisk(buildStreakRisk(activitySummary.reflectedToday(), latestCurrentTurn, prioritizedReviewNodes.size(), completionForecast.paceStatus(), backlogMinutes))
                .tasks(orderedTasks.isEmpty() ? List.of(mission) : orderedTasks)
                .reviewQueue(buildReviewQueue(prioritizedReviewNodes, latestCompletedReviewByNodeId, growthHighlights, latestCurrentTurn))
                .carryOverQueue(carryOverQueue)
                .recoveryNote(buildRecoveryNote(recoveryModeMeta.recoveryMode(), recoveryModeMeta.recoveryModeTitle(), mission, activitySummary.reflectedToday()))
                .build();
    }

    private PlanningContext loadPlanningContext(Long goalId) {
        return buildPlanningContext(loadPlanningReplayData(goalId), null);
    }

    private PlanningReplayData loadPlanningReplayData(Long goalId) {
        Long userId = requireUserId();
        LearningGoalEntity goal = learningGoalService.getOwnedGoalById(goalId, userId);
        LearningErrorEnum.INVALID_GOAL.assertNotNull(goal);

        LearningMapNodeEntity currentNode = learningGoalService.getCurrentNode(goal);
        List<LearningMapNodeEntity> nodes = learningGoalService.listNodesByGoalId(goalId, userId);
        Map<Long, LearningMapNodeEntity> nodeById = nodes.stream()
                .filter(node -> node.getId() != null)
                .collect(Collectors.toMap(LearningMapNodeEntity::getId, Function.identity(), (left, right) -> right));
        Map<Long, LearningNodeProgressEntity> progressByNodeId = learningGoalService.listProgressByGoalId(goalId, userId).stream()
                .collect(Collectors.toMap(LearningNodeProgressEntity::getMapNodeId, Function.identity(), (left, right) -> right));

        List<TutorTurnEntity> tutorTurns = tutorTurnMapper.selectList(
                new QueryWrapper<TutorTurnEntity>()
                        .lambda()
                        .eq(TutorTurnEntity::getGoalId, goalId)
                        .eq(TutorTurnEntity::getUserId, userId)
                        .orderByAsc(TutorTurnEntity::getTurnNo)
                        .orderByAsc(TutorTurnEntity::getCreatedAt)
        );
        List<PracticeTaskEntity> practiceTasks = practiceTaskMapper.selectList(
                new QueryWrapper<PracticeTaskEntity>()
                        .lambda()
                        .eq(PracticeTaskEntity::getGoalId, goalId)
                        .eq(PracticeTaskEntity::getUserId, userId)
        );
        Map<Long, PracticeTaskEntity> practiceTaskById = practiceTasks.stream()
                .filter(task -> task.getId() != null)
                .collect(Collectors.toMap(PracticeTaskEntity::getId, Function.identity(), (left, right) -> right));
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
        List<GrowthSnapshotEntity> growthSnapshots = growthSnapshotMapper.selectList(
                new QueryWrapper<GrowthSnapshotEntity>()
                        .lambda()
                        .eq(GrowthSnapshotEntity::getGoalId, goalId)
                        .eq(GrowthSnapshotEntity::getUserId, userId)
                        .orderByDesc(GrowthSnapshotEntity::getSnapshotDate)
                        .orderByDesc(GrowthSnapshotEntity::getCreatedAt)
        );
        List<DailyDigestEntity> dailyDigests = dailyDigestMapper.selectList(
                new QueryWrapper<DailyDigestEntity>()
                        .lambda()
                        .eq(DailyDigestEntity::getGoalId, goalId)
                        .eq(DailyDigestEntity::getUserId, userId)
                        .orderByDesc(DailyDigestEntity::getDigestDate)
                        .orderByDesc(DailyDigestEntity::getCreatedAt)
        );
        List<LearningEventEntity> events = learningEventReadService.loadGoalSnapshot(goalId, userId).events();

        return new PlanningReplayData(
                userId,
                goal,
                currentNode,
                nodes,
                nodeById,
                progressByNodeId,
                tutorTurns,
                practiceTasks,
                practiceTaskById,
                practiceAttempts,
                reviewAttempts,
                growthSnapshots,
                dailyDigests,
                events);
    }

    private PlanningContext buildPlanningContext(PlanningReplayData replayData, LocalDateTime cutoff) {
        List<TutorTurnEntity> tutorTurns = cutoff == null
                ? replayData.tutorTurns()
                : replayData.tutorTurns().stream()
                .filter(turn -> isOnOrBefore(turn.getCreatedAt(), cutoff))
                .toList();
        List<PracticeAttemptEntity> practiceAttempts = cutoff == null
                ? replayData.practiceAttempts()
                : replayData.practiceAttempts().stream()
                .filter(attempt -> isOnOrBefore(resolveAttemptAt(attempt), cutoff))
                .toList();
        List<ReviewAttemptEntity> reviewAttempts = cutoff == null
                ? replayData.reviewAttempts()
                : replayData.reviewAttempts().stream()
                .filter(attempt -> isOnOrBefore(resolveReviewAttemptAt(attempt), cutoff))
                .toList();
        List<GrowthSnapshotEntity> growthSnapshots = cutoff == null
                ? replayData.growthSnapshots()
                : replayData.growthSnapshots().stream()
                .filter(snapshot -> isOnOrBefore(resolveGrowthSnapshotAt(snapshot), cutoff))
                .toList();
        List<DailyDigestEntity> dailyDigests = cutoff == null
                ? replayData.dailyDigests()
                : replayData.dailyDigests().stream()
                .filter(digest -> isOnOrBefore(resolveDigestAt(digest), cutoff))
                .toList();
        List<LearningEventEntity> events = cutoff == null
                ? replayData.events()
                : replayData.events().stream()
                .filter(event -> isOnOrBefore(resolveEventAt(event), cutoff))
                .toList();
        LearningEventReadService.GoalEventSnapshot eventSnapshot = learningEventReadService.projectGoalEvents(events);

        Map<Long, TutorSignal> latestTutorByNodeId = new HashMap<>();
        latestTurnByNodeId(tutorTurns).forEach((nodeId, turn) -> latestTutorByNodeId.put(nodeId, toTutorSignal(turn)));
        eventSnapshot.latestTutorByNodeId().forEach((nodeId, signal) ->
                latestTutorByNodeId.merge(nodeId, toTutorSignal(signal), this::newerTutorSignal));

        Map<Long, TutorSignal> latestTutorBySessionId = new HashMap<>();
        latestTurnBySessionId(tutorTurns).forEach((sessionId, turn) -> latestTutorBySessionId.put(sessionId, toTutorSignal(turn)));
        eventSnapshot.latestTutorBySessionId().forEach((sessionId, signal) ->
                latestTutorBySessionId.merge(sessionId, toTutorSignal(signal), this::newerTutorSignal));

        Map<Long, CompletedPracticeSignal> completedPracticeSignalsByAttemptId = new HashMap<>(
                buildCompletedPracticeSignals(practiceAttempts, replayData.practiceTaskById()));
        eventSnapshot.latestPracticeByAttemptId().values().stream()
                .map(this::toCompletedPracticeSignal)
                .filter(Objects::nonNull)
                .forEach(signal -> completedPracticeSignalsByAttemptId.merge(
                        signal.attemptId(),
                        signal,
                        this::newerCompletedPracticeSignal));

        Map<Long, ReviewSignal> reviewSignalsByAttemptId = new HashMap<>(buildReviewSignals(reviewAttempts));
        eventSnapshot.latestReviewByAttemptId().values().stream()
                .map(this::toReviewSignal)
                .filter(Objects::nonNull)
                .forEach(signal -> reviewSignalsByAttemptId.merge(
                        signal.attemptId(),
                        signal,
                        this::newerReviewSignal));

        Map<Long, ReflectionSignal> reflectionSignalsByEntryId = new HashMap<>(buildReflectionSignals(dailyDigests));
        eventSnapshot.latestReflectionByEntryId().values().stream()
                .map(this::toReflectionSignal)
                .filter(Objects::nonNull)
                .forEach(signal -> reflectionSignalsByEntryId.merge(
                        signal.reflectionEntryId(),
                        signal,
                this::newerReflectionSignal));

        return new PlanningContext(
                replayData.userId(),
                replayData.goal(),
                replayData.currentNode(),
                replayData.nodes(),
                replayData.nodeById(),
                replayData.progressByNodeId(),
                latestTutorByNodeId,
                latestTutorBySessionId,
                new ArrayList<>(completedPracticeSignalsByAttemptId.values()),
                new ArrayList<>(reviewSignalsByAttemptId.values()),
                growthSnapshots,
                new ArrayList<>(reflectionSignalsByEntryId.values())
        );
    }

    private Map<Long, TutorTurnEntity> latestTurnByNodeId(List<TutorTurnEntity> turns) {
        return CollUtil.emptyIfNull(turns).stream()
                .filter(turn -> turn.getMapNodeId() != null)
                .collect(Collectors.toMap(
                        TutorTurnEntity::getMapNodeId,
                        Function.identity(),
                        (left, right) -> compareDateTime(left.getCreatedAt(), right.getCreatedAt()) >= 0 ? left : right));
    }

    private Map<Long, TutorTurnEntity> latestTurnBySessionId(List<TutorTurnEntity> turns) {
        return CollUtil.emptyIfNull(turns).stream()
                .filter(turn -> turn.getSessionId() != null)
                .collect(Collectors.toMap(
                        TutorTurnEntity::getSessionId,
                        Function.identity(),
                        (left, right) -> {
                            int turnCompare = Integer.compare(defaultInt(left.getTurnNo(), 0), defaultInt(right.getTurnNo(), 0));
                            if (turnCompare != 0) {
                                return turnCompare >= 0 ? left : right;
                            }
                            return compareDateTime(left.getCreatedAt(), right.getCreatedAt()) >= 0 ? left : right;
                        }));
    }

    private Map<Long, CompletedPracticeSignal> buildCompletedPracticeSignals(List<PracticeAttemptEntity> attempts,
                                                                             Map<Long, PracticeTaskEntity> taskById) {
        return CollUtil.emptyIfNull(attempts).stream()
                .filter(attempt -> attempt.getCompleted() != null && attempt.getCompleted() == 1)
                .map(attempt -> {
                    PracticeTaskEntity task = taskById.get(attempt.getPracticeTaskId());
                    return new CompletedPracticeSignal(
                            attempt.getId(),
                            attempt.getMapNodeId(),
                            resolvePracticeRating(attempt),
                            task == null ? null : task.getTaskType(),
                            task == null ? null : task.getEvidenceKind(),
                            task == null ? DEFAULT_PRACTICE_MINUTES : capMinutes(task.getEstimatedMinutes(), DEFAULT_PRACTICE_MINUTES, 20),
                            attempt.getUpdatedAt());
                })
                .filter(signal -> signal.attemptId() != null)
                .collect(Collectors.toMap(
                        CompletedPracticeSignal::attemptId,
                        Function.identity(),
                        this::newerCompletedPracticeSignal));
    }

    private ActivitySummary buildActivitySummary(PlanningContext context, LocalDate anchorDate) {
        LocalDate today = anchorDate == null ? LocalDate.now() : anchorDate;
        LocalDate windowStart = today.minusDays(SERVER_ACTIVITY_WINDOW_DAYS - 1L);
        Map<LocalDate, Integer> activityByDay = new HashMap<>();

        context.latestTurnBySessionId().values().forEach(turn ->
                addActivity(activityByDay, toDate(turn.eventAt()), DEFAULT_TUTOR_MINUTES, windowStart));
        context.completedPracticeSignals().forEach(signal ->
                addActivity(activityByDay, toDate(signal.updatedAt()), signal.minutes(), windowStart));
        context.reviewSignals().forEach(signal ->
                addActivity(activityByDay, toDate(signal.updatedAt()), signal.minutes(), windowStart));
        context.reflectionSignals().forEach(signal ->
                addActivity(activityByDay, signal.reflectionDate(), DEFAULT_REFLECTION_MINUTES, windowStart));

        int loggedMinutes = activityByDay.values().stream().mapToInt(Integer::intValue).sum();
        int activeDays = (int) activityByDay.values().stream().filter(minutes -> minutes > 0).count();
        boolean todayDone = activityByDay.getOrDefault(today, 0) > 0;
        boolean reflectedToday = context.reflectionSignals().stream().anyMatch(signal -> Objects.equals(signal.reflectionDate(), today));
        int weeklyTargetMinutes = Math.max(defaultInt(context.goal().getWeeklyLearningMinutes(), 0), DEFAULT_WEEKLY_MINUTES);
        int completionPercent = weeklyTargetMinutes <= 0 ? 0 : Math.min(100, (int) Math.round((loggedMinutes * 100.0) / weeklyTargetMinutes));
        int missedDays = SERVER_ACTIVITY_WINDOW_DAYS - activeDays;
        int missingMinutes = Math.max(0, weeklyTargetMinutes - loggedMinutes);
        return new ActivitySummary(
                loggedMinutes,
                activeDays,
                missedDays,
                completionPercent,
                weeklyTargetMinutes,
                todayDone,
                reflectedToday,
                missingMinutes
        );
    }

    private void addActivity(Map<LocalDate, Integer> activityByDay,
                             LocalDate activityDate,
                             int minutes,
                             LocalDate windowStart) {
        if (activityDate == null || activityDate.isBefore(windowStart)) {
            return;
        }
        activityByDay.merge(activityDate, minutes, Integer::sum);
    }

    private CompletionForecast buildCompletionForecast(LearningGoalEntity goal,
                                                       List<LearningMapNodeEntity> nodes,
                                                       Map<Long, LearningNodeProgressEntity> progressByNodeId,
                                                       int backlogMinutes,
                                                       ActivitySummary activitySummary,
                                                       LocalDate anchorDate) {
        int remainingMinutes = Math.max(
                sumEstimatedMinutes(
                        nodes.stream()
                                .filter(node -> !progressStatusOf(node.getId(), progressByNodeId).equals("COMPLETED"))
                                .toList(),
                        DEFAULT_REPAIR_MINUTES,
                        30) + backlogMinutes,
                DEFAULT_WEEKLY_MINUTES);
        int effectiveDailyMinutes = Math.max(10, activitySummary.weeklyTargetMinutes() / SERVER_ACTIVITY_WINDOW_DAYS);
        int expectedCompletionDays = Math.max(3, (int) Math.ceil((double) remainingMinutes / effectiveDailyMinutes));
        int targetCompletionDays = Math.max(7, defaultInt(goal.getEstimatedDays(), 42));
        LocalDate evaluationDate = anchorDate == null ? LocalDate.now() : anchorDate;
        long elapsedDays = goal.getCreatedAt() == null
                ? 1
                : Math.max(1, ChronoUnit.DAYS.between(goal.getCreatedAt().toLocalDate(), evaluationDate));
        int remainingDays = Math.max(1, targetCompletionDays - (int) elapsedDays);
        int scheduleDeltaDays = expectedCompletionDays - remainingDays;
        int completionConfidence = Math.max(
                35,
                Math.min(92,
                        72
                                + (activitySummary.completionPercent() >= 70 ? 8 : activitySummary.completionPercent() >= 50 ? 3 : -6)
                                + (backlogMinutes >= 35 ? -8 : backlogMinutes >= 20 ? -4 : 2)));
        String completionStatus = scheduleDeltaDays >= 7 || completionConfidence <= 48
                ? "at_risk"
                : scheduleDeltaDays >= 2 || completionConfidence <= 66
                ? "watch"
                : "on_track";
        String completionTitle = completionStatus.equals("on_track")
                ? "按当前节奏，这条主线仍在可兑现区间"
                : completionStatus.equals("watch")
                ? "这条主线还可达成，但已经开始轻微漂移"
                : "按当前节奏，这条主线大概率会延后，需要系统主动修正";
        String completionSummary = completionStatus.equals("on_track")
                ? "系统估计当前还需要约 " + remainingMinutes + " 分钟、" + expectedCompletionDays + " 天可以收住这条目标主线。"
                : completionStatus.equals("watch")
                ? "系统估计当前还需要约 " + remainingMinutes + " 分钟、" + expectedCompletionDays + " 天，已经开始轻微偏离原计划。"
                : "系统估计当前还需要约 " + remainingMinutes + " 分钟、" + expectedCompletionDays + " 天，已经明显慢于原计划，需要主动回收旧债并压缩切换成本。";
        int recoveryWindowDays = Math.max(1, (int) Math.ceil(Math.max(0, scheduleDeltaDays) / 2.0));
        List<String> recoveryStrategy = buildRecoveryStrategy(completionStatus, backlogMinutes, recoveryWindowDays);
        String paceStatus = buildPaceStatus(activitySummary.completionPercent(), activitySummary.missedDays(), backlogMinutes, activitySummary.todayDone());
        return new CompletionForecast(
                completionStatus,
                completionTitle,
                completionSummary,
                completionConfidence,
                remainingMinutes,
                expectedCompletionDays,
                targetCompletionDays,
                scheduleDeltaDays,
                recoveryWindowDays,
                recoveryStrategy,
                paceStatus
        );
    }

    private String buildPaceStatus(int completionPercent, int missedDays, int backlogMinutes, boolean todayDone) {
        if (completionPercent < 45 || missedDays >= 2 || backlogMinutes >= 35 || !todayDone) {
            return "behind";
        }
        if (completionPercent >= 90 && backlogMinutes < 18 && todayDone) {
            return "ahead";
        }
        return "steady";
    }

    private List<String> buildRecoveryStrategy(String completionStatus, int backlogMinutes, int recoveryWindowDays) {
        List<String> strategy = new ArrayList<>();
        strategy.add(completionStatus.equals("on_track")
                ? "继续保持当前主线，不要因为短期顺畅就额外扩张范围。"
                : "接下来 " + recoveryWindowDays + " 天先把主线、复盘堆积和 Reflection 排成固定顺序，不再额外开新主题。");
        strategy.add(backlogMinutes > 0
                ? "先回收大约 " + backlogMinutes + " 分钟的复盘、挂起或封箱堆积，避免旧债继续拖慢兑现速度。"
                : "当前没有明显旧债堆积，系统更适合把新增时间继续压在同一条主线上。");
        strategy.add("把这周新增投入优先给当前节点和最接近的下一节点，不要分散到更远的主题。");
        return strategy;
    }

    private RecoveryModeMeta buildRecoveryModeMeta(String completionStatus,
                                                   int scheduleDeltaDays,
                                                   int backlogMinutes,
                                                   TutorSignal latestTurn,
                                                   int reviewingCount,
                                                   int forgottenReviewCount,
                                                   ActivitySummary activitySummary) {
        boolean recoveryMode = completionStatus.equals("at_risk")
                || scheduleDeltaDays >= 3
                || backlogMinutes >= 35
                || forgottenReviewCount > 0
                || CharSequenceUtil.equals(latestDiagnosis(latestTurn), "needs_prereq")
                || reviewingCount >= 2
                || (activitySummary.completionPercent() < 45 && !activitySummary.todayDone());
        if (!recoveryMode) {
            return new RecoveryModeMeta(false, null);
        }
        if (forgottenReviewCount > 0) {
            return new RecoveryModeMeta(true, "先回收遗忘点，再恢复主线推进");
        }
        if (CharSequenceUtil.equals(latestDiagnosis(latestTurn), "needs_prereq")) {
            return new RecoveryModeMeta(true, "先补前置，再恢复主线推进");
        }
        if (backlogMinutes >= 35) {
            return new RecoveryModeMeta(true, "先回收旧债，再恢复主线兑现");
        }
        if (completionStatus.equals("at_risk") || scheduleDeltaDays >= 3) {
            return new RecoveryModeMeta(true, "先拉回兑现节奏，再继续扩张主线");
        }
        return new RecoveryModeMeta(true, "先恢复稳定闭环，再继续正常推进");
    }

    private List<LearningPlanTaskBO> applyRecoveryMode(List<LearningPlanTaskBO> tasks,
                                                       boolean recoveryMode,
                                                       TutorSignal latestTurn) {
        if (!recoveryMode) {
            return tasks;
        }
        return tasks.stream()
                .map(task -> {
                    int priority = defaultInt(task.getPriority(), 0);
                    int minutes = defaultInt(task.getEstimatedMinutes(), DEFAULT_TUTOR_MINUTES);
                    if (CharSequenceUtil.equals(task.getKind(), "review")) {
                        priority += 18;
                        minutes = Math.min(minutes, 15);
                    } else if (CharSequenceUtil.equals(task.getKind(), "repair")) {
                        priority += 20;
                        minutes = Math.min(minutes, 18);
                    } else if (CharSequenceUtil.equals(task.getKind(), "reflect")) {
                        priority += 16;
                        minutes = Math.min(minutes, 10);
                    } else if (CharSequenceUtil.equals(task.getKind(), "practice")) {
                        priority += 6;
                        minutes = Math.min(minutes, 12);
                    } else if (CharSequenceUtil.equals(task.getKind(), "learn")) {
                        if (task.getId() != null && task.getId().startsWith("ready-")) {
                            priority -= 28;
                        }
                        minutes = Math.min(minutes, 20);
                    }
                    return taskBuilder()
                            .id(task.getId())
                            .kind(task.getKind())
                            .title(task.getTitle())
                            .summary(CharSequenceUtil.equals(task.getKind(), "repair") && CharSequenceUtil.equals(latestDiagnosis(latestTurn), "needs_prereq")
                                    ? "系统先要求补前置，再回来推进主线，这样比继续硬冲更省时间。"
                                    : task.getSummary())
                            .reason(task.getReason())
                            .estimatedMinutes(minutes)
                            .nodeId(task.getNodeId())
                            .nodeTitle(task.getNodeTitle())
                            .targetSection(task.getTargetSection())
                            .priority(priority)
                            .recoveryMode(Boolean.TRUE)
                            .build();
                })
                .toList();
    }

    private StageMeta buildStageMeta(int completedNodes,
                                     int totalNodes,
                                     int reviewingCount,
                                     LearningMapNodeEntity currentNode,
                                     TutorSignal latestTurn,
                                     int currentNodePracticeCompleted) {
        int progressPercent = totalNodes == 0 ? 0 : (int) Math.round((completedNodes * 100.0) / totalNodes);
        if (completedNodes == 0) {
            return new StageMeta(
                    "启动搭建期",
                    currentNode == null ? "先完成第一轮真实学习闭环" : "先让「" + currentNode.getTitle() + "」形成第一个可验证闭环",
                    latestTurn == null
                            ? "现在最关键的不是学更多，而是先完成一轮 Tutor -> Practice / Reflection 的最小闭环。"
                            : "当前已经开始诊断，接下来最重要的是把理解转成练习和 Reflection。");
        }
        if (CharSequenceUtil.equals(latestDiagnosis(latestTurn), "needs_prereq")) {
            return new StageMeta("前置回补期", "先补当前最关键的前置缺口", "当前更重要的是把主线卡住的前置关系补顺，再恢复正常推进。");
        }
        if (reviewingCount > 0 || progressPercent >= 70) {
            return new StageMeta("收敛巩固期", "把已推进的节点收成稳定掌握", "已经有节点进入复盘窗口，当前里程碑不是开更多新内容，而是把学过的东西真正稳住。");
        }
        if (currentNodePracticeCompleted > 0 || progressPercent >= 35) {
            return new StageMeta(
                    "主线推进期",
                    currentNode == null ? "继续沿当前主线推进" : "继续沿「" + currentNode.getTitle() + "」把主线往前推",
                    "你已经不在纯启动阶段了。现在更重要的是保持单线推进，别频繁切到新主题。");
        }
        return new StageMeta(
                "基础建立期",
                currentNode == null ? "先把当前阶段的基础节点打牢" : "先把「" + currentNode.getTitle() + "」的前置和边界打牢",
                "当前适合把前置关系和核心概念先连顺，再追求更多内容覆盖。");
    }

    private String buildReplanReason(String paceStatus,
                                     TutorSignal latestTurn,
                                     GrowthHighlights growthHighlights,
                                     int backlogMinutes,
                                     int forgottenReviewCount) {
        if (forgottenReviewCount > 0) {
            return "最近复盘已经暴露真实遗忘点，系统会先把已学内容收回来，再决定还能不能继续向前推进。";
        }
        if (CharSequenceUtil.equals(latestDiagnosis(latestTurn), "needs_prereq")) {
            return "当前的重排重点不是多学一点，而是先补前置。系统会优先把时间重新分配给基础节点，而不是继续硬推主线。";
        }
        if (backlogMinutes >= 30) {
            return "当前已经有明显的复盘和挂起堆积，系统会先收旧内容，再决定还能不能往前推进。";
        }
        if (!growthHighlights.commonStickingPoints().isEmpty()) {
            return "成长时间线显示「" + growthHighlights.commonStickingPoints().getFirst() + "」在重复出现，所以今天的任务顺序会优先围绕这个卡点重排。";
        }
        if (CharSequenceUtil.equals(paceStatus, "behind")) {
            return "最近一周的真实学习动作还不够连续，当前计划会更偏向恢复动量和压缩切换成本。";
        }
        if (CharSequenceUtil.equals(paceStatus, "ahead")) {
            return "当前节奏已经跑顺，系统会把今天的计划维持在稳态推进，而不是强行加码。";
        }
        return "当前没有明显断档或堆积，系统会优先保持主线连续，再把复盘和封箱动作压进同一天完成。";
    }

    private String buildStreakRisk(boolean reflectedToday,
                                   TutorSignal latestTurn,
                                   int reviewingCount,
                                   String paceStatus,
                                   int backlogMinutes) {
        if ((!reflectedToday && latestTurn == null) || CharSequenceUtil.equals(paceStatus, "behind") || backlogMinutes >= 45) {
            return "high";
        }
        if (!reflectedToday || reviewingCount > 0 || CharSequenceUtil.equals(latestDiagnosis(latestTurn), "needs_prereq") || backlogMinutes >= 20) {
            return "medium";
        }
        return "low";
    }

    private List<String> buildReviewQueue(List<LearningMapNodeEntity> reviewingNodes,
                                          Map<Long, ReviewSignal> latestReviewByNodeId,
                                          GrowthHighlights growthHighlights,
                                          TutorSignal latestTurn) {
        LinkedHashSet<String> queue = new LinkedHashSet<>();
        reviewingNodes.stream()
                .limit(2)
                .forEach(node -> {
                    ReviewSignal latestReview = latestReviewByNodeId.get(node.getId());
                    if (latestReview != null && CharSequenceUtil.equals(latestReview.rating(), "forgotten")) {
                        queue.add(node.getTitle() + "：最近一次复盘已经忘了，今天优先回收这个遗忘点");
                        return;
                    }
                    if (latestReview != null && CharSequenceUtil.equals(latestReview.rating(), "wobbly")) {
                        queue.add(node.getTitle() + "：最近一次复盘还不稳，适合安排一轮短回想");
                        return;
                    }
                    if (latestReview != null && CharSequenceUtil.equals(latestReview.rating(), "solid")) {
                        queue.add(node.getTitle() + "：刚在复盘里稳住，下一轮只需要短检查别让它回落");
                        return;
                    }
                    queue.add(node.getTitle() + "：现在适合做一次短复盘");
                });
        growthHighlights.commonStickingPoints().stream().limit(2).forEach(queue::add);
        if (CharSequenceUtil.equals(latestDiagnosis(latestTurn), "misconception")) {
            queue.add("当前节点仍有概念误解，复述关键概念时要刻意用自己的话解释。");
        }
        if (CharSequenceUtil.equals(latestDiagnosis(latestTurn), "needs_prereq")) {
            queue.add("当前节点存在前置缺口，今天不要硬冲，先补基础再回来。");
        }
        return queue.stream().limit(3).toList();
    }

    private List<String> buildCarryOverQueue(List<LearningPlanTaskBO> orderedTasks,
                                             List<LearningMapNodeEntity> inProgressNodes,
                                             List<LearningMapNodeEntity> reviewingNodes,
                                             boolean reflectedToday) {
        LinkedHashSet<String> carryOvers = new LinkedHashSet<>();
        orderedTasks.stream().skip(1).forEach(task -> carryOvers.add(task.getTitle() + "：如果主任务做完，再继续处理它"));
        if (!inProgressNodes.isEmpty()) {
            carryOvers.add(inProgressNodes.getFirst().getTitle() + "：这是已经开始但还没收住的节点");
        }
        if (!reviewingNodes.isEmpty()) {
            carryOvers.add(reviewingNodes.getFirst().getTitle() + "：需要在接下来 1 到 2 次学习里回收");
        }
        if (!reflectedToday) {
            carryOvers.add("Reflection：今天结束前别忘记把理解和卡点写回系统");
        }
        return carryOvers.stream().limit(4).toList();
    }

    private List<PlannerHistoryEntryBO> buildReplanHistory(PlanningReplayData replayData) {
        List<PlannerHistoryEntryBO> chronological = new ArrayList<>();
        PlannerHistoryEntryBO previous = null;

        for (LearningEventEntity event : replayData.events()) {
            if (!isReplayCheckpoint(event)) {
                continue;
            }
            LocalDateTime eventAt = resolveEventAt(event);
            if (eventAt == null) {
                continue;
            }
            LearningPlanBO snapshot = buildPlan(buildPlanningContext(replayData, eventAt), eventAt);
            PlannerHistoryEntryBO next = toPlannerHistoryEntry(snapshot, previous, event);
            if (previous != null && CharSequenceUtil.equals(previous.getFingerprint(), next.getFingerprint())) {
                continue;
            }
            chronological.add(next);
            previous = next;
        }

        List<PlannerHistoryEntryBO> items = new ArrayList<>(chronological);
        Collections.reverse(items);
        return items;
    }

    private boolean isReplayCheckpoint(LearningEventEntity event) {
        if (event == null || event.getGoalId() == null) {
            return false;
        }
        if (CharSequenceUtil.equals(event.getEventSource(), "TUTOR")) {
            return CharSequenceUtil.equalsAny(event.getEventType(), "SESSION_STARTED", "TURN_RECORDED");
        }
        if (CharSequenceUtil.equals(event.getEventSource(), "PRACTICE")) {
            return CharSequenceUtil.equals(event.getEventType(), "ATTEMPT_SAVED");
        }
        if (CharSequenceUtil.equals(event.getEventSource(), "REVIEW")) {
            return CharSequenceUtil.equals(event.getEventType(), "ATTEMPT_SAVED");
        }
        if (CharSequenceUtil.equals(event.getEventSource(), "REFLECTION")) {
            return CharSequenceUtil.equals(event.getEventType(), "REFLECTION_SUBMITTED");
        }
        return false;
    }

    private PlannerHistoryEntryBO toPlannerHistoryEntry(LearningPlanBO snapshot,
                                                        PlannerHistoryEntryBO previous,
                                                        LearningEventEntity triggerEvent) {
        LocalDateTime recordedAt = snapshot.getGeneratedAt();
        return PlannerHistoryEntryBO.builder()
                .id("plan-" + recordedAt)
                .recordedAt(recordedAt)
                .fingerprint(buildPlanFingerprint(snapshot))
                .missionTitle(snapshot.getMissionTitle())
                .missionSummary(snapshot.getMissionSummary())
                .stageLabel(snapshot.getStageLabel())
                .templateValidationBadge(snapshot.getTemplateValidationBadge())
                .templateValidationSummary(snapshot.getTemplateValidationSummary())
                .paceStatus(snapshot.getPaceStatus())
                .completionStatus(snapshot.getCompletionStatus())
                .completionConfidence(snapshot.getCompletionConfidence())
                .suggestedTodayMinutes(snapshot.getSuggestedTodayMinutes())
                .backlogMinutes(snapshot.getBacklogMinutes())
                .streakRisk(snapshot.getStreakRisk())
                .milestoneTitle(snapshot.getMilestoneTitle())
                .replanReason(snapshot.getReplanReason())
                .reviewQueue(CollUtil.emptyIfNull(snapshot.getReviewQueue()))
                .carryOverQueue(CollUtil.emptyIfNull(snapshot.getCarryOverQueue()))
                .changedFields(detectChangedFields(previous, snapshot))
                .triggerEventSource(triggerEvent == null ? null : triggerEvent.getEventSource())
                .triggerEventDetailType(triggerEvent == null ? null : triggerEvent.getEventType())
                .triggerEventStatus(triggerEvent == null ? null : triggerEvent.getEventStatus())
                .triggerTitle(triggerEvent == null ? null : triggerEvent.getTitle())
                .triggerSummary(triggerEvent == null ? null : triggerEvent.getSummary())
                .build();
    }

    private List<String> detectChangedFields(PlannerHistoryEntryBO previous, LearningPlanBO snapshot) {
        if (previous == null) {
            return List.of("mission", "pace", "time", "review", "carry_over", "milestone");
        }

        List<String> changed = new ArrayList<>();
        if (!Objects.equals(previous.getMissionTitle(), snapshot.getMissionTitle())) {
            changed.add("mission");
        }
        if (!Objects.equals(CharSequenceUtil.emptyIfNull(previous.getTemplateValidationBadge()), CharSequenceUtil.emptyIfNull(snapshot.getTemplateValidationBadge()))
                || !Objects.equals(CharSequenceUtil.emptyIfNull(previous.getTemplateValidationSummary()), CharSequenceUtil.emptyIfNull(snapshot.getTemplateValidationSummary()))) {
            changed.add("template");
        }
        if (!Objects.equals(previous.getPaceStatus(), snapshot.getPaceStatus())
                || !Objects.equals(previous.getStreakRisk(), snapshot.getStreakRisk())
                || !Objects.equals(previous.getBacklogMinutes(), snapshot.getBacklogMinutes())) {
            changed.add("pace");
        }
        if (!Objects.equals(previous.getSuggestedTodayMinutes(), snapshot.getSuggestedTodayMinutes())) {
            changed.add("time");
        }
        if (arrayChanged(previous.getReviewQueue(), snapshot.getReviewQueue())) {
            changed.add("review");
        }
        if (arrayChanged(previous.getCarryOverQueue(), snapshot.getCarryOverQueue())) {
            changed.add("carry_over");
        }
        if (!Objects.equals(previous.getMilestoneTitle(), snapshot.getMilestoneTitle())) {
            changed.add("milestone");
        }
        return changed;
    }

    private String buildPlanFingerprint(LearningPlanBO snapshot) {
        return String.join("::",
                CharSequenceUtil.nullToEmpty(snapshot.getMissionTitle()),
                CharSequenceUtil.nullToEmpty(snapshot.getStageLabel()),
                CharSequenceUtil.nullToEmpty(snapshot.getTemplateValidationBadge()),
                CharSequenceUtil.nullToEmpty(snapshot.getTemplateValidationSummary()),
                CharSequenceUtil.nullToEmpty(snapshot.getPaceStatus()),
                CharSequenceUtil.nullToEmpty(snapshot.getCompletionStatus()),
                String.valueOf(snapshot.getCompletionConfidence()),
                String.valueOf(snapshot.getSuggestedTodayMinutes()),
                String.valueOf(snapshot.getBacklogMinutes()),
                CharSequenceUtil.nullToEmpty(snapshot.getStreakRisk()),
                CharSequenceUtil.nullToEmpty(snapshot.getMilestoneTitle()),
                String.join("|", CollUtil.emptyIfNull(snapshot.getReviewQueue())),
                String.join("|", CollUtil.emptyIfNull(snapshot.getCarryOverQueue())),
                CollUtil.emptyIfNull(snapshot.getTasks()).stream()
                        .map(task -> task.getId() + ":" + defaultInt(task.getPriority(), 0))
                        .collect(Collectors.joining("|")));
    }

    private boolean arrayChanged(List<String> left, List<String> right) {
        return String.join("|", CollUtil.emptyIfNull(left))
                .equals(String.join("|", CollUtil.emptyIfNull(right)))
                ? false
                : true;
    }

    private String buildRecoveryNote(boolean recoveryMode,
                                     String recoveryModeTitle,
                                     LearningPlanTaskBO mission,
                                     boolean reflectedToday) {
        if (!recoveryMode) {
            return reflectedToday
                    ? "如果今天推进顺利，明天优先沿着这条主任务继续，而不是重新换一个新主题开始。"
                    : "如果主任务完成，别直接离开，最后用 5 到 10 分钟把今天的理解和卡点写进 Reflection。";
        }
        return CharSequenceUtil.isNotBlank(recoveryModeTitle)
                ? recoveryModeTitle + "。先完成「" + mission.getTitle() + "」，再决定今天还能不能处理其他任务。"
                : "当前系统处于恢复模式，先完成主任务，再决定今天还能不能处理其他任务。";
    }

    private LearningPlanTaskBO fallbackMission(LearningMapNodeEntity currentNode) {
        return taskBuilder()
                .id("mission-fallback")
                .kind("learn")
                .title(currentNode == null ? "回到当前学习路径" : "回到「" + currentNode.getTitle() + "」")
                .summary("先打开地图确认当前阶段，再决定今天是推进新节点还是补复习。")
                .reason("系统还没有足够的上下文来生成更具体的任务。")
                .estimatedMinutes(DEFAULT_TUTOR_MINUTES)
                .nodeId(currentNode == null ? null : currentNode.getId())
                .nodeTitle(currentNode == null ? null : currentNode.getTitle())
                .targetSection("map")
                .priority(60)
                .recoveryMode(Boolean.FALSE)
                .build();
    }

    private LearningPlanTaskBO.LearningPlanTaskBOBuilder taskBuilder() {
        return LearningPlanTaskBO.builder();
    }

    private List<LearningPlanTaskBO> uniqueTasks(List<LearningPlanTaskBO> tasks) {
        Set<String> seen = new LinkedHashSet<>();
        return tasks.stream()
                .filter(task -> task.getId() != null)
                .filter(task -> seen.add(task.getId()))
                .toList();
    }

    private GrowthHighlights buildGrowthHighlights(List<GrowthSnapshotEntity> snapshots) {
        List<String> cognitiveChanges = CollUtil.emptyIfNull(snapshots).stream()
                .filter(item -> CharSequenceUtil.equals(item.getEventType(), "COGNITION"))
                .map(GrowthSnapshotEntity::getSummary)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .limit(4)
                .toList();
        List<String> stickingPoints = CollUtil.emptyIfNull(snapshots).stream()
                .filter(item -> CharSequenceUtil.equals(item.getEventType(), "STUCK"))
                .map(GrowthSnapshotEntity::getSummary)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .limit(4)
                .toList();
        return new GrowthHighlights(cognitiveChanges, stickingPoints);
    }

    private String progressStatusOf(Long nodeId, Map<Long, LearningNodeProgressEntity> progressByNodeId) {
        LearningNodeProgressEntity progress = nodeId == null ? null : progressByNodeId.get(nodeId);
        return progress == null ? "PENDING" : CharSequenceUtil.blankToDefault(progress.getStatus(), "PENDING");
    }

    private int sumEstimatedMinutes(Collection<LearningMapNodeEntity> nodes, int fallback, int max) {
        if (nodes == null || nodes.isEmpty()) {
            return 0;
        }
        return nodes.stream()
                .mapToInt(node -> capMinutes(node.getEstimatedMinutes(), fallback, max))
                .sum();
    }

    private int capMinutes(Integer minutes, int fallback, int max) {
        if (minutes == null || minutes <= 0) {
            return fallback;
        }
        return Math.max(10, Math.min(minutes, max));
    }

    private String latestDiagnosis(TutorSignal turn) {
        return turn == null ? null : turn.diagnosis();
    }

    private List<String> nextStepSuggestions(TutorSignal turn) {
        return turn == null ? Collections.emptyList() : CollUtil.emptyIfNull(turn.nextStepSuggestions());
    }

    private String firstNonBlank(List<String> values, String fallback) {
        return CollUtil.emptyIfNull(values).stream()
                .filter(CharSequenceUtil::isNotBlank)
                .findFirst()
                .orElse(fallback);
    }

    private LocalDate toDate(LocalDateTime time) {
        return time == null ? null : time.toLocalDate();
    }

    private String resolvePracticeRating(PracticeAttemptEntity attempt) {
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
                // fall through to self rating
            }
        }
        return CharSequenceUtil.blankToDefault(attempt.getSelfRating(), "stretch");
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

    private LocalDateTime resolveAttemptAt(PracticeAttemptEntity attempt) {
        if (attempt == null) {
            return null;
        }
        if (attempt.getUpdatedAt() != null) {
            return attempt.getUpdatedAt();
        }
        if (attempt.getCreatedAt() != null) {
            return attempt.getCreatedAt();
        }
        return null;
    }

    private LocalDateTime resolveGrowthSnapshotAt(GrowthSnapshotEntity snapshot) {
        if (snapshot == null) {
            return null;
        }
        if (snapshot.getCreatedAt() != null) {
            return snapshot.getCreatedAt();
        }
        if (snapshot.getUpdatedAt() != null) {
            return snapshot.getUpdatedAt();
        }
        return snapshot.getSnapshotDate() == null ? null : snapshot.getSnapshotDate().atStartOfDay();
    }

    private LocalDateTime resolveDigestAt(DailyDigestEntity digest) {
        if (digest == null) {
            return null;
        }
        if (digest.getCreatedAt() != null) {
            return digest.getCreatedAt();
        }
        if (digest.getUpdatedAt() != null) {
            return digest.getUpdatedAt();
        }
        return digest.getDigestDate() == null ? null : digest.getDigestDate().atStartOfDay();
    }

    private LocalDateTime resolveEventAt(LearningEventEntity event) {
        if (event == null) {
            return null;
        }
        if (event.getEventAt() != null) {
            return event.getEventAt();
        }
        if (event.getCreatedAt() != null) {
            return event.getCreatedAt();
        }
        return event.getUpdatedAt();
    }

    private boolean isOnOrBefore(LocalDateTime value, LocalDateTime cutoff) {
        if (cutoff == null) {
            return true;
        }
        if (value == null) {
            return false;
        }
        return !value.isAfter(cutoff);
    }

    private TutorSignal toTutorSignal(TutorTurnEntity turn) {
        if (turn == null) {
            return null;
        }
        return new TutorSignal(
                turn.getMapNodeId(),
                turn.getSessionId(),
                turn.getDiagnosis(),
                turn.getActionType(),
                turn.getRecommendedNodeId(),
                turn.getNodeCompleted() != null && turn.getNodeCompleted() == 1,
                turn.getTurnNo(),
                turn.getCreatedAt(),
                nextStepSuggestionsFromJson(turn.getNextStepSuggestionsJson()));
    }

    private TutorSignal toTutorSignal(LearningEventReadService.TutorEventProjection signal) {
        if (signal == null) {
            return null;
        }
        return new TutorSignal(
                signal.nodeId(),
                signal.sessionId(),
                signal.diagnosis(),
                signal.actionType(),
                signal.recommendedNodeId(),
                signal.nodeCompleted(),
                signal.turnNo(),
                signal.eventAt(),
                signal.nextStepSuggestions());
    }

    private TutorSignal newerTutorSignal(TutorSignal left, TutorSignal right) {
        int timeCompare = compareDateTime(left.eventAt(), right.eventAt());
        if (timeCompare != 0) {
            return timeCompare >= 0 ? left : right;
        }
        return defaultInt(left.turnNo(), 0) >= defaultInt(right.turnNo(), 0) ? left : right;
    }

    private CompletedPracticeSignal toCompletedPracticeSignal(LearningEventReadService.PracticeAttemptEventProjection signal) {
        if (signal == null || signal.attemptId() == null || !signal.completed()) {
            return null;
        }
        return new CompletedPracticeSignal(
                signal.attemptId(),
                signal.nodeId(),
                CharSequenceUtil.blankToDefault(signal.rating(), "stretch"),
                signal.taskType(),
                signal.evidenceKind(),
                signal.minutes() == null ? DEFAULT_PRACTICE_MINUTES : capMinutes(signal.minutes(), DEFAULT_PRACTICE_MINUTES, 20),
                signal.eventAt());
    }

    private CompletedPracticeSignal newerCompletedPracticeSignal(CompletedPracticeSignal left,
                                                                 CompletedPracticeSignal right) {
        return compareDateTime(left.updatedAt(), right.updatedAt()) >= 0 ? left : right;
    }

    private LocalDateTime resolveReviewAttemptAt(ReviewAttemptEntity attempt) {
        if (attempt == null) {
            return null;
        }
        if (attempt.getUpdatedAt() != null) {
            return attempt.getUpdatedAt();
        }
        if (attempt.getCreatedAt() != null) {
            return attempt.getCreatedAt();
        }
        return null;
    }

    private Map<Long, ReviewSignal> buildReviewSignals(List<ReviewAttemptEntity> reviewAttempts) {
        return CollUtil.emptyIfNull(reviewAttempts).stream()
                .map(attempt -> new ReviewSignal(
                        attempt.getId(),
                        attempt.getMapNodeId(),
                        CharSequenceUtil.blankToDefault(attempt.getSelfRating(), "wobbly"),
                        attempt.getCompleted() != null && attempt.getCompleted() == 1,
                        attempt.getCompleted() != null && attempt.getCompleted() == 1 ? DEFAULT_REVIEW_MINUTES : 6,
                        attempt.getScheduledDueAt(),
                        attempt.getIntervalDays(),
                        attempt.getMasteryScoreAtAttempt(),
                        resolveReviewAttemptAt(attempt)))
                .filter(signal -> signal.attemptId() != null)
                .collect(Collectors.toMap(
                        ReviewSignal::attemptId,
                        Function.identity(),
                        this::newerReviewSignal));
    }

    private ReviewSignal toReviewSignal(LearningEventReadService.ReviewAttemptEventProjection signal) {
        if (signal == null || signal.attemptId() == null) {
            return null;
        }
        return new ReviewSignal(
                signal.attemptId(),
                signal.nodeId(),
                CharSequenceUtil.blankToDefault(signal.rating(), "wobbly"),
                signal.completed(),
                signal.completed() ? capMinutes(signal.minutes(), DEFAULT_REVIEW_MINUTES, 18) : 6,
                signal.scheduledDueAt(),
                signal.intervalDays(),
                signal.masteryScoreAtAttempt(),
                signal.eventAt());
    }

    private ReviewSignal newerReviewSignal(ReviewSignal left, ReviewSignal right) {
        return compareDateTime(left.updatedAt(), right.updatedAt()) >= 0 ? left : right;
    }

    private Map<Long, ReviewSignal> latestCompletedReviewByNodeId(List<ReviewSignal> signals) {
        return CollUtil.emptyIfNull(signals).stream()
                .filter(ReviewSignal::completed)
                .filter(signal -> signal.nodeId() != null)
                .collect(Collectors.toMap(
                        ReviewSignal::nodeId,
                        Function.identity(),
                        this::newerReviewSignal));
    }

    private List<LearningMapNodeEntity> buildPrioritizedReviewNodes(List<LearningMapNodeEntity> reviewingNodes,
                                                                    Map<Long, ReviewSignal> latestCompletedReviewByNodeId,
                                                                    Map<Long, LearningMapNodeEntity> nodeById) {
        return CollUtil.emptyIfNull(reviewingNodes).stream()
                .filter(Objects::nonNull)
                .map(node -> node.getId() == null ? node : nodeById.getOrDefault(node.getId(), node))
                .sorted((left, right) -> {
                    ReviewSignal leftSignal = left.getId() == null ? null : latestCompletedReviewByNodeId.get(left.getId());
                    ReviewSignal rightSignal = right.getId() == null ? null : latestCompletedReviewByNodeId.get(right.getId());
                    int priorityCompare = Integer.compare(
                            reviewPriorityForSignal(rightSignal, 0),
                            reviewPriorityForSignal(leftSignal, 0));
                    if (priorityCompare != 0) {
                        return priorityCompare;
                    }

                    LocalDateTime leftDueAt = leftSignal == null ? null : leftSignal.scheduledDueAt();
                    LocalDateTime rightDueAt = rightSignal == null ? null : rightSignal.scheduledDueAt();
                    if (leftDueAt != null && rightDueAt != null) {
                        int dueCompare = compareDateTime(leftDueAt, rightDueAt);
                        if (dueCompare != 0) {
                            return dueCompare;
                        }
                    } else if (leftDueAt != null || rightDueAt != null) {
                        return leftDueAt == null ? 1 : -1;
                    }

                    LocalDateTime leftUpdatedAt = leftSignal == null ? null : leftSignal.updatedAt();
                    LocalDateTime rightUpdatedAt = rightSignal == null ? null : rightSignal.updatedAt();
                    int updatedCompare = compareDateTime(rightUpdatedAt, leftUpdatedAt);
                    if (updatedCompare != 0) {
                        return updatedCompare;
                    }
                    return Integer.compare(
                            defaultInt(left.getSortOrder(), Integer.MAX_VALUE),
                            defaultInt(right.getSortOrder(), Integer.MAX_VALUE));
                })
                .toList();
    }

    private int countReviewSignals(List<ReviewSignal> signals, String rating) {
        return (int) CollUtil.emptyIfNull(signals).stream()
                .filter(ReviewSignal::completed)
                .filter(signal -> CharSequenceUtil.equals(signal.rating(), rating))
                .count();
    }

    private String buildReviewTaskTitle(LearningMapNodeEntity node, ReviewSignal latestReviewSignal) {
        if (latestReviewSignal != null && CharSequenceUtil.equals(latestReviewSignal.rating(), "forgotten")) {
            return "先回收「" + node.getTitle() + "」这个遗忘点";
        }
        if (latestReviewSignal != null && CharSequenceUtil.equals(latestReviewSignal.rating(), "wobbly")) {
            return "稳住「" + node.getTitle() + "」这轮复盘";
        }
        if (latestReviewSignal != null && CharSequenceUtil.equals(latestReviewSignal.rating(), "solid")) {
            return "短检查「" + node.getTitle() + "」别让它回落";
        }
        return "安排一轮「" + node.getTitle() + "」复盘";
    }

    private String buildReviewTaskSummary(LearningMapNodeEntity node, ReviewSignal latestReviewSignal) {
        if (latestReviewSignal != null && CharSequenceUtil.equals(latestReviewSignal.rating(), "forgotten")) {
            return "别先重读，先不看资料回忆「" + node.getTitle() + "」的作用、边界和最容易断掉的那一步。";
        }
        if (latestReviewSignal != null && CharSequenceUtil.equals(latestReviewSignal.rating(), "wobbly")) {
            return "这次重点不是全量重学，而是把刚想起来但还不稳的部分重新说顺、重新连上。";
        }
        if (latestReviewSignal != null && CharSequenceUtil.equals(latestReviewSignal.rating(), "solid")) {
            return "刚稳住的内容最适合做一次短检查，确认它没有立刻回落成“我记得好像学过”。";
        }
        return "用一轮主动回想把这个节点从“学过”推进到“还能调出来、还能解释清楚”。";
    }

    private String buildReviewTaskReason(LearningMapNodeEntity node, ReviewSignal latestReviewSignal) {
        if (latestReviewSignal != null && CharSequenceUtil.equals(latestReviewSignal.rating(), "forgotten")) {
            return "最近一次复盘已经明确标记为遗忘，今天优先回收它，比继续开新内容更值。";
        }
        if (latestReviewSignal != null && CharSequenceUtil.equals(latestReviewSignal.rating(), "wobbly")) {
            return "最近一次复盘还不稳，趁记忆还在边缘时补一轮，成本最低。";
        }
        if (latestReviewSignal != null && CharSequenceUtil.equals(latestReviewSignal.rating(), "solid")) {
            return "这个节点刚在复盘里稳住，短检查能把它继续往长期记忆推一步。";
        }
        return CharSequenceUtil.blankToDefault(node.getWhyItMatters(), "节点已经进入复盘窗口，需要尽快建立第一条稳定的复盘记录。");
    }

    private int reviewPriorityForSignal(ReviewSignal latestReviewSignal, int index) {
        int basePriority;
        if (latestReviewSignal != null && CharSequenceUtil.equals(latestReviewSignal.rating(), "forgotten")) {
            basePriority = 95;
        } else if (latestReviewSignal != null && CharSequenceUtil.equals(latestReviewSignal.rating(), "wobbly")) {
            basePriority = 88;
        } else if (latestReviewSignal != null && CharSequenceUtil.equals(latestReviewSignal.rating(), "solid")) {
            basePriority = 74;
        } else {
            basePriority = 82;
        }
        return Math.max(60, basePriority - Math.max(0, index) * 2);
    }

    private Map<Long, ReflectionSignal> buildReflectionSignals(List<DailyDigestEntity> dailyDigests) {
        return CollUtil.emptyIfNull(dailyDigests).stream()
                .filter(digest -> digest.getReflectionEntryId() != null)
                .map(this::toReflectionSignal)
                .collect(Collectors.toMap(
                        ReflectionSignal::reflectionEntryId,
                        Function.identity(),
                        this::newerReflectionSignal));
    }

    private ReflectionSignal toReflectionSignal(DailyDigestEntity digest) {
        if (digest == null || digest.getReflectionEntryId() == null) {
            return null;
        }
        return new ReflectionSignal(
                digest.getReflectionEntryId(),
                digest.getSummary(),
                digest.getNextAction(),
                digest.getDigestDate(),
                digest.getCreatedAt() == null ? digest.getUpdatedAt() : digest.getCreatedAt());
    }

    private ReflectionSignal toReflectionSignal(LearningEventReadService.ReflectionEventProjection signal) {
        if (signal == null || signal.reflectionEntryId() == null) {
            return null;
        }
        return new ReflectionSignal(
                signal.reflectionEntryId(),
                signal.summary(),
                signal.nextAction(),
                signal.reflectionDate(),
                signal.eventAt());
    }

    private ReflectionSignal newerReflectionSignal(ReflectionSignal left, ReflectionSignal right) {
        return compareDateTime(left.eventAt(), right.eventAt()) >= 0 ? left : right;
    }

    private List<String> nextStepSuggestionsFromJson(String json) {
        if (CharSequenceUtil.isBlank(json)) {
            return Collections.emptyList();
        }
        try {
            return cn.hutool.json.JSONUtil.toList(cn.hutool.json.JSONUtil.parseArray(json), String.class);
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    private int defaultInt(Integer value, int fallback) {
        return value == null ? fallback : value;
    }

    private Long requireUserId() {
        Long userId = UserContextHolder.getUserId();
        LearningErrorEnum.USER_NOT_LOGGED_IN.assertNotNull(userId);
        return userId;
    }

    private record PlanningReplayData(Long userId,
                                      LearningGoalEntity goal,
                                      LearningMapNodeEntity currentNode,
                                      List<LearningMapNodeEntity> nodes,
                                      Map<Long, LearningMapNodeEntity> nodeById,
                                      Map<Long, LearningNodeProgressEntity> progressByNodeId,
                                      List<TutorTurnEntity> tutorTurns,
                                      List<PracticeTaskEntity> practiceTasks,
                                      Map<Long, PracticeTaskEntity> practiceTaskById,
                                      List<PracticeAttemptEntity> practiceAttempts,
                                      List<ReviewAttemptEntity> reviewAttempts,
                                      List<GrowthSnapshotEntity> growthSnapshots,
                                      List<DailyDigestEntity> dailyDigests,
                                      List<LearningEventEntity> events) {
    }

    private record PlanningContext(Long userId,
                                   LearningGoalEntity goal,
                                   LearningMapNodeEntity currentNode,
                                   List<LearningMapNodeEntity> nodes,
                                   Map<Long, LearningMapNodeEntity> nodeById,
                                   Map<Long, LearningNodeProgressEntity> progressByNodeId,
                                   Map<Long, TutorSignal> latestTurnByNodeId,
                                   Map<Long, TutorSignal> latestTurnBySessionId,
                                   List<CompletedPracticeSignal> completedPracticeSignals,
                                   List<ReviewSignal> reviewSignals,
                                   List<GrowthSnapshotEntity> growthSnapshots,
                                   List<ReflectionSignal> reflectionSignals) {
    }

    private record TutorSignal(Long nodeId,
                               Long sessionId,
                               String diagnosis,
                               String actionType,
                               Long recommendedNodeId,
                               boolean nodeCompleted,
                               Integer turnNo,
                               LocalDateTime eventAt,
                               List<String> nextStepSuggestions) {
    }

    private record CompletedPracticeSignal(Long attemptId,
                                           Long nodeId,
                                           String rating,
                                           String taskType,
                                           String evidenceKind,
                                           Integer minutes,
                                           LocalDateTime updatedAt) {
    }

    private record ReviewSignal(Long attemptId,
                                Long nodeId,
                                String rating,
                                boolean completed,
                                Integer minutes,
                                LocalDateTime scheduledDueAt,
                                Integer intervalDays,
                                Integer masteryScoreAtAttempt,
                                LocalDateTime updatedAt) {
    }

    private record ReflectionSignal(Long reflectionEntryId,
                                    String summary,
                                    String nextAction,
                                    LocalDate reflectionDate,
                                    LocalDateTime eventAt) {
    }

    private record GrowthHighlights(List<String> keyCognitiveChanges,
                                    List<String> commonStickingPoints) {
    }

    private record ActivitySummary(Integer loggedMinutes,
                                   Integer activeDays,
                                   Integer missedDays,
                                   Integer completionPercent,
                                   Integer weeklyTargetMinutes,
                                   boolean todayDone,
                                   boolean reflectedToday,
                                   Integer missingMinutes) {
    }

    private record CompletionForecast(String completionStatus,
                                      String completionTitle,
                                      String completionSummary,
                                      Integer completionConfidence,
                                      Integer remainingMinutes,
                                      Integer expectedCompletionDays,
                                      Integer targetCompletionDays,
                                      Integer scheduleDeltaDays,
                                      Integer recoveryWindowDays,
                                      List<String> recoveryStrategy,
                                      String paceStatus) {
    }

    private record RecoveryModeMeta(boolean recoveryMode,
                                    String recoveryModeTitle) {
    }

    private record StageMeta(String stageLabel,
                             String milestoneTitle,
                             String milestoneSummary) {
    }
}
