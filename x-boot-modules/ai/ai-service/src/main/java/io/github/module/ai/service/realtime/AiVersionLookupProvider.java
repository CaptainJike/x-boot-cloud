package io.github.module.ai.service.realtime;

/**
 * 版本检索数据源.
 */
public interface AiVersionLookupProvider {

    AiRealtimeLookupResult lookup(AiRealtimeLookupRequest request);
}
