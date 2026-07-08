package io.github.module.ai.service.workflow;

import io.github.module.ai.entity.AiWorkflowNodeEntity;
import io.github.module.ai.service.model.AiWorkflowNodeExecutionContext;
import io.github.module.ai.service.model.AiWorkflowNodeExecutionResult;

/**
 * 工作流节点执行器.
 */
public interface AiWorkflowNodeExecutor {

    /**
     * 是否支持节点类型.
     */
    boolean supports(String nodeType);

    /**
     * 执行节点.
     */
    AiWorkflowNodeExecutionResult execute(AiWorkflowNodeEntity node, AiWorkflowNodeExecutionContext context);
}
