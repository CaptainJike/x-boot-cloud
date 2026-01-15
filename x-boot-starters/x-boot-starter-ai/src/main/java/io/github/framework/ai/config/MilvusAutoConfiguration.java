package io.github.framework.ai.config;

import io.github.framework.ai.properties.AiProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Auto-configuration for Milvus client wrapper.
 * The wrapper hides direct SDK usage and provides minimal upsert/search primitives.
 */
@Configuration(proxyBeanMethods = false)
public class MilvusAutoConfiguration {
    @Bean
    @ConditionalOnProperty(prefix = "ai.milvus", name = "host")
    public MilvusClientWrapper milvusClientWrapper(AiProperties properties) {
        AiProperties.Milvus cfg = properties.getMilvus();
        return new MilvusClientWrapper(cfg.getHost(), cfg.getPort(), cfg.getCollectionPrefix(), cfg.getDimension());
    }

    /**
     * Simple wrapper that abstracts Milvus operations used by starter.
     * Production implementation should use milvus-sdk-java with connection pool, error handling and metrics.
     */
    public static class MilvusClientWrapper {
        private final String host;
        private final int port;
        private final String collectionPrefix;
        private final int dimension;

        public MilvusClientWrapper(String host, int port, String collectionPrefix, int dimension) {
            this.host = host;
            this.port = port;
            this.collectionPrefix = collectionPrefix;
            this.dimension = dimension;
        }

        public String getCollectionName(String name) {
            return collectionPrefix + name;
        }

        /**
         * Upsert vectors into Milvus collection.
         * @param collection collection name (without prefix)
         * @param ids ids aligned with vectors
         * @param vectors vectors to upsert
         */
        public void upsert(String collection, List<Long> ids, List<float[]> vectors) {
            // TODO: implement with Milvus SDK
            // placeholder: should connect to Milvus and upsert
            throw new UnsupportedOperationException("Milvus upsert not implemented: implement using milvus-sdk-java");
        }

        /**
         * Search vectors in collection. Returns list of Hit{ id, score }.
         */
        public List<SearchHit> search(String collection, float[] queryVector, int topK) {
            // TODO: implement with Milvus SDK
            throw new UnsupportedOperationException("Milvus search not implemented: implement using milvus-sdk-java");
        }

        public static class SearchHit {
            public final long id;
            public final double score;
            public SearchHit(long id, double score) {
                this.id = id;
                this.score = score;
            }
        }
    }
}
