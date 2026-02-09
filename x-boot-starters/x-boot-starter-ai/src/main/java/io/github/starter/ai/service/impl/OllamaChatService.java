package io.github.starter.ai.service.impl;

import io.github.starter.ai.service.XBootAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * Ollama 聊天提供者实现。
 */
@Service
@RequiredArgsConstructor
public class OllamaChatService implements XBootAiService {

    private final OllamaChatModel chatModel;

    @Override
    public String chat(String message) {
        return chatModel.call(message);
    }


    @Override
    public Flux<String> stream(String message) {
        return chatModel.stream(message);
    }
}
