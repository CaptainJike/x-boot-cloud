package io.github.module.adminapi.web.sys;

import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.framework.web.model.request.IdsDTO;
import io.github.framework.web.model.response.ApiResult;
import io.github.module.adminapi.helper.RolePermissionCacheHelper;
import io.github.module.sys.annotation.SysLog;
import io.github.module.sys.facade.SysRoleFacade;
import io.github.module.sys.model.request.AdminBindRoleMenuRelationDTO;
import io.github.module.sys.model.request.AdminInsertOrUpdateSysRoleDTO;
import io.github.module.sys.model.request.AdminListSysRoleDTO;
import io.github.module.sys.model.response.SysRoleBO;
import io.github.module.adminapi.util.AdminStpUtil;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Set;


@SaCheckLogin(type = AdminStpUtil.TYPE)
@Tag(name = "后台角色管理接口")
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@RestController
@Slf4j
public class AdminSysRoleController {

    private static final String PERMISSION_PREFIX = "SysRole:";

    private final RolePermissionCacheHelper rolePermissionCacheHelper;

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private SysRoleFacade sysRoleFacade;


    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.RETRIEVE)
    @Operation(summary = "分页列表")
    @GetMapping(value = "/sys/roles")
    public ApiResult<PageResult<SysRoleBO>> list(PageParam pageParam, AdminListSysRoleDTO dto) {
        return ApiResult.data(sysRoleFacade.adminList(pageParam, dto));
    }

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.RETRIEVE)
    @Operation(summary = "详情")
    @GetMapping(value = "/sys/roles/{id}")
    public ApiResult<SysRoleBO> getById(@PathVariable("id") Long id) {
        return ApiResult.data(sysRoleFacade.getOneById(id, true));
    }

    @SysLog(value = "新增后台角色")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.CREATE)
    @Operation(summary = "新增")
    @PostMapping(value = "/sys/roles")
    public ApiResult<Void> insert(@RequestBody @Valid AdminInsertOrUpdateSysRoleDTO dto) {
        dto.setTenantId(null);
        sysRoleFacade.adminInsert(dto);

        return ApiResult.success();
    }

    @SysLog(value = "编辑后台角色")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.UPDATE)
    @Operation(summary = "编辑")
    @PutMapping(value = "/sys/roles/{id}")
    public ApiResult<Void> update(@PathVariable("id") Long id, @RequestBody @Valid AdminInsertOrUpdateSysRoleDTO dto) {
        dto
                .setTenantId(null)
                .setId(id);
        sysRoleFacade.adminUpdate(dto);

        return ApiResult.success();
    }

    @SysLog(value = "删除后台角色")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.DELETE)
    @Operation(summary = "删除")
    @DeleteMapping(value = "/sys/roles")
    public ApiResult<Void> delete(@RequestBody @Valid IdsDTO<Long> dto) {
        sysRoleFacade.adminDelete(dto.getIds());

        // 角色删除时，删除对应缓存键
        rolePermissionCacheHelper.deleteCache(dto.getIds());

        return ApiResult.success();
    }

    @SysLog(value = "绑定角色与菜单关联关系")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + "bindMenus")
    @Operation(summary = "绑定角色与菜单关联关系")
    @PutMapping(value = "/sys/roles/{id}/menus")
    public ApiResult<Void> bindMenus(@PathVariable("id") Long id, @RequestBody @Valid AdminBindRoleMenuRelationDTO dto) {
        dto.setRoleId(id);
        Set<String> newPermissions = sysRoleFacade.adminBindMenus(dto);

        // 覆盖更新缓存
        rolePermissionCacheHelper.putCache(dto.getRoleId(), newPermissions);

        return ApiResult.success();
    }

}
