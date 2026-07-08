package io.github.module.ai.service.realtime;

import lombok.Builder;

/**
 * 实时检索引用来源.
 */
@Builder
public record AiRealtimeLookupReference(
        String sourceName,
        String title,
        String url,
        String version,
        String publishedAt
) {
}
