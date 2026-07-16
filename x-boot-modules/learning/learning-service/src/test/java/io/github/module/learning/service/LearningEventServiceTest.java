package io.github.module.learning.service;

import io.github.module.learning.entity.DailyDigestEntity;
import io.github.module.learning.entity.LearningEventEntity;
import io.github.module.learning.entity.LearningMapNodeEntity;
import io.github.module.learning.entity.PracticeAttemptEntity;
import io.github.module.learning.entity.PracticeTaskEntity;
import io.github.module.learning.entity.ReflectionEntryEntity;
import io.github.module.learning.entity.TutorSessionEntity;
import io.github.module.learning.entity.TutorTurnEntity;
import io.github.module.learning.mapper.LearningEventMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LearningEventServiceTest {

    @Mock
    private LearningEventMapper learningEventMapper;

    @Test
    void recordPracticeAttemptWritesVersionedUnifiedEvent() {
        LearningEventService learningEventService = new LearningEventService(learningEventMapper);
        when(learningEventMapper.insert(any(LearningEventEntity.class))).thenReturn(1);

        PracticeTaskEntity task = PracticeTaskEntity.builder()
                .id(100L)
                .userId(9L)
                .goalId(10L)
                .mapNodeId(20L)
                .taskKey("practice-20")
                .title("把概念放进真实场景")
                .taskType("apply")
                .evidenceKind("application")
                .nodeTitle("Spring AI 基础")
                .build();
        PracticeAttemptEntity attempt = PracticeAttemptEntity.builder()
                .id(200L)
                .userId(9L)
                .goalId(10L)
                .mapNodeId(20L)
                .completed(1)
                .selfRating("clear")
                .syncVersion(3L)
                .lastMutationId("mutation-3")
                .updatedAt(LocalDateTime.of(2026, 7, 15, 21, 0))
                .build();

        learningEventService.recordPracticeAttempt(task, attempt);

        ArgumentCaptor<LearningEventEntity> captor = ArgumentCaptor.forClass(LearningEventEntity.class);
        verify(learningEventMapper).insert(captor.capture());
        LearningEventEntity event = captor.getValue();
        assertThat(event.getEventSource()).isEqualTo("PRACTICE");
        assertThat(event.getEventType()).isEqualTo("ATTEMPT_SAVED");
        assertThat(event.getEventStatus()).isEqualTo("COMPLETED");
        assertThat(event.getSummary()).contains("版本 v3");
        assertThat(event.getPayloadJson()).contains("mutation-3");
    }

    @Test
    void recordTutorAndReflectionKeepDetailedSourceMetadata() {
        LearningEventService learningEventService = new LearningEventService(learningEventMapper);
        when(learningEventMapper.insert(any(LearningEventEntity.class))).thenReturn(1);

        LearningMapNodeEntity node = LearningMapNodeEntity.builder()
                .id(20L)
                .title("Prompt 设计")
                .build();
        TutorSessionEntity session = TutorSessionEntity.builder()
                .id(300L)
                .userId(9L)
                .goalId(10L)
                .mapNodeId(20L)
                .learnerQuestion("为什么提示词总是不稳定？")
                .createdAt(LocalDateTime.of(2026, 7, 15, 9, 0))
                .build();
        TutorTurnEntity firstTurn = TutorTurnEntity.builder()
                .id(301L)
                .userId(9L)
                .goalId(10L)
                .mapNodeId(20L)
                .turnNo(1)
                .diagnosis("needs_prereq")
                .actionType("redirect")
                .createdAt(LocalDateTime.of(2026, 7, 15, 9, 1))
                .build();

        learningEventService.recordTutorSessionStarted(null, node, session, firstTurn);

        ReflectionEntryEntity reflection = ReflectionEntryEntity.builder()
                .id(400L)
                .userId(9L)
                .goalId(10L)
                .reflectionDate(LocalDate.of(2026, 7, 15))
                .createdAt(LocalDateTime.of(2026, 7, 15, 22, 0))
                .build();
        DailyDigestEntity digest = DailyDigestEntity.builder()
                .id(401L)
                .userId(9L)
                .goalId(10L)
                .summary("今天把提示词的边界条件第一次写成了可验证步骤。")
                .nextAction("明天用同一模板换一个真实问题。")
                .createdAt(LocalDateTime.of(2026, 7, 15, 22, 1))
                .build();

        learningEventService.recordReflectionSubmitted(null, reflection, digest);

        ArgumentCaptor<LearningEventEntity> captor = ArgumentCaptor.forClass(LearningEventEntity.class);
        verify(learningEventMapper, org.mockito.Mockito.times(2)).insert(captor.capture());
        assertThat(captor.getAllValues()).extracting(LearningEventEntity::getEventSource)
                .containsExactly("TUTOR", "REFLECTION");
        assertThat(captor.getAllValues().getFirst().getSummary()).contains("需要补前置");
        assertThat(captor.getAllValues().get(1).getSummary()).contains("可验证步骤");
    }
}
