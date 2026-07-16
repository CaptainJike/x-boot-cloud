package io.github.module.learning.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.framework.core.context.UserContextHolder;
import io.github.module.learning.entity.DailyDigestEntity;
import io.github.module.learning.entity.GrowthSnapshotEntity;
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
import io.github.module.learning.model.response.GrowthTimelineBO;
import io.github.module.learning.model.response.KnowledgeGraphEdgeBO;
import io.github.module.learning.model.response.KnowledgeGraphEvidenceBO;
import io.github.module.learning.model.response.KnowledgeGraphNodeBO;
import io.github.module.learning.model.response.LearnerMemoryBO;
import io.github.module.learning.model.response.LearningKnowledgeGraphBO;
import io.github.module.learning.model.response.LearningRhythmBO;
import io.github.module.learning.model.response.LearningRhythmDayBO;
import io.github.module.learning.model.response.MasteryRecordBO;
import io.github.module.learning.model.response.WeeklyPlanBucketBO;
import io.github.module.learning.mapper.PracticeAttemptMapper;
import io.github.module.learning.mapper.PracticeTaskMapper;
import io.github.module.learning.mapper.ReviewAttemptMapper;
import io.github.module.learning.mapper.TutorTurnMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 成长分析服务.
 */
@RequiredArgsConstructor
@Service
public class LearningGrowthService {

    private static final int DEFAULT_WEEKLY_MINUTES = 60;
    private static final int DEFAULT_TUTOR_MINUTES = 15;
    private static final int DEFAULT_PRACTICE_MINUTES = 15;
    private static final int DEFAULT_REVIEW_MINUTES = 15;
    private static final int DEFAULT_REFLECTION_MINUTES = 10;
    private static final int SERVER_ACTIVITY_WINDOW_DAYS = 7;

    private final GrowthSnapshotMapper growthSnapshotMapper;
    private final PracticeTaskMapper practiceTaskMapper;
    private final PracticeAttemptMapper practiceAttemptMapper;
    private final ReviewAttemptMapper reviewAttemptMapper;
    private final TutorTurnMapper tutorTurnMapper;
    private final DailyDigestMapper dailyDigestMapper;
    private final LearningGoalService learningGoalService;
    private final LearningEventReadService learningEventReadService;
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
        List<io.github.module.learning.entity.LearningEventEntity> events = learningEventReadService.loadUserEvents(userId);
        List<io.github.module.learning.model.response.GrowthTimelineItemBO> items = new ArrayList<>();
        items.addAll(CollUtil.emptyIfNull(events).stream().map(learningAssembler::toGrowthTimelineItemBO).toList());
        items.addAll(CollUtil.emptyIfNull(snapshots).stream().map(learningAssembler::toGrowthTimelineItemBO).toList());
        return learningAssembler.toGrowthTimelineBO(items);
    }

    public List<MasteryRecordBO> getMasteryRecords(Long goalId) {
        GoalLearningContext context = loadGoalContext(goalId);
        return buildMasteryRecords(context);
    }

    public LearnerMemoryBO getLearnerMemory(Long goalId) {
        GoalLearningContext context = loadGoalContext(goalId);
        List<PracticeSignal> completedPracticeSignals = context.practiceSignals().stream()
                .filter(PracticeSignal::completed)
                .toList();
        List<ReviewSignal> completedReviewSignals = context.reviewSignals().stream()
                .filter(ReviewSignal::completed)
                .toList();
        List<TutorSignal> latestSessionTurns = context.latestTutorBySessionId().values().stream()
                .sorted((left, right) -> compareUpdatedAt(right.updatedAt(), left.updatedAt()))
                .toList();
        GrowthHighlights growthHighlights = buildGrowthHighlights(context.growthSnapshots());

        long completedNodeCount = context.progressByNodeId().values().stream()
                .filter(progress -> CharSequenceUtil.equals(progress.getStatus(), "COMPLETED"))
                .count();
        long reviewingNodeCount = context.progressByNodeId().values().stream()
                .filter(progress -> CharSequenceUtil.equals(progress.getStatus(), "REVIEWING"))
                .count();

        int misconceptionCount = (int) latestSessionTurns.stream()
                .filter(turn -> CharSequenceUtil.equals(turn.diagnosis(), "misconception"))
                .count();
        int prereqCount = (int) latestSessionTurns.stream()
                .filter(turn -> CharSequenceUtil.equals(turn.diagnosis(), "needs_prereq"))
                .count();
        int clearPracticeCount = countPracticeSignals(completedPracticeSignals, "clear", false);
        int stretchPracticeCount = countPracticeSignals(completedPracticeSignals, "stretch", false);
        int stuckPracticeCount = countPracticeSignals(completedPracticeSignals, "stuck", false);
        int handoffCompletedCount = (int) completedPracticeSignals.stream()
                .filter(PracticeSignal::handoffValidation)
                .count();
        int handoffClearCount = countPracticeSignals(completedPracticeSignals, "clear", true);
        int handoffStuckCount = countPracticeSignals(completedPracticeSignals, "stuck", true);
        int solidReviewCount = countReviewSignals(completedReviewSignals, "solid");
        int wobblyReviewCount = countReviewSignals(completedReviewSignals, "wobbly");
        int forgottenReviewCount = countReviewSignals(completedReviewSignals, "forgotten");
        ReviewSignal latestReview = latestCompletedReviewSignal(completedReviewSignals);

        boolean reflectedToday = context.reflectionSignals().stream()
                .anyMatch(item -> Objects.equals(item.reflectionDate(), LocalDate.now()));
        ReflectionSignal latestDigest = context.reflectionSignals().stream()
                .sorted((left, right) -> {
                    int dateCompare = compareDate(right.reflectionDate(), left.reflectionDate());
                    if (dateCompare != 0) {
                        return dateCompare;
                    }
                    return compareUpdatedAt(right.updatedAt(), left.updatedAt());
                })
                .findFirst()
                .orElse(null);
        String currentNodeTitle = context.currentNode() == null ? null : context.currentNode().getTitle();

        List<String> strengths = new ArrayList<>();
        appendIfNotBlank(strengths,
                handoffClearCount > 0
                        ? "目标重构后的验证练习已经出现正向信号，说明新版本目标开始接住真实执行。"
                        : null);
        appendIfNotBlank(strengths,
                completedNodeCount > 0
                        ? "已经稳定完成 " + completedNodeCount + " 个学习节点，说明学习不是只停留在浏览阶段。"
                        : null);
        appendIfNotBlank(strengths,
                clearPracticeCount > 0
                        ? "已经有 " + clearPracticeCount + " 个练习形成可复核的清晰输出。"
                        : null);
        appendIfNotBlank(strengths,
                solidReviewCount > 0
                        ? "已经有 " + solidReviewCount + " 个复盘节点重新稳住，说明记忆正在开始变厚。"
                        : null);
        appendIfNotBlank(strengths, reflectedToday ? "今天已经完成 Reflection，学习结果开始稳定写回系统。" : null);
        appendIfNotBlank(strengths,
                growthHighlights.keyCognitiveChanges().isEmpty()
                        ? null
                        : "成长时间线已记录到认知变化：" + growthHighlights.keyCognitiveChanges().getFirst());

        List<String> weakSignals = new ArrayList<>();
        appendIfNotBlank(weakSignals,
                handoffStuckCount > 0
                        ? "目标重构后的验证练习仍有 " + handoffStuckCount + " 个任务卡住，说明这版目标还没有真正改善执行质量。"
                        : null);
        appendIfNotBlank(weakSignals,
                stuckPracticeCount > 0
                        ? "当前仍有 " + stuckPracticeCount + " 个练习任务卡住，说明理解还没有转成稳定应用。"
                        : null);
        appendIfNotBlank(weakSignals,
                forgottenReviewCount > 0
                        ? "最近已有 " + forgottenReviewCount + " 个复盘节点直接暴露遗忘，说明学过的内容还没有真正沉淀住。"
                        : null);
        appendIfNotBlank(weakSignals,
                prereqCount > misconceptionCount && prereqCount > 0
                        ? "最近更常见的问题是前置缺口，系统需要更频繁地先补基础再推进。"
                        : null);
        appendIfNotBlank(weakSignals,
                misconceptionCount >= prereqCount && misconceptionCount > 0
                        ? "最近更常见的问题是概念边界混淆，需要更多解释和纠偏型练习。"
                        : null);
        appendIfNotBlank(weakSignals, reflectedToday ? null : "今天还没有完成 Reflection，容易出现“推进过但没有沉淀”的情况。");
        appendIfNotBlank(weakSignals,
                growthHighlights.commonStickingPoints().isEmpty()
                        ? null
                        : "重复卡点仍然存在：" + growthHighlights.commonStickingPoints().getFirst());

        List<String> habits = new ArrayList<>();
        appendIfNotBlank(habits,
                latestSessionTurns.isEmpty() ? null : "你更适合先用 Tutor 暴露理解边界，再进入练习，而不是先刷资料。");
        appendIfNotBlank(habits,
                stretchPracticeCount > clearPracticeCount
                        ? "当前更适合短频练习和快速复述，因为大多数时候是“能做但还不稳”。"
                        : null);
        appendIfNotBlank(habits,
                reviewingNodeCount > 0
                        ? "系统已经检测到复习窗口，复盘应该成为日常学习动作的一部分。"
                        : null);
        appendIfNotBlank(habits,
                !completedReviewSignals.isEmpty()
                        ? "你更适合把复盘当成主动暴露遗忘点的动作，而不是等感觉快忘了再回看。"
                        : null);
        appendIfNotBlank(habits,
                CharSequenceUtil.isBlank(currentNodeTitle) ? null : "最近的学习重心比较集中在「" + currentNodeTitle + "」，保持单线推进更有效。");
        appendIfNotBlank(habits, reflectedToday ? "你更适合每天完成一个最小闭环，而不是等到有整块时间才开始学习。" : null);

        List<String> recommendedAdjustments = new ArrayList<>();
        appendIfNotBlank(recommendedAdjustments,
                handoffStuckCount > 0
                        ? "先回看这轮目标重构最初想解决的问题，再围绕卡住的验证练习重写下一步动作。"
                        : null);
        appendIfNotBlank(recommendedAdjustments,
                stuckPracticeCount > 0
                        ? "把卡住的练习优先写进 Reflection，下一轮计划才能更准确地安排回补任务。"
                        : null);
        appendIfNotBlank(recommendedAdjustments,
                forgottenReviewCount > 0
                        ? "下一轮计划先回收这些已经忘掉的节点，别急着继续开新内容。"
                        : null);
        appendIfNotBlank(recommendedAdjustments,
                prereqCount > misconceptionCount && prereqCount > 0
                        ? "接下来几轮学习更适合增加“前置梳理”类型任务，而不是继续加新节点。"
                        : null);
        appendIfNotBlank(recommendedAdjustments,
                misconceptionCount >= prereqCount && misconceptionCount > 0
                        ? "继续强化“解释给昨天的自己听”这类练习，优先修正边界理解。"
                        : null);
        appendIfNotBlank(recommendedAdjustments, reflectedToday ? null : "今天结束前至少补一条 Reflection，避免学习记忆只停留在临时状态。");
        appendIfNotBlank(recommendedAdjustments,
                reviewingNodeCount > 0
                        ? "把复盘节点真正排进今天或明天的计划，而不是只停留在地图状态里。"
                        : null);
        appendIfNotBlank(recommendedAdjustments, latestDigest == null ? null : latestDigest.nextAction());

        String goalValidationStatus = handoffClearCount > 0
                ? "improving"
                : handoffStuckCount > 0
                ? "at_risk"
                : handoffCompletedCount > 0
                ? "uncertain"
                : "none";
        String goalValidationSummary = handoffClearCount > 0
                ? "这次目标重构已经开始改善真实执行，系统看到了明确的验证练习正向信号。"
                : handoffStuckCount > 0
                ? "这次目标重构还没有真正接住执行，验证练习里已经暴露出新的阻塞。"
                : handoffCompletedCount > 0
                ? "系统已经开始观察这次目标重构的真实效果，但还需要更多练习和反思证据。"
                : null;

        String masterySignal = handoffClearCount > 0
                ? "目标重构后的验证练习已经给出正向信号，说明这次调整开始改善真实执行。"
                : handoffStuckCount > 0
                ? "目标重构后的验证练习仍然偏脆弱，说明这次调整还没有完全转成执行改善。"
                : forgottenReviewCount > 0
                ? "最近复盘里已经出现真实遗忘点，说明系统需要优先回收已学内容，而不是继续只往前推。"
                : solidReviewCount > 0
                ? "最近复盘里已经有节点重新稳住，说明学习结果开始从“练过”进入“记住了”的阶段。"
                : stuckPracticeCount > 0
                ? "当前掌握度仍偏脆弱，说明已经开始练，但还需要更多结构化输出。"
                : clearPracticeCount > 0
                ? "当前掌握度开始变得可见，已经不只是“我感觉懂了”，而是有一部分能稳定输出。"
                : "当前掌握度信号仍偏早期，最好尽快把 Tutor 结果转成练习证据。";

        String momentumTitle = forgottenReviewCount > 0
                ? "记忆开始暴露真实断点"
                : !reflectedToday && completedPracticeSignals.isEmpty()
                ? "学习记忆还很浅"
                : stuckPracticeCount > 0
                ? "正在从理解走向掌握"
                : "开始形成可复用的学习模式";

        String momentumSummary = handoffClearCount > 0
                ? "系统已经看到目标重构后的第一轮验证开始跑通，这次调整不再只是改说法，而是在改善真实学习动作。"
                : handoffStuckCount > 0
                ? "系统已经看到目标重构后的第一轮验证仍然卡住，说明这次调整还需要继续压缩目标或重写起步动作。"
                : forgottenReviewCount > 0
                ? "系统已经开始看到哪些内容不是“没学过”，而是“学过后正在忘”。这会直接改变后续计划和复盘顺序。"
                : solidReviewCount > 0
                ? "练习之外，已经有一部分内容在复盘里重新稳住，说明长期记忆开始真的被建立起来。"
                : !reflectedToday && completedPracticeSignals.isEmpty()
                ? "系统已经知道你在学什么，但还缺足够多的练习与反思证据来形成稳定长期记忆。"
                : stuckPracticeCount > 0
                ? "你已经开始做练习并暴露真实卡点，下一步是把这些卡点稳定写回计划和反思。"
                : "从 Tutor 到 Practice 再到 Reflection 的闭环正在成型，系统开始能更具体地理解你的学习方式。";

        String summary = CharSequenceUtil.isNotBlank(currentNodeTitle)
                ? "Memory 目前主要围绕「" + currentNodeTitle + "」形成。系统开始能区分你是前置不足、概念误解，还是已经进入能练但不稳的阶段。"
                + (growthHighlights.commonStickingPoints().isEmpty()
                ? ""
                : " 最近重复出现的卡点是「" + growthHighlights.commonStickingPoints().getFirst() + "」。")
                + (handoffCompletedCount > 0 ? " 同时，系统也在观察这次目标重构后的验证练习有没有真正改善执行。" : "")
                + (forgottenReviewCount > 0 ? " 复盘里已经开始出现真实遗忘点，系统会据此更主动地安排回收顺序。" : "")
                : "系统已经开始根据 Tutor、Practice、Review、Reflection 和成长记录形成对你的长期学习判断。";

        return LearnerMemoryBO.builder()
                .generatedAt(LocalDateTime.now())
                .mode("server")
                .summary(summary)
                .momentumTitle(momentumTitle)
                .momentumSummary(momentumSummary)
                .masterySignal(masterySignal)
                .goalValidationStatus(goalValidationStatus)
                .goalValidationSummary(goalValidationSummary)
                .strengths(uniqueList(strengths, 4))
                .relationStrengths(Collections.emptyList())
                .weakSignals(uniqueList(weakSignals, 5))
                .relationWatchouts(Collections.emptyList())
                .habits(uniqueList(habits, 5))
                .recommendedAdjustments(uniqueList(recommendedAdjustments, 6))
                .evidence(buildMemoryEvidence(
                        misconceptionCount,
                        prereqCount,
                        completedPracticeSignals.size(),
                        stuckPracticeCount,
                        completedReviewSignals.size(),
                        solidReviewCount,
                        forgottenReviewCount,
                        handoffCompletedCount,
                        handoffClearCount,
                        handoffStuckCount,
                        reflectedToday,
                        latestDigest,
                        currentNodeTitle,
                        growthHighlights,
                        latestReview))
                .build();
    }

    public LearningKnowledgeGraphBO getLearningKnowledgeGraph(Long goalId) {
        GoalLearningContext context = loadGoalContext(goalId);
        return buildLearningKnowledgeGraph(context);
    }

    public LearningRhythmBO getLearningRhythm(Long goalId) {
        GoalLearningContext context = loadGoalContext(goalId);
        return buildLearningRhythm(context);
    }

    private GoalLearningContext loadGoalContext(Long goalId) {
        Long userId = requireUserId();
        LearningGoalEntity goal = learningGoalService.getOwnedGoalById(goalId, userId);
        LearningErrorEnum.INVALID_GOAL.assertNotNull(goal);

        List<LearningMapNodeEntity> nodes = learningGoalService.listNodesByGoalId(goalId, userId);
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
        Map<Long, PracticeTaskEntity> practiceTaskById = practiceTaskMapper.selectList(
                new QueryWrapper<PracticeTaskEntity>()
                        .lambda()
                        .eq(PracticeTaskEntity::getGoalId, goalId)
                        .eq(PracticeTaskEntity::getUserId, userId)
        ).stream().filter(item -> item.getId() != null)
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
        LearningEventReadService.GoalEventSnapshot eventSnapshot = learningEventReadService.loadGoalSnapshot(goalId, userId);

        Map<Long, TutorSignal> latestTutorByNodeId = new java.util.HashMap<>();
        latestTurnByNodeId(tutorTurns).forEach((nodeId, turn) -> latestTutorByNodeId.put(nodeId, toTutorSignal(turn)));
        eventSnapshot.latestTutorByNodeId().forEach((nodeId, signal) ->
                latestTutorByNodeId.merge(nodeId, toTutorSignal(signal), this::newerTutorSignal));

        Map<Long, TutorSignal> latestTutorBySessionId = new java.util.HashMap<>();
        latestTurnBySessionId(tutorTurns).forEach((sessionId, turn) -> latestTutorBySessionId.put(sessionId, toTutorSignal(turn)));
        eventSnapshot.latestTutorBySessionId().forEach((sessionId, signal) ->
                latestTutorBySessionId.merge(sessionId, toTutorSignal(signal), this::newerTutorSignal));

        Map<Long, PracticeSignal> practiceSignalsByAttemptId = new HashMap<>(
                buildPracticeSignals(practiceAttempts, practiceTaskById));
        eventSnapshot.latestPracticeByAttemptId().values().stream()
                .map(this::toPracticeSignal)
                .filter(Objects::nonNull)
                .forEach(signal -> practiceSignalsByAttemptId.merge(
                        signal.attemptId(),
                        signal,
                        this::newerPracticeSignal));

        Map<Long, ReviewSignal> reviewSignalsByAttemptId = new HashMap<>(buildReviewSignals(reviewAttempts));
        eventSnapshot.latestReviewByAttemptId().values().stream()
                .map(this::toReviewSignal)
                .filter(Objects::nonNull)
                .forEach(signal -> reviewSignalsByAttemptId.merge(
                        signal.attemptId(),
                        signal,
                        this::newerReviewSignal));

        Map<Long, ReflectionSignal> reflectionSignalsByEntryId = new java.util.HashMap<>(buildReflectionSignals(dailyDigests));
        eventSnapshot.latestReflectionByEntryId().values().stream()
                .map(this::toReflectionSignal)
                .filter(Objects::nonNull)
                .forEach(signal -> reflectionSignalsByEntryId.merge(
                        signal.reflectionEntryId(),
                        signal,
                        this::newerReflectionSignal));

        return new GoalLearningContext(
                userId,
                goal,
                learningGoalService.getCurrentNode(goal),
                nodes,
                progressByNodeId,
                latestTutorByNodeId,
                latestTutorBySessionId,
                new ArrayList<>(practiceSignalsByAttemptId.values()),
                new ArrayList<>(reviewSignalsByAttemptId.values()),
                growthSnapshots,
                new ArrayList<>(reflectionSignalsByEntryId.values())
        );
    }

    private List<MasteryRecordBO> buildMasteryRecords(GoalLearningContext context) {
        if (CollUtil.isEmpty(context.nodes())) {
            return Collections.emptyList();
        }

        Map<Long, List<PracticeSignal>> completedPracticeSignalsByNodeId = context.practiceSignals().stream()
                .filter(PracticeSignal::completed)
                .collect(Collectors.groupingBy(PracticeSignal::nodeId));
        Map<Long, List<ReviewSignal>> completedReviewSignalsByNodeId = context.reviewSignals().stream()
                .filter(ReviewSignal::completed)
                .collect(Collectors.groupingBy(ReviewSignal::nodeId));

        return context.nodes().stream()
                .sorted((left, right) -> Integer.compare(left.getSortOrder(), right.getSortOrder()))
                .map(node -> buildMasteryRecord(
                        node,
                        context.progressByNodeId().get(node.getId()),
                        context.latestTutorByNodeId().get(node.getId()),
                        completedPracticeSignalsByNodeId.getOrDefault(node.getId(), Collections.emptyList()),
                        completedReviewSignalsByNodeId.getOrDefault(node.getId(), Collections.emptyList())))
                .toList();
    }

    private MasteryRecordBO buildMasteryRecord(LearningMapNodeEntity node,
                                               LearningNodeProgressEntity progress,
                                               TutorSignal latestTutorTurn,
                                               List<PracticeSignal> practiceSignals,
                                               List<ReviewSignal> reviewSignals) {
        String progressStatus = progress == null ? "PENDING" : CharSequenceUtil.blankToDefault(progress.getStatus(), "PENDING");
        String latestDiagnosis = latestTutorTurn != null
                ? latestTutorTurn.diagnosis()
                : progress == null ? null : progress.getLastDiagnosis();
        int clearPracticeCount = countPracticeSignals(practiceSignals, "clear", false);
        int stretchPracticeCount = countPracticeSignals(practiceSignals, "stretch", false);
        int stuckPracticeCount = countPracticeSignals(practiceSignals, "stuck", false);
        int clearExplainCount = countPracticeSignals(practiceSignals, "clear", "explanation", false);
        int stretchExplainCount = countPracticeSignals(practiceSignals, "stretch", "explanation", false);
        int stuckExplainCount = countPracticeSignals(practiceSignals, "stuck", "explanation", false);
        int clearApplyCount = countPracticeSignalsByKinds(practiceSignals, "clear", Set.of("application", "goal_validation"), false);
        int stretchApplyCount = countPracticeSignalsByKinds(practiceSignals, "stretch", Set.of("application", "goal_validation"), false);
        int stuckApplyCount = countPracticeSignalsByKinds(practiceSignals, "stuck", Set.of("application", "goal_validation"), false);
        int clearRepairCount = countPracticeSignals(practiceSignals, "clear", "prerequisite_repair", false);
        int stretchRepairCount = countPracticeSignals(practiceSignals, "stretch", "prerequisite_repair", false);
        int stuckRepairCount = countPracticeSignals(practiceSignals, "stuck", "prerequisite_repair", false);
        int handoffClearCount = countPracticeSignals(practiceSignals, "clear", true);
        int handoffStuckCount = countPracticeSignals(practiceSignals, "stuck", true);
        int solidReviewCount = countReviewSignals(reviewSignals, "solid");
        int wobblyReviewCount = countReviewSignals(reviewSignals, "wobbly");
        int forgottenReviewCount = countReviewSignals(reviewSignals, "forgotten");
        ReviewSignal latestReviewSignal = latestCompletedReviewSignal(reviewSignals);

        int baseScore = baseScoreFromProgressStatus(progressStatus);
        int score = progress != null && progress.getMasteryLevel() != null && progress.getMasteryLevel() > 0
                ? progress.getMasteryLevel()
                : baseScore;
        score += clearPracticeCount * 10;
        score += stretchPracticeCount * 4;
        score -= stuckPracticeCount * 10;
        score += clearExplainCount * 4;
        score += clearApplyCount * 8;
        score += clearRepairCount * 5;
        score -= stuckApplyCount * 4;
        score -= stuckRepairCount * 3;
        score += handoffClearCount * 6;
        score -= handoffStuckCount * 6;
        score += solidReviewCount * 8;
        score += wobblyReviewCount * 2;
        score -= forgottenReviewCount * 12;
        if (CharSequenceUtil.equals(latestDiagnosis, "ready")) {
            score += 6;
        }
        if (CharSequenceUtil.equals(latestDiagnosis, "misconception")) {
            score -= 8;
        }
        if (CharSequenceUtil.equals(latestDiagnosis, "needs_prereq")) {
            score -= 10;
        }
        if (latestReviewSignal != null && CharSequenceUtil.equals(latestReviewSignal.rating(), "solid")) {
            score += 4;
        }
        if (latestReviewSignal != null && CharSequenceUtil.equals(latestReviewSignal.rating(), "forgotten")) {
            score -= 6;
        }

        int masteryScore = clampScore(score);
        LocalDateTime updatedAt = resolveLatestUpdatedAt(progress, latestTutorTurn, practiceSignals, reviewSignals);

        return MasteryRecordBO.builder()
                .nodeId(node.getId())
                .nodeTitle(node.getTitle())
                .nodeCode(node.getNodeCode())
                .masteryScore(masteryScore)
                .masteryLevel(levelFromScore(masteryScore))
                .progressStatus(progressStatus)
                .strongestSignal(strongestSignalForNode(
                        clearPracticeCount,
                        stuckPracticeCount,
                        clearExplainCount,
                        clearApplyCount,
                        clearRepairCount,
                        handoffClearCount,
                        handoffStuckCount,
                        solidReviewCount,
                        forgottenReviewCount,
                        latestDiagnosis,
                        progressStatus))
                .handoffValidationSignal(handoffValidationSignal(handoffClearCount, handoffStuckCount))
                .explanationStatus(summarizePracticeDimension(
                        clearExplainCount,
                        stretchExplainCount,
                        stuckExplainCount,
                        "还缺解释型证据",
                        "解释已经比较清楚",
                        "能解释但还不稳",
                        "解释时仍会卡住"))
                .applicationStatus(summarizePracticeDimension(
                        clearApplyCount,
                        stretchApplyCount,
                        stuckApplyCount,
                        "还缺应用型证据",
                        "已经能放进真实场景",
                        "能用但还不稳定",
                        "应用时仍会卡住"))
                .prerequisiteStatus(summarizePracticeDimension(
                        clearRepairCount,
                        stretchRepairCount,
                        stuckRepairCount,
                        CharSequenceUtil.equals(latestDiagnosis, "needs_prereq") ? "前置缺口还没被修复" : "当前没有明显前置修复证据",
                        "前置缺口已经开始补上",
                        "前置在补，但还没完全稳",
                        "前置修复仍然卡住"))
                .reviewState(resolveReviewState(progressStatus, latestReviewSignal))
                .updatedAt(updatedAt)
                .build();
    }

    private Map<Long, PracticeSignal> buildPracticeSignals(List<PracticeAttemptEntity> attempts,
                                                           Map<Long, PracticeTaskEntity> taskById) {
        return CollUtil.emptyIfNull(attempts).stream()
                .map(attempt -> {
                    PracticeTaskEntity task = taskById.get(attempt.getPracticeTaskId());
                    String evidenceKind = task == null ? null : task.getEvidenceKind();
                    boolean completed = attempt.getCompleted() != null && attempt.getCompleted() == 1;
                    return new PracticeSignal(
                            attempt.getId(),
                            attempt.getMapNodeId(),
                            evidenceKind,
                            resolvePracticeRating(attempt),
                            attempt.getHandoffValidation() != null && attempt.getHandoffValidation() == 1,
                            completed,
                            completed ? capMinutes(task == null ? null : task.getEstimatedMinutes(), DEFAULT_PRACTICE_MINUTES, 20) : 8,
                            attempt.getUpdatedAt());
                })
                .filter(signal -> signal.attemptId() != null)
                .collect(Collectors.toMap(
                        PracticeSignal::attemptId,
                        Function.identity(),
                        this::newerPracticeSignal));
    }

    private Map<Long, ReviewSignal> buildReviewSignals(List<ReviewAttemptEntity> attempts) {
        return CollUtil.emptyIfNull(attempts).stream()
                .map(attempt -> new ReviewSignal(
                        attempt.getId(),
                        attempt.getMapNodeId(),
                        CharSequenceUtil.blankToDefault(attempt.getSelfRating(), "wobbly"),
                        attempt.getCompleted() != null && attempt.getCompleted() == 1,
                        attempt.getCompleted() != null && attempt.getCompleted() == 1 ? DEFAULT_REVIEW_MINUTES : 6,
                        attempt.getScheduledDueAt(),
                        attempt.getIntervalDays(),
                        attempt.getMasteryScoreAtAttempt(),
                        defaultIfNull(attempt.getUpdatedAt(), attempt.getCreatedAt())))
                .filter(signal -> signal.attemptId() != null)
                .collect(Collectors.toMap(
                        ReviewSignal::attemptId,
                        Function.identity(),
                        this::newerReviewSignal));
    }

    private Map<Long, TutorTurnEntity> latestTurnByNodeId(List<TutorTurnEntity> tutorTurns) {
        return CollUtil.emptyIfNull(tutorTurns).stream()
                .filter(turn -> turn.getMapNodeId() != null)
                .collect(Collectors.toMap(
                        TutorTurnEntity::getMapNodeId,
                        Function.identity(),
                        (left, right) -> compareUpdatedAt(left.getCreatedAt(), right.getCreatedAt()) >= 0 ? left : right));
    }

    private Map<Long, TutorTurnEntity> latestTurnBySessionId(List<TutorTurnEntity> tutorTurns) {
        return CollUtil.emptyIfNull(tutorTurns).stream()
                .filter(turn -> turn.getSessionId() != null)
                .collect(Collectors.toMap(
                        TutorTurnEntity::getSessionId,
                        Function.identity(),
                        (left, right) -> {
                            int turnCompare = Integer.compare(
                                    left.getTurnNo() == null ? 0 : left.getTurnNo(),
                                    right.getTurnNo() == null ? 0 : right.getTurnNo());
                            if (turnCompare != 0) {
                                return turnCompare >= 0 ? left : right;
                            }
                            return compareUpdatedAt(left.getCreatedAt(), right.getCreatedAt()) >= 0 ? left : right;
                        }));
    }

    private TutorSignal toTutorSignal(TutorTurnEntity turn) {
        if (turn == null) {
            return null;
        }
        return new TutorSignal(
                turn.getMapNodeId(),
                turn.getSessionId(),
                turn.getDiagnosis(),
                turn.getTurnNo(),
                turn.getCreatedAt());
    }

    private TutorSignal toTutorSignal(LearningEventReadService.TutorEventProjection signal) {
        if (signal == null) {
            return null;
        }
        return new TutorSignal(
                signal.nodeId(),
                signal.sessionId(),
                signal.diagnosis(),
                signal.turnNo(),
                signal.eventAt());
    }

    private TutorSignal newerTutorSignal(TutorSignal left, TutorSignal right) {
        int timeCompare = compareUpdatedAt(left.updatedAt(), right.updatedAt());
        if (timeCompare != 0) {
            return timeCompare >= 0 ? left : right;
        }
        return Integer.compare(left.turnNo() == null ? 0 : left.turnNo(), right.turnNo() == null ? 0 : right.turnNo()) >= 0
                ? left
                : right;
    }

    private PracticeSignal toPracticeSignal(LearningEventReadService.PracticeAttemptEventProjection signal) {
        if (signal == null || signal.attemptId() == null) {
            return null;
        }
        return new PracticeSignal(
                signal.attemptId(),
                signal.nodeId(),
                signal.evidenceKind(),
                CharSequenceUtil.blankToDefault(signal.rating(), "stretch"),
                signal.handoffValidation(),
                signal.completed(),
                signal.completed() ? capMinutes(signal.minutes(), DEFAULT_PRACTICE_MINUTES, 20) : 8,
                signal.eventAt());
    }

    private PracticeSignal newerPracticeSignal(PracticeSignal left, PracticeSignal right) {
        return compareUpdatedAt(left.updatedAt(), right.updatedAt()) >= 0 ? left : right;
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
        return compareUpdatedAt(left.updatedAt(), right.updatedAt()) >= 0 ? left : right;
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
        return compareUpdatedAt(left.updatedAt(), right.updatedAt()) >= 0 ? left : right;
    }

    private GrowthHighlights buildGrowthHighlights(List<GrowthSnapshotEntity> snapshots) {
        List<String> keyCognitiveChanges = CollUtil.emptyIfNull(snapshots).stream()
                .filter(item -> CharSequenceUtil.equals(item.getEventType(), "COGNITION"))
                .map(GrowthSnapshotEntity::getSummary)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .limit(4)
                .toList();
        List<String> commonStickingPoints = CollUtil.emptyIfNull(snapshots).stream()
                .filter(item -> CharSequenceUtil.equals(item.getEventType(), "STUCK"))
                .map(GrowthSnapshotEntity::getSummary)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .limit(4)
                .toList();
        return new GrowthHighlights(keyCognitiveChanges, commonStickingPoints);
    }

    private List<LearnerMemoryBO.EvidenceBO> buildMemoryEvidence(int misconceptionCount,
                                                                 int prereqCount,
                                                                 int completedPracticeCount,
                                                                 int stuckPracticeCount,
                                                                 int completedReviewCount,
                                                                 int solidReviewCount,
                                                                 int forgottenReviewCount,
                                                                 int handoffCompletedCount,
                                                                 int handoffClearCount,
                                                                 int handoffStuckCount,
                                                                 boolean reflectedToday,
                                                                 ReflectionSignal latestDigest,
                                                                 String currentNodeTitle,
                                                                 GrowthHighlights growthHighlights,
                                                                 ReviewSignal latestReview) {
        List<LearnerMemoryBO.EvidenceBO> evidence = new ArrayList<>();
        evidence.add(LearnerMemoryBO.EvidenceBO.builder()
                .label("Tutor")
                .detail("累计 " + misconceptionCount + " 次概念误解，" + prereqCount + " 次前置缺口判断。")
                .build());
        evidence.add(LearnerMemoryBO.EvidenceBO.builder()
                .label("Practice")
                .detail("已完成 " + completedPracticeCount + " 个练习任务，其中 " + stuckPracticeCount + " 个仍标记为“卡住”。")
                .build());
        if (completedReviewCount > 0) {
            evidence.add(LearnerMemoryBO.EvidenceBO.builder()
                    .label("Review")
                    .detail("已完成 " + completedReviewCount + " 次复盘，其中 " + solidReviewCount + " 次已经稳住，"
                            + forgottenReviewCount + " 次直接暴露遗忘。"
                            + (latestReview == null ? "" : " 最近一次结果是「" + humanizeReviewRating(latestReview.rating()) + "」。"))
                    .build());
        }
        if (handoffCompletedCount > 0) {
            evidence.add(LearnerMemoryBO.EvidenceBO.builder()
                    .label("Goal handoff")
                    .detail("目标重构后的验证练习已完成 " + handoffCompletedCount + " 个，其中 "
                            + handoffClearCount + " 个偏清楚，" + handoffStuckCount + " 个仍卡住。")
                    .build());
        }
        evidence.add(LearnerMemoryBO.EvidenceBO.builder()
                .label("Reflection")
                .detail(reflectedToday
                        ? CharSequenceUtil.blankToDefault(latestDigest == null ? null : latestDigest.summary(), "今天已经完成 Reflection。")
                        : "今天还没有完成 Reflection。")
                .build());
        if (CharSequenceUtil.isNotBlank(currentNodeTitle)) {
            evidence.add(LearnerMemoryBO.EvidenceBO.builder()
                    .label("Current focus")
                    .detail("当前系统主要围绕「" + currentNodeTitle + "」组织学习与练习。")
                    .build());
        }
        if (!growthHighlights.commonStickingPoints().isEmpty()) {
            evidence.add(LearnerMemoryBO.EvidenceBO.builder()
                    .label("Growth")
                    .detail("最近重复出现的卡点是「" + growthHighlights.commonStickingPoints().getFirst() + "」。")
                    .build());
        }
        return evidence;
    }

    private LearningKnowledgeGraphBO buildLearningKnowledgeGraph(GoalLearningContext context) {
        if (CollUtil.isEmpty(context.nodes())) {
            return null;
        }

        List<LearningMapNodeEntity> nodesBySortOrder = context.nodes().stream()
                .sorted((left, right) -> Integer.compare(
                        defaultInt(left.getSortOrder(), Integer.MAX_VALUE),
                        defaultInt(right.getSortOrder(), Integer.MAX_VALUE)))
                .toList();
        Long currentNodeId = context.currentNode() == null ? null : context.currentNode().getId();
        List<LearningMapNodeEntity> orderedNodes = nodesBySortOrder.stream()
                .sorted((left, right) -> {
                    if (Objects.equals(left.getId(), currentNodeId)) {
                        return -1;
                    }
                    if (Objects.equals(right.getId(), currentNodeId)) {
                        return 1;
                    }
                    return Integer.compare(
                            defaultInt(left.getSortOrder(), Integer.MAX_VALUE),
                            defaultInt(right.getSortOrder(), Integer.MAX_VALUE));
                })
                .toList();

        Map<Long, LearningMapNodeEntity> nodeById = context.nodes().stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(LearningMapNodeEntity::getId, Function.identity(), (left, right) -> left));
        Map<String, LearningMapNodeEntity> nodeByCode = context.nodes().stream()
                .filter(item -> CharSequenceUtil.isNotBlank(item.getNodeCode()))
                .collect(Collectors.toMap(LearningMapNodeEntity::getNodeCode, Function.identity(), (left, right) -> left));
        Map<String, List<String>> prerequisiteNodeCodesByCode = context.nodes().stream()
                .filter(item -> CharSequenceUtil.isNotBlank(item.getNodeCode()))
                .collect(Collectors.toMap(
                        LearningMapNodeEntity::getNodeCode,
                        item -> parseNodeCodes(item.getPrerequisiteNodeCodes()),
                        (left, right) -> left));
        Map<String, List<String>> unlocksByCode = buildUnlocksByCode(context.nodes(), prerequisiteNodeCodesByCode);
        Map<Long, MasteryRecordBO> masteryByNodeId = buildMasteryRecords(context).stream()
                .filter(item -> item.getNodeId() != null)
                .collect(Collectors.toMap(MasteryRecordBO::getNodeId, Function.identity(), (left, right) -> left));
        Map<Long, List<PracticeSignal>> completedPracticeSignalsByNodeId = context.practiceSignals().stream()
                .filter(PracticeSignal::completed)
                .collect(Collectors.groupingBy(PracticeSignal::nodeId));
        Map<Long, List<ReviewSignal>> completedReviewSignalsByNodeId = context.reviewSignals().stream()
                .filter(ReviewSignal::completed)
                .collect(Collectors.groupingBy(ReviewSignal::nodeId));

        List<KnowledgeGraphNodeBO> graphNodes = orderedNodes.stream()
                .map(node -> buildKnowledgeGraphNode(
                        context,
                        node,
                        prerequisiteNodeCodesByCode.getOrDefault(node.getNodeCode(), Collections.emptyList()),
                        unlocksByCode.getOrDefault(node.getNodeCode(), Collections.emptyList()),
                        masteryByNodeId.get(node.getId()),
                        completedPracticeSignalsByNodeId.getOrDefault(node.getId(), Collections.emptyList()),
                        completedReviewSignalsByNodeId.getOrDefault(node.getId(), Collections.emptyList()),
                        currentNodeId,
                        nodeByCode))
                .toList();
        Map<String, KnowledgeGraphNodeBO> graphNodeByCode = graphNodes.stream()
                .filter(item -> CharSequenceUtil.isNotBlank(item.getNodeCode()))
                .collect(Collectors.toMap(KnowledgeGraphNodeBO::getNodeCode, Function.identity(), (left, right) -> left));

        List<KnowledgeGraphEdgeBO> edges = buildKnowledgeGraphEdges(nodesBySortOrder, prerequisiteNodeCodesByCode, nodeByCode, graphNodeByCode);
        KnowledgeGraphNodeBO focusNode = graphNodes.stream()
                .filter(item -> Objects.equals(item.getNodeId(), currentNodeId))
                .findFirst()
                .orElseGet(() -> graphNodes.stream()
                        .filter(item -> CharSequenceUtil.equals(item.getProgressStatus(), "IN_PROGRESS"))
                        .findFirst()
                        .orElse(graphNodes.getFirst()));

        LocalDateTime generatedAt = LocalDateTime.now();
        List<KnowledgeGraphEvidenceBO> evidence = graphNodes.stream()
                .flatMap(graphNode -> buildKnowledgeEvidenceForNode(
                        nodeById.get(graphNode.getNodeId()),
                        graphNode,
                        context.latestTutorByNodeId().get(graphNode.getNodeId()),
                        completedPracticeSignalsByNodeId.getOrDefault(graphNode.getNodeId(), Collections.emptyList()),
                        completedReviewSignalsByNodeId.getOrDefault(graphNode.getNodeId(), Collections.emptyList()),
                        context.progressByNodeId().get(graphNode.getNodeId()),
                        Objects.equals(graphNode.getNodeId(), focusNode.getNodeId())
                                || CharSequenceUtil.equals(graphNode.getMasteryLevel(), "fragile")
                                || CharSequenceUtil.equals(graphNode.getProgressStatus(), "READY"),
                        generatedAt).stream())
                .toList();
        GrowthHighlights growthHighlights = buildGrowthHighlights(context.growthSnapshots());
        List<String> frontier = buildKnowledgeFrontier(graphNodes, focusNode);
        List<String> weakAreas = buildKnowledgeWeakAreas(graphNodes, focusNode, growthHighlights);
        List<String> weakPaths = edges.stream()
                .filter(edge -> CharSequenceUtil.equals(edge.getType(), "prerequisite"))
                .filter(edge -> !CharSequenceUtil.equals(edge.getStrength(), "stable"))
                .limit(4)
                .map(edge -> CharSequenceUtil.blankToDefault(
                        edge.getSummary(),
                        edge.getSourceCode() + " -> " + edge.getTargetCode() + " 这条关系还需要补证据。"))
                .toList();
        long prerequisiteCount = edges.stream()
                .filter(edge -> CharSequenceUtil.equals(edge.getType(), "prerequisite"))
                .count();

        return LearningKnowledgeGraphBO.builder()
                .generatedAt(generatedAt)
                .mode("server")
                .focusNodeId(focusNode.getNodeId())
                .focusNodeTitle(focusNode.getTitle())
                .summary("图谱已经把当前目标拆成 " + graphNodes.size() + " 个概念节点、" + prerequisiteCount
                        + " 条前置依赖。系统现在开始把概念关系、验证方式和真实学习证据放到同一层结构里。")
                .graphReason("Knowledge Graph 当前更重要的职责，不是堆更多资料，而是把概念、前置关系、验证方式和学习证据组织成同一张可行动的图。")
                .conceptCount(graphNodes.size())
                .relationCount(edges.size())
                .weakRelationCount((int) edges.stream()
                        .filter(edge -> CharSequenceUtil.isNotBlank(edge.getStrength()))
                        .filter(edge -> !CharSequenceUtil.equals(edge.getStrength(), "stable"))
                        .count())
                .nodes(graphNodes)
                .edges(edges)
                .evidence(evidence)
                .frontier(frontier)
                .weakAreas(weakAreas)
                .weakPaths(weakPaths)
                .build();
    }

    private KnowledgeGraphNodeBO buildKnowledgeGraphNode(GoalLearningContext context,
                                                         LearningMapNodeEntity node,
                                                         List<String> prerequisiteNodeCodes,
                                                         List<String> unlocksNodeCodes,
                                                         MasteryRecordBO masteryRecord,
                                                         List<PracticeSignal> completedPracticeSignals,
                                                         List<ReviewSignal> completedReviewSignals,
                                                         Long currentNodeId,
                                                         Map<String, LearningMapNodeEntity> nodeByCode) {
        String progressStatus = resolveKnowledgeProgressStatus(
                context,
                node,
                prerequisiteNodeCodes,
                currentNodeId,
                nodeByCode);
        int evidenceCount = (CharSequenceUtil.isNotBlank(node.getVerificationMethod()) ? 1 : 0)
                + (context.latestTutorByNodeId().containsKey(node.getId()) ? 1 : 0)
                + CollUtil.size(completedPracticeSignals)
                + CollUtil.size(completedReviewSignals);
        ReviewSignal latestReviewSignal = latestCompletedReviewSignal(completedReviewSignals);
        return KnowledgeGraphNodeBO.builder()
                .id("concept-" + node.getId())
                .nodeId(node.getId())
                .nodeCode(node.getNodeCode())
                .title(node.getTitle())
                .description(compactText(node.getDescription(), node.getTitle() + " 是当前路径上的一个关键学习节点。"))
                .learningObjective(compactText(
                        node.getLearningObjective(),
                        "能用自己的话说明 " + node.getTitle() + " 的作用、边界和验证方式。"))
                .whyItMatters(compactText(node.getWhyItMatters(), "这是达成当前目标的一块关键能力拼图。"))
                .verificationMethod(compactText(node.getVerificationMethod(), "需要补上 " + node.getTitle() + " 的更明确验证方式。"))
                .completionCriteria(compactText(node.getCompletionCriteria(), "能把 " + node.getTitle() + " 说清楚并放进真实场景。"))
                .progressStatus(progressStatus)
                .masteryLevel(CharSequenceUtil.blankToDefault(masteryRecord == null ? null : masteryRecord.getMasteryLevel(), "unknown"))
                .masteryScore(masteryRecord == null ? null : masteryRecord.getMasteryScore())
                .evidenceCount(evidenceCount)
                .prerequisiteNodeCodes(prerequisiteNodeCodes)
                .unlocksNodeCodes(unlocksNodeCodes)
                .tags(buildKnowledgeTags(
                        node,
                        progressStatus,
                        masteryRecord,
                        context.latestTutorByNodeId().get(node.getId()),
                        latestReviewSignal,
                        evidenceCount,
                        currentNodeId))
                .build();
    }

    private String resolveKnowledgeProgressStatus(GoalLearningContext context,
                                                  LearningMapNodeEntity node,
                                                  List<String> prerequisiteNodeCodes,
                                                  Long currentNodeId,
                                                  Map<String, LearningMapNodeEntity> nodeByCode) {
        LearningNodeProgressEntity progress = context.progressByNodeId().get(node.getId());
        if (progress != null && CharSequenceUtil.isNotBlank(progress.getStatus())) {
            return progress.getStatus();
        }
        if (Objects.equals(node.getId(), currentNodeId)) {
            return "IN_PROGRESS";
        }
        if (CollUtil.isEmpty(prerequisiteNodeCodes)) {
            return "READY";
        }
        boolean prerequisitesSatisfied = prerequisiteNodeCodes.stream().allMatch(code -> {
            LearningMapNodeEntity prerequisiteNode = nodeByCode.get(code);
            if (prerequisiteNode == null) {
                return false;
            }
            LearningNodeProgressEntity prerequisiteProgress = context.progressByNodeId().get(prerequisiteNode.getId());
            return prerequisiteProgress != null
                    && (CharSequenceUtil.equals(prerequisiteProgress.getStatus(), "COMPLETED")
                    || CharSequenceUtil.equals(prerequisiteProgress.getStatus(), "REVIEWING"));
        });
        return prerequisitesSatisfied ? "READY" : "PENDING";
    }

    private Map<String, List<String>> buildUnlocksByCode(List<LearningMapNodeEntity> nodes,
                                                         Map<String, List<String>> prerequisiteNodeCodesByCode) {
        Map<String, List<String>> unlocksByCode = new HashMap<>();
        CollUtil.emptyIfNull(nodes).forEach(node -> prerequisiteNodeCodesByCode
                .getOrDefault(node.getNodeCode(), Collections.emptyList())
                .forEach(code -> unlocksByCode.computeIfAbsent(code, key -> new ArrayList<>()).add(node.getNodeCode())));
        return unlocksByCode;
    }

    private List<String> buildKnowledgeTags(LearningMapNodeEntity node,
                                            String progressStatus,
                                            MasteryRecordBO masteryRecord,
                                            TutorSignal latestTutorTurn,
                                            ReviewSignal latestReviewSignal,
                                            int evidenceCount,
                                            Long currentNodeId) {
        List<String> tags = new ArrayList<>();
        if (Objects.equals(node.getId(), currentNodeId)) {
            tags.add("当前焦点");
        }
        if (CharSequenceUtil.equals(progressStatus, "REVIEWING")) {
            tags.add("进入复盘");
        }
        if (CharSequenceUtil.equals(progressStatus, "READY")) {
            tags.add("可推进");
        }
        if (CharSequenceUtil.equals(masteryRecord == null ? null : masteryRecord.getMasteryLevel(), "stable")) {
            tags.add("稳定掌握");
        } else if (CharSequenceUtil.equals(masteryRecord == null ? null : masteryRecord.getMasteryLevel(), "fragile")) {
            tags.add("掌握脆弱");
        }
        if (CharSequenceUtil.equals(latestTutorTurn == null ? null : latestTutorTurn.diagnosis(), "misconception")) {
            tags.add("边界易混");
        }
        if (CharSequenceUtil.equals(latestTutorTurn == null ? null : latestTutorTurn.diagnosis(), "needs_prereq")) {
            tags.add("前置待补");
        }
        if (latestReviewSignal != null && CharSequenceUtil.equals(latestReviewSignal.rating(), "forgotten")) {
            tags.add("复盘遗忘");
        } else if (latestReviewSignal != null && CharSequenceUtil.equals(latestReviewSignal.rating(), "solid")) {
            tags.add("复盘稳住");
        }
        if (evidenceCount == 0) {
            tags.add("待补证据");
        }
        return uniqueList(tags, 4);
    }

    private List<KnowledgeGraphEdgeBO> buildKnowledgeGraphEdges(List<LearningMapNodeEntity> nodesBySortOrder,
                                                                Map<String, List<String>> prerequisiteNodeCodesByCode,
                                                                Map<String, LearningMapNodeEntity> nodeByCode,
                                                                Map<String, KnowledgeGraphNodeBO> graphNodeByCode) {
        List<KnowledgeGraphEdgeBO> edges = new ArrayList<>();
        for (int index = 0; index < CollUtil.size(nodesBySortOrder); index += 1) {
            LearningMapNodeEntity node = nodesBySortOrder.get(index);
            List<String> prerequisiteNodeCodes = prerequisiteNodeCodesByCode.getOrDefault(node.getNodeCode(), Collections.emptyList());
            for (String code : prerequisiteNodeCodes) {
                if (!nodeByCode.containsKey(code)) {
                    continue;
                }
                KnowledgeGraphNodeBO sourceNode = graphNodeByCode.get(code);
                KnowledgeGraphNodeBO targetNode = graphNodeByCode.get(node.getNodeCode());
                String strength = relationStrengthForNode(sourceNode);
                edges.add(KnowledgeGraphEdgeBO.builder()
                        .id("prerequisite-" + code + "-" + node.getNodeCode())
                        .sourceCode(code)
                        .targetCode(node.getNodeCode())
                        .type("prerequisite")
                        .label("前置依赖")
                        .strength(strength)
                        .summary(relationSummary(sourceNode, targetNode, "prerequisite", strength))
                        .build());
            }

            LearningMapNodeEntity nextNode = index + 1 >= nodesBySortOrder.size() ? null : nodesBySortOrder.get(index + 1);
            if (nextNode == null) {
                continue;
            }
            List<String> nextPrerequisites = prerequisiteNodeCodesByCode.getOrDefault(nextNode.getNodeCode(), Collections.emptyList());
            if (nextPrerequisites.contains(node.getNodeCode())) {
                continue;
            }
            KnowledgeGraphNodeBO sourceNode = graphNodeByCode.get(node.getNodeCode());
            KnowledgeGraphNodeBO targetNode = graphNodeByCode.get(nextNode.getNodeCode());
            String strength = relationStrengthForNode(sourceNode);
            edges.add(KnowledgeGraphEdgeBO.builder()
                    .id("path-" + node.getNodeCode() + "-" + nextNode.getNodeCode())
                    .sourceCode(node.getNodeCode())
                    .targetCode(nextNode.getNodeCode())
                    .type("path")
                    .label("路径延伸")
                    .strength(strength)
                    .summary(relationSummary(sourceNode, targetNode, "path", strength))
                    .build());
        }
        return edges;
    }

    private String relationStrengthForNode(KnowledgeGraphNodeBO node) {
        if (node == null) {
            return "missing";
        }
        if (CharSequenceUtil.equals(node.getMasteryLevel(), "fragile")
                || CollUtil.contains(node.getTags(), "前置待补")
                || CollUtil.contains(node.getTags(), "边界易混")
                || CollUtil.contains(node.getTags(), "待补证据")) {
            return "watch";
        }
        if (CharSequenceUtil.equals(node.getMasteryLevel(), "stable")
                || defaultInt(node.getEvidenceCount(), 0) >= 3) {
            return "stable";
        }
        return "missing";
    }

    private String relationSummary(KnowledgeGraphNodeBO source,
                                   KnowledgeGraphNodeBO target,
                                   String type,
                                   String strength) {
        if (CharSequenceUtil.equals(type, "prerequisite")) {
            if (CharSequenceUtil.equals(strength, "stable")) {
                return "前置「" + (source == null ? "未知概念" : source.getTitle())
                        + "」与当前概念的关系已经有较稳定的学习证据支撑。";
            }
            if (CharSequenceUtil.equals(strength, "watch")) {
                return "前置「" + (source == null ? "未知概念" : source.getTitle())
                        + "」和「" + (target == null ? "当前概念" : target.getTitle()) + "」之间还存在薄弱连接，先补证据会更稳。";
            }
            return "前置「" + (source == null ? "未知概念" : source.getTitle())
                    + "」和「" + (target == null ? "当前概念" : target.getTitle()) + "」之间还缺足够多的学习证据。";
        }
        if (CharSequenceUtil.equals(strength, "stable")) {
            return "这条路径延伸已经比较顺，可以继续自然推进到「" + (target == null ? "下一个概念" : target.getTitle()) + "」。";
        }
        if (CharSequenceUtil.equals(strength, "watch")) {
            return "从「" + (source == null ? "当前概念" : source.getTitle())
                    + "」往「" + (target == null ? "下一个概念" : target.getTitle()) + "」延伸前，最好先补强中间证据。";
        }
        return "这条路径延伸还比较早期，系统更希望先补厚前一个概念的验证证据。";
    }

    private List<KnowledgeGraphEvidenceBO> buildKnowledgeEvidenceForNode(LearningMapNodeEntity node,
                                                                         KnowledgeGraphNodeBO graphNode,
                                                                         TutorSignal latestTutorTurn,
                                                                         List<PracticeSignal> completedPracticeSignals,
                                                                         List<ReviewSignal> completedReviewSignals,
                                                                         LearningNodeProgressEntity progress,
                                                                         boolean includeMissing,
                                                                         LocalDateTime generatedAt) {
        if (node == null || graphNode == null) {
            return Collections.emptyList();
        }

        int clearPracticeCount = countPracticeSignals(completedPracticeSignals, "clear", false);
        int stretchPracticeCount = countPracticeSignals(completedPracticeSignals, "stretch", false);
        int stuckPracticeCount = countPracticeSignals(completedPracticeSignals, "stuck", false);
        String sharedStrength = buildKnowledgeEvidenceStrength(
                clearPracticeCount,
                stretchPracticeCount,
                stuckPracticeCount,
                latestTutorTurn == null ? null : latestTutorTurn.diagnosis());

        List<KnowledgeGraphEvidenceBO> evidence = new ArrayList<>();
        evidence.add(KnowledgeGraphEvidenceBO.builder()
                .id("map-" + node.getId())
                .nodeId(node.getId())
                .nodeTitle(node.getTitle())
                .label("验证方式")
                .detail(compactText(
                        node.getVerificationMethod(),
                        CharSequenceUtil.blankToDefault(node.getCompletionCriteria(), "当前还缺少更明确的验证标准。")))
                .source("map")
                .strength(CharSequenceUtil.isNotBlank(node.getVerificationMethod()) ? "strong" : "missing")
                .updatedAt(generatedAt)
                .build());

        if (latestTutorTurn != null) {
            evidence.add(KnowledgeGraphEvidenceBO.builder()
                    .id("tutor-" + node.getId())
                    .nodeId(node.getId())
                    .nodeTitle(node.getTitle())
                    .label("Tutor 诊断")
                    .detail("最近 Tutor 判断这里是「" + humanizeDiagnosis(latestTutorTurn.diagnosis()) + "」。")
                    .source("tutor")
                    .strength(CharSequenceUtil.equals(latestTutorTurn.diagnosis(), "ready") ? "strong" : "watch")
                    .updatedAt(latestTutorTurn.updatedAt())
                    .build());
        } else if (includeMissing) {
            evidence.add(KnowledgeGraphEvidenceBO.builder()
                    .id("tutor-missing-" + node.getId())
                    .nodeId(node.getId())
                    .nodeTitle(node.getTitle())
                    .label("Tutor 诊断")
                    .detail("这个概念还没有留下诊断证据，系统还不知道你真正卡在哪里。")
                    .source("tutor")
                    .strength("missing")
                    .updatedAt(generatedAt)
                    .build());
        }

        if (CollUtil.isNotEmpty(completedPracticeSignals)) {
            String practiceDetail = stuckPracticeCount > 0
                    ? "这部分已经完成 " + completedPracticeSignals.size() + " 个练习，但仍有 " + stuckPracticeCount + " 个任务的评测结论为“需要补强”。"
                    : clearPracticeCount > 0
                    ? "这部分已经完成 " + completedPracticeSignals.size() + " 个练习，其中 " + clearPracticeCount + " 个已通过证据评测。"
                    : "这部分已经留下 " + completedPracticeSignals.size() + " 个练习记录，目前仍处于“能做但不稳”的阶段。";
            LocalDateTime latestPracticeAt = completedPracticeSignals.stream()
                    .map(PracticeSignal::updatedAt)
                    .filter(Objects::nonNull)
                    .max(this::compareUpdatedAt)
                    .orElse(generatedAt);
            evidence.add(KnowledgeGraphEvidenceBO.builder()
                    .id("practice-" + node.getId())
                    .nodeId(node.getId())
                    .nodeTitle(node.getTitle())
                    .label("Practice 证据")
                    .detail(practiceDetail)
                    .source("practice")
                    .strength(sharedStrength)
                    .updatedAt(latestPracticeAt)
                    .build());
        } else if (includeMissing) {
            evidence.add(KnowledgeGraphEvidenceBO.builder()
                    .id("practice-missing-" + node.getId())
                    .nodeId(node.getId())
                    .nodeTitle(node.getTitle())
                    .label("Practice 证据")
                    .detail("这个概念还没有练习证据，当前更多停留在路径和理解层。")
                    .source("practice")
                    .strength("missing")
                    .updatedAt(generatedAt)
                    .build());
        }

        if (CollUtil.isNotEmpty(completedReviewSignals)) {
            int solidReviewCount = countReviewSignals(completedReviewSignals, "solid");
            int forgottenReviewCount = countReviewSignals(completedReviewSignals, "forgotten");
            ReviewSignal latestReviewSignal = latestCompletedReviewSignal(completedReviewSignals);
            LocalDateTime latestReviewAt = completedReviewSignals.stream()
                    .map(ReviewSignal::updatedAt)
                    .filter(Objects::nonNull)
                    .max(this::compareUpdatedAt)
                    .orElse(generatedAt);
            String reviewDetail = forgottenReviewCount > 0
                    ? "已经完成 " + completedReviewSignals.size() + " 次复盘，其中 " + forgottenReviewCount
                    + " 次直接暴露遗忘，说明这条概念链还需要优先回收。"
                    : solidReviewCount > 0
                    ? "已经完成 " + completedReviewSignals.size() + " 次复盘，其中 " + solidReviewCount
                    + " 次已经稳住，说明这部分开始变成长期记忆。"
                    : "已经完成 " + completedReviewSignals.size() + " 次复盘，目前仍处在“想起来但不稳”的阶段。";
            evidence.add(KnowledgeGraphEvidenceBO.builder()
                    .id("review-" + node.getId())
                    .nodeId(node.getId())
                    .nodeTitle(node.getTitle())
                    .label("Review 证据")
                    .detail(reviewDetail + (latestReviewSignal == null ? "" : " 最近一次结果是「" + humanizeReviewRating(latestReviewSignal.rating()) + "」。"))
                    .source("review")
                    .strength(forgottenReviewCount > 0 ? "watch" : solidReviewCount > 0 ? "strong" : "watch")
                    .updatedAt(latestReviewAt)
                    .build());
        } else if (CharSequenceUtil.equals(graphNode.getProgressStatus(), "REVIEWING")) {
            evidence.add(KnowledgeGraphEvidenceBO.builder()
                    .id("review-" + node.getId())
                    .nodeId(node.getId())
                    .nodeTitle(node.getTitle())
                    .label("Review 证据")
                    .detail("节点已经进入复盘窗口，但还缺真实复盘结果写回服务端。")
                    .source("review")
                    .strength("missing")
                    .updatedAt(progress == null ? generatedAt : defaultIfNull(progress.getUpdatedAt(), generatedAt))
                    .build());
        }
        return evidence;
    }

    private String buildKnowledgeEvidenceStrength(int clearPracticeCount,
                                                  int stretchPracticeCount,
                                                  int stuckPracticeCount,
                                                  String diagnosis) {
        if (stuckPracticeCount > 0 || CharSequenceUtil.equals(diagnosis, "needs_prereq")) {
            return "watch";
        }
        if (clearPracticeCount > 0 || CharSequenceUtil.equals(diagnosis, "ready")) {
            return "strong";
        }
        if (stretchPracticeCount > 0 || CharSequenceUtil.equals(diagnosis, "misconception")) {
            return "watch";
        }
        return "missing";
    }

    private List<String> buildKnowledgeFrontier(List<KnowledgeGraphNodeBO> graphNodes,
                                                KnowledgeGraphNodeBO focusNode) {
        List<String> frontier = new ArrayList<>();
        appendIfNotBlank(frontier, focusNode == null ? null : "当前图谱焦点：" + focusNode.getTitle());
        graphNodes.stream()
                .filter(node -> CharSequenceUtil.equals(node.getProgressStatus(), "READY"))
                .limit(2)
                .forEach(node -> frontier.add("接下来可推进：" + node.getTitle()));
        graphNodes.stream()
                .filter(node -> CharSequenceUtil.equals(node.getProgressStatus(), "REVIEWING"))
                .limit(1)
                .forEach(node -> frontier.add("待回收概念：" + node.getTitle()));
        return uniqueList(frontier, 4);
    }

    private List<String> buildKnowledgeWeakAreas(List<KnowledgeGraphNodeBO> graphNodes,
                                                 KnowledgeGraphNodeBO focusNode,
                                                 GrowthHighlights growthHighlights) {
        List<String> weakAreas = new ArrayList<>();
        graphNodes.stream()
                .filter(node -> CharSequenceUtil.equals(node.getMasteryLevel(), "fragile")
                        || CollUtil.contains(node.getTags(), "前置待补")
                        || CollUtil.contains(node.getTags(), "边界易混"))
                .limit(3)
                .forEach(node -> weakAreas.add(node.getTitle() + "："
                        + (CollUtil.contains(node.getTags(), "前置待补")
                        ? "前置待补"
                        : CollUtil.contains(node.getTags(), "边界易混")
                        ? "边界易混"
                        : "掌握脆弱")));
        if (focusNode != null && defaultInt(focusNode.getEvidenceCount(), 0) == 0) {
            weakAreas.add("当前焦点「" + focusNode.getTitle() + "」还缺足够验证证据。");
        }
        weakAreas.addAll(CollUtil.emptyIfNull(growthHighlights.commonStickingPoints()).stream().limit(2).toList());
        return uniqueList(weakAreas, 6);
    }

    private List<String> parseNodeCodes(String value) {
        return uniqueList(CharSequenceUtil.splitTrim(CharSequenceUtil.blankToDefault(value, ""), ','), 12);
    }

    private String compactText(String value, String fallback) {
        if (CharSequenceUtil.isBlank(value)) {
            return CharSequenceUtil.blankToDefault(fallback, "");
        }
        String next = value.replaceAll("\\s+", " ").trim();
        return CharSequenceUtil.blankToDefault(next, CharSequenceUtil.blankToDefault(fallback, ""));
    }

    private String humanizeDiagnosis(String diagnosis) {
        if (CharSequenceUtil.equals(diagnosis, "ready")) {
            return "准备充分";
        }
        if (CharSequenceUtil.equals(diagnosis, "misconception")) {
            return "存在概念误解";
        }
        if (CharSequenceUtil.equals(diagnosis, "needs_prereq")) {
            return "需要补前置";
        }
        return CharSequenceUtil.blankToDefault(diagnosis, "待判断");
    }

    private LocalDateTime defaultIfNull(LocalDateTime value, LocalDateTime fallback) {
        return value == null ? fallback : value;
    }

    private LearningRhythmBO buildLearningRhythm(GoalLearningContext context) {
        LocalDate today = LocalDate.now();
        LocalDate windowStart = today.minusDays(SERVER_ACTIVITY_WINDOW_DAYS - 1L);
        Map<LocalDate, ActivityDay> activityByDay = new HashMap<>();

        context.latestTutorBySessionId().values().forEach(turn ->
                addActivity(activityByDay, toDate(turn.updatedAt()), DEFAULT_TUTOR_MINUTES, "Tutor", turn.updatedAt(), windowStart));
        context.practiceSignals().forEach(signal ->
                addActivity(activityByDay,
                        toDate(signal.updatedAt()),
                        signal.minutes(),
                        signal.completed() ? "Practice 已完成" : "Practice 草稿",
                        signal.updatedAt(),
                        windowStart));
        context.reviewSignals().forEach(signal ->
                addActivity(activityByDay,
                        toDate(signal.updatedAt()),
                        signal.minutes(),
                        signal.completed() ? "Review 已完成" : "Review 草稿",
                        signal.updatedAt(),
                        windowStart));
        context.reflectionSignals().forEach(signal ->
                addActivity(activityByDay,
                        signal.reflectionDate(),
                        DEFAULT_REFLECTION_MINUTES,
                        "Reflection",
                        signal.updatedAt(),
                        windowStart));

        List<LearningRhythmDayBO> week = buildRhythmWeek(today, activityByDay);
        int weeklyTargetMinutes = Math.max(defaultInt(context.goal().getWeeklyLearningMinutes(), 0), DEFAULT_WEEKLY_MINUTES);
        int loggedMinutes = week.stream().mapToInt(item -> defaultInt(item.getMinutes(), 0)).sum();
        int remainingMinutes = Math.max(0, weeklyTargetMinutes - loggedMinutes);
        int activeDays = (int) week.stream().filter(item -> Boolean.TRUE.equals(item.getActive())).count();
        boolean todayDone = Boolean.TRUE.equals(week.getLast().getActive());
        int completionPercent = weeklyTargetMinutes <= 0
                ? 0
                : Math.min(100, (int) Math.round((loggedMinutes * 100.0) / weeklyTargetMinutes));
        boolean reflectedToday = context.reflectionSignals().stream()
                .anyMatch(item -> Objects.equals(item.reflectionDate(), today));
        String currentNodeTitle = context.currentNode() == null ? null : context.currentNode().getTitle();
        int completedPracticeCount = (int) context.practiceSignals().stream()
                .filter(PracticeSignal::completed)
                .count();
        int stuckPracticeCount = (int) context.practiceSignals().stream()
                .filter(PracticeSignal::completed)
                .filter(item -> CharSequenceUtil.equals(item.rating(), "stuck"))
                .count();
        List<ReviewSignal> completedReviewSignals = context.reviewSignals().stream()
                .filter(ReviewSignal::completed)
                .toList();
        int forgottenReviewCount = countReviewSignals(completedReviewSignals, "forgotten");
        int reviewingNodeCount = (int) context.progressByNodeId().values().stream()
                .filter(progress -> CharSequenceUtil.equals(progress.getStatus(), "REVIEWING"))
                .count();
        int pendingReviewCount = Math.max(
                reviewingNodeCount,
                (int) context.reviewSignals().stream().filter(signal -> !signal.completed()).count());

        int streakDays = 0;
        for (int index = week.size() - 1; index >= 0; index -= 1) {
            if (!Boolean.TRUE.equals(week.get(index).getActive())) {
                break;
            }
            streakDays += 1;
        }

        ActivityDay latestEntry = activityByDay.values().stream()
                .filter(Objects::nonNull)
                .max((left, right) -> compareUpdatedAt(left.latestAt(), right.latestAt()))
                .orElse(null);

        String rhythmTitle = "节奏正在形成";
        String rhythmSummary = "你已经开始留下连续学习动作，下一步要做的是把这些动作更稳定地接成闭环。";
        if (activeDays == 0) {
            rhythmTitle = "节奏还没建立";
            rhythmSummary = "系统已经知道你的目标，但最近 7 天里还缺足够的学习证据。先完成一个最小闭环，比重新规划更重要。";
        } else if (!todayDone && completionPercent < 40) {
            rhythmTitle = "今天适合先恢复动量";
            rhythmSummary = "这周不是完全没在学，但今天还没有把节奏接上。优先做一个主任务，别让连续性感消失。";
        } else if (streakDays >= 4 && completionPercent >= 70) {
            rhythmTitle = "节奏已经比较稳定";
            rhythmSummary = "连续性和投入都在上升，接下来重点不是加码，而是继续保持同一条学习主线。";
        } else if (activeDays >= 3 || streakDays >= 2) {
            rhythmTitle = "节奏开始成形";
            rhythmSummary = "你已经不只是偶尔学一下了。现在最重要的是把 Tutor、Practice、Review 更紧地串起来。";
        }

        String weeklyStatus = "最近 7 天已记录 " + loggedMinutes + "/" + weeklyTargetMinutes
                + " 分钟，活跃 " + activeDays + "/7 天，当前连续 " + streakDays + " 天。";
        String recoveryPlan = !todayDone
                ? "如果今天时间有限，先完成一个 15 分钟主任务，再用 5 分钟做 Reflection，把节奏接上即可。"
                : completionPercent < 60
                ? "接下来两天尽量各保留一次 20 分钟推进，优先当前节点，不再切到新主题。"
                : "保持当前节奏即可，学习结束前补一轮短复盘，让连续推进不会变成新的遗忘。";

        WeeklyPlanMeta weeklyPlanMeta = buildWeeklyPlan(
                remainingMinutes,
                todayDone,
                completionPercent,
                streakDays,
                activeDays,
                reflectedToday,
                currentNodeTitle,
                completedPracticeCount,
                stuckPracticeCount,
                forgottenReviewCount,
                pendingReviewCount);

        return LearningRhythmBO.builder()
                .generatedAt(LocalDateTime.now())
                .mode("server")
                .weeklyTargetMinutes(weeklyTargetMinutes)
                .loggedMinutes(loggedMinutes)
                .remainingMinutes(remainingMinutes)
                .completionPercent(completionPercent)
                .streakDays(streakDays)
                .activeDays(activeDays)
                .todayDone(todayDone)
                .rhythmTitle(rhythmTitle)
                .rhythmSummary(rhythmSummary)
                .weeklyStatus(weeklyStatus)
                .weeklyFocus(weeklyPlanMeta.weeklyFocus())
                .weeklyPlanSummary(weeklyPlanMeta.weeklyPlanSummary())
                .recoveryPlan(recoveryPlan)
                .signals(buildRhythmSignals(activeDays, streakDays, loggedMinutes, weeklyTargetMinutes, latestEntry == null ? null : latestEntry.source()))
                .nextNudges(buildRhythmNextNudges(todayDone, completionPercent, streakDays, reflectedToday, currentNodeTitle))
                .weeklyPlan(weeklyPlanMeta.weeklyPlan())
                .week(week)
                .build();
    }

    private List<LearningRhythmDayBO> buildRhythmWeek(LocalDate today, Map<LocalDate, ActivityDay> activityByDay) {
        List<LearningRhythmDayBO> week = new ArrayList<>();
        for (int index = 0; index < SERVER_ACTIVITY_WINDOW_DAYS; index += 1) {
            LocalDate date = today.minusDays(SERVER_ACTIVITY_WINDOW_DAYS - 1L - index);
            ActivityDay activity = activityByDay.get(date);
            week.add(LearningRhythmDayBO.builder()
                    .date(date.toString())
                    .label(formatDayLabel(date))
                    .minutes(activity == null ? 0 : activity.minutes())
                    .active(activity != null && activity.minutes() > 0)
                    .isToday(index == SERVER_ACTIVITY_WINDOW_DAYS - 1)
                    .build());
        }
        return week;
    }

    private void addActivity(Map<LocalDate, ActivityDay> activityByDay,
                             LocalDate activityDate,
                             int minutes,
                             String source,
                             LocalDateTime updatedAt,
                             LocalDate windowStart) {
        if (activityDate == null || activityDate.isBefore(windowStart)) {
            return;
        }
        ActivityDay current = activityByDay.get(activityDate);
        if (current == null) {
            activityByDay.put(activityDate, new ActivityDay(minutes, updatedAt, source));
            return;
        }
        boolean replaceSource = compareUpdatedAt(updatedAt, current.latestAt()) >= 0;
        activityByDay.put(activityDate, new ActivityDay(
                current.minutes() + minutes,
                replaceSource ? updatedAt : current.latestAt(),
                replaceSource ? source : current.source()));
    }

    private List<String> buildRhythmSignals(int activeDays,
                                            int streakDays,
                                            int loggedMinutes,
                                            int weeklyTargetMinutes,
                                            String latestSource) {
        List<String> signals = new ArrayList<>();
        signals.add("最近 7 天里，你有 " + activeDays + "/7 天留下了真实学习动作，当前连续天数是 " + streakDays + " 天。");
        signals.add("系统按 Tutor、Practice、Review、Reflection 的记录，估算你已经投入了 " + loggedMinutes + "/" + weeklyTargetMinutes + " 分钟。");
        if (CharSequenceUtil.isNotBlank(latestSource)) {
            signals.add("最近一次节奏证据来自 " + latestSource + "。");
        }
        return uniqueList(signals, 3);
    }

    private List<String> buildRhythmNextNudges(boolean todayDone,
                                               int completionPercent,
                                               int streakDays,
                                               boolean reflectedToday,
                                               String currentNodeTitle) {
        List<String> nudges = new ArrayList<>();
        if (!todayDone) {
            nudges.add("今天先完成一个最小学习闭环：15 分钟主任务，外加 5 分钟 Reflection。");
        }
        if (completionPercent < 60) {
            nudges.add("本周剩余学习时间优先给当前主线，不要再开一个新主题分散注意力。");
        }
        if (streakDays < 2) {
            nudges.add("给明天也留一个最小动作，哪怕只是一次 Tutor 诊断，也比完全断开更重要。");
        }
        if (CharSequenceUtil.isNotBlank(currentNodeTitle)) {
            nudges.add("下一轮最好继续围绕「" + currentNodeTitle + "」推进，不要频繁切换节点。");
        }
        if (!reflectedToday) {
            nudges.add("学习结束前补上 Reflection，节奏才能被系统真正记住。");
        }
        return uniqueList(nudges, 3);
    }

    private WeeklyPlanMeta buildWeeklyPlan(int remainingMinutes,
                                           boolean todayDone,
                                           int completionPercent,
                                           int streakDays,
                                           int activeDays,
                                           boolean reflectedToday,
                                           String currentNodeTitle,
                                           int completedPracticeCount,
                                           int stuckPracticeCount,
                                           int forgottenReviewCount,
                                           int pendingReviewCount) {
        int targetMinutes = Math.max(20, Math.min(180, remainingMinutes <= 0 ? 20 : remainingMinutes));
        List<WeeklyPlanBucketBO> buckets = new ArrayList<>();

        if (forgottenReviewCount > 0 || pendingReviewCount >= 2) {
            buckets.add(WeeklyPlanBucketBO.builder()
                    .id("weekly-review")
                    .kind("review")
                    .title("先回收复盘窗口")
                    .summary("先把已经进入遗忘区或复盘窗口的节点收住，再决定还能不能继续开新内容。")
                    .recommendedMinutes(forgottenReviewCount > 0 ? 45 : 35)
                    .reason(forgottenReviewCount > 0
                            ? "当前已有 " + forgottenReviewCount + " 个节点进入真实遗忘区间。"
                            : "当前至少有 " + pendingReviewCount + " 个节点正在等待复盘。")
                    .build());
        }

        if (stuckPracticeCount > 0) {
            buckets.add(WeeklyPlanBucketBO.builder()
                    .id("weekly-repair")
                    .kind("repair")
                    .title("处理卡住的练习点")
                    .summary("这周要先把“懂了一点但做不出来”的地方处理掉，避免卡点被继续带入下轮计划。")
                    .recommendedMinutes(30)
                    .reason("已有 " + stuckPracticeCount + " 个练习任务被标记为“卡住”。")
                    .build());
        }

        buckets.add(WeeklyPlanBucketBO.builder()
                .id("weekly-learn")
                .kind("learn")
                .title(CharSequenceUtil.isNotBlank(currentNodeTitle) ? "继续推进「" + currentNodeTitle + "」" : "继续当前学习主线")
                .summary(activeDays >= 3 || streakDays >= 2
                        ? "这周的新学时间应继续压在同一条主线上，不要频繁切换主题。"
                        : "这周先用一条最短主线把节奏接起来，别一开始就把范围拉太大。")
                .recommendedMinutes(completionPercent < 40 ? 50 : completedPracticeCount > 0 ? 60 : 55)
                .reason(CharSequenceUtil.isNotBlank(currentNodeTitle)
                        ? "当前最适合持续推进的节点仍然是「" + currentNodeTitle + "」。"
                        : "当前主线还需要持续的学习动作去形成稳定推进。")
                .build());

        if (!reflectedToday || !todayDone) {
            buckets.add(WeeklyPlanBucketBO.builder()
                    .id("weekly-reflect")
                    .kind("reflect")
                    .title("补上封箱与下一步确认")
                    .summary("每轮学习结束后都留 5 到 10 分钟做 Reflection，这样下次重排才不会重新从猜测开始。")
                    .recommendedMinutes(15)
                    .reason(!reflectedToday
                            ? "今天的理解和卡点还没有完整写回系统。"
                            : "当前节奏还没完全接上，封箱动作能帮助恢复连续性。")
                    .build());
        }

        List<WeeklyPlanBucketBO> weeklyPlan = normalizeWeeklyPlanMinutes(buckets, targetMinutes).stream()
                .limit(4)
                .toList();

        String weeklyFocus = forgottenReviewCount > 0
                ? "这周先收旧内容，再继续推主线。"
                : stuckPracticeCount > 0
                ? "这周先把卡住的地方打通，再放大学习投入。"
                : completionPercent < 50
                ? "这周重点是把学习节奏接回正轨。"
                : "这周重点是保持单线推进，同时别放掉复盘。";
        String weeklyPlanSummary = weeklyPlan.isEmpty()
                ? "这周的学习编排还在形成中，先完成一个最小闭环。"
                : "剩余约 " + targetMinutes + " 分钟里，系统建议优先安排 "
                + weeklyPlan.stream()
                .map(item -> item.getTitle() + item.getRecommendedMinutes() + " 分钟")
                .collect(Collectors.joining("、"))
                + "。";

        return new WeeklyPlanMeta(weeklyFocus, weeklyPlanSummary, weeklyPlan);
    }

    private List<WeeklyPlanBucketBO> normalizeWeeklyPlanMinutes(List<WeeklyPlanBucketBO> items, int targetMinutes) {
        if (CollUtil.isEmpty(items) || targetMinutes <= 0) {
            return Collections.emptyList();
        }
        int baseTotal = items.stream().mapToInt(item -> defaultInt(item.getRecommendedMinutes(), 0)).sum();
        if (baseTotal <= 0) {
            return items;
        }
        List<WeeklyPlanBucketBO> scaled = items.stream()
                .map(item -> WeeklyPlanBucketBO.builder()
                        .id(item.getId())
                        .kind(item.getKind())
                        .title(item.getTitle())
                        .summary(item.getSummary())
                        .recommendedMinutes(Math.max(10, (int) Math.round((defaultInt(item.getRecommendedMinutes(), 0) * 1.0 / baseTotal) * targetMinutes)))
                        .reason(item.getReason())
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));
        int currentTotal = scaled.stream().mapToInt(item -> defaultInt(item.getRecommendedMinutes(), 0)).sum();
        int delta = targetMinutes - currentTotal;
        if (!scaled.isEmpty() && delta != 0) {
            WeeklyPlanBucketBO first = scaled.getFirst();
            first.setRecommendedMinutes(Math.max(10, defaultInt(first.getRecommendedMinutes(), 10) + delta));
        }
        return scaled;
    }

    private int countPracticeSignals(List<PracticeSignal> signals,
                                     String rating,
                                     boolean handoffOnly) {
        return countPracticeSignalsByKinds(signals, rating, null, handoffOnly);
    }

    private int countPracticeSignals(List<PracticeSignal> signals,
                                     String rating,
                                     String evidenceKind,
                                     boolean handoffOnly) {
        return countPracticeSignalsByKinds(signals, rating, evidenceKind == null ? null : Set.of(evidenceKind), handoffOnly);
    }

    private int countPracticeSignalsByKinds(List<PracticeSignal> signals,
                                            String rating,
                                            Set<String> evidenceKinds,
                                            boolean handoffOnly) {
        return (int) CollUtil.emptyIfNull(signals).stream()
                .filter(item -> !handoffOnly || item.handoffValidation())
                .filter(item -> CharSequenceUtil.equals(item.rating(), rating))
                .filter(item -> evidenceKinds == null || evidenceKinds.contains(item.evidenceKind()))
                .count();
    }

    private int countReviewSignals(List<ReviewSignal> signals, String rating) {
        return (int) CollUtil.emptyIfNull(signals).stream()
                .filter(ReviewSignal::completed)
                .filter(item -> CharSequenceUtil.equals(item.rating(), rating))
                .count();
    }

    private ReviewSignal latestCompletedReviewSignal(List<ReviewSignal> signals) {
        return CollUtil.emptyIfNull(signals).stream()
                .filter(ReviewSignal::completed)
                .max((left, right) -> compareUpdatedAt(left.updatedAt(), right.updatedAt()))
                .orElse(null);
    }

    private LocalDateTime resolveLatestUpdatedAt(LearningNodeProgressEntity progress,
                                                 TutorSignal latestTutorTurn,
                                                 List<PracticeSignal> practiceSignals,
                                                 List<ReviewSignal> reviewSignals) {
        List<LocalDateTime> timestamps = new ArrayList<>();
        if (progress != null) {
            timestamps.add(progress.getUpdatedAt());
            timestamps.add(progress.getLastStudiedAt());
        }
        if (latestTutorTurn != null) {
            timestamps.add(latestTutorTurn.updatedAt());
        }
        CollUtil.emptyIfNull(practiceSignals).stream()
                .map(PracticeSignal::updatedAt)
                .forEach(timestamps::add);
        CollUtil.emptyIfNull(reviewSignals).stream()
                .map(ReviewSignal::updatedAt)
                .forEach(timestamps::add);
        return timestamps.stream()
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
    }

    private int baseScoreFromProgressStatus(String progressStatus) {
        return switch (progressStatus) {
            case "COMPLETED" -> 55;
            case "REVIEWING" -> 48;
            case "IN_PROGRESS" -> 32;
            case "READY" -> 18;
            default -> 10;
        };
    }

    private int clampScore(int score) {
        return Math.max(0, Math.min(100, score));
    }

    private String levelFromScore(int score) {
        if (score >= 75) {
            return "stable";
        }
        if (score >= 55) {
            return "developing";
        }
        if (score >= 30) {
            return "emerging";
        }
        return "fragile";
    }

    private String strongestSignalForNode(int clearPracticeCount,
                                          int stuckPracticeCount,
                                          int clearExplainCount,
                                          int clearApplyCount,
                                          int clearRepairCount,
                                          int handoffClearCount,
                                          int handoffStuckCount,
                                          int solidReviewCount,
                                          int forgottenReviewCount,
                                          String latestDiagnosis,
                                          String progressStatus) {
        if (handoffClearCount > 0) {
            return "目标重构后的验证练习已经给出正向信号，说明这版新目标开始真正接住执行。";
        }
        if (handoffStuckCount > 0) {
            return "目标重构后的验证练习仍然卡住，说明这版新目标还没有真正接住执行。";
        }
        if (forgottenReviewCount > 0) {
            return "最近一次复盘已经暴露遗忘，说明这里还没有真正沉淀成长期记忆。";
        }
        if (solidReviewCount > 0) {
            return "最近一次复盘已经稳住，说明这里开始形成长期记忆。";
        }
        if (clearApplyCount > 0) {
            return "已经留下场景应用证据，说明理解开始转成能用的能力。";
        }
        if (clearExplainCount > 0) {
            return "已经留下清楚的解释证据，说明概念边界开始变得可表达。";
        }
        if (clearRepairCount > 0) {
            return "已经留下前置修复证据，说明基础缺口开始被真正回补。";
        }
        if (stuckPracticeCount > 0) {
            return "练习里仍然卡住，说明理解还没转成稳定应用。";
        }
        if (clearPracticeCount > 0) {
            return "练习输出已经比较清楚，开始形成可复用能力。";
        }
        if (CharSequenceUtil.equals(latestDiagnosis, "misconception")) {
            return "Tutor 最近判断这里更像概念边界误解。";
        }
        if (CharSequenceUtil.equals(latestDiagnosis, "needs_prereq")) {
            return "Tutor 最近判断这里还存在前置缺口。";
        }
        if (CharSequenceUtil.equals(progressStatus, "REVIEWING")) {
            return "节点已经进入复盘窗口。";
        }
        return "当前仍在积累更多掌握度证据。";
    }

    private String handoffValidationSignal(int handoffClearCount, int handoffStuckCount) {
        if (handoffClearCount > 0) {
            return "这轮节点已经通过目标重构后的首轮验证练习。";
        }
        if (handoffStuckCount > 0) {
            return "这轮节点在目标重构后的首轮验证练习里仍然卡住。";
        }
        return null;
    }

    private String resolveReviewState(String progressStatus, ReviewSignal latestReviewSignal) {
        if (latestReviewSignal != null) {
            if (CharSequenceUtil.equals(latestReviewSignal.rating(), "solid")) {
                return "最近复盘已稳住";
            }
            if (CharSequenceUtil.equals(latestReviewSignal.rating(), "forgotten")) {
                return "最近复盘暴露遗忘";
            }
            return "最近复盘想起来但不稳";
        }
        return CharSequenceUtil.equals(progressStatus, "REVIEWING") ? "待复盘" : "未进入复盘";
    }

    private String summarizePracticeDimension(int clearCount,
                                              int stretchCount,
                                              int stuckCount,
                                              String emptyLabel,
                                              String clearLabel,
                                              String stretchLabel,
                                              String stuckLabel) {
        if (clearCount > 0) {
            return clearLabel;
        }
        if (stuckCount > 0) {
            return stuckLabel;
        }
        if (stretchCount > 0) {
            return stretchLabel;
        }
        return emptyLabel;
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
                // ignore malformed assessment json and fall back to self rating
            }
        }
        return CharSequenceUtil.blankToDefault(attempt.getSelfRating(), "stretch");
    }

    private String humanizeReviewRating(String rating) {
        return switch (rating) {
            case "solid" -> "已经稳住了";
            case "forgotten" -> "这里忘了";
            default -> "想起来但不稳";
        };
    }

    private LocalDate toDate(LocalDateTime value) {
        return value == null ? null : value.toLocalDate();
    }

    private String formatDayLabel(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.getMonthValue() + "/" + date.getDayOfMonth();
    }

    private int capMinutes(Integer value, int fallback, int maxValue) {
        return Math.min(maxValue, Math.max(fallback, value == null ? fallback : value));
    }

    private int defaultInt(Integer value, int fallback) {
        return value == null ? fallback : value;
    }

    private void appendIfNotBlank(List<String> target, String value) {
        if (CharSequenceUtil.isNotBlank(value)) {
            target.add(value);
        }
    }

    private List<String> uniqueList(List<String> input, int maxSize) {
        return new ArrayList<>(new LinkedHashSet<>(CollUtil.emptyIfNull(input))).stream()
                .filter(CharSequenceUtil::isNotBlank)
                .limit(maxSize)
                .toList();
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

    private int compareDate(LocalDate left, LocalDate right) {
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

    private Long requireUserId() {
        Long userId = UserContextHolder.getUserId();
        LearningErrorEnum.USER_NOT_LOGGED_IN.assertNotNull(userId);
        return userId;
    }

    private record GoalLearningContext(Long userId,
                                       LearningGoalEntity goal,
                                       LearningMapNodeEntity currentNode,
                                       List<LearningMapNodeEntity> nodes,
                                       Map<Long, LearningNodeProgressEntity> progressByNodeId,
                                       Map<Long, TutorSignal> latestTutorByNodeId,
                                       Map<Long, TutorSignal> latestTutorBySessionId,
                                       List<PracticeSignal> practiceSignals,
                                       List<ReviewSignal> reviewSignals,
                                       List<GrowthSnapshotEntity> growthSnapshots,
                                       List<ReflectionSignal> reflectionSignals) {
    }

    private record TutorSignal(Long nodeId,
                               Long sessionId,
                               String diagnosis,
                               Integer turnNo,
                               LocalDateTime updatedAt) {
    }

    private record PracticeSignal(Long attemptId,
                                  Long nodeId,
                                  String evidenceKind,
                                  String rating,
                                  boolean handoffValidation,
                                  boolean completed,
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
                                    LocalDateTime updatedAt) {
    }

    private record GrowthHighlights(List<String> keyCognitiveChanges,
                                    List<String> commonStickingPoints) {
    }

    private record ActivityDay(Integer minutes,
                               LocalDateTime latestAt,
                               String source) {
    }

    private record WeeklyPlanMeta(String weeklyFocus,
                                  String weeklyPlanSummary,
                                  List<WeeklyPlanBucketBO> weeklyPlan) {
    }
}
