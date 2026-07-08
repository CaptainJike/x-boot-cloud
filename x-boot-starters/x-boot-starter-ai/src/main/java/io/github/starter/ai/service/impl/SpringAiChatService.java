package io.github.starter.ai.service.impl;

import cn.hutool.core.util.StrUtil;
import io.github.starter.ai.factory.XBootChatModelFactory;
import io.github.starter.ai.service.XBootAiService;
import io.github.starter.ai.vo.AiChatMedia;
import io.github.starter.ai.vo.AiChatRequest;
import io.github.starter.ai.vo.AiModelConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.List;

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
    public String chat(final AiChatRequest request,
                       final AiModelConfig modelConfig) {
        ChatResponse response = chatModelFactory.create(modelConfig)
                .call(toPrompt(request));
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

    @Override
    public Flux<String> stream(final AiChatRequest request,
                               final AiModelConfig modelConfig) {
        return chatModelFactory.create(modelConfig)
                .stream(toPrompt(request))
                .map(this::extractText)
                .filter(StrUtil::isNotBlank);
    }

    private Prompt toPrompt(final AiChatRequest request) {
        if (request == null || request.getMedia() == null || request.getMedia().isEmpty()) {
            return new Prompt(request == null ? StrUtil.EMPTY : StrUtil.nullToEmpty(request.getText()));
        }

        UserMessage userMessage = UserMessage.builder()
                .text(StrUtil.nullToEmpty(request.getText()))
                .media(toMedia(request.getMedia()))
                .build();
        return new Prompt(userMessage);
    }

    private Media[] toMedia(final List<AiChatMedia> mediaList) {
        return mediaList.stream()
                .map(this::toMedia)
                .toArray(Media[]::new);
    }

    private Media toMedia(final AiChatMedia media) {
        Media.Builder builder = Media.builder()
                .mimeType(MimeTypeUtils.parseMimeType(media.getMimeType()))
                .name(StrUtil.blankToDefault(media.getName(), "attachment"));
        if (StrUtil.isNotBlank(media.getUri())) {
            return builder.data(URI.create(media.getUri())).build();
        }
        return builder.data(new ByteArrayResource(media.getData() == null ? new byte[0] : media.getData())).build();
    }

    private String extractText(final ChatResponse response) {
        if (response == null || response.getResult() == null) {
            return StrUtil.EMPTY;
        }
        return StrUtil.nullToEmpty(response.getResult().getOutput().getText());
    }
}
