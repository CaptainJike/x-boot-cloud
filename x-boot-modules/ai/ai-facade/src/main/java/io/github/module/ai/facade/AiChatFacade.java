package io.github.module.ai.facade;

import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.module.ai.model.request.AdminAiChatDTO;
import io.github.module.ai.model.request.AdminListAiConversationDTO;
import io.github.module.ai.model.request.AdminListAiMessageDTO;
import io.github.module.ai.model.response.AdminAiChatBO;
import io.github.module.ai.model.response.AdminAiChatModelOptionBO;
import io.github.module.ai.model.response.AdminAiChatStreamChunkBO;
import io.github.module.ai.model.response.AdminAiConversationBO;
import io.github.module.ai.model.response.AdminAiConversationDetailBO;
import io.github.module.ai.model.response.AdminAiMessageBO;

import java.util.List;

/**
 * 后台 AI 对话 Facade 接口.
 */
public interface AiChatFacade {

    /**
     * 后台对话-启用模型配置选项.
     */
    List<AdminAiChatModelOptionBO> adminListModelOptions();

    /**
     * 后台普通对话.
     */
    AdminAiChatBO adminChat(AdminAiChatDTO dto);

    /**
     * 后台对话流式片段.
     */
    List<AdminAiChatStreamChunkBO> adminStream(AdminAiChatDTO dto);

    /**
     * 后台管理-分页列表 AI 会话.
     */
    PageResult<AdminAiConversationBO> adminListConversations(PageParam pageParam, AdminListAiConversationDTO dto);

    /**
     * 后台管理-会话详情.
     */
    AdminAiConversationDetailBO adminGetConversation(String conversationId);

    /**
     * 后台管理-分页列表 AI 消息.
     */
    PageResult<AdminAiMessageBO> adminListMessages(String conversationId,
                                                   PageParam pageParam,
                                                   AdminListAiMessageDTO dto);
}
