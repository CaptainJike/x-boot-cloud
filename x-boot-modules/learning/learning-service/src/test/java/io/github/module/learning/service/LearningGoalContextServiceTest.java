package io.github.module.learning.service;

import io.github.framework.core.context.UserContext;
import io.github.framework.core.context.UserContextHolder;
import io.github.module.learning.entity.LearningGoalContextRecordEntity;
import io.github.module.learning.mapper.LearningGoalContextRecordMapper;
import io.github.module.learning.model.request.AppGoalAdjustmentRecordDTO;
import io.github.module.learning.model.request.AppGoalBriefDTO;
import io.github.module.learning.model.request.AppGoalBriefRecordDTO;
import io.github.module.learning.model.request.AppGoalCheckpointRecordDTO;
import io.github.module.learning.model.request.AppGoalExecutionHandoffDTO;
import io.github.module.learning.model.request.AppGoalTuningSnapshotDTO;
import io.github.module.learning.model.request.AppGoalTuningSuggestionDTO;
import io.github.module.learning.model.request.AppPortfolioCandidateValidationRecordDTO;
import io.github.module.learning.model.response.GoalContextBundleBO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LearningGoalContextServiceTest {

    @Mock
    private LearningGoalContextRecordMapper learningGoalContextRecordMapper;

    private LearningGoalContextService learningGoalContextService;

    @BeforeEach
    void setUp() {
        UserContextHolder.setUserContext(new UserContext()
                .setUserId(9L)
                .setUserName("learner"));
        learningGoalContextService = new LearningGoalContextService(learningGoalContextRecordMapper);
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void savesAndLoadsGoalContextBundle() {
        AtomicLong idGenerator = new AtomicLong(100L);
        List<LearningGoalContextRecordEntity> storedRecords = new ArrayList<>();
        AtomicReference<LearningGoalContextRecordEntity> activeHandoffRef = new AtomicReference<>();

        when(learningGoalContextRecordMapper.selectOne(any()))
                .thenReturn(null, null, null, null, null, null, null);
        when(learningGoalContextRecordMapper.selectList(any())).thenAnswer(invocation -> List.copyOf(storedRecords));
        doAnswer(invocation -> {
            LearningGoalContextRecordEntity entity = invocation.getArgument(0);
            entity.setId(idGenerator.getAndIncrement());
            entity.setCreatedAt(LocalDateTime.of(2026, 7, 16, 10, 0));
            entity.setUpdatedAt(LocalDateTime.of(2026, 7, 16, 10, 0));
            storedRecords.add(entity);
            if ("ACTIVE_HANDOFF".equals(entity.getRecordType())) {
                activeHandoffRef.set(entity);
            }
            return 1;
        }).when(learningGoalContextRecordMapper).insert(any(LearningGoalContextRecordEntity.class));
        doAnswer(invocation -> {
            Long deletedId = invocation.getArgument(0);
            storedRecords.removeIf(item -> deletedId.equals(item.getId()));
            return 1;
        }).when(learningGoalContextRecordMapper).deleteById(any(Long.class));

        learningGoalContextService.saveGoalBriefRecord(AppGoalBriefRecordDTO.builder()
                .goalId(201L)
                .goalTitle("学习 Spring AI")
                .sourceType("manual")
                .goalValidationStatus("none")
                .goalValidationSummary("")
                .brief(sampleBrief("学习 Spring AI"))
                .createdAt("2026-07-16T10:00:00Z")
                .updatedAt("2026-07-16T10:00:00Z")
                .build());
        learningGoalContextService.saveGoalAdjustmentRecord(AppGoalAdjustmentRecordDTO.builder()
                .id("adjust-1")
                .createdAt("2026-07-16T10:05:00Z")
                .sourceGoalId(201L)
                .sourceGoalTitle("学习 Spring AI")
                .nextGoalId(202L)
                .nextGoalTitle("做一个 Agent Demo")
                .checkpointDecision("reframe")
                .checkpointTitle("目标收得更窄")
                .tuningSummary("先收窄到一个最小可运行 Demo。")
                .changedFields(List.of("desiredOutcome"))
                .changes(List.of(AppGoalTuningSuggestionDTO.builder()
                        .field("desiredOutcome")
                        .title("先做最小 Demo")
                        .rationale("这样更容易形成第一个闭环")
                        .before("完成完整产品方案")
                        .after("完成一个可运行 Demo")
                        .priority(1)
                        .build()))
                .sourceBrief(sampleBrief("学习 Spring AI"))
                .resultingBrief(sampleBrief("做一个 Agent Demo"))
                .build());
        learningGoalContextService.saveActiveGoalExecutionHandoff(AppGoalExecutionHandoffDTO.builder()
                .id("handoff-1")
                .createdAt("2026-07-16T10:06:00Z")
                .sourceGoalId(201L)
                .sourceGoalTitle("学习 Spring AI")
                .nextGoalId(202L)
                .nextGoalTitle("做一个 Agent Demo")
                .checkpointDecision("reframe")
                .checkpointTitle("目标收得更窄")
                .handoffTitle("新主线交接")
                .handoffSummary("接下来集中把最小 Demo 跑通。")
                .firstMissionTitle("先完成 Hello Agent")
                .firstMissionSummary("把模型调用和基本交互先跑通。")
                .carryOverActions(List.of("保留现有学习节奏"))
                .watchouts(List.of("避免一次性扩太多功能"))
                .build());
        learningGoalContextService.savePortfolioCandidateValidationRecord(AppPortfolioCandidateValidationRecordDTO.builder()
                .goalId(202L)
                .goalTitle("做一个 Agent Demo")
                .summary("这个候选目标更容易形成真实作品。")
                .whyBetter("路径更短，反馈更快。")
                .firstProof("已经能跑通基础调用。")
                .riskWatchout("要防止过度扩需求。")
                .confidence(4)
                .decision("switch_now")
                .updatedAt("2026-07-16T10:07:00Z")
                .build());
        learningGoalContextService.saveGoalCheckpointRecord(AppGoalCheckpointRecordDTO.builder()
                .id("checkpoint-1")
                .recordedAt("2026-07-16T10:08:00Z")
                .title("阶段复盘")
                .summary("目标过大，需要收窄")
                .decision("reframe")
                .decisionTitle("先重调目标")
                .nextStepTitle("去 Goal Builder 微调")
                .evidenceHighlights(List.of("练习反馈太散"))
                .checkpointReason("当前目标跨度过大，难以形成稳定闭环。")
                .build());
        learningGoalContextService.saveGoalTuningSnapshot(AppGoalTuningSnapshotDTO.builder()
                .createdAt("2026-07-16T10:09:00Z")
                .mode("fallback")
                .goalTitle("学习 Spring AI")
                .sourceGoalId(201L)
                .candidateGoalId(202L)
                .checkpointDecision("reframe")
                .checkpointTitle("先重调目标")
                .checkpointReason("先收窄，形成第一个作品。")
                .tuningSummary("建议先围绕一个最小 Demo 重建目标。")
                .suggestedBrief(sampleBrief("做一个 Agent Demo"))
                .changes(List.of(AppGoalTuningSuggestionDTO.builder()
                        .field("desiredOutcome")
                        .title("改成可运行 Demo")
                        .rationale("结果更明确")
                        .before("完整方案")
                        .after("最小 Demo")
                        .priority(1)
                        .build()))
                .carryOverQuestions(List.of("先做哪个场景最小可行"))
                .build());

        GoalContextBundleBO bundle = learningGoalContextService.getGoalContext();

        assertThat(bundle.getGoalBriefRecords()).hasSize(1);
        assertThat(bundle.getGoalBriefRecords().getFirst().getGoalTitle()).isEqualTo("学习 Spring AI");
        assertThat(bundle.getGoalAdjustmentRecords()).hasSize(1);
        assertThat(bundle.getGoalAdjustmentRecords().getFirst().getNextGoalTitle()).isEqualTo("做一个 Agent Demo");
        assertThat(bundle.getActiveGoalExecutionHandoff()).isNotNull();
        assertThat(bundle.getActiveGoalExecutionHandoff().getHandoffTitle()).isEqualTo("新主线交接");
        assertThat(bundle.getPortfolioCandidateValidations()).hasSize(1);
        assertThat(bundle.getPortfolioCandidateValidations().getFirst().getDecision()).isEqualTo("switch_now");
        assertThat(bundle.getGoalCheckpointRecords()).hasSize(1);
        assertThat(bundle.getGoalCheckpointRecords().getFirst().getDecisionTitle()).isEqualTo("先重调目标");
        assertThat(bundle.getGoalTuningSnapshot()).isNotNull();
        assertThat(bundle.getGoalTuningSnapshot().getSuggestedBrief().getTitle()).isEqualTo("做一个 Agent Demo");

        when(learningGoalContextRecordMapper.selectOne(any())).thenReturn(activeHandoffRef.get());
        learningGoalContextService.clearActiveGoalExecutionHandoff();

        GoalContextBundleBO clearedBundle = learningGoalContextService.getGoalContext();
        assertThat(clearedBundle.getActiveGoalExecutionHandoff()).isNull();
    }

    private AppGoalBriefDTO sampleBrief(String title) {
        return AppGoalBriefDTO.builder()
                .title(title)
                .domain("后端开发")
                .motivation("想把 AI 能力接进真实项目")
                .currentLevel("理解基础概念，但缺少系统闭环")
                .desiredOutcome("完成一个可以讲清楚的成品")
                .weeklyLearningMinutes(240)
                .targetWeeks(4)
                .preferredLearningStyle("项目实战")
                .successCriteria(List.of("做出一个可运行结果"))
                .constraints(List.of("希望单次学习控制在 45 分钟"))
                .tags(List.of("Spring AI", "Agent"))
                .build();
    }
}
