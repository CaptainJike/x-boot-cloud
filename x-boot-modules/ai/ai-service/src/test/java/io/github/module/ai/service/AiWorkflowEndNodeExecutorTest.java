package io.github.module.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.module.ai.entity.AiWorkflowNodeEntity;
import io.github.module.ai.service.model.AiWorkflowEndNodeConfig;
import io.github.module.ai.service.model.AiWorkflowNodeExecutionContext;
import io.github.module.ai.service.model.AiWorkflowNodeExecutionResult;
import io.github.module.ai.service.workflow.AiWorkflowEndNodeExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AiWorkflowEndNodeExecutorTest {

    private ObjectMapper objectMapper;

    private AiWorkflowEndNodeExecutor executor;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        executor = new AiWorkflowEndNodeExecutor(objectMapper);
    }

    @Test
    void supportsEndNodeTypeIgnoringCase() {
        assertThat(executor.supports("end")).isTrue();
        assertThat(executor.supports(" END_NODE ")).isTrue();
        assertThat(executor.supports("condition")).isFalse();
    }

    @Test
    void executeEndNodeBuildsFinalOutputFromMappingsAndTemplate() throws JsonProcessingException {
        Map<String, String> outputMappings = new LinkedHashMap<>();
        outputMappings.put("customerName", "customer.name");
        outputMappings.put("score", "${score}");
        outputMappings.put("summaryText", "客户${customer.name}评分${score}");
        AiWorkflowNodeEntity node = endNode(configJson(AiWorkflowEndNodeConfig.builder()
                .outputMappings(outputMappings)
                .outputTemplate("客户${customer.name}评分${score}，路由${routeNextNodeKey}")
                .outputVariable("summary")
                .build()));

        AiWorkflowNodeExecutionResult result = executor.execute(node, context(Map.of(
                "customer", Map.of("name", "Alice"),
                "score", 88,
                "routeNextNodeKey", "end"
        )));

        assertThat(result.getStatus()).isEqualTo(AiWorkflowNodeExecutionResult.STATUS_SUCCESS);
        assertThat(result.getTerminalNode()).isTrue();
        assertThat(result.getWorkflowSuccess()).isTrue();
        assertThat(result.getFinalOutput()).isEqualTo("客户Alice评分88，路由end");
        assertThat(result.getOutputText()).isEqualTo("客户Alice评分88，路由end");
        assertThat(result.getOutputVariables()).containsEntry("customerName", "Alice");
        assertThat(result.getOutputVariables()).containsEntry("score", 88);
        assertThat(result.getOutputVariables()).containsEntry("summaryText", "客户Alice评分88");
        assertThat(result.getOutputVariables()).containsEntry("summary", "客户Alice评分88，路由end");
        assertThat(result.getErrorMessage()).isNull();
    }

    @Test
    void executeEndNodeUsesAllContextVariablesWhenOutputConfigBlank() {
        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("answer", "处理完成");
        variables.put("ticketId", "T-001");

        AiWorkflowNodeExecutionResult result = executor.execute(endNode(null), context(variables));

        assertThat(result.getStatus()).isEqualTo(AiWorkflowNodeExecutionResult.STATUS_SUCCESS);
        assertThat(result.getOutputVariables()).containsEntry("answer", "处理完成");
        assertThat(result.getOutputVariables()).containsEntry("ticketId", "T-001");
        assertThat(result.getOutputText()).contains("\"answer\":\"处理完成\"");
        assertThat(result.getOutputText()).contains("\"ticketId\":\"T-001\"");
    }

    @Test
    void executeEndNodeReturnsFailedResultWhenVariableMissing() throws JsonProcessingException {
        AiWorkflowNodeEntity node = endNode(configJson(AiWorkflowEndNodeConfig.builder()
                .outputMappings(Map.of("answer", "missingAnswer"))
                .build()));

        AiWorkflowNodeExecutionResult result = executor.execute(node, context(Map.of("answer", "处理完成")));

        assertThat(result.getStatus()).isEqualTo(AiWorkflowNodeExecutionResult.STATUS_FAILED);
        assertThat(result.getTerminalNode()).isTrue();
        assertThat(result.getWorkflowSuccess()).isFalse();
        assertThat(result.getOutputVariables()).isEmpty();
        assertThat(result.getErrorCode()).isEqualTo("BusinessException");
        assertThat(result.getErrorMessage()).isEqualTo("结束节点缺少变量：missingAnswer");
    }

    @Test
    void executeEndNodeReturnsFailedResultWhenStatusVariableFalse() throws JsonProcessingException {
        AiWorkflowNodeEntity node = endNode(configJson(AiWorkflowEndNodeConfig.builder()
                .outputMappings(Map.of("answer", "answer"))
                .statusVariable("approved")
                .errorCodeVariable("error.code")
                .errorMessageVariable("error.message")
                .build()));

        AiWorkflowNodeExecutionResult result = executor.execute(node, context(Map.of(
                "answer", "审批未通过",
                "approved", false,
                "error", Map.of("code", "REJECTED", "message", "人工审批拒绝")
        )));

        assertThat(result.getStatus()).isEqualTo(AiWorkflowNodeExecutionResult.STATUS_FAILED);
        assertThat(result.getTerminalNode()).isTrue();
        assertThat(result.getWorkflowSuccess()).isFalse();
        assertThat(result.getOutputVariables()).containsEntry("answer", "审批未通过");
        assertThat(result.getErrorCode()).isEqualTo("REJECTED");
        assertThat(result.getErrorMessage()).isEqualTo("人工审批拒绝");
    }

    @Test
    void executeEndNodeReturnsFailedResultWhenNextNodeConfigured() throws JsonProcessingException {
        AiWorkflowNodeEntity node = endNode(configJson(AiWorkflowEndNodeConfig.builder()
                .outputMappings(Map.of("answer", "answer"))
                .build()))
                .setNextNodeKeys("[\"next\"]");

        AiWorkflowNodeExecutionResult result = executor.execute(node, context(Map.of("answer", "处理完成")));

        assertThat(result.getStatus()).isEqualTo(AiWorkflowNodeExecutionResult.STATUS_FAILED);
        assertThat(result.getErrorCode()).isEqualTo("BusinessException");
        assertThat(result.getErrorMessage()).isEqualTo("结束节点不允许配置下游节点");
    }

    private AiWorkflowNodeEntity endNode(String configJson) {
        return AiWorkflowNodeEntity.builder()
                .nodeKey("end-1")
                .nodeName("结束节点")
                .nodeType("end")
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

    private String configJson(AiWorkflowEndNodeConfig config) throws JsonProcessingException {
        return objectMapper.writeValueAsString(config);
    }
}
