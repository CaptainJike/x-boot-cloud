package io.github.module.ai.service.vector;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.util.concurrent.Futures;
import io.github.framework.core.context.TenantContextHolder;
import io.github.framework.core.exception.BusinessException;
import io.github.module.ai.config.AiKnowledgeVectorStoreProperties;
import io.github.module.ai.enums.AiErrorEnum;
import io.github.module.ai.service.model.AiKnowledgeEmbeddingContext;
import io.github.module.ai.service.model.AiKnowledgeVectorDocument;
import io.github.module.ai.service.model.AiKnowledgeVectorSearchHit;
import io.github.module.ai.service.model.AiKnowledgeVectorSearchRequest;
import io.github.module.ai.service.model.AiKnowledgeVectorUpsertRequest;
import io.qdrant.client.ConditionFactory;
import io.qdrant.client.PointIdFactory;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.ValueFactory;
import io.qdrant.client.VectorsFactory;
import io.qdrant.client.WithPayloadSelectorFactory;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.PayloadSchemaType;
import io.qdrant.client.grpc.Collections.VectorParams;
import io.qdrant.client.grpc.Common.Filter;
import io.qdrant.client.grpc.Common.PointId;
import io.qdrant.client.grpc.JsonWithInt.Value;
import io.qdrant.client.grpc.Points.PointStruct;
import io.qdrant.client.grpc.Points.ScoredPoint;
import io.qdrant.client.grpc.Points.SearchPoints;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Qdrant 知识库向量存储实现.
 */
@RequiredArgsConstructor
@Slf4j
public class QdrantAiKnowledgeVectorStore implements AiKnowledgeVectorStore {

    private static final String PAYLOAD_TENANT_ID = "tenantId";

    private static final String PAYLOAD_KNOWLEDGE_BASE_ID = "knowledgeBaseId";

    private static final String PAYLOAD_DOCUMENT_ID = "documentId";

    private static final String PAYLOAD_CHUNK_ID = "chunkId";

    private static final String PAYLOAD_CHUNK_NO = "chunkNo";

    private static final String PAYLOAD_DOCUMENT_NAME = "documentName";

    private static final String PAYLOAD_SOURCE_PAGE = "sourcePage";

    private static final String PAYLOAD_SOURCE_POSITION = "sourcePosition";

    private static final String PAYLOAD_VECTOR_HASH = "vectorHash";

    private static final String PAYLOAD_CONTENT = "content";

    private static final int DEFAULT_TOP_K = 5;

    private final QdrantClient qdrantClient;

    private final AiKnowledgeVectorStoreProperties properties;

    private final Set<String> initializedCollections = ConcurrentHashMap.newKeySet();

    @Override
    public String storeType() {
        return "QDRANT";
    }

    @Override
    public void upsert(AiKnowledgeVectorUpsertRequest request) throws BusinessException {
        try {
            validateUpsertRequest(request);
            Long tenantId = currentTenantId();
            AiKnowledgeEmbeddingContext context = request.getContext();
            int dimension = resolveDimension(request.getDocuments());
            String collectionName = collectionName(context, dimension);
            ensureCollection(collectionName, dimension);

            List<PointStruct> points = request.getDocuments().stream()
                    .map(document -> toPoint(document, tenantId))
                    .toList();
            Futures.getUnchecked(qdrantClient.upsertAsync(collectionName, points, properties.getTimeout()));
        } catch (BusinessException e) {
            throw e;
        } catch (RuntimeException e) {
            throw vectorStoreUnavailable(e);
        }
    }

    @Override
    public void deleteByDocumentId(Long documentId) throws BusinessException {
        if (documentId == null) {
            throw new BusinessException(AiErrorEnum.INVALID_ID);
        }
        try {
            Long tenantId = currentTenantId();
            Filter filter = documentDeleteFilter(tenantId, documentId);
            List<String> collectionNames = Futures.getUnchecked(qdrantClient.listCollectionsAsync(properties.getTimeout()));
            String prefix = collectionPrefix() + "_";
            for (String collectionName : collectionNames) {
                if (StrUtil.startWith(collectionName, prefix)) {
                    Futures.getUnchecked(qdrantClient.deleteAsync(collectionName, filter, properties.getTimeout()));
                }
            }
        } catch (BusinessException e) {
            throw e;
        } catch (RuntimeException e) {
            throw vectorStoreUnavailable(e);
        }
    }

    @Override
    public List<AiKnowledgeVectorSearchHit> search(AiKnowledgeVectorSearchRequest request)
            throws BusinessException {
        try {
            validateSearchRequest(request);
            Long tenantId = currentTenantId();
            int dimension = request.getQueryVector().size();
            String collectionName = collectionName(request.getContext(), dimension);
            if (!collectionExists(collectionName)) {
                return Collections.emptyList();
            }

            SearchPoints searchPoints = SearchPoints.newBuilder()
                    .setCollectionName(collectionName)
                    .addAllVector(toFloats(request.getQueryVector()))
                    .setLimit(resolveTopK(request.getTopK()))
                    .setFilter(searchFilter(tenantId, request.getKnowledgeBaseIds()))
                    .setWithPayload(WithPayloadSelectorFactory.enable(true))
                    .setScoreThreshold(resolveThreshold(request.getSimilarityThreshold()))
                    .build();
            List<ScoredPoint> scoredPoints = Futures.getUnchecked(
                    qdrantClient.searchAsync(searchPoints, properties.getTimeout()));

            return scoredPoints.stream()
                    .map(this::toSearchHit)
                    .filter(Objects::nonNull)
                    .filter(hit -> hit.getSimilarityScore() == null
                            || hit.getSimilarityScore() >= resolveThreshold(request.getSimilarityThreshold()))
                    .toList();
        } catch (BusinessException e) {
            throw e;
        } catch (RuntimeException e) {
            throw vectorStoreUnavailable(e);
        }
    }

    String collectionName(AiKnowledgeEmbeddingContext context, int dimension) {
        if (context == null || context.getModelConfigId() == null || dimension <= 0) {
            throw new BusinessException(AiErrorEnum.INVALID_KNOWLEDGE_EMBEDDING_CONFIG);
        }

        return collectionPrefix() + "_" + context.getModelConfigId() + "_" + dimension;
    }

    Map<String, Value> payload(AiKnowledgeVectorDocument document, Long tenantId) {
        Map<String, Value> payload = new LinkedHashMap<>();
        putLong(payload, PAYLOAD_TENANT_ID, tenantId);
        putLong(payload, PAYLOAD_KNOWLEDGE_BASE_ID, document.getKnowledgeBaseId());
        putLong(payload, PAYLOAD_DOCUMENT_ID, document.getDocumentId());
        putLong(payload, PAYLOAD_CHUNK_ID, document.getChunkId());
        putLong(payload, PAYLOAD_CHUNK_NO, toLong(document.getChunkNo()));
        putString(payload, PAYLOAD_DOCUMENT_NAME, document.getDocumentName());
        putLong(payload, PAYLOAD_SOURCE_PAGE, toLong(document.getSourcePage()));
        putString(payload, PAYLOAD_SOURCE_POSITION, document.getSourcePosition());
        putString(payload, PAYLOAD_VECTOR_HASH, document.getVectorHash());
        putString(payload, PAYLOAD_CONTENT, document.getContent());
        return payload;
    }

    Filter searchFilter(Long tenantId, List<Long> knowledgeBaseIds) {
        Filter.Builder builder = Filter.newBuilder();
        if (tenantId != null) {
            builder.addMust(ConditionFactory.match(PAYLOAD_TENANT_ID, tenantId));
        }
        builder.addMust(ConditionFactory.matchValues(PAYLOAD_KNOWLEDGE_BASE_ID, knowledgeBaseIds));
        return builder.build();
    }

    List<Float> toFloats(List<Double> values) {
        if (CollUtil.isEmpty(values)) {
            throw new BusinessException(AiErrorEnum.INVALID_KNOWLEDGE_EMBEDDING_CONFIG);
        }
        List<Float> floats = new ArrayList<>(values.size());
        for (Double value : values) {
            if (value == null || !Double.isFinite(value)) {
                throw new BusinessException(AiErrorEnum.INVALID_KNOWLEDGE_EMBEDDING_CONFIG);
            }
            floats.add(value.floatValue());
        }
        return floats;
    }

    private void validateUpsertRequest(AiKnowledgeVectorUpsertRequest request) {
        if (request == null || request.getContext() == null || CollUtil.isEmpty(request.getDocuments())) {
            throw new BusinessException(AiErrorEnum.INVALID_KNOWLEDGE_EMBEDDING_CONFIG);
        }
        for (AiKnowledgeVectorDocument document : request.getDocuments()) {
            if (document == null
                    || document.getKnowledgeBaseId() == null
                    || document.getDocumentId() == null
                    || document.getChunkId() == null
                    || CollUtil.isEmpty(document.getVector())) {
                throw new BusinessException(AiErrorEnum.INVALID_KNOWLEDGE_EMBEDDING_CONFIG);
            }
        }
    }

    private void validateSearchRequest(AiKnowledgeVectorSearchRequest request) {
        if (request == null
                || request.getContext() == null
                || CollUtil.isEmpty(request.getKnowledgeBaseIds())
                || CollUtil.isEmpty(request.getQueryVector())) {
            throw new BusinessException(AiErrorEnum.INVALID_KNOWLEDGE_EMBEDDING_CONFIG);
        }
    }

    private int resolveDimension(List<AiKnowledgeVectorDocument> documents) {
        int dimension = documents.getFirst().getVector().size();
        boolean mixedDimension = documents.stream()
                .anyMatch(document -> document.getVector().size() != dimension);
        if (mixedDimension) {
            throw new BusinessException(AiErrorEnum.INVALID_KNOWLEDGE_EMBEDDING_CONFIG);
        }
        return dimension;
    }

    private PointStruct toPoint(AiKnowledgeVectorDocument document, Long tenantId) {
        return PointStruct.newBuilder()
                .setId(PointIdFactory.id(document.getChunkId()))
                .setVectors(VectorsFactory.vectors(toFloats(document.getVector())))
                .putAllPayload(payload(document, tenantId))
                .build();
    }

    private void ensureCollection(String collectionName, int dimension) {
        if (!properties.isInitializeSchema()) {
            return;
        }
        if (!initializedCollections.add(collectionName)) {
            return;
        }

        try {
            if (!collectionExists(collectionName)) {
                VectorParams vectorParams = VectorParams.newBuilder()
                        .setSize(dimension)
                        .setDistance(distance())
                        .build();
                Futures.getUnchecked(qdrantClient.createCollectionAsync(
                        collectionName,
                        vectorParams,
                        properties.getTimeout()));
            }
            createPayloadIndexesBestEffort(collectionName);
        } catch (RuntimeException e) {
            initializedCollections.remove(collectionName);
            throw e;
        }
    }

    private boolean collectionExists(String collectionName) {
        return Futures.getUnchecked(qdrantClient.collectionExistsAsync(collectionName, properties.getTimeout()));
    }

    private void createPayloadIndexesBestEffort(String collectionName) {
        createPayloadIndexBestEffort(collectionName, PAYLOAD_TENANT_ID, PayloadSchemaType.Integer);
        createPayloadIndexBestEffort(collectionName, PAYLOAD_KNOWLEDGE_BASE_ID, PayloadSchemaType.Integer);
        createPayloadIndexBestEffort(collectionName, PAYLOAD_DOCUMENT_ID, PayloadSchemaType.Integer);
        createPayloadIndexBestEffort(collectionName, PAYLOAD_CHUNK_ID, PayloadSchemaType.Integer);
        createPayloadIndexBestEffort(collectionName, PAYLOAD_VECTOR_HASH, PayloadSchemaType.Keyword);
    }

    private void createPayloadIndexBestEffort(String collectionName,
                                              String fieldName,
                                              PayloadSchemaType schemaType) {
        try {
            Futures.getUnchecked(qdrantClient.createPayloadIndexAsync(
                    collectionName,
                    fieldName,
                    schemaType,
                    null,
                    true,
                    null,
                    properties.getTimeout()));
        } catch (RuntimeException e) {
            log.debug("[Qdrant创建Payload索引跳过] >> collectionName={}, fieldName={}",
                    collectionName,
                    fieldName,
                    e);
        }
    }

    private Filter documentDeleteFilter(Long tenantId, Long documentId) {
        Filter.Builder builder = Filter.newBuilder();
        if (tenantId != null) {
            builder.addMust(ConditionFactory.match(PAYLOAD_TENANT_ID, tenantId));
        }
        builder.addMust(ConditionFactory.match(PAYLOAD_DOCUMENT_ID, documentId));
        return builder.build();
    }

    AiKnowledgeVectorSearchHit toSearchHit(ScoredPoint scoredPoint) {
        if (scoredPoint == null) {
            return null;
        }
        Map<String, Value> payload = scoredPoint.getPayloadMap();
        AiKnowledgeVectorDocument document = AiKnowledgeVectorDocument.builder()
                .knowledgeBaseId(toLong(payload.get(PAYLOAD_KNOWLEDGE_BASE_ID)))
                .documentId(toLong(payload.get(PAYLOAD_DOCUMENT_ID)))
                .chunkId(toLong(payload.get(PAYLOAD_CHUNK_ID)))
                .chunkNo(toInteger(payload.get(PAYLOAD_CHUNK_NO)))
                .documentName(toStringValue(payload.get(PAYLOAD_DOCUMENT_NAME)))
                .sourcePage(toInteger(payload.get(PAYLOAD_SOURCE_PAGE)))
                .sourcePosition(toStringValue(payload.get(PAYLOAD_SOURCE_POSITION)))
                .vectorHash(toStringValue(payload.get(PAYLOAD_VECTOR_HASH)))
                .content(toStringValue(payload.get(PAYLOAD_CONTENT)))
                .vectorId(pointId(scoredPoint.getId()))
                .build();

        return AiKnowledgeVectorSearchHit.builder()
                .document(document)
                .similarityScore((double) scoredPoint.getScore())
                .build();
    }

    private Distance distance() {
        String configured = StrUtil.blankToDefault(properties.getDistance(), "Cosine")
                .trim()
                .toUpperCase(Locale.ROOT);
        return switch (configured) {
            case "EUCLID", "EUCLIDEAN" -> Distance.Euclid;
            case "DOT" -> Distance.Dot;
            case "MANHATTAN" -> Distance.Manhattan;
            case "COSINE" -> Distance.Cosine;
            default -> throw new BusinessException(AiErrorEnum.INVALID_KNOWLEDGE_EMBEDDING_CONFIG);
        };
    }

    private String collectionPrefix() {
        String prefix = StrUtil.blankToDefault(properties.getCollectionPrefix(), "x_boot_ai_knowledge");
        return prefix.replaceAll("[^A-Za-z0-9_\\-]", "_");
    }

    private Long currentTenantId() {
        return TenantContextHolder.getTenantId();
    }

    private int resolveTopK(Integer topK) {
        return topK == null || topK <= 0 ? DEFAULT_TOP_K : topK;
    }

    private float resolveThreshold(Double threshold) {
        if (threshold == null) {
            return 0.0F;
        }
        if (threshold < 0) {
            throw new BusinessException(AiErrorEnum.INVALID_KNOWLEDGE_EMBEDDING_CONFIG);
        }
        return threshold.floatValue();
    }

    private void putString(Map<String, Value> payload, String key, String value) {
        if (CharSequenceUtil.isNotBlank(value)) {
            payload.put(key, ValueFactory.value(value));
        }
    }

    private void putLong(Map<String, Value> payload, String key, Long value) {
        if (value != null) {
            payload.put(key, ValueFactory.value(value));
        }
    }

    private Long toLong(Integer value) {
        return value == null ? null : value.longValue();
    }

    private Long toLong(Value value) {
        if (value == null) {
            return null;
        }
        if (value.hasIntegerValue()) {
            return value.getIntegerValue();
        }
        if (value.hasDoubleValue()) {
            return (long) value.getDoubleValue();
        }
        if (value.hasStringValue()) {
            try {
                return Long.valueOf(value.getStringValue());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private Integer toInteger(Value value) {
        Long longValue = toLong(value);
        return longValue == null ? null : longValue.intValue();
    }

    private String toStringValue(Value value) {
        if (value == null) {
            return null;
        }
        if (value.hasStringValue()) {
            return value.getStringValue();
        }
        if (value.hasIntegerValue()) {
            return String.valueOf(value.getIntegerValue());
        }
        if (value.hasDoubleValue()) {
            return String.valueOf(value.getDoubleValue());
        }
        if (value.hasBoolValue()) {
            return String.valueOf(value.getBoolValue());
        }
        return null;
    }

    private String pointId(PointId pointId) {
        if (pointId == null) {
            return null;
        }
        if (pointId.hasNum()) {
            return String.valueOf(pointId.getNum());
        }
        if (pointId.hasUuid()) {
            return pointId.getUuid();
        }
        return null;
    }

    private BusinessException vectorStoreUnavailable(RuntimeException e) {
        return new BusinessException(
                AiErrorEnum.KNOWLEDGE_VECTOR_STORE_UNAVAILABLE.getValue(),
                AiErrorEnum.KNOWLEDGE_VECTOR_STORE_UNAVAILABLE.getLabel() + "：" + rootMessage(e));
    }

    private String rootMessage(Throwable e) {
        Throwable root = e;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return StrUtil.blankToDefault(root.getMessage(), root.getClass().getSimpleName());
    }
}
