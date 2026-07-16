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
 * 练习提交记录.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "练习提交记录")
public class PracticeAttemptBO implements Serializable {

    private String taskId;
    private Long nodeId;
    private String taskType;
    private String evidenceKind;
    private String response;
    private String selfRating;
    private List<ArtifactBO> artifacts;
    private AssessmentBO assessment;
    private Boolean completed;
    private Boolean handoffValidation;

    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime updatedAt;

    private Long serverId;
    private Long serverVersion;
    private String lastMutationId;
    private String syncStatus;

    @Accessors(chain = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class ArtifactBO implements Serializable {
        private String id;
        private String kind;
        private String title;
        private String content;
        private String url;
        private String language;
        private String fileName;
        private String mimeType;
        private Long sizeBytes;
        private String createdAt;
    }

    @Accessors(chain = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class AssessmentBO implements Serializable {
        private String mode;
        private String level;
        private Integer score;
        private String summary;
        private List<CriterionBO> criteria;
        private String assessedAt;
    }

    @Accessors(chain = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class CriterionBO implements Serializable {
        private String id;
        private String label;
        private String status;
        private Integer score;
        private Integer maxScore;
        private String feedback;
    }
}
