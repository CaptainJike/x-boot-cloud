package io.github.module.ai.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.context.UserContextHolder;
import io.github.framework.core.enums.EnabledStatusEnum;
import io.github.framework.core.exception.BusinessException;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.module.ai.entity.AiAgentEntity;
import io.github.module.ai.entity.AiWorkflowDefinitionEntity;
import io.github.module.ai.entity.AiWorkflowExecutionEntity;
import io.github.module.ai.entity.AiWorkflowNodeEntity;
import io.github.module.ai.enums.AiErrorEnum;
import io.github.module.ai.mapper.AiAgentMapper;
import io.github.module.ai.mapper.AiWorkflowDefinitionMapper;
import io.github.module.ai.mapper.AiWorkflowExecutionMapper;
import io.github.module.ai.mapper.AiWorkflowNodeMapper;
import io.github.module.ai.model.request.AdminExecuteAiWorkflowDTO;
import io.github.module.ai.model.request.AdminListAiWorkflowExecutionDTO;
import io.github.module.ai.model.response.AdminAiWorkflowExecutionBO;
import io.github.module.ai.model.response.AdminAiWorkflowExecutionDetailBO;
import io.github.module.ai.model.response.AdminAiWorkflowExecutionResultBO;
import io.github.module.ai.model.response.AdminAiWorkflowNodeExecutionResultBO;
import io.github.module.ai.service.model.AiWorkflowNodeExecutionContext;
import io.github.module.ai.service.model.AiWorkflowNodeExecutionResult;
import io.github.module.ai.service.workflow.AiWorkflowNodeExecutor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AI 工作流执行.
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class AiWorkflowExecutionService {

    private static final int STATUS_FAILED = 0;

    private static final int STATUS_SUCCESS = 1;

    private static final int STATUS_RUNNING = 2;

    private static final String DEFAULT_TRIGGER_SOURCE = "admin";

    private static final int MAX_NODE_EXECUTION_COUNT = 100;

    private static final int ERROR_CODE_MAX_LENGTH = 64;

    private static final int ERROR_MESSAGE_MAX_LENGTH = 1000;

    private final AiWorkflowDefinitionMapper aiWorkflowDefinitionMapper;

    private final AiWorkflowNodeMapper aiWorkflowNodeMapper;

    private final AiWorkflowExecutionMapper aiWorkflowExecutionMapper;

    private final AiAgentMapper aiAgentMapper;

    private final List<AiWorkflowNodeExecutor> nodeExecutors;

    private final ObjectMapper objectMapper;

    /**
     * 后台管理-执行工作流.
     */
    public AdminAiWorkflowExecutionResultBO adminExecute(Long workflowDefinitionId, AdminExecuteAiWorkflowDTO dto) {
        AdminExecuteAiWorkflowDTO executeDTO = dto == null ? new AdminExecuteAiWorkflowDTO() : dto;
        executeDTO.setWorkflowDefinitionId(workflowDefinitionId);
        AiWorkflowDefinitionEntity workflow = getExecutableWorkflow(workflowDefinitionId);
        List<AiWorkflowNodeEntity> nodes = listEnabledNodes(workflowDefinitionId);
        Map<String, AiWorkflowNodeEntity> nodeMap = nodeMap(nodes);
        String entryNodeKey = resolveEntryNodeKey(workflow, nodes);
        AiErrorEnum.WORKFLOW_ENTRY_NODE_UNAVAILABLE.assertNotNull(nodeMap.get(entryNodeKey));

        Map<String, Object> variables = buildVariables(executeDTO);
        String executionId = IdUtil.fastSimpleUUID();
        String traceId = StrUtil.blankToDefault(trim(executeDTO.getTraceId()), executionId);
        LocalDateTime startedAt = LocalDateTime.now();
        long startedAtMillis = System.currentTimeMillis();
        AiWorkflowExecutionEntity execution = buildRunningExecution(
                workflow,
                executeDTO,
                variables,
                executionId,
                traceId,
                startedAt
        );
        aiWorkflowExecutionMapper.insert(execution);

        ExecutionState state = executeNodes(workflow, nodeMap, entryNodeKey, variables, executionId, traceId);
        LocalDateTime finishedAt = LocalDateTime.now();
        state.setDurationMs(Math.max(System.currentTimeMillis() - startedAtMillis, 0L));
        state.setFinishedAt(finishedAt);
        updateExecution(execution, state);
        refreshExecutionStats(workflow, finishedAt);

        return buildExecutionResultBO(execution, workflow, executeDTO, state, startedAt);
    }

    /**
     * 后台管理-工作流执行记录分页列表.
     */
    public PageResult<AdminAiWorkflowExecutionBO> adminListExecutions(
            PageParam pageParam,
            AdminListAiWorkflowExecutionDTO dto
    ) {
        AdminListAiWorkflowExecutionDTO query = dto == null ? new AdminListAiWorkflowExecutionDTO() : dto;
        Page<AiWorkflowExecutionEntity> entityPage = aiWorkflowExecutionMapper.selectPage(
                new Page<>(pageParam.getPageNum(), pageParam.getPageSize()),
                new QueryWrapper<AiWorkflowExecutionEntity>()
                        .lambda()
                        .like(CharSequenceUtil.isNotBlank(query.getExecutionId()),
                                AiWorkflowExecutionEntity::getExecutionId,
                                clean(query.getExecutionId()))
                        .eq(query.getWorkflowDefinitionId() != null,
                                AiWorkflowExecutionEntity::getWorkflowDefinitionId,
                                query.getWorkflowDefinitionId())
                        .eq(CharSequenceUtil.isNotBlank(query.getWorkflowCode()),
                                AiWorkflowExecutionEntity::getWorkflowCode,
                                clean(query.getWorkflowCode()))
                        .eq(query.getUserId() != null, AiWorkflowExecutionEntity::getUserId, query.getUserId())
                        .eq(CharSequenceUtil.isNotBlank(query.getTriggerSource()),
                                AiWorkflowExecutionEntity::getTriggerSource,
                                clean(query.getTriggerSource()))
                        .eq(CharSequenceUtil.isNotBlank(query.getTriggerId()),
                                AiWorkflowExecutionEntity::getTriggerId,
                                clean(query.getTriggerId()))
                        .eq(query.getStatus() != null, AiWorkflowExecutionEntity::getStatus, query.getStatus())
                        .eq(CharSequenceUtil.isNotBlank(query.getFailedNodeKey()),
                                AiWorkflowExecutionEntity::getFailedNodeKey,
                                clean(query.getFailedNodeKey()))
                        .orderByDesc(AiWorkflowExecutionEntity::getStartedAt)
                        .orderByDesc(AiWorkflowExecutionEntity::getCreatedAt)
        );

        return entityPage2BOPage(entityPage);
    }

    /**
     * 根据 ID 取工作流执行记录详情.
     */
    public AdminAiWorkflowExecutionDetailBO getExecutionById(Long id) {
        return this.getExecutionById(id, false);
    }

    /**
     * 根据 ID 取工作流执行记录详情.
     */
    public AdminAiWorkflowExecutionDetailBO getExecutionById(Long id, boolean throwIfInvalidId)
            throws BusinessException {
        AiWorkflowExecutionEntity entity = aiWorkflowExecutionMapper.selectById(id);
        if (throwIfInvalidId) {
            AiErrorEnum.INVALID_ID.assertNotNull(entity);
        }

        return entity2DetailBO(entity);
    }

    /*
    ----------------------------------------------------------------
                        私有方法 private methods
    ----------------------------------------------------------------
     */

    private AiWorkflowDefinitionEntity getExecutableWorkflow(Long workflowDefinitionId) {
        AiWorkflowDefinitionEntity workflow = aiWorkflowDefinitionMapper.selectById(workflowDefinitionId);
        AiErrorEnum.INVALID_ID.assertNotNull(workflow);
        if (!EnabledStatusEnum.ENABLED.getValue().equals(workflow.getStatus())) {
            throw new BusinessException(AiErrorEnum.INVALID_WORKFLOW_STATUS);
        }
        return workflow;
    }

    private List<AiWorkflowNodeEntity> listEnabledNodes(Long workflowDefinitionId) {
        List<AiWorkflowNodeEntity> nodes = aiWorkflowNodeMapper.selectList(
                new QueryWrapper<AiWorkflowNodeEntity>()
                        .lambda()
                        .eq(AiWorkflowNodeEntity::getWorkflowDefinitionId, workflowDefinitionId)
                        .eq(AiWorkflowNodeEntity::getStatus, EnabledStatusEnum.ENABLED.getValue())
                        .orderByAsc(AiWorkflowNodeEntity::getSortOrder)
                        .orderByAsc(AiWorkflowNodeEntity::getId)
        );
        if (CollUtil.isEmpty(nodes)) {
            throw new BusinessException(AiErrorEnum.WORKFLOW_NODE_UNAVAILABLE);
        }
        return nodes;
    }

    private Map<String, AiWorkflowNodeEntity> nodeMap(List<AiWorkflowNodeEntity> nodes) {
        return nodes.stream().collect(Collectors.toMap(
                AiWorkflowNodeEntity::getNodeKey,
                Function.identity(),
                (left, right) -> left,
                LinkedHashMap::new
        ));
    }

    private String resolveEntryNodeKey(AiWorkflowDefinitionEntity workflow, List<AiWorkflowNodeEntity> nodes) {
        String entryNodeKey = clean(workflow.getEntryNodeKey());
        if (StrUtil.isNotBlank(entryNodeKey)) {
            return entryNodeKey;
        }
        return nodes.getFirst().getNodeKey();
    }

    private Map<String, Object> buildVariables(AdminExecuteAiWorkflowDTO dto) {
        Map<String, Object> variables = new LinkedHashMap<>();
        if (dto.getVariables() != null) {
            variables.putAll(dto.getVariables());
        }
        if (StrUtil.isNotBlank(dto.getInput())) {
            variables.put("input", dto.getInput().trim());
        }
        return variables;
    }

    private AiWorkflowExecutionEntity buildRunningExecution(AiWorkflowDefinitionEntity workflow,
                                                            AdminExecuteAiWorkflowDTO dto,
                                                            Map<String, Object> variables,
                                                            String executionId,
                                                            String traceId,
                                                            LocalDateTime startedAt) {
        return new AiWorkflowExecutionEntity()
                .setExecutionId(executionId)
                .setWorkflowDefinitionId(workflow.getId())
                .setWorkflowCode(workflow.getWorkflowCode())
                .setWorkflowName(workflow.getName())
                .setVersionNo(workflow.getVersionNo())
                .setAgentId(workflow.getAgentId())
                .setUserId(currentUserId())
                .setTriggerSource(StrUtil.blankToDefault(clean(dto.getTriggerSource()), DEFAULT_TRIGGER_SOURCE))
                .setTriggerId(clean(dto.getTriggerId()))
                .setInputSummary(writeJson(variables))
                .setStatus(STATUS_RUNNING)
                .setTraceId(traceId)
                .setStartedAt(startedAt);
    }

    private ExecutionState executeNodes(AiWorkflowDefinitionEntity workflow,
                                        Map<String, AiWorkflowNodeEntity> nodeMap,
                                        String entryNodeKey,
                                        Map<String, Object> variables,
                                        String executionId,
                                        String traceId) {
        ExecutionState state = new ExecutionState()
                .setStatus(STATUS_FAILED)
                .setOutputVariables(new LinkedHashMap<>(variables))
                .setNodeResults(new ArrayList<>());
        String currentNodeKey = entryNodeKey;
        for (int i = 0; i < MAX_NODE_EXECUTION_COUNT; i++) {
            try {
                AiWorkflowNodeEntity node = nodeMap.get(currentNodeKey);
                if (node == null) {
                    return failState(state, currentNodeKey, AiErrorEnum.WORKFLOW_NODE_UNAVAILABLE);
                }
                state.setCurrentNodeKey(currentNodeKey);
                AiWorkflowNodeExecutionResult result = executeNode(workflow, node, variables, executionId, traceId);
                state.getNodeResults().add(nodeResult2BO(result));
                mergeVariables(variables, result.getOutputVariables());
                state.setOutputVariables(new LinkedHashMap<>(variables));
                if (Boolean.TRUE.equals(result.getTerminalNode())) {
                    return terminalState(state, currentNodeKey, result);
                }
                if (AiWorkflowNodeExecutionResult.STATUS_FAILED == defaultStatus(result.getStatus())) {
                    return failState(state, currentNodeKey, result.getErrorCode(), result.getErrorMessage());
                }

                String nextNodeKey = StrUtil.blankToDefault(trim(result.getNextNodeKey()), firstNextNodeKey(node));
                if (StrUtil.isBlank(nextNodeKey)) {
                    return failState(state, currentNodeKey, "WORKFLOW_NEXT_NODE_MISSING", "工作流节点缺少下游节点");
                }
                currentNodeKey = nextNodeKey;
            } catch (RuntimeException ex) {
                return failState(state, currentNodeKey, errorCode(ex), rootMessage(ex));
            }
        }
        return failState(state, currentNodeKey, "WORKFLOW_NODE_LIMIT_EXCEEDED", "工作流超过最大节点执行次数");
    }

    private AiWorkflowNodeExecutionResult executeNode(AiWorkflowDefinitionEntity workflow,
                                                      AiWorkflowNodeEntity node,
                                                      Map<String, Object> variables,
                                                      String executionId,
                                                      String traceId) {
        AiWorkflowNodeExecutor executor = nodeExecutors.stream()
                .filter(candidate -> candidate.supports(node.getNodeType()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(AiErrorEnum.WORKFLOW_NODE_EXECUTOR_UNAVAILABLE));
        AiWorkflowNodeExecutionContext context = AiWorkflowNodeExecutionContext.builder()
                .executionId(executionId)
                .workflowDefinitionId(workflow.getId())
                .workflowCode(workflow.getWorkflowCode())
                .traceId(traceId)
                .variables(new LinkedHashMap<>(variables))
                .build();
        AiWorkflowNodeExecutionResult result = executor.execute(node, context);
        if (result == null) {
            throw new BusinessException(AiErrorEnum.WORKFLOW_EXECUTION_FAILED);
        }
        return result;
    }

    private void updateExecution(AiWorkflowExecutionEntity execution, ExecutionState state) {
        AiWorkflowExecutionEntity update = new AiWorkflowExecutionEntity()
                .setStatus(state.getStatus())
                .setCurrentNodeKey(state.getCurrentNodeKey())
                .setFailedNodeKey(state.getFailedNodeKey())
                .setDurationMs(state.getDurationMs())
                .setErrorCode(truncate(state.getErrorCode(), ERROR_CODE_MAX_LENGTH))
                .setErrorMessage(truncate(state.getErrorMessage(), ERROR_MESSAGE_MAX_LENGTH))
                .setOutputSummary(writeJson(buildOutputSummary(state)))
                .setFinishedAt(state.getFinishedAt());
        update.setId(execution.getId());
        aiWorkflowExecutionMapper.updateById(update);
    }

    private void refreshExecutionStats(AiWorkflowDefinitionEntity workflow, LocalDateTime finishedAt) {
        AiWorkflowDefinitionEntity update = new AiWorkflowDefinitionEntity()
                .setLastExecutedAt(finishedAt)
                .setExecutionCount(defaultIfNull(workflow.getExecutionCount()) + 1);
        update.setId(workflow.getId());
        aiWorkflowDefinitionMapper.updateById(update);

        if (workflow.getAgentId() == null) {
            return;
        }
        AiAgentEntity agent = aiAgentMapper.selectById(workflow.getAgentId());
        if (agent == null) {
            return;
        }
        AiAgentEntity agentUpdate = new AiAgentEntity()
                .setLastExecutedAt(finishedAt)
                .setExecutionCount(defaultIfNull(agent.getExecutionCount()) + 1);
        agentUpdate.setId(agent.getId());
        aiAgentMapper.updateById(agentUpdate);
    }

    private AdminAiWorkflowExecutionResultBO buildExecutionResultBO(AiWorkflowExecutionEntity execution,
                                                                    AiWorkflowDefinitionEntity workflow,
                                                                    AdminExecuteAiWorkflowDTO dto,
                                                                    ExecutionState state,
                                                                    LocalDateTime startedAt) {
        return AdminAiWorkflowExecutionResultBO.builder()
                .id(execution.getId())
                .executionId(execution.getExecutionId())
                .workflowDefinitionId(workflow.getId())
                .workflowCode(workflow.getWorkflowCode())
                .workflowName(workflow.getName())
                .versionNo(workflow.getVersionNo())
                .agentId(workflow.getAgentId())
                .triggerSource(StrUtil.blankToDefault(clean(dto.getTriggerSource()), DEFAULT_TRIGGER_SOURCE))
                .triggerId(clean(dto.getTriggerId()))
                .status(state.getStatus())
                .outputVariables(state.getOutputVariables())
                .finalOutput(state.getFinalOutput())
                .currentNodeKey(state.getCurrentNodeKey())
                .failedNodeKey(state.getFailedNodeKey())
                .errorCode(state.getErrorCode())
                .errorMessage(state.getErrorMessage())
                .durationMs(state.getDurationMs())
                .traceId(execution.getTraceId())
                .nodeResults(state.getNodeResults())
                .startedAt(startedAt)
                .finishedAt(state.getFinishedAt())
                .build();
    }

    private Map<String, Object> buildOutputSummary(ExecutionState state) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("variables", state.getOutputVariables());
        summary.put("finalOutput", state.getFinalOutput());
        summary.put("nodeResults", state.getNodeResults());
        summary.put("errorCode", state.getErrorCode());
        summary.put("errorMessage", state.getErrorMessage());
        return summary;
    }

    private ExecutionState terminalState(ExecutionState state,
                                         String currentNodeKey,
                                         AiWorkflowNodeExecutionResult result) {
        boolean workflowSuccess = !Boolean.FALSE.equals(result.getWorkflowSuccess());
        return state
                .setStatus(workflowSuccess ? STATUS_SUCCESS : STATUS_FAILED)
                .setCurrentNodeKey(currentNodeKey)
                .setFailedNodeKey(workflowSuccess ? null : currentNodeKey)
                .setFinalOutput(result.getFinalOutput())
                .setErrorCode(workflowSuccess ? null : result.getErrorCode())
                .setErrorMessage(workflowSuccess ? null : result.getErrorMessage());
    }

    private ExecutionState failState(ExecutionState state, String nodeKey, AiErrorEnum errorEnum) {
        return failState(state, nodeKey, errorEnum.name(), errorEnum.getLabel());
    }

    private ExecutionState failState(ExecutionState state, String nodeKey, String errorCode, String errorMessage) {
        return state
                .setStatus(STATUS_FAILED)
                .setCurrentNodeKey(nodeKey)
                .setFailedNodeKey(nodeKey)
                .setErrorCode(errorCode)
                .setErrorMessage(errorMessage);
    }

    private AdminAiWorkflowNodeExecutionResultBO nodeResult2BO(AiWorkflowNodeExecutionResult result) {
        AdminAiWorkflowNodeExecutionResultBO bo = new AdminAiWorkflowNodeExecutionResultBO();
        BeanUtil.copyProperties(result, bo);
        return bo;
    }

    private void mergeVariables(Map<String, Object> variables, Map<String, Object> outputVariables) {
        if (outputVariables == null) {
            return;
        }
        variables.putAll(outputVariables);
    }

    private String firstNextNodeKey(AiWorkflowNodeEntity node) {
        List<String> nextNodeKeys = parseNextNodeKeys(node.getNextNodeKeys());
        if (nextNodeKeys.isEmpty()) {
            return null;
        }
        return nextNodeKeys.getFirst();
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
            throw new BusinessException(400, "工作流节点下游节点JSON格式错误");
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
                .toList();
    }

    private PageResult<AdminAiWorkflowExecutionBO> entityPage2BOPage(Page<AiWorkflowExecutionEntity> entityPage) {
        return new PageResult<AdminAiWorkflowExecutionBO>()
                .setCurrent(entityPage.getCurrent())
                .setSize(entityPage.getSize())
                .setTotal(entityPage.getTotal())
                .setRecords(entityList2BOs(entityPage.getRecords()));
    }

    private List<AdminAiWorkflowExecutionBO> entityList2BOs(List<AiWorkflowExecutionEntity> entityList) {
        if (CollUtil.isEmpty(entityList)) {
            return Collections.emptyList();
        }
        return entityList.stream().map(this::entity2BO).toList();
    }

    private AdminAiWorkflowExecutionBO entity2BO(AiWorkflowExecutionEntity entity) {
        if (entity == null) {
            return null;
        }
        AdminAiWorkflowExecutionBO bo = new AdminAiWorkflowExecutionBO();
        BeanUtil.copyProperties(entity, bo);
        return bo;
    }

    private AdminAiWorkflowExecutionDetailBO entity2DetailBO(AiWorkflowExecutionEntity entity) {
        if (entity == null) {
            return null;
        }
        AdminAiWorkflowExecutionDetailBO bo = new AdminAiWorkflowExecutionDetailBO();
        BeanUtil.copyProperties(entity, bo);
        return bo;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return StrUtil.toString(value);
        }
    }

    private int defaultStatus(Integer status) {
        return status == null ? AiWorkflowNodeExecutionResult.STATUS_FAILED : status;
    }

    private int defaultIfNull(Integer value) {
        return value == null ? 0 : value;
    }

    private Long currentUserId() {
        Long userId = UserContextHolder.getUserId();
        return userId == null ? 0L : userId;
    }

    private String errorCode(Throwable ex) {
        if (ex instanceof BusinessException businessException) {
            if (businessException.getCustomEnumField() != null) {
                return businessException.getCustomEnumField().name();
            }
            return String.valueOf(businessException.getCode());
        }
        return ex == null ? null : ex.getClass().getSimpleName();
    }

    private String rootMessage(Throwable e) {
        if (e == null) {
            return "工作流执行失败";
        }
        Throwable root = e;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return StrUtil.blankToDefault(root.getMessage(), "工作流执行失败");
    }

    private String clean(String value) {
        return CharSequenceUtil.cleanBlank(value);
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    @Data
    @Accessors(chain = true)
    private static class ExecutionState {
        private Integer status;
        private Map<String, Object> outputVariables;
        private String finalOutput;
        private String currentNodeKey;
        private String failedNodeKey;
        private String errorCode;
        private String errorMessage;
        private Long durationMs;
        private LocalDateTime finishedAt;
        private List<AdminAiWorkflowNodeExecutionResultBO> nodeResults;
    }
}
