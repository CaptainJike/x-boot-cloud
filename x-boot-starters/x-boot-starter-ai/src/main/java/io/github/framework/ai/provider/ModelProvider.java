package io.github.framework.ai.provider;

import java.util.List;
import java.util.Map;

/**
 * Abstracts a model provider that can handle text generation / chat.
 */
public interface ModelProvider {

    /**
     * Simple synchronous chat / generation call.
     *
     * @param messages messages or prompt
     * @param options  provider-specific options
     * @return generated text
     */
    String chat(List<Map<String, Object>> messages, Map<String, Object> options);

    /**
     * Streaming variant may be added later.
     */
}
