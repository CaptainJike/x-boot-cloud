package io.github.module.ai.service.workflow;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.framework.core.exception.BusinessException;
import io.github.module.ai.entity.AiWorkflowNodeEntity;
import io.github.module.ai.enums.AiErrorEnum;
import io.github.module.ai.model.response.AiModelConfigBO;
import io.github.module.ai.service.AiModelConfigService;
import io.github.module.ai.service.model.AiWorkflowLlmNodeConfig;
import io.github.module.ai.service.model.AiWorkflowNodeExecutionContext;
import io.github.module.ai.service.model.AiWorkflowNodeExecutionResult;
import io.github.starter.ai.service.XBootAiService;
import io.github.starter.ai.vo.AiModelConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工作流 LLM 节点执行器.
 */
@RequiredArgsConstructor
@Component
public class AiWorkflowLlmNodeExecutor implements AiWorkflowNodeExecutor {

    public static final String NODE_TYPE = "llm";

    private static final String DEFAULT_INPUT_VARIABLE = "input";

    private static final String DEFAULT_OUTPUT_VARIABLE = "answer";

    private static final Pattern TEMPLATE_VARIABLE_PATTERN = Pattern.compile("\\$\\{([A-Za-z0-9_.-]+)}");

    private final AiModelConfigService aiModelConfigService;

    private final XBootAiService xBootAiService;

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
            AiWorkflowLlmNodeConfig config = parseConfig(node.getNodeConfig());
            String prompt = buildPrompt(config, context);
            AiModelConfigBO modelConfig = resolveModelConfig(config.getModelConfigCode());
            AiModelConfig runtimeConfig = aiModelConfigService.toRuntimeConfig(modelConfig);
            String answer = xBootAiService.chat(prompt, runtimeConfig);

            return success(node, config, modelConfig, prompt, answer, startAt, startedAt);
        } catch (RuntimeException ex) {
            return failure(node, ex, startAt, startedAt);
        }
    }

    private void validateNode(AiWorkflowNodeEntity node) {
        if (node == null) {
            throw new BusinessException(400, "LLM节点不存在");
        }
        if (!supports(node.getNodeType())) {
            throw new BusinessException(400, "非LLM节点不支持LLM执行器");
        }
    }

    private AiWorkflowLlmNodeConfig parseConfig(String configJson) {
        if (StrUtil.isBlank(configJson)) {
            return new AiWorkflowLlmNodeConfig();
        }
        try {
            return objectMapper.readValue(configJson, AiWorkflowLlmNodeConfig.class);
        } catch (JsonProcessingException e) {
            throw new BusinessException(400, "LLM节点配置JSON格式错误");
        }
    }

    private String buildPrompt(AiWorkflowLlmNodeConfig config, AiWorkflowNodeExecutionContext context) {
        Map<String, Object> variables = variables(context);
        String promptTemplate = trim(config.getPromptTemplate());
        if (StrUtil.isNotBlank(promptTemplate)) {
            String prompt = renderTemplate(promptTemplate, variables);
            if (StrUtil.isBlank(prompt)) {
                throw new BusinessException(400, "LLM节点Prompt为空");
            }
            return prompt;
        }

        String inputVariable = StrUtil.blankToDefault(trim(config.getInputVariable()), DEFAULT_INPUT_VARIABLE);
        Object input = variables.get(inputVariable);
        if (input == null) {
            throw new BusinessException(400, "LLM节点缺少输入变量：" + inputVariable);
        }
        String prompt = String.valueOf(input);
        if (StrUtil.isBlank(prompt)) {
            throw new BusinessException(400, "LLM节点输入内容为空");
        }
        return prompt;
    }

    private String renderTemplate(String promptTemplate, Map<String, Object> variables) {
        Matcher matcher = TEMPLATE_VARIABLE_PATTERN.matcher(promptTemplate);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String variableName = matcher.group(1);
            Object variableValue = variables.get(variableName);
            if (variableValue == null) {
                throw new BusinessException(400, "LLM节点缺少模板变量：" + variableName);
            }
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(String.valueOf(variableValue)));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private AiModelConfigBO resolveModelConfig(String modelConfigCode) {
        String cleanCode = trim(modelConfigCode);
        if (StrUtil.isNotBlank(cleanCode)) {
            return aiModelConfigService.getEnabledConfigByCode(cleanCode, true);
        }

        AiModelConfigBO defaultConfig = aiModelConfigService.getDefaultEnabledConfig();
        AiErrorEnum.NO_ENABLED_MODEL_CONFIG.assertNotNull(defaultConfig);
        return defaultConfig;
    }

    private AiWorkflowNodeExecutionResult success(AiWorkflowNodeEntity node,
                                                  AiWorkflowLlmNodeConfig config,
                                                  AiModelConfigBO modelConfig,
                                                  String prompt,
                                                  String answer,
                                                  long startAt,
                                                  LocalDateTime startedAt) {
        String outputVariable = StrUtil.blankToDefault(trim(config.getOutputVariable()), DEFAULT_OUTPUT_VARIABLE);
        Map<String, Object> outputVariables = new LinkedHashMap<>();
        outputVariables.put(outputVariable, StrUtil.nullToEmpty(answer));

        return baseResult(node, startAt, startedAt)
                .setStatus(AiWorkflowNodeExecutionResult.STATUS_SUCCESS)
                .setPrompt(prompt)
                .setOutputText(StrUtil.nullToEmpty(answer))
                .setOutputVariables(outputVariables)
                .setModelConfigCode(modelConfig.getCode())
                .setProviderType(modelConfig.getProviderType())
                .setModelName(modelConfig.getModelName());
    }

    private AiWorkflowNodeExecutionResult failure(AiWorkflowNodeEntity node,
                                                  RuntimeException ex,
                                                  long startAt,
                                                  LocalDateTime startedAt) {
        return baseResult(node, startAt, startedAt)
                .setStatus(AiWorkflowNodeExecutionResult.STATUS_FAILED)
                .setOutputVariables(Collections.emptyMap())
                .setErrorCode(errorCode(ex))
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

    private String errorCode(RuntimeException ex) {
        if (ex instanceof BusinessException businessException && businessException.getCustomEnumField() != null) {
            return businessException.getCustomEnumField().name();
        }
        return ex.getClass().getSimpleName();
    }

    private String rootMessage(Throwable e) {
        Throwable root = e;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return StrUtil.blankToDefault(root.getMessage(), "LLM节点执行失败");
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
