package io.github.framework.ai.provider;

import cn.hutool.core.lang.Assert;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * LocalEmbeddingProvider: adapts to a local embedding microservice (HTTP) that you may run on-prem.
 * configuration: provide baseUrl to the local service which supports /embeddings endpoint.
 */
public class LocalEmbeddingProvider implements EmbeddingProvider {
    private final WebClient webClient;
    private final String baseUrl;

    public LocalEmbeddingProvider(WebClient webClient, String baseUrl) {
        Assert.notNull(webClient, "webClient required");
        this.webClient = webClient.mutate().baseUrl(baseUrl).build();
        this.baseUrl = baseUrl;
    }

    @Override
    public List<float[]> embed(List<String> texts) {
        JsonNode resp = webClient.post()
                .uri("/embeddings")
                .body(BodyInserters.fromValue(Map.of("input", texts)))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block(Duration.ofSeconds(30));

        if (resp == null || !resp.has("embeddings")) return List.of();

        JsonNode arr = resp.get("embeddings");
        List<float[]> out = new ArrayList<>();
        for (JsonNode item : arr) {
            float[] v = new float[item.size()];
            for (int i = 0; i < item.size(); i++) v[i] = (float) item.get(i).asDouble();
            out.add(v);
        }
        return out;
    }
}
