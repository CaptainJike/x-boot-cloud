package io.github.module.learning.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * Portfolio 候选目标验证记录 DTO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "Portfolio 候选目标验证记录")
public class AppPortfolioCandidateValidationRecordDTO implements Serializable {

    @Schema(description = "目标 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "目标ID不能为空")
    private Long goalId;

    @Schema(description = "目标标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "目标标题不能为空")
    @Size(max = 255, message = "【目标标题】最长255位")
    private String goalTitle;

    @Schema(description = "验证摘要", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "验证摘要不能为空")
    @Size(max = 2000, message = "【验证摘要】最长2000位")
    private String summary;

    @Schema(description = "为什么更好", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "为什么更好不能为空")
    @Size(max = 2000, message = "【为什么更好】最长2000位")
    private String whyBetter;

    @Schema(description = "第一条证据", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "第一条证据不能为空")
    @Size(max = 2000, message = "【第一条证据】最长2000位")
    private String firstProof;

    @Schema(description = "风险关注")
    @Size(max = 2000, message = "【风险关注】最长2000位")
    private String riskWatchout;

    @Schema(description = "置信度", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "置信度不能为空")
    @Min(value = 1, message = "置信度最小为1")
    @Max(value = 5, message = "置信度最大为5")
    private Integer confidence;

    @Schema(description = "决策", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "决策不能为空")
    @Size(max = 32, message = "【决策】最长32位")
    private String decision;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "更新时间不能为空")
    @Size(max = 40, message = "【更新时间】最长40位")
    private String updatedAt;

    @Schema(description = "归档时间")
    @Size(max = 40, message = "【归档时间】最长40位")
    private String archivedAt;

    @Schema(description = "归档原因")
    @Size(max = 32, message = "【归档原因】最长32位")
    private String archivedReason;
}
