package io.github.module.ai.service.realtime;

/**
 * 实时信息检索服务.
 */
public interface AiRealtimeLookupService {

    AiRealtimeLookupResult lookupLatestVersion(AiRealtimeLookupRequest request);
}
