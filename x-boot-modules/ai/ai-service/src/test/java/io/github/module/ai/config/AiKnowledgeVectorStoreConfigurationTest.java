package io.github.module.ai.config;

import io.github.module.ai.service.vector.AiKnowledgeVectorStore;
import io.github.module.ai.service.vector.QdrantAiKnowledgeVectorStore;
import io.github.module.ai.service.vector.UnavailableAiKnowledgeVectorStore;
import io.qdrant.client.QdrantClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AiKnowledgeVectorStoreConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(AiKnowledgeVectorStoreConfiguration.class);

    @Test
    void registersFallbackWhenQdrantIsDisabled() {
        contextRunner
                .withPropertyValues("x.ai.knowledge.vector-store.enabled=false")
                .run(context -> {
                    assertThat(context).hasSingleBean(AiKnowledgeVectorStore.class);
                    assertThat(context).doesNotHaveBean(QdrantClient.class);
                    assertThat(context.getBean(AiKnowledgeVectorStore.class))
                            .isInstanceOf(UnavailableAiKnowledgeVectorStore.class);
                });
    }

    @Test
    void registersQdrantStoreWhenQdrantIsEnabled() {
        contextRunner
                .withPropertyValues(
                        "x.ai.knowledge.vector-store.enabled=true",
                        "x.ai.knowledge.vector-store.type=qdrant",
                        "x.ai.knowledge.vector-store.host=127.0.0.1",
                        "x.ai.knowledge.vector-store.port=6334")
                .run(context -> {
                    assertThat(context).hasSingleBean(QdrantClient.class);
                    assertThat(context).hasSingleBean(AiKnowledgeVectorStore.class);
                    assertThat(context.getBean(AiKnowledgeVectorStore.class))
                            .isInstanceOf(QdrantAiKnowledgeVectorStore.class);
                });
    }
}
