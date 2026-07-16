package io.github.module.learning.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 周计划分桶.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "周计划分桶")
public class WeeklyPlanBucketBO implements Serializable {

    @Schema(description = "分桶标识")
    private String id;

    @Schema(description = "分桶类型")
    private String kind;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "摘要")
    private String summary;

    @Schema(description = "建议分钟数")
    private Integer recommendedMinutes;

    @Schema(description = "建议原因")
    private String reason;
}
