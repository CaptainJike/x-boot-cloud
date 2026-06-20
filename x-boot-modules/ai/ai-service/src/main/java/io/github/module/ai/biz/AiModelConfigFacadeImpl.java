package io.github.module.ai.biz;

import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.exception.BusinessException;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.module.ai.facade.AiModelConfigFacade;
import io.github.module.ai.model.request.AdminInsertOrUpdateAiModelConfigDTO;
import io.github.module.ai.model.request.AdminListAiModelConfigDTO;
import io.github.module.ai.model.request.AdminListProviderModelDTO;
import io.github.module.ai.model.response.AiModelConfigBO;
import io.github.module.ai.model.response.AiModelConfigTestBO;
import io.github.module.ai.model.response.AiProviderModelBO;
import io.github.module.ai.service.AiModelConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.Collection;
import java.util.List;

/**
 * AI 模型配置 Facade 接口实现类.
 */
@RequiredArgsConstructor
@DubboService(
        version = BaseConstant.Version.DUBBO_VERSION_V1,
        validation = BaseConstant.Dubbo.ENABLE_VALIDATION,
        timeout = BaseConstant.Dubbo.TIMEOUT
)
@Slf4j
public class AiModelConfigFacadeImpl implements AiModelConfigFacade {

    private final AiModelConfigService aiModelConfigService;

    @Override
    public PageResult<AiModelConfigBO> adminList(PageParam pageParam, AdminListAiModelConfigDTO dto) {
        return aiModelConfigService.adminList(pageParam, dto);
    }

    @Override
    public List<AiModelConfigBO> adminSelectOptions() {
        return aiModelConfigService.adminSelectOptions();
    }

    @Override
    public AiModelConfigBO getOneById(Long id) {
        return aiModelConfigService.getOneById(id);
    }

    @Override
    public AiModelConfigBO getOneById(Long id, boolean throwIfInvalidId) throws BusinessException {
        return aiModelConfigService.getOneById(id, throwIfInvalidId);
    }

    @Override
    public String adminGetApiKey(Long id) throws BusinessException {
        return aiModelConfigService.adminGetApiKey(id);
    }

    @Override
    public AiModelConfigBO getEnabledConfigByCode(String code, boolean throwIfInvalidCode) throws BusinessException {
        return aiModelConfigService.getEnabledConfigByCode(code, throwIfInvalidCode);
    }

    @Override
    public AiModelConfigBO getDefaultEnabledConfig() {
        return aiModelConfigService.getDefaultEnabledConfig();
    }

    @Override
    public Long adminInsert(AdminInsertOrUpdateAiModelConfigDTO dto) {
        return aiModelConfigService.adminInsert(dto);
    }

    @Override
    public void adminUpdate(AdminInsertOrUpdateAiModelConfigDTO dto) {
        aiModelConfigService.adminUpdate(dto);
    }

    @Override
    public void adminDelete(Collection<Long> ids) {
        aiModelConfigService.adminDelete(ids);
    }

    @Override
    public AiModelConfigTestBO adminTest(Long id) {
        return aiModelConfigService.adminTest(id);
    }

    @Override
    public List<AiProviderModelBO> adminListProviderModels(AdminListProviderModelDTO dto) {
        return aiModelConfigService.adminListProviderModels(dto);
    }
}
