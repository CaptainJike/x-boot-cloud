package io.github.module.ai.service.embedding;

import io.github.module.ai.service.model.AiKnowledgeEmbeddingContext;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAiCompatibleAiKnowledgeEmbeddingProviderTest {

    private final OpenAiCompatibleAiKnowledgeEmbeddingProvider provider =
            new OpenAiCompatibleAiKnowledgeEmbeddingProvider();

    @Test
    void supportsZhiPuEmbeddingProvider() {
        assertThat(provider.supports(AiKnowledgeEmbeddingContext.builder()
                .providerType("ZHIPU")
                .modelName("embedding-3")
                .build())).isTrue();
    }

    @Test
    void resolveBaseUrlUsesOfficialZhiPuDefault() {
        String baseUrl = ReflectionTestUtils.invokeMethod(
                provider,
                "resolveBaseUrl",
                AiKnowledgeEmbeddingContext.builder()
                        .providerType("ZHIPU")
                        .modelName("embedding-3")
                        .build()
        );

        assertThat(baseUrl).isEqualTo("https://open.bigmodel.cn/api/paas/v4");
    }
}
