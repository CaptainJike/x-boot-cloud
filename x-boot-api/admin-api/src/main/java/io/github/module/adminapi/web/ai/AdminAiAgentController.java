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
import io.github.module.ai.facade.AiAgentFacade;
import io.github.module.ai.model.request.AdminInsertOrUpdateAiAgentDTO;
import io.github.module.ai.model.request.AdminListAiAgentDTO;
import io.github.module.ai.model.response.AiAgentBO;
import io.github.module.ai.model.response.AiAgentDetailBO;
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
@Tag(name = "后台管理-AI Agent接口")
@RequestMapping(ApiPrefixConstant.API_PREFIX_ADMIN + ApiPrefixConstant.VERSION)
@RequiredArgsConstructor
@RestController
@Slf4j
public class AdminAiAgentController {

    private static final String PERMISSION_PREFIX = "AiAgent:";

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private AiAgentFacade aiAgentFacade;

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.RETRIEVE, orRole = "SuperAdmin")
    @Operation(summary = "Agent分页列表")
    @GetMapping(value = "/ai/agents")
    public ApiResult<PageResult<AiAgentBO>> list(PageParam pageParam, AdminListAiAgentDTO dto) {
        return ApiResult.data(aiAgentFacade.adminList(pageParam, dto));
    }

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.RETRIEVE, orRole = "SuperAdmin")
    @Operation(summary = "启用Agent下拉框")
    @GetMapping(value = "/ai/agents/options")
    public ApiResult<List<AiAgentBO>> options() {
        return ApiResult.data(aiAgentFacade.adminSelectOptions());
    }

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.RETRIEVE, orRole = "SuperAdmin")
    @Operation(summary = "Agent详情")
    @GetMapping(value = "/ai/agents/{id}")
    public ApiResult<AiAgentDetailBO> getById(@PathVariable("id") Long id) {
        return ApiResult.data(aiAgentFacade.getOneById(id, true));
    }

    @SysLog(value = "新增AI Agent")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.CREATE, orRole = "SuperAdmin")
    @Operation(summary = "新增Agent")
    @PostMapping(value = "/ai/agents")
    public ApiResult<Void> insert(@RequestBody @Valid AdminInsertOrUpdateAiAgentDTO dto) {
        aiAgentFacade.adminInsert(dto);

        return ApiResult.success();
    }

    @SysLog(value = "编辑AI Agent")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.UPDATE, orRole = "SuperAdmin")
    @Operation(summary = "编辑Agent")
    @PutMapping(value = "/ai/agents/{id}")
    public ApiResult<Void> update(@PathVariable("id") Long id,
                                  @RequestBody @Valid AdminInsertOrUpdateAiAgentDTO dto) {
        dto.setId(id);
        aiAgentFacade.adminUpdate(dto);

        return ApiResult.success();
    }

    @SysLog(value = "删除AI Agent")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.DELETE, orRole = "SuperAdmin")
    @Operation(summary = "删除Agent")
    @DeleteMapping(value = "/ai/agents")
    public ApiResult<Void> delete(@RequestBody @Valid IdsDTO<Long> dto) {
        aiAgentFacade.adminDelete(dto.getIds());

        return ApiResult.success();
    }

    @SysLog(value = "更新AI Agent启停状态")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + "enable", orRole = "SuperAdmin")
    @Operation(summary = "启停Agent")
    @PutMapping(value = "/ai/agents/{id}/status/{status}")
    public ApiResult<Void> updateStatus(@PathVariable("id") Long id,
                                        @PathVariable("status") Integer status) {
        aiAgentFacade.adminUpdateStatus(id, status);

        return ApiResult.success();
    }
}
