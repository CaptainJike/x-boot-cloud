package io.github.framework.ai.provider;

import java.util.List;

/**
 * Abstracts an embedding provider.
 */
public interface EmbeddingProvider {
    /**
     * Embed a list of texts into vectors.
     *
     * @param texts list of texts
     * @return list of float[] vectors matched by index
     */
    List<float[]> embed(List<String> texts);
}
