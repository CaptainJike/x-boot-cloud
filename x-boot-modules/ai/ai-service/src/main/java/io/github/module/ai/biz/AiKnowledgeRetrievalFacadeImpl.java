package io.github.module.ai.biz;

import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.exception.BusinessException;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.module.ai.facade.AiKnowledgeRetrievalFacade;
import io.github.module.ai.model.request.AdminListAiKnowledgeRetrievalLogDTO;
import io.github.module.ai.model.request.AdminRetrieveAiKnowledgeDTO;
import io.github.module.ai.model.response.AiKnowledgeRetrievalLogBO;
import io.github.module.ai.model.response.AiKnowledgeRetrievalResultBO;
import io.github.module.ai.service.AiKnowledgeRetrievalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 后台 AI 知识库检索 Facade 实现.
 */
@DubboService(
        version = BaseConstant.Version.DUBBO_VERSION_V1,
        validation = BaseConstant.Dubbo.ENABLE_VALIDATION,
        timeout = BaseConstant.Dubbo.TIMEOUT
)
@RequiredArgsConstructor
@Slf4j
public class AiKnowledgeRetrievalFacadeImpl implements AiKnowledgeRetrievalFacade {

    private final AiKnowledgeRetrievalService aiKnowledgeRetrievalService;

    @Override
    public AiKnowledgeRetrievalResultBO adminRetrieve(AdminRetrieveAiKnowledgeDTO dto) {
        return aiKnowledgeRetrievalService.adminRetrieve(dto);
    }

    @Override
    public PageResult<AiKnowledgeRetrievalLogBO> adminListLogs(PageParam pageParam,
                                                               AdminListAiKnowledgeRetrievalLogDTO dto) {
        return aiKnowledgeRetrievalService.adminListLogs(pageParam, dto);
    }

    @Override
    public AiKnowledgeRetrievalLogBO getLogById(Long id, boolean throwIfInvalidId) throws BusinessException {
        return aiKnowledgeRetrievalService.getLogById(id, throwIfInvalidId);
    }
}
