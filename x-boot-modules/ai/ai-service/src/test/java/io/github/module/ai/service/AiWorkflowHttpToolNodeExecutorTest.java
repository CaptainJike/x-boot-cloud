package io.github.module.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.module.ai.entity.AiWorkflowNodeEntity;
import io.github.module.ai.service.model.AiWorkflowHttpToolNodeConfig;
import io.github.module.ai.service.model.AiWorkflowHttpToolRequest;
import io.github.module.ai.service.model.AiWorkflowHttpToolResponse;
import io.github.module.ai.service.model.AiWorkflowNodeExecutionContext;
import io.github.module.ai.service.model.AiWorkflowNodeExecutionResult;
import io.github.module.ai.service.workflow.AiWorkflowHttpToolClient;
import io.github.module.ai.service.workflow.AiWorkflowHttpToolNodeExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiWorkflowHttpToolNodeExecutorTest {

    @Mock
    private AiWorkflowHttpToolClient httpToolClient;

    private ObjectMapper objectMapper;

    private AiWorkflowHttpToolNodeExecutor executor;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        executor = new AiWorkflowHttpToolNodeExecutor(httpToolClient, objectMapper);
    }

    @Test
    void supportsHttpToolNodeTypeIgnoringCase() {
        assertThat(executor.supports("http")).isTrue();
        assertThat(executor.supports(" HTTP_TOOL ")).isTrue();
        assertThat(executor.supports("llm")).isFalse();
    }

    @Test
    void executeHttpToolNodeRendersRequestAndRedactsResponse() throws JsonProcessingException {
        AiWorkflowNodeEntity node = httpNode(configJson(AiWorkflowHttpToolNodeConfig.builder()
                .method("post")
                .urlTemplate("https://api.example.com/customers/${customerId}")
                .allowedHosts(List.of("api.example.com"))
                .headers(Map.of("X-Trace-Id", "${trace}"))
                .authScheme("Bearer")
                .authTokenVariable("accessToken")
                .bodyTemplate("{\"name\":\"${name}\"}")
                .outputVariable("toolResult")
                .timeoutMs(3000)
                .build()));
        AiWorkflowNodeExecutionContext context = context(Map.of(
                "customerId", "C001",
                "trace", "trace-001",
                "accessToken", "secret-token",
                "name", "Alice"
        ));
        when(httpToolClient.exchange(org.mockito.ArgumentMatchers.any()))
                .thenReturn(response(200, "{\"id\":\"C001\",\"token\":\"secret-token\",\"name\":\"Alice\"}"));

        AiWorkflowNodeExecutionResult result = executor.execute(node, context);

        assertThat(result.getStatus()).isEqualTo(AiWorkflowNodeExecutionResult.STATUS_SUCCESS);
        assertThat(result.getHttpMethod()).isEqualTo("POST");
        assertThat(result.getRequestUrl()).isEqualTo("https://api.example.com/customers/C001");
        assertThat(result.getHttpStatusCode()).isEqualTo(200);
        assertThat(result.getOutputText()).isEqualTo("{\"id\":\"C001\",\"token\":\"***\",\"name\":\"Alice\"}");
        assertThat(result.getOutputVariables()).containsEntry("toolResultStatusCode", 200);
        assertThat(result.getOutputVariables()).containsEntry(
                "toolResult", "{\"id\":\"C001\",\"token\":\"***\",\"name\":\"Alice\"}"
        );

        ArgumentCaptor<AiWorkflowHttpToolRequest> requestCaptor =
                ArgumentCaptor.forClass(AiWorkflowHttpToolRequest.class);
        verify(httpToolClient).exchange(requestCaptor.capture());
        AiWorkflowHttpToolRequest request = requestCaptor.getValue();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getUrl()).isEqualTo("https://api.example.com/customers/C001");
        assertThat(request.getHeaders()).containsEntry("X-Trace-Id", "trace-001");
        assertThat(request.getHeaders()).containsEntry("Authorization", "Bearer secret-token");
        assertThat(request.getBody()).isEqualTo("{\"name\":\"Alice\"}");
        assertThat(request.getTimeoutMs()).isEqualTo(3000);
    }

    @Test
    void executeHttpToolNodeReturnsFailedResultWhenHostNotWhitelisted() throws JsonProcessingException {
        AiWorkflowNodeEntity node = httpNode(configJson(AiWorkflowHttpToolNodeConfig.builder()
                .method("GET")
                .urlTemplate("https://evil.example.com/orders")
                .allowedHosts(List.of("api.example.com"))
                .build()));

        AiWorkflowNodeExecutionResult result = executor.execute(node, context(Map.of()));

        assertThat(result.getStatus()).isEqualTo(AiWorkflowNodeExecutionResult.STATUS_FAILED);
        assertThat(result.getErrorCode()).isEqualTo("BusinessException");
        assertThat(result.getErrorMessage()).isEqualTo("HTTP工具节点目标地址不在白名单：evil.example.com");
        verify(httpToolClient, never()).exchange(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void executeHttpToolNodeReturnsFailedResultWhenStatusNotSuccessful() throws JsonProcessingException {
        AiWorkflowNodeEntity node = httpNode(configJson(AiWorkflowHttpToolNodeConfig.builder()
                .method("GET")
                .urlTemplate("https://api.example.com/orders")
                .allowedHosts(List.of("*.example.com"))
                .build()));
        when(httpToolClient.exchange(org.mockito.ArgumentMatchers.any()))
                .thenReturn(response(500, "{\"message\":\"down\"}"));

        AiWorkflowNodeExecutionResult result = executor.execute(node, context(Map.of()));

        assertThat(result.getStatus()).isEqualTo(AiWorkflowNodeExecutionResult.STATUS_FAILED);
        assertThat(result.getHttpStatusCode()).isEqualTo(500);
        assertThat(result.getOutputText()).isEqualTo("{\"message\":\"down\"}");
        assertThat(result.getOutputVariables()).isEmpty();
        assertThat(result.getErrorCode()).isEqualTo("HTTP_STATUS_ERROR");
        assertThat(result.getErrorMessage()).isEqualTo("HTTP工具节点返回非成功状态：500");
    }

    @Test
    void executeHttpToolNodeReturnsFailedResultWhenTimeoutOutOfRange() throws JsonProcessingException {
        AiWorkflowNodeEntity node = httpNode(configJson(AiWorkflowHttpToolNodeConfig.builder()
                .method("GET")
                .urlTemplate("https://api.example.com/orders")
                .allowedHosts(List.of("api.example.com"))
                .timeoutMs(60000)
                .build()));

        AiWorkflowNodeExecutionResult result = executor.execute(node, context(Map.of()));

        assertThat(result.getStatus()).isEqualTo(AiWorkflowNodeExecutionResult.STATUS_FAILED);
        assertThat(result.getErrorCode()).isEqualTo("BusinessException");
        assertThat(result.getErrorMessage()).isEqualTo("HTTP工具节点超时时间必须在100到30000毫秒之间");
        verify(httpToolClient, never()).exchange(org.mockito.ArgumentMatchers.any());
    }

    private AiWorkflowNodeEntity httpNode(String configJson) {
        return AiWorkflowNodeEntity.builder()
                .nodeKey("http-1")
                .nodeName("HTTP工具节点")
                .nodeType("http")
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

    private String configJson(AiWorkflowHttpToolNodeConfig config) throws JsonProcessingException {
        return objectMapper.writeValueAsString(config);
    }

    private AiWorkflowHttpToolResponse response(Integer statusCode, String body) {
        return AiWorkflowHttpToolResponse.builder()
                .statusCode(statusCode)
                .body(body)
                .build();
    }
}
