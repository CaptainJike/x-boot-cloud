package io.github.module.learning.service;

import io.github.module.learning.entity.LearningEventEntity;
import io.github.module.learning.mapper.LearningEventMapper;
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
class LearningEventReadServiceTest {

    @Mock
    private LearningEventMapper learningEventMapper;

    @Test
    void loadGoalSnapshotProjectsLatestTutorPracticeReviewAndReflectionSignals() {
        LearningEventReadService learningEventReadService = new LearningEventReadService(learningEventMapper);
        when(learningEventMapper.selectList(any())).thenReturn(List.of(
                LearningEventEntity.builder()
                        .userId(9L)
                        .goalId(10L)
                        .mapNodeId(20L)
                        .eventSource("TUTOR")
                        .eventType("SESSION_STARTED")
                        .eventStatus("READY")
                        .relatedEntityType("tutor_session")
                        .relatedEntityId(1L)
                        .eventAt(LocalDateTime.of(2026, 7, 15, 9, 0))
                        .payloadJson("""
                                {"sessionId":1,"turnNo":1,"diagnosis":"ready","actionType":"diagnose","recommendedNodeId":null,"nodeCompleted":0,"nextStepSuggestions":["先解释概念边界"]}
                                """)
                        .build(),
                LearningEventEntity.builder()
                        .userId(9L)
                        .goalId(10L)
                        .mapNodeId(20L)
                        .eventSource("TUTOR")
                        .eventType("TURN_RECORDED")
                        .eventStatus("NEEDS_PREREQ")
                        .relatedEntityType("tutor_turn")
                        .relatedEntityId(2L)
                        .eventAt(LocalDateTime.of(2026, 7, 15, 9, 30))
                        .payloadJson("""
                                {"sessionId":1,"turnNo":2,"diagnosis":"needs_prereq","actionType":"redirect","recommendedNodeId":21,"nodeCompleted":0,"nextStepSuggestions":["先补 Java 基础"]}
                                """)
                        .build(),
                LearningEventEntity.builder()
                        .userId(9L)
                        .goalId(10L)
                        .mapNodeId(20L)
                        .eventSource("PRACTICE")
                        .eventType("ATTEMPT_SAVED")
                        .eventStatus("COMPLETED")
                        .relatedEntityType("practice_attempt")
                        .relatedEntityId(200L)
                        .eventAt(LocalDateTime.of(2026, 7, 15, 11, 0))
                        .payloadJson("""
                                {"taskType":"apply","evidenceKind":"application","estimatedMinutes":12,"handoffValidation":1,"selfRating":"stretch","normalizedRating":"clear"}
                                """)
                        .build(),
                LearningEventEntity.builder()
                        .userId(9L)
                        .goalId(10L)
                        .mapNodeId(20L)
                        .eventSource("REVIEW")
                        .eventType("ATTEMPT_SAVED")
                        .eventStatus("COMPLETED")
                        .relatedEntityType("review_attempt")
                        .relatedEntityId(300L)
                        .eventAt(LocalDateTime.of(2026, 7, 15, 18, 0))
                        .payloadJson("""
                                {"taskType":"recall","estimatedMinutes":10,"selfRating":"forgotten","scheduledDueAt":"2026-07-16T09:00:00","intervalDays":1,"masteryScoreAtAttempt":36,"completed":true}
                                """)
                        .build(),
                LearningEventEntity.builder()
                        .userId(9L)
                        .goalId(10L)
                        .eventSource("REFLECTION")
                        .eventType("REFLECTION_SUBMITTED")
                        .eventStatus("COMPLETED")
                        .relatedEntityType("reflection_entry")
                        .relatedEntityId(400L)
                        .summary("今天把前置关系真正写清楚了。")
                        .eventAt(LocalDateTime.of(2026, 7, 15, 21, 0))
                        .payloadJson("""
                                {"reflectionDate":"2026-07-15","nextAction":"明天继续做一轮迁移练习。"}
                                """)
                        .build()
        ));

        LearningEventReadService.GoalEventSnapshot snapshot = learningEventReadService.loadGoalSnapshot(10L, 9L);

        assertThat(snapshot.latestTutorByNodeId()).containsKey(20L);
        assertThat(snapshot.latestTutorByNodeId().get(20L).diagnosis()).isEqualTo("needs_prereq");
        assertThat(snapshot.latestTutorBySessionId().get(1L).recommendedNodeId()).isEqualTo(21L);
        assertThat(snapshot.latestTutorBySessionId().get(1L).nextStepSuggestions()).contains("先补 Java 基础");

        assertThat(snapshot.latestPracticeByAttemptId().get(200L).completed()).isTrue();
        assertThat(snapshot.latestPracticeByAttemptId().get(200L).rating()).isEqualTo("clear");
        assertThat(snapshot.latestPracticeByAttemptId().get(200L).minutes()).isEqualTo(12);
        assertThat(snapshot.latestPracticeByAttemptId().get(200L).handoffValidation()).isTrue();

        assertThat(snapshot.latestReviewByAttemptId().get(300L).completed()).isTrue();
        assertThat(snapshot.latestReviewByAttemptId().get(300L).rating()).isEqualTo("forgotten");
        assertThat(snapshot.latestReviewByAttemptId().get(300L).minutes()).isEqualTo(10);
        assertThat(snapshot.latestReviewByAttemptId().get(300L).intervalDays()).isEqualTo(1);

        assertThat(snapshot.latestReflectionByEntryId().get(400L).summary()).contains("前置关系");
        assertThat(snapshot.latestReflectionByEntryId().get(400L).nextAction()).contains("迁移练习");
        assertThat(snapshot.latestReflectionByEntryId().get(400L).reflectionDate()).isEqualTo(LocalDate.of(2026, 7, 15));
    }
}
