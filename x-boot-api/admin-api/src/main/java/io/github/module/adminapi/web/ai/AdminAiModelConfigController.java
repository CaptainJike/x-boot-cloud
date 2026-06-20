package io.github.module.adminapi.web.ai;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.framework.core.constant.ApiPrefixConstant;
import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.framework.web.model.request.IdsDTO;
import io.github.framework.web.model.response.ApiResult;
import io.github.module.adminapi.util.AdminStpUtil;
import io.github.module.ai.facade.AiModelConfigFacade;
import io.github.module.ai.model.request.AdminInsertOrUpdateAiModelConfigDTO;
import io.github.module.ai.model.request.AdminListAiModelConfigDTO;
import io.github.module.ai.model.request.AdminListProviderModelDTO;
import io.github.module.ai.model.response.AiModelConfigBO;
import io.github.module.ai.model.response.AiModelConfigTestBO;
import io.github.module.ai.model.response.AiProviderModelBO;
import io.github.module.sys.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SaCheckLogin(type = AdminStpUtil.TYPE)
@Tag(name = "后台管理-AI模型配置接口")
@RequestMapping(ApiPrefixConstant.API_PREFIX_ADMIN + ApiPrefixConstant.VERSION)
@RequiredArgsConstructor
@RestController
@Slf4j
public class AdminAiModelConfigController {

    private static final String PERMISSION_PREFIX = "AiModelConfig:";

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private AiModelConfigFacade aiModelConfigFacade;

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.RETRIEVE, orRole = "SuperAdmin")
    @Operation(summary = "分页列表")
    @GetMapping(value = "/ai/model-configs")
    public ApiResult<PageResult<AiModelConfigBO>> list(PageParam pageParam, AdminListAiModelConfigDTO dto) {
        return ApiResult.data(aiModelConfigFacade.adminList(pageParam, dto));
    }

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.RETRIEVE, orRole = "SuperAdmin")
    @Operation(summary = "详情")
    @GetMapping(value = "/ai/model-configs/{id}")
    public ApiResult<AiModelConfigBO> getById(@PathVariable("id") Long id) {
        return ApiResult.data(aiModelConfigFacade.getOneById(id, true));
    }

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + "key", orRole = "SuperAdmin")
    @Operation(summary = "查看完整API Key")
    @GetMapping(value = "/ai/model-configs/{id}/api-key")
    public ApiResult<String> getApiKey(@PathVariable("id") Long id) {
        return ApiResult.data(aiModelConfigFacade.adminGetApiKey(id));
    }

    @SysLog(value = "新增AI模型配置")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.CREATE, orRole = "SuperAdmin")
    @Operation(summary = "新增")
    @PostMapping(value = "/ai/model-configs")
    public ApiResult<Void> insert(@RequestBody @Valid AdminInsertOrUpdateAiModelConfigDTO dto) {
        aiModelConfigFacade.adminInsert(dto);

        return ApiResult.success();
    }

    @SysLog(value = "编辑AI模型配置")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.UPDATE, orRole = "SuperAdmin")
    @Operation(summary = "编辑")
    @PutMapping(value = "/ai/model-configs/{id}")
    public ApiResult<Void> update(@PathVariable("id") Long id,
                                  @RequestBody @Valid AdminInsertOrUpdateAiModelConfigDTO dto) {
        dto.setId(id);
        aiModelConfigFacade.adminUpdate(dto);

        return ApiResult.success();
    }

    @SysLog(value = "删除AI模型配置")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.DELETE, orRole = "SuperAdmin")
    @Operation(summary = "删除")
    @DeleteMapping(value = "/ai/model-configs")
    public ApiResult<Void> delete(@RequestBody @Valid IdsDTO<Long> dto) {
        aiModelConfigFacade.adminDelete(dto.getIds());

        return ApiResult.success();
    }

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + "test", orRole = "SuperAdmin")
    @Operation(summary = "检测模型配置")
    @PostMapping(value = "/ai/model-configs/{id}:test")
    public ApiResult<AiModelConfigTestBO> test(@PathVariable("id") Long id) {
        return ApiResult.data(aiModelConfigFacade.adminTest(id));
    }

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + "models", orRole = "SuperAdmin")
    @Operation(summary = "查询供应商模型列表")
    @PostMapping(value = "/ai/model-configs/provider-models")
    public ApiResult<List<AiProviderModelBO>> providerModels(@RequestBody @Valid AdminListProviderModelDTO dto) {
        return ApiResult.data(aiModelConfigFacade.adminListProviderModels(dto));
    }
}
