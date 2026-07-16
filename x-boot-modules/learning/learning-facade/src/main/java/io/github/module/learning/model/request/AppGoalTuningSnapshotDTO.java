package io.github.module.learning.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * Goal 调参快照 DTO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "Goal 调参快照")
public class AppGoalTuningSnapshotDTO implements Serializable {

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "创建时间不能为空")
    @Size(max = 40, message = "【创建时间】最长40位")
    private String createdAt;

    @Schema(description = "模式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "模式不能为空")
    @Size(max = 32, message = "【模式】最长32位")
    private String mode;

    @Schema(description = "目标标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "目标标题不能为空")
    @Size(max = 255, message = "【目标标题】最长255位")
    private String goalTitle;

    @Schema(description = "源目标 ID")
    private Long sourceGoalId;

    @Schema(description = "候选目标 ID")
    private Long candidateGoalId;

    @Schema(description = "复盘决策", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "复盘决策不能为空")
    @Size(max = 32, message = "【复盘决策】最长32位")
    private String checkpointDecision;

    @Schema(description = "复盘标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "复盘标题不能为空")
    @Size(max = 255, message = "【复盘标题】最长255位")
    private String checkpointTitle;

    @Schema(description = "复盘原因", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "复盘原因不能为空")
    @Size(max = 2000, message = "【复盘原因】最长2000位")
    private String checkpointReason;

    @Schema(description = "调参摘要", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "调参摘要不能为空")
    @Size(max = 2000, message = "【调参摘要】最长2000位")
    private String tuningSummary;

    @Schema(description = "建议的 Goal Brief", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "建议的Goal Brief不能为空")
    @Valid
    private AppGoalBriefDTO suggestedBrief;

    @Schema(description = "详细调整建议")
    @Valid
    private List<AppGoalTuningSuggestionDTO> changes;

    @Schema(description = "承接问题")
    private List<String> carryOverQuestions;
}
