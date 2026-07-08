package io.github.module.ai.biz;

import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.exception.BusinessException;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.module.ai.facade.AiAgentFacade;
import io.github.module.ai.model.request.AdminInsertOrUpdateAiAgentDTO;
import io.github.module.ai.model.request.AdminListAiAgentDTO;
import io.github.module.ai.model.response.AiAgentBO;
import io.github.module.ai.model.response.AiAgentDetailBO;
import io.github.module.ai.service.AiAgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.Collection;
import java.util.List;

/**
 * 后台 AI Agent Facade 接口实现类.
 */
@RequiredArgsConstructor
@DubboService(
        version = BaseConstant.Version.DUBBO_VERSION_V1,
        validation = BaseConstant.Dubbo.ENABLE_VALIDATION,
        timeout = BaseConstant.Dubbo.TIMEOUT
)
@Slf4j
public class AiAgentFacadeImpl implements AiAgentFacade {

    private final AiAgentService aiAgentService;

    @Override
    public PageResult<AiAgentBO> adminList(PageParam pageParam, AdminListAiAgentDTO dto) {
        return aiAgentService.adminList(pageParam, dto);
    }

    @Override
    public List<AiAgentBO> adminSelectOptions() {
        return aiAgentService.adminSelectOptions();
    }

    @Override
    public AiAgentDetailBO getOneById(Long id) {
        return aiAgentService.getOneById(id);
    }

    @Override
    public AiAgentDetailBO getOneById(Long id, boolean throwIfInvalidId) throws BusinessException {
        return aiAgentService.getOneById(id, throwIfInvalidId);
    }

    @Override
    public Long adminInsert(AdminInsertOrUpdateAiAgentDTO dto) {
        return aiAgentService.adminInsert(dto);
    }

    @Override
    public void adminUpdate(AdminInsertOrUpdateAiAgentDTO dto) {
        aiAgentService.adminUpdate(dto);
    }

    @Override
    public void adminDelete(Collection<Long> ids) {
        aiAgentService.adminDelete(ids);
    }

    @Override
    public void adminUpdateStatus(Long id, Integer status) {
        aiAgentService.adminUpdateStatus(id, status);
    }
}
