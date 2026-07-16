package io.github.module.learning.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 地图模板快照 DTO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "地图模板快照")
public class AppMapTemplateSnapshotDTO implements Serializable {

    @Schema(description = "生成摘要")
    @Size(max = 1000, message = "【生成摘要】最长1000位")
    private String generationSummary;

    @Schema(description = "预计完成天数")
    private Integer estimatedDays;

    @Schema(description = "当前激活节点标题")
    @Size(max = 255, message = "【当前激活节点标题】最长255位")
    private String activeNodeTitle;

    @Schema(description = "节点列表")
    @Valid
    @Size(max = 100, message = "【地图模板节点】最多100个")
    private List<AppMapTemplateNodeDTO> nodes;
}
