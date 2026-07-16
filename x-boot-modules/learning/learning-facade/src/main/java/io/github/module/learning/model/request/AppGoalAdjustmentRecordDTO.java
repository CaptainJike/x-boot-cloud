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
 * Goal 调整记录 DTO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "Goal 调整记录")
public class AppGoalAdjustmentRecordDTO implements Serializable {

    @Schema(description = "记录 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "记录ID不能为空")
    @Size(max = 128, message = "【记录ID】最长128位")
    private String id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "创建时间不能为空")
    @Size(max = 40, message = "【创建时间】最长40位")
    private String createdAt;

    @Schema(description = "源目标 ID")
    private Long sourceGoalId;

    @Schema(description = "源目标标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "源目标标题不能为空")
    @Size(max = 255, message = "【源目标标题】最长255位")
    private String sourceGoalTitle;

    @Schema(description = "下一个目标 ID")
    private Long nextGoalId;

    @Schema(description = "下一个目标标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "下一个目标标题不能为空")
    @Size(max = 255, message = "【下一个目标标题】最长255位")
    private String nextGoalTitle;

    @Schema(description = "复盘决策", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "复盘决策不能为空")
    @Size(max = 32, message = "【复盘决策】最长32位")
    private String checkpointDecision;

    @Schema(description = "复盘标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "复盘标题不能为空")
    @Size(max = 255, message = "【复盘标题】最长255位")
    private String checkpointTitle;

    @Schema(description = "调参摘要", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "调参摘要不能为空")
    @Size(max = 2000, message = "【调参摘要】最长2000位")
    private String tuningSummary;

    @Schema(description = "变更字段")
    private List<String> changedFields;

    @Schema(description = "详细变更")
    @Valid
    private List<AppGoalTuningSuggestionDTO> changes;

    @Schema(description = "源 Goal Brief", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "源 Goal Brief 不能为空")
    @Valid
    private AppGoalBriefDTO sourceBrief;

    @Schema(description = "结果 Goal Brief", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "结果 Goal Brief 不能为空")
    @Valid
    private AppGoalBriefDTO resultingBrief;

    @Schema(description = "候选验证摘要")
    @Size(max = 2000, message = "【候选验证摘要】最长2000位")
    private String candidateValidationSummary;

    @Schema(description = "候选验证置信度")
    private Double candidateValidationConfidence;

    @Schema(description = "候选验证证据")
    @Size(max = 2000, message = "【候选验证证据】最长2000位")
    private String candidateValidationProof;

    @Schema(description = "候选验证风险")
    @Size(max = 2000, message = "【候选验证风险】最长2000位")
    private String candidateValidationRisk;
}
