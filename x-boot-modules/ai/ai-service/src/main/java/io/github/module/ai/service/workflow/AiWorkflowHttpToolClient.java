package io.github.module.ai.service.workflow;

import io.github.module.ai.service.model.AiWorkflowHttpToolRequest;
import io.github.module.ai.service.model.AiWorkflowHttpToolResponse;

/**
 * 工作流 HTTP 工具调用适配器.
 */
public interface AiWorkflowHttpToolClient {

    /**
     * 执行 HTTP 工具请求.
     */
    AiWorkflowHttpToolResponse exchange(AiWorkflowHttpToolRequest request);
}
