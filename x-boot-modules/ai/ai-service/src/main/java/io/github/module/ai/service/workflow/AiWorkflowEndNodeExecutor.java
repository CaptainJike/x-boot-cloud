package io.github.module.ai.service.workflow;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.framework.core.exception.BusinessException;
import io.github.module.ai.entity.AiWorkflowNodeEntity;
import io.github.module.ai.service.model.AiWorkflowEndNodeConfig;
import io.github.module.ai.service.model.AiWorkflowNodeExecutionContext;
import io.github.module.ai.service.model.AiWorkflowNodeExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工作流结束节点执行器.
 */
@RequiredArgsConstructor
@Component
public class AiWorkflowEndNodeExecutor implements AiWorkflowNodeExecutor {

    public static final String NODE_TYPE = "end";

    public static final String NODE_TYPE_END_NODE = "end_node";

    private static final String DEFAULT_OUTPUT_VARIABLE = "finalOutput";

    private static final String DEFAULT_ERROR_CODE = "WORKFLOW_END_FAILED";

    private static final Pattern TEMPLATE_VARIABLE_PATTERN = Pattern.compile("\\$\\{([A-Za-z0-9_.-]+)}");

    private static final Pattern EXACT_VARIABLE_PATTERN = Pattern.compile("^\\$\\{([A-Za-z0-9_.-]+)}$");

    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(String nodeType) {
        String cleanType = trim(nodeType);
        return NODE_TYPE.equalsIgnoreCase(cleanType) || NODE_TYPE_END_NODE.equalsIgnoreCase(cleanType);
    }

    @Override
    public AiWorkflowNodeExecutionResult execute(AiWorkflowNodeEntity node, AiWorkflowNodeExecutionContext context) {
        long startAt = System.currentTimeMillis();
        LocalDateTime startedAt = LocalDateTime.now();
        try {
            validateNode(node);
            validateNoNextNode(node);
            AiWorkflowEndNodeConfig config = parseConfig(node.getNodeConfig());
            Map<String, Object> variables = variables(context);
            EndOutput output = buildOutput(config, variables);
            boolean workflowSuccess = resolveWorkflowSuccess(config, variables);
            if (workflowSuccess) {
                return success(node, output, startAt, startedAt);
            }
            return workflowFailure(node, config, variables, output, startAt, startedAt);
        } catch (RuntimeException ex) {
            return failure(node, ex, startAt, startedAt);
        }
    }

    private void validateNode(AiWorkflowNodeEntity node) {
        if (node == null) {
            throw new BusinessException(400, "结束节点不存在");
        }
        if (!supports(node.getNodeType())) {
            throw new BusinessException(400, "非结束节点不支持结束执行器");
        }
    }

    private void validateNoNextNode(AiWorkflowNodeEntity node) {
        if (!parseNextNodeKeys(node.getNextNodeKeys()).isEmpty()) {
            throw new BusinessException(400, "结束节点不允许配置下游节点");
        }
    }

    private AiWorkflowEndNodeConfig parseConfig(String configJson) {
        if (StrUtil.isBlank(configJson)) {
            return new AiWorkflowEndNodeConfig();
        }
        try {
            return objectMapper.readValue(configJson, AiWorkflowEndNodeConfig.class);
        } catch (JsonProcessingException e) {
            throw new BusinessException(400, "结束节点配置JSON格式错误");
        }
    }

    private EndOutput buildOutput(AiWorkflowEndNodeConfig config, Map<String, Object> variables) {
        Map<String, Object> outputVariables = new LinkedHashMap<>();
        if (config.getOutputMappings() != null && !config.getOutputMappings().isEmpty()) {
            for (Map.Entry<String, String> entry : config.getOutputMappings().entrySet()) {
                String outputName = trim(entry.getKey());
                if (StrUtil.isBlank(outputName)) {
                    throw new BusinessException(400, "结束节点输出字段不能为空");
                }
                outputVariables.put(outputName, resolveOutputValue(entry.getValue(), variables, config));
            }
        }

        String outputTemplate = trim(config.getOutputTemplate());
        if (StrUtil.isNotBlank(outputTemplate)) {
            String outputText = renderTemplate(outputTemplate, variables, config);
            String outputVariable = StrUtil.blankToDefault(trim(config.getOutputVariable()), DEFAULT_OUTPUT_VARIABLE);
            outputVariables.put(outputVariable, outputText);
            return new EndOutput(outputText, outputVariables);
        }

        if (outputVariables.isEmpty()) {
            outputVariables.putAll(variables);
        }
        return new EndOutput(writeOutputSummary(outputVariables), outputVariables);
    }

    private Object resolveOutputValue(String mappingExpression,
                                      Map<String, Object> variables,
                                      AiWorkflowEndNodeConfig config) {
        String cleanExpression = trim(mappingExpression);
        if (StrUtil.isBlank(cleanExpression)) {
            throw new BusinessException(400, "结束节点输出变量不能为空");
        }
        Matcher exactMatcher = EXACT_VARIABLE_PATTERN.matcher(cleanExpression);
        if (exactMatcher.matches()) {
            return variableValue(exactMatcher.group(1), variables, config);
        }
        if (TEMPLATE_VARIABLE_PATTERN.matcher(cleanExpression).find()) {
            return renderTemplate(cleanExpression, variables, config);
        }
        if (isQuoted(cleanExpression)) {
            return cleanExpression.substring(1, cleanExpression.length() - 1);
        }
        return variableValue(cleanExpression, variables, config);
    }

    private String renderTemplate(String template, Map<String, Object> variables, AiWorkflowEndNodeConfig config) {
        Matcher matcher = TEMPLATE_VARIABLE_PATTERN.matcher(template);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String variableName = matcher.group(1);
            Object variableValue = variableValue(variableName, variables, config);
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(StrUtil.nullToEmpty(asString(variableValue))));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private Object variableValue(String variableName, Map<String, Object> variables, AiWorkflowEndNodeConfig config) {
        if (variables.containsKey(variableName)) {
            return variables.get(variableName);
        }
        Object current = variables;
        for (String segment : variableName.split("\\.")) {
            if (!(current instanceof Map<?, ?> currentMap) || !currentMap.containsKey(segment)) {
                if (Boolean.FALSE.equals(config.getFailWhenMissingVariable())) {
                    return null;
                }
                throw new BusinessException(400, "结束节点缺少变量：" + variableName);
            }
            current = currentMap.get(segment);
        }
        return current;
    }

    private boolean resolveWorkflowSuccess(AiWorkflowEndNodeConfig config, Map<String, Object> variables) {
        if (config.getSuccess() != null) {
            return config.getSuccess();
        }
        String statusVariable = trim(config.getStatusVariable());
        if (StrUtil.isBlank(statusVariable)) {
            return true;
        }
        return truthy(variableValue(statusVariable, variables, config));
    }

    private AiWorkflowNodeExecutionResult success(AiWorkflowNodeEntity node,
                                                  EndOutput output,
                                                  long startAt,
                                                  LocalDateTime startedAt) {
        return baseResult(node, startAt, startedAt)
                .setStatus(AiWorkflowNodeExecutionResult.STATUS_SUCCESS)
                .setTerminalNode(true)
                .setWorkflowSuccess(true)
                .setFinalOutput(output.outputText())
                .setOutputText(output.outputText())
                .setOutputVariables(output.outputVariables());
    }

    private AiWorkflowNodeExecutionResult workflowFailure(AiWorkflowNodeEntity node,
                                                          AiWorkflowEndNodeConfig config,
                                                          Map<String, Object> variables,
                                                          EndOutput output,
                                                          long startAt,
                                                          LocalDateTime startedAt) {
        return baseResult(node, startAt, startedAt)
                .setStatus(AiWorkflowNodeExecutionResult.STATUS_FAILED)
                .setTerminalNode(true)
                .setWorkflowSuccess(false)
                .setFinalOutput(output.outputText())
                .setOutputText(output.outputText())
                .setOutputVariables(output.outputVariables())
                .setErrorCode(resolveErrorCode(config, variables))
                .setErrorMessage(resolveErrorMessage(config, variables));
    }

    private AiWorkflowNodeExecutionResult failure(AiWorkflowNodeEntity node,
                                                  RuntimeException ex,
                                                  long startAt,
                                                  LocalDateTime startedAt) {
        return baseResult(node, startAt, startedAt)
                .setStatus(AiWorkflowNodeExecutionResult.STATUS_FAILED)
                .setTerminalNode(true)
                .setWorkflowSuccess(false)
                .setOutputVariables(Collections.emptyMap())
                .setErrorCode(ex.getClass().getSimpleName())
                .setErrorMessage(rootMessage(ex));
    }

    private AiWorkflowNodeExecutionResult baseResult(AiWorkflowNodeEntity node,
                                                     long startAt,
                                                     LocalDateTime startedAt) {
        return AiWorkflowNodeExecutionResult.builder()
                .nodeKey(node == null ? null : node.getNodeKey())
                .nodeType(node == null ? NODE_TYPE : node.getNodeType())
                .durationMs(System.currentTimeMillis() - startAt)
                .startedAt(startedAt)
                .finishedAt(LocalDateTime.now())
                .build();
    }

    private String resolveErrorCode(AiWorkflowEndNodeConfig config, Map<String, Object> variables) {
        String configuredCode = trim(config.getErrorCode());
        if (StrUtil.isNotBlank(configuredCode)) {
            return configuredCode;
        }
        String codeVariable = trim(config.getErrorCodeVariable());
        if (StrUtil.isNotBlank(codeVariable)) {
            Object variableValue = variableValue(codeVariable, variables, config);
            return StrUtil.blankToDefault(trim(asString(variableValue)), DEFAULT_ERROR_CODE);
        }
        return DEFAULT_ERROR_CODE;
    }

    private String resolveErrorMessage(AiWorkflowEndNodeConfig config, Map<String, Object> variables) {
        String configuredMessage = trim(config.getErrorMessage());
        if (StrUtil.isNotBlank(configuredMessage)) {
            return configuredMessage;
        }
        String messageVariable = trim(config.getErrorMessageVariable());
        if (StrUtil.isNotBlank(messageVariable)) {
            Object variableValue = variableValue(messageVariable, variables, config);
            return StrUtil.blankToDefault(trim(asString(variableValue)), "工作流结束节点标记为失败");
        }
        return "工作流结束节点标记为失败";
    }

    private List<String> parseNextNodeKeys(String nextNodeKeys) {
        if (StrUtil.isBlank(nextNodeKeys)) {
            return Collections.emptyList();
        }
        String cleanNextNodeKeys = trim(nextNodeKeys);
        try {
            if (cleanNextNodeKeys.startsWith("[")) {
                return cleanList(objectMapper.readValue(cleanNextNodeKeys, new TypeReference<List<String>>() {
                }));
            }
        } catch (JsonProcessingException e) {
            throw new BusinessException(400, "结束节点下游节点JSON格式错误");
        }
        return cleanList(List.of(cleanNextNodeKeys.split(",")));
    }

    private List<String> cleanList(Collection<String> values) {
        if (values == null) {
            return Collections.emptyList();
        }
        return values.stream()
                .map(this::trim)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .toList();
    }

    private Map<String, Object> variables(AiWorkflowNodeExecutionContext context) {
        if (context == null || context.getVariables() == null) {
            return Collections.emptyMap();
        }
        return context.getVariables();
    }

    private String writeOutputSummary(Map<String, Object> outputVariables) {
        try {
            return objectMapper.writeValueAsString(outputVariables);
        } catch (JsonProcessingException e) {
            return String.valueOf(outputVariables);
        }
    }

    private boolean truthy(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        String text = trim(String.valueOf(value));
        return StrUtil.isNotBlank(text)
                && !"false".equalsIgnoreCase(text)
                && !"0".equals(text)
                && !"null".equalsIgnoreCase(text);
    }

    private boolean isQuoted(String value) {
        return value.length() >= 2
                && ((value.startsWith("'") && value.endsWith("'"))
                || (value.startsWith("\"") && value.endsWith("\"")));
    }

    private String rootMessage(Throwable e) {
        Throwable root = e;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return StrUtil.blankToDefault(root.getMessage(), "结束节点执行失败");
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private record EndOutput(String outputText, Map<String, Object> outputVariables) {
    }
}
