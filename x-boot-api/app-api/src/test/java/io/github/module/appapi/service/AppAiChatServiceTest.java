package io.github.module.appapi.service;

import io.github.framework.core.enums.EnabledStatusEnum;
import io.github.framework.core.enums.YesOrNoEnum;
import io.github.framework.core.exception.BusinessException;
import io.github.module.ai.facade.AiModelConfigFacade;
import io.github.module.ai.model.request.AppAiChatDTO;
import io.github.module.ai.model.response.AiModelConfigBO;
import io.github.module.ai.model.response.AppAiChatBO;
import io.github.module.ai.model.response.AppAiChatStreamChunkBO;
import io.github.starter.ai.enums.AiProviderTypeEnum;
import io.github.starter.ai.factory.XBootAiFactory;
import io.github.starter.ai.vo.AiModelConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppAiChatServiceTest {

    @Mock
    private XBootAiFactory xBootAiFactory;

    @Mock
    private AiModelConfigFacade aiModelConfigFacade;

    private AppAiChatService appAiChatService;

    @BeforeEach
    void setUp() {
        appAiChatService = new AppAiChatService(xBootAiFactory);
        ReflectionTestUtils.setField(appAiChatService, "aiModelConfigFacade", aiModelConfigFacade);
    }

    @Test
    void chatUsesProvidedModelConfigCode() {
        when(aiModelConfigFacade.getEnabledConfigByCode("qwen", true)).thenReturn(enabledConfig("qwen", "qwen-plus"));
        when(xBootAiFactory.chat(eq("你好"), any(AiModelConfig.class))).thenReturn("你好呀");

        AppAiChatBO response = appAiChatService.chat(new AppAiChatDTO()
                .setConversationId("conv-1")
                .setModelConfigCode(" qwen ")
                .setContent("你好"));

        assertThat(response.getConversationId()).isEqualTo("conv-1");
        assertThat(response.getModelConfigCode()).isEqualTo("qwen");
        assertThat(response.getModelName()).isEqualTo("qwen-plus");
        assertThat(response.getAnswer()).isEqualTo("你好呀");

        ArgumentCaptor<AiModelConfig> configCaptor = ArgumentCaptor.forClass(AiModelConfig.class);
        org.mockito.Mockito.verify(xBootAiFactory).chat(eq("你好"), configCaptor.capture());
        assertThat(configCaptor.getValue().getProviderType()).isEqualTo(AiProviderTypeEnum.OLLAMA);
    }

    @Test
    void chatThrowsWhenDefaultModelConfigMissing() {
        when(aiModelConfigFacade.getDefaultEnabledConfig()).thenReturn(null);

        assertThatThrownBy(() -> appAiChatService.chat(new AppAiChatDTO().setContent("你好")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("当前租户没有可用AI模型配置");
    }

    @Test
    void chatThrowsWhenNonOllamaMissingApiKey() {
        AiModelConfigBO config = enabledConfig("openai", "gpt-4o-mini").setProviderType("OPENAI");
        when(aiModelConfigFacade.getEnabledConfigByCode("openai", true)).thenReturn(config);

        assertThatThrownBy(() -> appAiChatService.chat(new AppAiChatDTO()
                .setModelConfigCode("openai")
                .setContent("你好")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("AI模型配置缺少可用API Key");
    }

    @Test
    void streamEmitsMessageAndDoneEvents() {
        when(aiModelConfigFacade.getDefaultEnabledConfig()).thenReturn(enabledConfig("default", "llama3.2"));
        when(xBootAiFactory.streamChat(eq("你好"), any(AiModelConfig.class))).thenReturn(Flux.just("你", "好"));

        List<AppAiChatStreamChunkBO> chunks = appAiChatService.stream(new AppAiChatDTO()
                .setConversationId("conv-1")
                .setContent("你好")).collectList().block();

        assertThat(chunks).hasSize(3);
        assertThat(chunks).extracting(AppAiChatStreamChunkBO::getEvent)
                .containsExactly("message", "message", "done");
        assertThat(chunks.getLast().getContent()).isEqualTo("[DONE]");
        assertThat(chunks.getLast().getConversationId()).isEqualTo("conv-1");
    }

    @Test
    void streamReturnsErrorEventWhenModelConfigMissing() {
        when(aiModelConfigFacade.getDefaultEnabledConfig()).thenReturn(null);

        List<AppAiChatStreamChunkBO> chunks = appAiChatService.stream(new AppAiChatDTO()
                .setConversationId("conv-1")
                .setContent("你好")).collectList().block();

        assertThat(chunks).hasSize(1);
        assertThat(chunks.getFirst().getEvent()).isEqualTo("error");
        assertThat(chunks.getFirst().getFinish()).isTrue();
        assertThat(chunks.getFirst().getContent()).isEqualTo("当前租户没有可用AI模型配置");
    }

    private AiModelConfigBO enabledConfig(String code, String modelName) {
        return AiModelConfigBO.builder()
                .id(1L)
                .code(code)
                .name("默认模型")
                .providerType("OLLAMA")
                .baseUrl("http://localhost:11434")
                .modelName(modelName)
                .status(EnabledStatusEnum.ENABLED.getValue())
                .defaultFlag(YesOrNoEnum.YES.getValue())
                .build();
    }
}
