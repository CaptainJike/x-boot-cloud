package io.github.module.appapi.web.ai;

import io.github.framework.web.model.response.ApiResult;
import io.github.module.appapi.service.AppAiChatService;
import io.github.module.ai.model.request.AppAiChatDTO;
import io.github.module.ai.model.response.AppAiChatBO;
import io.github.module.ai.model.response.AppAiChatStreamChunkBO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppAiChatControllerTest {

    @Mock
    private AppAiChatService appAiChatService;

    @Test
    void chatReturnsApiResult() {
        AppAiChatController controller = new AppAiChatController(appAiChatService);
        when(appAiChatService.chat(any())).thenReturn(AppAiChatBO.builder()
                .conversationId("conv-1")
                .modelConfigCode("default")
                .answer("answer")
                .build());

        ApiResult<AppAiChatBO> result = controller.chat(new AppAiChatDTO().setContent("hi"));

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().getConversationId()).isEqualTo("conv-1");
        assertThat(result.getData().getAnswer()).isEqualTo("answer");
    }

    @Test
    void streamMapsChunksToServerSentEvents() {
        AppAiChatController controller = new AppAiChatController(appAiChatService);
        when(appAiChatService.stream(any())).thenReturn(Flux.just(AppAiChatStreamChunkBO.builder()
                .event("message")
                .messageId("msg-1")
                .conversationId("conv-1")
                .content("hello")
                .finish(false)
                .build()));

        List<ServerSentEvent<AppAiChatStreamChunkBO>> events = controller.stream(new AppAiChatDTO().setContent("hi"))
                .collectList()
                .block();

        assertThat(events).hasSize(1);
        assertThat(events.getFirst().event()).isEqualTo("message");
        assertThat(events.getFirst().id()).isEqualTo("msg-1");
        assertThat(events.getFirst().data().getContent()).isEqualTo("hello");
    }
}
