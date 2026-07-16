package io.github.module.learning.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 学习节奏单日快照.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "学习节奏单日快照")
public class LearningRhythmDayBO implements Serializable {

    @Schema(description = "日期")
    private String date;

    @Schema(description = "展示标签")
    private String label;

    @Schema(description = "当天累计分钟数")
    private Integer minutes;

    @Schema(description = "当天是否有学习动作")
    private Boolean active;

    @Schema(description = "是否为今天")
    private Boolean isToday;
}
