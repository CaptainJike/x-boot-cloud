package io.github.module.ai.model.response;

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
import java.util.Map;

/**
 * AI 工作流节点执行结果 BO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminAiWorkflowNodeExecutionResultBO implements Serializable {

    @Schema(description = "节点Key")
    private String nodeKey;

    @Schema(description = "节点类型")
    private String nodeType;

    @Schema(description = "状态(0=失败 1=成功)")
    private Integer status;

    @Schema(description = "输出文本")
    private String outputText;

    @Schema(description = "输出变量")
    private Map<String, Object> outputVariables;

    @Schema(description = "下游节点Key")
    private String nextNodeKey;

    @Schema(description = "是否终止节点")
    private Boolean terminalNode;

    @Schema(description = "工作流是否成功")
    private Boolean workflowSuccess;

    @Schema(description = "最终输出")
    private String finalOutput;

    @Schema(description = "错误编码")
    private String errorCode;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "耗时，单位毫秒")
    private Long durationMs;

    @Schema(description = "执行开始时刻")
    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime startedAt;

    @Schema(description = "执行结束时刻")
    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime finishedAt;
}
