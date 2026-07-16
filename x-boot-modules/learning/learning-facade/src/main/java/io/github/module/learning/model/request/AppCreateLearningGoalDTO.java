package io.github.module.learning.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 创建学习目标请求.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "创建学习目标请求")
public class AppCreateLearningGoalDTO implements Serializable {

    @Schema(description = "学习目标主题", requiredMode = Schema.RequiredMode.REQUIRED, example = "学习 Spring AI")
    @NotBlank(message = "学习目标不能为空")
    @Size(max = 120, message = "【学习目标】最长120位")
    private String targetTopic;

    @Schema(description = "用户自评基础", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "我会 Spring Boot，但没用过 Spring AI 和 MCP")
    @NotBlank(message = "自评基础不能为空")
    @Size(max = 2000, message = "【自评基础】最长2000位")
    private String selfAssessment;

    @Schema(description = "每周学习分钟数", requiredMode = Schema.RequiredMode.REQUIRED, example = "300")
    @Min(value = 30, message = "【每周学习分钟数】最少30分钟")
    @Max(value = 10080, message = "【每周学习分钟数】最多10080分钟")
    private Integer weeklyLearningMinutes;

    @Schema(description = "偏好学习风格", requiredMode = Schema.RequiredMode.REQUIRED, example = "喜欢结合例子和项目实践")
    @NotBlank(message = "偏好学习风格不能为空")
    @Size(max = 500, message = "【偏好学习风格】最长500位")
    private String preferredLearningStyle;

    @Schema(description = "Goal Brief 草稿")
    @Valid
    private AppGoalBriefDTO brief;

    @Schema(description = "来源类型")
    @Pattern(regexp = "manual|ai|goal_template|map_template|starter_template", message = "来源类型无效")
    private String sourceType;

    @Schema(description = "选中的目标模板ID")
    @Size(max = 64, message = "【目标模板ID】最长64位")
    private String goalTemplateId;

    @Schema(description = "选中的地图模板ID")
    @Size(max = 64, message = "【地图模板ID】最长64位")
    private String mapTemplateId;

    @Schema(description = "是否同时保存为目标模板")
    private Boolean saveGoalTemplate;
}
