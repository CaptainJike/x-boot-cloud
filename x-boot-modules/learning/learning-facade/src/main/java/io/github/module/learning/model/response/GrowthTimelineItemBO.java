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
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 成长时间线项 BO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "成长时间线项")
public class GrowthTimelineItemBO implements Serializable {

    @Schema(description = "日期")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_FORMAT)
    private LocalDate snapshotDate;

    @Schema(description = "精确记录时间")
    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime recordedAt;

    @Schema(description = "事件类型")
    private String eventType;

    @Schema(description = "事件来源")
    private String eventSource;

    @Schema(description = "事件状态")
    private String eventStatus;

    @Schema(description = "事件细分类型")
    private String eventDetailType;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "摘要")
    private String summary;

    @Schema(description = "关联目标ID")
    private Long goalId;

    @Schema(description = "关联实体类型")
    private String relatedEntityType;

    @Schema(description = "关联实体ID")
    private Long relatedEntityId;
}
