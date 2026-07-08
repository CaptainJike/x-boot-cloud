package io.github.module.ai.biz;

import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
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
import io.github.module.ai.service.AiChatService;
import io.github.module.ai.service.AiConversationQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

/**
 * 后台 AI 对话 Facade 接口实现类.
 */
@RequiredArgsConstructor
@DubboService(
        version = BaseConstant.Version.DUBBO_VERSION_V1,
        validation = BaseConstant.Dubbo.ENABLE_VALIDATION,
        timeout = AiChatFacadeImpl.CHAT_RPC_TIMEOUT
)
@Slf4j
public class AiChatFacadeImpl implements AiChatFacade {

    static final int CHAT_RPC_TIMEOUT = 60000;

    private final AiChatService aiChatService;

    private final AiConversationQueryService aiConversationQueryService;

    @Override
    public List<AdminAiChatModelOptionBO> adminListModelOptions() {
        return aiChatService.adminListModelOptions();
    }

    @Override
    public AdminAiChatBO adminChat(AdminAiChatDTO dto) {
        return aiChatService.adminChat(dto);
    }

    @Override
    public List<AdminAiChatStreamChunkBO> adminStream(AdminAiChatDTO dto) {
        List<AdminAiChatStreamChunkBO> chunks = aiChatService.adminStream(dto).collectList().block();
        return chunks == null ? List.of() : chunks;
    }

    @Override
    public PageResult<AdminAiConversationBO> adminListConversations(PageParam pageParam,
                                                                    AdminListAiConversationDTO dto) {
        return aiConversationQueryService.adminListConversations(pageParam, dto);
    }

    @Override
    public AdminAiConversationDetailBO adminGetConversation(String conversationId) {
        return aiConversationQueryService.adminGetConversation(conversationId);
    }

    @Override
    public PageResult<AdminAiMessageBO> adminListMessages(String conversationId,
                                                          PageParam pageParam,
                                                          AdminListAiMessageDTO dto) {
        return aiConversationQueryService.adminListMessages(conversationId, pageParam, dto);
    }
}
