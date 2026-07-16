package io.github.module.learning.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 学习计划任务.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "学习计划任务")
public class LearningPlanTaskBO implements Serializable {

    private String id;
    private String kind;
    private String title;
    private String summary;
    private String reason;
    private Integer estimatedMinutes;
    private Long nodeId;
    private String nodeTitle;
    private String targetSection;
    private Integer priority;
    private Boolean recoveryMode;
}
