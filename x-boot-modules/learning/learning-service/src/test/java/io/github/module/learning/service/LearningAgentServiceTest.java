package io.github.module.learning.service;

import io.github.framework.core.context.UserContext;
import io.github.framework.core.context.UserContextHolder;
import io.github.module.learning.entity.DailyDigestEntity;
import io.github.module.learning.entity.LearningGoalEntity;
import io.github.module.learning.entity.LearningMapNodeEntity;
import io.github.module.learning.entity.PracticeAttemptEntity;
import io.github.module.learning.entity.ReviewAttemptEntity;
import io.github.module.learning.entity.TutorTurnEntity;
import io.github.module.learning.mapper.DailyDigestMapper;
import io.github.module.learning.mapper.PracticeAttemptMapper;
import io.github.module.learning.mapper.ReviewAttemptMapper;
import io.github.module.learning.mapper.TutorTurnMapper;
import io.github.module.learning.model.response.LearnerMemoryBO;
import io.github.module.learning.model.response.LearningAgentBO;
import io.github.module.learning.model.response.LearningGoalBO;
import io.github.module.learning.model.response.LearningKnowledgeGraphBO;
import io.github.module.learning.model.response.LearningMapNodeBO;
import io.github.module.learning.model.response.LearningPlanBO;
import io.github.module.learning.model.response.LearningPlanTaskBO;
import io.github.module.learning.model.response.LearningRhythmBO;
import io.github.module.learning.model.response.TodayLearningBO;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LearningAgentServiceTest {

    @Mock
    private LearningGoalService learningGoalService;

    @Mock
    private LearningPlanService learningPlanService;

    @Mock
    private LearningGrowthService learningGrowthService;

    @Mock
    private TutorTurnMapper tutorTurnMapper;

    @Mock
    private PracticeAttemptMapper practiceAttemptMapper;

    @Mock
    private ReviewAttemptMapper reviewAttemptMapper;

    @Mock
    private DailyDigestMapper dailyDigestMapper;

    private LearningAgentService learningAgentService;

    @BeforeEach
    void setUp() {
        UserContextHolder.setUserContext(new UserContext()
                .setUserId(9L)
                .setUserName("learner"));
        learningAgentService = new LearningAgentService(
                learningGoalService,
                learningPlanService,
                learningGrowthService,
                tutorTurnMapper,
                practiceAttemptMapper,
                reviewAttemptMapper,
                dailyDigestMapper
        );
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void getLearningAgentBuildsServerSnapshotFromRepairSignals() {
        when(learningGoalService.getOwnedGoalById(10L, 9L)).thenReturn(goal());
        when(learningGoalService.getToday()).thenReturn(today(false));
        when(learningGoalService.listNodesByGoalId(10L, 9L)).thenReturn(List.of(node()));
        when(learningPlanService.getCurrentPlan(10L)).thenReturn(plan("behind"));
        when(learningGrowthService.getLearnerMemory(10L)).thenReturn(memory("at_risk"));
        when(learningGrowthService.getLearningKnowledgeGraph(10L)).thenReturn(knowledge("当前最薄的是概念边界和验证方式的连接"));
        when(learningGrowthService.getLearningRhythm(10L)).thenReturn(rhythm(false));
        when(tutorTurnMapper.selectOne(any())).thenReturn(tutorTurn());
        when(practiceAttemptMapper.selectOne(any())).thenReturn(practiceAttempt("verified", LocalDateTime.of(2026, 7, 16, 10, 0)));
        when(reviewAttemptMapper.selectList(any())).thenReturn(List.of(
                reviewAttempt("forgotten", LocalDateTime.of(2026, 7, 16, 18, 0))
        ));
        when(dailyDigestMapper.selectOne(any())).thenReturn(digest());

        LearningAgentBO agent = learningAgentService.getLearningAgent(10L);

        assertThat(agent.getMode()).isEqualTo("server");
        assertThat(agent.getPresenceTitle()).contains("校正");
        assertThat(agent.getUrgentCount()).isGreaterThan(0);
        assertThat(agent.getInterventions()).isNotEmpty();
        assertThat(agent.getInterventions().getFirst().getTitle()).contains("验证断点");
        assertThat(agent.getCarryOverNotes()).anyMatch(item -> item.contains("目标重构验证"));
        assertThat(agent.getWatchouts()).anyMatch(item -> item.contains("验证练习"));
        assertThat(agent.getSceneNudges()).hasSize(4);
    }

    @Test
    void getLearningAgentFallsBackToMainlineResumeWhenNoUrgentSignal() {
        when(learningGoalService.getOwnedGoalById(10L, 9L)).thenReturn(goal());
        when(learningGoalService.getToday()).thenReturn(today(true));
        when(learningGoalService.listNodesByGoalId(10L, 9L)).thenReturn(List.of(node()));
        when(learningPlanService.getCurrentPlan(10L)).thenReturn(plan("steady", List.of(), List.of()));
        when(learningGrowthService.getLearnerMemory(10L)).thenReturn(memory(null));
        when(learningGrowthService.getLearningKnowledgeGraph(10L)).thenReturn(knowledge(null));
        when(learningGrowthService.getLearningRhythm(10L)).thenReturn(rhythm(true));
        when(tutorTurnMapper.selectOne(any())).thenReturn(tutorTurn());
        when(practiceAttemptMapper.selectOne(any())).thenReturn(practiceAttempt("verified", LocalDateTime.of(2026, 7, 16, 19, 0)));
        when(reviewAttemptMapper.selectList(any())).thenReturn(List.of());
        when(dailyDigestMapper.selectOne(any())).thenReturn(null);

        LearningAgentBO agent = learningAgentService.getLearningAgent(10L);

        assertThat(agent.getMode()).isEqualTo("server");
        assertThat(agent.getPresenceTitle()).contains("继续陪你沿当前主线推进");
        assertThat(agent.getResumeTitle()).contains("Practice");
        assertThat(agent.getInterventions()).hasSize(1);
        assertThat(agent.getInterventions().getFirst().getTitle()).contains("继续「继续推进");
        assertThat(agent.getNextTwoSteps()).hasSize(2);
        assertThat(agent.getNextTwoSteps().getFirst().getTitle()).contains("继续「继续推进");
    }

    private LearningGoalEntity goal() {
        return LearningGoalEntity.builder()
                .id(10L)
                .userId(9L)
                .activeNodeId(20L)
                .status("ACTIVE")
                .build();
    }

    private LearningMapNodeEntity node() {
        return LearningMapNodeEntity.builder()
                .id(20L)
                .goalId(10L)
                .userId(9L)
                .title("Spring AI 基础")
                .build();
    }

    private TodayLearningBO today(boolean reflectedToday) {
        return TodayLearningBO.builder()
                .goal(LearningGoalBO.builder().id(10L).build())
                .currentNode(LearningMapNodeBO.builder().id(20L).title("Spring AI 基础").build())
                .reflectedToday(reflectedToday)
                .recommendedActions(List.of("继续主线"))
                .build();
    }

    private LearningPlanBO plan(String paceStatus) {
        return plan(
                paceStatus,
                List.of("Spring AI 基础：最近一次复盘已经忘了，今天优先回收这个遗忘点"),
                List.of("Reflection：今天结束前别忘记把理解和卡点写回系统")
        );
    }

    private LearningPlanBO plan(String paceStatus, List<String> reviewQueue, List<String> carryOverQueue) {
        return LearningPlanBO.builder()
                .mode("server")
                .missionTitle("继续推进当前主线")
                .missionSummary("先继续推进当前节点，再补上一轮 Reflection。")
                .replanReason("系统判断当前需要收拢主线并减少切换。")
                .recoveryNote("先完成主任务，再决定还能不能扩张。")
                .paceStatus(paceStatus)
                .reviewQueue(reviewQueue)
                .carryOverQueue(carryOverQueue)
                .tasks(List.of(
                        LearningPlanTaskBO.builder()
                                .id("continue-20")
                                .kind("learn")
                                .title("继续推进「Spring AI 基础」")
                                .summary("把当前节点的概念边界和验证方式真正讲清楚。")
                                .reason("这是当前学习路径上的主推进点。")
                                .estimatedMinutes(25)
                                .nodeId(20L)
                                .nodeTitle("Spring AI 基础")
                                .targetSection("tutor")
                                .priority(88)
                                .recoveryMode(Boolean.FALSE)
                                .build(),
                        LearningPlanTaskBO.builder()
                                .id("reflect-today")
                                .kind("reflect")
                                .title("给今天做一次 Reflection 封箱")
                                .summary("把今天的理解、误解和下一步动作写下来。")
                                .reason("没有 Reflection，今天的学习无法稳定写回长期记忆。")
                                .estimatedMinutes(10)
                                .nodeId(20L)
                                .nodeTitle("Spring AI 基础")
                                .targetSection("reflection")
                                .priority(80)
                                .recoveryMode(Boolean.FALSE)
                                .build()
                ))
                .build();
    }

    private LearnerMemoryBO memory(String goalValidationStatus) {
        return LearnerMemoryBO.builder()
                .mode("server")
                .goalValidationStatus(goalValidationStatus)
                .goalValidationSummary(goalValidationStatus == null
                        ? null
                        : "这次目标重构后的验证练习仍然没有真正接住执行。")
                .weakSignals(List.of("这次目标重构后的验证练习仍然没有真正接住执行。"))
                .recommendedAdjustments(List.of("先回看这轮目标重构最初想解决的问题。"))
                .build();
    }

    private LearningKnowledgeGraphBO knowledge(String weakArea) {
        return LearningKnowledgeGraphBO.builder()
                .mode("server")
                .focusNodeId(20L)
                .focusNodeTitle("Spring AI 基础")
                .weakAreas(weakArea == null ? List.of() : List.of(weakArea))
                .weakPaths(weakArea == null ? List.of() : List.of("Spring AI 基础 -> 验证方式 这条关系还需要补证据。"))
                .build();
    }

    private LearningRhythmBO rhythm(boolean todayDone) {
        return LearningRhythmBO.builder()
                .mode("server")
                .todayDone(todayDone)
                .recoveryPlan("今天先完成一个最小学习闭环：15 分钟主任务，外加 5 分钟 Reflection。")
                .build();
    }

    private TutorTurnEntity tutorTurn() {
        return TutorTurnEntity.builder()
                .goalId(10L)
                .userId(9L)
                .mapNodeId(20L)
                .turnNo(2)
                .createdAt(LocalDateTime.of(2026, 7, 16, 9, 30))
                .nextStepSuggestionsJson("[\"先讲清楚概念边界，再举一个最小验证例子\"]")
                .build();
    }

    private PracticeAttemptEntity practiceAttempt(String assessmentLevel, LocalDateTime updatedAt) {
        return PracticeAttemptEntity.builder()
                .id(200L)
                .goalId(10L)
                .userId(9L)
                .mapNodeId(20L)
                .selfRating("stretch")
                .assessmentJson("{\"level\":\"" + assessmentLevel + "\"}")
                .completed(1)
                .updatedAt(updatedAt)
                .build();
    }

    private ReviewAttemptEntity reviewAttempt(String selfRating, LocalDateTime updatedAt) {
        return ReviewAttemptEntity.builder()
                .id(300L)
                .goalId(10L)
                .userId(9L)
                .mapNodeId(20L)
                .selfRating(selfRating)
                .completed(1)
                .updatedAt(updatedAt)
                .build();
    }

    private DailyDigestEntity digest() {
        return DailyDigestEntity.builder()
                .userId(9L)
                .goalId(10L)
                .summary("今天已经把主线里的关键概念边界写清楚了。")
                .nextAction("明天先做一轮最小验证。")
                .createdAt(LocalDateTime.of(2026, 7, 16, 20, 0))
                .build();
    }
}
