package io.github.module.learning.service;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import io.github.framework.core.context.TenantContext;
import io.github.framework.core.context.TenantContextHolder;
import io.github.framework.core.context.UserContext;
import io.github.framework.core.context.UserContextHolder;
import io.github.framework.core.exception.BusinessException;
import io.github.framework.crud.handler.MybatisPlusAutoFillColumnHandler;
import io.github.module.learning.entity.LearningGoalEntity;
import io.github.module.learning.entity.LearningMapNodeEntity;
import io.github.module.learning.entity.LearningNodeProgressEntity;
import io.github.module.learning.entity.PracticeAttemptEntity;
import io.github.module.learning.entity.PracticeTaskEntity;
import io.github.module.learning.mapper.PracticeAttemptMapper;
import io.github.module.learning.mapper.PracticeTaskMapper;
import io.github.module.learning.model.request.AppSavePracticeAttemptDTO;
import io.github.module.learning.model.response.PracticeAttemptBO;
import io.github.module.learning.model.response.PracticeWorkspaceBO;
import org.apache.ibatis.reflection.MetaObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LearningPracticeServiceTest {

    @Mock
    private PracticeTaskMapper practiceTaskMapper;

    @Mock
    private PracticeAttemptMapper practiceAttemptMapper;

    @Mock
    private LearningGoalService learningGoalService;

    @Mock
    private LearningEventService learningEventService;

    private LearningPracticeService learningPracticeService;

    @BeforeEach
    void setUp() {
        UserContextHolder.setUserContext(new UserContext()
                .setUserId(9L)
                .setUserName("learner"));
        learningPracticeService = new LearningPracticeService(
                practiceTaskMapper, practiceAttemptMapper, learningGoalService, learningEventService);
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
        TenantContextHolder.clear();
    }

    @Test
    void autoFillUsesPrivilegedTenantWhenContextIsMissing() {
        RecordingAutoFillHandler handler = new RecordingAutoFillHandler();

        TenantContextHolder.clear();
        handler.insertFill(null);

        assertThat(handler.tenantId).isZero();

        TenantContextHolder.setTenantContext(new TenantContext().setTenantId(42L));
        handler.insertFill(null);

        assertThat(handler.tenantId).isEqualTo(42L);
    }

    @Test
    void getWorkspaceGeneratesAndPersistsStableTasksForCurrentNode() {
        LearningGoalEntity goal = LearningGoalEntity.builder()
                .id(10L)
                .userId(9L)
                .activeNodeId(20L)
                .build();
        LearningMapNodeEntity node = LearningMapNodeEntity.builder()
                .id(20L)
                .goalId(10L)
                .userId(9L)
                .title("线程同步")
                .learningObjective("能解释线程同步的必要性")
                .whyItMatters("避免共享状态竞态")
                .verificationMethod("写出一个最小竞态复现")
                .completionCriteria("能够修复并解释竞态")
                .estimatedMinutes(30)
                .build();
        LearningNodeProgressEntity progress = LearningNodeProgressEntity.builder()
                .lastDiagnosis("misconception")
                .build();
        when(learningGoalService.getOwnedGoalById(10L, 9L)).thenReturn(goal);
        when(learningGoalService.getCurrentNode(goal)).thenReturn(node);
        when(learningGoalService.getProgress(10L, 20L, 9L)).thenReturn(progress);
        when(practiceTaskMapper.selectList(any())).thenReturn(List.of());
        when(practiceAttemptMapper.selectList(any())).thenReturn(List.of());
        AtomicLong ids = new AtomicLong(100L);
        when(practiceTaskMapper.insert(any(PracticeTaskEntity.class))).thenAnswer(invocation -> {
            PracticeTaskEntity entity = invocation.getArgument(0);
            entity.setId(ids.getAndIncrement());
            return 1;
        });

        PracticeWorkspaceBO workspace = learningPracticeService.getWorkspace(10L);

        assertThat(workspace.getMode()).isEqualTo("server");
        assertThat(workspace.getSchemaVersion()).isEqualTo(1);
        assertThat(workspace.getTasks()).extracting("id").containsExactly(
                "practice-check-20", "practice-explain-20", "practice-apply-20");
        assertThat(workspace.getAttempts()).isEmpty();

        ArgumentCaptor<PracticeTaskEntity> captor = ArgumentCaptor.forClass(PracticeTaskEntity.class);
        verify(practiceTaskMapper, org.mockito.Mockito.times(3)).insert(captor.capture());
        assertThat(captor.getAllValues()).allSatisfy(task -> {
            assertThat(task.getTenantId()).isZero();
            assertThat(task.getUserId()).isEqualTo(9L);
            assertThat(task.getGoalId()).isEqualTo(10L);
            assertThat(task.getMapNodeId()).isEqualTo(20L);
            assertThat(task.getActive()).isEqualTo(1);
        });
    }

    @Test
    void saveAttemptReturnsExistingRecordForRepeatedMutationWithoutWriting() {
        PracticeTaskEntity task = task();
        PracticeAttemptEntity existing = attempt(2L, "mutation-same");
        when(practiceTaskMapper.selectOne(any())).thenReturn(task);
        when(practiceAttemptMapper.selectOne(any())).thenReturn(existing);
        when(learningGoalService.getOwnedGoalById(10L, 9L)).thenReturn(goal());

        PracticeAttemptBO result = learningPracticeService.saveAttempt(
                task.getTaskKey(), completeDto("mutation-same", 1L));

        assertThat(result.getServerVersion()).isEqualTo(2L);
        assertThat(result.getLastMutationId()).isEqualTo("mutation-same");
        verify(practiceAttemptMapper, never()).insert(any(PracticeAttemptEntity.class));
        verify(practiceAttemptMapper, never()).update(any(PracticeAttemptEntity.class), any());
        verify(learningEventService, never()).recordPracticeAttempt(any(), any());
    }

    @Test
    void saveAttemptUsesDefaultTenantForNewRecord() {
        PracticeTaskEntity task = task();
        when(practiceTaskMapper.selectOne(any())).thenReturn(task);
        when(practiceAttemptMapper.selectOne(any())).thenReturn(null);
        when(practiceAttemptMapper.insert(any(PracticeAttemptEntity.class))).thenAnswer(invocation -> {
            invocation.<PracticeAttemptEntity>getArgument(0).setId(200L);
            return 1;
        });
        when(learningGoalService.getOwnedGoalById(10L, 9L)).thenReturn(goal());

        learningPracticeService.saveAttempt(task.getTaskKey(), completeDto("mutation-new", 0L));

        ArgumentCaptor<PracticeAttemptEntity> captor = ArgumentCaptor.forClass(PracticeAttemptEntity.class);
        verify(practiceAttemptMapper).insert(captor.capture());
        assertThat(captor.getValue().getTenantId()).isZero();
    }

    @Test
    void saveAttemptRejectsStaleBaseVersion() {
        PracticeTaskEntity task = task();
        PracticeAttemptEntity existing = attempt(2L, "mutation-old");
        when(practiceTaskMapper.selectOne(any())).thenReturn(task);
        when(practiceAttemptMapper.selectOne(any())).thenReturn(existing);
        when(learningGoalService.getOwnedGoalById(10L, 9L)).thenReturn(goal());

        BusinessException exception = catchThrowableOfType(
                () -> learningPracticeService.saveAttempt(
                        task.getTaskKey(), completeDto("mutation-new", 1L)),
                BusinessException.class);

        assertThat(exception.getCode()).isEqualTo(409);
        verify(practiceAttemptMapper, never()).update(any(PracticeAttemptEntity.class), any());
    }

    @Test
    void saveAttemptUpdatesWithVersionGuardAndIncrementsServerVersion() {
        PracticeTaskEntity task = task();
        PracticeAttemptEntity existing = attempt(2L, "mutation-old");
        when(practiceTaskMapper.selectOne(any())).thenReturn(task);
        when(practiceAttemptMapper.selectOne(any())).thenReturn(existing);
        when(practiceAttemptMapper.update(any(PracticeAttemptEntity.class), any())).thenReturn(1);
        when(learningGoalService.getOwnedGoalById(10L, 9L)).thenReturn(goal());

        PracticeAttemptBO result = learningPracticeService.saveAttempt(
                task.getTaskKey(), completeDto("mutation-new", 2L));

        assertThat(result.getServerVersion()).isEqualTo(3L);
        assertThat(result.getLastMutationId()).isEqualTo("mutation-new");
        assertThat(existing.getSyncVersion()).isEqualTo(3L);
        verify(practiceAttemptMapper).update(any(PracticeAttemptEntity.class), any());
        verify(learningEventService).recordPracticeAttempt(task, existing);
    }

    @Test
    void saveAttemptTreatsConcurrentSameMutationAsSuccessfulRetry() {
        PracticeTaskEntity task = task();
        PracticeAttemptEntity existing = attempt(2L, "mutation-old");
        PracticeAttemptEntity concurrentWinner = attempt(3L, "mutation-new");
        when(practiceTaskMapper.selectOne(any())).thenReturn(task);
        when(practiceAttemptMapper.selectOne(any())).thenReturn(existing, concurrentWinner);
        when(practiceAttemptMapper.update(any(PracticeAttemptEntity.class), any())).thenReturn(0);
        when(learningGoalService.getOwnedGoalById(10L, 9L)).thenReturn(goal());

        PracticeAttemptBO result = learningPracticeService.saveAttempt(
                task.getTaskKey(), completeDto("mutation-new", 2L));

        assertThat(result.getServerVersion()).isEqualTo(3L);
        assertThat(result.getLastMutationId()).isEqualTo("mutation-new");
    }

    private LearningGoalEntity goal() {
        return LearningGoalEntity.builder().id(10L).userId(9L).activeNodeId(20L).build();
    }

    private PracticeTaskEntity task() {
        return PracticeTaskEntity.builder()
                .id(100L)
                .userId(9L)
                .goalId(10L)
                .mapNodeId(20L)
                .taskKey("practice-check-20")
                .taskType("check")
                .evidenceKind("concept_check")
                .active(1)
                .build();
    }

    private PracticeAttemptEntity attempt(Long version, String mutationId) {
        return PracticeAttemptEntity.builder()
                .id(200L)
                .userId(9L)
                .goalId(10L)
                .practiceTaskId(100L)
                .mapNodeId(20L)
                .attemptKey("practice-check-20")
                .responseContent("因为共享状态会产生竞态，所以需要同步，并用重复运行结果验证。")
                .selfRating("clear")
                .artifactsJson("[]")
                .assessmentJson(null)
                .completed(1)
                .handoffValidation(0)
                .lastMutationId(mutationId)
                .syncVersion(version)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private AppSavePracticeAttemptDTO completeDto(String mutationId, Long baseVersion) {
        AppSavePracticeAttemptDTO.ArtifactDTO artifact = AppSavePracticeAttemptDTO.ArtifactDTO.builder()
                .id("practice-check-20-work")
                .kind("work")
                .title("线程同步练习")
                .content("因为共享状态会产生竞态，所以需要同步，并用重复运行结果验证。")
                .createdAt("2026-07-15T10:00:00.000Z")
                .build();
        AppSavePracticeAttemptDTO.AssessmentDTO assessment = AppSavePracticeAttemptDTO.AssessmentDTO.builder()
                .mode("rule")
                .level("verified")
                .score(90)
                .summary("规则评测通过")
                .criteria(List.of())
                .assessedAt("2026-07-15T10:00:00.000Z")
                .build();
        return AppSavePracticeAttemptDTO.builder()
                .goalId(10L)
                .mutationId(mutationId)
                .baseVersion(baseVersion)
                .response("因为共享状态会产生竞态，所以需要同步，并用重复运行结果验证。")
                .selfRating("clear")
                .artifacts(List.of(artifact))
                .assessment(assessment)
                .completed(true)
                .handoffValidation(false)
                .clientUpdatedAt("2026-07-15T10:00:00.000Z")
                .build();
    }

    private static final class RecordingAutoFillHandler extends MybatisPlusAutoFillColumnHandler {

        private Long tenantId;

        @Override
        public <T, E extends T> MetaObjectHandler strictInsertFill(
                MetaObject metaObject, String fieldName, Class<T> fieldType, E fieldVal) {
            if ("tenantId".equals(fieldName)) {
                tenantId = (Long) fieldVal;
            }
            return this;
        }

        @Override
        public <T, E extends T> MetaObjectHandler strictUpdateFill(
                MetaObject metaObject, String fieldName, Class<T> fieldType, E fieldVal) {
            return this;
        }
    }
}
