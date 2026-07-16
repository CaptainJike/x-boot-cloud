package io.github.module.learning.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.framework.core.constant.BaseConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 学习 Agent 快照.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "学习 Agent 快照")
public class LearningAgentBO implements Serializable {

    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime generatedAt;

    private String mode;
    private String presenceTitle;
    private String presenceSummary;
    private Integer urgentCount;
    private String resumeTitle;
    private String resumeSummary;
    private String replanSummary;
    private String reentryReason;
    private List<AgendaStepBO> nextTwoSteps;
    private List<String> carryOverNotes;
    private List<String> watchouts;
    private List<InterventionBO> interventions;
    private List<SceneNudgeBO> sceneNudges;

    @Accessors(chain = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class InterventionBO implements Serializable {
        private String id;
        private String kind;
        private String title;
        private String summary;
        private String whyNow;
        private String targetSection;
        private Long nodeId;
        private String nodeTitle;
        private Integer priority;
    }

    @Accessors(chain = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class AgendaStepBO implements Serializable {
        private String id;
        private String title;
        private String summary;
        private String targetSection;
        private Long nodeId;
        private String nodeTitle;
    }

    @Accessors(chain = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class SceneNudgeBO implements Serializable {
        private String id;
        private String contextLabel;
        private String prompt;
    }
}
