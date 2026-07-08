package io.github.module.ai.facade;

import io.github.framework.core.exception.BusinessException;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
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

import java.util.Collection;
import java.util.List;

/**
 * 后台 AI 工作流 Facade 接口.
 */
public interface AiWorkflowFacade {

    /**
     * 后台管理-工作流分页列表.
     */
    PageResult<AiWorkflowBO> adminList(PageParam pageParam, AdminListAiWorkflowDTO dto);

    /**
     * 后台管理-启用工作流下拉框.
     */
    List<AiWorkflowBO> adminSelectOptions();

    /**
     * 根据 ID 取工作流详情.
     */
    AiWorkflowDetailBO getOneById(Long id);

    /**
     * 根据 ID 取工作流详情.
     */
    AiWorkflowDetailBO getOneById(Long id, boolean throwIfInvalidId) throws BusinessException;

    /**
     * 后台管理-新增工作流.
     *
     * @return 主键ID
     */
    Long adminInsert(AdminInsertOrUpdateAiWorkflowDTO dto);

    /**
     * 后台管理-编辑工作流.
     */
    void adminUpdate(AdminInsertOrUpdateAiWorkflowDTO dto);

    /**
     * 后台管理-删除工作流.
     */
    void adminDelete(Collection<Long> ids);

    /**
     * 后台管理-更新工作流启停状态.
     */
    void adminUpdateStatus(Long id, Integer status);

    /**
     * 后台管理-工作流节点列表.
     */
    List<AiWorkflowNodeBO> adminListNodes(AdminListAiWorkflowNodeDTO dto);

    /**
     * 根据 ID 取工作流节点详情.
     */
    AiWorkflowNodeDetailBO getNodeById(Long id);

    /**
     * 根据 ID 取工作流节点详情.
     */
    AiWorkflowNodeDetailBO getNodeById(Long id, boolean throwIfInvalidId) throws BusinessException;

    /**
     * 后台管理-新增工作流节点.
     *
     * @return 主键ID
     */
    Long adminInsertNode(AdminInsertOrUpdateAiWorkflowNodeDTO dto);

    /**
     * 后台管理-编辑工作流节点.
     */
    void adminUpdateNode(AdminInsertOrUpdateAiWorkflowNodeDTO dto);

    /**
     * 后台管理-删除工作流节点.
     */
    void adminDeleteNodes(Collection<Long> ids);

    /**
     * 后台管理-执行工作流.
     */
    AdminAiWorkflowExecutionResultBO adminExecute(Long workflowDefinitionId, AdminExecuteAiWorkflowDTO dto);

    /**
     * 后台管理-工作流执行记录分页列表.
     */
    PageResult<AdminAiWorkflowExecutionBO> adminListExecutions(PageParam pageParam, AdminListAiWorkflowExecutionDTO dto);

    /**
     * 根据 ID 取工作流执行记录详情.
     */
    AdminAiWorkflowExecutionDetailBO getExecutionById(Long id);

    /**
     * 根据 ID 取工作流执行记录详情.
     */
    AdminAiWorkflowExecutionDetailBO getExecutionById(Long id, boolean throwIfInvalidId) throws BusinessException;
}
