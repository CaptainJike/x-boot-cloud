package io.github.module.ai.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * 工作流节点执行上下文.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AiWorkflowNodeExecutionContext {

    private String executionId;

    private Long workflowDefinitionId;

    private String workflowCode;

    private String traceId;

    private Map<String, Object> variables;
}
