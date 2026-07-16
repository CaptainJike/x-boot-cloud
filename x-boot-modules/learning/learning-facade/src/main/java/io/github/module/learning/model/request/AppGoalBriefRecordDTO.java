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

/**
 * Goal Brief 记录 DTO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "Goal Brief 记录")
public class AppGoalBriefRecordDTO implements Serializable {

    @Schema(description = "目标 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "目标ID不能为空")
    private Long goalId;

    @Schema(description = "目标标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "目标标题不能为空")
    @Size(max = 255, message = "【目标标题】最长255位")
    private String goalTitle;

    @Schema(description = "来源类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "来源类型不能为空")
    @Size(max = 32, message = "【来源类型】最长32位")
    private String sourceType;

    @Schema(description = "目标模板 ID")
    @Size(max = 64, message = "【目标模板ID】最长64位")
    private String goalTemplateId;

    @Schema(description = "目标模板名称")
    @Size(max = 255, message = "【目标模板名称】最长255位")
    private String goalTemplateName;

    @Schema(description = "地图模板 ID")
    @Size(max = 64, message = "【地图模板ID】最长64位")
    private String mapTemplateId;

    @Schema(description = "地图模板名称")
    @Size(max = 255, message = "【地图模板名称】最长255位")
    private String mapTemplateName;

    @Schema(description = "目标验证状态")
    @Size(max = 32, message = "【目标验证状态】最长32位")
    private String goalValidationStatus;

    @Schema(description = "目标验证摘要")
    @Size(max = 2000, message = "【目标验证摘要】最长2000位")
    private String goalValidationSummary;

    @Schema(description = "最近验证时间")
    @Size(max = 40, message = "【最近验证时间】最长40位")
    private String lastValidationAt;

    @Schema(description = "Goal Brief", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Goal Brief 不能为空")
    @Valid
    private AppGoalBriefDTO brief;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "创建时间不能为空")
    @Size(max = 40, message = "【创建时间】最长40位")
    private String createdAt;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "更新时间不能为空")
    @Size(max = 40, message = "【更新时间】最长40位")
    private String updatedAt;
}
