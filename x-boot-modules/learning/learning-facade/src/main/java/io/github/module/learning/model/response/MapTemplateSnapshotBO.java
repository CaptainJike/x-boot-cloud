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
 * 地图模板快照 BO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "地图模板快照")
public class MapTemplateSnapshotBO implements Serializable {

    @Schema(description = "生成摘要")
    private String generationSummary;

    @Schema(description = "预计完成天数")
    private Integer estimatedDays;

    @Schema(description = "当前激活节点标题")
    private String activeNodeTitle;

    @Schema(description = "节点列表")
    private List<MapTemplateNodeBO> nodes;
}
