package io.github.module.ai.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.framework.core.context.TenantContext;
import io.github.framework.core.context.TenantContextHolder;
import io.github.framework.core.context.UserContext;
import io.github.framework.core.context.UserContextHolder;
import io.github.framework.core.exception.BusinessException;
import io.github.module.ai.constant.AiModelCapabilityConstant;
import io.github.module.ai.entity.AiMessageEntity;
import io.github.module.ai.enums.AiErrorEnum;
import io.github.module.ai.mapper.AiMessageMapper;
import io.github.module.ai.model.request.AdminAiChatDTO;
import io.github.module.ai.model.request.AdminRetrieveAiKnowledgeDTO;
import io.github.module.ai.model.response.AdminAiChatBO;
import io.github.module.ai.model.response.AdminAiChatModelOptionBO;
import io.github.module.ai.model.response.AdminAiRealtimeReferenceBO;
import io.github.module.ai.model.response.AdminAiChatStreamChunkBO;
import io.github.module.ai.model.response.AiKnowledgeRetrievalHitBO;
import io.github.module.ai.model.response.AiKnowledgeRetrievalResultBO;
import io.github.module.ai.model.response.AiModelConfigBO;
import io.github.module.ai.service.realtime.AiRealtimeIntentResult;
import io.github.module.ai.service.realtime.AiRealtimeIntentService;
import io.github.module.ai.service.realtime.AiRealtimeLookupReference;
import io.github.module.ai.service.realtime.AiRealtimeLookupRequest;
import io.github.module.ai.service.realtime.AiRealtimeLookupResult;
import io.github.module.ai.service.realtime.AiRealtimeLookupService;
import io.github.starter.ai.service.XBootAiService;
import io.github.starter.ai.vo.AiChatMedia;
import io.github.starter.ai.vo.AiChatRequest;
import io.github.starter.ai.vo.AiModelConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * 后台 AI 对话编排服务.
 */
@RequiredArgsConstructor
@Service
public class AiChatService {

    public static final String EVENT_MESSAGE = "message";
    public static final String EVENT_DONE = "done";
    public static final String EVENT_ERROR = "error";
    public static final String DONE_MARKER = "[DONE]";
    private static final String REQUEST_TYPE_CHAT = "chat";
    private static final String REQUEST_TYPE_STREAM = "stream";
    private static final int RETRIEVAL_SUCCESS = 1;
    private static final int RAG_REFERENCE_CONTENT_MAX_LENGTH = 1200;
    private static final int HISTORY_MESSAGE_LIMIT = 20;
    private static final int HISTORY_CONTEXT_MAX_LENGTH = 12000;
    private static final int HISTORY_MESSAGE_CONTENT_MAX_LENGTH = 1000;
    private static final int MESSAGE_STATUS_SUCCESS = 1;
    private static final Duration MINIMUM_STREAM_TIMEOUT = Duration.ofMinutes(10);
    private static final String ROLE_USER = "user";
    private static final String ROLE_ASSISTANT = "assistant";
    private static final String ROLE_SYSTEM = "system";
    private static final String CHAT_ASSISTANT_INSTRUCTION = """
            你是企业 AI 对话助手。
            回答要求：
            1. 优先基于当前任务中明确提供的信息、知识库引用片段和历史对话回答，不要凭空补充未提供的事实。
            2. 如果用户在询问“最新版本、今天、当前、实时、官方最新、最近发布”等依赖外部实时信息的问题，而当前任务里没有给出可核验的来源、检索结果或引用片段，请明确说明当前会话没有提供可验证的实时数据，不能确认“最新”结论。
            3. 遇到上一条场景时，不要编造具体版本号、发布日期、价格、新闻结论或“截至某年某月”的说法；可以建议用户开启联网搜索、提供官方链接，或改问不依赖实时信息的问题。
            4. 如果信息充分，直接给出清晰、简洁、有依据的答案。
            """;

    private final AiModelConfigService aiModelConfigService;

    private final XBootAiService xBootAiService;

    private final AiChatPersistenceService aiChatPersistenceService;

    private final AiKnowledgeRetrievalService aiKnowledgeRetrievalService;

    private final AiMessageMapper aiMessageMapper;

    private final AiChatAttachmentService aiChatAttachmentService;

    private final AiRealtimeIntentService aiRealtimeIntentService;

    private final AiRealtimeLookupService aiRealtimeLookupService;

    /**
     * 后台对话-启用模型配置选项.
     */
    public List<AdminAiChatModelOptionBO> adminListModelOptions() {
        List<AiModelConfigBO> modelConfigs = aiModelConfigService.adminSelectOptions();
        if (CollUtil.isEmpty(modelConfigs)) {
            return Collections.emptyList();
        }
        return modelConfigs.stream()
                .filter(modelConfig -> AiModelCapabilityConstant.contains(
                        modelConfig.getSupportedCapabilities(),
                        AiModelCapabilityConstant.CHAT))
                .map(this::modelOption)
                .toList();
    }

    /**
     * 后台普通对话.
     */
    public AdminAiChatBO adminChat(AdminAiChatDTO dto) {
        String conversationId = resolveConversationId(dto.getConversationId());
        String messageId = newId();
        String callId = newId();
        RuntimeChatContext context = buildContext(dto, conversationId);
        AiChatAttachmentService.ChatAttachments attachments = aiChatAttachmentService.prepare(dto, context.modelConfig());
        AiChatPersistenceService.PersistenceContext persistenceContext = aiChatPersistenceService.start(
                dto, context.conversationId(), context.modelConfig(), attachments.all(), messageId, callId, REQUEST_TYPE_CHAT, false);

        try {
            RealtimeAnswerContext realtimeAnswer = buildRealtimeAnswer(dto);
            if (realtimeAnswer != null) {
                aiChatPersistenceService.completeSuccess(persistenceContext, realtimeAnswer.answer());
                return AdminAiChatBO.builder()
                        .conversationId(context.conversationId())
                        .messageId(messageId)
                        .modelConfigCode(context.modelConfig().getCode())
                        .providerType(context.modelConfig().getProviderType())
                        .modelName(context.modelConfig().getModelName())
                        .answer(realtimeAnswer.answer())
                        .realtimeVerified(realtimeAnswer.lookupResult().verified())
                        .realtimeLookupType(realtimeAnswer.lookupResult().lookupType())
                        .realtimeLookupTimestamp(realtimeAnswer.lookupResult().queriedAt())
                        .realtimeReferences(toRealtimeReferences(realtimeAnswer.lookupResult().references()))
                        .build();
            }
            RagContext ragContext = buildRagContext(dto, context.conversationId(), messageId);
            String prompt = buildPromptWithHistory(dto, ragContext, persistenceContext);
            AiChatRequest request = buildAiRequest(prompt, attachments);
            String answer = hasMedia(request)
                    ? xBootAiService.chat(request, context.runtimeConfig())
                    : xBootAiService.chat(prompt, context.runtimeConfig());
            aiChatPersistenceService.completeSuccess(persistenceContext, answer);
            return AdminAiChatBO.builder()
                    .conversationId(context.conversationId())
                    .messageId(messageId)
                    .modelConfigCode(context.modelConfig().getCode())
                    .providerType(context.modelConfig().getProviderType())
                    .modelName(context.modelConfig().getModelName())
                    .answer(answer)
                    .knowledgeRetrievalLogId(ragContext.knowledgeRetrievalLogId())
                    .references(ragContext.references())
                    .build();
        } catch (RuntimeException ex) {
            completeFailure(persistenceContext, rootMessage(ex), null, ex);
            throw ex;
        }
    }

    /**
     * 后台 SSE 流式对话.
     */
    public Flux<AdminAiChatStreamChunkBO> adminStream(AdminAiChatDTO dto) {
        String conversationId = resolveConversationId(dto.getConversationId());
        String messageId = newId();
        String callId = newId();
        UserContext userContext = UserContextHolder.getUserContext();
        TenantContext tenantContext = TenantContextHolder.getTenantContext();
        return Flux.defer(() -> withRequestContexts(userContext, tenantContext, () -> {
            AiChatPersistenceService.PersistenceContext persistenceContext = null;
            try {
                RuntimeChatContext context = buildContext(dto, conversationId);
                AiChatAttachmentService.ChatAttachments attachments = aiChatAttachmentService.prepare(dto, context.modelConfig());
                persistenceContext = aiChatPersistenceService.start(
                        dto, context.conversationId(), context.modelConfig(), attachments.all(), messageId, callId, REQUEST_TYPE_STREAM, true);
                AiChatPersistenceService.PersistenceContext startedContext = persistenceContext;
                RealtimeAnswerContext realtimeAnswer = buildRealtimeAnswer(dto);
                if (realtimeAnswer != null) {
                    return Flux.just(messageChunk(context, messageId, realtimeAnswer.answer()))
                            .concatWith(Mono.fromSupplier(() -> withRequestContexts(userContext, tenantContext, () -> {
                                aiChatPersistenceService.completeSuccess(startedContext, realtimeAnswer.answer());
                                return doneChunk(context, messageId, new RagContext(null, null, Collections.emptyList()), realtimeAnswer);
                            })));
                }
                RagContext ragContext = buildRagContext(dto, context.conversationId(), messageId);
                String prompt = buildPromptWithHistory(dto, ragContext, startedContext);
                AiChatRequest request = buildAiRequest(prompt, attachments);
                AiModelConfig streamRuntimeConfig = withMinimumStreamTimeout(context.runtimeConfig());
                StringBuilder answer = new StringBuilder();
                Flux<String> responseStream = hasMedia(request)
                        ? xBootAiService.stream(request, streamRuntimeConfig)
                        : xBootAiService.stream(prompt, streamRuntimeConfig);
                return responseStream
                        .map(content -> {
                            return withRequestContexts(userContext, tenantContext, () -> {
                                answer.append(StrUtil.nullToEmpty(content));
                                return messageChunk(context, messageId, content);
                            });
                        })
                        .concatWith(Mono.fromSupplier(() -> {
                            return withRequestContexts(userContext, tenantContext, () -> {
                                aiChatPersistenceService.completeSuccess(startedContext, answer.toString());
                                return doneChunk(context, messageId, ragContext, null);
                            });
                        }))
                        .onErrorResume(ex -> {
                            return withRequestContexts(userContext, tenantContext, () -> {
                                completeFailure(startedContext, rootMessage(ex), answer.toString(), ex);
                                return Flux.just(errorChunk(conversationId, messageId, ex));
                            });
                        });
            } catch (RuntimeException ex) {
                if (persistenceContext != null) {
                    completeFailure(persistenceContext, rootMessage(ex), null, ex);
                }
                return Flux.just(errorChunk(conversationId, messageId, ex));
            }
        })).onErrorResume(ex -> Flux.just(errorChunk(conversationId, messageId, ex)));
    }

    private AiModelConfig withMinimumStreamTimeout(AiModelConfig runtimeConfig) {
        if (runtimeConfig == null) {
            return null;
        }
        Duration timeout = runtimeConfig.getTimeout();
        if (timeout == null || timeout.compareTo(MINIMUM_STREAM_TIMEOUT) < 0) {
            runtimeConfig.setTimeout(MINIMUM_STREAM_TIMEOUT);
        }
        return runtimeConfig;
    }

    private <T> T withRequestContexts(UserContext userContext, TenantContext tenantContext, Supplier<T> supplier) {
        restoreRequestContexts(userContext, tenantContext);
        try {
            return supplier.get();
        } finally {
            clearRequestContexts();
        }
    }

    private void restoreRequestContexts(UserContext userContext, TenantContext tenantContext) {
        clearRequestContexts();
        if (userContext != null) {
            UserContextHolder.setUserContext(userContext);
        }
        if (tenantContext != null) {
            TenantContextHolder.setTenantContext(tenantContext);
        }
    }

    private void clearRequestContexts() {
        UserContextHolder.clear();
        TenantContextHolder.clear();
    }

    private void completeFailure(AiChatPersistenceService.PersistenceContext persistenceContext,
                                 String errorMessage,
                                 String partialAnswer,
                                 Throwable ex) {
        try {
            aiChatPersistenceService.completeFailure(persistenceContext, errorMessage, partialAnswer, ex);
        } catch (RuntimeException persistenceEx) {
            ex.addSuppressed(persistenceEx);
        }
    }

    private RuntimeChatContext buildContext(AdminAiChatDTO dto, String conversationId) {
        AiModelConfigBO modelConfig = resolveModelConfig(dto.getModelConfigCode());
        AiModelConfig runtimeConfig = aiModelConfigService.toRuntimeConfig(modelConfig);
        return new RuntimeChatContext(conversationId, modelConfig, runtimeConfig);
    }

    private AdminAiChatModelOptionBO modelOption(AiModelConfigBO modelConfig) {
        return AdminAiChatModelOptionBO.builder()
                .id(modelConfig.getId())
                .code(modelConfig.getCode())
                .name(modelConfig.getName())
                .providerType(modelConfig.getProviderType())
                .modelName(modelConfig.getModelName())
                .supportedModalities(modelConfig.getSupportedModalities())
                .supportedCapabilities(modelConfig.getSupportedCapabilities())
                .defaultFlag(modelConfig.getDefaultFlag())
                .description(modelConfig.getDescription())
                .build();
    }

    private AiModelConfigBO resolveModelConfig(String modelConfigCode) {
        String cleanCode = clean(modelConfigCode);
        if (StrUtil.isNotBlank(cleanCode)) {
            AiModelConfigBO modelConfig = aiModelConfigService.getEnabledConfigByCode(cleanCode, true);
            AiErrorEnum.NO_ENABLED_MODEL_CONFIG.assertNotNull(modelConfig);
            return ensureChatCapable(modelConfig);
        }

        AiModelConfigBO defaultConfig = aiModelConfigService.getDefaultEnabledConfig();
        AiErrorEnum.NO_ENABLED_MODEL_CONFIG.assertNotNull(defaultConfig);
        return ensureChatCapable(defaultConfig);
    }

    private AiModelConfigBO ensureChatCapable(AiModelConfigBO modelConfig) {
        if (!AiModelCapabilityConstant.contains(
                modelConfig == null ? null : modelConfig.getSupportedCapabilities(),
                AiModelCapabilityConstant.CHAT)) {
            throw new BusinessException(AiErrorEnum.UNSUPPORTED_CHAT_MODEL_CAPABILITY);
        }
        return modelConfig;
    }

    private String buildPromptWithHistory(AdminAiChatDTO dto,
                                          RagContext ragContext,
                                          AiChatPersistenceService.PersistenceContext persistenceContext) {
        String historyContext = buildHistoryContext(
                listHistoryMessages(persistenceContext.conversationId(), persistenceContext.userMessageId()),
                persistenceContext.userMessageId());
        StringBuilder prompt = new StringBuilder()
                .append(CHAT_ASSISTANT_INSTRUCTION)
                .append(System.lineSeparator())
                .append(System.lineSeparator())
                .append("【当前任务】")
                .append(System.lineSeparator());

        if (StrUtil.isNotBlank(historyContext)) {
            prompt.append("【历史对话】")
                    .append(System.lineSeparator())
                    .append(historyContext)
                    .append(System.lineSeparator());
        }

        if (hasRagContext(dto)) {
            return prompt.append(ragContext.prompt()).toString();
        }
        return prompt.append("【当前用户问题】")
                .append(System.lineSeparator())
                .append(currentUserText(dto))
                .toString();
    }

    private AiChatRequest buildAiRequest(String prompt, AiChatAttachmentService.ChatAttachments attachments) {
        List<AiChatMedia> media = aiChatAttachmentService.toImageMedia(attachments);
        return new AiChatRequest()
                .setText(prompt)
                .setMedia(media);
    }

    private boolean hasMedia(AiChatRequest request) {
        return request != null && CollUtil.isNotEmpty(request.getMedia());
    }

    private String currentUserText(AdminAiChatDTO dto) {
        String content = dto == null ? null : dto.getContent();
        if (StrUtil.isNotBlank(content)) {
            return content;
        }
        return "请根据我上传的图片内容进行分析并回答。";
    }

    private List<AiMessageEntity> listHistoryMessages(String conversationId, String currentUserMessageId) {
        List<AiMessageEntity> messages = aiMessageMapper.selectList(new QueryWrapper<AiMessageEntity>()
                .lambda()
                .eq(AiMessageEntity::getConversationId, conversationId)
                .ne(StrUtil.isNotBlank(currentUserMessageId), AiMessageEntity::getMessageId, currentUserMessageId)
                .eq(AiMessageEntity::getStatus, MESSAGE_STATUS_SUCCESS)
                .in(AiMessageEntity::getRole, ROLE_USER, ROLE_ASSISTANT, ROLE_SYSTEM)
                .orderByDesc(AiMessageEntity::getSequenceNo)
                .last("LIMIT " + HISTORY_MESSAGE_LIMIT));
        if (CollUtil.isEmpty(messages)) {
            return Collections.emptyList();
        }

        List<AiMessageEntity> orderedMessages = new ArrayList<>(messages);
        Collections.reverse(orderedMessages);
        return orderedMessages;
    }

    private String buildHistoryContext(List<AiMessageEntity> messages, String currentUserMessageId) {
        if (CollUtil.isEmpty(messages)) {
            return StrUtil.EMPTY;
        }

        StringBuilder history = new StringBuilder();
        for (AiMessageEntity message : messages) {
            if (message == null || StrUtil.equals(message.getMessageId(), currentUserMessageId)
                    || StrUtil.isBlank(message.getContent())) {
                continue;
            }

            String line = new StringBuilder()
                    .append(messageRoleLabel(message.getRole()))
                    .append("：")
                    .append(truncate(message.getContent(), HISTORY_MESSAGE_CONTENT_MAX_LENGTH))
                    .append(System.lineSeparator())
                    .toString();
            if (history.length() + line.length() > HISTORY_CONTEXT_MAX_LENGTH) {
                break;
            }
            history.append(line);
        }
        return history.toString();
    }

    private String messageRoleLabel(String role) {
        return switch (StrUtil.nullToEmpty(role)) {
            case ROLE_USER -> "用户";
            case ROLE_ASSISTANT -> "助手";
            case ROLE_SYSTEM -> "系统";
            default -> "消息";
        };
    }

    private RagContext buildRagContext(AdminAiChatDTO dto, String conversationId, String messageId) {
        if (!hasRagContext(dto)) {
            return new RagContext(currentUserText(dto), null, Collections.emptyList());
        }

        String question = currentUserText(dto);
        AiKnowledgeRetrievalResultBO retrievalResult = aiKnowledgeRetrievalService.adminRetrieve(AdminRetrieveAiKnowledgeDTO.builder()
                .knowledgeBaseIds(dto.getKnowledgeBaseIds())
                .query(question)
                .conversationId(conversationId)
                .messageId(messageId)
                .logFlag(true)
                .build());
        if (!Integer.valueOf(RETRIEVAL_SUCCESS).equals(retrievalResult.getStatus())) {
            throw new BusinessException(400, StrUtil.blankToDefault(retrievalResult.getErrorMessage(), "知识库检索失败"));
        }

        List<AiKnowledgeRetrievalHitBO> references = CollUtil.isEmpty(retrievalResult.getHits())
                ? Collections.emptyList()
                : retrievalResult.getHits();
        return new RagContext(buildRagPrompt(question, references), retrievalResult.getLogId(), references);
    }

    private String buildRagPrompt(String question, List<AiKnowledgeRetrievalHitBO> references) {
        StringBuilder prompt = new StringBuilder()
                .append("你是企业知识库问答助手。请优先基于【知识库引用片段】回答用户问题；")
                .append("如果引用片段不足以回答，请明确说明依据不足，不要编造。")
                .append(System.lineSeparator())
                .append(System.lineSeparator())
                .append("【知识库引用片段】")
                .append(System.lineSeparator());
        if (CollUtil.isEmpty(references)) {
            prompt.append("未检索到相关片段。").append(System.lineSeparator());
        } else {
            for (int i = 0; i < references.size(); i++) {
                AiKnowledgeRetrievalHitBO reference = references.get(i);
                prompt.append("[")
                        .append(i + 1)
                        .append("] 知识库：")
                        .append(StrUtil.blankToDefault(reference.getKnowledgeBaseName(), "-"))
                        .append("；文档：")
                        .append(StrUtil.blankToDefault(reference.getDocumentName(), "-"))
                        .append("；切片：")
                        .append(StrUtil.toString(reference.getChunkNo()))
                        .append("；相似度：")
                        .append(StrUtil.toString(reference.getSimilarityScore()))
                        .append(System.lineSeparator())
                        .append("内容：")
                        .append(truncate(reference.getContent(), RAG_REFERENCE_CONTENT_MAX_LENGTH))
                        .append(System.lineSeparator());
            }
        }
        return prompt.append(System.lineSeparator())
                .append("【用户问题】")
                .append(System.lineSeparator())
                .append(question)
                .toString();
    }

    private boolean hasRagContext(AdminAiChatDTO dto) {
        return CollUtil.isNotEmpty(dto.getKnowledgeBaseIds());
    }

    private String truncate(String content, int maxLength) {
        String cleanContent = StrUtil.nullToEmpty(content);
        if (cleanContent.length() <= maxLength) {
            return cleanContent;
        }
        return cleanContent.substring(0, maxLength) + "...";
    }

    private AdminAiChatStreamChunkBO messageChunk(RuntimeChatContext context, String messageId, String content) {
        return chunk(context, messageId, EVENT_MESSAGE, content, false);
    }

    private AdminAiChatStreamChunkBO doneChunk(RuntimeChatContext context,
                                               String messageId,
                                               RagContext ragContext,
                                               RealtimeAnswerContext realtimeAnswer) {
        AdminAiChatStreamChunkBO chunk = chunk(context, messageId, EVENT_DONE, DONE_MARKER, true)
                .setKnowledgeRetrievalLogId(ragContext.knowledgeRetrievalLogId())
                .setReferences(ragContext.references());
        if (realtimeAnswer != null) {
            chunk.setRealtimeVerified(realtimeAnswer.lookupResult().verified())
                    .setRealtimeLookupType(realtimeAnswer.lookupResult().lookupType())
                    .setRealtimeLookupTimestamp(realtimeAnswer.lookupResult().queriedAt())
                    .setRealtimeReferences(toRealtimeReferences(realtimeAnswer.lookupResult().references()));
        }
        return chunk;
    }

    private AdminAiChatStreamChunkBO errorChunk(String conversationId, String messageId, Throwable ex) {
        return AdminAiChatStreamChunkBO.builder()
                .event(EVENT_ERROR)
                .messageId(messageId)
                .conversationId(conversationId)
                .content(rootMessage(ex))
                .finish(true)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    private AdminAiChatStreamChunkBO chunk(RuntimeChatContext context,
                                          String messageId,
                                          String event,
                                          String content,
                                          boolean finish) {
        return AdminAiChatStreamChunkBO.builder()
                .event(event)
                .messageId(messageId)
                .conversationId(context.conversationId())
                .modelConfigCode(context.modelConfig().getCode())
                .providerType(context.modelConfig().getProviderType())
                .modelName(context.modelConfig().getModelName())
                .content(content)
                .finish(finish)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    private String resolveConversationId(String conversationId) {
        String cleanConversationId = clean(conversationId);
        return StrUtil.blankToDefault(cleanConversationId, newId());
    }

    private RealtimeAnswerContext buildRealtimeAnswer(AdminAiChatDTO dto) {
        String question = currentUserText(dto);
        AiRealtimeIntentResult intentResult = aiRealtimeIntentService.analyze(question);
        if (!intentResult.realtimeLookupRequired()) {
            return null;
        }
        AiRealtimeLookupResult lookupResult = aiRealtimeLookupService.lookupLatestVersion(AiRealtimeLookupRequest.builder()
                .queryText(question)
                .lookupType(intentResult.lookupType())
                .subject(intentResult.subject())
                .build());
        return new RealtimeAnswerContext(buildRealtimeAnswerText(lookupResult), lookupResult);
    }

    private String buildRealtimeAnswerText(AiRealtimeLookupResult lookupResult) {
        if (lookupResult.success()) {
            StringBuilder answer = new StringBuilder()
                    .append("已联网核验：")
                    .append(StrUtil.blankToDefault(lookupResult.subjectName(), "该依赖"))
                    .append(" 最新版本是 ")
                    .append(StrUtil.blankToDefault(lookupResult.resolvedVersion(), "未知"))
                    .append("。");
            if (StrUtil.isNotBlank(lookupResult.releaseType())) {
                answer.append(System.lineSeparator())
                        .append("发布类型：")
                        .append(lookupResult.releaseType());
            }
            answer.append(System.lineSeparator())
                    .append("查询时间戳：")
                    .append(lookupResult.queriedAt());
            if (CollUtil.isNotEmpty(lookupResult.references())) {
                answer.append(System.lineSeparator()).append("来源：");
                for (int i = 0; i < lookupResult.references().size(); i++) {
                    AiRealtimeLookupReference reference = lookupResult.references().get(i);
                    answer.append(System.lineSeparator())
                            .append(i + 1)
                            .append(". ")
                            .append(StrUtil.blankToDefault(reference.sourceName(), "来源"))
                            .append(" - ")
                            .append(StrUtil.blankToDefault(reference.url(), "-"));
                }
            }
            return answer.toString();
        }
        return new StringBuilder()
                .append("我尝试联网核验 ")
                .append(StrUtil.blankToDefault(lookupResult.subjectName(), "该依赖"))
                .append(" 的最新版本，但本次查询未成功，因此当前不能可靠确认“最新版本”。")
                .append(System.lineSeparator())
                .append("失败原因：")
                .append(StrUtil.blankToDefault(lookupResult.errorMessage(), "实时版本核验失败"))
                .toString();
    }

    private List<AdminAiRealtimeReferenceBO> toRealtimeReferences(List<AiRealtimeLookupReference> references) {
        if (CollUtil.isEmpty(references)) {
            return Collections.emptyList();
        }
        return references.stream()
                .map(reference -> AdminAiRealtimeReferenceBO.builder()
                        .sourceName(reference.sourceName())
                        .title(reference.title())
                        .url(reference.url())
                        .version(reference.version())
                        .publishedAt(reference.publishedAt())
                        .build())
                .toList();
    }

    private String newId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String rootMessage(Throwable e) {
        Throwable root = e;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return StrUtil.blankToDefault(root.getMessage(), "AI对话调用失败");
    }

    private String clean(String value) {
        return CharSequenceUtil.cleanBlank(value);
    }

    private record RuntimeChatContext(
            String conversationId,
            AiModelConfigBO modelConfig,
            AiModelConfig runtimeConfig
    ) {
    }

    private record RagContext(
            String prompt,
            Long knowledgeRetrievalLogId,
            List<AiKnowledgeRetrievalHitBO> references
    ) {
    }

    private record RealtimeAnswerContext(
            String answer,
            AiRealtimeLookupResult lookupResult
    ) {
    }
}
