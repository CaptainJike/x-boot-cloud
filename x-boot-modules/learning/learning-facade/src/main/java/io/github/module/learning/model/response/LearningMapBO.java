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
 * 学习地图 BO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "学习地图")
public class LearningMapBO implements Serializable {

    @Schema(description = "地图ID")
    private Long id;

    @Schema(description = "目标信息")
    private LearningGoalBO goal;

    @Schema(description = "节点列表")
    private List<LearningMapNodeBO> nodes;

    @Schema(description = "生成版本")
    private Integer generationVersion;

    @Schema(description = "生成说明")
    private String generationSummary;
}
