package io.github.starter.ai.service.impl;

import cn.hutool.core.util.StrUtil;
import io.github.starter.ai.factory.XBootChatModelFactory;
import io.github.starter.ai.service.XBootAiService;
import io.github.starter.ai.vo.AiModelConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public final class SpringAiChatService implements XBootAiService {

    /**
     * ChatModel 动态工厂.
     */
    private final XBootChatModelFactory chatModelFactory;

    @Override
    public String chat(final String message,
                       final AiModelConfig modelConfig) {
        ChatResponse response = chatModelFactory.create(modelConfig)
                .call(new Prompt(message));
        return extractText(response);
    }

    @Override
    public Flux<String> stream(final String message,
                               final AiModelConfig modelConfig) {
        return chatModelFactory.create(modelConfig)
                .stream(new Prompt(message))
                .map(this::extractText)
                .filter(StrUtil::isNotBlank);
    }

    private String extractText(final ChatResponse response) {
        if (response == null || response.getResult() == null) {
            return StrUtil.EMPTY;
        }
        return StrUtil.nullToEmpty(response.getResult().getOutput().getText());
    }
}
