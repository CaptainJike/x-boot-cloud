package io.github.module.ai.service.realtime;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 默认实时检索服务.
 */
@Service
public class DefaultAiRealtimeLookupService implements AiRealtimeLookupService {

    private final MavenCentralVersionLookupProvider mavenCentralVersionLookupProvider;

    private final GithubReleaseLookupProvider githubReleaseLookupProvider;

    public DefaultAiRealtimeLookupService(MavenCentralVersionLookupProvider mavenCentralVersionLookupProvider,
                                          GithubReleaseLookupProvider githubReleaseLookupProvider) {
        this.mavenCentralVersionLookupProvider = mavenCentralVersionLookupProvider;
        this.githubReleaseLookupProvider = githubReleaseLookupProvider;
    }

    @Override
    public AiRealtimeLookupResult lookupLatestVersion(AiRealtimeLookupRequest request) {
        long startAt = System.currentTimeMillis();
        long queriedAt = System.currentTimeMillis();
        AiRealtimeLookupResult mavenResult = mavenCentralVersionLookupProvider.lookup(request);
        AiRealtimeLookupResult githubResult = githubReleaseLookupProvider.lookup(request);
        List<AiRealtimeLookupReference> references = new ArrayList<>();
        if (CollUtil.isNotEmpty(mavenResult.references())) {
            references.addAll(mavenResult.references());
        }
        if (CollUtil.isNotEmpty(githubResult.references())) {
            references.addAll(githubResult.references());
        }

        if (mavenResult.success()) {
            return AiRealtimeLookupResult.success(request.lookupType(),
                    request.subject().displayName(),
                    mavenResult.resolvedVersion(),
                    mavenResult.releaseType(),
                    queriedAt,
                    System.currentTimeMillis() - startAt,
                    references);
        }
        if (githubResult.success()) {
            return AiRealtimeLookupResult.success(request.lookupType(),
                    request.subject().displayName(),
                    githubResult.resolvedVersion(),
                    githubResult.releaseType(),
                    queriedAt,
                    System.currentTimeMillis() - startAt,
                    references);
        }
        return AiRealtimeLookupResult.failure(request.lookupType(), request.subject().displayName(), queriedAt,
                System.currentTimeMillis() - startAt, mergeErrors(mavenResult, githubResult), references);
    }

    private String mergeErrors(AiRealtimeLookupResult mavenResult, AiRealtimeLookupResult githubResult) {
        List<String> errors = new ArrayList<>();
        if (StrUtil.isNotBlank(mavenResult.errorMessage())) {
            errors.add("Maven Central：" + mavenResult.errorMessage());
        }
        if (StrUtil.isNotBlank(githubResult.errorMessage())) {
            errors.add("GitHub Releases：" + githubResult.errorMessage());
        }
        if (errors.isEmpty()) {
            return "实时版本核验失败";
        }
        return String.join("；", errors);
    }
}
