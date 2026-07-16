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
import io.github.module.learning.model.response.LearningPlanBO;
import io.github.module.learning.model.response.ReplanTimelineBO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LearningPlanServiceTest {

    @Mock
    private LearningGoalService learningGoalService;

    @Mock
    private TutorTurnMapper tutorTurnMapper;

    @Mock
    private PracticeTaskMapper practiceTaskMapper;

    @Mock
    private PracticeAttemptMapper practiceAttemptMapper;

    @Mock
    private ReviewAttemptMapper reviewAttemptMapper;

    @Mock
    private GrowthSnapshotMapper growthSnapshotMapper;

    @Mock
    private DailyDigestMapper dailyDigestMapper;

    @Mock
    private LearningEventReadService learningEventReadService;

    private LearningPlanService learningPlanService;

    @BeforeEach
    void setUp() {
        UserContextHolder.setUserContext(new UserContext()
                .setUserId(9L)
                .setUserName("learner"));
        LearningEventReadService projector = new LearningEventReadService(null);
        learningPlanService = new LearningPlanService(
                learningGoalService,
                tutorTurnMapper,
                practiceTaskMapper,
                practiceAttemptMapper,
                reviewAttemptMapper,
                growthSnapshotMapper,
                dailyDigestMapper,
                learningEventReadService);
        when(learningEventReadService.projectGoalEvents(anyList()))
                .thenAnswer(invocation -> projector.projectGoalEvents(invocation.getArgument(0)));
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void getCurrentPlanStartsWithTutorDiagnosisWhenCurrentNodeHasNoTurnYet() {
        LearningGoalEntity goal = goal();
        LearningMapNodeEntity currentNode = node(20L, "Spring AI 基础", "IN_PROGRESS");
        LearningMapNodeEntity readyNode = node(21L, "Prompt 设计", "READY");

        mockPlanningContext(goal, currentNode, List.of(
                progress(20L, "IN_PROGRESS"),
                progress(21L, "READY")
        ), List.of(currentNode, readyNode));
        when(tutorTurnMapper.selectList(any())).thenReturn(List.of());
        when(practiceTaskMapper.selectList(any())).thenReturn(List.of());
        when(practiceAttemptMapper.selectList(any())).thenReturn(List.of());
        when(growthSnapshotMapper.selectList(any())).thenReturn(List.of());
        when(dailyDigestMapper.selectList(any())).thenReturn(List.of());
        when(learningEventReadService.loadGoalSnapshot(10L, 9L)).thenReturn(emptyEventSnapshot());

        LearningPlanBO plan = learningPlanService.getCurrentPlan(10L);

        assertThat(plan.getMode()).isEqualTo("server");
        assertThat(plan.getMissionTitle()).contains("先诊断");
        assertThat(plan.getTasks()).isNotEmpty();
        assertThat(plan.getTasks().getFirst().getId()).isEqualTo("learn-20");
        assertThat(plan.getTasks()).extracting("id").contains("reflect-today");
    }

    @Test
    void getCurrentPlanPrefersPrerequisiteRepairWhenTutorMarkedNeedsPrereq() {
        LearningGoalEntity goal = goal();
        LearningMapNodeEntity currentNode = node(20L, "Spring AI 基础", "IN_PROGRESS");
        LearningMapNodeEntity prereqNode = node(21L, "Java 客户端基础", "READY");

        mockPlanningContext(goal, currentNode, List.of(
                progress(20L, "IN_PROGRESS"),
                progress(21L, "READY")
        ), List.of(currentNode, prereqNode));
        when(tutorTurnMapper.selectList(any())).thenReturn(List.of(
                tutorTurn("needs_prereq", 21L)
        ));
        when(practiceTaskMapper.selectList(any())).thenReturn(List.of());
        when(practiceAttemptMapper.selectList(any())).thenReturn(List.of());
        when(growthSnapshotMapper.selectList(any())).thenReturn(List.of(
                GrowthSnapshotEntity.builder()
                        .userId(9L)
                        .goalId(10L)
                        .eventType("STUCK")
                        .summary("主线先被前置关系卡住")
                        .build()
        ));
        when(dailyDigestMapper.selectList(any())).thenReturn(List.of(
                DailyDigestEntity.builder()
                        .userId(9L)
                        .goalId(10L)
                        .digestDate(LocalDateTime.now().toLocalDate())
                        .summary("今天已经做过一次封箱")
                        .build()
        ));
        when(learningEventReadService.loadGoalSnapshot(10L, 9L)).thenReturn(emptyEventSnapshot());

        LearningPlanBO plan = learningPlanService.getCurrentPlan(10L);

        assertThat(plan.getMissionTitle()).contains("先回补");
        assertThat(plan.getTasks().getFirst().getId()).isEqualTo("repair-21");
        assertThat(plan.getTasks().getFirst().getNodeId()).isEqualTo(21L);
        assertThat(plan.getRecoveryMode()).isTrue();
        assertThat(plan.getRecoveryModeTitle()).contains("先补前置");
    }

    @Test
    void getCurrentPlanPrefersUnifiedTutorEventOverOlderRawTurn() {
        LearningMapNodeEntity currentNode = node(20L, "Spring AI 基础", "IN_PROGRESS");
        LearningMapNodeEntity prereqNode = node(21L, "Java 客户端基础", "READY");

        mockPlanningContext(goal(), currentNode, List.of(
                progress(20L, "IN_PROGRESS"),
                progress(21L, "READY")
        ), List.of(currentNode, prereqNode));
        when(tutorTurnMapper.selectList(any())).thenReturn(List.of(
                tutorTurn("ready", null)
        ));
        when(practiceTaskMapper.selectList(any())).thenReturn(List.of());
        when(practiceAttemptMapper.selectList(any())).thenReturn(List.of());
        when(growthSnapshotMapper.selectList(any())).thenReturn(List.of());
        when(dailyDigestMapper.selectList(any())).thenReturn(List.of());
        when(learningEventReadService.loadGoalSnapshot(10L, 9L)).thenReturn(eventSnapshot(List.of(
                tutorEvent("needs_prereq", 21L, LocalDateTime.of(2026, 7, 15, 9, 45))
        )));

        LearningPlanBO plan = learningPlanService.getCurrentPlan(10L);

        assertThat(plan.getMissionTitle()).contains("先回补");
        assertThat(plan.getTasks().getFirst().getId()).isEqualTo("repair-21");
    }

    @Test
    void getReplanTimelineBuildsHistoryFromUnifiedEvents() {
        LearningMapNodeEntity currentNode = node(20L, "Spring AI 基础", "IN_PROGRESS");
        LearningMapNodeEntity prereqNode = node(21L, "Java 客户端基础", "READY");

        mockPlanningContext(goal(), currentNode, List.of(
                progress(20L, "IN_PROGRESS"),
                progress(21L, "READY")
        ), List.of(currentNode, prereqNode));
        when(tutorTurnMapper.selectList(any())).thenReturn(List.of());
        when(practiceTaskMapper.selectList(any())).thenReturn(List.of());
        when(practiceAttemptMapper.selectList(any())).thenReturn(List.of());
        when(growthSnapshotMapper.selectList(any())).thenReturn(List.of());
        when(dailyDigestMapper.selectList(any())).thenReturn(List.of());
        when(learningEventReadService.loadGoalSnapshot(10L, 9L)).thenReturn(eventSnapshot(List.of(
                tutorEvent("ready", null, LocalDateTime.of(2026, 7, 15, 9, 0)),
                tutorEvent("needs_prereq", 21L, LocalDateTime.of(2026, 7, 15, 9, 45))
        )));

        ReplanTimelineBO timeline = learningPlanService.getReplanTimeline(10L);

        assertThat(timeline.getMode()).isEqualTo("server");
        assertThat(timeline.getTotalReplans()).isEqualTo(1);
        assertThat(timeline.getItems()).hasSize(2);
        assertThat(timeline.getItems().get(0).getMissionTitle()).contains("先回补");
        assertThat(timeline.getItems().get(0).getTriggerEventDetailType()).isEqualTo("TURN_RECORDED");
        assertThat(timeline.getItems().get(0).getChangedFields()).contains("mission");
        assertThat(timeline.getItems().get(1).getMissionTitle()).contains("继续推进");
    }

    @Test
    void getCurrentPlanPrioritizesForgottenReviewSignals() {
        LearningMapNodeEntity forgottenNode = node(20L, "Spring AI 基础", "REVIEWING");
        LearningMapNodeEntity secondaryNode = node(21L, "Prompt 设计", "REVIEWING");

        mockPlanningContext(goal(), null, List.of(
                progress(20L, "REVIEWING"),
                progress(21L, "REVIEWING")
        ), List.of(forgottenNode, secondaryNode));
        when(tutorTurnMapper.selectList(any())).thenReturn(List.of());
        when(practiceTaskMapper.selectList(any())).thenReturn(List.of());
        when(practiceAttemptMapper.selectList(any())).thenReturn(List.of());
        when(reviewAttemptMapper.selectList(any())).thenReturn(List.of(
                reviewAttempt(300L, 20L, "forgotten", LocalDateTime.of(2026, 7, 15, 8, 30))));
        when(growthSnapshotMapper.selectList(any())).thenReturn(List.of());
        when(dailyDigestMapper.selectList(any())).thenReturn(List.of());
        when(learningEventReadService.loadGoalSnapshot(10L, 9L)).thenReturn(emptyEventSnapshot());

        LearningPlanBO plan = learningPlanService.getCurrentPlan(10L);

        assertThat(plan.getMissionTitle()).contains("遗忘点");
        assertThat(plan.getTasks()).extracting("id").contains("review-20");
        assertThat(plan.getReviewQueue()).isNotEmpty();
        assertThat(plan.getReviewQueue().getFirst()).contains("最近一次复盘已经忘了");
        assertThat(plan.getRecoveryMode()).isTrue();
    }

    @Test
    void getReplanTimelineTreatsReviewAttemptSavedAsReplayCheckpoint() {
        LearningMapNodeEntity reviewNode = node(20L, "Spring AI 基础", "REVIEWING");

        mockPlanningContext(goal(), null, List.of(
                progress(20L, "REVIEWING")
        ), List.of(reviewNode));
        when(tutorTurnMapper.selectList(any())).thenReturn(List.of());
        when(practiceTaskMapper.selectList(any())).thenReturn(List.of());
        when(practiceAttemptMapper.selectList(any())).thenReturn(List.of());
        when(reviewAttemptMapper.selectList(any())).thenReturn(List.of());
        when(growthSnapshotMapper.selectList(any())).thenReturn(List.of());
        when(dailyDigestMapper.selectList(any())).thenReturn(List.of());
        when(learningEventReadService.loadGoalSnapshot(10L, 9L)).thenReturn(eventSnapshot(List.of(
                reviewEvent("forgotten", LocalDateTime.of(2026, 7, 15, 9, 15))
        )));

        ReplanTimelineBO timeline = learningPlanService.getReplanTimeline(10L);

        assertThat(timeline.getItems()).hasSize(1);
        assertThat(timeline.getItems().getFirst().getTriggerEventSource()).isEqualTo("REVIEW");
        assertThat(timeline.getItems().getFirst().getTriggerEventDetailType()).isEqualTo("ATTEMPT_SAVED");
        assertThat(timeline.getItems().getFirst().getMissionTitle()).contains("遗忘点");
    }

    private void mockPlanningContext(LearningGoalEntity goal,
                                     LearningMapNodeEntity currentNode,
                                     List<LearningNodeProgressEntity> progressList,
                                     List<LearningMapNodeEntity> nodes) {
        when(learningGoalService.getOwnedGoalById(10L, 9L)).thenReturn(goal);
        when(learningGoalService.getCurrentNode(goal)).thenReturn(currentNode);
        when(learningGoalService.listProgressByGoalId(10L, 9L)).thenReturn(progressList);
        when(learningGoalService.listNodesByGoalId(10L, 9L)).thenReturn(nodes);
        when(reviewAttemptMapper.selectList(any())).thenReturn(List.of());
    }

    private LearningGoalEntity goal() {
        return LearningGoalEntity.builder()
                .id(10L)
                .userId(9L)
                .activeNodeId(20L)
                .weeklyLearningMinutes(240)
                .estimatedDays(42)
                .createdAt(LocalDateTime.of(2026, 7, 10, 9, 0))
                .status("ACTIVE")
                .build();
    }

    private LearningMapNodeEntity node(Long id, String title, String status) {
        return LearningMapNodeEntity.builder()
                .id(id)
                .goalId(10L)
                .userId(9L)
                .title(title)
                .nodeCode(title)
                .whyItMatters("这是当前主线上最关键的节点。")
                .completionCriteria("说清楚验证方式")
                .estimatedMinutes(30)
                .sortOrder(id.intValue())
                .build();
    }

    private LearningNodeProgressEntity progress(Long nodeId, String status) {
        return LearningNodeProgressEntity.builder()
                .goalId(10L)
                .mapNodeId(nodeId)
                .userId(9L)
                .status(status)
                .masteryLevel(30)
                .build();
    }

    private TutorTurnEntity tutorTurn(String diagnosis, Long recommendedNodeId) {
        return TutorTurnEntity.builder()
                .sessionId(1L)
                .goalId(10L)
                .mapNodeId(20L)
                .userId(9L)
                .turnNo(1)
                .diagnosis(diagnosis)
                .recommendedNodeId(recommendedNodeId)
                .createdAt(LocalDateTime.of(2026, 7, 15, 9, 30))
                .build();
    }

    private LearningEventEntity tutorEvent(String diagnosis, Long recommendedNodeId, LocalDateTime eventAt) {
        String normalizedDiagnosis = diagnosis == null ? null : diagnosis.toUpperCase();
        String actionType = "needs_prereq".equals(diagnosis) ? "redirect" : "coach";
        return LearningEventEntity.builder()
                .userId(9L)
                .goalId(10L)
                .mapNodeId(20L)
                .eventSource("TUTOR")
                .eventType("TURN_RECORDED")
                .eventStatus(normalizedDiagnosis == null ? "READY" : normalizedDiagnosis)
                .title("Tutor 诊断")
                .summary("根据诊断结果调整计划")
                .eventAt(eventAt)
                .payloadJson("""
                        {
                          "sessionId": 1,
                          "turnNo": %d,
                          "diagnosis": %s,
                          "actionType": "%s",
                          "recommendedNodeId": %s,
                          "nodeCompleted": false,
                          "nextStepSuggestions": ["%s"]
                        }
                        """.formatted(
                        recommendedNodeId == null ? 1 : 2,
                        diagnosis == null ? "null" : "\"" + diagnosis + "\"",
                        actionType,
                        recommendedNodeId == null ? "null" : recommendedNodeId,
                        recommendedNodeId == null ? "继续推进当前节点" : "先补 Java 基础"))
                .build();
    }

    private LearningEventEntity reviewEvent(String selfRating, LocalDateTime eventAt) {
        return LearningEventEntity.builder()
                .userId(9L)
                .goalId(10L)
                .mapNodeId(20L)
                .eventSource("REVIEW")
                .eventType("ATTEMPT_SAVED")
                .eventStatus("COMPLETED")
                .relatedEntityType("review_attempt")
                .relatedEntityId(300L)
                .title("完成复盘")
                .summary("一次复盘已经写回统一事件流。")
                .eventAt(eventAt)
                .payloadJson("""
                        {
                          "taskType": "recall",
                          "estimatedMinutes": 12,
                          "selfRating": "%s",
                          "scheduledDueAt": "2026-07-16T09:15:00",
                          "intervalDays": 1,
                          "masteryScoreAtAttempt": 42,
                          "completed": true
                        }
                        """.formatted(selfRating))
                .build();
    }

    private ReviewAttemptEntity reviewAttempt(Long id,
                                              Long nodeId,
                                              String selfRating,
                                              LocalDateTime updatedAt) {
        return ReviewAttemptEntity.builder()
                .id(id)
                .goalId(10L)
                .userId(9L)
                .reviewTaskId(300L)
                .mapNodeId(nodeId)
                .attemptKey("review-" + nodeId)
                .responseContent("先主动回忆，再检查遗漏。")
                .selfRating(selfRating)
                .scheduledDueAt(updatedAt.plusDays(1))
                .intervalDays("forgotten".equals(selfRating) ? 1 : 3)
                .masteryScoreAtAttempt("forgotten".equals(selfRating) ? 38 : 72)
                .completed(1)
                .updatedAt(updatedAt)
                .build();
    }

    private LearningEventReadService.GoalEventSnapshot emptyEventSnapshot() {
        return eventSnapshot(List.of());
    }

    private LearningEventReadService.GoalEventSnapshot eventSnapshot(List<LearningEventEntity> events) {
        return new LearningEventReadService(null).projectGoalEvents(events);
    }
}
