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
 * 练习任务.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "练习任务")
public class PracticeTaskBO implements Serializable {

    private String id;
    private String type;
    private String title;
    private String prompt;
    private String expectedOutcome;
    private String hint;
    private Integer estimatedMinutes;
    private Long nodeId;
    private String nodeTitle;
    private String sourceDiagnosis;
    private List<String> relatedConcepts;
    private String evidenceKind;
    private String knowledgeFocus;
    private Boolean handoffValidation;
    private String handoffTitle;
}
