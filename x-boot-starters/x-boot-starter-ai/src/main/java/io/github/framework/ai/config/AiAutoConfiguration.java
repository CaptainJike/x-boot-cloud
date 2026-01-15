package io.github.framework.ai.config;

import io.github.framework.ai.properties.AiProperties;
import io.github.framework.ai.provider.EmbeddingProvider;
import io.github.framework.ai.provider.ModelProvider;
import io.github.framework.ai.provider.OpenAiProvider;
import io.github.framework.ai.service.EmbeddingService;
import io.github.framework.ai.service.ModelService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AiProperties.class)
public class AiAutoConfiguration {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    @ConditionalOnProperty(prefix = "ai.providers.configs.openai", name = "enabled", havingValue = "true", matchIfMissing = true)
    public OpenAiProvider openAiProvider(AiProperties properties, WebClient.Builder webClientBuilder) {
        AiProperties.ProviderConfig cfg = properties.getProviders().getConfigs() == null
                ? null : properties.getProviders().getConfigs().get("openai");

        String baseUrl = cfg != null && cfg.getBaseUrl() != null ? cfg.getBaseUrl() : "https://api.openai.com/v1";
        String apiKey = cfg == null ? null : cfg.getApiKey();
        return new OpenAiProvider(webClientBuilder.build(), baseUrl, apiKey);
    }

    @Bean
    public ModelService modelService(ModelProvider modelProvider) {
        return new ModelService(modelProvider);
    }

    @Bean
    public EmbeddingService embeddingService(EmbeddingProvider embeddingProvider, MilvusAutoConfiguration.MilvusClientWrapper milvusClientWrapper, AiProperties properties) {
        return new EmbeddingService(embeddingProvider, milvusClientWrapper, properties);
    }
}
