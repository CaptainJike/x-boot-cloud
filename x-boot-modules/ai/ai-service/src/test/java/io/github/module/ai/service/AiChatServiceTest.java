package io.github.module.ai.service;

import io.github.framework.core.enums.EnabledStatusEnum;
import io.github.framework.core.enums.YesOrNoEnum;
import io.github.framework.core.exception.BusinessException;
import io.github.module.ai.constant.AiModelCapabilityConstant;
import io.github.module.ai.entity.AiMessageEntity;
import io.github.module.ai.enums.AiErrorEnum;
import io.github.module.ai.mapper.AiMessageMapper;
import io.github.module.ai.model.request.AdminAiChatDTO;
import io.github.module.ai.model.request.AdminAiChatAttachmentDTO;
import io.github.module.ai.model.request.AdminRetrieveAiKnowledgeDTO;
import io.github.module.ai.model.response.AdminAiChatBO;
import io.github.module.ai.model.response.AdminAiChatModelOptionBO;
import io.github.module.ai.model.response.AdminAiChatStreamChunkBO;
import io.github.module.ai.model.response.AdminAiMessageAttachmentBO;
import io.github.module.ai.model.response.AiKnowledgeRetrievalHitBO;
import io.github.module.ai.model.response.AiKnowledgeRetrievalResultBO;
import io.github.module.ai.model.response.AiModelConfigBO;
import io.github.module.ai.service.realtime.AiRealtimeIntentService;
import io.github.module.ai.service.realtime.AiRealtimeLookupResult;
import io.github.module.ai.service.realtime.AiRealtimeLookupService;
import io.github.starter.ai.enums.AiProviderTypeEnum;
import io.github.starter.ai.service.XBootAiService;
import io.github.starter.ai.vo.AiChatMedia;
import io.github.starter.ai.vo.AiChatRequest;
import io.github.starter.ai.vo.AiModelConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiChatServiceTest {

    @Mock
    private AiModelConfigService aiModelConfigService;

    @Mock
    private XBootAiService xBootAiService;

    @Mock
    private AiChatPersistenceService aiChatPersistenceService;

    @Mock
    private AiKnowledgeRetrievalService aiKnowledgeRetrievalService;

    @Mock
    private AiMessageMapper aiMessageMapper;

    @Mock
    private AiChatAttachmentService aiChatAttachmentService;

    @Mock
    private AiRealtimeLookupService aiRealtimeLookupService;

    private AiChatService aiChatService;

    @BeforeEach
    void setUp() {
        aiChatService = new AiChatService(
                aiModelConfigService, xBootAiService, aiChatPersistenceService, aiKnowledgeRetrievalService,
                aiMessageMapper, aiChatAttachmentService, new AiRealtimeIntentService(), aiRealtimeLookupService);
        lenient().when(aiChatAttachmentService.prepare(any(AdminAiChatDTO.class), any(AiModelConfigBO.class)))
                .thenReturn(AiChatAttachmentService.ChatAttachments.empty());
        lenient().when(aiChatAttachmentService.toImageMedia(any()))
                .thenReturn(List.of());
    }

    @Test
    void adminListModelOptionsReturnsSafeEnabledChatModelOptions() {
        AiModelConfigBO modelConfig = enabledConfig("qwen", "qwen-plus")
                .setId(7L)
                .setName("通义千问")
                .setProviderType("OPENAI_COMPATIBLE")
                .setApiKey("sk-secret")
                .setApiKeyMasked("sk******")
                .setBaseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .setDescription("适合日常对话");
        AiModelConfigBO embeddingConfig = enabledConfig("embedding", "text-embedding-v1")
                .setSupportedCapabilities(AiModelCapabilityConstant.EMBEDDING);
        when(aiModelConfigService.adminSelectOptions()).thenReturn(List.of(modelConfig, embeddingConfig));

        List<AdminAiChatModelOptionBO> options = aiChatService.adminListModelOptions();

        assertThat(options).hasSize(1);
        assertThat(options.getFirst().getId()).isEqualTo(7L);
        assertThat(options.getFirst().getCode()).isEqualTo("qwen");
        assertThat(options.getFirst().getName()).isEqualTo("通义千问");
        assertThat(options.getFirst().getProviderType()).isEqualTo("OPENAI_COMPATIBLE");
        assertThat(options.getFirst().getModelName()).isEqualTo("qwen-plus");
        assertThat(options.getFirst().getSupportedCapabilities()).isEqualTo("chat");
        assertThat(options.getFirst().getDefaultFlag()).isEqualTo(1);
        assertThat(options.getFirst().getDescription()).isEqualTo("适合日常对话");
    }

    @Test
    void adminChatRejectsEmbeddingOnlyModelConfig() {
        AiModelConfigBO embeddingConfig = enabledConfig("embedding", "text-embedding-v1")
                .setSupportedCapabilities(AiModelCapabilityConstant.EMBEDDING);
        when(aiModelConfigService.getEnabledConfigByCode("embedding", true)).thenReturn(embeddingConfig);

        assertThatThrownBy(() -> aiChatService.adminChat(AdminAiChatDTO.builder()
                .conversationId("conv-1")
                .modelConfigCode("embedding")
                .content("你好")
                .build()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("当前AI模型配置不支持对话能力");
    }

    @Test
    void adminChatUsesProvidedModelConfigCode() {
        AiModelConfigBO modelConfig = enabledConfig("qwen", "qwen-plus");
        AiModelConfig runtimeConfig = runtimeConfig("qwen-plus");
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        when(aiModelConfigService.getEnabledConfigByCode("qwen", true)).thenReturn(modelConfig);
        when(aiModelConfigService.toRuntimeConfig(modelConfig)).thenReturn(runtimeConfig);
        stubPersistenceStart();
        when(xBootAiService.chat(promptCaptor.capture(), eq(runtimeConfig))).thenReturn("你好呀");

        AdminAiChatBO response = aiChatService.adminChat(AdminAiChatDTO.builder()
                .conversationId("conv-1")
                .modelConfigCode(" qwen ")
                .content("你好")
                .build());

        assertThat(response.getConversationId()).isEqualTo("conv-1");
        assertThat(response.getMessageId()).isNotBlank();
        assertThat(response.getModelConfigCode()).isEqualTo("qwen");
        assertThat(response.getModelName()).isEqualTo("qwen-plus");
        assertThat(response.getAnswer()).isEqualTo("你好呀");
        assertThat(promptCaptor.getValue())
                .contains("你是企业 AI 对话助手", "【当前任务】", "【当前用户问题】", "你好");
        verify(aiChatPersistenceService).start(any(AdminAiChatDTO.class), eq("conv-1"), eq(modelConfig),
                anyList(), eq(response.getMessageId()), any(), eq("chat"), eq(false));
        verify(aiChatPersistenceService).completeSuccess(any(), eq("你好呀"));
    }

    @Test
    void adminChatUsesDifferentSelectedModelOnSameConversationTurns() {
        AiModelConfigBO qwenConfig = enabledConfig("qwen", "qwen-plus").setId(1L);
        AiModelConfigBO deepseekConfig = enabledConfig("deepseek", "deepseek-chat")
                .setId(2L)
                .setProviderType("DEEPSEEK");
        AiModelConfig qwenRuntimeConfig = runtimeConfig("qwen-plus");
        AiModelConfig deepseekRuntimeConfig = runtimeConfig("deepseek-chat");
        when(aiModelConfigService.getEnabledConfigByCode("qwen", true)).thenReturn(qwenConfig);
        when(aiModelConfigService.getEnabledConfigByCode("deepseek", true)).thenReturn(deepseekConfig);
        when(aiModelConfigService.toRuntimeConfig(qwenConfig)).thenReturn(qwenRuntimeConfig);
        when(aiModelConfigService.toRuntimeConfig(deepseekConfig)).thenReturn(deepseekRuntimeConfig);
        stubPersistenceStart();
        when(xBootAiService.chat(any(String.class), eq(qwenRuntimeConfig))).thenReturn("qwen回答");
        when(xBootAiService.chat(any(String.class), eq(deepseekRuntimeConfig))).thenReturn("deepseek回答");

        aiChatService.adminChat(AdminAiChatDTO.builder()
                .conversationId("conv-1")
                .modelConfigCode("qwen")
                .content("第一轮")
                .build());
        aiChatService.adminChat(AdminAiChatDTO.builder()
                .conversationId("conv-1")
                .modelConfigCode("deepseek")
                .content("第二轮")
                .build());

        ArgumentCaptor<AiModelConfigBO> modelCaptor = ArgumentCaptor.forClass(AiModelConfigBO.class);
        verify(aiChatPersistenceService, times(2)).start(any(AdminAiChatDTO.class), eq("conv-1"), modelCaptor.capture(),
                anyList(), any(), any(), eq("chat"), eq(false));
        assertThat(modelCaptor.getAllValues()).extracting(AiModelConfigBO::getCode)
                .containsExactly("qwen", "deepseek");
        verify(aiChatPersistenceService).completeSuccess(any(), eq("qwen回答"));
        verify(aiChatPersistenceService).completeSuccess(any(), eq("deepseek回答"));
    }

    @Test
    void adminChatUsesRecentHistoryInPromptAndExcludesCurrentUserMessage() {
        AiModelConfigBO modelConfig = enabledConfig("qwen", "qwen-plus");
        AiModelConfig runtimeConfig = runtimeConfig("qwen-plus");
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        when(aiModelConfigService.getEnabledConfigByCode("qwen", true)).thenReturn(modelConfig);
        when(aiModelConfigService.toRuntimeConfig(modelConfig)).thenReturn(runtimeConfig);
        stubPersistenceStart();
        when(aiMessageMapper.selectList(any())).thenReturn(List.of(
                message("assistant-1", "conv-1", "assistant", "我是小助手", 2),
                message("user-message-id", "conv-1", "user", "这条是本轮消息，不应重复进入历史", 3),
                message("user-1", "conv-1", "user", "我叫张三", 1)
        ));
        when(xBootAiService.chat(promptCaptor.capture(), eq(runtimeConfig))).thenReturn("你好张三");

        AdminAiChatBO response = aiChatService.adminChat(AdminAiChatDTO.builder()
                .conversationId("conv-1")
                .modelConfigCode("qwen")
                .content("我是谁？")
                .build());

        assertThat(response.getAnswer()).isEqualTo("你好张三");
        assertThat(promptCaptor.getValue())
                .contains("【历史对话】", "用户：我叫张三", "助手：我是小助手", "【当前用户问题】", "我是谁？")
                .doesNotContain("这条是本轮消息");
        verify(aiChatPersistenceService).completeSuccess(any(), eq("你好张三"));
    }

    @Test
    void adminChatWithKnowledgeBaseCombinesHistoryAndRagPrompt() {
        AiModelConfigBO modelConfig = enabledConfig("qwen", "qwen-plus");
        AiModelConfig runtimeConfig = runtimeConfig("qwen-plus");
        AiKnowledgeRetrievalHitBO hit = referenceHit();
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        when(aiModelConfigService.getEnabledConfigByCode("qwen", true)).thenReturn(modelConfig);
        when(aiModelConfigService.toRuntimeConfig(modelConfig)).thenReturn(runtimeConfig);
        stubPersistenceStart();
        when(aiMessageMapper.selectList(any())).thenReturn(List.of(
                message("assistant-1", "conv-1", "assistant", "你之前在问员工福利。", 2),
                message("user-1", "conv-1", "user", "我们继续聊福利制度", 1)
        ));
        when(aiKnowledgeRetrievalService.adminRetrieve(any())).thenReturn(AiKnowledgeRetrievalResultBO.builder()
                .logId(77L)
                .status(1)
                .hits(List.of(hit))
                .build());
        when(xBootAiService.chat(promptCaptor.capture(), eq(runtimeConfig))).thenReturn("依据员工手册，年假为5天");

        aiChatService.adminChat(AdminAiChatDTO.builder()
                .conversationId("conv-1")
                .modelConfigCode("qwen")
                .content("那年假几天？")
                .knowledgeBaseIds(List.of(11L))
                .build());

        assertThat(promptCaptor.getValue())
                .contains("【历史对话】", "用户：我们继续聊福利制度", "助手：你之前在问员工福利。")
                .contains("【知识库引用片段】", "员工手册.md", "【用户问题】", "那年假几天？");
    }

    @Test
    void adminChatTruncatesLongHistoryMessage() {
        AiModelConfigBO modelConfig = enabledConfig("qwen", "qwen-plus");
        AiModelConfig runtimeConfig = runtimeConfig("qwen-plus");
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        String longContent = "a".repeat(1500);
        when(aiModelConfigService.getEnabledConfigByCode("qwen", true)).thenReturn(modelConfig);
        when(aiModelConfigService.toRuntimeConfig(modelConfig)).thenReturn(runtimeConfig);
        stubPersistenceStart();
        when(aiMessageMapper.selectList(any())).thenReturn(List.of(
                message("user-1", "conv-1", "user", longContent, 1)
        ));
        when(xBootAiService.chat(promptCaptor.capture(), eq(runtimeConfig))).thenReturn("ok");

        aiChatService.adminChat(AdminAiChatDTO.builder()
                .conversationId("conv-1")
                .modelConfigCode("qwen")
                .content("继续")
                .build());

        assertThat(promptCaptor.getValue()).contains("a".repeat(1000) + "...");
        assertThat(promptCaptor.getValue()).doesNotContain("a".repeat(1200));
    }

    @Test
    void adminChatWithKnowledgeBaseRetrievesReferencesAndUsesRagPrompt() {
        AiModelConfigBO modelConfig = enabledConfig("qwen", "qwen-plus");
        AiModelConfig runtimeConfig = runtimeConfig("qwen-plus");
        AiKnowledgeRetrievalHitBO hit = referenceHit();
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        when(aiModelConfigService.getEnabledConfigByCode("qwen", true)).thenReturn(modelConfig);
        when(aiModelConfigService.toRuntimeConfig(modelConfig)).thenReturn(runtimeConfig);
        stubPersistenceStart();
        when(aiKnowledgeRetrievalService.adminRetrieve(any())).thenReturn(AiKnowledgeRetrievalResultBO.builder()
                .logId(77L)
                .status(1)
                .hits(List.of(hit))
                .build());
        when(xBootAiService.chat(promptCaptor.capture(), eq(runtimeConfig))).thenReturn("依据员工手册，年假为5天");

        AdminAiChatBO response = aiChatService.adminChat(AdminAiChatDTO.builder()
                .conversationId("conv-1")
                .modelConfigCode("qwen")
                .content("年假几天？")
                .knowledgeBaseIds(List.of(11L))
                .build());

        assertThat(response.getKnowledgeRetrievalLogId()).isEqualTo(77L);
        assertThat(response.getReferences()).containsExactly(hit);
        assertThat(response.getAnswer()).isEqualTo("依据员工手册，年假为5天");
        assertThat(promptCaptor.getValue())
                .contains("【知识库引用片段】", "员工手册", "员工手册.md", "年假制度内容", "【用户问题】", "年假几天？");
        ArgumentCaptor<AdminRetrieveAiKnowledgeDTO> retrieveCaptor = ArgumentCaptor.forClass(AdminRetrieveAiKnowledgeDTO.class);
        verify(aiKnowledgeRetrievalService).adminRetrieve(retrieveCaptor.capture());
        assertThat(retrieveCaptor.getValue().getKnowledgeBaseIds()).containsExactly(11L);
        assertThat(retrieveCaptor.getValue().getQuery()).isEqualTo("年假几天？");
        assertThat(retrieveCaptor.getValue().getConversationId()).isEqualTo("conv-1");
        assertThat(retrieveCaptor.getValue().getMessageId()).isEqualTo(response.getMessageId());
        assertThat(retrieveCaptor.getValue().getLogFlag()).isTrue();
        verify(aiChatPersistenceService).completeSuccess(any(), eq("依据员工手册，年假为5天"));
    }

    @Test
    void adminChatWithKnowledgeBaseUsesNoReferencePromptWhenRetrievalHitsEmpty() {
        AiModelConfigBO modelConfig = enabledConfig("qwen", "qwen-plus");
        AiModelConfig runtimeConfig = runtimeConfig("qwen-plus");
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        when(aiModelConfigService.getEnabledConfigByCode("qwen", true)).thenReturn(modelConfig);
        when(aiModelConfigService.toRuntimeConfig(modelConfig)).thenReturn(runtimeConfig);
        stubPersistenceStart();
        when(aiKnowledgeRetrievalService.adminRetrieve(any())).thenReturn(AiKnowledgeRetrievalResultBO.builder()
                .logId(78L)
                .status(1)
                .hits(List.of())
                .build());
        when(xBootAiService.chat(promptCaptor.capture(), eq(runtimeConfig))).thenReturn("未检索到依据，无法确认。");

        AdminAiChatBO response = aiChatService.adminChat(AdminAiChatDTO.builder()
                .conversationId("conv-1")
                .modelConfigCode("qwen")
                .content("有没有餐补？")
                .knowledgeBaseIds(List.of(11L))
                .build());

        assertThat(response.getKnowledgeRetrievalLogId()).isEqualTo(78L);
        assertThat(response.getReferences()).isEmpty();
        assertThat(response.getAnswer()).isEqualTo("未检索到依据，无法确认。");
        assertThat(promptCaptor.getValue())
                .contains("未检索到相关片段。", "如果引用片段不足以回答", "有没有餐补？");
        verify(aiChatPersistenceService).completeSuccess(any(), eq("未检索到依据，无法确认。"));
    }

    @Test
    void adminChatWithKnowledgeBasePersistsFailureWhenRetrievalFails() {
        AiModelConfigBO modelConfig = enabledConfig("qwen", "qwen-plus");
        AiModelConfig runtimeConfig = runtimeConfig("qwen-plus");
        when(aiModelConfigService.getEnabledConfigByCode("qwen", true)).thenReturn(modelConfig);
        when(aiModelConfigService.toRuntimeConfig(modelConfig)).thenReturn(runtimeConfig);
        stubPersistenceStart();
        when(aiKnowledgeRetrievalService.adminRetrieve(any())).thenReturn(AiKnowledgeRetrievalResultBO.builder()
                .status(0)
                .errorMessage("知识库向量存储不可用")
                .build());

        assertThatThrownBy(() -> aiChatService.adminChat(AdminAiChatDTO.builder()
                .conversationId("conv-1")
                .modelConfigCode("qwen")
                .content("年假几天？")
                .knowledgeBaseIds(List.of(11L))
                .build()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("知识库向量存储不可用");

        verify(xBootAiService, never()).chat(any(String.class), any(AiModelConfig.class));
        verify(aiChatPersistenceService).completeFailure(
                any(), eq("知识库向量存储不可用"), isNull(), any(BusinessException.class));
    }

    @Test
    void adminChatWithImageAttachmentsUsesMultimodalRequest() {
        AiModelConfigBO modelConfig = enabledConfig("qwen-vl", "qwen-vl-plus")
                .setProviderType("OPENAI_COMPATIBLE")
                .setSupportedModalities("text,image");
        AiModelConfig runtimeConfig = runtimeConfig("qwen-vl-plus");
        AiChatAttachmentService.ChatAttachments attachments = imageAttachments();
        ArgumentCaptor<AiChatRequest> requestCaptor = ArgumentCaptor.forClass(AiChatRequest.class);
        when(aiModelConfigService.getEnabledConfigByCode("qwen-vl", true)).thenReturn(modelConfig);
        when(aiModelConfigService.toRuntimeConfig(modelConfig)).thenReturn(runtimeConfig);
        when(aiChatAttachmentService.prepare(any(AdminAiChatDTO.class), eq(modelConfig))).thenReturn(attachments);
        when(aiChatAttachmentService.toImageMedia(attachments)).thenReturn(List.of(new AiChatMedia()
                .setMimeType("image/png")
                .setName("chart.png")
                .setData(new byte[]{1, 2, 3})));
        stubPersistenceStart();
        when(xBootAiService.chat(requestCaptor.capture(), eq(runtimeConfig))).thenReturn("这是一张图表");

        AdminAiChatBO response = aiChatService.adminChat(AdminAiChatDTO.builder()
                .conversationId("conv-1")
                .modelConfigCode("qwen-vl")
                .content("")
                .attachments(List.of(AdminAiChatAttachmentDTO.builder()
                        .ossFileId(11L)
                        .attachmentType("image")
                        .build()))
                .build());

        assertThat(response.getAnswer()).isEqualTo("这是一张图表");
        assertThat(requestCaptor.getValue().getText()).contains("请根据我上传的图片内容进行分析并回答。");
        assertThat(requestCaptor.getValue().getMedia()).hasSize(1);
        verify(xBootAiService, never()).chat(any(String.class), any(AiModelConfig.class));
        verify(aiChatPersistenceService).start(any(AdminAiChatDTO.class), eq("conv-1"), eq(modelConfig),
                eq(attachments.all()), eq(response.getMessageId()), any(), eq("chat"), eq(false));
    }

    @Test
    void adminChatThrowsWhenDefaultModelConfigMissing() {
        when(aiModelConfigService.getDefaultEnabledConfig()).thenReturn(null);

        assertThatThrownBy(() -> aiChatService.adminChat(AdminAiChatDTO.builder()
                .content("你好")
                .build()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("当前租户没有可用AI模型配置");
        verify(xBootAiService, never()).chat(any(String.class), any(AiModelConfig.class));
        verify(aiChatPersistenceService, never()).start(any(), any(), any(), anyList(), any(), any(), any(), anyBoolean());
    }

    @Test
    void adminChatThrowsWhenProvidedModelConfigCodeIsNotEnabled() {
        when(aiModelConfigService.getEnabledConfigByCode("disabled", true))
                .thenThrow(new BusinessException(AiErrorEnum.INVALID_CODE));

        assertThatThrownBy(() -> aiChatService.adminChat(AdminAiChatDTO.builder()
                .conversationId("conv-1")
                .modelConfigCode("disabled")
                .content("你好")
                .build()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("无效AI模型配置编码");
        verify(xBootAiService, never()).chat(any(String.class), any(AiModelConfig.class));
        verify(aiChatPersistenceService, never()).start(any(), any(), any(), anyList(), any(), any(), any(), anyBoolean());
    }

    @Test
    void adminChatPropagatesMissingApiKeyFromRuntimeConfig() {
        AiModelConfigBO modelConfig = enabledConfig("openai", "gpt-4o-mini").setProviderType("OPENAI");
        when(aiModelConfigService.getEnabledConfigByCode("openai", true)).thenReturn(modelConfig);
        when(aiModelConfigService.toRuntimeConfig(modelConfig)).thenThrow(new BusinessException(AiErrorEnum.MISSING_API_KEY));

        assertThatThrownBy(() -> aiChatService.adminChat(AdminAiChatDTO.builder()
                .modelConfigCode("openai")
                .content("你好")
                .build()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("AI模型配置缺少可用API Key");
        verify(xBootAiService, never()).chat(any(String.class), any(AiModelConfig.class));
        verify(aiChatPersistenceService, never()).start(any(), any(), any(), anyList(), any(), any(), any(), anyBoolean());
    }

    @Test
    void adminChatPersistsFailureBeforePropagatingProviderError() {
        AiModelConfigBO modelConfig = enabledConfig("qwen", "qwen-plus");
        AiModelConfig runtimeConfig = runtimeConfig("qwen-plus");
        RuntimeException providerError = new IllegalStateException("provider down");
        when(aiModelConfigService.getEnabledConfigByCode("qwen", true)).thenReturn(modelConfig);
        when(aiModelConfigService.toRuntimeConfig(modelConfig)).thenReturn(runtimeConfig);
        stubPersistenceStart();
        when(xBootAiService.chat(any(String.class), eq(runtimeConfig))).thenThrow(providerError);

        assertThatThrownBy(() -> aiChatService.adminChat(AdminAiChatDTO.builder()
                .conversationId("conv-1")
                .modelConfigCode("qwen")
                .content("你好")
                .build()))
                .isSameAs(providerError);

        verify(aiChatPersistenceService).completeFailure(any(), eq("provider down"), isNull(), same(providerError));
    }

    @Test
    void adminChatAddsRealtimeQuestionGuardrailsToPrompt() {
        AiModelConfigBO modelConfig = enabledConfig("qwen", "qwen-plus");
        AiModelConfig runtimeConfig = runtimeConfig("qwen-plus");
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        when(aiModelConfigService.getEnabledConfigByCode("qwen", true)).thenReturn(modelConfig);
        when(aiModelConfigService.toRuntimeConfig(modelConfig)).thenReturn(runtimeConfig);
        stubPersistenceStart();
        when(xBootAiService.chat(promptCaptor.capture(), eq(runtimeConfig))).thenReturn("当前会话没有提供可验证的实时数据。");

        aiChatService.adminChat(AdminAiChatDTO.builder()
                .conversationId("conv-1")
                .modelConfigCode("qwen")
                .content("现在 spring ai 最新的版本是哪个？")
                .build());

        assertThat(promptCaptor.getValue())
                .contains("最新版本、今天、当前、实时、官方最新、最近发布")
                .contains("不能确认“最新”结论")
                .contains("不要编造具体版本号")
                .contains("现在 spring ai 最新的版本是哪个？");
    }

    @Test
    void adminStreamEmitsMessageAndDoneEvents() {
        AiModelConfigBO modelConfig = enabledConfig("default", "llama3.2");
        AiModelConfig runtimeConfig = runtimeConfig("llama3.2");
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        when(aiModelConfigService.getDefaultEnabledConfig()).thenReturn(modelConfig);
        when(aiModelConfigService.toRuntimeConfig(modelConfig)).thenReturn(runtimeConfig);
        stubPersistenceStart();
        when(xBootAiService.stream(promptCaptor.capture(), eq(runtimeConfig))).thenReturn(Flux.just("你", "好"));

        List<AdminAiChatStreamChunkBO> chunks = aiChatService.adminStream(AdminAiChatDTO.builder()
                .conversationId("conv-1")
                .content("你好")
                .build()).collectList().block();

        assertThat(chunks).hasSize(3);
        assertThat(chunks).extracting(AdminAiChatStreamChunkBO::getEvent)
                .containsExactly(AiChatService.EVENT_MESSAGE, AiChatService.EVENT_MESSAGE, AiChatService.EVENT_DONE);
        assertThat(chunks.getLast().getContent()).isEqualTo(AiChatService.DONE_MARKER);
        assertThat(chunks.getLast().getConversationId()).isEqualTo("conv-1");
        assertThat(chunks).extracting(AdminAiChatStreamChunkBO::getMessageId).containsOnly(chunks.getFirst().getMessageId());
        assertThat(promptCaptor.getValue())
                .contains("你是企业 AI 对话助手", "【当前用户问题】", "你好");
        verify(aiChatPersistenceService).start(any(AdminAiChatDTO.class), eq("conv-1"), eq(modelConfig),
                anyList(), eq(chunks.getFirst().getMessageId()), any(), eq("stream"), eq(true));
        verify(aiChatPersistenceService).completeSuccess(any(), eq("你好"));
    }

    @Test
    void adminStreamRaisesShortRuntimeTimeoutToMinimum() {
        AiModelConfigBO modelConfig = enabledConfig("default", "llama3.2");
        AiModelConfig runtimeConfig = runtimeConfig("llama3.2").setTimeout(Duration.ofSeconds(40));
        ArgumentCaptor<AiModelConfig> runtimeConfigCaptor = ArgumentCaptor.forClass(AiModelConfig.class);
        when(aiModelConfigService.getDefaultEnabledConfig()).thenReturn(modelConfig);
        when(aiModelConfigService.toRuntimeConfig(modelConfig)).thenReturn(runtimeConfig);
        stubPersistenceStart();
        when(xBootAiService.stream(any(String.class), any(AiModelConfig.class))).thenReturn(Flux.just("好"));

        List<AdminAiChatStreamChunkBO> chunks = aiChatService.adminStream(AdminAiChatDTO.builder()
                .conversationId("conv-1")
                .content("你好")
                .build()).collectList().block();

        assertThat(chunks).hasSize(2);
        verify(xBootAiService).stream(any(String.class), runtimeConfigCaptor.capture());
        assertThat(runtimeConfigCaptor.getValue().getTimeout()).isEqualTo(Duration.ofMinutes(10));
    }

    @Test
    void adminStreamUsesRecentHistoryInPrompt() {
        AiModelConfigBO modelConfig = enabledConfig("default", "llama3.2");
        AiModelConfig runtimeConfig = runtimeConfig("llama3.2");
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        when(aiModelConfigService.getDefaultEnabledConfig()).thenReturn(modelConfig);
        when(aiModelConfigService.toRuntimeConfig(modelConfig)).thenReturn(runtimeConfig);
        stubPersistenceStart();
        when(aiMessageMapper.selectList(any())).thenReturn(List.of(
                message("assistant-1", "conv-1", "assistant", "你刚才说喜欢蓝色。", 2),
                message("user-1", "conv-1", "user", "我喜欢蓝色", 1)
        ));
        when(xBootAiService.stream(promptCaptor.capture(), eq(runtimeConfig))).thenReturn(Flux.just("蓝色"));

        List<AdminAiChatStreamChunkBO> chunks = aiChatService.adminStream(AdminAiChatDTO.builder()
                .conversationId("conv-1")
                .content("我喜欢什么颜色？")
                .build()).collectList().block();

        assertThat(chunks).extracting(AdminAiChatStreamChunkBO::getEvent)
                .containsExactly(AiChatService.EVENT_MESSAGE, AiChatService.EVENT_DONE);
        assertThat(promptCaptor.getValue())
                .contains("【历史对话】", "用户：我喜欢蓝色", "助手：你刚才说喜欢蓝色。", "我喜欢什么颜色？");
        verify(aiChatPersistenceService).completeSuccess(any(), eq("蓝色"));
    }

    @Test
    void adminStreamReturnsReferencesOnDoneWhenKnowledgeBaseSelected() {
        AiModelConfigBO modelConfig = enabledConfig("default", "llama3.2");
        AiModelConfig runtimeConfig = runtimeConfig("llama3.2");
        AiKnowledgeRetrievalHitBO hit = referenceHit();
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        when(aiModelConfigService.getDefaultEnabledConfig()).thenReturn(modelConfig);
        when(aiModelConfigService.toRuntimeConfig(modelConfig)).thenReturn(runtimeConfig);
        stubPersistenceStart();
        when(aiKnowledgeRetrievalService.adminRetrieve(any())).thenReturn(AiKnowledgeRetrievalResultBO.builder()
                .logId(77L)
                .status(1)
                .hits(List.of(hit))
                .build());
        when(xBootAiService.stream(promptCaptor.capture(), eq(runtimeConfig))).thenReturn(Flux.just("答"));

        List<AdminAiChatStreamChunkBO> chunks = aiChatService.adminStream(AdminAiChatDTO.builder()
                .conversationId("conv-1")
                .content("年假几天？")
                .knowledgeBaseIds(List.of(11L))
                .build()).collectList().block();

        assertThat(chunks).hasSize(2);
        assertThat(chunks).extracting(AdminAiChatStreamChunkBO::getEvent)
                .containsExactly(AiChatService.EVENT_MESSAGE, AiChatService.EVENT_DONE);
        assertThat(chunks.getLast().getKnowledgeRetrievalLogId()).isEqualTo(77L);
        assertThat(chunks.getLast().getReferences()).containsExactly(hit);
        assertThat(promptCaptor.getValue()).contains("【知识库引用片段】", "员工手册.md", "年假制度内容", "年假几天？");
        ArgumentCaptor<AdminRetrieveAiKnowledgeDTO> retrieveCaptor = ArgumentCaptor.forClass(AdminRetrieveAiKnowledgeDTO.class);
        verify(aiKnowledgeRetrievalService).adminRetrieve(retrieveCaptor.capture());
        assertThat(retrieveCaptor.getValue().getMessageId()).isEqualTo(chunks.getFirst().getMessageId());
        verify(aiChatPersistenceService).completeSuccess(any(), eq("答"));
    }

    @Test
    void adminStreamPersistsFailureWhenRetrievalFails() {
        AiModelConfigBO modelConfig = enabledConfig("default", "llama3.2");
        AiModelConfig runtimeConfig = runtimeConfig("llama3.2");
        when(aiModelConfigService.getDefaultEnabledConfig()).thenReturn(modelConfig);
        when(aiModelConfigService.toRuntimeConfig(modelConfig)).thenReturn(runtimeConfig);
        stubPersistenceStart();
        when(aiKnowledgeRetrievalService.adminRetrieve(any())).thenReturn(AiKnowledgeRetrievalResultBO.builder()
                .status(0)
                .errorMessage("知识库向量存储不可用")
                .build());

        List<AdminAiChatStreamChunkBO> chunks = aiChatService.adminStream(AdminAiChatDTO.builder()
                .conversationId("conv-1")
                .content("年假几天？")
                .knowledgeBaseIds(List.of(11L))
                .build()).collectList().block();

        assertThat(chunks).hasSize(1);
        assertThat(chunks.getFirst().getEvent()).isEqualTo(AiChatService.EVENT_ERROR);
        assertThat(chunks.getFirst().getContent()).isEqualTo("知识库向量存储不可用");
        verify(xBootAiService, never()).stream(any(String.class), any(AiModelConfig.class));
        verify(aiChatPersistenceService).completeFailure(
                any(), eq("知识库向量存储不可用"), isNull(), any(BusinessException.class));
    }

    @Test
    void adminStreamReturnsErrorEventWhenModelConfigMissing() {
        when(aiModelConfigService.getDefaultEnabledConfig()).thenReturn(null);

        List<AdminAiChatStreamChunkBO> chunks = aiChatService.adminStream(AdminAiChatDTO.builder()
                .conversationId("conv-1")
                .content("你好")
                .build()).collectList().block();

        assertThat(chunks).hasSize(1);
        assertThat(chunks.getFirst().getEvent()).isEqualTo(AiChatService.EVENT_ERROR);
        assertThat(chunks.getFirst().getFinish()).isTrue();
        assertThat(chunks.getFirst().getContent()).isEqualTo("当前租户没有可用AI模型配置");
        verify(xBootAiService, never()).stream(any(String.class), any(AiModelConfig.class));
        verify(aiChatPersistenceService, never()).start(any(), any(), any(), anyList(), any(), any(), any(), anyBoolean());
    }

    @Test
    void adminStreamPersistsFailureWhenProviderStreamFails() {
        AiModelConfigBO modelConfig = enabledConfig("default", "llama3.2");
        AiModelConfig runtimeConfig = runtimeConfig("llama3.2");
        RuntimeException providerError = new IllegalStateException("provider down");
        when(aiModelConfigService.getDefaultEnabledConfig()).thenReturn(modelConfig);
        when(aiModelConfigService.toRuntimeConfig(modelConfig)).thenReturn(runtimeConfig);
        stubPersistenceStart();
        when(xBootAiService.stream(any(String.class), eq(runtimeConfig)))
                .thenReturn(Flux.concat(Flux.just("半"), Flux.error(providerError)));

        List<AdminAiChatStreamChunkBO> chunks = aiChatService.adminStream(AdminAiChatDTO.builder()
                .conversationId("conv-1")
                .content("你好")
                .build()).collectList().block();

        assertThat(chunks).hasSize(2);
        assertThat(chunks).extracting(AdminAiChatStreamChunkBO::getEvent)
                .containsExactly(AiChatService.EVENT_MESSAGE, AiChatService.EVENT_ERROR);
        assertThat(chunks.getLast().getContent()).isEqualTo("provider down");
        assertThat(chunks).extracting(AdminAiChatStreamChunkBO::getMessageId).containsOnly(chunks.getFirst().getMessageId());
        verify(aiChatPersistenceService).completeFailure(any(), eq("provider down"), eq("半"), same(providerError));
    }

    @Test
    void adminStreamWithImageAttachmentsUsesMultimodalStreamRequest() {
        AiModelConfigBO modelConfig = enabledConfig("qwen-vl", "qwen-vl-plus")
                .setSupportedModalities("text,image");
        AiModelConfig runtimeConfig = runtimeConfig("qwen-vl-plus");
        AiChatAttachmentService.ChatAttachments attachments = imageAttachments();
        ArgumentCaptor<AiChatRequest> requestCaptor = ArgumentCaptor.forClass(AiChatRequest.class);
        when(aiModelConfigService.getDefaultEnabledConfig()).thenReturn(modelConfig);
        when(aiModelConfigService.toRuntimeConfig(modelConfig)).thenReturn(runtimeConfig);
        when(aiChatAttachmentService.prepare(any(AdminAiChatDTO.class), eq(modelConfig))).thenReturn(attachments);
        when(aiChatAttachmentService.toImageMedia(attachments)).thenReturn(List.of(new AiChatMedia()
                .setMimeType("image/png")
                .setName("chart.png")
                .setData(new byte[]{1, 2, 3})));
        stubPersistenceStart();
        when(xBootAiService.stream(requestCaptor.capture(), eq(runtimeConfig))).thenReturn(Flux.just("图", "表"));

        List<AdminAiChatStreamChunkBO> chunks = aiChatService.adminStream(AdminAiChatDTO.builder()
                .conversationId("conv-1")
                .attachments(List.of(AdminAiChatAttachmentDTO.builder()
                        .ossFileId(11L)
                        .attachmentType("image")
                        .build()))
                .build()).collectList().block();

        assertThat(chunks).hasSize(3);
        assertThat(requestCaptor.getValue().getText()).contains("请根据我上传的图片内容进行分析并回答。");
        assertThat(requestCaptor.getValue().getMedia()).hasSize(1);
        verify(xBootAiService, never()).stream(any(String.class), any(AiModelConfig.class));
        verify(aiChatPersistenceService).completeSuccess(any(), eq("图表"));
    }

    @Test
    void adminStreamReturnsErrorEventWhenPersistenceStartFails() {
        AiModelConfigBO modelConfig = enabledConfig("default", "llama3.2");
        AiModelConfig runtimeConfig = runtimeConfig("llama3.2");
        when(aiModelConfigService.getDefaultEnabledConfig()).thenReturn(modelConfig);
        when(aiModelConfigService.toRuntimeConfig(modelConfig)).thenReturn(runtimeConfig);
        when(aiChatPersistenceService.start(any(AdminAiChatDTO.class), any(), any(), anyList(), any(), any(), any(), anyBoolean()))
                .thenThrow(new IllegalStateException("db down"));

        List<AdminAiChatStreamChunkBO> chunks = aiChatService.adminStream(AdminAiChatDTO.builder()
                .conversationId("conv-1")
                .content("你好")
                .build()).collectList().block();

        assertThat(chunks).hasSize(1);
        assertThat(chunks.getFirst().getEvent()).isEqualTo(AiChatService.EVENT_ERROR);
        assertThat(chunks.getFirst().getContent()).isEqualTo("db down");
        assertThat(chunks.getFirst().getConversationId()).isEqualTo("conv-1");
        assertThat(chunks.getFirst().getFinish()).isTrue();
        verify(xBootAiService, never()).stream(any(String.class), any(AiModelConfig.class));
        verify(aiChatPersistenceService, never()).completeFailure(any(), any(), any(), any());
    }

    private AiModelConfigBO enabledConfig(String code, String modelName) {
        return AiModelConfigBO.builder()
                .id(1L)
                .code(code)
                .name("默认模型")
                .providerType("OLLAMA")
                .baseUrl("http://localhost:11434")
                .modelName(modelName)
                .supportedModalities("text")
                .supportedCapabilities(AiModelCapabilityConstant.CHAT)
                .status(EnabledStatusEnum.ENABLED.getValue())
                .defaultFlag(YesOrNoEnum.YES.getValue())
                .build();
    }

    private AiModelConfig runtimeConfig(String modelName) {
        return new AiModelConfig()
                .setProviderType(AiProviderTypeEnum.OLLAMA)
                .setBaseUrl("http://localhost:11434")
                .setModelName(modelName);
    }

    private AiKnowledgeRetrievalHitBO referenceHit() {
        return AiKnowledgeRetrievalHitBO.builder()
                .knowledgeBaseId(11L)
                .knowledgeBaseName("员工手册")
                .documentId(21L)
                .documentName("员工手册.md")
                .chunkId(31L)
                .chunkNo(1)
                .content("年假制度内容：入职满一年可享受5天年假。")
                .similarityScore(0.91)
                .build();
    }

    private void stubPersistenceStart() {
        when(aiChatPersistenceService.start(any(AdminAiChatDTO.class), any(), any(), anyList(), any(), any(), any(), anyBoolean()))
                .thenAnswer(invocation -> new AiChatPersistenceService.PersistenceContext(
                        invocation.getArgument(1, String.class),
                        "user-message-id",
                        invocation.getArgument(4, String.class),
                        invocation.getArgument(5, String.class),
                        1L,
                        invocation.getArgument(2, AiModelConfigBO.class),
                        invocation.getArgument(6, String.class),
                        invocation.getArgument(7, Boolean.class),
                        0,
                        System.currentTimeMillis()
                ));
    }

    private AiChatAttachmentService.ChatAttachments imageAttachments() {
        AdminAiMessageAttachmentBO image = AdminAiMessageAttachmentBO.builder()
                .ossFileId(11L)
                .attachmentType("image")
                .fileName("chart.png")
                .mimeType("image/png")
                .fileSize(1024L)
                .sortNo(0)
                .build();
        return new AiChatAttachmentService.ChatAttachments(List.of(image), List.of(image));
    }

    private AiMessageEntity message(String messageId, String conversationId, String role, String content, int sequenceNo) {
        return AiMessageEntity.builder()
                .messageId(messageId)
                .conversationId(conversationId)
                .role(role)
                .content(content)
                .status(1)
                .sequenceNo(sequenceNo)
                .build();
    }
}
