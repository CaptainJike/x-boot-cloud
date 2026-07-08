package io.github.module.ai.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 工作流 LLM 节点配置.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AiWorkflowLlmNodeConfig {

    private String modelConfigCode;

    private String promptTemplate;

    private String inputVariable;

    private String outputVariable;
}
