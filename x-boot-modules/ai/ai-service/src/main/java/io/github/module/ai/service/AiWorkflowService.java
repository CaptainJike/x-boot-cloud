package io.github.module.ai.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.enums.EnabledStatusEnum;
import io.github.framework.core.exception.BusinessException;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.module.ai.entity.AiAgentEntity;
import io.github.module.ai.entity.AiWorkflowDefinitionEntity;
import io.github.module.ai.entity.AiWorkflowNodeEntity;
import io.github.module.ai.enums.AiErrorEnum;
import io.github.module.ai.mapper.AiAgentMapper;
import io.github.module.ai.mapper.AiWorkflowDefinitionMapper;
import io.github.module.ai.mapper.AiWorkflowNodeMapper;
import io.github.module.ai.model.request.AdminInsertOrUpdateAiWorkflowDTO;
import io.github.module.ai.model.request.AdminInsertOrUpdateAiWorkflowNodeDTO;
import io.github.module.ai.model.request.AdminListAiWorkflowDTO;
import io.github.module.ai.model.request.AdminListAiWorkflowNodeDTO;
import io.github.module.ai.model.response.AiWorkflowBO;
import io.github.module.ai.model.response.AiWorkflowDetailBO;
import io.github.module.ai.model.response.AiWorkflowNodeBO;
import io.github.module.ai.model.response.AiWorkflowNodeDetailBO;
import io.github.module.ai.service.workflow.AiWorkflowConditionNodeExecutor;
import io.github.module.ai.service.workflow.AiWorkflowEndNodeExecutor;
import io.github.module.ai.service.workflow.AiWorkflowHttpToolNodeExecutor;
import io.github.module.ai.service.workflow.AiWorkflowLlmNodeExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * AI 工作流.
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class AiWorkflowService {

    private static final int DEFAULT_WORKFLOW_VERSION = 1;

    private static final int DEFAULT_PUBLISH_STATUS_DRAFT = 0;

    private static final Set<String> SUPPORTED_NODE_TYPES = Set.of(
            AiWorkflowLlmNodeExecutor.NODE_TYPE,
            AiWorkflowHttpToolNodeExecutor.NODE_TYPE,
            AiWorkflowHttpToolNodeExecutor.NODE_TYPE_HTTP_TOOL,
            AiWorkflowConditionNodeExecutor.NODE_TYPE,
            AiWorkflowEndNodeExecutor.NODE_TYPE,
            AiWorkflowEndNodeExecutor.NODE_TYPE_END_NODE
    );

    private final AiWorkflowDefinitionMapper aiWorkflowDefinitionMapper;

    private final AiWorkflowNodeMapper aiWorkflowNodeMapper;

    private final AiAgentMapper aiAgentMapper;

    /**
     * 后台管理-工作流分页列表.
     */
    public PageResult<AiWorkflowBO> adminList(PageParam pageParam, AdminListAiWorkflowDTO dto) {
        Page<AiWorkflowDefinitionEntity> entityPage = aiWorkflowDefinitionMapper.selectPage(
                new Page<>(pageParam.getPageNum(), pageParam.getPageSize()),
                new QueryWrapper<AiWorkflowDefinitionEntity>()
                        .lambda()
                        .like(CharSequenceUtil.isNotBlank(dto.getWorkflowCode()),
                                AiWorkflowDefinitionEntity::getWorkflowCode,
                                clean(dto.getWorkflowCode()))
                        .like(CharSequenceUtil.isNotBlank(dto.getName()),
                                AiWorkflowDefinitionEntity::getName,
                                clean(dto.getName()))
                        .eq(dto.getAgentId() != null, AiWorkflowDefinitionEntity::getAgentId, dto.getAgentId())
                        .eq(dto.getStatus() != null, AiWorkflowDefinitionEntity::getStatus, dto.getStatus())
                        .eq(dto.getPublishStatus() != null,
                                AiWorkflowDefinitionEntity::getPublishStatus,
                                dto.getPublishStatus())
                        .orderByDesc(AiWorkflowDefinitionEntity::getUpdatedAt)
                        .orderByDesc(AiWorkflowDefinitionEntity::getCreatedAt)
        );

        return this.entityPage2BOPage(entityPage);
    }

    /**
     * 后台管理-启用工作流下拉框.
     */
    public List<AiWorkflowBO> adminSelectOptions() {
        List<AiWorkflowDefinitionEntity> entityList = aiWorkflowDefinitionMapper.selectList(
                new QueryWrapper<AiWorkflowDefinitionEntity>()
                        .lambda()
                        .eq(AiWorkflowDefinitionEntity::getStatus, EnabledStatusEnum.ENABLED.getValue())
                        .orderByAsc(AiWorkflowDefinitionEntity::getName)
                        .orderByAsc(AiWorkflowDefinitionEntity::getWorkflowCode)
                        .orderByDesc(AiWorkflowDefinitionEntity::getUpdatedAt)
        );

        return this.entityList2BOs(entityList);
    }

    /**
     * 根据 ID 取工作流详情.
     */
    public AiWorkflowDetailBO getOneById(Long id) {
        return this.getOneById(id, false);
    }

    /**
     * 根据 ID 取工作流详情.
     */
    public AiWorkflowDetailBO getOneById(Long id, boolean throwIfInvalidId) throws BusinessException {
        AiWorkflowDefinitionEntity entity = aiWorkflowDefinitionMapper.selectById(id);
        if (throwIfInvalidId) {
            AiErrorEnum.INVALID_ID.assertNotNull(entity);
        }

        return this.entity2DetailBO(entity);
    }

    /**
     * 后台管理-新增工作流.
     */
    @Transactional(rollbackFor = Exception.class)
    public Long adminInsert(AdminInsertOrUpdateAiWorkflowDTO dto) {
        log.info("[后台管理-新增AI工作流] >> workflowCode={}, name={}", dto.getWorkflowCode(), dto.getName());
        this.checkWorkflowStatus(dto.getStatus());
        this.checkAgent(dto.getAgentId());
        this.checkWorkflowExistence(dto);

        dto.setId(null);
        AiWorkflowDefinitionEntity entity = new AiWorkflowDefinitionEntity();
        BeanUtil.copyProperties(dto, entity);
        normalizeWorkflow(entity);
        entity.setPublishStatus(DEFAULT_PUBLISH_STATUS_DRAFT);
        entity.setPublishedSnapshot(StrUtil.EMPTY);
        entity.setExecutionCount(0);
        aiWorkflowDefinitionMapper.insert(entity);

        return entity.getId();
    }

    /**
     * 后台管理-编辑工作流.
     */
    @Transactional(rollbackFor = Exception.class)
    public void adminUpdate(AdminInsertOrUpdateAiWorkflowDTO dto) {
        log.info("[后台管理-编辑AI工作流] >> id={}, workflowCode={}, name={}",
                dto.getId(),
                dto.getWorkflowCode(),
                dto.getName());
        this.checkWorkflowStatus(dto.getStatus());
        this.checkAgent(dto.getAgentId());
        AiWorkflowDefinitionEntity existingEntity = aiWorkflowDefinitionMapper.selectById(dto.getId());
        AiErrorEnum.INVALID_ID.assertNotNull(existingEntity);
        this.checkWorkflowExistence(dto);

        AiWorkflowDefinitionEntity entity = new AiWorkflowDefinitionEntity();
        BeanUtil.copyProperties(dto, entity);
        normalizeWorkflow(entity);
        aiWorkflowDefinitionMapper.updateById(entity);
    }

    /**
     * 后台管理-删除工作流.
     */
    @Transactional(rollbackFor = Exception.class)
    public void adminDelete(Collection<Long> ids) {
        log.info("[后台管理-删除AI工作流] >> 入参={}", ids);
        aiWorkflowDefinitionMapper.deleteBatchIds(ids);
        if (CollUtil.isNotEmpty(ids)) {
            aiWorkflowNodeMapper.delete(
                    new QueryWrapper<AiWorkflowNodeEntity>()
                            .lambda()
                            .in(AiWorkflowNodeEntity::getWorkflowDefinitionId, ids)
            );
        }
    }

    /**
     * 后台管理-更新工作流启停状态.
     */
    @Transactional(rollbackFor = Exception.class)
    public void adminUpdateStatus(Long id, Integer status) {
        log.info("[后台管理-更新AI工作流启停状态] >> id={}, status={}", id, status);
        this.checkWorkflowStatus(status);
        AiWorkflowDefinitionEntity existingEntity = aiWorkflowDefinitionMapper.selectById(id);
        AiErrorEnum.INVALID_ID.assertNotNull(existingEntity);

        AiWorkflowDefinitionEntity entity = new AiWorkflowDefinitionEntity()
                .setStatus(status);
        entity.setId(id);
        aiWorkflowDefinitionMapper.updateById(entity);
    }

    /**
     * 后台管理-工作流节点列表.
     */
    public List<AiWorkflowNodeBO> adminListNodes(AdminListAiWorkflowNodeDTO dto) {
        AiErrorEnum.INVALID_ID.assertNotNull(aiWorkflowDefinitionMapper.selectById(dto.getWorkflowDefinitionId()));
        List<AiWorkflowNodeEntity> entityList = aiWorkflowNodeMapper.selectList(
                new QueryWrapper<AiWorkflowNodeEntity>()
                        .lambda()
                        .eq(AiWorkflowNodeEntity::getWorkflowDefinitionId, dto.getWorkflowDefinitionId())
                        .like(CharSequenceUtil.isNotBlank(dto.getNodeKey()),
                                AiWorkflowNodeEntity::getNodeKey,
                                clean(dto.getNodeKey()))
                        .like(CharSequenceUtil.isNotBlank(dto.getNodeName()),
                                AiWorkflowNodeEntity::getNodeName,
                                clean(dto.getNodeName()))
                        .eq(CharSequenceUtil.isNotBlank(dto.getNodeType()),
                                AiWorkflowNodeEntity::getNodeType,
                                normalizeNodeType(dto.getNodeType()))
                        .eq(dto.getStatus() != null, AiWorkflowNodeEntity::getStatus, dto.getStatus())
                        .orderByAsc(AiWorkflowNodeEntity::getSortOrder)
                        .orderByAsc(AiWorkflowNodeEntity::getId)
        );

        return this.nodeEntityList2BOs(entityList);
    }

    /**
     * 根据 ID 取工作流节点详情.
     */
    public AiWorkflowNodeDetailBO getNodeById(Long id) {
        return this.getNodeById(id, false);
    }

    /**
     * 根据 ID 取工作流节点详情.
     */
    public AiWorkflowNodeDetailBO getNodeById(Long id, boolean throwIfInvalidId) throws BusinessException {
        AiWorkflowNodeEntity entity = aiWorkflowNodeMapper.selectById(id);
        if (throwIfInvalidId) {
            AiErrorEnum.INVALID_ID.assertNotNull(entity);
        }

        return this.nodeEntity2DetailBO(entity);
    }

    /**
     * 后台管理-新增工作流节点.
     */
    @Transactional(rollbackFor = Exception.class)
    public Long adminInsertNode(AdminInsertOrUpdateAiWorkflowNodeDTO dto) {
        log.info("[后台管理-新增AI工作流节点] >> workflowDefinitionId={}, nodeKey={}",
                dto.getWorkflowDefinitionId(),
                dto.getNodeKey());
        this.checkNodeStatus(dto.getStatus());
        AiWorkflowDefinitionEntity workflow = this.getWorkflowEntity(dto.getWorkflowDefinitionId());
        this.checkNodeType(dto.getNodeType(), dto.getNextNodeKeys());
        this.checkNodeExistence(dto);

        dto.setId(null);
        AiWorkflowNodeEntity entity = new AiWorkflowNodeEntity();
        BeanUtil.copyProperties(dto, entity);
        normalizeNode(entity);
        syncWorkflowSnapshot(entity, workflow);
        aiWorkflowNodeMapper.insert(entity);

        return entity.getId();
    }

    /**
     * 后台管理-编辑工作流节点.
     */
    @Transactional(rollbackFor = Exception.class)
    public void adminUpdateNode(AdminInsertOrUpdateAiWorkflowNodeDTO dto) {
        log.info("[后台管理-编辑AI工作流节点] >> id={}, workflowDefinitionId={}, nodeKey={}",
                dto.getId(),
                dto.getWorkflowDefinitionId(),
                dto.getNodeKey());
        this.checkNodeStatus(dto.getStatus());
        AiWorkflowNodeEntity existingEntity = aiWorkflowNodeMapper.selectById(dto.getId());
        AiErrorEnum.INVALID_ID.assertNotNull(existingEntity);
        AiWorkflowDefinitionEntity workflow = this.getWorkflowEntity(dto.getWorkflowDefinitionId());
        this.checkNodeType(dto.getNodeType(), dto.getNextNodeKeys());
        this.checkNodeExistence(dto);

        AiWorkflowNodeEntity entity = new AiWorkflowNodeEntity();
        BeanUtil.copyProperties(dto, entity);
        normalizeNode(entity);
        syncWorkflowSnapshot(entity, workflow);
        aiWorkflowNodeMapper.updateById(entity);
    }

    /**
     * 后台管理-删除工作流节点.
     */
    @Transactional(rollbackFor = Exception.class)
    public void adminDeleteNodes(Collection<Long> ids) {
        log.info("[后台管理-删除AI工作流节点] >> 入参={}", ids);
        aiWorkflowNodeMapper.deleteBatchIds(ids);
    }

    /*
    ----------------------------------------------------------------
                        私有方法 private methods
    ----------------------------------------------------------------
     */

    private AiWorkflowBO entity2BO(AiWorkflowDefinitionEntity entity) {
        if (entity == null) {
            return null;
        }

        AiWorkflowBO bo = new AiWorkflowBO();
        BeanUtil.copyProperties(entity, bo);

        return bo;
    }

    private AiWorkflowDetailBO entity2DetailBO(AiWorkflowDefinitionEntity entity) {
        if (entity == null) {
            return null;
        }

        AiWorkflowDetailBO bo = new AiWorkflowDetailBO();
        BeanUtil.copyProperties(entity, bo);

        return bo;
    }

    private AiWorkflowNodeBO nodeEntity2BO(AiWorkflowNodeEntity entity) {
        if (entity == null) {
            return null;
        }

        AiWorkflowNodeBO bo = new AiWorkflowNodeBO();
        BeanUtil.copyProperties(entity, bo);

        return bo;
    }

    private AiWorkflowNodeDetailBO nodeEntity2DetailBO(AiWorkflowNodeEntity entity) {
        if (entity == null) {
            return null;
        }

        AiWorkflowNodeDetailBO bo = new AiWorkflowNodeDetailBO();
        BeanUtil.copyProperties(entity, bo);

        return bo;
    }

    private List<AiWorkflowBO> entityList2BOs(List<AiWorkflowDefinitionEntity> entityList) {
        if (CollUtil.isEmpty(entityList)) {
            return Collections.emptyList();
        }

        List<AiWorkflowBO> ret = new ArrayList<>(entityList.size());
        entityList.forEach(entity -> ret.add(this.entity2BO(entity)));

        return ret;
    }

    private List<AiWorkflowNodeBO> nodeEntityList2BOs(List<AiWorkflowNodeEntity> entityList) {
        if (CollUtil.isEmpty(entityList)) {
            return Collections.emptyList();
        }

        List<AiWorkflowNodeBO> ret = new ArrayList<>(entityList.size());
        entityList.forEach(entity -> ret.add(this.nodeEntity2BO(entity)));

        return ret;
    }

    private PageResult<AiWorkflowBO> entityPage2BOPage(Page<AiWorkflowDefinitionEntity> entityPage) {
        return new PageResult<AiWorkflowBO>()
                .setCurrent(entityPage.getCurrent())
                .setSize(entityPage.getSize())
                .setTotal(entityPage.getTotal())
                .setRecords(this.entityList2BOs(entityPage.getRecords()));
    }

    private void checkWorkflowExistence(AdminInsertOrUpdateAiWorkflowDTO dto) {
        AiWorkflowDefinitionEntity existingEntity = aiWorkflowDefinitionMapper.selectOne(
                new QueryWrapper<AiWorkflowDefinitionEntity>()
                        .lambda()
                        .select(AiWorkflowDefinitionEntity::getId)
                        .eq(AiWorkflowDefinitionEntity::getWorkflowCode, clean(dto.getWorkflowCode()))
                        .eq(AiWorkflowDefinitionEntity::getVersionNo,
                                defaultIfNull(dto.getVersionNo(), DEFAULT_WORKFLOW_VERSION))
                        .last(BaseConstant.CRUD.SQL_LIMIT_1)
        );

        if (existingEntity != null && !existingEntity.getId().equals(dto.getId())) {
            throw new BusinessException(AiErrorEnum.DUPLICATE_WORKFLOW);
        }
    }

    private void checkNodeExistence(AdminInsertOrUpdateAiWorkflowNodeDTO dto) {
        AiWorkflowNodeEntity existingEntity = aiWorkflowNodeMapper.selectOne(
                new QueryWrapper<AiWorkflowNodeEntity>()
                        .lambda()
                        .select(AiWorkflowNodeEntity::getId)
                        .eq(AiWorkflowNodeEntity::getWorkflowDefinitionId, dto.getWorkflowDefinitionId())
                        .eq(AiWorkflowNodeEntity::getNodeKey, clean(dto.getNodeKey()))
                        .last(BaseConstant.CRUD.SQL_LIMIT_1)
        );

        if (existingEntity != null && !existingEntity.getId().equals(dto.getId())) {
            throw new BusinessException(AiErrorEnum.DUPLICATE_WORKFLOW_NODE);
        }
    }

    private void checkAgent(Long agentId) {
        if (agentId == null) {
            return;
        }

        AiAgentEntity agent = aiAgentMapper.selectById(agentId);
        AiErrorEnum.INVALID_ID.assertNotNull(agent);
    }

    private AiWorkflowDefinitionEntity getWorkflowEntity(Long workflowDefinitionId) {
        AiWorkflowDefinitionEntity workflow = aiWorkflowDefinitionMapper.selectById(workflowDefinitionId);
        AiErrorEnum.INVALID_ID.assertNotNull(workflow);
        return workflow;
    }

    private void normalizeWorkflow(AiWorkflowDefinitionEntity entity) {
        entity.setWorkflowCode(clean(entity.getWorkflowCode()));
        entity.setName(clean(entity.getName()));
        entity.setDescription(trimToEmpty(entity.getDescription()));
        entity.setVersionNo(defaultIfNull(entity.getVersionNo(), DEFAULT_WORKFLOW_VERSION));
        entity.setEntryNodeKey(clean(entity.getEntryNodeKey()));
        entity.setDefinitionSnapshot(trimToEmpty(entity.getDefinitionSnapshot()));
    }

    private void normalizeNode(AiWorkflowNodeEntity entity) {
        entity.setNodeKey(clean(entity.getNodeKey()));
        entity.setNodeName(clean(entity.getNodeName()));
        entity.setNodeType(normalizeNodeType(entity.getNodeType()));
        entity.setDescription(trimToEmpty(entity.getDescription()));
        entity.setNodeConfig(trimToEmpty(entity.getNodeConfig()));
        entity.setInputMapping(trimToEmpty(entity.getInputMapping()));
        entity.setOutputMapping(trimToEmpty(entity.getOutputMapping()));
        entity.setNextNodeKeys(trimToEmpty(entity.getNextNodeKeys()));
        entity.setConditionExpression(trimToEmpty(entity.getConditionExpression()));
        entity.setErrorStrategy(trimToEmpty(entity.getErrorStrategy()));
        entity.setRetryCount(defaultIfNull(entity.getRetryCount(), 0));
        entity.setSortOrder(defaultIfNull(entity.getSortOrder(), 0));
    }

    private void syncWorkflowSnapshot(AiWorkflowNodeEntity entity, AiWorkflowDefinitionEntity workflow) {
        entity.setWorkflowDefinitionId(workflow.getId());
        entity.setWorkflowCode(workflow.getWorkflowCode());
        entity.setVersionNo(workflow.getVersionNo());
    }

    private void checkWorkflowStatus(Integer status) {
        if (EnabledStatusEnum.of(status) == null) {
            throw new BusinessException(AiErrorEnum.INVALID_WORKFLOW_STATUS);
        }
    }

    private void checkNodeStatus(Integer status) {
        if (EnabledStatusEnum.of(status) == null) {
            throw new BusinessException(AiErrorEnum.INVALID_WORKFLOW_NODE_STATUS);
        }
    }

    private void checkNodeType(String nodeType, String nextNodeKeys) {
        String cleanNodeType = normalizeNodeType(nodeType);
        if (!SUPPORTED_NODE_TYPES.contains(cleanNodeType)) {
            throw new BusinessException(AiErrorEnum.INVALID_WORKFLOW_NODE_TYPE);
        }
        if ((AiWorkflowEndNodeExecutor.NODE_TYPE.equals(cleanNodeType)
                || AiWorkflowEndNodeExecutor.NODE_TYPE_END_NODE.equals(cleanNodeType))
                && StrUtil.isNotBlank(nextNodeKeys)) {
            throw new BusinessException(AiErrorEnum.INVALID_WORKFLOW_NODE_TYPE);
        }
    }

    private String normalizeNodeType(String nodeType) {
        return StrUtil.nullToEmpty(clean(nodeType)).toLowerCase(Locale.ROOT);
    }

    private Integer defaultIfNull(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }

    private String clean(String value) {
        return CharSequenceUtil.cleanBlank(value);
    }

    private String trimToEmpty(String value) {
        return StrUtil.blankToDefault(trim(value), StrUtil.EMPTY);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
