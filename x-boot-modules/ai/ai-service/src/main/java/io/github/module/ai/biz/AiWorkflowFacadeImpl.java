package io.github.module.ai.biz;

import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.exception.BusinessException;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.module.ai.facade.AiWorkflowFacade;
import io.github.module.ai.model.request.AdminExecuteAiWorkflowDTO;
import io.github.module.ai.model.request.AdminInsertOrUpdateAiWorkflowDTO;
import io.github.module.ai.model.request.AdminInsertOrUpdateAiWorkflowNodeDTO;
import io.github.module.ai.model.request.AdminListAiWorkflowExecutionDTO;
import io.github.module.ai.model.request.AdminListAiWorkflowDTO;
import io.github.module.ai.model.request.AdminListAiWorkflowNodeDTO;
import io.github.module.ai.model.response.AdminAiWorkflowExecutionBO;
import io.github.module.ai.model.response.AdminAiWorkflowExecutionDetailBO;
import io.github.module.ai.model.response.AdminAiWorkflowExecutionResultBO;
import io.github.module.ai.model.response.AiWorkflowBO;
import io.github.module.ai.model.response.AiWorkflowDetailBO;
import io.github.module.ai.model.response.AiWorkflowNodeBO;
import io.github.module.ai.model.response.AiWorkflowNodeDetailBO;
import io.github.module.ai.service.AiWorkflowExecutionService;
import io.github.module.ai.service.AiWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.Collection;
import java.util.List;

/**
 * 后台 AI 工作流 Facade 接口实现类.
 */
@RequiredArgsConstructor
@DubboService(
        version = BaseConstant.Version.DUBBO_VERSION_V1,
        validation = BaseConstant.Dubbo.ENABLE_VALIDATION,
        timeout = BaseConstant.Dubbo.TIMEOUT
)
@Slf4j
public class AiWorkflowFacadeImpl implements AiWorkflowFacade {

    private final AiWorkflowService aiWorkflowService;

    private final AiWorkflowExecutionService aiWorkflowExecutionService;

    @Override
    public PageResult<AiWorkflowBO> adminList(PageParam pageParam, AdminListAiWorkflowDTO dto) {
        return aiWorkflowService.adminList(pageParam, dto);
    }

    @Override
    public List<AiWorkflowBO> adminSelectOptions() {
        return aiWorkflowService.adminSelectOptions();
    }

    @Override
    public AiWorkflowDetailBO getOneById(Long id) {
        return aiWorkflowService.getOneById(id);
    }

    @Override
    public AiWorkflowDetailBO getOneById(Long id, boolean throwIfInvalidId) throws BusinessException {
        return aiWorkflowService.getOneById(id, throwIfInvalidId);
    }

    @Override
    public Long adminInsert(AdminInsertOrUpdateAiWorkflowDTO dto) {
        return aiWorkflowService.adminInsert(dto);
    }

    @Override
    public void adminUpdate(AdminInsertOrUpdateAiWorkflowDTO dto) {
        aiWorkflowService.adminUpdate(dto);
    }

    @Override
    public void adminDelete(Collection<Long> ids) {
        aiWorkflowService.adminDelete(ids);
    }

    @Override
    public void adminUpdateStatus(Long id, Integer status) {
        aiWorkflowService.adminUpdateStatus(id, status);
    }

    @Override
    public List<AiWorkflowNodeBO> adminListNodes(AdminListAiWorkflowNodeDTO dto) {
        return aiWorkflowService.adminListNodes(dto);
    }

    @Override
    public AiWorkflowNodeDetailBO getNodeById(Long id) {
        return aiWorkflowService.getNodeById(id);
    }

    @Override
    public AiWorkflowNodeDetailBO getNodeById(Long id, boolean throwIfInvalidId) throws BusinessException {
        return aiWorkflowService.getNodeById(id, throwIfInvalidId);
    }

    @Override
    public Long adminInsertNode(AdminInsertOrUpdateAiWorkflowNodeDTO dto) {
        return aiWorkflowService.adminInsertNode(dto);
    }

    @Override
    public void adminUpdateNode(AdminInsertOrUpdateAiWorkflowNodeDTO dto) {
        aiWorkflowService.adminUpdateNode(dto);
    }

    @Override
    public void adminDeleteNodes(Collection<Long> ids) {
        aiWorkflowService.adminDeleteNodes(ids);
    }

    @Override
    public AdminAiWorkflowExecutionResultBO adminExecute(Long workflowDefinitionId, AdminExecuteAiWorkflowDTO dto) {
        return aiWorkflowExecutionService.adminExecute(workflowDefinitionId, dto);
    }

    @Override
    public PageResult<AdminAiWorkflowExecutionBO> adminListExecutions(
            PageParam pageParam,
            AdminListAiWorkflowExecutionDTO dto
    ) {
        return aiWorkflowExecutionService.adminListExecutions(pageParam, dto);
    }

    @Override
    public AdminAiWorkflowExecutionDetailBO getExecutionById(Long id) {
        return aiWorkflowExecutionService.getExecutionById(id);
    }

    @Override
    public AdminAiWorkflowExecutionDetailBO getExecutionById(Long id, boolean throwIfInvalidId)
            throws BusinessException {
        return aiWorkflowExecutionService.getExecutionById(id, throwIfInvalidId);
    }
}
