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
 * Goal Draft Assist BO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "Goal Draft AI 补全结果")
public class GoalDraftAssistBO implements Serializable {

    @Schema(description = "补全后的 Goal Brief")
    private GoalBriefBO draftBrief;

    @Schema(description = "追问问题")
    private List<String> followUpQuestions;

    @Schema(description = "补全摘要")
    private String builderSummary;

    @Schema(description = "置信度")
    private Double confidence;
}
