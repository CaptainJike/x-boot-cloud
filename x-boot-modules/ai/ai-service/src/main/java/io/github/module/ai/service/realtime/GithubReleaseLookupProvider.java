package io.github.module.ai.service.realtime;

import cn.hutool.core.util.StrUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

/**
 * 基于 GitHub Releases 的版本检索补充数据源.
 */
@Component
public class GithubReleaseLookupProvider implements AiVersionLookupProvider {

    @Override
    @SuppressWarnings("unchecked")
    public AiRealtimeLookupResult lookup(AiRealtimeLookupRequest request) {
        long startAt = System.currentTimeMillis();
        AiRealtimeResolvedSubject subject = request.subject();
        long queriedAt = System.currentTimeMillis();
        if (StrUtil.isBlank(subject.githubRepo())) {
            return AiRealtimeLookupResult.failure(request.lookupType(), subject.displayName(), queriedAt,
                    System.currentTimeMillis() - startAt, "未配置 GitHub 仓库", List.of());
        }
        try {
            Map<String, Object> body = RestClient.builder()
                    .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                    .defaultHeader(HttpHeaders.USER_AGENT, "x-boot-cloud-ai-service")
                    .build()
                    .get()
                    .uri("https://api.github.com/repos/" + subject.githubRepo() + "/releases/latest")
                    .retrieve()
                    .body(Map.class);
            if (body == null || body.isEmpty()) {
                return AiRealtimeLookupResult.failure(request.lookupType(), subject.displayName(), queriedAt,
                        System.currentTimeMillis() - startAt, "GitHub Releases 未返回数据", List.of());
            }
            String version = version(body);
            return AiRealtimeLookupResult.success(request.lookupType(), subject.displayName(), version, releaseType(version),
                    queriedAt, System.currentTimeMillis() - startAt, List.of(AiRealtimeLookupReference.builder()
                            .sourceName("GitHub Releases")
                            .title(StrUtil.blankToDefault(String.valueOf(body.get("name")), subject.displayName()))
                            .url(String.valueOf(body.get("html_url")))
                            .version(version)
                            .publishedAt(body.get("published_at") == null ? null : String.valueOf(body.get("published_at")))
                            .build()));
        } catch (RestClientException | IllegalArgumentException ex) {
            return AiRealtimeLookupResult.failure(request.lookupType(), subject.displayName(), queriedAt,
                    System.currentTimeMillis() - startAt, rootMessage(ex), List.of());
        }
    }

    private String version(Map<String, Object> body) {
        String tagName = body.get("tag_name") == null ? null : String.valueOf(body.get("tag_name"));
        String cleanTagName = StrUtil.removePrefixIgnoreCase(StrUtil.nullToEmpty(tagName), "v");
        return StrUtil.blankToDefault(cleanTagName, String.valueOf(body.get("name")));
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

    private String rootMessage(Throwable ex) {
        Throwable root = ex;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return StrUtil.blankToDefault(root.getMessage(), "GitHub Releases 查询失败");
    }
}
