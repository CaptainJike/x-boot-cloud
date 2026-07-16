package io.github.module.learning.service;

import io.github.framework.core.context.UserContext;
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
import io.github.module.learning.mapper.DailyDigestMapper;
import io.github.module.learning.mapper.GrowthSnapshotMapper;
import io.github.module.learning.mapper.PracticeAttemptMapper;
import io.github.module.learning.mapper.PracticeTaskMapper;
import io.github.module.learning.mapper.ReviewAttemptMapper;
import io.github.module.learning.mapper.TutorTurnMapper;
import io.github.module.learning.model.response.LearnerMemoryBO;
import io.github.module.learning.model.response.MasteryRecordBO;
import io.github.module.learning.model.response.GrowthTimelineBO;
import io.github.module.learning.model.response.LearningKnowledgeGraphBO;
import io.github.module.learning.model.response.LearningRhythmBO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LearningGrowthServiceTest {

    @Mock
    private GrowthSnapshotMapper growthSnapshotMapper;

    @Mock
    private LearningEventReadService learningEventReadService;

    @Mock
    private PracticeTaskMapper practiceTaskMapper;

    @Mock
    private PracticeAttemptMapper practiceAttemptMapper;

    @Mock
    private ReviewAttemptMapper reviewAttemptMapper;

    @Mock
    private TutorTurnMapper tutorTurnMapper;

    @Mock
    private DailyDigestMapper dailyDigestMapper;

    @Mock
    private LearningGoalService learningGoalService;

    private LearningGrowthService learningGrowthService;

    @BeforeEach
    void setUp() {
        UserContextHolder.setUserContext(new UserContext()
                .setUserId(9L)
                .setUserName("learner"));
        learningGrowthService = new LearningGrowthService(
                growthSnapshotMapper,
                practiceTaskMapper,
                practiceAttemptMapper,
                reviewAttemptMapper,
                tutorTurnMapper,
                dailyDigestMapper,
                learningGoalService,
                learningEventReadService,
                new LearningAssembler());
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void getMasteryRecordsAggregatesTutorAndPracticeSignals() {
        LearningGoalEntity goal = goal();
        LearningMapNodeEntity node = node();
        LearningNodeProgressEntity progress = LearningNodeProgressEntity.builder()
                .goalId(10L)
                .mapNodeId(20L)
                .userId(9L)
                .status("REVIEWING")
                .masteryLevel(40)
                .lastDiagnosis("ready")
                .updatedAt(LocalDateTime.of(2026, 7, 15, 10, 0))
                .build();
        PracticeTaskEntity explanationTask = practiceTask("explanation");
        PracticeAttemptEntity verifiedExplanation = practiceAttempt("verified", 0, LocalDateTime.of(2026, 7, 15, 11, 0));
        TutorTurnEntity latestTutorTurn = tutorTurn("ready", 1L, LocalDateTime.of(2026, 7, 15, 9, 30));

        mockGoalContext(goal, node, progress);
        when(practiceTaskMapper.selectList(any())).thenReturn(List.of(explanationTask));
        when(practiceAttemptMapper.selectList(any())).thenReturn(List.of(verifiedExplanation));
        when(tutorTurnMapper.selectList(any())).thenReturn(List.of(latestTutorTurn));
        when(growthSnapshotMapper.selectList(any())).thenReturn(List.of());
        when(dailyDigestMapper.selectList(any())).thenReturn(List.of());
        when(learningEventReadService.loadGoalSnapshot(10L, 9L)).thenReturn(emptyEventSnapshot());

        List<MasteryRecordBO> records = learningGrowthService.getMasteryRecords(10L);

        assertThat(records).hasSize(1);
        MasteryRecordBO record = records.getFirst();
        assertThat(record.getNodeId()).isEqualTo(20L);
        assertThat(record.getMasteryScore()).isEqualTo(60);
        assertThat(record.getMasteryLevel()).isEqualTo("developing");
        assertThat(record.getStrongestSignal()).contains("解释证据");
        assertThat(record.getExplanationStatus()).isEqualTo("解释已经比较清楚");
        assertThat(record.getReviewState()).isEqualTo("待复盘");
    }

    @Test
    void getMasteryRecordsPrefersUnifiedPracticeEventOverOlderRawAttempt() {
        LearningGoalEntity goal = goal();
        LearningMapNodeEntity node = node();
        LearningNodeProgressEntity progress = LearningNodeProgressEntity.builder()
                .goalId(10L)
                .mapNodeId(20L)
                .userId(9L)
                .status("REVIEWING")
                .masteryLevel(40)
                .lastDiagnosis("ready")
                .updatedAt(LocalDateTime.of(2026, 7, 15, 10, 0))
                .build();
        PracticeTaskEntity explanationTask = practiceTask("explanation");
        PracticeAttemptEntity olderRawAttempt = practiceAttempt("partial", 0, LocalDateTime.of(2026, 7, 15, 10, 30));

        mockGoalContext(goal, node, progress);
        when(practiceTaskMapper.selectList(any())).thenReturn(List.of(explanationTask));
        when(practiceAttemptMapper.selectList(any())).thenReturn(List.of(olderRawAttempt));
        when(tutorTurnMapper.selectList(any())).thenReturn(List.of());
        when(growthSnapshotMapper.selectList(any())).thenReturn(List.of());
        when(dailyDigestMapper.selectList(any())).thenReturn(List.of());
        when(learningEventReadService.loadGoalSnapshot(10L, 9L)).thenReturn(new LearningEventReadService.GoalEventSnapshot(
                List.of(),
                java.util.Map.of(),
                java.util.Map.of(),
                java.util.Map.of(200L, new LearningEventReadService.PracticeAttemptEventProjection(
                        200L,
                        20L,
                        "clear",
                        "apply",
                        "explanation",
                        15,
                        false,
                        true,
                        LocalDateTime.of(2026, 7, 15, 11, 0))),
                java.util.Map.of(),
                java.util.Map.of()));

        List<MasteryRecordBO> records = learningGrowthService.getMasteryRecords(10L);

        assertThat(records).hasSize(1);
        MasteryRecordBO record = records.getFirst();
        assertThat(record.getMasteryScore()).isEqualTo(60);
        assertThat(record.getStrongestSignal()).contains("解释证据");
        assertThat(record.getExplanationStatus()).isEqualTo("解释已经比较清楚");
    }

    @Test
    void getLearnerMemoryBuildsServerSnapshotFromPersistedSignals() {
        LearningGoalEntity goal = goal();
        LearningMapNodeEntity node = node();
        LearningNodeProgressEntity progress = LearningNodeProgressEntity.builder()
                .goalId(10L)
                .mapNodeId(20L)
                .userId(9L)
                .status("COMPLETED")
                .masteryLevel(85)
                .lastDiagnosis("needs_prereq")
                .updatedAt(LocalDateTime.of(2026, 7, 15, 10, 0))
                .build();
        PracticeTaskEntity handoffTask = practiceTask("goal_validation");
        PracticeAttemptEntity handoffAttempt = practiceAttempt("verified", 1, LocalDateTime.of(2026, 7, 15, 11, 0));
        TutorTurnEntity latestTutorTurn = tutorTurn("needs_prereq", 2L, LocalDateTime.of(2026, 7, 15, 9, 0));
        GrowthSnapshotEntity cognition = GrowthSnapshotEntity.builder()
                .userId(9L)
                .goalId(10L)
                .snapshotDate(LocalDate.of(2026, 7, 15))
                .eventType("COGNITION")
                .title("认知变化")
                .summary("开始把概念边界写成可验证证据")
                .build();
        GrowthSnapshotEntity stuck = GrowthSnapshotEntity.builder()
                .userId(9L)
                .goalId(10L)
                .snapshotDate(LocalDate.of(2026, 7, 15))
                .eventType("STUCK")
                .title("常见卡点")
                .summary("容易把理解当成掌握")
                .build();
        DailyDigestEntity digest = DailyDigestEntity.builder()
                .userId(9L)
                .goalId(10L)
                .reflectionEntryId(400L)
                .digestDate(LocalDate.now())
                .summary("今天已经把调用链补成了可验证闭环。")
                .nextAction("明天先做一轮迁移练习。")
                .createdAt(LocalDateTime.of(2026, 7, 15, 20, 0))
                .build();

        mockGoalContext(goal, node, progress);
        when(practiceTaskMapper.selectList(any())).thenReturn(List.of(handoffTask));
        when(practiceAttemptMapper.selectList(any())).thenReturn(List.of(handoffAttempt));
        when(reviewAttemptMapper.selectList(any())).thenReturn(List.of(
                reviewAttempt(300L, 20L, "forgotten", 1, LocalDateTime.of(2026, 7, 15, 18, 0))));
        when(tutorTurnMapper.selectList(any())).thenReturn(List.of(latestTutorTurn));
        when(growthSnapshotMapper.selectList(any())).thenReturn(List.of(cognition, stuck));
        when(dailyDigestMapper.selectList(any())).thenReturn(List.of(digest));
        when(learningEventReadService.loadGoalSnapshot(10L, 9L)).thenReturn(emptyEventSnapshot());

        LearnerMemoryBO memory = learningGrowthService.getLearnerMemory(10L);

        assertThat(memory.getMode()).isEqualTo("server");
        assertThat(memory.getGoalValidationStatus()).isEqualTo("improving");
        assertThat(memory.getStrengths()).anyMatch(item -> item.contains("目标重构后的验证练习"));
        assertThat(memory.getWeakSignals()).anyMatch(item -> item.contains("暴露遗忘"));
        assertThat(memory.getEvidence()).extracting(LearnerMemoryBO.EvidenceBO::getLabel)
                .contains("Reflection", "Goal handoff", "Review");
        assertThat(memory.getRecommendedAdjustments()).anyMatch(item -> item.contains("迁移练习"));
    }

    @Test
    void getTimelineMergesUnifiedLearningEventsWithGrowthSnapshots() {
        LearningEventEntity practiceEvent = LearningEventEntity.builder()
                .userId(9L)
                .goalId(10L)
                .eventSource("PRACTICE")
                .eventType("ATTEMPT_SAVED")
                .eventStatus("COMPLETED")
                .title("完成练习任务")
                .summary("围绕当前节点完成了一次应用练习。")
                .eventAt(LocalDateTime.of(2026, 7, 15, 21, 30))
                .build();
        GrowthSnapshotEntity cognition = GrowthSnapshotEntity.builder()
                .userId(9L)
                .goalId(10L)
                .snapshotDate(LocalDate.of(2026, 7, 15))
                .eventType("COGNITION")
                .title("认知变化")
                .summary("开始把练习结果写回长期记忆。")
                .createdAt(LocalDateTime.of(2026, 7, 15, 22, 0))
                .build();
        when(learningEventReadService.loadUserEvents(9L)).thenReturn(List.of(practiceEvent));
        when(growthSnapshotMapper.selectList(any())).thenReturn(List.of(cognition));

        GrowthTimelineBO timeline = learningGrowthService.getTimeline();

        assertThat(timeline.getItems()).hasSize(2);
        assertThat(timeline.getItems().getFirst().getEventType()).isEqualTo("COGNITION");
        assertThat(timeline.getItems().get(1).getEventType()).isEqualTo("PRACTICE");
        assertThat(timeline.getItems().get(1).getRecordedAt()).isEqualTo(LocalDateTime.of(2026, 7, 15, 21, 30));
    }

    @Test
    void getLearnerMemoryUsesUnifiedReflectionEventWhenDigestTableIsEmpty() {
        LearningGoalEntity goal = goal();
        LearningMapNodeEntity node = node();
        LearningNodeProgressEntity progress = LearningNodeProgressEntity.builder()
                .goalId(10L)
                .mapNodeId(20L)
                .userId(9L)
                .status("IN_PROGRESS")
                .masteryLevel(35)
                .updatedAt(LocalDateTime.of(2026, 7, 15, 10, 0))
                .build();

        mockGoalContext(goal, node, progress);
        when(practiceTaskMapper.selectList(any())).thenReturn(List.of());
        when(practiceAttemptMapper.selectList(any())).thenReturn(List.of());
        when(tutorTurnMapper.selectList(any())).thenReturn(List.of());
        when(growthSnapshotMapper.selectList(any())).thenReturn(List.of());
        when(dailyDigestMapper.selectList(any())).thenReturn(List.of());
        when(learningEventReadService.loadGoalSnapshot(10L, 9L)).thenReturn(new LearningEventReadService.GoalEventSnapshot(
                List.of(),
                java.util.Map.of(),
                java.util.Map.of(),
                java.util.Map.of(),
                java.util.Map.of(),
                java.util.Map.of(400L, new LearningEventReadService.ReflectionEventProjection(
                        400L,
                        "今天通过统一事件流写下了一次 Reflection。",
                        "明天继续做一轮迁移练习。",
                        LocalDate.now(),
                        LocalDateTime.of(2026, 7, 15, 21, 30)))));

        LearnerMemoryBO memory = learningGrowthService.getLearnerMemory(10L);

        assertThat(memory.getEvidence()).anyMatch(item ->
                item.getLabel().equals("Reflection") && item.getDetail().contains("统一事件流"));
        assertThat(memory.getRecommendedAdjustments()).anyMatch(item -> item.contains("迁移练习"));
    }

    @Test
    void getLearningKnowledgeGraphBuildsServerSnapshotFromPersistedSignals() {
        LearningGoalEntity goal = goal();
        LearningMapNodeEntity prerequisiteNode = LearningMapNodeEntity.builder()
                .id(19L)
                .goalId(10L)
                .userId(9L)
                .nodeCode("java-client-basics")
                .title("Java 客户端基础")
                .verificationMethod("解释客户端调用和响应处理链。")
                .sortOrder(1)
                .build();
        LearningMapNodeEntity currentNode = LearningMapNodeEntity.builder()
                .id(20L)
                .goalId(10L)
                .userId(9L)
                .nodeCode("spring-ai-basics")
                .title("Spring AI 基础")
                .verificationMethod("说明 ChatModel、Prompt 与消息链之间的关系。")
                .prerequisiteNodeCodes("java-client-basics")
                .sortOrder(2)
                .build();
        LearningNodeProgressEntity currentProgress = LearningNodeProgressEntity.builder()
                .goalId(10L)
                .mapNodeId(20L)
                .userId(9L)
                .status("REVIEWING")
                .masteryLevel(55)
                .updatedAt(LocalDateTime.of(2026, 7, 15, 10, 0))
                .build();
        PracticeTaskEntity currentPracticeTask = PracticeTaskEntity.builder()
                .id(100L)
                .goalId(10L)
                .userId(9L)
                .mapNodeId(20L)
                .taskKey("practice-20")
                .taskType("apply")
                .evidenceKind("application")
                .build();
        PracticeAttemptEntity currentPracticeAttempt = practiceAttempt("verified", 0, LocalDateTime.of(2026, 7, 15, 11, 0));
        TutorTurnEntity latestTutorTurn = tutorTurn("ready", 1L, LocalDateTime.of(2026, 7, 15, 9, 30));
        GrowthSnapshotEntity stuck = GrowthSnapshotEntity.builder()
                .userId(9L)
                .goalId(10L)
                .snapshotDate(LocalDate.of(2026, 7, 15))
                .eventType("STUCK")
                .title("常见卡点")
                .summary("前置理解还没有彻底压实")
                .build();

        when(learningGoalService.getOwnedGoalById(10L, 9L)).thenReturn(goal);
        when(learningGoalService.getCurrentNode(goal)).thenReturn(currentNode);
        when(learningGoalService.listNodesByGoalId(10L, 9L)).thenReturn(List.of(prerequisiteNode, currentNode));
        when(learningGoalService.listProgressByGoalId(10L, 9L)).thenReturn(List.of(currentProgress));
        when(practiceTaskMapper.selectList(any())).thenReturn(List.of(currentPracticeTask));
        when(practiceAttemptMapper.selectList(any())).thenReturn(List.of(currentPracticeAttempt));
        when(reviewAttemptMapper.selectList(any())).thenReturn(List.of(
                reviewAttempt(301L, 20L, "solid", 1, LocalDateTime.of(2026, 7, 15, 12, 0))));
        when(tutorTurnMapper.selectList(any())).thenReturn(List.of(latestTutorTurn));
        when(growthSnapshotMapper.selectList(any())).thenReturn(List.of(stuck));
        when(dailyDigestMapper.selectList(any())).thenReturn(List.of());
        when(learningEventReadService.loadGoalSnapshot(10L, 9L)).thenReturn(emptyEventSnapshot());

        LearningKnowledgeGraphBO knowledgeGraph = learningGrowthService.getLearningKnowledgeGraph(10L);

        assertThat(knowledgeGraph.getMode()).isEqualTo("server");
        assertThat(knowledgeGraph.getConceptCount()).isEqualTo(2);
        assertThat(knowledgeGraph.getFocusNodeId()).isEqualTo(20L);
        assertThat(knowledgeGraph.getFocusNodeTitle()).isEqualTo("Spring AI 基础");
        assertThat(knowledgeGraph.getEdges()).anyMatch(edge ->
                edge.getType().equals("prerequisite")
                        && edge.getSourceCode().equals("java-client-basics")
                        && edge.getTargetCode().equals("spring-ai-basics"));
        assertThat(knowledgeGraph.getWeakPaths()).isNotEmpty();
        assertThat(knowledgeGraph.getFrontier()).anyMatch(item -> item.contains("当前图谱焦点"));
        assertThat(knowledgeGraph.getWeakAreas()).anyMatch(item -> item.contains("前置理解还没有彻底压实"));
        assertThat(knowledgeGraph.getEvidence()).anyMatch(item ->
                item.getNodeId().equals(20L) && item.getLabel().equals("Practice 证据"));
        assertThat(knowledgeGraph.getEvidence()).anyMatch(item ->
                item.getNodeId().equals(20L)
                        && item.getLabel().equals("Review 证据")
                        && item.getDetail().contains("已经稳住了"));
        assertThat(knowledgeGraph.getNodes()).anyMatch(item ->
                item.getNodeId().equals(20L) && item.getTags().contains("复盘稳住"));
    }

    @Test
    void getLearningRhythmBuildsServerSnapshotFromMergedSignals() {
        LearningGoalEntity goal = goal();
        LearningMapNodeEntity node = node();
        LearningNodeProgressEntity progress = LearningNodeProgressEntity.builder()
                .goalId(10L)
                .mapNodeId(20L)
                .userId(9L)
                .status("REVIEWING")
                .updatedAt(LocalDateTime.of(2026, 7, 15, 10, 0))
                .build();
        PracticeTaskEntity practiceTask = practiceTask("application");
        PracticeAttemptEntity practiceAttempt = practiceAttempt("verified", 0, LocalDateTime.of(2026, 7, 15, 11, 0));
        TutorTurnEntity tutorTurn = tutorTurn("ready", 1L, LocalDateTime.of(2026, 7, 14, 9, 0));
        DailyDigestEntity digest = DailyDigestEntity.builder()
                .userId(9L)
                .goalId(10L)
                .reflectionEntryId(400L)
                .digestDate(LocalDate.now())
                .summary("今天已经完成 Reflection。")
                .nextAction("明天继续推进当前节点。")
                .createdAt(LocalDateTime.of(2026, 7, 15, 20, 0))
                .build();

        mockGoalContext(goal, node, progress);
        when(practiceTaskMapper.selectList(any())).thenReturn(List.of(practiceTask));
        when(practiceAttemptMapper.selectList(any())).thenReturn(List.of(practiceAttempt));
        when(reviewAttemptMapper.selectList(any())).thenReturn(List.of(
                reviewAttempt(302L, 20L, "forgotten", 1, LocalDateTime.of(2026, 7, 15, 12, 0))));
        when(tutorTurnMapper.selectList(any())).thenReturn(List.of(tutorTurn));
        when(growthSnapshotMapper.selectList(any())).thenReturn(List.of());
        when(dailyDigestMapper.selectList(any())).thenReturn(List.of(digest));
        when(learningEventReadService.loadGoalSnapshot(10L, 9L)).thenReturn(emptyEventSnapshot());

        LearningRhythmBO rhythm = learningGrowthService.getLearningRhythm(10L);

        assertThat(rhythm.getMode()).isEqualTo("server");
        assertThat(rhythm.getWeeklyTargetMinutes()).isEqualTo(180);
        assertThat(rhythm.getTodayDone()).isTrue();
        assertThat(rhythm.getActiveDays()).isGreaterThanOrEqualTo(2);
        assertThat(rhythm.getLoggedMinutes()).isGreaterThan(0);
        assertThat(rhythm.getSignals()).isNotEmpty();
        assertThat(rhythm.getSignals()).anyMatch(item -> item.contains("Review"));
        assertThat(rhythm.getWeeklyPlanSummary()).contains("系统建议优先安排");
        assertThat(rhythm.getWeek()).hasSize(7);
    }

    private void mockGoalContext(LearningGoalEntity goal,
                                 LearningMapNodeEntity node,
                                 LearningNodeProgressEntity progress) {
        when(learningGoalService.getOwnedGoalById(10L, 9L)).thenReturn(goal);
        when(learningGoalService.getCurrentNode(goal)).thenReturn(node);
        when(learningGoalService.listNodesByGoalId(10L, 9L)).thenReturn(List.of(node));
        when(learningGoalService.listProgressByGoalId(10L, 9L)).thenReturn(List.of(progress));
        when(reviewAttemptMapper.selectList(any())).thenReturn(List.of());
    }

    private LearningGoalEntity goal() {
        return LearningGoalEntity.builder()
                .id(10L)
                .userId(9L)
                .activeNodeId(20L)
                .weeklyLearningMinutes(180)
                .status("ACTIVE")
                .build();
    }

    private LearningMapNodeEntity node() {
        return LearningMapNodeEntity.builder()
                .id(20L)
                .goalId(10L)
                .userId(9L)
                .nodeCode("spring-ai-basics")
                .title("Spring AI 基础")
                .sortOrder(1)
                .build();
    }

    private PracticeTaskEntity practiceTask(String evidenceKind) {
        return PracticeTaskEntity.builder()
                .id(100L)
                .goalId(10L)
                .userId(9L)
                .mapNodeId(20L)
                .taskKey("practice-20")
                .taskType("apply")
                .evidenceKind(evidenceKind)
                .build();
    }

    private PracticeAttemptEntity practiceAttempt(String assessmentLevel,
                                                  Integer handoffValidation,
                                                  LocalDateTime updatedAt) {
        return PracticeAttemptEntity.builder()
                .id(200L)
                .goalId(10L)
                .userId(9L)
                .practiceTaskId(100L)
                .mapNodeId(20L)
                .attemptKey("practice-20")
                .responseContent("我已经把核心调用链解释清楚，并留下了可验证证据。")
                .selfRating("stretch")
                .assessmentJson("{\"level\":\"" + assessmentLevel + "\"}")
                .completed(1)
                .handoffValidation(handoffValidation)
                .updatedAt(updatedAt)
                .build();
    }

    private TutorTurnEntity tutorTurn(String diagnosis, Long sessionId, LocalDateTime createdAt) {
        return TutorTurnEntity.builder()
                .sessionId(sessionId)
                .goalId(10L)
                .mapNodeId(20L)
                .userId(9L)
                .turnNo(1)
                .diagnosis(diagnosis)
                .createdAt(createdAt)
                .build();
    }

    private ReviewAttemptEntity reviewAttempt(Long id,
                                              Long nodeId,
                                              String selfRating,
                                              Integer completed,
                                              LocalDateTime updatedAt) {
        return ReviewAttemptEntity.builder()
                .id(id)
                .goalId(10L)
                .userId(9L)
                .reviewTaskId(300L)
                .mapNodeId(nodeId)
                .attemptKey("review-" + nodeId)
                .responseContent("先尝试主动回忆，再检查遗漏。")
                .selfRating(selfRating)
                .scheduledDueAt(updatedAt.plusDays(1))
                .intervalDays("solid".equals(selfRating) ? 3 : 1)
                .masteryScoreAtAttempt("solid".equals(selfRating) ? 78 : 42)
                .completed(completed)
                .updatedAt(updatedAt)
                .build();
    }

    private LearningEventReadService.GoalEventSnapshot emptyEventSnapshot() {
        return new LearningEventReadService.GoalEventSnapshot(
                List.of(),
                java.util.Map.of(),
                java.util.Map.of(),
                java.util.Map.of(),
                java.util.Map.of(),
                java.util.Map.of());
    }
}
