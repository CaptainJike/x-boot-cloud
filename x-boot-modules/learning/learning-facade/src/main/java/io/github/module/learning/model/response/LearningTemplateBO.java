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
 * 学习模板资产 BO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "学习模板资产")
public class LearningTemplateBO implements Serializable {

    @Schema(description = "模板ID")
    private Long id;

    @Schema(description = "模板类型")
    private String type;

    @Schema(description = "模板名称")
    private String name;

    @Schema(description = "模板摘要")
    private String summary;

    @Schema(description = "领域")
    private String domain;

    @Schema(description = "面向人群")
    private String audience;

    @Schema(description = "标签")
    private List<String> tags;

    @Schema(description = "可见性")
    private String visibility;

    @Schema(description = "是否有市场化意图")
    private Boolean marketIntent;

    @Schema(description = "发布状态")
    private String publishStatus;

    @Schema(description = "使用次数")
    private Integer usageCount;

    @Schema(description = "来源类型")
    private String sourceType;

    @Schema(description = "Goal Brief")
    private GoalBriefBO brief;

    @Schema(description = "地图生成摘要")
    private String generationSummary;

    @Schema(description = "地图模板快照")
    private MapTemplateSnapshotBO mapSnapshot;

    @Schema(description = "创建时刻")
    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime updatedAt;
}
