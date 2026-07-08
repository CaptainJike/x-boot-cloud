package io.github.module.ai.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.context.UserContextHolder;
import io.github.framework.core.enums.YesOrNoEnum;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 后台 AI 对话持久化服务.
 */
@RequiredArgsConstructor
@Service
public class AiChatPersistenceService {

    private static final int CONVERSATION_STATUS_ACTIVE = 1;

    private static final int MESSAGE_STATUS_FAILED = 0;

    private static final int MESSAGE_STATUS_SUCCESS = 1;

    private static final int CALL_STATUS_FAILED = 0;

    private static final int CALL_STATUS_SUCCESS = 1;

    private static final int CALL_STATUS_RUNNING = 2;

    private static final String ROLE_USER = "user";

    private static final String ROLE_ASSISTANT = "assistant";

    private static final String CONTENT_TYPE_TEXT = "text";

    private static final String CONTENT_TYPE_MULTIMODAL = "multimodal";

    private static final String FINISH_REASON_STOP = "stop";

    private static final int TITLE_LENGTH = 200;

    private static final int CONVERSATION_PREVIEW_LENGTH = 500;

    private static final int CALL_PREVIEW_LENGTH = 1000;

    private static final int ERROR_MESSAGE_LENGTH = 1000;

    private final AiConversationMapper aiConversationMapper;

    private final AiMessageMapper aiMessageMapper;

    private final AiCallLogMapper aiCallLogMapper;

    private final AiMessageAttachmentMapper aiMessageAttachmentMapper;

    /**
     * 创建或更新会话起始状态，并保存用户消息和调用中日志.
     */
    @Transactional(rollbackFor = Exception.class)
    public PersistenceContext start(AdminAiChatDTO dto,
                                    String conversationId,
                                    AiModelConfigBO modelConfig,
                                    List<AdminAiMessageAttachmentBO> attachments,
                                    String assistantMessageId,
                                    String callId,
                                    String requestType,
                                    boolean streamFlag) {
        LocalDateTime now = LocalDateTime.now();
        Long userId = currentUserId();
        AiConversationEntity conversation = findConversation(conversationId);
        int baseMessageCount = messageCount(conversation);
        String userMessageId = newId();
        List<AdminAiMessageAttachmentBO> safeAttachments = safeAttachments(attachments);
        String requestPreview = requestPreview(dto.getContent(), safeAttachments);

        if (conversation == null) {
            aiConversationMapper.insert(newConversation(conversationId, userId, requestPreview, modelConfig, baseMessageCount + 1, now));
        } else {
            updateConversation(conversationId, modelConfig, baseMessageCount + 1, requestPreview, now);
        }
        aiMessageMapper.insert(userMessage(dto, conversationId, userMessageId, modelConfig,
                safeAttachments, baseMessageCount + 1, now));
        saveAttachments(conversationId, userMessageId, safeAttachments);
        aiCallLogMapper.insert(runningCallLog(requestPreview, conversationId, assistantMessageId, callId, userId, modelConfig,
                requestType, streamFlag, now));

        return new PersistenceContext(conversationId, userMessageId, assistantMessageId, callId, userId,
                modelConfig, requestType, streamFlag, baseMessageCount, System.currentTimeMillis());
    }

    /**
     * 保存成功回复，并更新会话与调用日志.
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeSuccess(PersistenceContext context, String answer) {
        LocalDateTime now = LocalDateTime.now();
        String safeAnswer = StrUtil.nullToEmpty(answer);
        aiMessageMapper.insert(assistantMessage(context, safeAnswer, MESSAGE_STATUS_SUCCESS, null, null, now));
        updateConversation(context.conversationId(), context.modelConfig(), context.assistantSequenceNo(), safeAnswer, now);
        updateCallLogSuccess(context, safeAnswer, now);
    }

    /**
     * 保存失败回复，并更新会话与调用日志.
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeFailure(PersistenceContext context, String errorMessage, String partialAnswer, Throwable throwable) {
        LocalDateTime now = LocalDateTime.now();
        String safeErrorMessage = preview(errorMessage, ERROR_MESSAGE_LENGTH);
        String safePartialAnswer = StrUtil.nullToEmpty(partialAnswer);
        String content = StrUtil.blankToDefault(safePartialAnswer, safeErrorMessage);
        String errorCode = errorCode(throwable);

        aiMessageMapper.insert(assistantMessage(context, content, MESSAGE_STATUS_FAILED, errorCode, safeErrorMessage, now));
        updateConversation(context.conversationId(), context.modelConfig(), context.assistantSequenceNo(), safeErrorMessage, now);
        updateCallLogFailure(context, safePartialAnswer, errorCode, safeErrorMessage, now);
    }

    private AiConversationEntity findConversation(String conversationId) {
        return aiConversationMapper.selectOne(new QueryWrapper<AiConversationEntity>()
                .lambda()
                .eq(AiConversationEntity::getConversationId, conversationId)
                .last(BaseConstant.CRUD.SQL_LIMIT_1));
    }

    private AiConversationEntity newConversation(String conversationId,
                                                 Long userId,
                                                 String content,
                                                 AiModelConfigBO modelConfig,
                                                 int messageCount,
                                                 LocalDateTime now) {
        return AiConversationEntity.builder()
                .conversationId(conversationId)
                .userId(userId)
                .title(preview(content, TITLE_LENGTH))
                .modelConfigId(modelConfig.getId())
                .modelConfigCode(modelConfig.getCode())
                .providerType(modelConfig.getProviderType())
                .modelName(modelConfig.getModelName())
                .status(CONVERSATION_STATUS_ACTIVE)
                .messageCount(messageCount)
                .lastMessageAt(now)
                .lastMessagePreview(preview(content, CONVERSATION_PREVIEW_LENGTH))
                .build();
    }

    private AiMessageEntity userMessage(AdminAiChatDTO dto,
                                        String conversationId,
                                        String userMessageId,
                                        AiModelConfigBO modelConfig,
                                        List<AdminAiMessageAttachmentBO> attachments,
                                        int sequenceNo,
                                        LocalDateTime now) {
        return AiMessageEntity.builder()
                .messageId(userMessageId)
                .conversationId(conversationId)
                .role(ROLE_USER)
                .content(StrUtil.nullToEmpty(dto.getContent()))
                .contentType(CollUtil.isEmpty(attachments) ? CONTENT_TYPE_TEXT : CONTENT_TYPE_MULTIMODAL)
                .modelConfigId(modelConfig.getId())
                .modelConfigCode(modelConfig.getCode())
                .providerType(modelConfig.getProviderType())
                .modelName(modelConfig.getModelName())
                .status(MESSAGE_STATUS_SUCCESS)
                .sequenceNo(sequenceNo)
                .sentAt(now)
                .build();
    }

    private void saveAttachments(String conversationId,
                                 String userMessageId,
                                 List<AdminAiMessageAttachmentBO> attachments) {
        if (CollUtil.isEmpty(attachments)) {
            return;
        }
        for (AdminAiMessageAttachmentBO attachment : attachments) {
            aiMessageAttachmentMapper.insert(AiMessageAttachmentEntity.builder()
                    .messageId(userMessageId)
                    .conversationId(conversationId)
                    .ossFileId(attachment.getOssFileId())
                    .attachmentType(attachment.getAttachmentType())
                    .fileName(attachment.getFileName())
                    .mimeType(attachment.getMimeType())
                    .fileSize(attachment.getFileSize())
                    .sortNo(attachment.getSortNo())
                    .build());
        }
    }

    private AiMessageEntity assistantMessage(PersistenceContext context,
                                             String content,
                                             int status,
                                             String errorCode,
                                             String errorMessage,
                                             LocalDateTime now) {
        AiModelConfigBO modelConfig = context.modelConfig();
        return AiMessageEntity.builder()
                .messageId(context.assistantMessageId())
                .conversationId(context.conversationId())
                .parentMessageId(context.userMessageId())
                .role(ROLE_ASSISTANT)
                .content(StrUtil.nullToEmpty(content))
                .contentType(CONTENT_TYPE_TEXT)
                .modelConfigId(modelConfig.getId())
                .modelConfigCode(modelConfig.getCode())
                .providerType(modelConfig.getProviderType())
                .modelName(modelConfig.getModelName())
                .status(status)
                .sequenceNo(context.assistantSequenceNo())
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .sentAt(now)
                .build();
    }

    private AiCallLogEntity runningCallLog(String requestPreview,
                                           String conversationId,
                                           String assistantMessageId,
                                           String callId,
                                           Long userId,
                                           AiModelConfigBO modelConfig,
                                           String requestType,
                                           boolean streamFlag,
                                           LocalDateTime now) {
        return AiCallLogEntity.builder()
                .callId(callId)
                .conversationId(conversationId)
                .messageId(assistantMessageId)
                .userId(userId)
                .modelConfigId(modelConfig.getId())
                .modelConfigCode(modelConfig.getCode())
                .providerType(modelConfig.getProviderType())
                .modelName(modelConfig.getModelName())
                .requestType(requestType)
                .streamFlag(streamFlag ? YesOrNoEnum.YES.getValue() : YesOrNoEnum.NO.getValue())
                .requestPreview(preview(requestPreview, CALL_PREVIEW_LENGTH))
                .responsePreview("")
                .status(CALL_STATUS_RUNNING)
                .startedAt(now)
                .build();
    }

    private void updateConversation(String conversationId,
                                    AiModelConfigBO modelConfig,
                                    int messageCount,
                                    String lastMessagePreview,
                                    LocalDateTime now) {
        AiConversationEntity entity = AiConversationEntity.builder()
                .modelConfigId(modelConfig.getId())
                .modelConfigCode(modelConfig.getCode())
                .providerType(modelConfig.getProviderType())
                .modelName(modelConfig.getModelName())
                .status(CONVERSATION_STATUS_ACTIVE)
                .messageCount(messageCount)
                .lastMessageAt(now)
                .lastMessagePreview(preview(lastMessagePreview, CONVERSATION_PREVIEW_LENGTH))
                .build();
        aiConversationMapper.update(entity, new UpdateWrapper<AiConversationEntity>()
                .lambda()
                .eq(AiConversationEntity::getConversationId, conversationId));
    }

    private void updateCallLogSuccess(PersistenceContext context, String answer, LocalDateTime now) {
        AiCallLogEntity entity = AiCallLogEntity.builder()
                .responsePreview(preview(answer, CALL_PREVIEW_LENGTH))
                .status(CALL_STATUS_SUCCESS)
                .durationMs(durationMillis(context.startedAtMillis()))
                .finishReason(FINISH_REASON_STOP)
                .finishedAt(now)
                .build();
        aiCallLogMapper.update(entity, new UpdateWrapper<AiCallLogEntity>()
                .lambda()
                .eq(AiCallLogEntity::getCallId, context.callId()));
    }

    private void updateCallLogFailure(PersistenceContext context,
                                      String partialAnswer,
                                      String errorCode,
                                      String errorMessage,
                                      LocalDateTime now) {
        AiCallLogEntity entity = AiCallLogEntity.builder()
                .responsePreview(preview(partialAnswer, CALL_PREVIEW_LENGTH))
                .status(CALL_STATUS_FAILED)
                .durationMs(durationMillis(context.startedAtMillis()))
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .finishedAt(now)
                .build();
        aiCallLogMapper.update(entity, new UpdateWrapper<AiCallLogEntity>()
                .lambda()
                .eq(AiCallLogEntity::getCallId, context.callId()));
    }

    private Long currentUserId() {
        Long userId = UserContextHolder.getUserId();
        return userId == null ? 0L : userId;
    }

    private int messageCount(AiConversationEntity conversation) {
        if (conversation == null || conversation.getMessageCount() == null) {
            return 0;
        }
        return conversation.getMessageCount();
    }

    private long durationMillis(long startedAtMillis) {
        return Math.max(System.currentTimeMillis() - startedAtMillis, 0L);
    }

    private String errorCode(Throwable throwable) {
        return throwable == null ? "UNKNOWN" : preview(throwable.getClass().getSimpleName(), 64);
    }

    private String preview(String value, int maxLength) {
        String cleanValue = StrUtil.trimToEmpty(value);
        if (cleanValue.length() <= maxLength) {
            return cleanValue;
        }
        return cleanValue.substring(0, maxLength);
    }

    private List<AdminAiMessageAttachmentBO> safeAttachments(List<AdminAiMessageAttachmentBO> attachments) {
        return attachments == null ? Collections.emptyList() : attachments;
    }

    private String requestPreview(String content, List<AdminAiMessageAttachmentBO> attachments) {
        String cleanContent = StrUtil.trimToEmpty(content);
        if (CollUtil.isEmpty(attachments)) {
            return cleanContent;
        }
        String attachmentPreview = attachments.stream()
                .map(attachment -> String.format("[%s]%s",
                        StrUtil.blankToDefault(attachment.getAttachmentType(), "file"),
                        StrUtil.blankToDefault(attachment.getFileName(), StrUtil.toString(attachment.getOssFileId()))))
                .reduce((left, right) -> left + " " + right)
                .orElse("");
        if (StrUtil.isBlank(cleanContent)) {
            return attachmentPreview;
        }
        return cleanContent + System.lineSeparator() + attachmentPreview;
    }

    private String newId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public record PersistenceContext(
            String conversationId,
            String userMessageId,
            String assistantMessageId,
            String callId,
            Long userId,
            AiModelConfigBO modelConfig,
            String requestType,
            boolean streamFlag,
            int baseMessageCount,
            long startedAtMillis
    ) {

        public int assistantSequenceNo() {
            return baseMessageCount + 2;
        }
    }
}
