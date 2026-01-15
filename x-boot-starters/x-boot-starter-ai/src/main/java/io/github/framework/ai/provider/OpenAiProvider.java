package io.github.framework.ai.provider;

import cn.hutool.core.lang.Assert;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.*;

/**
 * Minimal OpenAI provider implementation using WebClient.
 * NOTE: production should handle retries, rate limit, streaming, error mapping, token usage accounting, etc.
 */
public class OpenAiProvider implements ModelProvider, EmbeddingProvider{
    private final WebClient webClient;
    private final String baseUrl;
    private final String apiKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAiProvider(WebClient webClient, String baseUrl, String apiKey) {
        Assert.notNull(webClient, "webClient is required");
        this.webClient = webClient.mutate().baseUrl(baseUrl).build();
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    @Override
    public String chat(List<Map<String, Object>> messages, Map<String, Object> options) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", options != null && options.get("model") != null ? options.get("model") : "gpt-4o-mini");
        body.put("messages", messages);

        JsonNode resp = webClient.post()
                .uri("/chat/completions")
                .headers(h -> {
                    if (apiKey != null) h.setBearerAuth(apiKey);
                })
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block(Duration.ofSeconds(30));

        if (resp == null) return "";

        // naive extraction
        JsonNode choices = resp.get("choices");
        if (choices != null && choices.isArray() && choices.size() > 0) {
            JsonNode first = choices.get(0);
            JsonNode message = first.get("message");
            if (message != null && message.has("content")) {
                return message.get("content").asText();
            } else if (first.has("text")) {
                return first.get("text").asText();
            }
        }
        return resp.toString();
    }

    @Override
    public List<float[]> embed(List<String> texts) {
        if (texts == null || texts.isEmpty()) return Collections.emptyList();

        Map<String, Object> body = new HashMap<>();
        body.put("model", "text-embedding-3-small");
        body.put("input", texts);

        JsonNode resp = webClient.post()
                .uri("/embeddings")
                .headers(h -> {
                    if (apiKey != null) h.setBearerAuth(apiKey);
                })
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block(Duration.ofSeconds(30));

        if (resp == null) return Collections.emptyList();

        JsonNode data = resp.get("data");
        if (data == null || !data.isArray()) return Collections.emptyList();

        List<float[]> vectors = new ArrayList<>();
        for (JsonNode item : data) {
            JsonNode vec = item.get("embedding");
            if (vec != null && vec.isArray()) {
                float[] arr = new float[vec.size()];
                for (int i = 0; i < vec.size(); i++) arr[i] = (float) vec.get(i).asDouble();
                vectors.add(arr);
            }
        }
        return vectors;
    }
}
