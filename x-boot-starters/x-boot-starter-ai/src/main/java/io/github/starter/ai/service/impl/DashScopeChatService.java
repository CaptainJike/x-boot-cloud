package io.github.starter.ai.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import io.github.starter.ai.service.XBootAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * 阿里云 聊天提供者实现。
 */
@Service
@RequiredArgsConstructor
public class DashScopeChatService implements XBootAiService {

    private final DashScopeChatModel chatModel;

    @Override
    public String chat(String message) {
        return chatModel.call(message);
    }

    @Override
    public Flux<String> stream(String message) {
        return chatModel.stream(message);
    }
}
