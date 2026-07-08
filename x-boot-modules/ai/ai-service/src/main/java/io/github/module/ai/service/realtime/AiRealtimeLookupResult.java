package io.github.module.ai.service.realtime;

import java.util.List;

/**
 * 实时检索结果.
 */
public record AiRealtimeLookupResult(
        boolean success,
        boolean verified,
        String lookupType,
        String subjectName,
        String resolvedVersion,
        String releaseType,
        long queriedAt,
        long elapsedMs,
        List<AiRealtimeLookupReference> references,
        String errorMessage
) {

    public static AiRealtimeLookupResult success(String lookupType,
                                                 String subjectName,
                                                 String resolvedVersion,
                                                 String releaseType,
                                                 long queriedAt,
                                                 long elapsedMs,
                                                 List<AiRealtimeLookupReference> references) {
        return new AiRealtimeLookupResult(true, true, lookupType, subjectName, resolvedVersion,
                releaseType, queriedAt, elapsedMs, references, null);
    }

    public static AiRealtimeLookupResult failure(String lookupType,
                                                 String subjectName,
                                                 long queriedAt,
                                                 long elapsedMs,
                                                 String errorMessage,
                                                 List<AiRealtimeLookupReference> references) {
        return new AiRealtimeLookupResult(false, false, lookupType, subjectName, null,
                null, queriedAt, elapsedMs, references, errorMessage);
    }
}
