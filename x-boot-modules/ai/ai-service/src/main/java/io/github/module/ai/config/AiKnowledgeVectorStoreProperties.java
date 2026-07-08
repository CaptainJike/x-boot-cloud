package io.github.module.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * AI 知识库向量存储配置.
 */
@Data
@ConfigurationProperties(prefix = "x.ai.knowledge.vector-store")
public class AiKnowledgeVectorStoreProperties {

    private String type = "qdrant";

    private boolean enabled = true;

    private String host = "127.0.0.1";

    private int port = 6334;

    private String apiKey;

    private boolean useTls = false;

    private String collectionPrefix = "x_boot_ai_knowledge";

    private String distance = "Cosine";

    private boolean initializeSchema = true;

    private Duration timeout = Duration.ofSeconds(10);
}
