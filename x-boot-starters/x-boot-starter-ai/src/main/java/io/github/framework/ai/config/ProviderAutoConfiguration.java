package io.github.framework.ai.config;


import io.github.framework.ai.properties.AiProperties;
import io.github.framework.ai.provider.DeepSeekProvider;
import io.github.framework.ai.provider.LocalEmbeddingProvider;
import io.github.framework.ai.provider.QianWenProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Provider auto configuration (additional providers beyond OpenAI).
 */
@Configuration(proxyBeanMethods = false)
public class ProviderAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "ai.providers.configs.qianwen", name = "enabled", havingValue = "true")
    public QianWenProvider qianWenProvider(AiProperties properties, WebClient.Builder webClientBuilder) {
        AiProperties.ProviderConfig cfg = properties.getProviders().getConfigs().get("qianwen");
        String baseUrl = cfg.getBaseUrl() == null ? "https://qianwen.example.com" : cfg.getBaseUrl();
        return new QianWenProvider(webClientBuilder.build(), baseUrl, cfg.getApiKey());
    }

    @Bean
    @ConditionalOnProperty(prefix = "ai.providers.configs.deepseek", name = "enabled", havingValue = "true")
    public DeepSeekProvider deepSeekProvider(AiProperties properties, WebClient.Builder webClientBuilder) {
        AiProperties.ProviderConfig cfg = properties.getProviders().getConfigs().get("deepseek");
        String baseUrl = cfg.getBaseUrl() == null ? "https://deepseek.example.com" : cfg.getBaseUrl();
        return new DeepSeekProvider(webClientBuilder.build(), baseUrl, cfg.getApiKey());
    }

    @Bean
    @ConditionalOnProperty(prefix = "ai.providers.configs.local", name = "enabled", havingValue = "true")
    public LocalEmbeddingProvider localEmbeddingProvider(AiProperties properties, WebClient.Builder webClientBuilder) {
        AiProperties.ProviderConfig cfg = properties.getProviders().getConfigs().get("local");
        String baseUrl = cfg.getBaseUrl() == null ? "http://localhost:9000" : cfg.getBaseUrl();
        return new LocalEmbeddingProvider(webClientBuilder.build(), baseUrl);
    }

}
