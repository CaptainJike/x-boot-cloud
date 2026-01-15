package io.github.framework.ai.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "ai")
@Data
public class AiProperties {
    /**
     * default provider key (e.g. openai)
     */
    private String defaultProvider = "openai";

    private Providers providers = new Providers();

    private Milvus milvus = new Milvus();

    public String getDefaultProvider() {
        return defaultProvider;
    }

    public void setDefaultProvider(String defaultProvider) {
        this.defaultProvider = defaultProvider;
    }

    public Providers getProviders() {
        return providers;
    }

    public void setProviders(Providers providers) {
        this.providers = providers;
    }

    public Milvus getMilvus() {
        return milvus;
    }

    public void setMilvus(Milvus milvus) {
        this.milvus = milvus;
    }

    public static class Providers {
        private Map<String, ProviderConfig> configs;

        public Map<String, ProviderConfig> getConfigs() {
            return configs;
        }

        public void setConfigs(Map<String, ProviderConfig> configs) {
            this.configs = configs;
        }
    }

    public static class ProviderConfig {
        private String type;
        private String apiKey;
        private String baseUrl;
        private Boolean enabled = true;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class Milvus {
        private String host = "127.0.0.1";
        private Integer port = 19530;
        private String collectionPrefix = "xboot_ai_";
        private Integer dimension = 1536;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getCollectionPrefix() {
            return collectionPrefix;
        }

        public void setCollectionPrefix(String collectionPrefix) {
            this.collectionPrefix = collectionPrefix;
        }

        public Integer getDimension() {
            return dimension;
        }

        public void setDimension(Integer dimension) {
            this.dimension = dimension;
        }
    }
}
