package io.github.module.learning.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.framework.core.context.UserContextHolder;
import io.github.module.learning.entity.DailyDigestEntity;
import io.github.module.learning.entity.LearningGoalEntity;
import io.github.module.learning.entity.LearningMapNodeEntity;
import io.github.module.learning.entity.PracticeAttemptEntity;
import io.github.module.learning.entity.ReviewAttemptEntity;
import io.github.module.learning.entity.TutorTurnEntity;
import io.github.module.learning.enums.LearningErrorEnum;
import io.github.module.learning.mapper.DailyDigestMapper;
import io.github.module.learning.mapper.PracticeAttemptMapper;
import io.github.module.learning.mapper.ReviewAttemptMapper;
import io.github.module.learning.mapper.TutorTurnMapper;
import io.github.module.learning.model.response.LearnerMemoryBO;
import io.github.module.learning.model.response.LearningAgentBO;
import io.github.module.learning.model.response.LearningKnowledgeGraphBO;
import io.github.module.learning.model.response.LearningPlanBO;
import io.github.module.learning.model.response.LearningPlanTaskBO;
import io.github.module.learning.model.response.LearningRhythmBO;
import io.github.module.learning.model.response.TodayLearningBO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 学习 Agent 服务端快照.
 */
@RequiredArgsConstructor
@Service
public class LearningAgentService {

    private final LearningGoalService learningGoalService;
    private final LearningPlanService learningPlanService;
    private final LearningGrowthService learningGrowthService;
    private final TutorTurnMapper tutorTurnMapper;
    private final PracticeAttemptMapper practiceAttemptMapper;
    private final ReviewAttemptMapper reviewAttemptMapper;
    private final DailyDigestMapper dailyDigestMapper;

    public LearningAgentBO getLearningAgent(Long goalId) {
        Long userId = requireUserId();
        LearningGoalEntity goal = learningGoalService.getOwnedGoalById(goalId, userId);
        LearningErrorEnum.INVALID_GOAL.assertNotNull(goal);

        TodayLearningBO today = learningGoalService.getToday();
        LearningPlanBO plan = learningPlanService.getCurrentPlan(goalId);
        LearnerMemoryBO memory = learningGrowthService.getLearnerMemory(goalId);
        LearningKnowledgeGraphBO knowledge = learningGrowthService.getLearningKnowledgeGraph(goalId);
        LearningRhythmBO rhythm = learningGrowthService.getLearningRhythm(goalId);
        AgentContext context = loadAgentContext(goalId, userId, goal, today);

        LatestEvidence latestEvidence = buildLatestEvidence(context);
        List<LearningAgentBO.InterventionBO> interventions = buildInterventions(
                context.today(),
                plan,
                rhythm,
                memory,
                knowledge,
                context.forgottenReviewCount(),
                latestEvidence);
        List<String> watchouts = buildWatchouts(memory, knowledge);
        List<String> carryOverNotes = buildCarryOverNotes(latestEvidence, memory, plan);

        String presenceTitle = buildPresenceTitle(plan, rhythm, memory, knowledge, context.forgottenReviewCount());
        String presenceSummary = buildPresenceSummary(plan, rhythm, memory, knowledge, context.forgottenReviewCount());
        String resumeTitle = latestEvidence == null
                ? "从当前学习主线继续"
                : "从 " + latestEvidence.source() + " 继续最省切换成本";
        String resumeSummary = latestEvidence == null
                ? CharSequenceUtil.blankToDefault(plan.getMissionSummary(), "系统已经整理好当前目标的主线，可以直接继续推进。")
                : latestEvidence.title() + " 是你最近一次留下的上下文，直接从这里续上最自然。";
        String reentryReason = buildReentryReason(plan, rhythm, memory, knowledge);
        String replanSummary = buildReplanSummary(plan, rhythm, memory);

        return LearningAgentBO.builder()
                .generatedAt(LocalDateTime.now())
                .mode("server")
                .presenceTitle(presenceTitle)
                .presenceSummary(presenceSummary)
                .urgentCount((int) interventions.stream()
                        .filter(item -> defaultInt(item.getPriority(), 0) >= 90)
                        .count())
                .resumeTitle(resumeTitle)
                .resumeSummary(resumeSummary)
                .replanSummary(replanSummary)
                .reentryReason(reentryReason)
                .nextTwoSteps(buildNextTwoSteps(plan, interventions))
                .carryOverNotes(carryOverNotes)
                .watchouts(watchouts)
                .interventions(interventions)
                .sceneNudges(buildSceneNudges(context.today(), plan, rhythm, knowledge))
                .build();
    }

    private AgentContext loadAgentContext(Long goalId,
                                          Long userId,
                                          LearningGoalEntity goal,
                                          TodayLearningBO today) {
        List<LearningMapNodeEntity> nodes = learningGoalService.listNodesByGoalId(goalId, userId);
        Map<Long, LearningMapNodeEntity> nodeById = nodes.stream()
                .filter(node -> node.getId() != null)
                .collect(Collectors.toMap(LearningMapNodeEntity::getId, Function.identity(), (left, right) -> left));
        TutorTurnEntity latestTutorTurn = tutorTurnMapper.selectOne(
                new QueryWrapper<TutorTurnEntity>()
                        .lambda()
                        .eq(TutorTurnEntity::getGoalId, goalId)
                        .eq(TutorTurnEntity::getUserId, userId)
                        .orderByDesc(TutorTurnEntity::getCreatedAt)
                        .orderByDesc(TutorTurnEntity::getTurnNo)
                        .last(" LIMIT 1")
        );
        PracticeAttemptEntity latestPracticeAttempt = practiceAttemptMapper.selectOne(
                new QueryWrapper<PracticeAttemptEntity>()
                        .lambda()
                        .eq(PracticeAttemptEntity::getGoalId, goalId)
                        .eq(PracticeAttemptEntity::getUserId, userId)
                        .eq(PracticeAttemptEntity::getCompleted, 1)
                        .orderByDesc(PracticeAttemptEntity::getUpdatedAt)
                        .last(" LIMIT 1")
        );
        List<ReviewAttemptEntity> completedReviewAttempts = reviewAttemptMapper.selectList(
                new QueryWrapper<ReviewAttemptEntity>()
                        .lambda()
                        .eq(ReviewAttemptEntity::getGoalId, goalId)
                        .eq(ReviewAttemptEntity::getUserId, userId)
                        .eq(ReviewAttemptEntity::getCompleted, 1)
                        .orderByDesc(ReviewAttemptEntity::getUpdatedAt)
        );
        ReviewAttemptEntity latestReviewAttempt = CollUtil.getFirst(completedReviewAttempts);
        DailyDigestEntity latestDigest = dailyDigestMapper.selectOne(
                new QueryWrapper<DailyDigestEntity>()
                        .lambda()
                        .eq(DailyDigestEntity::getGoalId, goalId)
                        .eq(DailyDigestEntity::getUserId, userId)
                        .orderByDesc(DailyDigestEntity::getCreatedAt)
                        .orderByDesc(DailyDigestEntity::getDigestDate)
                        .last(" LIMIT 1")
        );
        int forgottenReviewCount = (int) completedReviewAttempts.stream()
                .filter(item -> CharSequenceUtil.equals(item.getSelfRating(), "forgotten"))
                .count();

        return new AgentContext(
                goal,
                today == null || today.getGoal() == null || !Objects.equals(today.getGoal().getId(), goalId)
                        ? null
                        : today,
                nodeById,
                latestTutorTurn,
                latestPracticeAttempt,
                latestReviewAttempt,
                latestDigest,
                forgottenReviewCount
        );
    }

    private LatestEvidence buildLatestEvidence(AgentContext context) {
        List<LatestEvidence> candidates = new ArrayList<>();

        if (context.latestTutorTurn() != null) {
            LearningMapNodeEntity node = context.nodeById().get(context.latestTutorTurn().getMapNodeId());
            candidates.add(new LatestEvidence(
                    "Tutor",
                    node == null ? "最近 Tutor" : CharSequenceUtil.blankToDefault(node.getTitle(), "最近 Tutor"),
                    firstSuggestion(context.latestTutorTurn().getNextStepSuggestionsJson(),
                            "最近一轮 Tutor 已经留下下一步建议。"),
                    defaultIfNull(context.latestTutorTurn().getCreatedAt(), LocalDateTime.MIN)
            ));
        }

        if (context.latestPracticeAttempt() != null) {
            candidates.add(new LatestEvidence(
                    "Practice",
                    "最近练习",
                    CharSequenceUtil.equals(resolvePracticeRating(context.latestPracticeAttempt()), "stuck")
                            ? "最近一次练习被标记为“卡住”，最好尽快回补。"
                            : "最近一次练习已经完成，可以沿这个节点继续压实。",
                    defaultIfNull(context.latestPracticeAttempt().getUpdatedAt(), LocalDateTime.MIN)
            ));
        }

        if (context.latestReviewAttempt() != null) {
            candidates.add(new LatestEvidence(
                    "Review",
                    "最近复盘",
                    CharSequenceUtil.equals(context.latestReviewAttempt().getSelfRating(), "forgotten")
                            ? "最近一次复盘明确暴露了遗忘点。"
                            : "最近一次复盘已经留下可继续跟进的记忆信号。",
                    defaultIfNull(context.latestReviewAttempt().getUpdatedAt(), LocalDateTime.MIN)
            ));
        }

        if (context.latestDigest() != null) {
            candidates.add(new LatestEvidence(
                    "Reflection",
                    "今日反思",
                    CharSequenceUtil.blankToDefault(context.latestDigest().getNextAction(), "今天的 Reflection 已经写回系统。"),
                    defaultIfNull(context.latestDigest().getCreatedAt(), LocalDateTime.MIN)
            ));
        }

        return candidates.stream()
                .max((left, right) -> left.at().compareTo(right.at()))
                .orElse(null);
    }

    private List<LearningAgentBO.InterventionBO> buildInterventions(TodayLearningBO today,
                                                                    LearningPlanBO plan,
                                                                    LearningRhythmBO rhythm,
                                                                    LearnerMemoryBO memory,
                                                                    LearningKnowledgeGraphBO knowledge,
                                                                    int forgottenReviewCount,
                                                                    LatestEvidence latestEvidence) {
        List<LearningAgentBO.InterventionBO> interventions = new ArrayList<>();
        Long currentNodeId = today == null || today.getCurrentNode() == null ? null : today.getCurrentNode().getId();
        String currentNodeTitle = today == null || today.getCurrentNode() == null ? null : today.getCurrentNode().getTitle();

        if (CharSequenceUtil.equals(memory == null ? null : memory.getGoalValidationStatus(), "at_risk")) {
            interventions.add(intervention(
                    "agent-handoff-repair",
                    "recover",
                    "先修这轮目标重构后的验证断点",
                    firstNonBlank(memory == null ? null : memory.getGoalValidationSummary(),
                            "这次目标重构后的验证练习仍然没有真正接住执行。"),
                    "系统已经看到这次目标重构还没有真正改善执行，继续盲推主线会放大偏差。",
                    "practice",
                    currentNodeId,
                    currentNodeTitle,
                    108
            ));
        }

        if (CharSequenceUtil.equals(memory == null ? null : memory.getGoalValidationStatus(), "improving")) {
            interventions.add(intervention(
                    "agent-handoff-continue",
                    "resume",
                    "沿这版新目标继续压实第二轮证据",
                    firstNonBlank(memory == null ? null : memory.getGoalValidationSummary(),
                            "系统已经看到新目标版本开始改善真实执行。"),
                    "这次调整已经开始改善执行，现在最值得做的是继续把正向信号压成更稳定的证据。",
                    resolvePlanTargetSection(plan, "practice"),
                    resolvePlanNodeId(plan, currentNodeId),
                    resolvePlanNodeTitle(plan, currentNodeTitle),
                    104
            ));
        }

        if (rhythm != null && !Boolean.TRUE.equals(rhythm.getTodayDone())) {
            interventions.add(intervention(
                    "agent-recover-today",
                    "recover",
                    "先把今天的学习重新接上",
                    firstNonBlank(plan == null ? null : plan.getReplanReason(),
                            plan == null ? null : plan.getRecoveryNote(),
                            "今天先完成一个最小学习闭环，让节奏不要继续断开。"),
                    "Rhythm 判断今天还没有新的学习动作写回系统。",
                    resolvePlanTargetSection(plan, "today"),
                    resolvePlanNodeId(plan, currentNodeId),
                    resolvePlanNodeTitle(plan, currentNodeTitle),
                    100
            ));
        }

        if (forgottenReviewCount > 0 || CollUtil.isNotEmpty(plan == null ? null : plan.getReviewQueue())) {
            interventions.add(intervention(
                    "agent-review-backlog",
                    "review",
                    "优先回收已经开始遗忘的内容",
                    firstNonBlank(CollUtil.getFirst(plan == null ? null : plan.getReviewQueue()),
                            forgottenReviewCount > 0
                                    ? "当前已有 " + forgottenReviewCount + " 个复盘任务暴露了真实遗忘。"
                                    : "当前已经有节点进入复盘窗口。"),
                    "Review 信号显示先回收旧内容，比继续开新内容更划算。",
                    "review",
                    currentNodeId,
                    currentNodeTitle,
                    forgottenReviewCount > 0 ? 96 : 84
            ));
        }

        if (knowledge != null && CharSequenceUtil.isNotBlank(CollUtil.getFirst(knowledge.getWeakAreas()))) {
            interventions.add(intervention(
                    "agent-clarify-knowledge",
                    "clarify",
                    "先修正当前概念网络里最薄的连接",
                    CollUtil.getFirst(knowledge.getWeakAreas()),
                    "Knowledge Graph 已经识别出当前最需要补的概念关系。",
                    "knowledge",
                    knowledge.getFocusNodeId(),
                    knowledge.getFocusNodeTitle(),
                    90
            ));
        }

        if (today != null && !Boolean.TRUE.equals(today.getReflectedToday())) {
            interventions.add(intervention(
                    "agent-reflect-close",
                    "reflect",
                    "学习结束前把今天封箱",
                    firstNonBlank(CollUtil.getFirst(memory == null ? null : memory.getRecommendedAdjustments()),
                            "补一条 Reflection，明天系统才能继续读懂你的学习状态。"),
                    "今天还没有 Reflection，这会让很多学习信号停留在临时状态。",
                    "reflection",
                    currentNodeId,
                    currentNodeTitle,
                    82
            ));
        }

        if (interventions.isEmpty() && plan != null && CollUtil.isNotEmpty(plan.getTasks())) {
            LearningPlanTaskBO task = plan.getTasks().getFirst();
            interventions.add(intervention(
                    "agent-resume-main",
                    "resume",
                    "继续「" + task.getTitle() + "」",
                    task.getSummary(),
                    "当前没有更高优先级的回补任务，适合沿主线继续推进。",
                    task.getTargetSection(),
                    task.getNodeId(),
                    task.getNodeTitle(),
                    78
            ));
        }

        if (interventions.isEmpty() && latestEvidence != null && CharSequenceUtil.equals(latestEvidence.source(), "Tutor")) {
            interventions.add(intervention(
                    "agent-resume-tutor",
                    "resume",
                    "从最近 Tutor 上下文继续",
                    latestEvidence.summary(),
                    "你已经在这个节点上打开了上下文，继续推进成本最低。",
                    "tutor",
                    currentNodeId,
                    currentNodeTitle,
                    72
            ));
        }

        return uniqueById(interventions.stream()
                .sorted((left, right) -> Integer.compare(defaultInt(right.getPriority(), 0), defaultInt(left.getPriority(), 0)))
                .toList());
    }

    private String buildPresenceTitle(LearningPlanBO plan,
                                      LearningRhythmBO rhythm,
                                      LearnerMemoryBO memory,
                                      LearningKnowledgeGraphBO knowledge,
                                      int forgottenReviewCount) {
        if (CharSequenceUtil.equals(memory == null ? null : memory.getGoalValidationStatus(), "at_risk")) {
            return "我建议尽早再次校正这版新目标";
        }
        if (CharSequenceUtil.equals(memory == null ? null : memory.getGoalValidationStatus(), "improving")) {
            return "这次目标重构开始见效了，我会帮你继续压实";
        }
        if (plan != null && CharSequenceUtil.equals(plan.getPaceStatus(), "behind")) {
            return "我先帮你把这轮计划重新排顺";
        }
        if (rhythm != null && !Boolean.TRUE.equals(rhythm.getTodayDone())) {
            return "我先帮你把今天重新接上";
        }
        if (forgottenReviewCount > 0) {
            return "我建议先回收已经开始遗忘的内容";
        }
        if (knowledge != null && CharSequenceUtil.isNotBlank(CollUtil.getFirst(knowledge.getWeakAreas()))) {
            return "我建议先修正当前概念网络里的薄弱连接";
        }
        return "我会继续陪你沿当前主线推进";
    }

    private String buildPresenceSummary(LearningPlanBO plan,
                                        LearningRhythmBO rhythm,
                                        LearnerMemoryBO memory,
                                        LearningKnowledgeGraphBO knowledge,
                                        int forgottenReviewCount) {
        if (CharSequenceUtil.equals(memory == null ? null : memory.getGoalValidationStatus(), "at_risk")) {
            return firstNonBlank(memory == null ? null : memory.getGoalValidationSummary(),
                    "这次目标重构后的验证练习仍然卡住，说明当前更适合修复或重新缩焦。");
        }
        if (CharSequenceUtil.equals(memory == null ? null : memory.getGoalValidationStatus(), "improving")) {
            return firstNonBlank(memory == null ? null : memory.getGoalValidationSummary(),
                    "系统已经看到新目标版本带来的正向执行信号，现在适合沿新主线继续形成第二轮证据。");
        }
        if (plan != null && CharSequenceUtil.equals(plan.getPaceStatus(), "behind")) {
            return firstNonBlank(plan.getReplanReason(), plan.getRecoveryNote(),
                    "系统已经开始主动重排这轮计划，重点是恢复连续性与兑现节奏。");
        }
        if (rhythm != null && !Boolean.TRUE.equals(rhythm.getTodayDone())) {
            return "今天还没有新的学习动作写回系统。现在最重要的不是加更多内容，而是先把动量接回来。";
        }
        if (forgottenReviewCount > 0) {
            return "当前已经出现真实遗忘信号。先补回旧内容，会比继续开新内容更稳。";
        }
        if (knowledge != null && CharSequenceUtil.isNotBlank(CollUtil.getFirst(knowledge.getWeakAreas()))) {
            return "图谱已经看到哪里还没真正连上。现在优先补这块，会让后续学习更省力。";
        }
        return "系统已经开始读懂你的节奏、图谱和记忆信号，现在重点是减少切换成本，让下一步尽量自然发生。";
    }

    private String buildReplanSummary(LearningPlanBO plan,
                                      LearningRhythmBO rhythm,
                                      LearnerMemoryBO memory) {
        if (rhythm != null && !Boolean.TRUE.equals(rhythm.getTodayDone())) {
            return firstNonBlank(plan == null ? null : plan.getReplanReason(),
                    rhythm.getRecoveryPlan(),
                    "今天先补一个最小闭环，明天再继续扩张主线。");
        }
        if (CharSequenceUtil.equals(memory == null ? null : memory.getGoalValidationStatus(), "at_risk")) {
            return firstNonBlank(memory == null ? null : memory.getGoalValidationSummary(),
                    "如果后面两轮验证还继续卡住，就不要只补任务，应该尽早再次触发目标重构。");
        }
        if (CharSequenceUtil.equals(memory == null ? null : memory.getGoalValidationStatus(), "improving")) {
            return firstNonBlank(memory == null ? null : memory.getGoalValidationSummary(),
                    "这次调整已经开始见效，下一步要尽快形成第二轮验证练习和 Reflection 证据。");
        }
        return firstNonBlank(plan == null ? null : plan.getReplanReason(),
                plan == null ? null : plan.getRecoveryNote(),
                "如果今天推进顺利，明天继续沿当前节点推进；如果出现卡点，就优先把它写回 Reflection 和 Review。");
    }

    private String buildReentryReason(LearningPlanBO plan,
                                      LearningRhythmBO rhythm,
                                      LearnerMemoryBO memory,
                                      LearningKnowledgeGraphBO knowledge) {
        if (CharSequenceUtil.equals(memory == null ? null : memory.getGoalValidationStatus(), "at_risk")) {
            return firstNonBlank(memory == null ? null : memory.getGoalValidationSummary(),
                    "系统已经看到这次目标重构后的验证仍然卡住，所以现在更适合重新接回修复动作。");
        }
        if (rhythm != null && !Boolean.TRUE.equals(rhythm.getTodayDone())) {
            return firstNonBlank(rhythm.getRecoveryPlan(),
                    "今天先接回一个最小动作，避免学习节奏继续断开。");
        }
        return firstNonBlank(
                knowledge == null ? null : CollUtil.getFirst(knowledge.getWeakPaths()),
                knowledge == null ? null : CollUtil.getFirst(knowledge.getWeakAreas()),
                plan == null ? null : plan.getReplanReason(),
                "现在已经有足够多的系统信号能判断最值得先接回哪一步。");
    }

    private List<LearningAgentBO.AgendaStepBO> buildNextTwoSteps(LearningPlanBO plan,
                                                                 List<LearningAgentBO.InterventionBO> interventions) {
        List<LearningAgentBO.AgendaStepBO> steps = new ArrayList<>();
        if (CollUtil.isNotEmpty(interventions)) {
            LearningAgentBO.InterventionBO first = interventions.getFirst();
            steps.add(LearningAgentBO.AgendaStepBO.builder()
                    .id("agenda-" + first.getId())
                    .title(first.getTitle())
                    .summary(first.getSummary())
                    .targetSection(first.getTargetSection())
                    .nodeId(first.getNodeId())
                    .nodeTitle(first.getNodeTitle())
                    .build());
        }
        if (plan != null && CollUtil.isNotEmpty(plan.getTasks())) {
            LearningPlanTaskBO task = plan.getTasks().size() > 1 ? plan.getTasks().get(1) : plan.getTasks().getFirst();
            steps.add(LearningAgentBO.AgendaStepBO.builder()
                    .id("agenda-plan-" + task.getId())
                    .title(task.getTitle())
                    .summary(task.getSummary())
                    .targetSection(task.getTargetSection())
                    .nodeId(task.getNodeId())
                    .nodeTitle(task.getNodeTitle())
                    .build());
        }
        return uniqueAgendaSteps(steps);
    }

    private List<String> buildCarryOverNotes(LatestEvidence latestEvidence,
                                             LearnerMemoryBO memory,
                                             LearningPlanBO plan) {
        List<String> notes = new ArrayList<>();
        if (latestEvidence != null) {
            notes.add("最近上下文来自 " + latestEvidence.source() + "：" + latestEvidence.summary());
        }
        if (CharSequenceUtil.isNotBlank(memory == null ? null : memory.getGoalValidationSummary())) {
            notes.add("目标重构验证：" + memory.getGoalValidationSummary());
        }
        notes.addAll(CollUtil.sub(CollUtil.emptyIfNull(plan == null ? null : plan.getCarryOverQueue()), 0, 2));
        if (CollUtil.isNotEmpty(plan == null ? null : plan.getReviewQueue())) {
            notes.add(plan.getReviewQueue().getFirst());
        }
        notes.addAll(CollUtil.sub(CollUtil.emptyIfNull(memory == null ? null : memory.getRecommendedAdjustments()), 0, 2));
        return uniqueStrings(notes, 4);
    }

    private List<String> buildWatchouts(LearnerMemoryBO memory,
                                        LearningKnowledgeGraphBO knowledge) {
        List<String> items = new ArrayList<>();
        if (CharSequenceUtil.equals(memory == null ? null : memory.getGoalValidationStatus(), "at_risk")
                && CharSequenceUtil.isNotBlank(memory.getGoalValidationSummary())) {
            items.add(memory.getGoalValidationSummary());
        }
        items.addAll(CollUtil.sub(CollUtil.emptyIfNull(knowledge == null ? null : knowledge.getWeakAreas()), 0, 2));
        items.addAll(CollUtil.sub(CollUtil.emptyIfNull(memory == null ? null : memory.getWeakSignals()), 0, 2));
        return uniqueStrings(items, 4);
    }

    private List<LearningAgentBO.SceneNudgeBO> buildSceneNudges(TodayLearningBO today,
                                                                LearningPlanBO plan,
                                                                LearningRhythmBO rhythm,
                                                                LearningKnowledgeGraphBO knowledge) {
        String currentNodeTitle = today == null || today.getCurrentNode() == null
                ? "当前节点"
                : CharSequenceUtil.blankToDefault(today.getCurrentNode().getTitle(), "当前节点");
        return List.of(
                LearningAgentBO.SceneNudgeBO.builder()
                        .id("scene-short-time")
                        .contextLabel("只剩 20 分钟时")
                        .prompt(firstNonBlank(
                                rhythm == null ? null : rhythm.getRecoveryPlan(),
                                plan == null ? null : plan.getRecoveryNote(),
                                "先完成一个主任务，再留下 5 分钟 Reflection，不要重新开一条新线。"))
                        .build(),
                LearningAgentBO.SceneNudgeBO.builder()
                        .id("scene-reading")
                        .contextLabel("打开资料或课程时")
                        .prompt(knowledge != null && CharSequenceUtil.isNotBlank(knowledge.getFocusNodeTitle())
                                ? "先带着「" + knowledge.getFocusNodeTitle() + " 要怎么验证」这个问题去看，不要漫游式浏览。"
                                : "先明确这次是为理解、验证还是回忆，再开始看资料。")
                        .build(),
                LearningAgentBO.SceneNudgeBO.builder()
                        .id("scene-practice")
                        .contextLabel("准备写练习或代码时")
                        .prompt(knowledge != null && CharSequenceUtil.isNotBlank(CollUtil.getFirst(knowledge.getWeakAreas()))
                                ? "优先围绕「" + knowledge.getWeakAreas().getFirst() + "」做最小验证，不要一上来做太大题目。"
                                : "直接围绕「" + currentNodeTitle + "」做一个最小可验证输出，先把理解压成证据。")
                        .build(),
                LearningAgentBO.SceneNudgeBO.builder()
                        .id("scene-close")
                        .contextLabel("准备结束今天学习时")
                        .prompt(today != null && Boolean.TRUE.equals(today.getReflectedToday())
                                ? "离开前回看一下今天的主任务和卡点，确认明天第一步是什么。"
                                : "离开前先补上 Reflection，把今天的理解、误解和下一步动作写回系统。")
                        .build()
        );
    }

    private LearningAgentBO.InterventionBO intervention(String id,
                                                        String kind,
                                                        String title,
                                                        String summary,
                                                        String whyNow,
                                                        String targetSection,
                                                        Long nodeId,
                                                        String nodeTitle,
                                                        Integer priority) {
        return LearningAgentBO.InterventionBO.builder()
                .id(id)
                .kind(kind)
                .title(title)
                .summary(summary)
                .whyNow(whyNow)
                .targetSection(targetSection)
                .nodeId(nodeId)
                .nodeTitle(nodeTitle)
                .priority(priority)
                .build();
    }

    private List<LearningAgentBO.InterventionBO> uniqueById(List<LearningAgentBO.InterventionBO> items) {
        Set<String> seen = new LinkedHashSet<>();
        return CollUtil.emptyIfNull(items).stream()
                .filter(item -> CharSequenceUtil.isNotBlank(item.getId()))
                .filter(item -> seen.add(item.getId()))
                .toList();
    }

    private List<LearningAgentBO.AgendaStepBO> uniqueAgendaSteps(List<LearningAgentBO.AgendaStepBO> items) {
        Set<String> seen = new LinkedHashSet<>();
        return CollUtil.emptyIfNull(items).stream()
                .filter(item -> CharSequenceUtil.isNotBlank(item.getId()))
                .filter(item -> seen.add(item.getId()))
                .limit(2)
                .toList();
    }

    private List<String> uniqueStrings(List<String> items, int maxSize) {
        return new ArrayList<>(new LinkedHashSet<>(CollUtil.emptyIfNull(items))).stream()
                .filter(CharSequenceUtil::isNotBlank)
                .limit(maxSize)
                .toList();
    }

    private String resolvePracticeRating(PracticeAttemptEntity attempt) {
        if (attempt == null) {
            return "stretch";
        }
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

    private String firstSuggestion(String json, String fallback) {
        if (CharSequenceUtil.isBlank(json)) {
            return fallback;
        }
        try {
            List<String> suggestions = JSONUtil.toList(JSONUtil.parseArray(json), String.class);
            return firstNonBlank(CollUtil.getFirst(suggestions), fallback);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (CharSequenceUtil.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private String resolvePlanTargetSection(LearningPlanBO plan, String fallback) {
        if (plan == null || CollUtil.isEmpty(plan.getTasks()) || CharSequenceUtil.isBlank(plan.getTasks().getFirst().getTargetSection())) {
            return fallback;
        }
        return plan.getTasks().getFirst().getTargetSection();
    }

    private Long resolvePlanNodeId(LearningPlanBO plan, Long fallback) {
        if (plan == null || CollUtil.isEmpty(plan.getTasks()) || plan.getTasks().getFirst().getNodeId() == null) {
            return fallback;
        }
        return plan.getTasks().getFirst().getNodeId();
    }

    private String resolvePlanNodeTitle(LearningPlanBO plan, String fallback) {
        if (plan == null || CollUtil.isEmpty(plan.getTasks()) || CharSequenceUtil.isBlank(plan.getTasks().getFirst().getNodeTitle())) {
            return fallback;
        }
        return plan.getTasks().getFirst().getNodeTitle();
    }

    private int defaultInt(Integer value, int fallback) {
        return value == null ? fallback : value;
    }

    private LocalDateTime defaultIfNull(LocalDateTime value, LocalDateTime fallback) {
        return value == null ? fallback : value;
    }

    private Long requireUserId() {
        Long userId = UserContextHolder.getUserId();
        LearningErrorEnum.USER_NOT_LOGGED_IN.assertNotNull(userId);
        return userId;
    }

    private record AgentContext(LearningGoalEntity goal,
                                TodayLearningBO today,
                                Map<Long, LearningMapNodeEntity> nodeById,
                                TutorTurnEntity latestTutorTurn,
                                PracticeAttemptEntity latestPracticeAttempt,
                                ReviewAttemptEntity latestReviewAttempt,
                                DailyDigestEntity latestDigest,
                                int forgottenReviewCount) {
    }

    private record LatestEvidence(String source,
                                  String title,
                                  String summary,
                                  LocalDateTime at) {
    }
}
