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
 * 节点掌握记录.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "节点掌握记录")
public class MasteryRecordBO implements Serializable {

    private Long nodeId;
    private String nodeTitle;
    private String nodeCode;
    private Integer masteryScore;
    private String masteryLevel;
    private String progressStatus;
    private String strongestSignal;
    private String handoffValidationSignal;
    private String explanationStatus;
    private String applicationStatus;
    private String prerequisiteStatus;
    private String reviewState;

    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime updatedAt;
}
