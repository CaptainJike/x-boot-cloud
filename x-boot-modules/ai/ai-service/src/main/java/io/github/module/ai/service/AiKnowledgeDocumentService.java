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
import io.github.module.ai.entity.AiKnowledgeBaseEntity;
import io.github.module.ai.entity.AiKnowledgeDocumentChunkEntity;
import io.github.module.ai.entity.AiKnowledgeDocumentEntity;
import io.github.module.ai.enums.AiErrorEnum;
import io.github.module.ai.mapper.AiKnowledgeBaseMapper;
import io.github.module.ai.mapper.AiKnowledgeDocumentChunkMapper;
import io.github.module.ai.mapper.AiKnowledgeDocumentMapper;
import io.github.module.ai.model.request.AdminBindAiKnowledgeDocumentDTO;
import io.github.module.ai.model.request.AdminListAiKnowledgeDocumentChunkDTO;
import io.github.module.ai.model.request.AdminListAiKnowledgeDocumentDTO;
import io.github.module.ai.model.response.AiKnowledgeDocumentBO;
import io.github.module.ai.model.response.AiKnowledgeDocumentChunkBO;
import io.github.module.ai.model.response.AiKnowledgeDocumentDetailBO;
import io.github.module.oss.facade.OssFileInfoFacade;
import io.github.module.oss.model.response.OssFileInfoBO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AI 知识库文档.
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class AiKnowledgeDocumentService {

    private static final int STATUS_FAILED = 0;

    private static final int STATUS_PENDING = 3;

    private final AiKnowledgeDocumentMapper aiKnowledgeDocumentMapper;

    private final AiKnowledgeDocumentChunkMapper aiKnowledgeDocumentChunkMapper;

    private final AiKnowledgeBaseMapper aiKnowledgeBaseMapper;

    private final AiKnowledgeDocumentIndexService aiKnowledgeDocumentIndexService;

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private OssFileInfoFacade ossFileInfoFacade;

    /**
     * 后台管理-分页列表知识库文档.
     */
    public PageResult<AiKnowledgeDocumentBO> adminList(PageParam pageParam, AdminListAiKnowledgeDocumentDTO dto) {
        Page<AiKnowledgeDocumentEntity> entityPage = aiKnowledgeDocumentMapper.selectPage(
                new Page<>(pageParam.getPageNum(), pageParam.getPageSize()),
                new QueryWrapper<AiKnowledgeDocumentEntity>()
                        .lambda()
                        .eq(dto.getKnowledgeBaseId() != null,
                                AiKnowledgeDocumentEntity::getKnowledgeBaseId,
                                dto.getKnowledgeBaseId())
                        .eq(dto.getOssFileId() != null, AiKnowledgeDocumentEntity::getOssFileId, dto.getOssFileId())
                        .like(CharSequenceUtil.isNotBlank(dto.getDocumentName()),
                                AiKnowledgeDocumentEntity::getDocumentName,
                                clean(dto.getDocumentName()))
                        .like(CharSequenceUtil.isNotBlank(dto.getOriginalFilename()),
                                AiKnowledgeDocumentEntity::getOriginalFilename,
                                clean(dto.getOriginalFilename()))
                        .eq(dto.getParseStatus() != null,
                                AiKnowledgeDocumentEntity::getParseStatus,
                                dto.getParseStatus())
                        .eq(dto.getChunkStatus() != null,
                                AiKnowledgeDocumentEntity::getChunkStatus,
                                dto.getChunkStatus())
                        .eq(dto.getStatus() != null, AiKnowledgeDocumentEntity::getStatus, dto.getStatus())
                        .orderByDesc(AiKnowledgeDocumentEntity::getUpdatedAt)
                        .orderByDesc(AiKnowledgeDocumentEntity::getCreatedAt)
        );

        return this.entityPage2BOPage(entityPage);
    }

    /**
     * 根据 ID 取知识库文档详情.
     */
    public AiKnowledgeDocumentDetailBO getOneById(Long id) {
        return this.getOneById(id, false);
    }

    /**
     * 根据 ID 取知识库文档详情.
     */
    public AiKnowledgeDocumentDetailBO getOneById(Long id, boolean throwIfInvalidId) throws BusinessException {
        AiKnowledgeDocumentEntity entity = aiKnowledgeDocumentMapper.selectById(id);
        if (throwIfInvalidId) {
            AiErrorEnum.INVALID_ID.assertNotNull(entity);
        }

        return this.entity2DetailBO(entity);
    }

    /**
     * 后台管理-关联 OSS 文件为知识库文档.
     *
     * @return 主键ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long adminBindOssFile(AdminBindAiKnowledgeDocumentDTO dto) {
        log.info("[后台管理-关联OSS文件为AI知识库文档] >> knowledgeBaseId={}, ossFileId={}",
                dto.getKnowledgeBaseId(),
                dto.getOssFileId());
        AiKnowledgeBaseEntity knowledgeBase = aiKnowledgeBaseMapper.selectById(dto.getKnowledgeBaseId());
        AiErrorEnum.INVALID_ID.assertNotNull(knowledgeBase);
        this.checkDuplicate(dto.getKnowledgeBaseId(), dto.getOssFileId());
        OssFileInfoBO ossFileInfo = ossFileInfoFacade.getOneById(dto.getOssFileId(), true);

        AiKnowledgeDocumentEntity entity = new AiKnowledgeDocumentEntity()
                .setKnowledgeBaseId(dto.getKnowledgeBaseId())
                .setOssFileId(dto.getOssFileId())
                .setDocumentName(resolveDocumentName(dto.getDocumentName(), ossFileInfo))
                .setDescription(StrUtil.blankToDefault(clean(dto.getDescription()), StrUtil.EMPTY))
                .setOriginalFilename(resolveOriginalFilename(ossFileInfo))
                .setExtendName(clean(ossFileInfo.getExtendName()))
                .setFileSize(ossFileInfo.getFileSize())
                .setMd5(clean(ossFileInfo.getMd5()))
                .setStoragePlatform(clean(ossFileInfo.getStoragePlatform()))
                .setParseStatus(STATUS_PENDING)
                .setChunkStatus(STATUS_PENDING)
                .setEmbeddingStatus(STATUS_PENDING)
                .setStatus(EnabledStatusEnum.ENABLED.getValue())
                .setChunkCount(0)
                .setRetryCount(0);
        aiKnowledgeDocumentMapper.insert(entity);
        this.refreshKnowledgeBaseStats(List.of(dto.getKnowledgeBaseId()));
        this.afterCommit(() -> aiKnowledgeDocumentIndexService.indexDocument(entity.getId()));

        return entity.getId();
    }

    /**
     * 后台管理-删除知识库文档.
     */
    @Transactional(rollbackFor = Exception.class)
    public void adminDelete(Collection<Long> ids) {
        log.info("[后台管理-删除AI知识库文档] >> 入参={}", ids);
        if (CollUtil.isEmpty(ids)) {
            return;
        }

        List<AiKnowledgeDocumentEntity> entityList = aiKnowledgeDocumentMapper.selectBatchIds(ids);
        Set<Long> knowledgeBaseIds = entityList.stream()
                .map(AiKnowledgeDocumentEntity::getKnowledgeBaseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        aiKnowledgeDocumentChunkMapper.delete(
                new QueryWrapper<AiKnowledgeDocumentChunkEntity>()
                        .lambda()
                        .in(AiKnowledgeDocumentChunkEntity::getDocumentId, ids)
        );
        aiKnowledgeDocumentMapper.deleteBatchIds(ids);
        this.refreshKnowledgeBaseStats(knowledgeBaseIds);
        List<Long> documentIds = entityList.stream()
                .map(AiKnowledgeDocumentEntity::getId)
                .filter(Objects::nonNull)
                .toList();
        this.afterCommit(() -> documentIds.forEach(aiKnowledgeDocumentIndexService::deleteDocumentVectors));
    }

    /**
     * 后台管理-重试文档解析或切片.
     */
    @Transactional(rollbackFor = Exception.class)
    public void adminRetry(Long id) {
        log.info("[后台管理-重试AI知识库文档] >> id={}", id);
        AiKnowledgeDocumentEntity existingEntity = aiKnowledgeDocumentMapper.selectById(id);
        AiErrorEnum.INVALID_ID.assertNotNull(existingEntity);

        AiKnowledgeDocumentEntity entity = new AiKnowledgeDocumentEntity()
                .setParseStatus(STATUS_PENDING)
                .setChunkStatus(STATUS_PENDING)
                .setEmbeddingStatus(STATUS_PENDING)
                .setParseErrorMessage(StrUtil.EMPTY)
                .setChunkErrorMessage(StrUtil.EMPTY)
                .setRetryCount(defaultIfNull(existingEntity.getRetryCount(), 0) + 1)
                .setLastRetryAt(LocalDateTime.now());
        entity.setId(id);
        aiKnowledgeDocumentMapper.updateById(entity);
        this.afterCommit(() -> aiKnowledgeDocumentIndexService.indexDocument(id));
    }

    /**
     * 后台管理-分页列表文档切片.
     */
    public PageResult<AiKnowledgeDocumentChunkBO> adminListChunks(Long documentId,
                                                                  PageParam pageParam,
                                                                  AdminListAiKnowledgeDocumentChunkDTO dto) {
        AiKnowledgeDocumentEntity document = aiKnowledgeDocumentMapper.selectById(documentId);
        AiErrorEnum.INVALID_ID.assertNotNull(document);
        Page<AiKnowledgeDocumentChunkEntity> entityPage = aiKnowledgeDocumentChunkMapper.selectPage(
                new Page<>(pageParam.getPageNum(), pageParam.getPageSize()),
                new QueryWrapper<AiKnowledgeDocumentChunkEntity>()
                        .lambda()
                        .eq(AiKnowledgeDocumentChunkEntity::getDocumentId, documentId)
                        .like(CharSequenceUtil.isNotBlank(dto.getKeyword()),
                                AiKnowledgeDocumentChunkEntity::getContent,
                                clean(dto.getKeyword()))
                        .eq(dto.getStatus() != null, AiKnowledgeDocumentChunkEntity::getStatus, dto.getStatus())
                        .eq(dto.getEmbeddingStatus() != null,
                                AiKnowledgeDocumentChunkEntity::getEmbeddingStatus,
                                dto.getEmbeddingStatus())
                        .orderByAsc(AiKnowledgeDocumentChunkEntity::getChunkNo)
        );

        return this.chunkEntityPage2BOPage(entityPage, document);
    }

    /*
    ----------------------------------------------------------------
                        私有方法 private methods
    ----------------------------------------------------------------
     */

    private AiKnowledgeDocumentBO entity2BO(AiKnowledgeDocumentEntity entity, Map<Long, String> knowledgeBaseNameMap) {
        if (entity == null) {
            return null;
        }

        AiKnowledgeDocumentBO bo = new AiKnowledgeDocumentBO();
        BeanUtil.copyProperties(entity, bo);
        bo.setKnowledgeBaseName(knowledgeBaseNameMap.get(entity.getKnowledgeBaseId()));
        bo.setErrorMessage(resolveErrorMessage(entity));

        return bo;
    }

    private AiKnowledgeDocumentDetailBO entity2DetailBO(AiKnowledgeDocumentEntity entity) {
        if (entity == null) {
            return null;
        }

        AiKnowledgeDocumentDetailBO bo = new AiKnowledgeDocumentDetailBO();
        BeanUtil.copyProperties(entity, bo);
        bo.setKnowledgeBaseName(resolveKnowledgeBaseName(entity.getKnowledgeBaseId()));

        return bo;
    }

    private AiKnowledgeDocumentChunkBO chunkEntity2BO(AiKnowledgeDocumentChunkEntity entity,
                                                     AiKnowledgeDocumentEntity document) {
        if (entity == null) {
            return null;
        }

        AiKnowledgeDocumentChunkBO bo = new AiKnowledgeDocumentChunkBO();
        BeanUtil.copyProperties(entity, bo);
        bo.setDocumentName(document.getDocumentName());

        return bo;
    }

    private List<AiKnowledgeDocumentBO> entityList2BOs(List<AiKnowledgeDocumentEntity> entityList) {
        if (CollUtil.isEmpty(entityList)) {
            return Collections.emptyList();
        }

        Map<Long, String> knowledgeBaseNameMap = loadKnowledgeBaseNameMap(entityList);
        List<AiKnowledgeDocumentBO> ret = new ArrayList<>(entityList.size());
        entityList.forEach(entity -> ret.add(this.entity2BO(entity, knowledgeBaseNameMap)));

        return ret;
    }

    private List<AiKnowledgeDocumentChunkBO> chunkEntityList2BOs(List<AiKnowledgeDocumentChunkEntity> entityList,
                                                                 AiKnowledgeDocumentEntity document) {
        if (CollUtil.isEmpty(entityList)) {
            return Collections.emptyList();
        }

        List<AiKnowledgeDocumentChunkBO> ret = new ArrayList<>(entityList.size());
        entityList.forEach(entity -> ret.add(this.chunkEntity2BO(entity, document)));

        return ret;
    }

    private PageResult<AiKnowledgeDocumentBO> entityPage2BOPage(Page<AiKnowledgeDocumentEntity> entityPage) {
        return new PageResult<AiKnowledgeDocumentBO>()
                .setCurrent(entityPage.getCurrent())
                .setSize(entityPage.getSize())
                .setTotal(entityPage.getTotal())
                .setRecords(this.entityList2BOs(entityPage.getRecords()));
    }

    private PageResult<AiKnowledgeDocumentChunkBO> chunkEntityPage2BOPage(
            Page<AiKnowledgeDocumentChunkEntity> entityPage,
            AiKnowledgeDocumentEntity document) {
        return new PageResult<AiKnowledgeDocumentChunkBO>()
                .setCurrent(entityPage.getCurrent())
                .setSize(entityPage.getSize())
                .setTotal(entityPage.getTotal())
                .setRecords(this.chunkEntityList2BOs(entityPage.getRecords(), document));
    }

    private Map<Long, String> loadKnowledgeBaseNameMap(List<AiKnowledgeDocumentEntity> entityList) {
        Set<Long> knowledgeBaseIds = entityList.stream()
                .map(AiKnowledgeDocumentEntity::getKnowledgeBaseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
        if (CollUtil.isEmpty(knowledgeBaseIds)) {
            return Collections.emptyMap();
        }

        return aiKnowledgeBaseMapper.selectBatchIds(knowledgeBaseIds).stream()
                .collect(Collectors.toMap(AiKnowledgeBaseEntity::getId, AiKnowledgeBaseEntity::getName));
    }

    private String resolveKnowledgeBaseName(Long knowledgeBaseId) {
        AiKnowledgeBaseEntity knowledgeBase = aiKnowledgeBaseMapper.selectById(knowledgeBaseId);
        return knowledgeBase == null ? null : knowledgeBase.getName();
    }

    private void checkDuplicate(Long knowledgeBaseId, Long ossFileId) {
        AiKnowledgeDocumentEntity existingEntity = aiKnowledgeDocumentMapper.selectOne(
                new QueryWrapper<AiKnowledgeDocumentEntity>()
                        .lambda()
                        .select(AiKnowledgeDocumentEntity::getId)
                        .eq(AiKnowledgeDocumentEntity::getKnowledgeBaseId, knowledgeBaseId)
                        .eq(AiKnowledgeDocumentEntity::getOssFileId, ossFileId)
                        .last(BaseConstant.CRUD.SQL_LIMIT_1)
        );

        if (existingEntity != null) {
            throw new BusinessException(AiErrorEnum.DUPLICATE_KNOWLEDGE_DOCUMENT);
        }
    }

    private void refreshKnowledgeBaseStats(Collection<Long> knowledgeBaseIds) {
        if (CollUtil.isEmpty(knowledgeBaseIds)) {
            return;
        }

        for (Long knowledgeBaseId : knowledgeBaseIds) {
            Long documentCount = aiKnowledgeDocumentMapper.selectCount(
                    new QueryWrapper<AiKnowledgeDocumentEntity>()
                            .lambda()
                            .eq(AiKnowledgeDocumentEntity::getKnowledgeBaseId, knowledgeBaseId)
            );
            List<AiKnowledgeDocumentEntity> documentList = aiKnowledgeDocumentMapper.selectList(
                    new QueryWrapper<AiKnowledgeDocumentEntity>()
                            .lambda()
                            .select(AiKnowledgeDocumentEntity::getChunkCount)
                            .eq(AiKnowledgeDocumentEntity::getKnowledgeBaseId, knowledgeBaseId)
            );
            int chunkCount = documentList.stream()
                    .map(AiKnowledgeDocumentEntity::getChunkCount)
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .sum();

            AiKnowledgeBaseEntity entity = new AiKnowledgeBaseEntity()
                    .setDocumentCount(documentCount.intValue())
                    .setChunkCount(chunkCount);
            entity.setId(knowledgeBaseId);
            aiKnowledgeBaseMapper.updateById(entity);
        }
    }

    private String resolveDocumentName(String documentName, OssFileInfoBO ossFileInfo) {
        return StrUtil.blankToDefault(clean(documentName), resolveOriginalFilename(ossFileInfo));
    }

    private String resolveOriginalFilename(OssFileInfoBO ossFileInfo) {
        String originalFilename = clean(ossFileInfo.getOriginalFilename());
        String extendName = clean(ossFileInfo.getExtendName());
        if (StrUtil.isBlank(extendName)) {
            return originalFilename;
        }
        if (StrUtil.isBlank(originalFilename)) {
            return extendName;
        }
        if (originalFilename.endsWith("." + extendName)) {
            return originalFilename;
        }

        return originalFilename + "." + extendName;
    }

    private String resolveErrorMessage(AiKnowledgeDocumentEntity entity) {
        if (Objects.equals(entity.getParseStatus(), STATUS_FAILED)) {
            return entity.getParseErrorMessage();
        }
        if (Objects.equals(entity.getChunkStatus(), STATUS_FAILED)) {
            return entity.getChunkErrorMessage();
        }
        if (StrUtil.isNotBlank(entity.getParseErrorMessage())) {
            return entity.getParseErrorMessage();
        }

        return entity.getChunkErrorMessage();
    }

    private Integer defaultIfNull(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }

    private void afterCommit(Runnable runnable) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            runnable.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                runnable.run();
            }
        });
    }

    private String clean(String value) {
        return CharSequenceUtil.cleanBlank(value);
    }
}
