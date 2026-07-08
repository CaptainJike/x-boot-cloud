package io.github.module.ai.facade;

import io.github.framework.core.exception.BusinessException;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.module.ai.model.request.AdminInsertOrUpdateAiAgentDTO;
import io.github.module.ai.model.request.AdminListAiAgentDTO;
import io.github.module.ai.model.response.AiAgentBO;
import io.github.module.ai.model.response.AiAgentDetailBO;

import java.util.Collection;
import java.util.List;

/**
 * 后台 AI Agent Facade 接口.
 */
public interface AiAgentFacade {

    /**
     * 后台管理-Agent 分页列表.
     */
    PageResult<AiAgentBO> adminList(PageParam pageParam, AdminListAiAgentDTO dto);

    /**
     * 后台管理-启用 Agent 下拉框.
     */
    List<AiAgentBO> adminSelectOptions();

    /**
     * 根据 ID 取 Agent 详情.
     */
    AiAgentDetailBO getOneById(Long id);

    /**
     * 根据 ID 取 Agent 详情.
     */
    AiAgentDetailBO getOneById(Long id, boolean throwIfInvalidId) throws BusinessException;

    /**
     * 后台管理-新增 Agent.
     *
     * @return 主键ID
     */
    Long adminInsert(AdminInsertOrUpdateAiAgentDTO dto);

    /**
     * 后台管理-编辑 Agent.
     */
    void adminUpdate(AdminInsertOrUpdateAiAgentDTO dto);

    /**
     * 后台管理-删除 Agent.
     */
    void adminDelete(Collection<Long> ids);

    /**
     * 后台管理-更新 Agent 启停状态.
     */
    void adminUpdateStatus(Long id, Integer status);
}
