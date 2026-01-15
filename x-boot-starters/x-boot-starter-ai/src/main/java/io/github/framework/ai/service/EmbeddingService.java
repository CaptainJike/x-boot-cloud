package io.github.framework.ai.service;

import io.github.framework.ai.config.MilvusAutoConfiguration;
import io.github.framework.ai.properties.AiProperties;
import io.github.framework.ai.provider.EmbeddingProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class EmbeddingService {

    private final EmbeddingProvider embeddingProvider;
    private final MilvusAutoConfiguration.MilvusClientWrapper milvus;
    private final AiProperties properties;
    private final AtomicLong idCounter = new AtomicLong(System.currentTimeMillis());

    public EmbeddingService(EmbeddingProvider embeddingProvider, MilvusAutoConfiguration.MilvusClientWrapper milvus, AiProperties properties) {
        this.embeddingProvider = embeddingProvider;
        this.milvus = milvus;
        this.properties = properties;
    }

    /**
     * Embed texts and upsert into milvus collection.
     *
     * @param collection plain collection name (without prefix)
     * @param texts texts to embed (aligned)
     * @return list of assigned ids (milvus ids)
     */
    public List<Long> embedAndUpsert(String collection, List<String> texts) {
        List<float[]> vectors = embeddingProvider.embed(texts);
        List<Long> ids = new ArrayList<>(vectors.size());
        for (int i = 0; i < vectors.size(); i++) {
            ids.add(idCounter.incrementAndGet());
        }
        // upsert to milvus (placeholder)
        milvus.upsert(collection, ids, vectors);
        return ids;
    }
}
