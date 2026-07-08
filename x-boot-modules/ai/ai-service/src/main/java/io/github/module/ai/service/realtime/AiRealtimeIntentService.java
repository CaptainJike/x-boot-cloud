package io.github.module.ai.service.realtime;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 实时检索意图分析服务.
 */
@Service
public class AiRealtimeIntentService {

    public static final String LOOKUP_TYPE_LATEST_VERSION = "latest_version";

    private static final Pattern MAVEN_COORDINATE_PATTERN =
            Pattern.compile("([a-zA-Z0-9_.-]+):([a-zA-Z0-9_.-]+)");

    private static final List<String> REALTIME_KEYWORDS = List.of(
            "最新", "最新版", "最新版本", "当前", "现在", "实时", "最近发布", "版本", "latest", "release", "ga", "rc"
    );

    private static final Map<String, AiRealtimeResolvedSubject> SUBJECT_ALIASES = createAliases();

    public AiRealtimeIntentResult analyze(String question) {
        String cleanQuestion = CharSequenceUtil.cleanBlank(question);
        if (StrUtil.isBlank(cleanQuestion) || !containsRealtimeSignal(cleanQuestion)) {
            return AiRealtimeIntentResult.builder()
                    .realtimeLookupRequired(false)
                    .lookupType(null)
                    .subject(null)
                    .build();
        }

        AiRealtimeResolvedSubject subject = resolveSubject(cleanQuestion);
        if (subject == null) {
            return AiRealtimeIntentResult.builder()
                    .realtimeLookupRequired(false)
                    .lookupType(null)
                    .subject(null)
                    .build();
        }

        return AiRealtimeIntentResult.builder()
                .realtimeLookupRequired(true)
                .lookupType(LOOKUP_TYPE_LATEST_VERSION)
                .subject(subject)
                .build();
    }

    private boolean containsRealtimeSignal(String question) {
        String lowerQuestion = StrUtil.nullToEmpty(question).toLowerCase();
        return REALTIME_KEYWORDS.stream().anyMatch(lowerQuestion::contains);
    }

    private AiRealtimeResolvedSubject resolveSubject(String question) {
        Matcher matcher = MAVEN_COORDINATE_PATTERN.matcher(question);
        if (matcher.find()) {
            String groupId = matcher.group(1);
            String artifactId = matcher.group(2);
            return AiRealtimeResolvedSubject.builder()
                    .displayName(groupId + ":" + artifactId)
                    .groupId(groupId)
                    .artifactId(artifactId)
                    .githubRepo(null)
                    .build();
        }

        String lowerQuestion = question.toLowerCase();
        return SUBJECT_ALIASES.entrySet().stream()
                .filter(entry -> lowerQuestion.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private static Map<String, AiRealtimeResolvedSubject> createAliases() {
        Map<String, AiRealtimeResolvedSubject> aliases = new LinkedHashMap<>();
        register(aliases, "spring ai", "Spring AI", "org.springframework.ai", "spring-ai-bom", "spring-projects/spring-ai");
        register(aliases, "spring-ai", "Spring AI", "org.springframework.ai", "spring-ai-bom", "spring-projects/spring-ai");
        register(aliases, "spring boot", "Spring Boot", "org.springframework.boot", "spring-boot-starter-parent",
                "spring-projects/spring-boot");
        register(aliases, "springboot", "Spring Boot", "org.springframework.boot", "spring-boot-starter-parent",
                "spring-projects/spring-boot");
        register(aliases, "spring cloud alibaba", "Spring Cloud Alibaba", "com.alibaba.cloud",
                "spring-cloud-alibaba-dependencies", "alibaba/spring-cloud-alibaba");
        register(aliases, "spring cloud", "Spring Cloud", "org.springframework.cloud",
                "spring-cloud-dependencies", "spring-cloud/spring-cloud-release");
        return aliases;
    }

    private static void register(Map<String, AiRealtimeResolvedSubject> aliases,
                                 String alias,
                                 String displayName,
                                 String groupId,
                                 String artifactId,
                                 String githubRepo) {
        aliases.put(alias, AiRealtimeResolvedSubject.builder()
                .displayName(displayName)
                .groupId(groupId)
                .artifactId(artifactId)
                .githubRepo(githubRepo)
                .build());
    }
}
