package io.github.module.ai.facade;

import io.github.framework.core.exception.BusinessException;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.module.ai.model.request.AdminListAiKnowledgeRetrievalLogDTO;
import io.github.module.ai.model.request.AdminRetrieveAiKnowledgeDTO;
import io.github.module.ai.model.response.AiKnowledgeRetrievalLogBO;
import io.github.module.ai.model.response.AiKnowledgeRetrievalResultBO;

/**
 * 后台 AI 知识库检索 Facade 接口.
 */
public interface AiKnowledgeRetrievalFacade {

    /**
     * 后台管理-执行知识库基础检索.
     */
    AiKnowledgeRetrievalResultBO adminRetrieve(AdminRetrieveAiKnowledgeDTO dto);

    /**
     * 后台管理-分页列表知识库检索日志.
     */
    PageResult<AiKnowledgeRetrievalLogBO> adminListLogs(PageParam pageParam,
                                                        AdminListAiKnowledgeRetrievalLogDTO dto);

    /**
     * 根据 ID 取知识库检索日志详情.
     */
    AiKnowledgeRetrievalLogBO getLogById(Long id, boolean throwIfInvalidId) throws BusinessException;
}
