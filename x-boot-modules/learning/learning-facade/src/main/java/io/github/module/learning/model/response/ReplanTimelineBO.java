package io.github.module.learning.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.framework.core.constant.BaseConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务端计划重排时间线.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "服务端计划重排时间线")
public class ReplanTimelineBO implements Serializable {

    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @Schema(description = "生成时间")
    private LocalDateTime generatedAt;

    @Schema(description = "数据模式")
    private String mode;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "摘要")
    private String summary;

    @Schema(description = "累计重排次数")
    private Integer totalReplans;

    @Schema(description = "当前稳定性标签")
    private String stabilityLabel;

    @Schema(description = "最近重排原因")
    private String latestReason;

    @Schema(description = "当前漂移判断")
    private String currentDrift;

    @Schema(description = "下一次建议调整")
    private String nextAdjustment;

    @Schema(description = "计划历史条目")
    private List<PlannerHistoryEntryBO> items;
}
