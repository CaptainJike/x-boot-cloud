package io.github.module.ai.constant;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * AI 模型支持能力常量.
 */
public final class AiModelCapabilityConstant {

    public static final String CHAT = "chat";

    public static final String EMBEDDING = "embedding";

    public static final String IMAGE = "image";

    private static final List<String> ORDERED_CAPABILITIES = List.of(CHAT, EMBEDDING, IMAGE);

    private AiModelCapabilityConstant() {
    }

    /**
     * 规范化支持能力，默认保留 chat 能力.
     */
    public static String normalize(String supportedCapabilities) {
        Set<String> capabilitySet = splitRaw(supportedCapabilities);
        if (capabilitySet.isEmpty()) {
            return CHAT;
        }
        return ORDERED_CAPABILITIES.stream()
                .filter(capabilitySet::contains)
                .collect(Collectors.joining(","));
    }

    /**
     * 是否包含指定能力.
     */
    public static boolean contains(String supportedCapabilities, String capability) {
        String normalizedCapability = normalizeSingle(capability);
        if (normalizedCapability == null) {
            return false;
        }
        return splitRaw(normalize(supportedCapabilities)).contains(normalizedCapability);
    }

    private static Set<String> splitRaw(String supportedCapabilities) {
        return Stream.of(supportedCapabilities == null ? new String[0] : supportedCapabilities.split(","))
                .map(AiModelCapabilityConstant::normalizeSingle)
                .filter(item -> item != null && !item.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static String normalizeSingle(String capability) {
        if (capability == null) {
            return null;
        }
        String normalized = capability.trim().toLowerCase(Locale.ROOT);
        if (!ORDERED_CAPABILITIES.contains(normalized)) {
            return null;
        }
        return normalized;
    }
}
