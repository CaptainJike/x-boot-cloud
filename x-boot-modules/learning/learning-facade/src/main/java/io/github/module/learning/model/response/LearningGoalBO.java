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

/**
 * 学习目标 BO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "学习目标")
public class LearningGoalBO implements Serializable {

    @Schema(description = "目标ID")
    private Long id;

    @Schema(description = "学习目标主题")
    private String targetTopic;

    @Schema(description = "用户自评基础")
    private String selfAssessment;

    @Schema(description = "每周学习分钟数")
    private Integer weeklyLearningMinutes;

    @Schema(description = "偏好学习风格")
    private String preferredLearningStyle;

    @Schema(description = "当前状态")
    private String status;

    @Schema(description = "当前激活节点ID")
    private Long activeNodeId;

    @Schema(description = "预计完成天数")
    private Integer estimatedDays;

    @Schema(description = "模板编码")
    private String templateCode;

    @Schema(description = "创建时刻")
    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime updatedAt;
}
