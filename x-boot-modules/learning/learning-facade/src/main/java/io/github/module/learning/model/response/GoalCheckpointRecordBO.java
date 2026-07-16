package io.github.module.learning.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 阶段复盘记录 BO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "阶段复盘记录")
public class GoalCheckpointRecordBO implements Serializable {

    private String id;
    private String recordedAt;
    private String title;
    private String summary;
    private String decision;
    private String decisionTitle;
    private String nextStepTitle;
    private String templateValidationBadge;
    private String templateValidationSummary;
    private List<String> evidenceHighlights;
    private String checkpointReason;
}
