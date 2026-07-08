package io.github.module.ai.facade;

import io.github.framework.core.exception.BusinessException;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.module.ai.model.request.AdminInsertOrUpdateAiKnowledgeBaseDTO;
import io.github.module.ai.model.request.AdminListAiKnowledgeBaseDTO;
import io.github.module.ai.model.response.AiKnowledgeBaseBO;
import io.github.module.ai.model.response.AiKnowledgeBaseDetailBO;

import java.util.Collection;
import java.util.List;

/**
 * 后台 AI 知识库 Facade 接口.
 */
public interface AiKnowledgeBaseFacade {

    /**
     * 后台管理-分页列表知识库.
     */
    PageResult<AiKnowledgeBaseBO> adminList(PageParam pageParam, AdminListAiKnowledgeBaseDTO dto);

    /**
     * 后台管理-启用知识库下拉框.
     */
    List<AiKnowledgeBaseBO> adminSelectOptions();

    /**
     * 根据 ID 取知识库详情.
     */
    AiKnowledgeBaseDetailBO getOneById(Long id);

    /**
     * 根据 ID 取知识库详情.
     */
    AiKnowledgeBaseDetailBO getOneById(Long id, boolean throwIfInvalidId) throws BusinessException;

    /**
     * 后台管理-新增知识库.
     *
     * @return 主键ID
     */
    Long adminInsert(AdminInsertOrUpdateAiKnowledgeBaseDTO dto);

    /**
     * 后台管理-编辑知识库.
     */
    void adminUpdate(AdminInsertOrUpdateAiKnowledgeBaseDTO dto);

    /**
     * 后台管理-删除知识库.
     */
    void adminDelete(Collection<Long> ids);

    /**
     * 后台管理-更新知识库启停状态.
     */
    void adminUpdateStatus(Long id, Integer status);
}
