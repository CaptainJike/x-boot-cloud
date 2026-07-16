package io.github.module.learning.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * Goal Brief 记录 BO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "Goal Brief 记录")
public class GoalBriefRecordBO implements Serializable {

    private Long goalId;
    private String goalTitle;
    private String sourceType;
    private String goalTemplateId;
    private String goalTemplateName;
    private String mapTemplateId;
    private String mapTemplateName;
    private String goalValidationStatus;
    private String goalValidationSummary;
    private String lastValidationAt;
    private GoalBriefBO brief;
    private String createdAt;
    private String updatedAt;
}
