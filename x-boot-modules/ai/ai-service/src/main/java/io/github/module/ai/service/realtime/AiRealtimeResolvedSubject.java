package io.github.module.ai.service.realtime;

import lombok.Builder;

/**
 * 实时检索主题.
 */
@Builder
public record AiRealtimeResolvedSubject(
        String displayName,
        String groupId,
        String artifactId,
        String githubRepo
) {
}
