package io.github.module.learning.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 地图模板节点 DTO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "地图模板节点")
public class AppMapTemplateNodeDTO implements Serializable {

    @Schema(description = "节点编码")
    @Size(max = 64, message = "【节点编码】最长64位")
    private String nodeCode;

    @Schema(description = "节点标题")
    @Size(max = 255, message = "【节点标题】最长255位")
    private String title;

    @Schema(description = "节点描述")
    @Size(max = 2000, message = "【节点描述】最长2000位")
    private String description;

    @Schema(description = "学习目标")
    @Size(max = 2000, message = "【学习目标】最长2000位")
    private String learningObjective;

    @Schema(description = "学习价值")
    @Size(max = 2000, message = "【学习价值】最长2000位")
    private String whyItMatters;

    @Schema(description = "建议学习分钟数")
    @Min(value = 1, message = "【建议学习分钟数】最少1分钟")
    @Max(value = 10080, message = "【建议学习分钟数】最多10080分钟")
    private Integer estimatedMinutes;

    @Schema(description = "难度等级")
    @Min(value = 1, message = "【难度等级】最小1")
    @Max(value = 5, message = "【难度等级】最大5")
    private Integer difficultyLevel;

    @Schema(description = "验证方式")
    @Size(max = 1000, message = "【验证方式】最长1000位")
    private String verificationMethod;

    @Schema(description = "完成条件")
    @Size(max = 2000, message = "【完成条件】最长2000位")
    private String completionCriteria;

    @Schema(description = "前置节点编码")
    @Size(max = 20, message = "【前置节点编码】最多20条")
    private List<@Size(max = 64, message = "【前置节点编码项】最长64位") String> prerequisiteNodeCodes;
}
