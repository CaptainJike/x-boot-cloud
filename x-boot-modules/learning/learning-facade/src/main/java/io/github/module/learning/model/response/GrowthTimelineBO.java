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
 * 成长时间线 BO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "成长时间线")
public class GrowthTimelineBO implements Serializable {

    @Schema(description = "成长摘要")
    private String overview;

    @Schema(description = "关键认知变化")
    private List<String> keyCognitiveChanges;

    @Schema(description = "常见卡点")
    private List<String> commonStickingPoints;

    @Schema(description = "时间线事件列表")
    private List<GrowthTimelineItemBO> items;
}
