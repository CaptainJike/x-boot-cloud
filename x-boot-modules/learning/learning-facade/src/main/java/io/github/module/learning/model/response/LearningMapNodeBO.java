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
 * 学习地图节点 BO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "学习地图节点")
public class LearningMapNodeBO implements Serializable {

    @Schema(description = "节点ID")
    private Long id;

    @Schema(description = "节点编码")
    private String nodeCode;

    @Schema(description = "节点标题")
    private String title;

    @Schema(description = "节点描述")
    private String description;

    @Schema(description = "学习目标")
    private String learningObjective;

    @Schema(description = "为什么要学")
    private String whyItMatters;

    @Schema(description = "建议学习时长，单位分钟")
    private Integer estimatedMinutes;

    @Schema(description = "难度等级")
    private Integer difficultyLevel;

    @Schema(description = "验证方式")
    private String verificationMethod;

    @Schema(description = "完成条件")
    private String completionCriteria;

    @Schema(description = "前置节点编码")
    private List<String> prerequisiteNodeCodes;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "当前进度状态")
    private String progressStatus;

    @Schema(description = "是否当前激活节点")
    private Boolean active;
}
