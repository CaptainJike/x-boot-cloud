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
 * 阶段复盘记录 DTO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "阶段复盘记录")
public class AppGoalCheckpointRecordDTO implements Serializable {

    @Schema(description = "记录 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "记录ID不能为空")
    @Size(max = 128, message = "【记录ID】最长128位")
    private String id;

    @Schema(description = "记录时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "记录时间不能为空")
    @Size(max = 40, message = "【记录时间】最长40位")
    private String recordedAt;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "【标题】最长255位")
    private String title;

    @Schema(description = "摘要", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "摘要不能为空")
    @Size(max = 2000, message = "【摘要】最长2000位")
    private String summary;

    @Schema(description = "决策", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "决策不能为空")
    @Size(max = 32, message = "【决策】最长32位")
    private String decision;

    @Schema(description = "决策标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "决策标题不能为空")
    @Size(max = 255, message = "【决策标题】最长255位")
    private String decisionTitle;

    @Schema(description = "下一步标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "下一步标题不能为空")
    @Size(max = 255, message = "【下一步标题】最长255位")
    private String nextStepTitle;

    @Schema(description = "模板验证徽标")
    @Size(max = 64, message = "【模板验证徽标】最长64位")
    private String templateValidationBadge;

    @Schema(description = "模板验证摘要")
    @Size(max = 2000, message = "【模板验证摘要】最长2000位")
    private String templateValidationSummary;

    @Schema(description = "证据亮点")
    private List<String> evidenceHighlights;

    @Schema(description = "复盘原因", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "复盘原因不能为空")
    @Size(max = 2000, message = "【复盘原因】最长2000位")
    private String checkpointReason;
}
