package io.github.module.ai.biz;

import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.exception.BusinessException;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.module.ai.facade.AiKnowledgeBaseFacade;
import io.github.module.ai.model.request.AdminInsertOrUpdateAiKnowledgeBaseDTO;
import io.github.module.ai.model.request.AdminListAiKnowledgeBaseDTO;
import io.github.module.ai.model.response.AiKnowledgeBaseBO;
import io.github.module.ai.model.response.AiKnowledgeBaseDetailBO;
import io.github.module.ai.service.AiKnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.Collection;
import java.util.List;

/**
 * 后台 AI 知识库 Facade 接口实现类.
 */
@RequiredArgsConstructor
@DubboService(
        version = BaseConstant.Version.DUBBO_VERSION_V1,
        validation = BaseConstant.Dubbo.ENABLE_VALIDATION,
        timeout = BaseConstant.Dubbo.TIMEOUT
)
@Slf4j
public class AiKnowledgeBaseFacadeImpl implements AiKnowledgeBaseFacade {

    private final AiKnowledgeBaseService aiKnowledgeBaseService;

    @Override
    public PageResult<AiKnowledgeBaseBO> adminList(PageParam pageParam, AdminListAiKnowledgeBaseDTO dto) {
        return aiKnowledgeBaseService.adminList(pageParam, dto);
    }

    @Override
    public List<AiKnowledgeBaseBO> adminSelectOptions() {
        return aiKnowledgeBaseService.adminSelectOptions();
    }

    @Override
    public AiKnowledgeBaseDetailBO getOneById(Long id) {
        return aiKnowledgeBaseService.getOneById(id);
    }

    @Override
    public AiKnowledgeBaseDetailBO getOneById(Long id, boolean throwIfInvalidId) throws BusinessException {
        return aiKnowledgeBaseService.getOneById(id, throwIfInvalidId);
    }

    @Override
    public Long adminInsert(AdminInsertOrUpdateAiKnowledgeBaseDTO dto) {
        return aiKnowledgeBaseService.adminInsert(dto);
    }

    @Override
    public void adminUpdate(AdminInsertOrUpdateAiKnowledgeBaseDTO dto) {
        aiKnowledgeBaseService.adminUpdate(dto);
    }

    @Override
    public void adminDelete(Collection<Long> ids) {
        aiKnowledgeBaseService.adminDelete(ids);
    }

    @Override
    public void adminUpdateStatus(Long id, Integer status) {
        aiKnowledgeBaseService.adminUpdateStatus(id, status);
    }
}
