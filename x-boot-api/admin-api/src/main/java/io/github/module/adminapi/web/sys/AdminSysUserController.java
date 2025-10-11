package io.github.module.adminapi.web.sys;

import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.framework.web.model.request.IdsDTO;
import io.github.framework.web.model.response.ApiResult;
import io.github.module.adminapi.event.KickOutSysUsersEvent;
import io.github.module.adminapi.util.AdminStpUtil;
import io.github.module.sys.annotation.SysLog;
import io.github.module.sys.enums.SysUserStatusEnum;
import io.github.module.sys.facade.SysUserFacade;
import io.github.module.sys.model.request.AdminBindUserRoleRelationDTO;
import io.github.module.sys.model.request.AdminInsertOrUpdateSysUserDTO;
import io.github.module.sys.model.request.AdminListSysUserDTO;
import io.github.module.sys.model.request.AdminResetSysUserPasswordDTO;
import io.github.module.sys.model.response.SysUserBO;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.extra.spring.SpringUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Set;


@SaCheckLogin(type = AdminStpUtil.TYPE)
@Tag(name = "后台用户管理接口")
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@RestController
@Slf4j
public class AdminSysUserController {

    private static final String PERMISSION_PREFIX = "SysUser:";

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private SysUserFacade sysUserFacade;


    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.RETRIEVE)
    @Operation(summary = "分页列表")
    @GetMapping(value = "/sys/users")
    public ApiResult<PageResult<SysUserBO>> list(PageParam pageParam, AdminListSysUserDTO dto) {
        return ApiResult.data(sysUserFacade.adminList(pageParam, dto));
    }

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.RETRIEVE)
    @Operation(summary = "详情")
    @GetMapping(value = "/sys/users/{id}")
    public ApiResult<SysUserBO> getById(@PathVariable("id") Long id) {
        return ApiResult.data(sysUserFacade.getOneById(id, true));
    }

    @SysLog(value = "新增后台用户")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.CREATE)
    @Operation(summary = "新增")
    @PostMapping(value = "/sys/users")
    public ApiResult<Void> insert(@RequestBody @Valid AdminInsertOrUpdateSysUserDTO dto) {
        dto.setId(null).setTenantId(null).validate();
        sysUserFacade.adminInsert(dto);

        return ApiResult.success();
    }

    @SysLog(value = "编辑后台用户")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.UPDATE)
    @Operation(summary = "编辑")
    @PutMapping(value = "/sys/users/{id}")
    public ApiResult<Void> update(@PathVariable("id") Long id, @RequestBody @Valid AdminInsertOrUpdateSysUserDTO dto) {
        dto.setId(id).setTenantId(null).validate();
        sysUserFacade.adminUpdate(dto);

        // 新状态是禁用，异步强制登出
        if (dto.getStatus() == SysUserStatusEnum.BANNED) {
            SpringUtil.publishEvent(new KickOutSysUsersEvent(
                    new KickOutSysUsersEvent.EventData(Collections.singleton(dto.getId()))
            ));
        }

        return ApiResult.success();
    }

    @SysLog(value = "删除后台用户")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.DELETE)
    @Operation(summary = "删除")
    @DeleteMapping(value = "/sys/users")
    public ApiResult<Void> delete(@RequestBody @Valid IdsDTO<Long> dto) {
        sysUserFacade.adminDelete(dto.getIds());

        // 异步强制登出
        SpringUtil.publishEvent(new KickOutSysUsersEvent(
                new KickOutSysUsersEvent.EventData(dto.getIds())
        ));

        return ApiResult.success();
    }

    @SysLog(value = "重置后台用户密码")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + "resetPassword")
    @Operation(summary = "重置后台用户密码")
    @PutMapping(value = "/sys/users/{userId}/password")
    public ApiResult<Void> resetPassword(@PathVariable("userId") Long userId, @RequestBody @Valid AdminResetSysUserPasswordDTO dto) {
        dto.setUserId(userId);
        sysUserFacade.adminResetUserPassword(dto);

        // 强制登出
        AdminStpUtil.kickout(dto.getUserId());

        return ApiResult.success();
    }

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + "bindRoles")
    @Operation(summary = "绑定用户与角色关联关系")
    @PutMapping(value = "/sys/users/{userId}/roles")
    public ApiResult<Void> bindRoles(@PathVariable("userId") Long userId, @RequestBody AdminBindUserRoleRelationDTO dto) {
        dto.setUserId(userId);
        sysUserFacade.adminBindRoles(dto);

        // 异步强制登出，以更新对应权限；可以视业务需要决定是否删除该代码
        SpringUtil.publishEvent(new KickOutSysUsersEvent(
                new KickOutSysUsersEvent.EventData(Collections.singleton(dto.getUserId()))
        ));

        return ApiResult.success();
    }

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + "kickOut")
    @Operation(summary = "踢某用户下线")
    @PostMapping(value = "/sys/users/{userId}:kick-out")
    public ApiResult<Void> kickOut(@PathVariable("userId") Long userId) {
        AdminStpUtil.kickout(userId);

        return ApiResult.success();
    }

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.RETRIEVE)
    @Operation(summary = "取指定用户关联角色ID")
    @GetMapping(value = "/sys/users/{userId}/roles")
    public ApiResult<Set<Long>> listRelatedRoleIds(@PathVariable("userId") Long userId) {
        return ApiResult.data(sysUserFacade.listRelatedRoleIds(userId));
    }

}
