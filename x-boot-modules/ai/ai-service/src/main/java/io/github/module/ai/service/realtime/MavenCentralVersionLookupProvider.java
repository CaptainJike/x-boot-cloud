package io.github.module.ai.service.realtime;

import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 基于 Maven Central 的版本检索.
 */
@Component
public class MavenCentralVersionLookupProvider implements AiVersionLookupProvider {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    @Override
    @SuppressWarnings("unchecked")
    public AiRealtimeLookupResult lookup(AiRealtimeLookupRequest request) {
        long startAt = System.currentTimeMillis();
        AiRealtimeResolvedSubject subject = request.subject();
        long queriedAt = System.currentTimeMillis();
        try {
            Map<String, Object> body = RestClient.builder()
                    .build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("search.maven.org")
                            .path("/solrsearch/select")
                            .queryParam("q", "g:\"" + subject.groupId() + "\" AND a:\"" + subject.artifactId() + "\"")
                            .queryParam("rows", 1)
                            .queryParam("wt", "json")
                            .build())
                    .retrieve()
                    .body(Map.class);
            Map<String, Object> response = body == null ? null : (Map<String, Object>) body.get("response");
            List<Map<String, Object>> docs = response == null ? null : (List<Map<String, Object>>) response.get("docs");
            if (docs == null || docs.isEmpty()) {
                return AiRealtimeLookupResult.failure(request.lookupType(), subject.displayName(), queriedAt,
                        System.currentTimeMillis() - startAt, "未查询到对应依赖坐标", List.of());
            }
            Map<String, Object> doc = docs.getFirst();
            String version = objectToString(doc.get("latestVersion"));
            Long timestamp = objectToLong(doc.get("timestamp"));
            String publishedAt = timestamp == null ? null : DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(timestamp));
            String detailUrl = "https://search.maven.org/artifact/" + subject.groupId() + "/" + subject.artifactId();
            return AiRealtimeLookupResult.success(request.lookupType(), subject.displayName(), version, releaseType(version),
                    queriedAt, System.currentTimeMillis() - startAt, List.of(AiRealtimeLookupReference.builder()
                            .sourceName("Maven Central")
                            .title(subject.groupId() + ":" + subject.artifactId())
                            .url(detailUrl)
                            .version(version)
                            .publishedAt(publishedAt)
                            .build()));
        } catch (RestClientException | IllegalArgumentException ex) {
            return AiRealtimeLookupResult.failure(request.lookupType(), subject.displayName(), queriedAt,
                    System.currentTimeMillis() - startAt, rootMessage(ex), List.of());
        }
    }

    private String releaseType(String version) {
        String cleanVersion = StrUtil.nullToEmpty(version).toLowerCase();
        if (cleanVersion.contains("rc")) {
            return "RC";
        }
        if (cleanVersion.contains("-m") || cleanVersion.contains("milestone")) {
            return "MILESTONE";
        }
        return "GA";
    }

    private String objectToString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Long objectToLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String rootMessage(Throwable ex) {
        Throwable root = ex;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return StrUtil.blankToDefault(root.getMessage(), "Maven Central 查询失败");
    }
}
