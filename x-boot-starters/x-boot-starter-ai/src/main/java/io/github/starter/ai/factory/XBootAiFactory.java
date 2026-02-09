package io.github.starter.ai.factory;

import cn.hutool.core.util.StrUtil;
import io.github.framework.core.enums.ModelTypeEnum;
import io.github.starter.ai.service.XBootAiService;
import io.github.starter.ai.service.impl.DashScopeChatService;
import io.github.starter.ai.service.impl.OllamaChatService;
import io.github.starter.ai.service.impl.OpenAiChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
public class XBootAiFactory {

    private final OllamaChatService ollamaChatService;
    private final OpenAiChatService openAiChatService;
    private final DashScopeChatService dashScopeChatService;

    public String chat(String message, ModelTypeEnum modelTypeEnum) {
        if (StrUtil.isBlank(message)) {
            throw new IllegalArgumentException("问题不能为空");
        }
        return getProvider(modelTypeEnum).chat(message);
    }

    public Flux<String> streamChat(String message, ModelTypeEnum modelTypeEnum) {
        if (StrUtil.isBlank(message)) {
            return Flux.error(new IllegalArgumentException("问题不能为空"));
        }
        return getProvider(modelTypeEnum).stream(message);
    }

    private XBootAiService getProvider(ModelTypeEnum modelTypeEnum) {
        ModelTypeEnum effectiveType = (modelTypeEnum != null) ? modelTypeEnum : ModelTypeEnum.OLLAMA;
        log.info("使用模型: {}", effectiveType);
        if (effectiveType.equals(ModelTypeEnum.OPENAI)){
            return openAiChatService;
        } else if (effectiveType.equals(ModelTypeEnum.OLLAMA)){
            return ollamaChatService;
        } else if (effectiveType.equals(ModelTypeEnum.DASHSCOPE)){
            return dashScopeChatService;
        } else {
            return ollamaChatService;
        }
    }
}
