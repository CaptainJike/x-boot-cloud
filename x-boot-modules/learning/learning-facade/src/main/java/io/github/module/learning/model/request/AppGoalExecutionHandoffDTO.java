package io.github.module.learning.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * Goal 交接状态 DTO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "Goal 交接状态")
public class AppGoalExecutionHandoffDTO implements Serializable {

    @Schema(description = "交接 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "交接ID不能为空")
    @Size(max = 128, message = "【交接ID】最长128位")
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

    @Schema(description = "交接标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "交接标题不能为空")
    @Size(max = 255, message = "【交接标题】最长255位")
    private String handoffTitle;

    @Schema(description = "交接摘要", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "交接摘要不能为空")
    @Size(max = 2000, message = "【交接摘要】最长2000位")
    private String handoffSummary;

    @Schema(description = "首个任务标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "首个任务标题不能为空")
    @Size(max = 255, message = "【首个任务标题】最长255位")
    private String firstMissionTitle;

    @Schema(description = "首个任务摘要", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "首个任务摘要不能为空")
    @Size(max = 2000, message = "【首个任务摘要】最长2000位")
    private String firstMissionSummary;

    @Schema(description = "延续动作")
    private List<String> carryOverActions;

    @Schema(description = "风险关注")
    private List<String> watchouts;
}
