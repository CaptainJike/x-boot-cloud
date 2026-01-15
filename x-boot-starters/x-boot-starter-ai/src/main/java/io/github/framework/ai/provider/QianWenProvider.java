package io.github.framework.ai.provider;

import cn.hutool.core.lang.Assert;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.*;

/**
 * Minimal adapter for 阿里千问（QianWen）- HTTP style.
 * Note: endpoint and payload vary per provider; adapt as needed.
 */
public class QianWenProvider implements ModelProvider, EmbeddingProvider{

    private final WebClient webClient;
    private final String baseUrl;
    private final String apiKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public QianWenProvider(WebClient webClient, String baseUrl, String apiKey) {
        Assert.notNull(webClient, "webClient required");
        this.webClient = webClient.mutate().baseUrl(baseUrl).build();
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    @Override
    public String chat(List<Map<String, Object>> messages, Map<String, Object> options) {
        Map<String, Object> body = new HashMap<>();
        body.put("prompt", messages); // provider-specific adapt
        JsonNode resp = webClient.post()
                .uri("/qa") // placeholder
                .headers(h -> { if (apiKey != null) h.setBearerAuth(apiKey); })
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block(Duration.ofSeconds(30));
        if (resp == null) return "";
        // provider-specific parse
        if (resp.has("answer")) return resp.get("answer").asText();
        return resp.toString();
    }

    @Override
    public List<float[]> embed(List<String> texts) {
        Map<String, Object> body = new HashMap<>();
        body.put("texts", texts);
        JsonNode resp = webClient.post()
                .uri("/embeddings") // placeholder
                .headers(h -> { if (apiKey != null) h.setBearerAuth(apiKey); })
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block(Duration.ofSeconds(30));
        if (resp == null || !resp.has("embeddings")) return Collections.emptyList();
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
