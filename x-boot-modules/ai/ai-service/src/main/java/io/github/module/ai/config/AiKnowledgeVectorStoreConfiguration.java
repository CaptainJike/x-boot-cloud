package io.github.module.ai.config;

import cn.hutool.core.text.CharSequenceUtil;
import io.github.module.ai.service.vector.AiKnowledgeVectorStore;
import io.github.module.ai.service.vector.QdrantAiKnowledgeVectorStore;
import io.github.module.ai.service.vector.UnavailableAiKnowledgeVectorStore;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 知识库向量存储装配.
 */
@RequiredArgsConstructor
@EnableConfigurationProperties(AiKnowledgeVectorStoreProperties.class)
@Configuration
public class AiKnowledgeVectorStoreConfiguration {

    private final AiKnowledgeVectorStoreProperties properties;

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(prefix = "x.ai.knowledge.vector-store", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnExpression("'${x.ai.knowledge.vector-store.type:qdrant}'.equalsIgnoreCase('qdrant')")
    public QdrantClient qdrantClient() {
        QdrantGrpcClient.Builder builder = QdrantGrpcClient
                .newBuilder(properties.getHost(), properties.getPort(), properties.isUseTls(), false)
                .withTimeout(properties.getTimeout());
        if (CharSequenceUtil.isNotBlank(properties.getApiKey())) {
            builder.withApiKey(properties.getApiKey());
        }

        return new QdrantClient(builder.build());
    }

    @Bean
    @ConditionalOnBean(QdrantClient.class)
    @Primary
    public AiKnowledgeVectorStore qdrantAiKnowledgeVectorStore(QdrantClient qdrantClient) {
        return new QdrantAiKnowledgeVectorStore(qdrantClient, properties);
    }

    @Bean
    @ConditionalOnProperty(prefix = "x.ai.knowledge.vector-store", name = "enabled", havingValue = "false")
    public AiKnowledgeVectorStore unavailableAiKnowledgeVectorStore() {
        return new UnavailableAiKnowledgeVectorStore();
    }
}
