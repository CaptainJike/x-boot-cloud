package io.github.module.ai.service.workflow;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.framework.core.exception.BusinessException;
import io.github.module.ai.entity.AiWorkflowNodeEntity;
import io.github.module.ai.service.model.AiWorkflowConditionBranchConfig;
import io.github.module.ai.service.model.AiWorkflowConditionNodeConfig;
import io.github.module.ai.service.model.AiWorkflowNodeExecutionContext;
import io.github.module.ai.service.model.AiWorkflowNodeExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工作流条件节点执行器.
 */
@RequiredArgsConstructor
@Component
public class AiWorkflowConditionNodeExecutor implements AiWorkflowNodeExecutor {

    public static final String NODE_TYPE = "condition";

    private static final String DEFAULT_OUTPUT_VARIABLE = "condition";

    private static final String DEFAULT_BRANCH_KEY = "default";

    private static final Pattern COMPARISON_PATTERN = Pattern.compile(
            "^(.+?)\\s*(==|!=|>=|<=|>|<)\\s*(.+)$"
    );

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("^\\$\\{([A-Za-z0-9_.-]+)}$");

    private static final Pattern PLAIN_VARIABLE_PATTERN = Pattern.compile("^[A-Za-z_][A-Za-z0-9_.-]*$");

    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(String nodeType) {
        return NODE_TYPE.equalsIgnoreCase(trim(nodeType));
    }

    @Override
    public AiWorkflowNodeExecutionResult execute(AiWorkflowNodeEntity node, AiWorkflowNodeExecutionContext context) {
        long startAt = System.currentTimeMillis();
        LocalDateTime startedAt = LocalDateTime.now();
        try {
            validateNode(node);
            AiWorkflowConditionNodeConfig config = parseConfig(node.getNodeConfig());
            ConditionSelection selection = selectBranch(node, config, variables(context));
            validateNextNodeKey(selection.nextNodeKey(), parseNextNodeKeys(node.getNextNodeKeys()));
            return success(node, config, selection, startAt, startedAt);
        } catch (RuntimeException ex) {
            return failure(node, ex, startAt, startedAt);
        }
    }

    private void validateNode(AiWorkflowNodeEntity node) {
        if (node == null) {
            throw new BusinessException(400, "条件节点不存在");
        }
        if (!supports(node.getNodeType())) {
            throw new BusinessException(400, "非条件节点不支持条件执行器");
        }
    }

    private AiWorkflowConditionNodeConfig parseConfig(String configJson) {
        if (StrUtil.isBlank(configJson)) {
            return new AiWorkflowConditionNodeConfig();
        }
        try {
            return objectMapper.readValue(configJson, AiWorkflowConditionNodeConfig.class);
        } catch (JsonProcessingException e) {
            throw new BusinessException(400, "条件节点配置JSON格式错误");
        }
    }

    private ConditionSelection selectBranch(AiWorkflowNodeEntity node,
                                            AiWorkflowConditionNodeConfig config,
                                            Map<String, Object> variables) {
        if (config.getBranches() != null && !config.getBranches().isEmpty()) {
            for (AiWorkflowConditionBranchConfig branch : config.getBranches()) {
                String expression = trim(branch.getExpression());
                if (StrUtil.isBlank(expression)) {
                    throw new BusinessException(400, "条件节点分支表达式不能为空");
                }
                if (evaluate(expression, variables)) {
                    String branchKey = StrUtil.blankToDefault(trim(branch.getBranchKey()), branch.getNextNodeKey());
                    return new ConditionSelection(branchKey, expression, branch.getNextNodeKey(), true);
                }
            }
            return defaultSelection(config);
        }
        return expressionSelection(node, config, variables);
    }

    private ConditionSelection expressionSelection(AiWorkflowNodeEntity node,
                                                   AiWorkflowConditionNodeConfig config,
                                                   Map<String, Object> variables) {
        String expression = trim(node.getConditionExpression());
        if (StrUtil.isBlank(expression)) {
            throw new BusinessException(400, "条件节点表达式不能为空");
        }
        List<String> nextNodeKeys = parseNextNodeKeys(node.getNextNodeKeys());
        if (nextNodeKeys.isEmpty()) {
            throw new BusinessException(400, "条件节点缺少下游节点");
        }
        boolean matched = evaluate(expression, variables);
        if (matched) {
            return new ConditionSelection("true", expression, nextNodeKeys.get(0), true);
        }
        String defaultNextNodeKey = StrUtil.blankToDefault(trim(config.getDefaultNextNodeKey()), secondNode(nextNodeKeys));
        return buildNoMatchSelection(config, expression, defaultNextNodeKey);
    }

    private ConditionSelection defaultSelection(AiWorkflowConditionNodeConfig config) {
        return buildNoMatchSelection(config, null, trim(config.getDefaultNextNodeKey()));
    }

    private ConditionSelection buildNoMatchSelection(AiWorkflowConditionNodeConfig config,
                                                     String expression,
                                                     String defaultNextNodeKey) {
        if (StrUtil.isBlank(defaultNextNodeKey) && Boolean.TRUE.equals(config.getFailWhenNoMatch())) {
            throw new BusinessException(400, "条件节点没有匹配分支");
        }
        if (StrUtil.isBlank(defaultNextNodeKey)) {
            return new ConditionSelection(null, expression, null, false);
        }
        return new ConditionSelection(DEFAULT_BRANCH_KEY, expression, defaultNextNodeKey, false);
    }

    private boolean evaluate(String expression, Map<String, Object> variables) {
        String cleanExpression = trim(expression);
        if (StrUtil.isBlank(cleanExpression)) {
            throw new BusinessException(400, "条件节点表达式不能为空");
        }
        for (String orPart : split(cleanExpression, "||")) {
            boolean andMatched = true;
            for (String andPart : split(orPart, "&&")) {
                andMatched = andMatched && evaluateAtom(andPart, variables);
                if (!andMatched) {
                    break;
                }
            }
            if (andMatched) {
                return true;
            }
        }
        return false;
    }

    private List<String> split(String expression, String delimiter) {
        List<String> parts = new ArrayList<>();
        int start = 0;
        boolean quoted = false;
        char quote = '\0';
        for (int i = 0; i < expression.length() - 1; i++) {
            char current = expression.charAt(i);
            if ((current == '\'' || current == '"') && (i == 0 || expression.charAt(i - 1) != '\\')) {
                if (!quoted) {
                    quoted = true;
                    quote = current;
                } else if (quote == current) {
                    quoted = false;
                }
            }
            if (!quoted && expression.startsWith(delimiter, i)) {
                parts.add(expression.substring(start, i).trim());
                start = i + delimiter.length();
                i += delimiter.length() - 1;
            }
        }
        parts.add(expression.substring(start).trim());
        return parts;
    }

    private boolean evaluateAtom(String atom, Map<String, Object> variables) {
        String cleanAtom = trim(atom);
        if (StrUtil.isBlank(cleanAtom)) {
            throw new BusinessException(400, "条件节点表达式片段不能为空");
        }
        Matcher matcher = COMPARISON_PATTERN.matcher(cleanAtom);
        if (!matcher.matches()) {
            return truthy(resolveOperand(cleanAtom, variables));
        }
        Object left = resolveOperand(matcher.group(1), variables);
        Object right = resolveOperand(matcher.group(3), variables);
        return compare(left, matcher.group(2), right);
    }

    private Object resolveOperand(String token, Map<String, Object> variables) {
        String cleanToken = trim(token);
        Matcher matcher = VARIABLE_PATTERN.matcher(cleanToken);
        if (matcher.matches()) {
            return variableValue(matcher.group(1), variables);
        }
        if (isQuoted(cleanToken)) {
            return cleanToken.substring(1, cleanToken.length() - 1);
        }
        String lowerToken = cleanToken.toLowerCase(Locale.ROOT);
        if ("null".equals(lowerToken)) {
            return null;
        }
        if ("true".equals(lowerToken) || "false".equals(lowerToken)) {
            return Boolean.valueOf(lowerToken);
        }
        BigDecimal number = number(cleanToken);
        if (number != null) {
            return number;
        }
        if (variables.containsKey(cleanToken)) {
            return variableValue(cleanToken, variables);
        }
        if (PLAIN_VARIABLE_PATTERN.matcher(cleanToken).matches()) {
            return variableValue(cleanToken, variables);
        }
        return cleanToken;
    }

    private Object variableValue(String variableName, Map<String, Object> variables) {
        if (variables.containsKey(variableName)) {
            return variables.get(variableName);
        }
        Object current = variables;
        for (String segment : variableName.split("\\.")) {
            if (!(current instanceof Map<?, ?> currentMap) || !currentMap.containsKey(segment)) {
                throw new BusinessException(400, "条件节点缺少变量：" + variableName);
            }
            current = currentMap.get(segment);
        }
        return current;
    }

    private boolean compare(Object left, String operator, Object right) {
        if ("==".equals(operator)) {
            return Objects.equals(normalize(left), normalize(right));
        }
        if ("!=".equals(operator)) {
            return !Objects.equals(normalize(left), normalize(right));
        }
        int compared = compareValue(left, right);
        return switch (operator) {
            case ">" -> compared > 0;
            case ">=" -> compared >= 0;
            case "<" -> compared < 0;
            case "<=" -> compared <= 0;
            default -> throw new BusinessException(400, "条件节点操作符不受支持：" + operator);
        };
    }

    private Object normalize(Object value) {
        BigDecimal number = number(value);
        if (number != null) {
            return number.stripTrailingZeros();
        }
        return value;
    }

    private int compareValue(Object left, Object right) {
        BigDecimal leftNumber = number(left);
        BigDecimal rightNumber = number(right);
        if (leftNumber != null && rightNumber != null) {
            return leftNumber.compareTo(rightNumber);
        }
        if (left == null || right == null) {
            throw new BusinessException(400, "条件节点不能比较空值大小");
        }
        return String.valueOf(left).compareTo(String.valueOf(right));
    }

    private boolean truthy(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        BigDecimal number = number(value);
        if (number != null) {
            return number.compareTo(BigDecimal.ZERO) != 0;
        }
        String text = trim(String.valueOf(value));
        return StrUtil.isNotBlank(text)
                && !"false".equalsIgnoreCase(text)
                && !"0".equals(text)
                && !"null".equalsIgnoreCase(text);
    }

    private void validateNextNodeKey(String nextNodeKey, List<String> declaredNextNodeKeys) {
        if (StrUtil.isBlank(nextNodeKey) || declaredNextNodeKeys.isEmpty()) {
            return;
        }
        if (!declaredNextNodeKeys.contains(nextNodeKey)) {
            throw new BusinessException(400, "条件节点下游节点未声明：" + nextNodeKey);
        }
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
            throw new BusinessException(400, "条件节点下游节点JSON格式错误");
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

    private AiWorkflowNodeExecutionResult success(AiWorkflowNodeEntity node,
                                                  AiWorkflowConditionNodeConfig config,
                                                  ConditionSelection selection,
                                                  long startAt,
                                                  LocalDateTime startedAt) {
        String outputVariable = StrUtil.blankToDefault(trim(config.getOutputVariable()), DEFAULT_OUTPUT_VARIABLE);
        Map<String, Object> outputVariables = new LinkedHashMap<>();
        outputVariables.put(outputVariable + "Matched", selection.matched());
        outputVariables.put(outputVariable + "BranchKey", selection.branchKey());
        outputVariables.put(outputVariable + "NextNodeKey", selection.nextNodeKey());

        return baseResult(node, startAt, startedAt)
                .setStatus(AiWorkflowNodeExecutionResult.STATUS_SUCCESS)
                .setConditionExpression(selection.expression())
                .setConditionMatched(selection.matched())
                .setSelectedBranchKey(selection.branchKey())
                .setNextNodeKey(selection.nextNodeKey())
                .setOutputText(selection.nextNodeKey())
                .setOutputVariables(outputVariables);
    }

    private AiWorkflowNodeExecutionResult failure(AiWorkflowNodeEntity node,
                                                  RuntimeException ex,
                                                  long startAt,
                                                  LocalDateTime startedAt) {
        return baseResult(node, startAt, startedAt)
                .setStatus(AiWorkflowNodeExecutionResult.STATUS_FAILED)
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

    private Map<String, Object> variables(AiWorkflowNodeExecutionContext context) {
        if (context == null || context.getVariables() == null) {
            return Collections.emptyMap();
        }
        return context.getVariables();
    }

    private BigDecimal number(Object value) {
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number || value instanceof String) {
            try {
                return new BigDecimal(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private boolean isQuoted(String value) {
        return value.length() >= 2
                && (value.startsWith("'") && value.endsWith("'") || value.startsWith("\"") && value.endsWith("\""));
    }

    private String secondNode(List<String> nextNodeKeys) {
        return nextNodeKeys.size() > 1 ? nextNodeKeys.get(1) : null;
    }

    private String rootMessage(Throwable e) {
        Throwable root = e;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return StrUtil.blankToDefault(root.getMessage(), "条件节点执行失败");
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private record ConditionSelection(String branchKey, String expression, String nextNodeKey, boolean matched) {
    }
}
