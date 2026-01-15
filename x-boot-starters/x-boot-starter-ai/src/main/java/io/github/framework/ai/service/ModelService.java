package io.github.framework.ai.service;

import io.github.framework.ai.provider.ModelProvider;

import java.util.List;
import java.util.Map;

public class ModelService {

    private final ModelProvider modelProvider;

    public ModelService(ModelProvider modelProvider) {
        this.modelProvider = modelProvider;
    }

    /**
     * Chat helper: constructs messages and forwards to provider.
     */
    public String chat(List<Map<String, Object>> messages, Map<String, Object> options) {
        return modelProvider.chat(messages, options);
    }
}
