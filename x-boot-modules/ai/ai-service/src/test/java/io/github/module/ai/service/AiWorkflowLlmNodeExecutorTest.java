package io.github.module.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.framework.core.enums.EnabledStatusEnum;
import io.github.framework.core.enums.YesOrNoEnum;
import io.github.module.ai.entity.AiWorkflowNodeEntity;
import io.github.module.ai.model.response.AiModelConfigBO;
import io.github.module.ai.service.model.AiWorkflowLlmNodeConfig;
import io.github.module.ai.service.model.AiWorkflowNodeExecutionContext;
import io.github.module.ai.service.model.AiWorkflowNodeExecutionResult;
import io.github.module.ai.service.workflow.AiWorkflowLlmNodeExecutor;
import io.github.starter.ai.enums.AiProviderTypeEnum;
import io.github.starter.ai.service.XBootAiService;
import io.github.starter.ai.vo.AiModelConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiWorkflowLlmNodeExecutorTest {

    @Mock
    private AiModelConfigService aiModelConfigService;

    @Mock
    private XBootAiService xBootAiService;

    private ObjectMapper objectMapper;

    private AiWorkflowLlmNodeExecutor executor;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        executor = new AiWorkflowLlmNodeExecutor(aiModelConfigService, xBootAiService, objectMapper);
    }

    @Test
    void supportsLlmNodeTypeIgnoringCase() {
        assertThat(executor.supports("llm")).isTrue();
        assertThat(executor.supports(" LLM ")).isTrue();
        assertThat(executor.supports("condition")).isFalse();
    }

    @Test
    void executeLlmNodeUsesProvidedModelConfigAndPromptTemplate() throws JsonProcessingException {
        AiModelConfigBO modelConfig = enabledConfig("qwen", "qwen-plus");
        AiModelConfig runtimeConfig = runtimeConfig("qwen-plus");
        AiWorkflowNodeEntity node = llmNode(configJson(AiWorkflowLlmNodeConfig.builder()
                .modelConfigCode(" qwen ")
                .promptTemplate("请根据规则回答：${question}")
                .outputVariable("answer")
                .build()));
        AiWorkflowNodeExecutionContext context = context(Map.of("question", "年假几天？"));
        when(aiModelConfigService.getEnabledConfigByCode("qwen", true)).thenReturn(modelConfig);
        when(aiModelConfigService.toRuntimeConfig(modelConfig)).thenReturn(runtimeConfig);
        when(xBootAiService.chat("请根据规则回答：年假几天？", runtimeConfig)).thenReturn("年假为5天");

        AiWorkflowNodeExecutionResult result = executor.execute(node, context);

        assertThat(result.getStatus()).isEqualTo(AiWorkflowNodeExecutionResult.STATUS_SUCCESS);
        assertThat(result.getNodeKey()).isEqualTo("llm-1");
        assertThat(result.getPrompt()).isEqualTo("请根据规则回答：年假几天？");
        assertThat(result.getOutputText()).isEqualTo("年假为5天");
        assertThat(result.getOutputVariables()).containsEntry("answer", "年假为5天");
        assertThat(result.getModelConfigCode()).isEqualTo("qwen");
        assertThat(result.getProviderType()).isEqualTo("OLLAMA");
        assertThat(result.getModelName()).isEqualTo("qwen-plus");
        assertThat(result.getErrorMessage()).isNull();
        verify(xBootAiService).chat("请根据规则回答：年假几天？", runtimeConfig);
    }

    @Test
    void executeLlmNodeUsesDefaultModelAndInputVariableWhenTemplateBlank() throws JsonProcessingException {
        AiModelConfigBO modelConfig = enabledConfig("default", "llama3.2");
        AiModelConfig runtimeConfig = runtimeConfig("llama3.2");
        AiWorkflowNodeEntity node = llmNode(configJson(AiWorkflowLlmNodeConfig.builder()
                .inputVariable("userInput")
                .outputVariable("summary")
                .build()));
        AiWorkflowNodeExecutionContext context = context(Map.of("userInput", "请总结报销制度"));
        when(aiModelConfigService.getDefaultEnabledConfig()).thenReturn(modelConfig);
        when(aiModelConfigService.toRuntimeConfig(modelConfig)).thenReturn(runtimeConfig);
        when(xBootAiService.chat("请总结报销制度", runtimeConfig)).thenReturn("报销需提供发票");

        AiWorkflowNodeExecutionResult result = executor.execute(node, context);

        assertThat(result.getStatus()).isEqualTo(AiWorkflowNodeExecutionResult.STATUS_SUCCESS);
        assertThat(result.getPrompt()).isEqualTo("请总结报销制度");
        assertThat(result.getOutputVariables()).containsEntry("summary", "报销需提供发票");
        verify(aiModelConfigService).getDefaultEnabledConfig();
        verify(xBootAiService).chat("请总结报销制度", runtimeConfig);
    }

    @Test
    void executeLlmNodeReturnsFailedResultWhenProviderFails() throws JsonProcessingException {
        AiModelConfigBO modelConfig = enabledConfig("qwen", "qwen-plus");
        AiModelConfig runtimeConfig = runtimeConfig("qwen-plus");
        RuntimeException providerError = new IllegalStateException("provider down");
        AiWorkflowNodeEntity node = llmNode(configJson(AiWorkflowLlmNodeConfig.builder()
                .modelConfigCode("qwen")
                .promptTemplate("${question}")
                .build()));
        when(aiModelConfigService.getEnabledConfigByCode("qwen", true)).thenReturn(modelConfig);
        when(aiModelConfigService.toRuntimeConfig(modelConfig)).thenReturn(runtimeConfig);
        when(xBootAiService.chat("你好", runtimeConfig)).thenThrow(providerError);

        AiWorkflowNodeExecutionResult result = executor.execute(node, context(Map.of("question", "你好")));

        assertThat(result.getStatus()).isEqualTo(AiWorkflowNodeExecutionResult.STATUS_FAILED);
        assertThat(result.getOutputVariables()).isEmpty();
        assertThat(result.getErrorCode()).isEqualTo("IllegalStateException");
        assertThat(result.getErrorMessage()).isEqualTo("provider down");
        assertThat(result.getDurationMs()).isNotNegative();
    }

    @Test
    void executeLlmNodeReturnsFailedResultWhenTemplateVariableMissing() throws JsonProcessingException {
        AiWorkflowNodeEntity node = llmNode(configJson(AiWorkflowLlmNodeConfig.builder()
                .modelConfigCode("qwen")
                .promptTemplate("请回答：${question}")
                .build()));

        AiWorkflowNodeExecutionResult result = executor.execute(node, context(Map.of("input", "你好")));

        assertThat(result.getStatus()).isEqualTo(AiWorkflowNodeExecutionResult.STATUS_FAILED);
        assertThat(result.getErrorCode()).isEqualTo("BusinessException");
        assertThat(result.getErrorMessage()).isEqualTo("LLM节点缺少模板变量：question");
        verify(aiModelConfigService, never()).getEnabledConfigByCode(any(), anyBoolean());
        verify(xBootAiService, never()).chat(any(String.class), any());
    }

    private AiWorkflowNodeEntity llmNode(String configJson) {
        return AiWorkflowNodeEntity.builder()
                .nodeKey("llm-1")
                .nodeName("LLM节点")
                .nodeType("llm")
                .nodeConfig(configJson)
                .build();
    }

    private AiWorkflowNodeExecutionContext context(Map<String, Object> variables) {
        return AiWorkflowNodeExecutionContext.builder()
                .executionId("exec-1")
                .workflowDefinitionId(1L)
                .workflowCode("wf-demo")
                .traceId("trace-1")
                .variables(variables)
                .build();
    }

    private String configJson(AiWorkflowLlmNodeConfig config) throws JsonProcessingException {
        return objectMapper.writeValueAsString(config);
    }

    private AiModelConfigBO enabledConfig(String code, String modelName) {
        return AiModelConfigBO.builder()
                .id(1L)
                .code(code)
                .name("默认模型")
                .providerType("OLLAMA")
                .baseUrl("http://localhost:11434")
                .modelName(modelName)
                .status(EnabledStatusEnum.ENABLED.getValue())
                .defaultFlag(YesOrNoEnum.YES.getValue())
                .build();
    }

    private AiModelConfig runtimeConfig(String modelName) {
        return new AiModelConfig()
                .setProviderType(AiProviderTypeEnum.OLLAMA)
                .setBaseUrl("http://localhost:11434")
                .setModelName(modelName);
    }
}
