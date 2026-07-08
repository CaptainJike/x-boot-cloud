package io.github.module.ai.web;

import io.github.framework.core.context.TenantContext;
import io.github.framework.core.context.TenantContextHolder;
import io.github.framework.core.context.UserContext;
import io.github.framework.core.context.UserContextHolder;
import io.github.framework.core.exception.BusinessException;
import io.github.module.ai.model.request.AdminAiChatDTO;
import io.github.module.ai.model.request.AdminAiChatStreamRequestDTO;
import io.github.module.ai.model.response.AdminAiChatStreamChunkBO;
import io.github.module.ai.service.AiChatService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiChatInternalControllerTest {

    private final AiChatService aiChatService = mock(AiChatService.class);

    private final AiChatInternalController controller = new AiChatInternalController(aiChatService);

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
        TenantContextHolder.clear();
    }

    @Test
    void streamRestoresRequestContextsBeforeDelegating() {
        when(aiChatService.adminStream(any())).thenAnswer(invocation -> {
            assertThat(UserContextHolder.getUserId()).isEqualTo(7L);
            assertThat(TenantContextHolder.getTenantId()).isEqualTo(9L);
            return Flux.just(AdminAiChatStreamChunkBO.builder()
                    .event("message")
                    .messageId("msg-1")
                    .content("hello")
                    .build());
        });

        List<ServerSentEvent<AdminAiChatStreamChunkBO>> events = controller.stream(null, AdminAiChatStreamRequestDTO.builder()
                        .chat(AdminAiChatDTO.builder().content("hi").build())
                        .userContext(UserContext.builder().userId(7L).build())
                        .tenantContext(TenantContext.builder().tenantId(9L).build())
                        .build())
                .collectList()
                .block();

        assertThat(events).hasSize(1);
        assertThat(events.getFirst().id()).isEqualTo("msg-1");
        assertThat(events.getFirst().event()).isEqualTo("message");
        assertThat(events.getFirst().data().getContent()).isEqualTo("hello");
        assertThat(UserContextHolder.getUserContext()).isNull();
        assertThat(TenantContextHolder.getTenantContext()).isNull();
    }

    @Test
    void streamMapsErrorChunkWithoutMessageId() {
        when(aiChatService.adminStream(any())).thenReturn(Flux.just(AdminAiChatStreamChunkBO.builder()
                .event("error")
                .content("stream was reset: CANCEL")
                .finish(true)
                .build()));

        List<ServerSentEvent<AdminAiChatStreamChunkBO>> events = controller.stream(null, AdminAiChatStreamRequestDTO.builder()
                        .chat(AdminAiChatDTO.builder().content("hi").build())
                        .build())
                .collectList()
                .block();

        assertThat(events).hasSize(1);
        assertThat(events.getFirst().event()).isEqualTo("error");
        assertThat(events.getFirst().id()).isNull();
        assertThat(events.getFirst().data().getContent()).isEqualTo("stream was reset: CANCEL");
    }

    @Test
    void streamRejectsInvalidInternalTokenWhenConfigured() {
        ReflectionTestUtils.setField(controller, "internalToken", "secret");

        assertThatThrownBy(() -> controller.stream("bad-token", AdminAiChatStreamRequestDTO.builder()
                .chat(AdminAiChatDTO.builder().content("hi").build())
                .build()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("AI流式内部接口认证失败");
    }
}
