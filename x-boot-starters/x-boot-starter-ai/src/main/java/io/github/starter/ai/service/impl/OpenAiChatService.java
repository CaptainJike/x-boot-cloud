package io.github.starter.ai.service.impl;

import io.github.starter.ai.service.XBootAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * OpenAI 聊天提供者实现。
 */
@Service
@RequiredArgsConstructor
public class OpenAiChatService implements XBootAiService {

    private final OpenAiChatModel chatModel;

    @Override
    public String chat(String message) {
        return chatModel.call(message);
    }


    @Override
    public Flux<String> stream(String message) {
        return chatModel.stream(message);
    }
}
