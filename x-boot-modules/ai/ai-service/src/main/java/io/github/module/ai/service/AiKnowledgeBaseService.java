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
import io.github.module.ai.constant.AiModelCapabilityConstant;
import io.github.module.ai.entity.AiKnowledgeBaseEntity;
import io.github.module.ai.enums.AiErrorEnum;
import io.github.module.ai.mapper.AiKnowledgeBaseMapper;
import io.github.module.ai.model.request.AdminInsertOrUpdateAiKnowledgeBaseDTO;
import io.github.module.ai.model.request.AdminListAiKnowledgeBaseDTO;
import io.github.module.ai.model.response.AiKnowledgeBaseBO;
import io.github.module.ai.model.response.AiKnowledgeBaseDetailBO;
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
 * AI 知识库.
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class AiKnowledgeBaseService {

    private static final int DEFAULT_RETRIEVAL_TOP_K = 5;

    private static final double DEFAULT_SIMILARITY_THRESHOLD = 0.0D;

    private final AiKnowledgeBaseMapper aiKnowledgeBaseMapper;

    private final AiModelConfigService aiModelConfigService;

    /**
     * 后台管理-分页列表知识库.
     */
    public PageResult<AiKnowledgeBaseBO> adminList(PageParam pageParam, AdminListAiKnowledgeBaseDTO dto) {
        Page<AiKnowledgeBaseEntity> entityPage = aiKnowledgeBaseMapper.selectPage(
                new Page<>(pageParam.getPageNum(), pageParam.getPageSize()),
                new QueryWrapper<AiKnowledgeBaseEntity>()
                        .lambda()
                        .like(CharSequenceUtil.isNotBlank(dto.getName()), AiKnowledgeBaseEntity::getName, clean(dto.getName()))
                        .eq(CharSequenceUtil.isNotBlank(dto.getEmbeddingModelConfigCode()),
                                AiKnowledgeBaseEntity::getEmbeddingModelConfigCode,
                                clean(dto.getEmbeddingModelConfigCode()))
                        .eq(dto.getStatus() != null, AiKnowledgeBaseEntity::getStatus, dto.getStatus())
                        .orderByDesc(AiKnowledgeBaseEntity::getUpdatedAt)
                        .orderByDesc(AiKnowledgeBaseEntity::getCreatedAt)
        );

        return this.entityPage2BOPage(entityPage);
    }

    /**
     * 后台管理-启用知识库下拉框.
     */
    public List<AiKnowledgeBaseBO> adminSelectOptions() {
        List<AiKnowledgeBaseEntity> entityList = aiKnowledgeBaseMapper.selectList(
                new QueryWrapper<AiKnowledgeBaseEntity>()
                        .lambda()
                        .eq(AiKnowledgeBaseEntity::getStatus, EnabledStatusEnum.ENABLED.getValue())
                        .orderByAsc(AiKnowledgeBaseEntity::getName)
        );

        return this.entityList2BOs(entityList);
    }

    /**
     * 根据 ID 取知识库详情.
     */
    public AiKnowledgeBaseDetailBO getOneById(Long id) {
        return this.getOneById(id, false);
    }

    /**
     * 根据 ID 取知识库详情.
     */
    public AiKnowledgeBaseDetailBO getOneById(Long id, boolean throwIfInvalidId) throws BusinessException {
        AiKnowledgeBaseEntity entity = aiKnowledgeBaseMapper.selectById(id);
        if (throwIfInvalidId) {
            AiErrorEnum.INVALID_ID.assertNotNull(entity);
        }

        return this.entity2DetailBO(entity);
    }

    /**
     * 后台管理-新增知识库.
     */
    @Transactional(rollbackFor = Exception.class)
    public Long adminInsert(AdminInsertOrUpdateAiKnowledgeBaseDTO dto) {
        log.info("[后台管理-新增AI知识库] >> name={}", dto.getName());
        this.checkStatus(dto.getStatus());
        this.checkExistence(dto);

        dto.setId(null);
        AiKnowledgeBaseEntity entity = new AiKnowledgeBaseEntity();
        BeanUtil.copyProperties(dto, entity);
        normalize(entity);
        resolveEmbeddingSnapshot(entity);
        entity.setDocumentCount(0);
        entity.setChunkCount(0);
        aiKnowledgeBaseMapper.insert(entity);

        return entity.getId();
    }

    /**
     * 后台管理-编辑知识库.
     */
    @Transactional(rollbackFor = Exception.class)
    public void adminUpdate(AdminInsertOrUpdateAiKnowledgeBaseDTO dto) {
        log.info("[后台管理-编辑AI知识库] >> id={}, name={}", dto.getId(), dto.getName());
        this.checkStatus(dto.getStatus());
        AiKnowledgeBaseEntity existingEntity = aiKnowledgeBaseMapper.selectById(dto.getId());
        AiErrorEnum.INVALID_ID.assertNotNull(existingEntity);
        this.checkExistence(dto);

        AiKnowledgeBaseEntity entity = new AiKnowledgeBaseEntity();
        BeanUtil.copyProperties(dto, entity);
        normalize(entity);
        resolveEmbeddingSnapshot(entity);
        aiKnowledgeBaseMapper.updateById(entity);
    }

    /**
     * 后台管理-删除知识库.
     */
    @Transactional(rollbackFor = Exception.class)
    public void adminDelete(Collection<Long> ids) {
        log.info("[后台管理-删除AI知识库] >> 入参={}", ids);
        aiKnowledgeBaseMapper.deleteBatchIds(ids);
    }

    /**
     * 后台管理-更新知识库启停状态.
     */
    @Transactional(rollbackFor = Exception.class)
    public void adminUpdateStatus(Long id, Integer status) {
        log.info("[后台管理-更新AI知识库启停状态] >> id={}, status={}", id, status);
        this.checkStatus(status);
        AiKnowledgeBaseEntity existingEntity = aiKnowledgeBaseMapper.selectById(id);
        AiErrorEnum.INVALID_ID.assertNotNull(existingEntity);

        AiKnowledgeBaseEntity entity = new AiKnowledgeBaseEntity()
                .setStatus(status);
        entity.setId(id);
        aiKnowledgeBaseMapper.updateById(entity);
    }

    /*
    ----------------------------------------------------------------
                        私有方法 private methods
    ----------------------------------------------------------------
     */

    private AiKnowledgeBaseBO entity2BO(AiKnowledgeBaseEntity entity) {
        if (entity == null) {
            return null;
        }

        AiKnowledgeBaseBO bo = new AiKnowledgeBaseBO();
        BeanUtil.copyProperties(entity, bo);

        return bo;
    }

    private AiKnowledgeBaseDetailBO entity2DetailBO(AiKnowledgeBaseEntity entity) {
        if (entity == null) {
            return null;
        }

        AiKnowledgeBaseDetailBO bo = new AiKnowledgeBaseDetailBO();
        BeanUtil.copyProperties(entity, bo);

        return bo;
    }

    private List<AiKnowledgeBaseBO> entityList2BOs(List<AiKnowledgeBaseEntity> entityList) {
        if (CollUtil.isEmpty(entityList)) {
            return Collections.emptyList();
        }

        List<AiKnowledgeBaseBO> ret = new ArrayList<>(entityList.size());
        entityList.forEach(entity -> ret.add(this.entity2BO(entity)));

        return ret;
    }

    private PageResult<AiKnowledgeBaseBO> entityPage2BOPage(Page<AiKnowledgeBaseEntity> entityPage) {
        return new PageResult<AiKnowledgeBaseBO>()
                .setCurrent(entityPage.getCurrent())
                .setSize(entityPage.getSize())
                .setTotal(entityPage.getTotal())
                .setRecords(this.entityList2BOs(entityPage.getRecords()));
    }

    private void checkExistence(AdminInsertOrUpdateAiKnowledgeBaseDTO dto) {
        AiKnowledgeBaseEntity existingEntity = aiKnowledgeBaseMapper.selectOne(
                new QueryWrapper<AiKnowledgeBaseEntity>()
                        .lambda()
                        .select(AiKnowledgeBaseEntity::getId)
                        .eq(AiKnowledgeBaseEntity::getName, clean(dto.getName()))
                        .last(BaseConstant.CRUD.SQL_LIMIT_1)
        );

        if (existingEntity != null && !existingEntity.getId().equals(dto.getId())) {
            throw new BusinessException(AiErrorEnum.DUPLICATE_KNOWLEDGE_BASE);
        }
    }

    private void normalize(AiKnowledgeBaseEntity entity) {
        entity.setName(clean(entity.getName()));
        entity.setDescription(StrUtil.blankToDefault(clean(entity.getDescription()), StrUtil.EMPTY));
        entity.setEmbeddingModelConfigCode(clean(entity.getEmbeddingModelConfigCode()));
        entity.setRetrievalTopK(defaultIfNull(entity.getRetrievalTopK(), DEFAULT_RETRIEVAL_TOP_K));
        entity.setSimilarityThreshold(defaultIfNull(entity.getSimilarityThreshold(), DEFAULT_SIMILARITY_THRESHOLD));
    }

    private void resolveEmbeddingSnapshot(AiKnowledgeBaseEntity entity) {
        if (StrUtil.isBlank(entity.getEmbeddingModelConfigCode())) {
            entity.setEmbeddingModelConfigId(null);
            entity.setEmbeddingProviderType(null);
            entity.setEmbeddingModelName(null);
            return;
        }

        AiModelConfigBO embeddingConfig =
                aiModelConfigService.getEnabledConfigByCode(entity.getEmbeddingModelConfigCode(), true);
        if (!AiModelCapabilityConstant.contains(
                embeddingConfig == null ? null : embeddingConfig.getSupportedCapabilities(),
                AiModelCapabilityConstant.EMBEDDING)) {
            throw new BusinessException(AiErrorEnum.INVALID_KNOWLEDGE_EMBEDDING_CONFIG);
        }
        entity.setEmbeddingModelConfigId(embeddingConfig.getId());
        entity.setEmbeddingModelConfigCode(embeddingConfig.getCode());
        entity.setEmbeddingProviderType(embeddingConfig.getProviderType());
        entity.setEmbeddingModelName(embeddingConfig.getModelName());
    }

    private void checkStatus(Integer status) {
        if (EnabledStatusEnum.of(status) == null) {
            throw new BusinessException(AiErrorEnum.INVALID_KNOWLEDGE_BASE_STATUS);
        }
    }

    private Integer defaultIfNull(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }

    private Double defaultIfNull(Double value, Double defaultValue) {
        return value == null ? defaultValue : value;
    }

    private String clean(String value) {
        return CharSequenceUtil.cleanBlank(value);
    }
}
