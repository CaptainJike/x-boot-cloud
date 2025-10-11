package io.github.module.adminapi.web.sys;

import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.framework.web.model.request.IdsDTO;
import io.github.framework.web.model.response.ApiResult;
import io.github.module.adminapi.event.KickOutSysUsersEvent;
import io.github.module.adminapi.util.AdminStpUtil;
import io.github.module.sys.annotation.SysLog;
import io.github.module.sys.facade.SysTenantFacade;
import io.github.module.sys.model.request.AdminInsertSysTenantDTO;
import io.github.module.sys.model.request.AdminListSysTenantDTO;
import io.github.module.sys.model.request.AdminUpdateSysTenantDTO;
import io.github.module.sys.model.response.SysTenantBO;
import io.github.module.sys.model.response.SysTenantKickOutUsersBO;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.extra.spring.SpringUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;


@SaCheckLogin(type = AdminStpUtil.TYPE)
@Tag(name = "系统租户管理接口")
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@RestController
@Slf4j
public class AdminSysTenantController {

    private static final String PERMISSION_PREFIX = "SysTenant:";

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private SysTenantFacade sysTenantFacade;


    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.RETRIEVE)
    @Operation(summary = "分页列表")
    @GetMapping(value = "/sys/tenants")
    public ApiResult<PageResult<SysTenantBO>> list(PageParam pageParam, AdminListSysTenantDTO dto) {
        return ApiResult.data(sysTenantFacade.adminList(pageParam, dto));
    }

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.RETRIEVE)
    @Operation(summary = "详情")
    @GetMapping(value = "/sys/tenants/{id}")
    public ApiResult<SysTenantBO> getById(@PathVariable("id") Long id) {
        return ApiResult.data(sysTenantFacade.getOneById(id, true));
    }

    @SysLog(value = "新增系统租户")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.CREATE)
    @Operation(summary = "新增")
    @PostMapping(value = "/sys/tenants")
    public ApiResult<Void> insert(@RequestBody @Valid AdminInsertSysTenantDTO dto) {
        sysTenantFacade.adminInsert(dto);

        return ApiResult.success();
    }

    @SysLog(value = "编辑系统租户")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.UPDATE)
    @Operation(summary = "编辑")
    @PutMapping(value = "/sys/tenants/{id}")
    public ApiResult<Void> update(@PathVariable("id") Long id, @RequestBody @Valid AdminUpdateSysTenantDTO dto) {
        dto.setId(id);
        SysTenantKickOutUsersBO evictedUsers = sysTenantFacade.adminUpdate(dto);

        // 强制登出所有租户用户
        SpringUtil.publishEvent(new KickOutSysUsersEvent(
                new KickOutSysUsersEvent.EventData(evictedUsers.getSysUserIds())
        ));

        return ApiResult.success();
    }

    @SysLog(value = "删除系统租户")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.DELETE)
    @Operation(summary = "删除")
    @DeleteMapping(value = "/sys/tenants")
    public ApiResult<Void> delete(@RequestBody @Valid IdsDTO<Long> dto) {
        SysTenantKickOutUsersBO evictedUsers = sysTenantFacade.adminDelete(dto.getIds());

        // 强制登出所有租户用户
        SpringUtil.publishEvent(new KickOutSysUsersEvent(
                new KickOutSysUsersEvent.EventData(evictedUsers.getSysUserIds())
        ));

        return ApiResult.success();
    }

}
