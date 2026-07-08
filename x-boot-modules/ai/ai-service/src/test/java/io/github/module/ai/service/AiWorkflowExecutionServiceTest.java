package io.github.module.ai.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.framework.core.context.UserContext;
import io.github.framework.core.context.UserContextHolder;
import io.github.framework.core.enums.EnabledStatusEnum;
import io.github.framework.core.exception.BusinessException;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.module.ai.entity.AiAgentEntity;
import io.github.module.ai.entity.AiWorkflowDefinitionEntity;
import io.github.module.ai.entity.AiWorkflowExecutionEntity;
import io.github.module.ai.entity.AiWorkflowNodeEntity;
import io.github.module.ai.mapper.AiAgentMapper;
import io.github.module.ai.mapper.AiWorkflowDefinitionMapper;
import io.github.module.ai.mapper.AiWorkflowExecutionMapper;
import io.github.module.ai.mapper.AiWorkflowNodeMapper;
import io.github.module.ai.model.request.AdminExecuteAiWorkflowDTO;
import io.github.module.ai.model.request.AdminListAiWorkflowExecutionDTO;
import io.github.module.ai.model.response.AdminAiWorkflowExecutionBO;
import io.github.module.ai.model.response.AdminAiWorkflowExecutionDetailBO;
import io.github.module.ai.model.response.AdminAiWorkflowExecutionResultBO;
import io.github.module.ai.service.model.AiWorkflowNodeExecutionContext;
import io.github.module.ai.service.model.AiWorkflowNodeExecutionResult;
import io.github.module.ai.service.workflow.AiWorkflowNodeExecutor;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiWorkflowExecutionServiceTest {

    @BeforeAll
    static void initTableInfo() {
        MapperBuilderAssistant mapperBuilderAssistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, AiAgentEntity.class);
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, AiWorkflowDefinitionEntity.class);
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, AiWorkflowNodeEntity.class);
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, AiWorkflowExecutionEntity.class);
    }

    @Mock
    private AiWorkflowDefinitionMapper aiWorkflowDefinitionMapper;

    @Mock
    private AiWorkflowNodeMapper aiWorkflowNodeMapper;

    @Mock
    private AiWorkflowExecutionMapper aiWorkflowExecutionMapper;

    @Mock
    private AiAgentMapper aiAgentMapper;

    @Mock
    private AiWorkflowNodeExecutor nodeExecutor;

    private ObjectMapper objectMapper;

    private AiWorkflowExecutionService aiWorkflowExecutionService;

    @BeforeEach
    void setUp() {
        UserContextHolder.setUserContext(UserContext.builder().userId(9L).userName("admin").build());
        objectMapper = new ObjectMapper();
        aiWorkflowExecutionService = new AiWorkflowExecutionService(
                aiWorkflowDefinitionMapper,
                aiWorkflowNodeMapper,
                aiWorkflowExecutionMapper,
                aiAgentMapper,
                List.of(nodeExecutor),
                objectMapper
        );
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void adminExecutePersistsSuccessAndRefreshesStats() {
        AiWorkflowDefinitionEntity workflow = workflow().setExecutionCount(2);
        AiAgentEntity agent = new AiAgentEntity().setExecutionCount(4);
        agent.setId(8L);
        when(aiWorkflowDefinitionMapper.selectById(1L)).thenReturn(workflow);
        when(aiWorkflowNodeMapper.selectList(any())).thenReturn(List.of(node("start", "mock", "")));
        when(aiWorkflowExecutionMapper.insert(any(AiWorkflowExecutionEntity.class))).thenAnswer(invocation -> {
            AiWorkflowExecutionEntity entity = invocation.getArgument(0);
            entity.setId(100L);
            return 1;
        });
        when(nodeExecutor.supports("mock")).thenReturn(true);
        when(nodeExecutor.execute(any(), any())).thenReturn(successNodeResult());
        when(aiAgentMapper.selectById(8L)).thenReturn(agent);

        AdminAiWorkflowExecutionResultBO result = aiWorkflowExecutionService.adminExecute(
                1L,
                AdminExecuteAiWorkflowDTO.builder()
                        .input("请生成回复")
                        .variables(Map.of("customer", "张三"))
                        .triggerSource("manual")
                        .triggerId("ticket-1")
                        .traceId("trace-1")
                        .build()
        );

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getStatus()).isEqualTo(1);
        assertThat(result.getFinalOutput()).isEqualTo("done");
        assertThat(result.getOutputVariables()).containsEntry("answer", "done");
        assertThat(result.getNodeResults()).hasSize(1);

        ArgumentCaptor<AiWorkflowExecutionEntity> insertCaptor =
                ArgumentCaptor.forClass(AiWorkflowExecutionEntity.class);
        verify(aiWorkflowExecutionMapper).insert(insertCaptor.capture());
        AiWorkflowExecutionEntity inserted = insertCaptor.getValue();
        assertThat(inserted.getExecutionId()).isNotBlank();
        assertThat(inserted.getUserId()).isEqualTo(9L);
        assertThat(inserted.getTriggerSource()).isEqualTo("manual");
        assertThat(inserted.getTriggerId()).isEqualTo("ticket-1");
        assertThat(inserted.getTraceId()).isEqualTo("trace-1");
        assertThat(inserted.getStatus()).isEqualTo(2);
        assertThat(inserted.getInputSummary()).contains("customer", "input");

        ArgumentCaptor<AiWorkflowExecutionEntity> updateCaptor =
                ArgumentCaptor.forClass(AiWorkflowExecutionEntity.class);
        verify(aiWorkflowExecutionMapper).updateById(updateCaptor.capture());
        AiWorkflowExecutionEntity updated = updateCaptor.getValue();
        assertThat(updated.getId()).isEqualTo(100L);
        assertThat(updated.getStatus()).isEqualTo(1);
        assertThat(updated.getCurrentNodeKey()).isEqualTo("start");
        assertThat(updated.getFailedNodeKey()).isNull();
        assertThat(updated.getOutputSummary()).contains("done", "nodeResults");

        ArgumentCaptor<AiWorkflowDefinitionEntity> workflowCaptor =
                ArgumentCaptor.forClass(AiWorkflowDefinitionEntity.class);
        verify(aiWorkflowDefinitionMapper).updateById(workflowCaptor.capture());
        assertThat(workflowCaptor.getValue().getId()).isEqualTo(1L);
        assertThat(workflowCaptor.getValue().getExecutionCount()).isEqualTo(3);

        ArgumentCaptor<AiAgentEntity> agentCaptor = ArgumentCaptor.forClass(AiAgentEntity.class);
        verify(aiAgentMapper).updateById(agentCaptor.capture());
        assertThat(agentCaptor.getValue().getId()).isEqualTo(8L);
        assertThat(agentCaptor.getValue().getExecutionCount()).isEqualTo(5);
    }

    @Test
    void adminExecuteRunsMultipleNodesAndMergesExecutionVariables() {
        AiWorkflowDefinitionEntity workflow = workflow().setAgentId(null);
        when(aiWorkflowDefinitionMapper.selectById(1L)).thenReturn(workflow);
        when(aiWorkflowNodeMapper.selectList(any())).thenReturn(List.of(
                node("start", "mock", "condition"),
                node("condition", "mock", "end"),
                node("end", "mock", "")
        ));
        when(aiWorkflowExecutionMapper.insert(any(AiWorkflowExecutionEntity.class))).thenAnswer(invocation -> {
            AiWorkflowExecutionEntity entity = invocation.getArgument(0);
            entity.setId(102L);
            return 1;
        });
        when(nodeExecutor.supports("mock")).thenReturn(true);
        when(nodeExecutor.execute(any(AiWorkflowNodeEntity.class), any(AiWorkflowNodeExecutionContext.class)))
                .thenAnswer(invocation -> {
                    AiWorkflowNodeEntity node = invocation.getArgument(0);
                    return switch (node.getNodeKey()) {
                        case "start" -> AiWorkflowNodeExecutionResult.builder()
                                .nodeKey("start")
                                .nodeType("mock")
                                .status(AiWorkflowNodeExecutionResult.STATUS_SUCCESS)
                                .nextNodeKey("condition")
                                .outputVariables(Map.of("draft", "初稿"))
                                .build();
                        case "condition" -> AiWorkflowNodeExecutionResult.builder()
                                .nodeKey("condition")
                                .nodeType("mock")
                                .status(AiWorkflowNodeExecutionResult.STATUS_SUCCESS)
                                .nextNodeKey("end")
                                .outputVariables(Map.of("routeNextNodeKey", "end"))
                                .build();
                        default -> AiWorkflowNodeExecutionResult.builder()
                                .nodeKey("end")
                                .nodeType("mock")
                                .status(AiWorkflowNodeExecutionResult.STATUS_SUCCESS)
                                .terminalNode(true)
                                .workflowSuccess(true)
                                .finalOutput("处理完成")
                                .outputVariables(Map.of("summary", "处理完成"))
                                .build();
                    };
                });

        AdminAiWorkflowExecutionResultBO result = aiWorkflowExecutionService.adminExecute(
                1L,
                AdminExecuteAiWorkflowDTO.builder()
                        .input("处理客户工单")
                        .variables(Map.of("customerId", "C-001"))
                        .build()
        );

        assertThat(result.getStatus()).isEqualTo(1);
        assertThat(result.getCurrentNodeKey()).isEqualTo("end");
        assertThat(result.getFailedNodeKey()).isNull();
        assertThat(result.getFinalOutput()).isEqualTo("处理完成");
        assertThat(result.getOutputVariables()).containsEntry("customerId", "C-001");
        assertThat(result.getOutputVariables()).containsEntry("input", "处理客户工单");
        assertThat(result.getOutputVariables()).containsEntry("draft", "初稿");
        assertThat(result.getOutputVariables()).containsEntry("routeNextNodeKey", "end");
        assertThat(result.getOutputVariables()).containsEntry("summary", "处理完成");
        assertThat(result.getNodeResults()).extracting("nodeKey").containsExactly("start", "condition", "end");

        ArgumentCaptor<AiWorkflowNodeExecutionContext> contextCaptor =
                ArgumentCaptor.forClass(AiWorkflowNodeExecutionContext.class);
        verify(nodeExecutor, times(3)).execute(any(AiWorkflowNodeEntity.class), contextCaptor.capture());
        List<AiWorkflowNodeExecutionContext> contexts = contextCaptor.getAllValues();
        assertThat(contexts.get(0).getVariables()).containsEntry("input", "处理客户工单");
        assertThat(contexts.get(1).getVariables()).containsEntry("draft", "初稿");
        assertThat(contexts.get(2).getVariables()).containsEntry("routeNextNodeKey", "end");

        ArgumentCaptor<AiWorkflowExecutionEntity> updateCaptor =
                ArgumentCaptor.forClass(AiWorkflowExecutionEntity.class);
        verify(aiWorkflowExecutionMapper).updateById(updateCaptor.capture());
        AiWorkflowExecutionEntity updated = updateCaptor.getValue();
        assertThat(updated.getStatus()).isEqualTo(1);
        assertThat(updated.getOutputSummary()).contains("variables", "nodeResults", "处理完成");
    }

    @Test
    void adminExecutePersistsFailureWhenNodeFails() {
        AiWorkflowDefinitionEntity workflow = workflow().setAgentId(null);
        when(aiWorkflowDefinitionMapper.selectById(1L)).thenReturn(workflow);
        when(aiWorkflowNodeMapper.selectList(any())).thenReturn(List.of(node("start", "mock", "")));
        when(aiWorkflowExecutionMapper.insert(any(AiWorkflowExecutionEntity.class))).thenAnswer(invocation -> {
            AiWorkflowExecutionEntity entity = invocation.getArgument(0);
            entity.setId(101L);
            return 1;
        });
        when(nodeExecutor.supports("mock")).thenReturn(true);
        when(nodeExecutor.execute(any(), any())).thenReturn(AiWorkflowNodeExecutionResult.builder()
                .nodeKey("start")
                .nodeType("mock")
                .status(AiWorkflowNodeExecutionResult.STATUS_FAILED)
                .errorCode("MOCK_FAILED")
                .errorMessage("模拟失败")
                .outputVariables(Map.of())
                .build());

        AdminAiWorkflowExecutionResultBO result = aiWorkflowExecutionService.adminExecute(
                1L,
                AdminExecuteAiWorkflowDTO.builder().input("请生成回复").build()
        );

        assertThat(result.getStatus()).isZero();
        assertThat(result.getFailedNodeKey()).isEqualTo("start");
        assertThat(result.getErrorCode()).isEqualTo("MOCK_FAILED");
        assertThat(result.getErrorMessage()).isEqualTo("模拟失败");

        ArgumentCaptor<AiWorkflowExecutionEntity> updateCaptor =
                ArgumentCaptor.forClass(AiWorkflowExecutionEntity.class);
        verify(aiWorkflowExecutionMapper).updateById(updateCaptor.capture());
        assertThat(updateCaptor.getValue().getStatus()).isZero();
        assertThat(updateCaptor.getValue().getFailedNodeKey()).isEqualTo("start");
        assertThat(updateCaptor.getValue().getErrorCode()).isEqualTo("MOCK_FAILED");
        verify(aiAgentMapper, never()).updateById(any(AiAgentEntity.class));
    }

    @Test
    void adminExecuteKeepsTerminalFailureOutputInExecutionRecord() {
        AiWorkflowDefinitionEntity workflow = workflow().setAgentId(null).setEntryNodeKey("end");
        when(aiWorkflowDefinitionMapper.selectById(1L)).thenReturn(workflow);
        when(aiWorkflowNodeMapper.selectList(any())).thenReturn(List.of(node("end", "mock", "")));
        when(aiWorkflowExecutionMapper.insert(any(AiWorkflowExecutionEntity.class))).thenAnswer(invocation -> {
            AiWorkflowExecutionEntity entity = invocation.getArgument(0);
            entity.setId(103L);
            return 1;
        });
        when(nodeExecutor.supports("mock")).thenReturn(true);
        when(nodeExecutor.execute(any(), any())).thenReturn(AiWorkflowNodeExecutionResult.builder()
                .nodeKey("end")
                .nodeType("mock")
                .status(AiWorkflowNodeExecutionResult.STATUS_FAILED)
                .terminalNode(true)
                .workflowSuccess(false)
                .finalOutput("审批未通过")
                .outputVariables(Map.of("summary", "审批未通过"))
                .errorCode("REJECTED")
                .errorMessage("人工审批拒绝")
                .build());

        AdminAiWorkflowExecutionResultBO result = aiWorkflowExecutionService.adminExecute(
                1L,
                AdminExecuteAiWorkflowDTO.builder().input("提交审批").build()
        );

        assertThat(result.getStatus()).isZero();
        assertThat(result.getCurrentNodeKey()).isEqualTo("end");
        assertThat(result.getFailedNodeKey()).isEqualTo("end");
        assertThat(result.getFinalOutput()).isEqualTo("审批未通过");
        assertThat(result.getErrorCode()).isEqualTo("REJECTED");
        assertThat(result.getErrorMessage()).isEqualTo("人工审批拒绝");
        assertThat(result.getOutputVariables()).containsEntry("summary", "审批未通过");

        ArgumentCaptor<AiWorkflowExecutionEntity> updateCaptor =
                ArgumentCaptor.forClass(AiWorkflowExecutionEntity.class);
        verify(aiWorkflowExecutionMapper).updateById(updateCaptor.capture());
        AiWorkflowExecutionEntity updated = updateCaptor.getValue();
        assertThat(updated.getStatus()).isZero();
        assertThat(updated.getFailedNodeKey()).isEqualTo("end");
        assertThat(updated.getErrorCode()).isEqualTo("REJECTED");
        assertThat(updated.getOutputSummary()).contains("审批未通过", "REJECTED", "nodeResults");
    }

    @Test
    void adminExecutePersistsFailureWhenNodeExecutorUnavailable() {
        AiWorkflowDefinitionEntity workflow = workflow().setAgentId(null);
        when(aiWorkflowDefinitionMapper.selectById(1L)).thenReturn(workflow);
        when(aiWorkflowNodeMapper.selectList(any())).thenReturn(List.of(node("start", "unknown", "")));
        when(aiWorkflowExecutionMapper.insert(any(AiWorkflowExecutionEntity.class))).thenAnswer(invocation -> {
            AiWorkflowExecutionEntity entity = invocation.getArgument(0);
            entity.setId(104L);
            return 1;
        });

        AdminAiWorkflowExecutionResultBO result = aiWorkflowExecutionService.adminExecute(
                1L,
                AdminExecuteAiWorkflowDTO.builder().input("执行未知节点").build()
        );

        assertThat(result.getStatus()).isZero();
        assertThat(result.getFailedNodeKey()).isEqualTo("start");
        assertThat(result.getErrorCode()).isEqualTo("WORKFLOW_NODE_EXECUTOR_UNAVAILABLE");
        assertThat(result.getErrorMessage()).isEqualTo("工作流节点执行器不可用");
        assertThat(result.getNodeResults()).isEmpty();

        ArgumentCaptor<AiWorkflowExecutionEntity> updateCaptor =
                ArgumentCaptor.forClass(AiWorkflowExecutionEntity.class);
        verify(aiWorkflowExecutionMapper).updateById(updateCaptor.capture());
        AiWorkflowExecutionEntity updated = updateCaptor.getValue();
        assertThat(updated.getStatus()).isZero();
        assertThat(updated.getCurrentNodeKey()).isEqualTo("start");
        assertThat(updated.getFailedNodeKey()).isEqualTo("start");
        assertThat(updated.getOutputSummary()).contains(
                "WORKFLOW_NODE_EXECUTOR_UNAVAILABLE",
                "工作流节点执行器不可用",
                "nodeResults"
        );
        verify(nodeExecutor, never()).execute(any(), any());
    }

    @Test
    void adminExecuteRejectsDisabledWorkflowBeforeWritingExecution() {
        when(aiWorkflowDefinitionMapper.selectById(1L)).thenReturn(workflow()
                .setStatus(EnabledStatusEnum.DISABLED.getValue()));

        assertThatThrownBy(() -> aiWorkflowExecutionService.adminExecute(1L, new AdminExecuteAiWorkflowDTO()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("无效工作流状态");
        verify(aiWorkflowExecutionMapper, never()).insert(any(AiWorkflowExecutionEntity.class));
    }

    @Test
    void adminListExecutionsReturnsPagedRecords() {
        AiWorkflowExecutionEntity entity = execution();
        Page<AiWorkflowExecutionEntity> entityPage = new Page<AiWorkflowExecutionEntity>()
                .setCurrent(1)
                .setSize(10)
                .setTotal(1)
                .setRecords(List.of(entity));
        when(aiWorkflowExecutionMapper.selectPage(any(), any())).thenReturn(entityPage);

        PageResult<AdminAiWorkflowExecutionBO> result = aiWorkflowExecutionService.adminListExecutions(
                new PageParam(),
                AdminListAiWorkflowExecutionDTO.builder().workflowCode("customer-flow").build()
        );

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().getFirst().getExecutionId()).isEqualTo("exec-1");
        assertThat(result.getRecords().getFirst().getWorkflowName()).isEqualTo("客服流程");
    }

    @Test
    void getExecutionByIdReturnsDetail() {
        when(aiWorkflowExecutionMapper.selectById(100L)).thenReturn(execution()
                .setInputSummary("{\"input\":\"hi\"}")
                .setOutputSummary("{\"finalOutput\":\"done\"}"));

        AdminAiWorkflowExecutionDetailBO result = aiWorkflowExecutionService.getExecutionById(100L, true);

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getExecutionId()).isEqualTo("exec-1");
        assertThat(result.getInputSummary()).contains("input");
        assertThat(result.getOutputSummary()).contains("done");
    }

    private AiWorkflowNodeExecutionResult successNodeResult() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 22, 10, 0);
        return AiWorkflowNodeExecutionResult.builder()
                .nodeKey("start")
                .nodeType("mock")
                .status(AiWorkflowNodeExecutionResult.STATUS_SUCCESS)
                .terminalNode(true)
                .workflowSuccess(true)
                .finalOutput("done")
                .outputText("done")
                .outputVariables(Map.of("answer", "done"))
                .durationMs(12L)
                .startedAt(now)
                .finishedAt(now.plusSeconds(1))
                .build();
    }

    private AiWorkflowDefinitionEntity workflow() {
        AiWorkflowDefinitionEntity entity = new AiWorkflowDefinitionEntity()
                .setWorkflowCode("customer-flow")
                .setName("客服流程")
                .setAgentId(8L)
                .setVersionNo(1)
                .setEntryNodeKey("start")
                .setStatus(EnabledStatusEnum.ENABLED.getValue())
                .setExecutionCount(0);
        entity.setId(1L);
        return entity;
    }

    private AiWorkflowNodeEntity node(String nodeKey, String nodeType, String nextNodeKeys) {
        AiWorkflowNodeEntity entity = new AiWorkflowNodeEntity()
                .setWorkflowDefinitionId(1L)
                .setWorkflowCode("customer-flow")
                .setVersionNo(1)
                .setNodeKey(nodeKey)
                .setNodeName(nodeKey)
                .setNodeType(nodeType)
                .setNextNodeKeys(nextNodeKeys)
                .setStatus(EnabledStatusEnum.ENABLED.getValue());
        entity.setId(10L);
        return entity;
    }

    private AiWorkflowExecutionEntity execution() {
        AiWorkflowExecutionEntity entity = new AiWorkflowExecutionEntity()
                .setExecutionId("exec-1")
                .setWorkflowDefinitionId(1L)
                .setWorkflowCode("customer-flow")
                .setWorkflowName("客服流程")
                .setVersionNo(1)
                .setAgentId(8L)
                .setUserId(9L)
                .setTriggerSource("manual")
                .setTriggerId("ticket-1")
                .setStatus(1)
                .setCurrentNodeKey("start")
                .setDurationMs(12L)
                .setTraceId("trace-1")
                .setStartedAt(LocalDateTime.of(2026, 6, 22, 10, 0))
                .setFinishedAt(LocalDateTime.of(2026, 6, 22, 10, 0, 1));
        entity.setId(100L);
        return entity;
    }
}
