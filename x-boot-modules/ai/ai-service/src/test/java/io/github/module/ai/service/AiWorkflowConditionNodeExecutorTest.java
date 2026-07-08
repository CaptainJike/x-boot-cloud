package io.github.module.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.module.ai.entity.AiWorkflowNodeEntity;
import io.github.module.ai.service.model.AiWorkflowConditionBranchConfig;
import io.github.module.ai.service.model.AiWorkflowConditionNodeConfig;
import io.github.module.ai.service.model.AiWorkflowNodeExecutionContext;
import io.github.module.ai.service.model.AiWorkflowNodeExecutionResult;
import io.github.module.ai.service.workflow.AiWorkflowConditionNodeExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AiWorkflowConditionNodeExecutorTest {

    private ObjectMapper objectMapper;

    private AiWorkflowConditionNodeExecutor executor;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        executor = new AiWorkflowConditionNodeExecutor(objectMapper);
    }

    @Test
    void supportsConditionNodeTypeIgnoringCase() {
        assertThat(executor.supports("condition")).isTrue();
        assertThat(executor.supports(" CONDITION ")).isTrue();
        assertThat(executor.supports("llm")).isFalse();
    }

    @Test
    void executeConditionNodeSelectsFirstMatchingBranch() throws JsonProcessingException {
        AiWorkflowNodeEntity node = conditionNode(configJson(AiWorkflowConditionNodeConfig.builder()
                .branches(List.of(
                        branch("vip", "${score} >= 80 && ${customer.tier} == 'gold'", "approve"),
                        branch("manual", "${score} >= 60", "manual")
                ))
                .defaultNextNodeKey("reject")
                .outputVariable("route")
                .build()))
                .setNextNodeKeys("[\"approve\",\"manual\",\"reject\"]");

        AiWorkflowNodeExecutionResult result = executor.execute(
                node, context(Map.of("score", 88, "customer", Map.of("tier", "gold")))
        );

        assertThat(result.getStatus()).isEqualTo(AiWorkflowNodeExecutionResult.STATUS_SUCCESS);
        assertThat(result.getConditionMatched()).isTrue();
        assertThat(result.getSelectedBranchKey()).isEqualTo("vip");
        assertThat(result.getConditionExpression()).isEqualTo("${score} >= 80 && ${customer.tier} == 'gold'");
        assertThat(result.getNextNodeKey()).isEqualTo("approve");
        assertThat(result.getOutputVariables()).containsEntry("routeMatched", true);
        assertThat(result.getOutputVariables()).containsEntry("routeBranchKey", "vip");
        assertThat(result.getOutputVariables()).containsEntry("routeNextNodeKey", "approve");
    }

    @Test
    void executeConditionNodeUsesDefaultBranchWhenNoExpressionMatches() throws JsonProcessingException {
        AiWorkflowNodeEntity node = conditionNode(configJson(AiWorkflowConditionNodeConfig.builder()
                .branches(List.of(branch("vip", "${score} >= 80", "approve")))
                .defaultNextNodeKey("reject")
                .build()))
                .setNextNodeKeys("approve,reject");

        AiWorkflowNodeExecutionResult result = executor.execute(node, context(Map.of("score", 40)));

        assertThat(result.getStatus()).isEqualTo(AiWorkflowNodeExecutionResult.STATUS_SUCCESS);
        assertThat(result.getConditionMatched()).isFalse();
        assertThat(result.getSelectedBranchKey()).isEqualTo("default");
        assertThat(result.getNextNodeKey()).isEqualTo("reject");
        assertThat(result.getOutputVariables()).containsEntry("conditionNextNodeKey", "reject");
    }

    @Test
    void executeConditionNodeUsesEntityExpressionAndNextNodeOrder() {
        AiWorkflowNodeEntity node = conditionNode(null)
                .setConditionExpression("${approved}")
                .setNextNodeKeys("pass,fail");

        AiWorkflowNodeExecutionResult result = executor.execute(node, context(Map.of("approved", false)));

        assertThat(result.getStatus()).isEqualTo(AiWorkflowNodeExecutionResult.STATUS_SUCCESS);
        assertThat(result.getConditionMatched()).isFalse();
        assertThat(result.getSelectedBranchKey()).isEqualTo("default");
        assertThat(result.getNextNodeKey()).isEqualTo("fail");
    }

    @Test
    void executeConditionNodeReturnsFailedResultWhenVariableMissing() throws JsonProcessingException {
        AiWorkflowNodeEntity node = conditionNode(configJson(AiWorkflowConditionNodeConfig.builder()
                .branches(List.of(branch("manual", "${score} >= 60", "manual")))
                .build()));

        AiWorkflowNodeExecutionResult result = executor.execute(node, context(Map.of()));

        assertThat(result.getStatus()).isEqualTo(AiWorkflowNodeExecutionResult.STATUS_FAILED);
        assertThat(result.getErrorCode()).isEqualTo("BusinessException");
        assertThat(result.getErrorMessage()).isEqualTo("条件节点缺少变量：score");
        assertThat(result.getOutputVariables()).isEmpty();
    }

    @Test
    void executeConditionNodeReturnsFailedResultWhenSelectedNextNodeNotDeclared() throws JsonProcessingException {
        AiWorkflowNodeEntity node = conditionNode(configJson(AiWorkflowConditionNodeConfig.builder()
                .branches(List.of(branch("vip", "${score} >= 80", "unknown")))
                .build()))
                .setNextNodeKeys("approve,reject");

        AiWorkflowNodeExecutionResult result = executor.execute(node, context(Map.of("score", 99)));

        assertThat(result.getStatus()).isEqualTo(AiWorkflowNodeExecutionResult.STATUS_FAILED);
        assertThat(result.getErrorCode()).isEqualTo("BusinessException");
        assertThat(result.getErrorMessage()).isEqualTo("条件节点下游节点未声明：unknown");
    }

    private AiWorkflowNodeEntity conditionNode(String configJson) {
        return AiWorkflowNodeEntity.builder()
                .nodeKey("condition-1")
                .nodeName("条件节点")
                .nodeType("condition")
                .nodeConfig(configJson)
                .build();
    }

    private AiWorkflowConditionBranchConfig branch(String branchKey, String expression, String nextNodeKey) {
        return AiWorkflowConditionBranchConfig.builder()
                .branchKey(branchKey)
                .expression(expression)
                .nextNodeKey(nextNodeKey)
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

    private String configJson(AiWorkflowConditionNodeConfig config) throws JsonProcessingException {
        return objectMapper.writeValueAsString(config);
    }
}
