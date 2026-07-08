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
import io.github.module.ai.enums.AiErrorEnum;
import io.github.module.ai.mapper.AiAgentMapper;
import io.github.module.ai.model.request.AdminInsertOrUpdateAiAgentDTO;
import io.github.module.ai.model.request.AdminListAiAgentDTO;
import io.github.module.ai.model.response.AiAgentBO;
import io.github.module.ai.model.response.AiAgentDetailBO;
import io.github.module.ai.model.response.AiModelConfigBO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * AI Agent.
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class AiAgentService {

    private static final int DEFAULT_PUBLISH_STATUS_DRAFT = 0;

    private final AiAgentMapper aiAgentMapper;

    private final AiModelConfigService aiModelConfigService;

    /**
     * 后台管理-Agent 分页列表.
     */
    public PageResult<AiAgentBO> adminList(PageParam pageParam, AdminListAiAgentDTO dto) {
        Page<AiAgentEntity> entityPage = aiAgentMapper.selectPage(
                new Page<>(pageParam.getPageNum(), pageParam.getPageSize()),
                new QueryWrapper<AiAgentEntity>()
                        .lambda()
                        .like(CharSequenceUtil.isNotBlank(dto.getAgentCode()),
                                AiAgentEntity::getAgentCode,
                                clean(dto.getAgentCode()))
                        .like(CharSequenceUtil.isNotBlank(dto.getName()), AiAgentEntity::getName, clean(dto.getName()))
                        .eq(CharSequenceUtil.isNotBlank(dto.getModelConfigCode()),
                                AiAgentEntity::getModelConfigCode,
                                clean(dto.getModelConfigCode()))
                        .eq(dto.getStatus() != null, AiAgentEntity::getStatus, dto.getStatus())
                        .eq(dto.getPublishStatus() != null, AiAgentEntity::getPublishStatus, dto.getPublishStatus())
                        .orderByDesc(AiAgentEntity::getUpdatedAt)
                        .orderByDesc(AiAgentEntity::getCreatedAt)
        );

        return this.entityPage2BOPage(entityPage);
    }

    /**
     * 后台管理-启用 Agent 下拉框.
     */
    public List<AiAgentBO> adminSelectOptions() {
        List<AiAgentEntity> entityList = aiAgentMapper.selectList(
                new QueryWrapper<AiAgentEntity>()
                        .lambda()
                        .eq(AiAgentEntity::getStatus, EnabledStatusEnum.ENABLED.getValue())
                        .orderByAsc(AiAgentEntity::getName)
        );

        return this.entityList2BOs(entityList);
    }

    /**
     * 根据 ID 取 Agent 详情.
     */
    public AiAgentDetailBO getOneById(Long id) {
        return this.getOneById(id, false);
    }

    /**
     * 根据 ID 取 Agent 详情.
     */
    public AiAgentDetailBO getOneById(Long id, boolean throwIfInvalidId) throws BusinessException {
        AiAgentEntity entity = aiAgentMapper.selectById(id);
        if (throwIfInvalidId) {
            AiErrorEnum.INVALID_ID.assertNotNull(entity);
        }

        return this.entity2DetailBO(entity);
    }

    /**
     * 后台管理-新增 Agent.
     */
    @Transactional(rollbackFor = Exception.class)
    public Long adminInsert(AdminInsertOrUpdateAiAgentDTO dto) {
        log.info("[后台管理-新增AI Agent] >> agentCode={}, name={}", dto.getAgentCode(), dto.getName());
        this.checkStatus(dto.getStatus());
        this.checkExistence(dto);

        dto.setId(null);
        AiAgentEntity entity = new AiAgentEntity();
        BeanUtil.copyProperties(dto, entity);
        normalize(entity);
        resolveModelSnapshot(entity);
        entity.setPublishStatus(DEFAULT_PUBLISH_STATUS_DRAFT);
        entity.setExecutionCount(0);
        aiAgentMapper.insert(entity);

        return entity.getId();
    }

    /**
     * 后台管理-编辑 Agent.
     */
    @Transactional(rollbackFor = Exception.class)
    public void adminUpdate(AdminInsertOrUpdateAiAgentDTO dto) {
        log.info("[后台管理-编辑AI Agent] >> id={}, agentCode={}, name={}", dto.getId(), dto.getAgentCode(), dto.getName());
        this.checkStatus(dto.getStatus());
        AiAgentEntity existingEntity = aiAgentMapper.selectById(dto.getId());
        AiErrorEnum.INVALID_ID.assertNotNull(existingEntity);
        this.checkExistence(dto);

        AiAgentEntity entity = new AiAgentEntity();
        BeanUtil.copyProperties(dto, entity);
        normalize(entity);
        resolveModelSnapshot(entity);
        aiAgentMapper.updateById(entity);
    }

    /**
     * 后台管理-删除 Agent.
     */
    @Transactional(rollbackFor = Exception.class)
    public void adminDelete(Collection<Long> ids) {
        log.info("[后台管理-删除AI Agent] >> 入参={}", ids);
        aiAgentMapper.deleteBatchIds(ids);
    }

    /**
     * 后台管理-更新 Agent 启停状态.
     */
    @Transactional(rollbackFor = Exception.class)
    public void adminUpdateStatus(Long id, Integer status) {
        log.info("[后台管理-更新AI Agent启停状态] >> id={}, status={}", id, status);
        this.checkStatus(status);
        AiAgentEntity existingEntity = aiAgentMapper.selectById(id);
        AiErrorEnum.INVALID_ID.assertNotNull(existingEntity);

        AiAgentEntity entity = new AiAgentEntity()
                .setStatus(status);
        entity.setId(id);
        aiAgentMapper.updateById(entity);
    }

    /*
    ----------------------------------------------------------------
                        私有方法 private methods
    ----------------------------------------------------------------
     */

    private AiAgentBO entity2BO(AiAgentEntity entity) {
        if (entity == null) {
            return null;
        }

        AiAgentBO bo = new AiAgentBO();
        BeanUtil.copyProperties(entity, bo);

        return bo;
    }

    private AiAgentDetailBO entity2DetailBO(AiAgentEntity entity) {
        if (entity == null) {
            return null;
        }

        AiAgentDetailBO bo = new AiAgentDetailBO();
        BeanUtil.copyProperties(entity, bo);

        return bo;
    }

    private List<AiAgentBO> entityList2BOs(List<AiAgentEntity> entityList) {
        if (CollUtil.isEmpty(entityList)) {
            return Collections.emptyList();
        }

        List<AiAgentBO> ret = new ArrayList<>(entityList.size());
        entityList.forEach(entity -> ret.add(this.entity2BO(entity)));

        return ret;
    }

    private PageResult<AiAgentBO> entityPage2BOPage(Page<AiAgentEntity> entityPage) {
        return new PageResult<AiAgentBO>()
                .setCurrent(entityPage.getCurrent())
                .setSize(entityPage.getSize())
                .setTotal(entityPage.getTotal())
                .setRecords(this.entityList2BOs(entityPage.getRecords()));
    }

    private void checkExistence(AdminInsertOrUpdateAiAgentDTO dto) {
        AiAgentEntity existingEntity = aiAgentMapper.selectOne(
                new QueryWrapper<AiAgentEntity>()
                        .lambda()
                        .select(AiAgentEntity::getId)
                        .eq(AiAgentEntity::getAgentCode, clean(dto.getAgentCode()))
                        .last(BaseConstant.CRUD.SQL_LIMIT_1)
        );

        if (existingEntity != null && !existingEntity.getId().equals(dto.getId())) {
            throw new BusinessException(AiErrorEnum.DUPLICATE_AGENT);
        }
    }

    private void normalize(AiAgentEntity entity) {
        entity.setAgentCode(clean(entity.getAgentCode()));
        entity.setName(clean(entity.getName()));
        entity.setDescription(trimToEmpty(entity.getDescription()));
        entity.setAvatar(trimToEmpty(entity.getAvatar()));
        entity.setSystemPrompt(trimToEmpty(entity.getSystemPrompt()));
        entity.setModelConfigCode(clean(entity.getModelConfigCode()));
        entity.setKnowledgeBaseIds(trimToEmpty(entity.getKnowledgeBaseIds()));
        entity.setExecutionConfig(trimToEmpty(entity.getExecutionConfig()));
    }

    private void resolveModelSnapshot(AiAgentEntity entity) {
        if (StrUtil.isBlank(entity.getModelConfigCode())) {
            entity.setModelConfigId(null);
            entity.setProviderType(null);
            entity.setModelName(null);
            return;
        }

        AiModelConfigBO modelConfig = aiModelConfigService.getEnabledConfigByCode(entity.getModelConfigCode(), true);
        entity.setModelConfigId(modelConfig.getId());
        entity.setModelConfigCode(modelConfig.getCode());
        entity.setProviderType(modelConfig.getProviderType());
        entity.setModelName(modelConfig.getModelName());
    }

    private void checkStatus(Integer status) {
        if (EnabledStatusEnum.of(status) == null) {
            throw new BusinessException(AiErrorEnum.INVALID_AGENT_STATUS);
        }
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
