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
 * 学习者长期记忆快照.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "学习者长期记忆快照")
public class LearnerMemoryBO implements Serializable {

    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime generatedAt;

    private String mode;
    private String summary;
    private String momentumTitle;
    private String momentumSummary;
    private String masterySignal;
    private String goalValidationStatus;
    private String goalValidationSummary;
    private List<String> strengths;
    private List<String> relationStrengths;
    private List<String> weakSignals;
    private List<String> relationWatchouts;
    private List<String> habits;
    private List<String> recommendedAdjustments;
    private List<EvidenceBO> evidence;

    @Accessors(chain = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class EvidenceBO implements Serializable {
        private String label;
        private String detail;
    }
}
