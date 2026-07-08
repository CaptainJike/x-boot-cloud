package io.github.module.ai.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 工作流节点执行结果.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AiWorkflowNodeExecutionResult {

    public static final int STATUS_FAILED = 0;

    public static final int STATUS_SUCCESS = 1;

    private String nodeKey;

    private String nodeType;

    private Integer status;

    private String prompt;

    private String outputText;

    private Map<String, Object> outputVariables;

    private String modelConfigCode;

    private String providerType;

    private String modelName;

    private String httpMethod;

    private String requestUrl;

    private Integer httpStatusCode;

    private String conditionExpression;

    private Boolean conditionMatched;

    private String selectedBranchKey;

    private String nextNodeKey;

    private Boolean terminalNode;

    private Boolean workflowSuccess;

    private String finalOutput;

    private String errorCode;

    private String errorMessage;

    private Long durationMs;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;
}
