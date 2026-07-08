package io.github.module.appapi.service;

import io.github.module.ai.facade.AiChatFacade;
import io.github.module.ai.model.request.AdminAiChatDTO;
import io.github.module.ai.model.request.AppAiChatDTO;
import io.github.module.ai.model.response.AdminAiChatBO;
import io.github.module.ai.model.response.AdminAiChatStreamChunkBO;
import io.github.module.ai.model.response.AppAiChatBO;
import io.github.module.ai.model.response.AppAiChatStreamChunkBO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("deprecation")
class AppAiChatServiceTest {

    @Mock
    private AiChatFacade aiChatFacade;

    private AppAiChatService appAiChatService;

    @BeforeEach
    void setUp() {
        appAiChatService = new AppAiChatService();
        ReflectionTestUtils.setField(appAiChatService, "aiChatFacade", aiChatFacade);
    }

    @Test
    void serviceIsMarkedAsHistoricalNonMvpAdapter() {
        assertThat(AppAiChatService.class).hasAnnotation(Deprecated.class);
    }

    @Test
    void chatDelegatesToAiChatFacade() {
        when(aiChatFacade.adminChat(any())).thenReturn(AdminAiChatBO.builder()
                .conversationId("conv-1")
                .messageId("msg-1")
                .modelConfigCode("qwen")
                .providerType("OPENAI_COMPATIBLE")
                .modelName("qwen-plus")
                .answer("你好呀")
                .build());

        AppAiChatBO response = appAiChatService.chat(new AppAiChatDTO()
                .setConversationId("conv-1")
                .setModelConfigCode("qwen")
                .setContent("你好"));

        assertThat(response.getConversationId()).isEqualTo("conv-1");
        assertThat(response.getMessageId()).isEqualTo("msg-1");
        assertThat(response.getModelConfigCode()).isEqualTo("qwen");
        assertThat(response.getProviderType()).isEqualTo("OPENAI_COMPATIBLE");
        assertThat(response.getModelName()).isEqualTo("qwen-plus");
        assertThat(response.getAnswer()).isEqualTo("你好呀");
        ArgumentCaptor<AdminAiChatDTO> dtoCaptor = ArgumentCaptor.forClass(AdminAiChatDTO.class);
        verify(aiChatFacade).adminChat(dtoCaptor.capture());
        assertThat(dtoCaptor.getValue().getConversationId()).isEqualTo("conv-1");
        assertThat(dtoCaptor.getValue().getModelConfigCode()).isEqualTo("qwen");
        assertThat(dtoCaptor.getValue().getContent()).isEqualTo("你好");
    }

    @Test
    void streamDelegatesToAiChatFacade() {
        when(aiChatFacade.adminStream(any())).thenReturn(List.of(AdminAiChatStreamChunkBO.builder()
                .event("message")
                .messageId("msg-1")
                .conversationId("conv-1")
                .modelConfigCode("default")
                .providerType("OLLAMA")
                .modelName("llama3.2")
                .content("hello")
                .finish(false)
                .timestamp(1L)
                .build()));

        List<AppAiChatStreamChunkBO> chunks = appAiChatService.stream(new AppAiChatDTO()
                .setConversationId("conv-1")
                .setContent("hi")).collectList().block();

        assertThat(chunks).hasSize(1);
        assertThat(chunks.getFirst().getEvent()).isEqualTo("message");
        assertThat(chunks.getFirst().getMessageId()).isEqualTo("msg-1");
        assertThat(chunks.getFirst().getConversationId()).isEqualTo("conv-1");
        assertThat(chunks.getFirst().getContent()).isEqualTo("hello");
        verify(aiChatFacade).adminStream(any(AdminAiChatDTO.class));
    }
}
