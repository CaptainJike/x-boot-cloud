package io.github.module.ai.service.workflow;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.framework.core.exception.BusinessException;
import io.github.module.ai.entity.AiWorkflowNodeEntity;
import io.github.module.ai.service.model.AiWorkflowHttpToolNodeConfig;
import io.github.module.ai.service.model.AiWorkflowHttpToolRequest;
import io.github.module.ai.service.model.AiWorkflowHttpToolResponse;
import io.github.module.ai.service.model.AiWorkflowNodeExecutionContext;
import io.github.module.ai.service.model.AiWorkflowNodeExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工作流 HTTP 工具节点执行器.
 */
@RequiredArgsConstructor
@Component
public class AiWorkflowHttpToolNodeExecutor implements AiWorkflowNodeExecutor {

    public static final String NODE_TYPE = "http";

    public static final String NODE_TYPE_HTTP_TOOL = "http_tool";

    private static final String DEFAULT_METHOD = "GET";

    private static final String DEFAULT_OUTPUT_VARIABLE = "httpResponse";

    private static final int DEFAULT_TIMEOUT_MS = 5000;

    private static final int MIN_TIMEOUT_MS = 100;

    private static final int MAX_TIMEOUT_MS = 30000;

    private static final int DEFAULT_MAX_RESPONSE_LENGTH = 4096;

    private static final int MAX_RESPONSE_LENGTH_LIMIT = 20000;

    private static final Set<String> ALLOWED_METHODS = Set.of("GET", "POST", "PUT", "PATCH", "DELETE");

    private static final Set<String> DEFAULT_SENSITIVE_KEYS = Set.of(
            "apikey", "api_key", "token", "accesstoken", "authorization", "password", "secret"
    );

    private static final Pattern TEMPLATE_VARIABLE_PATTERN = Pattern.compile("\\$\\{([A-Za-z0-9_.-]+)}");

    private static final Pattern HEADER_NAME_PATTERN = Pattern.compile("[A-Za-z0-9!#$%&'*+.^_`|~-]+");

    private final AiWorkflowHttpToolClient httpToolClient;

    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(String nodeType) {
        String cleanType = trim(nodeType);
        return NODE_TYPE.equalsIgnoreCase(cleanType) || NODE_TYPE_HTTP_TOOL.equalsIgnoreCase(cleanType);
    }

    @Override
    public AiWorkflowNodeExecutionResult execute(AiWorkflowNodeEntity node, AiWorkflowNodeExecutionContext context) {
        long startAt = System.currentTimeMillis();
        LocalDateTime startedAt = LocalDateTime.now();
        try {
            validateNode(node);
            AiWorkflowHttpToolNodeConfig config = parseConfig(node.getNodeConfig());
            AiWorkflowHttpToolRequest request = buildRequest(config, context);
            AiWorkflowHttpToolResponse response = httpToolClient.exchange(request);
            String responseBody = sanitizeResponse(response.getBody(), config);
            if (!isSuccess(response.getStatusCode(), config.getSuccessStatusCodes())) {
                return httpFailure(node, request, response, responseBody, startAt, startedAt);
            }
            return success(node, config, request, response, responseBody, startAt, startedAt);
        } catch (RuntimeException ex) {
            return failure(node, ex, startAt, startedAt);
        }
    }

    private void validateNode(AiWorkflowNodeEntity node) {
        if (node == null) {
            throw new BusinessException(400, "HTTP工具节点不存在");
        }
        if (!supports(node.getNodeType())) {
            throw new BusinessException(400, "非HTTP工具节点不支持HTTP工具执行器");
        }
    }

    private AiWorkflowHttpToolNodeConfig parseConfig(String configJson) {
        if (StrUtil.isBlank(configJson)) {
            return new AiWorkflowHttpToolNodeConfig();
        }
        try {
            return objectMapper.readValue(configJson, AiWorkflowHttpToolNodeConfig.class);
        } catch (JsonProcessingException e) {
            throw new BusinessException(400, "HTTP工具节点配置JSON格式错误");
        }
    }

    private AiWorkflowHttpToolRequest buildRequest(AiWorkflowHttpToolNodeConfig config,
                                                   AiWorkflowNodeExecutionContext context) {
        Map<String, Object> variables = variables(context);
        String method = resolveMethod(config.getMethod());
        String url = renderTemplate(trim(config.getUrlTemplate()), variables);
        URI uri = validateUrl(url, config.getAllowedHosts());
        return AiWorkflowHttpToolRequest.builder()
                .method(method)
                .url(uri.toString())
                .headers(buildHeaders(config, variables))
                .body(buildBody(config, variables, method))
                .timeoutMs(resolveTimeout(config.getTimeoutMs()))
                .build();
    }

    private String resolveMethod(String method) {
        String cleanMethod = StrUtil.blankToDefault(trim(method), DEFAULT_METHOD).toUpperCase(Locale.ROOT);
        if (!ALLOWED_METHODS.contains(cleanMethod)) {
            throw new BusinessException(400, "HTTP工具节点请求方法不受支持：" + cleanMethod);
        }
        return cleanMethod;
    }

    private URI validateUrl(String url, List<String> allowedHosts) {
        if (StrUtil.isBlank(url)) {
            throw new BusinessException(400, "HTTP工具节点URL不能为空");
        }
        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(400, "HTTP工具节点URL格式错误");
        }
        String scheme = lower(uri.getScheme());
        if ((!"http".equals(scheme) && !"https".equals(scheme)) || StrUtil.isBlank(uri.getHost())) {
            throw new BusinessException(400, "HTTP工具节点URL只允许HTTP或HTTPS地址");
        }
        if (StrUtil.isNotBlank(uri.getUserInfo())) {
            throw new BusinessException(400, "HTTP工具节点URL不允许携带用户信息");
        }
        if (!isAllowedHost(uri, allowedHosts)) {
            throw new BusinessException(400, "HTTP工具节点目标地址不在白名单：" + uri.getHost());
        }
        return uri;
    }

    private boolean isAllowedHost(URI uri, List<String> allowedHosts) {
        if (allowedHosts == null || allowedHosts.isEmpty()) {
            return false;
        }
        String host = lower(uri.getHost());
        int port = uri.getPort();
        return allowedHosts.stream()
                .map(this::parseAllowedHost)
                .anyMatch(allowed -> allowed.matches(host, port));
    }

    private AllowedHost parseAllowedHost(String allowedHost) {
        String cleanHost = lower(trim(allowedHost));
        if (StrUtil.isBlank(cleanHost)) {
            return new AllowedHost("", null);
        }
        if (cleanHost.contains("://")) {
            URI uri = URI.create(cleanHost);
            return new AllowedHost(lower(uri.getHost()), uri.getPort() < 0 ? null : uri.getPort());
        }
        int colonIndex = cleanHost.lastIndexOf(':');
        if (colonIndex > 0 && colonIndex < cleanHost.length() - 1) {
            String portText = cleanHost.substring(colonIndex + 1);
            if (portText.chars().allMatch(Character::isDigit)) {
                return new AllowedHost(cleanHost.substring(0, colonIndex), Integer.valueOf(portText));
            }
        }
        return new AllowedHost(cleanHost, null);
    }

    private Map<String, String> buildHeaders(AiWorkflowHttpToolNodeConfig config, Map<String, Object> variables) {
        Map<String, String> headers = new LinkedHashMap<>();
        if (config.getHeaders() != null) {
            for (Map.Entry<String, String> entry : config.getHeaders().entrySet()) {
                String headerName = trim(entry.getKey());
                validateHeaderName(headerName);
                headers.put(headerName, renderTemplate(entry.getValue(), variables));
            }
        }
        addAuthorizationHeader(config, variables, headers);
        return headers;
    }

    private void validateHeaderName(String headerName) {
        if (StrUtil.isBlank(headerName) || !HEADER_NAME_PATTERN.matcher(headerName).matches()) {
            throw new BusinessException(400, "HTTP工具节点Header名称不合法");
        }
        String lowerHeaderName = lower(headerName);
        if (Set.of("host", "content-length").contains(lowerHeaderName)) {
            throw new BusinessException(400, "HTTP工具节点Header不允许配置：" + headerName);
        }
    }

    private void addAuthorizationHeader(AiWorkflowHttpToolNodeConfig config,
                                        Map<String, Object> variables,
                                        Map<String, String> headers) {
        String tokenVariable = trim(config.getAuthTokenVariable());
        if (StrUtil.isBlank(tokenVariable)) {
            return;
        }
        Object token = variables.get(tokenVariable);
        if (token == null || StrUtil.isBlank(String.valueOf(token))) {
            throw new BusinessException(400, "HTTP工具节点缺少鉴权变量：" + tokenVariable);
        }
        String scheme = StrUtil.blankToDefault(trim(config.getAuthScheme()), "Bearer");
        headers.put("Authorization", scheme + " " + token);
    }

    private String buildBody(AiWorkflowHttpToolNodeConfig config, Map<String, Object> variables, String method) {
        if (!Set.of("POST", "PUT", "PATCH", "DELETE").contains(method)) {
            return null;
        }
        String bodyTemplate = trim(config.getBodyTemplate());
        if (StrUtil.isNotBlank(bodyTemplate)) {
            return renderTemplate(bodyTemplate, variables);
        }
        String inputVariable = trim(config.getInputVariable());
        if (StrUtil.isBlank(inputVariable)) {
            return null;
        }
        Object input = variables.get(inputVariable);
        if (input == null) {
            throw new BusinessException(400, "HTTP工具节点缺少请求体变量：" + inputVariable);
        }
        return String.valueOf(input);
    }

    private int resolveTimeout(Integer timeoutMs) {
        int resolvedTimeout = timeoutMs == null ? DEFAULT_TIMEOUT_MS : timeoutMs;
        if (resolvedTimeout < MIN_TIMEOUT_MS || resolvedTimeout > MAX_TIMEOUT_MS) {
            throw new BusinessException(400, "HTTP工具节点超时时间必须在100到30000毫秒之间");
        }
        return resolvedTimeout;
    }

    private String sanitizeResponse(String responseBody, AiWorkflowHttpToolNodeConfig config) {
        String cleanBody = StrUtil.nullToEmpty(responseBody);
        Set<String> sensitiveKeys = new LinkedHashSet<>(DEFAULT_SENSITIVE_KEYS);
        addSensitiveKeys(config.getSensitiveResponseKeys(), sensitiveKeys);
        try {
            Object responseObject = objectMapper.readValue(cleanBody, Object.class);
            cleanBody = objectMapper.writeValueAsString(redact(responseObject, sensitiveKeys));
        } catch (JsonProcessingException | IllegalArgumentException ignored) {
            // 非 JSON 响应保持原文，仅执行长度限制。
        }
        return truncate(cleanBody, resolveMaxResponseLength(config.getMaxResponseLength()));
    }

    private void addSensitiveKeys(Collection<String> keys, Set<String> sensitiveKeys) {
        if (keys == null) {
            return;
        }
        for (String key : keys) {
            String cleanKey = lower(trim(key));
            if (StrUtil.isNotBlank(cleanKey)) {
                sensitiveKeys.add(cleanKey);
            }
        }
    }

    private Object redact(Object value, Set<String> sensitiveKeys) {
        if (value instanceof Map<?, ?> valueMap) {
            Map<String, Object> redactedMap = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : valueMap.entrySet()) {
                String key = String.valueOf(entry.getKey());
                Object entryValue = sensitiveKeys.contains(lower(key)) ? "***" : redact(entry.getValue(), sensitiveKeys);
                redactedMap.put(key, entryValue);
            }
            return redactedMap;
        }
        if (value instanceof List<?> valueList) {
            return valueList.stream()
                    .map(item -> redact(item, sensitiveKeys))
                    .toList();
        }
        return value;
    }

    private int resolveMaxResponseLength(Integer maxResponseLength) {
        int resolvedLength = maxResponseLength == null ? DEFAULT_MAX_RESPONSE_LENGTH : maxResponseLength;
        if (resolvedLength < 1 || resolvedLength > MAX_RESPONSE_LENGTH_LIMIT) {
            throw new BusinessException(400, "HTTP工具节点响应摘要长度必须在1到20000字符之间");
        }
        return resolvedLength;
    }

    private String truncate(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private boolean isSuccess(Integer statusCode, List<Integer> successStatusCodes) {
        if (statusCode == null) {
            return false;
        }
        if (successStatusCodes == null || successStatusCodes.isEmpty()) {
            return statusCode >= 200 && statusCode < 300;
        }
        return successStatusCodes.contains(statusCode);
    }

    private AiWorkflowNodeExecutionResult success(AiWorkflowNodeEntity node,
                                                  AiWorkflowHttpToolNodeConfig config,
                                                  AiWorkflowHttpToolRequest request,
                                                  AiWorkflowHttpToolResponse response,
                                                  String responseBody,
                                                  long startAt,
                                                  LocalDateTime startedAt) {
        String outputVariable = StrUtil.blankToDefault(trim(config.getOutputVariable()), DEFAULT_OUTPUT_VARIABLE);
        Map<String, Object> outputVariables = new LinkedHashMap<>();
        outputVariables.put(outputVariable, responseBody);
        outputVariables.put(outputVariable + "StatusCode", response.getStatusCode());

        return baseResult(node, startAt, startedAt)
                .setStatus(AiWorkflowNodeExecutionResult.STATUS_SUCCESS)
                .setHttpMethod(request.getMethod())
                .setRequestUrl(request.getUrl())
                .setHttpStatusCode(response.getStatusCode())
                .setOutputText(responseBody)
                .setOutputVariables(outputVariables);
    }

    private AiWorkflowNodeExecutionResult httpFailure(AiWorkflowNodeEntity node,
                                                      AiWorkflowHttpToolRequest request,
                                                      AiWorkflowHttpToolResponse response,
                                                      String responseBody,
                                                      long startAt,
                                                      LocalDateTime startedAt) {
        return baseResult(node, startAt, startedAt)
                .setStatus(AiWorkflowNodeExecutionResult.STATUS_FAILED)
                .setHttpMethod(request.getMethod())
                .setRequestUrl(request.getUrl())
                .setHttpStatusCode(response.getStatusCode())
                .setOutputText(responseBody)
                .setOutputVariables(Collections.emptyMap())
                .setErrorCode("HTTP_STATUS_ERROR")
                .setErrorMessage("HTTP工具节点返回非成功状态：" + response.getStatusCode());
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

    private String renderTemplate(String template, Map<String, Object> variables) {
        if (StrUtil.isBlank(template)) {
            return template;
        }
        Matcher matcher = TEMPLATE_VARIABLE_PATTERN.matcher(template);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String variableName = matcher.group(1);
            Object variableValue = variables.get(variableName);
            if (variableValue == null) {
                throw new BusinessException(400, "HTTP工具节点缺少模板变量：" + variableName);
            }
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(String.valueOf(variableValue)));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private Map<String, Object> variables(AiWorkflowNodeExecutionContext context) {
        if (context == null || context.getVariables() == null) {
            return Collections.emptyMap();
        }
        return context.getVariables();
    }

    private String rootMessage(Throwable e) {
        Throwable root = e;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return StrUtil.blankToDefault(root.getMessage(), "HTTP工具节点执行失败");
    }

    private String lower(String value) {
        return value == null ? null : value.toLowerCase(Locale.ROOT);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private record AllowedHost(String host, Integer port) {

        private boolean matches(String targetHost, int targetPort) {
            if (StrUtil.isBlank(host)) {
                return false;
            }
            if (port != null && port != targetPort) {
                return false;
            }
            if (host.startsWith("*.")) {
                String suffix = host.substring(2);
                return targetHost.endsWith("." + suffix);
            }
            return host.equals(targetHost);
        }
    }
}
