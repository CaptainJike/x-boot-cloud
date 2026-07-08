package io.github.module.ai.service.realtime;

import lombok.Builder;

/**
 * 实时检索意图分析结果.
 */
@Builder
public record AiRealtimeIntentResult(
        boolean realtimeLookupRequired,
        String lookupType,
        AiRealtimeResolvedSubject subject
) {
}
