package io.github.module.ai.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.framework.core.exception.BusinessException;
import io.github.module.ai.constant.AiModelCapabilityConstant;
import io.github.module.ai.entity.AiKnowledgeBaseEntity;
import io.github.module.ai.entity.AiKnowledgeDocumentChunkEntity;
import io.github.module.ai.entity.AiKnowledgeDocumentEntity;
import io.github.module.ai.enums.AiErrorEnum;
import io.github.module.ai.mapper.AiKnowledgeBaseMapper;
import io.github.module.ai.mapper.AiKnowledgeDocumentChunkMapper;
import io.github.module.ai.mapper.AiKnowledgeDocumentMapper;
import io.github.module.ai.model.response.AiModelConfigBO;
import io.github.module.ai.service.embedding.AiKnowledgeEmbeddingProviderService;
import io.github.module.ai.service.model.AiKnowledgeDocumentChunkConfig;
import io.github.module.ai.service.model.AiKnowledgeDocumentChunkDraft;
import io.github.module.ai.service.model.AiKnowledgeDocumentSource;
import io.github.module.ai.service.model.AiKnowledgeEmbeddingContext;
import io.github.module.ai.service.model.AiKnowledgeEmbeddingRequest;
import io.github.module.ai.service.model.AiKnowledgeEmbeddingResponse;
import io.github.module.ai.service.model.AiKnowledgeVectorDocument;
import io.github.module.ai.service.model.AiKnowledgeVectorUpsertRequest;
import io.github.module.ai.service.vector.AiKnowledgeVectorStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * AI 知识库文档索引服务.
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class AiKnowledgeDocumentIndexService {

    private static final int STATUS_FAILED = 0;

    private static final int STATUS_SUCCESS = 1;

    private static final int STATUS_PROCESSING = 2;

    private static final int ERROR_MESSAGE_MAX_LENGTH = 1000;

    private final AiKnowledgeDocumentMapper aiKnowledgeDocumentMapper;

    private final AiKnowledgeDocumentChunkMapper aiKnowledgeDocumentChunkMapper;

    private final AiKnowledgeBaseMapper aiKnowledgeBaseMapper;

    private final AiModelConfigService aiModelConfigService;

    private final AiKnowledgeDocumentSourceService aiKnowledgeDocumentSourceService;

    private final AiKnowledgeDocumentParseStrategyService aiKnowledgeDocumentParseStrategyService;

    private final AiKnowledgeEmbeddingProviderService embeddingProviderService;

    private final AiKnowledgeVectorStore vectorStore;

    /**
     * 执行文档解析、切片、向量化和向量库写入.
     */
    @Transactional(rollbackFor = Exception.class)
    public void indexDocument(Long documentId) {
        log.info("[AI知识库文档索引-开始] >> documentId={}", documentId);
        IndexStage stage = IndexStage.PARSE;
        AiKnowledgeDocumentEntity document = aiKnowledgeDocumentMapper.selectById(documentId);
        AiErrorEnum.INVALID_ID.assertNotNull(document);

        try {
            markProcessing(documentId);
            AiKnowledgeBaseEntity knowledgeBase = loadKnowledgeBase(document.getKnowledgeBaseId());
            AiKnowledgeEmbeddingContext embeddingContext = buildEmbeddingContext(knowledgeBase);

            AiKnowledgeDocumentSource source = aiKnowledgeDocumentSourceService.loadSource(document);
            List<AiKnowledgeDocumentChunkDraft> drafts = parseAndChunk(source);
            stage = IndexStage.EMBEDDING;

            vectorStore.deleteByDocumentId(documentId);
            replaceChunks(documentId, drafts);
            List<AiKnowledgeDocumentChunkEntity> chunks = loadDocumentChunks(documentId);
            List<AiKnowledgeVectorDocument> vectorDocuments = embedChunks(document, chunks, embeddingContext);
            vectorStore.upsert(AiKnowledgeVectorUpsertRequest.builder()
                    .context(embeddingContext)
                    .documents(vectorDocuments)
                    .build());

            markChunksSucceeded(chunks, embeddingContext, vectorDocuments);
            markDocumentSucceeded(document, chunks.size());
            refreshKnowledgeBaseStats(List.of(document.getKnowledgeBaseId()));
            log.info("[AI知识库文档索引-完成] >> documentId={}, chunkCount={}", documentId, chunks.size());
        } catch (Exception e) {
            log.warn("[AI知识库文档索引-失败] >> documentId={}, stage={}", documentId, stage, e);
            markDocumentFailed(documentId, stage, rootMessage(e));
            refreshKnowledgeBaseStats(List.of(document.getKnowledgeBaseId()));
        }
    }

    /**
     * 删除文档在向量库中的向量数据.
     */
    public void deleteDocumentVectors(Long documentId) {
        try {
            vectorStore.deleteByDocumentId(documentId);
        } catch (RuntimeException e) {
            log.warn("[AI知识库文档向量删除失败] >> documentId={}", documentId, e);
        }
    }

    private AiKnowledgeBaseEntity loadKnowledgeBase(Long knowledgeBaseId) {
        AiKnowledgeBaseEntity knowledgeBase = aiKnowledgeBaseMapper.selectById(knowledgeBaseId);
        AiErrorEnum.INVALID_ID.assertNotNull(knowledgeBase);
        return knowledgeBase;
    }

    private AiKnowledgeEmbeddingContext buildEmbeddingContext(AiKnowledgeBaseEntity knowledgeBase) {
        AiErrorEnum.INVALID_KNOWLEDGE_EMBEDDING_CONFIG.assertNotBlank(
                knowledgeBase.getEmbeddingModelConfigCode());
        AiModelConfigBO config = aiModelConfigService.getEnabledConfigByCode(
                knowledgeBase.getEmbeddingModelConfigCode(),
                true);
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

    private List<AiKnowledgeDocumentChunkDraft> parseAndChunk(AiKnowledgeDocumentSource source) {
        List<AiKnowledgeDocumentChunkDraft> drafts = aiKnowledgeDocumentParseStrategyService.parseAndChunk(
                source,
                AiKnowledgeDocumentChunkConfig.builder().build());
        if (CollUtil.isEmpty(drafts)) {
            throw new BusinessException(AiErrorEnum.KNOWLEDGE_DOCUMENT_PARSE_CONTENT_EMPTY);
        }
        return drafts;
    }

    private void replaceChunks(Long documentId, List<AiKnowledgeDocumentChunkDraft> drafts) {
        aiKnowledgeDocumentChunkMapper.delete(new QueryWrapper<AiKnowledgeDocumentChunkEntity>()
                .lambda()
                .eq(AiKnowledgeDocumentChunkEntity::getDocumentId, documentId));

        for (AiKnowledgeDocumentChunkDraft draft : drafts) {
            AiKnowledgeDocumentChunkEntity entity = new AiKnowledgeDocumentChunkEntity()
                    .setKnowledgeBaseId(draft.getKnowledgeBaseId())
                    .setDocumentId(documentId)
                    .setChunkNo(draft.getChunkNo())
                    .setContent(draft.getContent())
                    .setContentPreview(StrUtil.blankToDefault(draft.getContentPreview(), StrUtil.EMPTY))
                    .setSourcePage(draft.getSourcePage())
                    .setSourcePosition(clean(draft.getSourcePosition()))
                    .setTokenCount(draft.getTokenCount())
                    .setStatus(STATUS_SUCCESS)
                    .setEmbeddingStatus(STATUS_PROCESSING)
                    .setErrorMessage(StrUtil.EMPTY);
            aiKnowledgeDocumentChunkMapper.insert(entity);
        }
    }

    private List<AiKnowledgeDocumentChunkEntity> loadDocumentChunks(Long documentId) {
        return aiKnowledgeDocumentChunkMapper.selectList(new QueryWrapper<AiKnowledgeDocumentChunkEntity>()
                .lambda()
                .eq(AiKnowledgeDocumentChunkEntity::getDocumentId, documentId)
                .orderByAsc(AiKnowledgeDocumentChunkEntity::getChunkNo));
    }

    private List<AiKnowledgeVectorDocument> embedChunks(AiKnowledgeDocumentEntity document,
                                                        List<AiKnowledgeDocumentChunkEntity> chunks,
                                                        AiKnowledgeEmbeddingContext embeddingContext) {
        List<AiKnowledgeVectorDocument> vectorDocuments = new ArrayList<>(chunks.size());
        for (AiKnowledgeDocumentChunkEntity chunk : chunks) {
            AiKnowledgeEmbeddingResponse embedding = embeddingProviderService.embed(
                    AiKnowledgeEmbeddingRequest.builder()
                            .knowledgeBaseId(chunk.getKnowledgeBaseId())
                            .documentId(chunk.getDocumentId())
                            .chunkId(chunk.getId())
                            .chunkNo(chunk.getChunkNo())
                            .text(chunk.getContent())
                            .context(embeddingContext)
                            .build());

            vectorDocuments.add(AiKnowledgeVectorDocument.builder()
                    .knowledgeBaseId(chunk.getKnowledgeBaseId())
                    .documentId(chunk.getDocumentId())
                    .documentName(document.getDocumentName())
                    .chunkId(chunk.getId())
                    .chunkNo(chunk.getChunkNo())
                    .content(chunk.getContent())
                    .sourcePage(chunk.getSourcePage())
                    .sourcePosition(chunk.getSourcePosition())
                    .vectorId(String.valueOf(chunk.getId()))
                    .vectorHash(embedding.getVectorHash())
                    .vector(embedding.getVector())
                    .build());
        }
        return vectorDocuments;
    }

    private void markProcessing(Long documentId) {
        AiKnowledgeDocumentEntity updateEntity = new AiKnowledgeDocumentEntity()
                .setParseStatus(STATUS_PROCESSING)
                .setChunkStatus(STATUS_PROCESSING)
                .setEmbeddingStatus(STATUS_PROCESSING)
                .setParseErrorMessage(StrUtil.EMPTY)
                .setChunkErrorMessage(StrUtil.EMPTY);
        updateEntity.setId(documentId);
        aiKnowledgeDocumentMapper.updateById(updateEntity);
    }

    private void markChunksSucceeded(List<AiKnowledgeDocumentChunkEntity> chunks,
                                     AiKnowledgeEmbeddingContext embeddingContext,
                                     List<AiKnowledgeVectorDocument> vectorDocuments) {
        for (int i = 0; i < chunks.size(); i++) {
            AiKnowledgeDocumentChunkEntity chunk = chunks.get(i);
            AiKnowledgeVectorDocument vectorDocument = vectorDocuments.get(i);
            AiKnowledgeDocumentChunkEntity updateEntity = new AiKnowledgeDocumentChunkEntity()
                    .setEmbeddingStatus(STATUS_SUCCESS)
                    .setEmbeddingModelConfigId(embeddingContext.getModelConfigId())
                    .setEmbeddingModelConfigCode(embeddingContext.getModelConfigCode())
                    .setEmbeddingProviderType(embeddingContext.getProviderType())
                    .setEmbeddingModelName(embeddingContext.getModelName())
                    .setVectorId(vectorDocument.getVectorId())
                    .setVectorHash(vectorDocument.getVectorHash())
                    .setErrorMessage(StrUtil.EMPTY);
            updateEntity.setId(chunk.getId());
            aiKnowledgeDocumentChunkMapper.updateById(updateEntity);
        }
    }

    private void markDocumentSucceeded(AiKnowledgeDocumentEntity document, int chunkCount) {
        LocalDateTime now = LocalDateTime.now();
        AiKnowledgeDocumentEntity updateEntity = new AiKnowledgeDocumentEntity()
                .setParseStatus(STATUS_SUCCESS)
                .setChunkStatus(STATUS_SUCCESS)
                .setEmbeddingStatus(STATUS_SUCCESS)
                .setChunkCount(chunkCount)
                .setParseErrorMessage(StrUtil.EMPTY)
                .setChunkErrorMessage(StrUtil.EMPTY)
                .setParsedAt(now)
                .setChunkedAt(now);
        updateEntity.setId(document.getId());
        aiKnowledgeDocumentMapper.updateById(updateEntity);

        AiKnowledgeBaseEntity knowledgeBaseUpdate = new AiKnowledgeBaseEntity()
                .setLastParsedAt(now);
        knowledgeBaseUpdate.setId(document.getKnowledgeBaseId());
        aiKnowledgeBaseMapper.updateById(knowledgeBaseUpdate);
    }

    private void markDocumentFailed(Long documentId, IndexStage stage, String message) {
        String errorMessage = StrUtil.maxLength(
                StrUtil.blankToDefault(message, AiErrorEnum.INVALID_KNOWLEDGE_EMBEDDING_CONFIG.getLabel()),
                ERROR_MESSAGE_MAX_LENGTH);
        AiKnowledgeDocumentEntity updateEntity = switch (stage) {
            case PARSE -> new AiKnowledgeDocumentEntity()
                    .setParseStatus(STATUS_FAILED)
                    .setChunkStatus(STATUS_FAILED)
                    .setEmbeddingStatus(STATUS_FAILED)
                    .setParseErrorMessage(errorMessage)
                    .setChunkErrorMessage(errorMessage);
            case EMBEDDING -> new AiKnowledgeDocumentEntity()
                    .setParseStatus(STATUS_SUCCESS)
                    .setChunkStatus(STATUS_SUCCESS)
                    .setEmbeddingStatus(STATUS_FAILED)
                    .setParseErrorMessage(StrUtil.EMPTY)
                    .setChunkErrorMessage(errorMessage);
        };
        updateEntity.setId(documentId);
        aiKnowledgeDocumentMapper.updateById(updateEntity);

        AiKnowledgeDocumentChunkEntity chunkUpdate = new AiKnowledgeDocumentChunkEntity()
                .setEmbeddingStatus(STATUS_FAILED)
                .setErrorMessage(errorMessage);
        aiKnowledgeDocumentChunkMapper.update(
                chunkUpdate,
                new QueryWrapper<AiKnowledgeDocumentChunkEntity>()
                        .lambda()
                        .eq(AiKnowledgeDocumentChunkEntity::getDocumentId, documentId));
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

    private String clean(String value) {
        return CharSequenceUtil.cleanBlank(value);
    }

    private String rootMessage(Throwable e) {
        Throwable root = e;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return StrUtil.blankToDefault(root.getMessage(), root.getClass().getSimpleName());
    }

    private enum IndexStage {
        PARSE,
        EMBEDDING
    }
}
