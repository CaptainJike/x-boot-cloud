package io.github.module.ai.service.vector;

import com.google.common.util.concurrent.Futures;
import io.github.framework.core.context.TenantContext;
import io.github.framework.core.context.TenantContextHolder;
import io.github.framework.core.exception.BusinessException;
import io.github.module.ai.config.AiKnowledgeVectorStoreProperties;
import io.github.module.ai.service.model.AiKnowledgeEmbeddingContext;
import io.github.module.ai.service.model.AiKnowledgeVectorDocument;
import io.github.module.ai.service.model.AiKnowledgeVectorSearchHit;
import io.github.module.ai.service.model.AiKnowledgeVectorSearchRequest;
import io.qdrant.client.PointIdFactory;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Common.Filter;
import io.qdrant.client.grpc.JsonWithInt.Value;
import io.qdrant.client.grpc.Points.ScoredPoint;
import io.qdrant.client.grpc.Points.SearchPoints;
import io.qdrant.client.grpc.Points.UpdateResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QdrantAiKnowledgeVectorStoreTest {

    @Mock
    private QdrantClient qdrantClient;

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void collectionNameUsesPrefixModelConfigAndDimension() {
        QdrantAiKnowledgeVectorStore store = newStore(properties());

        String collectionName = store.collectionName(AiKnowledgeEmbeddingContext.builder()
                .modelConfigId(7L)
                .build(), 1536);

        assertThat(collectionName).isEqualTo("x_boot_ai_knowledge_7_1536");
    }

    @Test
    void payloadContainsTenantAndDocumentMetadata() {
        QdrantAiKnowledgeVectorStore store = newStore(properties());

        Map<String, Value> payload = store.payload(vectorDocument(), 10L);

        assertThat(payload.get("tenantId").getIntegerValue()).isEqualTo(10L);
        assertThat(payload.get("knowledgeBaseId").getIntegerValue()).isEqualTo(1L);
        assertThat(payload.get("documentId").getIntegerValue()).isEqualTo(2L);
        assertThat(payload.get("chunkId").getIntegerValue()).isEqualTo(3L);
        assertThat(payload.get("chunkNo").getIntegerValue()).isEqualTo(4L);
        assertThat(payload.get("documentName").getStringValue()).isEqualTo("制度.md");
        assertThat(payload.get("sourcePage").getIntegerValue()).isEqualTo(5L);
        assertThat(payload.get("sourcePosition").getStringValue()).isEqualTo("paragraph:1");
        assertThat(payload.get("vectorHash").getStringValue()).isEqualTo("hash");
        assertThat(payload.get("content").getStringValue()).isEqualTo("制度内容");
    }

    @Test
    void payloadOmitsTenantWhenTenantContextAbsent() {
        QdrantAiKnowledgeVectorStore store = newStore(properties());

        Map<String, Value> payload = store.payload(vectorDocument(), null);

        assertThat(payload).doesNotContainKey("tenantId");
        assertThat(payload.get("knowledgeBaseId").getIntegerValue()).isEqualTo(1L);
    }

    @Test
    void searchFilterRequiresTenantAndKnowledgeBases() {
        QdrantAiKnowledgeVectorStore store = newStore(properties());

        Filter filter = store.searchFilter(10L, List.of(1L, 2L));

        assertThat(filter.getMustCount()).isEqualTo(2);
        assertThat(filter.getMust(0).getField().getKey()).isEqualTo("tenantId");
        assertThat(filter.getMust(1).getField().getKey()).isEqualTo("knowledgeBaseId");
    }

    @Test
    void searchFilterOmitsTenantWhenTenantContextAbsent() {
        QdrantAiKnowledgeVectorStore store = newStore(properties());

        Filter filter = store.searchFilter(null, List.of(1L, 2L));

        assertThat(filter.getMustCount()).isEqualTo(1);
        assertThat(filter.getMust(0).getField().getKey()).isEqualTo("knowledgeBaseId");
    }

    @Test
    void toFloatsRejectsInvalidValues() {
        QdrantAiKnowledgeVectorStore store = newStore(properties());

        assertThat(store.toFloats(List.of(0.1D, 0.2D))).containsExactly(0.1F, 0.2F);
        assertThatThrownBy(() -> store.toFloats(List.of(Double.NaN)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void toSearchHitMapsPayloadAndScore() {
        QdrantAiKnowledgeVectorStore store = newStore(properties());
        ScoredPoint scoredPoint = ScoredPoint.newBuilder()
                .setId(PointIdFactory.id(3L))
                .setScore(0.88F)
                .putAllPayload(store.payload(vectorDocument(), 10L))
                .build();

        AiKnowledgeVectorSearchHit hit = store.toSearchHit(scoredPoint);

        assertThat(hit.getSimilarityScore()).isCloseTo(0.88D, within(0.000001D));
        assertThat(hit.getDocument().getKnowledgeBaseId()).isEqualTo(1L);
        assertThat(hit.getDocument().getDocumentId()).isEqualTo(2L);
        assertThat(hit.getDocument().getChunkId()).isEqualTo(3L);
        assertThat(hit.getDocument().getVectorId()).isEqualTo("3");
        assertThat(hit.getDocument().getContent()).isEqualTo("制度内容");
    }

    @Test
    void searchPassesTenantKnowledgeBaseFilterAndScoreThreshold() {
        TenantContextHolder.setTenantContext(new TenantContext().setTenantId(10L));
        AiKnowledgeVectorStoreProperties properties = properties();
        QdrantAiKnowledgeVectorStore store = newStore(properties);
        ScoredPoint scoredPoint = ScoredPoint.newBuilder()
                .setId(PointIdFactory.id(3L))
                .setScore(0.88F)
                .putAllPayload(store.payload(vectorDocument(), 10L))
                .build();
        when(qdrantClient.collectionExistsAsync(anyString(), any(Duration.class)))
                .thenReturn(Futures.immediateFuture(true));
        when(qdrantClient.searchAsync(any(SearchPoints.class), any(Duration.class)))
                .thenReturn(Futures.immediateFuture(List.of(scoredPoint)));

        List<AiKnowledgeVectorSearchHit> hits = store.search(AiKnowledgeVectorSearchRequest.builder()
                .knowledgeBaseIds(List.of(1L, 2L))
                .queryVector(List.of(0.1D, 0.2D, 0.3D))
                .topK(8)
                .similarityThreshold(0.7D)
                .context(AiKnowledgeEmbeddingContext.builder().modelConfigId(9L).build())
                .build());

        assertThat(hits).hasSize(1);
        ArgumentCaptor<SearchPoints> captor = ArgumentCaptor.forClass(SearchPoints.class);
        verify(qdrantClient).searchAsync(captor.capture(), any(Duration.class));
        assertThat(captor.getValue().getCollectionName()).isEqualTo("x_boot_ai_knowledge_9_3");
        assertThat(captor.getValue().getLimit()).isEqualTo(8);
        assertThat(captor.getValue().getScoreThreshold()).isEqualTo(0.7F);
        assertThat(captor.getValue().getFilter().getMustCount()).isEqualTo(2);
    }

    @Test
    void searchWithoutTenantContextFiltersKnowledgeBaseOnly() {
        QdrantAiKnowledgeVectorStore store = newStore(properties());
        ScoredPoint scoredPoint = ScoredPoint.newBuilder()
                .setId(PointIdFactory.id(3L))
                .setScore(0.88F)
                .putAllPayload(store.payload(vectorDocument(), null))
                .build();
        when(qdrantClient.collectionExistsAsync(anyString(), any(Duration.class)))
                .thenReturn(Futures.immediateFuture(true));
        when(qdrantClient.searchAsync(any(SearchPoints.class), any(Duration.class)))
                .thenReturn(Futures.immediateFuture(List.of(scoredPoint)));

        List<AiKnowledgeVectorSearchHit> hits = store.search(AiKnowledgeVectorSearchRequest.builder()
                .knowledgeBaseIds(List.of(1L, 2L))
                .queryVector(List.of(0.1D, 0.2D, 0.3D))
                .topK(8)
                .similarityThreshold(0.7D)
                .context(AiKnowledgeEmbeddingContext.builder().modelConfigId(9L).build())
                .build());

        assertThat(hits).hasSize(1);
        ArgumentCaptor<SearchPoints> captor = ArgumentCaptor.forClass(SearchPoints.class);
        verify(qdrantClient).searchAsync(captor.capture(), any(Duration.class));
        assertThat(captor.getValue().getFilter().getMustCount()).isEqualTo(1);
        assertThat(captor.getValue().getFilter().getMust(0).getField().getKey()).isEqualTo("knowledgeBaseId");
    }

    @Test
    void deleteByDocumentIdScansManagedCollectionsOnly() {
        TenantContextHolder.setTenantContext(new TenantContext().setTenantId(10L));
        QdrantAiKnowledgeVectorStore store = newStore(properties());
        when(qdrantClient.listCollectionsAsync(any(Duration.class)))
                .thenReturn(Futures.immediateFuture(List.of("x_boot_ai_knowledge_1_3", "other")));
        when(qdrantClient.deleteAsync(anyString(), any(Filter.class), any(Duration.class)))
                .thenReturn(Futures.immediateFuture(UpdateResult.getDefaultInstance()));

        store.deleteByDocumentId(2L);

        ArgumentCaptor<Filter> filterCaptor = ArgumentCaptor.forClass(Filter.class);
        verify(qdrantClient).deleteAsync(anyString(), filterCaptor.capture(), any(Duration.class));
        assertThat(filterCaptor.getValue().getMust(0).getField().getKey()).isEqualTo("tenantId");
        assertThat(filterCaptor.getValue().getMust(1).getField().getKey()).isEqualTo("documentId");
    }

    @Test
    void deleteByDocumentIdWithoutTenantFiltersDocumentOnly() {
        QdrantAiKnowledgeVectorStore store = newStore(properties());
        when(qdrantClient.listCollectionsAsync(any(Duration.class)))
                .thenReturn(Futures.immediateFuture(List.of("x_boot_ai_knowledge_1_3", "other")));
        when(qdrantClient.deleteAsync(anyString(), any(Filter.class), any(Duration.class)))
                .thenReturn(Futures.immediateFuture(UpdateResult.getDefaultInstance()));

        store.deleteByDocumentId(2L);

        ArgumentCaptor<Filter> filterCaptor = ArgumentCaptor.forClass(Filter.class);
        verify(qdrantClient).deleteAsync(anyString(), filterCaptor.capture(), any(Duration.class));
        assertThat(filterCaptor.getValue().getMustCount()).isEqualTo(1);
        assertThat(filterCaptor.getValue().getMust(0).getField().getKey()).isEqualTo("documentId");
    }

    private QdrantAiKnowledgeVectorStore newStore(AiKnowledgeVectorStoreProperties properties) {
        return new QdrantAiKnowledgeVectorStore(qdrantClient, properties);
    }

    private AiKnowledgeVectorStoreProperties properties() {
        AiKnowledgeVectorStoreProperties properties = new AiKnowledgeVectorStoreProperties();
        properties.setTimeout(Duration.ofMillis(10));
        return properties;
    }

    private AiKnowledgeVectorDocument vectorDocument() {
        return AiKnowledgeVectorDocument.builder()
                .knowledgeBaseId(1L)
                .documentId(2L)
                .documentName("制度.md")
                .chunkId(3L)
                .chunkNo(4)
                .content("制度内容")
                .sourcePage(5)
                .sourcePosition("paragraph:1")
                .vectorHash("hash")
                .vector(List.of(0.1D, 0.2D, 0.3D))
                .build();
    }
}
