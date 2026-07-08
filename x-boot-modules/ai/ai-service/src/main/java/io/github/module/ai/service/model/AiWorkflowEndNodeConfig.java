package io.github.module.ai.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * 工作流结束节点配置.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AiWorkflowEndNodeConfig {

    private Map<String, String> outputMappings;

    private String outputTemplate;

    private String outputVariable;

    private Boolean success;

    private String statusVariable;

    private String errorCode;

    private String errorMessage;

    private String errorCodeVariable;

    private String errorMessageVariable;

    private Boolean failWhenMissingVariable;
}
