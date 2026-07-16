package io.github.module.learning.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 模板创建/更新 DTO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "模板创建/更新请求")
public class AppTemplateUpsertDTO implements Serializable {

    @Schema(description = "模板类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "模板类型不能为空")
    @Pattern(regexp = "GOAL|MAP", message = "模板类型无效")
    private String type;

    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "模板名称不能为空")
    @Size(max = 255, message = "【模板名称】最长255位")
    private String name;

    @Schema(description = "模板摘要", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "模板摘要不能为空")
    @Size(max = 1000, message = "【模板摘要】最长1000位")
    private String summary;

    @Schema(description = "面向人群", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "面向人群不能为空")
    @Size(max = 500, message = "【面向人群】最长500位")
    private String audience;

    @Schema(description = "领域", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "领域不能为空")
    @Size(max = 255, message = "【领域】最长255位")
    private String domain;

    @Schema(description = "标签")
    @Size(max = 20, message = "【标签】最多20条")
    private List<@Size(max = 64, message = "【标签项】最长64位") String> tags;

    @Schema(description = "可见性", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "可见性不能为空")
    @Pattern(regexp = "PRIVATE|MARKET_READY", message = "可见性无效")
    private String visibility;

    @Schema(description = "是否有市场化意图", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "市场化意图不能为空")
    private Boolean marketIntent;

    @Schema(description = "发布状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "发布状态不能为空")
    @Pattern(regexp = "DRAFT|READY_FOR_MARKET", message = "发布状态无效")
    private String publishStatus;

    @Schema(description = "来源类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "来源类型不能为空")
    @Pattern(regexp = "manual|ai|goal_template|map_template|starter_template", message = "来源类型无效")
    private String sourceType;

    @Schema(description = "Goal Brief", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Goal Brief 不能为空")
    @Valid
    private AppGoalBriefDTO brief;

    @Schema(description = "地图生成摘要")
    @Size(max = 1000, message = "【地图生成摘要】最长1000位")
    private String generationSummary;

    @Schema(description = "地图模板快照")
    @Valid
    private AppMapTemplateSnapshotDTO mapSnapshot;
}
