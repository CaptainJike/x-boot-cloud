package io.github.module.ai.facade;

import io.github.framework.core.exception.BusinessException;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.module.ai.model.request.AdminInsertOrUpdateAiModelConfigDTO;
import io.github.module.ai.model.request.AdminListAiModelConfigDTO;
import io.github.module.ai.model.request.AdminListProviderModelDTO;
import io.github.module.ai.model.response.AiModelConfigBO;
import io.github.module.ai.model.response.AiModelConfigTestBO;
import io.github.module.ai.model.response.AiProviderModelBO;

import java.util.Collection;
import java.util.List;

/**
 * AI 模型配置 Facade 接口.
 */
public interface AiModelConfigFacade {

    /**
     * 后台管理-分页列表.
     */
    PageResult<AiModelConfigBO> adminList(PageParam pageParam, AdminListAiModelConfigDTO dto);

    /**
     * 后台管理-启用模型配置下拉框.
     */
    List<AiModelConfigBO> adminSelectOptions();

    /**
     * 根据 ID 取详情.
     */
    AiModelConfigBO getOneById(Long id);

    /**
     * 根据 ID 取详情.
     */
    AiModelConfigBO getOneById(Long id, boolean throwIfInvalidId) throws BusinessException;

    /**
     * 后台管理-查看完整 API Key.
     */
    String adminGetApiKey(Long id) throws BusinessException;

    /**
     * 根据配置编码取启用配置.
     */
    AiModelConfigBO getEnabledConfigByCode(String code, boolean throwIfInvalidCode) throws BusinessException;

    /**
     * 取默认启用配置.
     */
    AiModelConfigBO getDefaultEnabledConfig();

    /**
     * 后台管理-新增.
     *
     * @return 主键ID
     */
    Long adminInsert(AdminInsertOrUpdateAiModelConfigDTO dto);

    /**
     * 后台管理-编辑.
     */
    void adminUpdate(AdminInsertOrUpdateAiModelConfigDTO dto);

    /**
     * 后台管理-删除.
     *
     * @param ids 主键IDs
     */
    void adminDelete(Collection<Long> ids);

    /**
     * 后台管理-检测模型配置是否可用.
     */
    AiModelConfigTestBO adminTest(Long id);

    /**
     * 后台管理-查询供应商模型列表.
     */
    List<AiProviderModelBO> adminListProviderModels(AdminListProviderModelDTO dto);
}
