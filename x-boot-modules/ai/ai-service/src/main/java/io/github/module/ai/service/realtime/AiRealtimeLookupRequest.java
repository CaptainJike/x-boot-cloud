package io.github.module.ai.service.realtime;

import lombok.Builder;

/**
 * 实时检索请求.
 */
@Builder
public record AiRealtimeLookupRequest(
        String queryText,
        String lookupType,
        AiRealtimeResolvedSubject subject
) {
}
