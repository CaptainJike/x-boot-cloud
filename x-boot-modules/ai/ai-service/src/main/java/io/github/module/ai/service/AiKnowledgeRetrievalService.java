package io.github.module.ai.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.context.UserContextHolder;
import io.github.framework.core.enums.EnabledStatusEnum;
import io.github.framework.core.exception.BusinessException;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.module.ai.constant.AiModelCapabilityConstant;
import io.github.module.ai.entity.AiKnowledgeBaseEntity;
import io.github.module.ai.entity.AiKnowledgeRetrievalLogEntity;
import io.github.module.ai.enums.AiErrorEnum;
import io.github.module.ai.mapper.AiKnowledgeBaseMapper;
import io.github.module.ai.mapper.AiKnowledgeRetrievalLogMapper;
import io.github.module.ai.model.request.AdminListAiKnowledgeRetrievalLogDTO;
import io.github.module.ai.model.request.AdminRetrieveAiKnowledgeDTO;
import io.github.module.ai.model.response.AiKnowledgeRetrievalHitBO;
import io.github.module.ai.model.response.AiKnowledgeRetrievalLogBO;
import io.github.module.ai.model.response.AiKnowledgeRetrievalResultBO;
import io.github.module.ai.model.response.AiModelConfigBO;
import io.github.module.ai.service.model.AiKnowledgeEmbeddingContext;
import io.github.module.ai.service.model.AiKnowledgeVectorDocument;
import io.github.module.ai.service.model.AiKnowledgeVectorSearchHit;
import io.github.module.ai.service.model.AiKnowledgeVectorSearchRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AI 知识库基础检索服务.
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class AiKnowledgeRetrievalService {

    private static final int DEFAULT_RETRIEVAL_TOP_K = 5;

    private static final double DEFAULT_SIMILARITY_THRESHOLD = 0.0D;

    private static final int STATUS_FAILURE = 0;

    private static final int STATUS_SUCCESS = 1;

    private static final int HITS_SUMMARY_MAX_LENGTH = 1000;

    private final AiKnowledgeBaseMapper aiKnowledgeBaseMapper;

    private final AiKnowledgeRetrievalLogMapper aiKnowledgeRetrievalLogMapper;

    private final AiModelConfigService aiModelConfigService;

    private final AiKnowledgeVectorRetrievalService aiKnowledgeVectorRetrievalService;

    /**
     * 后台管理-执行知识库基础检索.
     */
    @Transactional(rollbackFor = Exception.class)
    public AiKnowledgeRetrievalResultBO adminRetrieve(AdminRetrieveAiKnowledgeDTO dto) {
        long startAt = System.currentTimeMillis();
        String query = cleanQuery(dto.getQuery());
        List<Long> knowledgeBaseIds = cleanKnowledgeBaseIds(dto.getKnowledgeBaseIds());
        List<AiKnowledgeBaseEntity> knowledgeBases = loadEnabledKnowledgeBases(knowledgeBaseIds);
        Integer topK = resolveTopK(dto, knowledgeBases);
        Double similarityThreshold = resolveSimilarityThreshold(dto, knowledgeBases);
        AiKnowledgeEmbeddingContext context = buildEmbeddingContext(knowledgeBases);

        List<AiKnowledgeVectorSearchHit> searchHits;
        try {
            searchHits = aiKnowledgeVectorRetrievalService.search(AiKnowledgeVectorSearchRequest.builder()
                    .knowledgeBaseIds(knowledgeBaseIds)
                    .query(query)
                    .topK(topK)
                    .similarityThreshold(similarityThreshold)
                    .context(context)
                    .build());
        } catch (RuntimeException ex) {
            log.warn("[后台管理-AI知识库基础检索失败] >> knowledgeBaseIds={}, query={}", knowledgeBaseIds, query, ex);
            LocalDateTime retrievedAt = LocalDateTime.now();
            AiKnowledgeRetrievalResultBO result = baseResult(query, topK, similarityThreshold, startAt)
                    .setStatus(STATUS_FAILURE)
                    .setHitCount(0)
                    .setHits(Collections.emptyList())
                    .setErrorMessage(rootMessage(ex));
            writeRetrievalLogIfNeeded(dto, result, knowledgeBaseIds, retrievedAt, ex);
            return result;
        }

        List<AiKnowledgeRetrievalHitBO> hits = vectorHits2BOs(searchHits, knowledgeBases, topK, similarityThreshold);
        LocalDateTime retrievedAt = LocalDateTime.now();
        AiKnowledgeRetrievalResultBO result = baseResult(query, topK, similarityThreshold, startAt)
                .setStatus(STATUS_SUCCESS)
                .setHitCount(hits.size())
                .setHits(hits);

        refreshLastRetrievedAt(knowledgeBaseIds, retrievedAt);
        writeRetrievalLogIfNeeded(dto, result, knowledgeBaseIds, retrievedAt, null);
        return result;
    }

    /**
     * 后台管理-分页列表知识库检索日志.
     */
    public PageResult<AiKnowledgeRetrievalLogBO> adminListLogs(PageParam pageParam,
                                                               AdminListAiKnowledgeRetrievalLogDTO dto) {
        Page<AiKnowledgeRetrievalLogEntity> entityPage = aiKnowledgeRetrievalLogMapper.selectPage(
                new Page<>(pageParam.getPageNum(), pageParam.getPageSize()),
                new QueryWrapper<AiKnowledgeRetrievalLogEntity>()
                        .lambda()
                        .like(dto.getKnowledgeBaseId() != null,
                                AiKnowledgeRetrievalLogEntity::getKnowledgeBaseIds,
                                logKnowledgeBaseId(dto.getKnowledgeBaseId()))
                        .eq(CharSequenceUtil.isNotBlank(dto.getConversationId()),
                                AiKnowledgeRetrievalLogEntity::getConversationId,
                                clean(dto.getConversationId()))
                        .eq(CharSequenceUtil.isNotBlank(dto.getMessageId()),
                                AiKnowledgeRetrievalLogEntity::getMessageId,
                                clean(dto.getMessageId()))
                        .like(CharSequenceUtil.isNotBlank(dto.getQueryKeyword()),
                                AiKnowledgeRetrievalLogEntity::getQueryText,
                                cleanQuery(dto.getQueryKeyword()))
                        .eq(dto.getStatus() != null, AiKnowledgeRetrievalLogEntity::getStatus, dto.getStatus())
                        .orderByDesc(AiKnowledgeRetrievalLogEntity::getRetrievedAt)
                        .orderByDesc(AiKnowledgeRetrievalLogEntity::getCreatedAt)
        );

        return entityPage2BOPage(entityPage);
    }

    /**
     * 根据 ID 取知识库检索日志详情.
     */
    public AiKnowledgeRetrievalLogBO getLogById(Long id, boolean throwIfInvalidId) throws BusinessException {
        AiKnowledgeRetrievalLogEntity entity = aiKnowledgeRetrievalLogMapper.selectById(id);
        if (throwIfInvalidId) {
            AiErrorEnum.INVALID_ID.assertNotNull(entity);
        }

        return entity2LogBO(entity);
    }

    /*
    ----------------------------------------------------------------
                        私有方法 private methods
    ----------------------------------------------------------------
     */

    private List<Long> cleanKnowledgeBaseIds(List<Long> knowledgeBaseIds) {
        List<Long> ids = knowledgeBaseIds == null
                ? Collections.emptyList()
                : knowledgeBaseIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        AiErrorEnum.INVALID_ID.assertNotEmpty(ids);
        return ids;
    }

    private List<AiKnowledgeBaseEntity> loadEnabledKnowledgeBases(List<Long> knowledgeBaseIds) {
        List<AiKnowledgeBaseEntity> entityList = aiKnowledgeBaseMapper.selectBatchIds(knowledgeBaseIds);
        if (CollUtil.isEmpty(entityList) || entityList.size() != knowledgeBaseIds.size()) {
            throw new BusinessException(AiErrorEnum.INVALID_ID);
        }

        Map<Long, AiKnowledgeBaseEntity> entityMap = entityList.stream()
                .collect(Collectors.toMap(AiKnowledgeBaseEntity::getId, Function.identity()));
        List<AiKnowledgeBaseEntity> orderedEntityList = knowledgeBaseIds.stream()
                .map(entityMap::get)
                .toList();
        if (orderedEntityList.stream().anyMatch(Objects::isNull)) {
            throw new BusinessException(AiErrorEnum.INVALID_ID);
        }
        if (orderedEntityList.stream()
                .anyMatch(entity -> !EnabledStatusEnum.ENABLED.getValue().equals(entity.getStatus()))) {
            throw new BusinessException(AiErrorEnum.INVALID_KNOWLEDGE_BASE_STATUS);
        }

        return orderedEntityList;
    }

    private Integer resolveTopK(AdminRetrieveAiKnowledgeDTO dto, List<AiKnowledgeBaseEntity> knowledgeBases) {
        if (dto.getTopK() != null) {
            return dto.getTopK();
        }

        return knowledgeBases.stream()
                .map(AiKnowledgeBaseEntity::getRetrievalTopK)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(DEFAULT_RETRIEVAL_TOP_K);
    }

    private Double resolveSimilarityThreshold(AdminRetrieveAiKnowledgeDTO dto,
                                              List<AiKnowledgeBaseEntity> knowledgeBases) {
        if (dto.getSimilarityThreshold() != null) {
            return dto.getSimilarityThreshold();
        }

        return knowledgeBases.stream()
                .map(AiKnowledgeBaseEntity::getSimilarityThreshold)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(DEFAULT_SIMILARITY_THRESHOLD);
    }

    private AiKnowledgeEmbeddingContext buildEmbeddingContext(List<AiKnowledgeBaseEntity> knowledgeBases) {
        String embeddingConfigCode = knowledgeBases.getFirst().getEmbeddingModelConfigCode();
        AiErrorEnum.INVALID_KNOWLEDGE_EMBEDDING_CONFIG.assertNotBlank(embeddingConfigCode);
        if (knowledgeBases.stream()
                .map(AiKnowledgeBaseEntity::getEmbeddingModelConfigCode)
                .anyMatch(code -> !StrUtil.equals(embeddingConfigCode, code))) {
            throw new BusinessException(AiErrorEnum.INVALID_KNOWLEDGE_EMBEDDING_CONFIG);
        }

        AiModelConfigBO config = aiModelConfigService.getEnabledConfigByCode(embeddingConfigCode, true);
        AiErrorEnum.INVALID_KNOWLEDGE_EMBEDDING_CONFIG.assertNotNull(config);
        if (!AiModelCapabilityConstant.contains(
                config == null ? null : config.getSupportedCapabilities(),
                AiModelCapabilityConstant.EMBEDDING)) {
            throw new BusinessException(AiErrorEnum.INVALID_KNOWLEDGE_EMBEDDING_CONFIG);
        }
        return AiKnowledgeEmbeddingContext.builder()
                .modelConfigId(config.getId())
                .modelConfigCode(config.getCode())
                .providerType(config.getProviderType())
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .modelName(config.getModelName())
                .timeoutSeconds(config.getTimeoutSeconds())
                .build();
    }

    private List<AiKnowledgeRetrievalHitBO> vectorHits2BOs(List<AiKnowledgeVectorSearchHit> searchHits,
                                                           List<AiKnowledgeBaseEntity> knowledgeBases,
                                                           Integer topK,
                                                           Double similarityThreshold) {
        if (CollUtil.isEmpty(searchHits)) {
            return Collections.emptyList();
        }
        Map<Long, String> knowledgeBaseNameMap = knowledgeBases.stream()
                .collect(Collectors.toMap(AiKnowledgeBaseEntity::getId, AiKnowledgeBaseEntity::getName));

        return searchHits.stream()
                .filter(Objects::nonNull)
                .map(hit -> vectorHit2BO(hit, knowledgeBaseNameMap))
                .filter(Objects::nonNull)
                .filter(hit -> hit.getSimilarityScore() == null
                        || hit.getSimilarityScore() >= similarityThreshold)
                .limit(topK)
                .toList();
    }

    private AiKnowledgeRetrievalHitBO vectorHit2BO(AiKnowledgeVectorSearchHit hit,
                                                   Map<Long, String> knowledgeBaseNameMap) {
        AiKnowledgeVectorDocument document = hit.getDocument();
        if (document == null) {
            return null;
        }

        return AiKnowledgeRetrievalHitBO.builder()
                .knowledgeBaseId(document.getKnowledgeBaseId())
                .knowledgeBaseName(StrUtil.blankToDefault(
                        document.getKnowledgeBaseName(),
                        knowledgeBaseNameMap.get(document.getKnowledgeBaseId())))
                .documentId(document.getDocumentId())
                .documentName(document.getDocumentName())
                .chunkId(document.getChunkId())
                .chunkNo(document.getChunkNo())
                .content(document.getContent())
                .sourcePage(document.getSourcePage())
                .sourcePosition(document.getSourcePosition())
                .similarityScore(hit.getSimilarityScore())
                .build();
    }

    private AiKnowledgeRetrievalResultBO baseResult(String query,
                                                    Integer topK,
                                                    Double similarityThreshold,
                                                    long startAt) {
        return AiKnowledgeRetrievalResultBO.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(similarityThreshold)
                .elapsedMillis(System.currentTimeMillis() - startAt)
                .build();
    }

    private void writeRetrievalLogIfNeeded(AdminRetrieveAiKnowledgeDTO dto,
                                           AiKnowledgeRetrievalResultBO result,
                                           List<Long> knowledgeBaseIds,
                                           LocalDateTime retrievedAt,
                                           Throwable ex) {
        if (Boolean.FALSE.equals(dto.getLogFlag())) {
            return;
        }

        AiKnowledgeRetrievalLogEntity entity = new AiKnowledgeRetrievalLogEntity()
                .setRetrievalId(IdUtil.fastSimpleUUID())
                .setUserId(UserContextHolder.getUserId())
                .setKnowledgeBaseIds(logKnowledgeBaseIds(knowledgeBaseIds))
                .setConversationId(clean(dto.getConversationId()))
                .setMessageId(clean(dto.getMessageId()))
                .setQueryText(result.getQuery())
                .setTopK(result.getTopK())
                .setSimilarityThreshold(result.getSimilarityThreshold())
                .setHitCount(result.getHitCount())
                .setHitsSummary(buildHitsSummary(result.getHits()))
                .setElapsedMs(result.getElapsedMillis())
                .setStatus(result.getStatus())
                .setErrorCode(errorCode(ex))
                .setErrorMessage(result.getErrorMessage())
                .setRetrievedAt(retrievedAt);
        aiKnowledgeRetrievalLogMapper.insert(entity);
        result.setLogId(entity.getId());
    }

    private void refreshLastRetrievedAt(List<Long> knowledgeBaseIds, LocalDateTime retrievedAt) {
        knowledgeBaseIds.forEach(id -> {
            AiKnowledgeBaseEntity entity = new AiKnowledgeBaseEntity()
                    .setLastRetrievedAt(retrievedAt);
            entity.setId(id);
            aiKnowledgeBaseMapper.updateById(entity);
        });
    }

    private PageResult<AiKnowledgeRetrievalLogBO> entityPage2BOPage(Page<AiKnowledgeRetrievalLogEntity> entityPage) {
        return new PageResult<AiKnowledgeRetrievalLogBO>()
                .setCurrent(entityPage.getCurrent())
                .setSize(entityPage.getSize())
                .setTotal(entityPage.getTotal())
                .setRecords(entityList2LogBOs(entityPage.getRecords()));
    }

    private List<AiKnowledgeRetrievalLogBO> entityList2LogBOs(List<AiKnowledgeRetrievalLogEntity> entityList) {
        if (CollUtil.isEmpty(entityList)) {
            return Collections.emptyList();
        }

        return entityList.stream()
                .map(this::entity2LogBO)
                .toList();
    }

    private AiKnowledgeRetrievalLogBO entity2LogBO(AiKnowledgeRetrievalLogEntity entity) {
        if (entity == null) {
            return null;
        }

        return AiKnowledgeRetrievalLogBO.builder()
                .id(entity.getId())
                .createdAt(entity.getCreatedAt())
                .userId(entity.getUserId())
                .knowledgeBaseIds(parseKnowledgeBaseIds(entity.getKnowledgeBaseIds()))
                .conversationId(entity.getConversationId())
                .messageId(entity.getMessageId())
                .query(entity.getQueryText())
                .topK(entity.getTopK())
                .similarityThreshold(entity.getSimilarityThreshold())
                .hitCount(entity.getHitCount())
                .hitsSummary(entity.getHitsSummary())
                .elapsedMillis(entity.getElapsedMs())
                .status(entity.getStatus())
                .errorCode(entity.getErrorCode())
                .errorMessage(entity.getErrorMessage())
                .build();
    }

    private String buildHitsSummary(List<AiKnowledgeRetrievalHitBO> hits) {
        if (CollUtil.isEmpty(hits)) {
            return StrUtil.EMPTY;
        }

        String summary = hits.stream()
                .map(this::hitSummary)
                .collect(Collectors.joining(";"));
        return truncate(summary, HITS_SUMMARY_MAX_LENGTH);
    }

    private String hitSummary(AiKnowledgeRetrievalHitBO hit) {
        String documentName = StrUtil.blankToDefault(hit.getDocumentName(), "document");
        String chunkNo = hit.getChunkNo() == null ? StrUtil.EMPTY : "#" + hit.getChunkNo();
        String score = hit.getSimilarityScore() == null ? StrUtil.EMPTY : "@" + hit.getSimilarityScore();
        return documentName + chunkNo + score;
    }

    private List<Long> parseKnowledgeBaseIds(String value) {
        if (StrUtil.isBlank(value)) {
            return Collections.emptyList();
        }

        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .map(Long::valueOf)
                .toList();
    }

    private String logKnowledgeBaseIds(List<Long> knowledgeBaseIds) {
        return "," + knowledgeBaseIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")) + ",";
    }

    private String logKnowledgeBaseId(Long knowledgeBaseId) {
        return "," + knowledgeBaseId + ",";
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
        Throwable root = e;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return StrUtil.blankToDefault(root.getMessage(), "知识库检索失败");
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String cleanQuery(String value) {
        return StrUtil.trimToEmpty(value);
    }

    private String clean(String value) {
        return CharSequenceUtil.cleanBlank(value);
    }
}
