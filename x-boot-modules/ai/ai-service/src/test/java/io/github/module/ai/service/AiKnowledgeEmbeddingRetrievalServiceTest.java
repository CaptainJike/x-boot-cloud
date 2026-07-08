package io.github.module.ai.service;

import io.github.framework.core.exception.BusinessException;
import io.github.module.ai.service.embedding.AiKnowledgeEmbeddingProvider;
import io.github.module.ai.service.embedding.AiKnowledgeEmbeddingProviderService;
import io.github.module.ai.service.model.AiKnowledgeEmbeddingContext;
import io.github.module.ai.service.model.AiKnowledgeEmbeddingRequest;
import io.github.module.ai.service.model.AiKnowledgeEmbeddingResponse;
import io.github.module.ai.service.model.AiKnowledgeVectorDocument;
import io.github.module.ai.service.model.AiKnowledgeVectorSearchHit;
import io.github.module.ai.service.model.AiKnowledgeVectorSearchRequest;
import io.github.module.ai.service.model.AiKnowledgeVectorUpsertRequest;
import io.github.module.ai.service.vector.AiKnowledgeVectorStore;
import io.github.module.ai.service.vector.UnavailableAiKnowledgeVectorStore;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiKnowledgeEmbeddingRetrievalServiceTest {

    @Test
    void embeddingProviderServiceSelectsMatchingProviderAndCompletesMetadata() {
        TestEmbeddingProvider unsupportedProvider = new TestEmbeddingProvider("OLLAMA", List.of(9.9D));
        TestEmbeddingProvider supportedProvider = new TestEmbeddingProvider("OPENAI_COMPATIBLE", List.of(0.1D, 0.2D));
        AiKnowledgeEmbeddingProviderService service =
                new AiKnowledgeEmbeddingProviderService(List.of(unsupportedProvider, supportedProvider));

        AiKnowledgeEmbeddingResponse response = service.embed(AiKnowledgeEmbeddingRequest.builder()
                .knowledgeBaseId(1L)
                .documentId(2L)
                .chunkId(3L)
                .chunkNo(4)
                .text("制度内容")
                .context(context())
                .build());

        assertThat(unsupportedProvider.getCallCount()).isZero();
        assertThat(supportedProvider.getCallCount()).isEqualTo(1);
        assertThat(response.getKnowledgeBaseId()).isEqualTo(1L);
        assertThat(response.getDocumentId()).isEqualTo(2L);
        assertThat(response.getChunkId()).isEqualTo(3L);
        assertThat(response.getDimensions()).isEqualTo(2);
        assertThat(response.getProviderType()).isEqualTo("OPENAI_COMPATIBLE");
        assertThat(response.getModelName()).isEqualTo("text-embedding-v1");
        assertThat(response.getVectorHash()).isNotBlank();
    }

    @Test
    void embeddingProviderServiceRejectsUnsupportedProvider() {
        AiKnowledgeEmbeddingProviderService service = new AiKnowledgeEmbeddingProviderService(List.of());

        assertThatThrownBy(() -> service.embed(AiKnowledgeEmbeddingRequest.builder()
                .text("制度内容")
                .context(context())
                .build()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("不支持的知识库向量化供应商");
    }

    @Test
    void embeddingProviderServiceRejectsInvalidConfig() {
        AiKnowledgeEmbeddingProviderService service = new AiKnowledgeEmbeddingProviderService(List.of());

        assertThatThrownBy(() -> service.embed(AiKnowledgeEmbeddingRequest.builder()
                .text(" ")
                .context(context())
                .build()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("无效知识库向量化配置");
    }

    @Test
    void vectorRetrievalServiceEmbedsQueryAndSearchesVectorStore() {
        AiKnowledgeEmbeddingProviderService embeddingProviderService =
                new AiKnowledgeEmbeddingProviderService(List.of(
                        new TestEmbeddingProvider("OPENAI_COMPATIBLE", List.of(0.3D, 0.7D))
                ));
        TestVectorStore vectorStore = new TestVectorStore();
        AiKnowledgeVectorRetrievalService retrievalService =
                new AiKnowledgeVectorRetrievalService(embeddingProviderService, vectorStore);

        List<AiKnowledgeVectorSearchHit> hits = retrievalService.search(AiKnowledgeVectorSearchRequest.builder()
                .knowledgeBaseIds(List.of(1L, 2L))
                .query("休假制度")
                .context(context())
                .build());

        assertThat(hits).hasSize(1);
        assertThat(hits.getFirst().getSimilarityScore()).isEqualTo(0.91D);
        assertThat(vectorStore.getLastSearchRequest().getKnowledgeBaseIds()).containsExactly(1L, 2L);
        assertThat(vectorStore.getLastSearchRequest().getQueryVector()).containsExactly(0.3D, 0.7D);
        assertThat(vectorStore.getLastSearchRequest().getTopK()).isEqualTo(5);
        assertThat(vectorStore.getLastSearchRequest().getSimilarityThreshold()).isEqualTo(0.0D);
    }

    @Test
    void unavailableVectorStoreFailsFast() {
        AiKnowledgeVectorStore vectorStore = new UnavailableAiKnowledgeVectorStore();

        assertThatThrownBy(() -> vectorStore.search(AiKnowledgeVectorSearchRequest.builder()
                .knowledgeBaseIds(List.of(1L))
                .query("休假制度")
                .queryVector(List.of(0.1D))
                .context(context())
                .build()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("知识库向量存储不可用");
    }

    private AiKnowledgeEmbeddingContext context() {
        return AiKnowledgeEmbeddingContext.builder()
                .modelConfigId(9L)
                .modelConfigCode("embedding-default")
                .providerType("OPENAI_COMPATIBLE")
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .apiKey("sk-test")
                .modelName("text-embedding-v1")
                .timeoutSeconds(30L)
                .build();
    }

    private static class TestEmbeddingProvider implements AiKnowledgeEmbeddingProvider {

        private final String supportedProviderType;

        private final List<Double> vector;

        private int callCount;

        TestEmbeddingProvider(String supportedProviderType, List<Double> vector) {
            this.supportedProviderType = supportedProviderType;
            this.vector = vector;
        }

        @Override
        public boolean supports(AiKnowledgeEmbeddingContext context) {
            return supportedProviderType.equals(context.getProviderType());
        }

        @Override
        public AiKnowledgeEmbeddingResponse embed(AiKnowledgeEmbeddingRequest request) {
            callCount++;
            return AiKnowledgeEmbeddingResponse.builder()
                    .vector(vector)
                    .build();
        }

        int getCallCount() {
            return callCount;
        }
    }

    private static class TestVectorStore implements AiKnowledgeVectorStore {

        private AiKnowledgeVectorSearchRequest lastSearchRequest;

        @Override
        public String storeType() {
            return "TEST";
        }

        @Override
        public void upsert(AiKnowledgeVectorUpsertRequest request) {
        }

        @Override
        public void deleteByDocumentId(Long documentId) {
        }

        @Override
        public List<AiKnowledgeVectorSearchHit> search(AiKnowledgeVectorSearchRequest request) {
            lastSearchRequest = request;
            return List.of(AiKnowledgeVectorSearchHit.builder()
                    .document(AiKnowledgeVectorDocument.builder()
                            .knowledgeBaseId(1L)
                            .documentId(2L)
                            .chunkId(3L)
                            .content("休假制度内容")
                            .build())
                    .similarityScore(0.91D)
                    .build());
        }

        AiKnowledgeVectorSearchRequest getLastSearchRequest() {
            return lastSearchRequest;
        }
    }
}
