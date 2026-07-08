package io.github.module.adminapi.web.ai;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.framework.core.constant.ApiPrefixConstant;
import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.framework.web.model.response.ApiResult;
import io.github.module.adminapi.service.AdminAiChatStreamClient;
import io.github.module.adminapi.util.AdminStpUtil;
import io.github.module.ai.facade.AiChatFacade;
import io.github.module.ai.model.request.AdminAiChatDTO;
import io.github.module.ai.model.request.AdminListAiConversationDTO;
import io.github.module.ai.model.request.AdminListAiMessageDTO;
import io.github.module.ai.model.response.AdminAiChatBO;
import io.github.module.ai.model.response.AdminAiChatModelOptionBO;
import io.github.module.ai.model.response.AdminAiChatStreamChunkBO;
import io.github.module.ai.model.response.AdminAiConversationBO;
import io.github.module.ai.model.response.AdminAiConversationDetailBO;
import io.github.module.ai.model.response.AdminAiMessageBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@SaCheckLogin(type = AdminStpUtil.TYPE)
@Tag(name = "后台管理-AI对话接口")
@RequestMapping(ApiPrefixConstant.API_PREFIX_ADMIN + ApiPrefixConstant.VERSION)
@RequiredArgsConstructor
@RestController
@Slf4j
public class AdminAiChatController {

    private static final String PERMISSION_PREFIX = "AiChat:";

    private final AdminAiChatStreamClient aiChatStreamClient;

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private AiChatFacade aiChatFacade;

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.RETRIEVE, orRole = "SuperAdmin")
    @Operation(summary = "对话可选模型配置")
    @GetMapping(value = "/ai/chats/model-config-options")
    public ApiResult<List<AdminAiChatModelOptionBO>> listModelOptions() {
        return ApiResult.data(aiChatFacade.adminListModelOptions());
    }

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.RETRIEVE, orRole = "SuperAdmin")
    @Operation(summary = "会话分页列表")
    @GetMapping(value = "/ai/conversations")
    public ApiResult<PageResult<AdminAiConversationBO>> listConversations(PageParam pageParam,
                                                                          AdminListAiConversationDTO dto) {
        return ApiResult.data(aiChatFacade.adminListConversations(pageParam, dto));
    }

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.RETRIEVE, orRole = "SuperAdmin")
    @Operation(summary = "会话详情")
    @GetMapping(value = "/ai/conversations/{conversationId}")
    public ApiResult<AdminAiConversationDetailBO> getConversation(@PathVariable("conversationId") String conversationId) {
        return ApiResult.data(aiChatFacade.adminGetConversation(conversationId));
    }

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.RETRIEVE, orRole = "SuperAdmin")
    @Operation(summary = "消息分页列表")
    @GetMapping(value = "/ai/conversations/{conversationId}/messages")
    public ApiResult<PageResult<AdminAiMessageBO>> listMessages(@PathVariable("conversationId") String conversationId,
                                                                PageParam pageParam,
                                                                AdminListAiMessageDTO dto) {
        return ApiResult.data(aiChatFacade.adminListMessages(conversationId, pageParam, dto));
    }

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + "chat", orRole = "SuperAdmin")
    @Operation(summary = "普通对话")
    @PostMapping(value = "/ai/chats")
    public ApiResult<AdminAiChatBO> chat(@RequestBody @Valid AdminAiChatDTO dto) {
        return ApiResult.data(aiChatFacade.adminChat(dto));
    }

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + "stream", orRole = "SuperAdmin")
    @Operation(summary = "流式对话")
    @PostMapping(value = "/ai/chats/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<AdminAiChatStreamChunkBO>> stream(@RequestBody @Valid AdminAiChatDTO dto) {
        return aiChatStreamClient.stream(dto)
                .map(this::toServerSentEvent);
    }

    private ServerSentEvent<AdminAiChatStreamChunkBO> toServerSentEvent(AdminAiChatStreamChunkBO chunk) {
        ServerSentEvent.Builder<AdminAiChatStreamChunkBO> builder = ServerSentEvent.builder(chunk);
        if (chunk == null) {
            return builder.build();
        }
        if (chunk.getMessageId() != null) {
            builder.id(chunk.getMessageId());
        }
        if (chunk.getEvent() != null) {
            builder.event(chunk.getEvent());
        }
        return builder.build();
    }
}
