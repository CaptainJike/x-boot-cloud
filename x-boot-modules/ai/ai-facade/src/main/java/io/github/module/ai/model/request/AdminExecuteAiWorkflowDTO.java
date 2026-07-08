package io.github.module.ai.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

/**
 * 后台管理-执行工作流.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminExecuteAiWorkflowDTO implements Serializable {

    @Schema(description = "工作流定义ID", hidden = true)
    private Long workflowDefinitionId;

    @Schema(description = "默认输入内容，会写入变量 input")
    @Size(max = 4000, message = "【默认输入内容】最长4000位")
    private String input;

    @Schema(description = "执行变量")
    private Map<String, Object> variables;

    @Schema(description = "触发来源")
    @Size(max = 32, message = "【触发来源】最长32位")
    private String triggerSource;

    @Schema(description = "触发业务ID")
    @Size(max = 64, message = "【触发业务ID】最长64位")
    private String triggerId;

    @Schema(description = "链路追踪ID")
    @Size(max = 128, message = "【链路追踪ID】最长128位")
    private String traceId;
}
