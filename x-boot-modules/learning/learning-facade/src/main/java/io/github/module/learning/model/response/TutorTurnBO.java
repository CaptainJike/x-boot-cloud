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
 * Tutor 单轮输出 BO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "Tutor 单轮输出")
public class TutorTurnBO implements Serializable {

    @Schema(description = "轮次ID")
    private Long id;

    @Schema(description = "会话ID")
    private Long sessionId;

    @Schema(description = "诊断结果")
    private String diagnosis;

    @Schema(description = "Tutor 动作")
    private String actionType;

    @Schema(description = "诊断问题列表")
    private List<String> diagnosticQuestions;

    @Schema(description = "Tutor 回应")
    private String tutorResponse;

    @Schema(description = "补充学习建议")
    private List<String> nextStepSuggestions;

    @Schema(description = "建议切换到的节点ID")
    private Long recommendedNodeId;

    @Schema(description = "是否已完成当前节点")
    private Boolean nodeCompleted;

    @Schema(description = "创建时刻")
    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime createdAt;
}
