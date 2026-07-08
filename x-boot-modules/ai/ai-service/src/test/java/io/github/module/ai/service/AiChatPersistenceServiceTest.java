package io.github.module.ai.service;

import io.github.framework.core.context.UserContext;
import io.github.framework.core.context.UserContextHolder;
import io.github.module.ai.entity.AiCallLogEntity;
import io.github.module.ai.entity.AiConversationEntity;
import io.github.module.ai.entity.AiMessageAttachmentEntity;
import io.github.module.ai.entity.AiMessageEntity;
import io.github.module.ai.mapper.AiCallLogMapper;
import io.github.module.ai.mapper.AiConversationMapper;
import io.github.module.ai.mapper.AiMessageAttachmentMapper;
import io.github.module.ai.mapper.AiMessageMapper;
import io.github.module.ai.model.request.AdminAiChatDTO;
import io.github.module.ai.model.response.AdminAiMessageAttachmentBO;
import io.github.module.ai.model.response.AiModelConfigBO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiChatPersistenceServiceTest {

    @Mock
    private AiConversationMapper aiConversationMapper;

    @Mock
    private AiMessageMapper aiMessageMapper;

    @Mock
    private AiCallLogMapper aiCallLogMapper;

    @Mock
    private AiMessageAttachmentMapper aiMessageAttachmentMapper;

    private AiChatPersistenceService aiChatPersistenceService;

    @BeforeEach
    void setUp() {
        UserContextHolder.setUserContext(new UserContext()
                .setUserId(9L)
                .setUserName("admin"));
        aiChatPersistenceService = new AiChatPersistenceService(
                aiConversationMapper, aiMessageMapper, aiCallLogMapper, aiMessageAttachmentMapper);
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void startCreatesConversationUserMessageAndRunningCallLog() {
        AiModelConfigBO modelConfig = modelConfig();
        AdminAiChatDTO dto = AdminAiChatDTO.builder()
                .conversationId("conv-1")
                .content("你好")
                .build();

        AiChatPersistenceService.PersistenceContext context = aiChatPersistenceService.start(
                dto, "conv-1", modelConfig, List.of(), "assistant-msg-1", "call-1", "chat", false);

        assertThat(context.conversationId()).isEqualTo("conv-1");
        assertThat(context.userMessageId()).isNotBlank();
        assertThat(context.assistantMessageId()).isEqualTo("assistant-msg-1");
        assertThat(context.callId()).isEqualTo("call-1");
        assertThat(context.userId()).isEqualTo(9L);
        assertThat(context.baseMessageCount()).isZero();

        ArgumentCaptor<AiConversationEntity> conversationCaptor = ArgumentCaptor.forClass(AiConversationEntity.class);
        verify(aiConversationMapper).insert(conversationCaptor.capture());
        AiConversationEntity conversation = conversationCaptor.getValue();
        assertThat(conversation.getConversationId()).isEqualTo("conv-1");
        assertThat(conversation.getUserId()).isEqualTo(9L);
        assertThat(conversation.getTitle()).isEqualTo("你好");
        assertThat(conversation.getModelConfigCode()).isEqualTo("qwen");
        assertThat(conversation.getStatus()).isEqualTo(1);
        assertThat(conversation.getMessageCount()).isEqualTo(1);
        assertThat(conversation.getLastMessagePreview()).isEqualTo("你好");

        ArgumentCaptor<AiMessageEntity> messageCaptor = ArgumentCaptor.forClass(AiMessageEntity.class);
        verify(aiMessageMapper).insert(messageCaptor.capture());
        AiMessageEntity message = messageCaptor.getValue();
        assertThat(message.getMessageId()).isEqualTo(context.userMessageId());
        assertThat(message.getConversationId()).isEqualTo("conv-1");
        assertThat(message.getRole()).isEqualTo("user");
        assertThat(message.getContent()).isEqualTo("你好");
        assertThat(message.getContentType()).isEqualTo("text");
        assertThat(message.getStatus()).isEqualTo(1);
        assertThat(message.getSequenceNo()).isEqualTo(1);

        ArgumentCaptor<AiCallLogEntity> callLogCaptor = ArgumentCaptor.forClass(AiCallLogEntity.class);
        verify(aiCallLogMapper).insert(callLogCaptor.capture());
        AiCallLogEntity callLog = callLogCaptor.getValue();
        assertThat(callLog.getCallId()).isEqualTo("call-1");
        assertThat(callLog.getMessageId()).isEqualTo("assistant-msg-1");
        assertThat(callLog.getUserId()).isEqualTo(9L);
        assertThat(callLog.getRequestType()).isEqualTo("chat");
        assertThat(callLog.getStreamFlag()).isZero();
        assertThat(callLog.getRequestPreview()).isEqualTo("你好");
        assertThat(callLog.getResponsePreview()).isEmpty();
        assertThat(callLog.getStatus()).isEqualTo(2);
    }

    @Test
    void startUpdatesExistingConversationAndAppendsUserMessage() {
        AiModelConfigBO modelConfig = modelConfig();
        AdminAiChatDTO dto = AdminAiChatDTO.builder()
                .conversationId("conv-1")
                .content("继续聊")
                .build();
        AiConversationEntity existingConversation = AiConversationEntity.builder()
                .conversationId("conv-1")
                .messageCount(4)
                .build();
        when(aiConversationMapper.selectOne(any())).thenReturn(existingConversation);

        AiChatPersistenceService.PersistenceContext context = aiChatPersistenceService.start(
                dto, "conv-1", modelConfig, List.of(), "assistant-msg-1", "call-1", "stream", true);

        assertThat(context.baseMessageCount()).isEqualTo(4);
        assertThat(context.requestType()).isEqualTo("stream");
        assertThat(context.streamFlag()).isTrue();
        verify(aiConversationMapper, never()).insert(any(AiConversationEntity.class));

        ArgumentCaptor<AiConversationEntity> conversationCaptor = ArgumentCaptor.forClass(AiConversationEntity.class);
        verify(aiConversationMapper).update(conversationCaptor.capture(), any());
        AiConversationEntity conversation = conversationCaptor.getValue();
        assertThat(conversation.getMessageCount()).isEqualTo(5);
        assertThat(conversation.getLastMessagePreview()).isEqualTo("继续聊");
        assertThat(conversation.getModelConfigCode()).isEqualTo("qwen");

        ArgumentCaptor<AiMessageEntity> messageCaptor = ArgumentCaptor.forClass(AiMessageEntity.class);
        verify(aiMessageMapper).insert(messageCaptor.capture());
        AiMessageEntity message = messageCaptor.getValue();
        assertThat(message.getRole()).isEqualTo("user");
        assertThat(message.getContent()).isEqualTo("继续聊");
        assertThat(message.getSequenceNo()).isEqualTo(5);

        ArgumentCaptor<AiCallLogEntity> callLogCaptor = ArgumentCaptor.forClass(AiCallLogEntity.class);
        verify(aiCallLogMapper).insert(callLogCaptor.capture());
        AiCallLogEntity callLog = callLogCaptor.getValue();
        assertThat(callLog.getRequestType()).isEqualTo("stream");
        assertThat(callLog.getStreamFlag()).isEqualTo(1);
        assertThat(callLog.getStatus()).isEqualTo(2);
    }

    @Test
    void startPersistsUserMessageAttachments() {
        AiModelConfigBO modelConfig = modelConfig();
        AdminAiChatDTO dto = AdminAiChatDTO.builder()
                .conversationId("conv-1")
                .content("")
                .build();
        List<AdminAiMessageAttachmentBO> attachments = List.of(AdminAiMessageAttachmentBO.builder()
                .ossFileId(11L)
                .attachmentType("image")
                .fileName("chart.png")
                .mimeType("image/png")
                .fileSize(1024L)
                .sortNo(0)
                .build());

        AiChatPersistenceService.PersistenceContext context = aiChatPersistenceService.start(
                dto, "conv-1", modelConfig, attachments, "assistant-msg-1", "call-1", "chat", false);

        ArgumentCaptor<AiMessageEntity> messageCaptor = ArgumentCaptor.forClass(AiMessageEntity.class);
        verify(aiMessageMapper).insert(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getContentType()).isEqualTo("multimodal");

        ArgumentCaptor<AiMessageAttachmentEntity> attachmentCaptor = ArgumentCaptor.forClass(AiMessageAttachmentEntity.class);
        verify(aiMessageAttachmentMapper).insert(attachmentCaptor.capture());
        AiMessageAttachmentEntity attachment = attachmentCaptor.getValue();
        assertThat(attachment.getMessageId()).isEqualTo(context.userMessageId());
        assertThat(attachment.getConversationId()).isEqualTo("conv-1");
        assertThat(attachment.getOssFileId()).isEqualTo(11L);
        assertThat(attachment.getAttachmentType()).isEqualTo("image");
        assertThat(attachment.getFileName()).isEqualTo("chart.png");
        assertThat(attachment.getMimeType()).isEqualTo("image/png");
        assertThat(attachment.getFileSize()).isEqualTo(1024L);
        assertThat(attachment.getSortNo()).isZero();
    }

    @Test
    void completeSuccessPersistsAssistantMessageAndUpdatesConversationAndCallLog() {
        AiChatPersistenceService.PersistenceContext context = context(2);

        aiChatPersistenceService.completeSuccess(context, "你好呀");

        ArgumentCaptor<AiMessageEntity> messageCaptor = ArgumentCaptor.forClass(AiMessageEntity.class);
        verify(aiMessageMapper).insert(messageCaptor.capture());
        AiMessageEntity message = messageCaptor.getValue();
        assertThat(message.getMessageId()).isEqualTo("assistant-msg-1");
        assertThat(message.getParentMessageId()).isEqualTo("user-msg-1");
        assertThat(message.getRole()).isEqualTo("assistant");
        assertThat(message.getContent()).isEqualTo("你好呀");
        assertThat(message.getStatus()).isEqualTo(1);
        assertThat(message.getSequenceNo()).isEqualTo(4);

        ArgumentCaptor<AiConversationEntity> conversationCaptor = ArgumentCaptor.forClass(AiConversationEntity.class);
        verify(aiConversationMapper).update(conversationCaptor.capture(), any());
        AiConversationEntity conversation = conversationCaptor.getValue();
        assertThat(conversation.getMessageCount()).isEqualTo(4);
        assertThat(conversation.getLastMessagePreview()).isEqualTo("你好呀");
        assertThat(conversation.getModelConfigCode()).isEqualTo("qwen");

        ArgumentCaptor<AiCallLogEntity> callLogCaptor = ArgumentCaptor.forClass(AiCallLogEntity.class);
        verify(aiCallLogMapper).update(callLogCaptor.capture(), any());
        AiCallLogEntity callLog = callLogCaptor.getValue();
        assertThat(callLog.getResponsePreview()).isEqualTo("你好呀");
        assertThat(callLog.getStatus()).isEqualTo(1);
        assertThat(callLog.getDurationMs()).isNotNull();
        assertThat(callLog.getFinishReason()).isEqualTo("stop");
        assertThat(callLog.getFinishedAt()).isNotNull();
    }

    @Test
    void completeFailurePersistsAssistantErrorMessageAndUpdatesCallLog() {
        AiChatPersistenceService.PersistenceContext context = context(0);
        RuntimeException providerError = new IllegalStateException("provider down");

        aiChatPersistenceService.completeFailure(context, "provider down", "partial answer", providerError);

        ArgumentCaptor<AiMessageEntity> messageCaptor = ArgumentCaptor.forClass(AiMessageEntity.class);
        verify(aiMessageMapper).insert(messageCaptor.capture());
        AiMessageEntity message = messageCaptor.getValue();
        assertThat(message.getRole()).isEqualTo("assistant");
        assertThat(message.getContent()).isEqualTo("partial answer");
        assertThat(message.getStatus()).isZero();
        assertThat(message.getSequenceNo()).isEqualTo(2);
        assertThat(message.getErrorCode()).isEqualTo("IllegalStateException");
        assertThat(message.getErrorMessage()).isEqualTo("provider down");

        ArgumentCaptor<AiConversationEntity> conversationCaptor = ArgumentCaptor.forClass(AiConversationEntity.class);
        verify(aiConversationMapper).update(conversationCaptor.capture(), any());
        assertThat(conversationCaptor.getValue().getMessageCount()).isEqualTo(2);
        assertThat(conversationCaptor.getValue().getLastMessagePreview()).isEqualTo("provider down");

        ArgumentCaptor<AiCallLogEntity> callLogCaptor = ArgumentCaptor.forClass(AiCallLogEntity.class);
        verify(aiCallLogMapper).update(callLogCaptor.capture(), any());
        AiCallLogEntity callLog = callLogCaptor.getValue();
        assertThat(callLog.getResponsePreview()).isEqualTo("partial answer");
        assertThat(callLog.getStatus()).isZero();
        assertThat(callLog.getErrorCode()).isEqualTo("IllegalStateException");
        assertThat(callLog.getErrorMessage()).isEqualTo("provider down");
        assertThat(callLog.getFinishedAt()).isNotNull();
    }

    @Test
    void completeFailureUsesErrorMessageWhenPartialAnswerBlank() {
        AiChatPersistenceService.PersistenceContext context = context(0);
        RuntimeException providerError = new IllegalStateException("provider down");

        aiChatPersistenceService.completeFailure(context, "provider down", "", providerError);

        ArgumentCaptor<AiMessageEntity> messageCaptor = ArgumentCaptor.forClass(AiMessageEntity.class);
        verify(aiMessageMapper).insert(messageCaptor.capture());
        AiMessageEntity message = messageCaptor.getValue();
        assertThat(message.getContent()).isEqualTo("provider down");
        assertThat(message.getStatus()).isZero();
        assertThat(message.getSequenceNo()).isEqualTo(2);
        assertThat(message.getErrorCode()).isEqualTo("IllegalStateException");
        assertThat(message.getErrorMessage()).isEqualTo("provider down");

        ArgumentCaptor<AiCallLogEntity> callLogCaptor = ArgumentCaptor.forClass(AiCallLogEntity.class);
        verify(aiCallLogMapper).update(callLogCaptor.capture(), any());
        AiCallLogEntity callLog = callLogCaptor.getValue();
        assertThat(callLog.getResponsePreview()).isEmpty();
        assertThat(callLog.getStatus()).isZero();
        assertThat(callLog.getErrorCode()).isEqualTo("IllegalStateException");
        assertThat(callLog.getErrorMessage()).isEqualTo("provider down");
    }

    private AiChatPersistenceService.PersistenceContext context(int baseMessageCount) {
        return new AiChatPersistenceService.PersistenceContext(
                "conv-1",
                "user-msg-1",
                "assistant-msg-1",
                "call-1",
                9L,
                modelConfig(),
                "chat",
                false,
                baseMessageCount,
                System.currentTimeMillis()
        );
    }

    private AiModelConfigBO modelConfig() {
        return AiModelConfigBO.builder()
                .id(1L)
                .code("qwen")
                .name("通义千问")
                .providerType("OPENAI_COMPATIBLE")
                .modelName("qwen-plus")
                .build();
    }
}
