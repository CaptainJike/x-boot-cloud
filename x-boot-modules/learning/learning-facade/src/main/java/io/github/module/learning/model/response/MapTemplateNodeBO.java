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
 * 地图模板节点 BO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "地图模板节点")
public class MapTemplateNodeBO implements Serializable {

    @Schema(description = "节点编码")
    private String nodeCode;

    @Schema(description = "节点标题")
    private String title;

    @Schema(description = "节点描述")
    private String description;

    @Schema(description = "学习目标")
    private String learningObjective;

    @Schema(description = "学习价值")
    private String whyItMatters;

    @Schema(description = "建议学习分钟数")
    private Integer estimatedMinutes;

    @Schema(description = "难度等级")
    private Integer difficultyLevel;

    @Schema(description = "验证方式")
    private String verificationMethod;

    @Schema(description = "完成条件")
    private String completionCriteria;

    @Schema(description = "前置节点编码")
    private List<String> prerequisiteNodeCodes;
}
