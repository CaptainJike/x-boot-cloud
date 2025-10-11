package io.github.module.adminapi.web.auth;


import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.context.TenantContext;
import io.github.framework.core.context.TenantContextHolder;
import io.github.framework.core.context.UserContext;
import io.github.framework.core.context.UserContextHolder;
import io.github.framework.web.model.response.ApiResult;
import io.github.module.adminapi.aspect.extension.SysLogAspectExtensionForSysUserLogin;
import io.github.module.adminapi.helper.CaptchaHelper;
import io.github.module.adminapi.helper.RolePermissionCacheHelper;
import io.github.module.adminapi.model.interior.AdminCaptchaContainer;
import io.github.module.adminapi.model.response.AdminCaptchaVO;
import io.github.module.adminapi.util.AdminStpUtil;
import io.github.module.sys.annotation.SysLog;
import io.github.module.sys.facade.SysUserFacade;
import io.github.module.sys.model.request.SysUserLoginDTO;
import io.github.module.sys.model.response.SysUserLoginBO;
import io.github.module.sys.model.response.SysUserLoginVO;
import cn.dev33.satoken.annotation.SaCheckLogin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;



@Tag(name = "后台管理-鉴权接口")
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@RestController
@Slf4j
public class AdminAuthController {

    private final RolePermissionCacheHelper rolePermissionCacheHelper;

    private final CaptchaHelper captchaHelper;

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private SysUserFacade sysUserFacade;


    @SysLog(value = "登录后台用户", syncSave = true, extension = SysLogAspectExtensionForSysUserLogin.class, queryIPLocation = true)
    @Operation(summary = "登录")
    @PostMapping(value = "/auth/login")
    public ApiResult<SysUserLoginVO> login(@RequestBody @Valid SysUserLoginDTO dto) {
        // AdminApiErrorEnum.CAPTCHA_VALIDATE_FAILED.assertTrue(captchaHelper.validate(dto.getCaptchaId(), dto.getCaptchaAnswer()))

        // RPC调用, 失败抛异常, 成功返回用户信息
        SysUserLoginBO userInfo = sysUserFacade.adminLogin(dto);

        // 构造用户上下文
        UserContext userContext = UserContext.builder()
                .userId(userInfo.getId())
                .userName(userInfo.getUsername())
                .userPhoneNo(userInfo.getPhoneNo())
                .userTypeStr("ADMIN_USER")
                .extraData(null)
                .rolesIds(userInfo.getRoleIds())
                .roles(userInfo.getRoles())
                .build();

        // 将用户ID注册到 SA-Token ，并附加一些业务字段
        AdminStpUtil.login(userInfo.getId(), dto.getRememberMe());
        AdminStpUtil.getSession().set(UserContext.CAMEL_NAME, userContext);
        AdminStpUtil.getSession().set(TenantContext.CAMEL_NAME, userInfo.getTenantContext());

        // 更新角色-权限缓存
        rolePermissionCacheHelper.putCache(userInfo.getRoleIdPermissionMap());

        // 返回登录token
        SysUserLoginVO tokenInfo = SysUserLoginVO.builder()
                .tokenName(AdminStpUtil.getTokenName())
                .tokenValue(AdminStpUtil.getTokenValue())
                .roles(userInfo.getRoles())
                .permissions(userInfo.getPermissions())
                .build();

        return ApiResult.data("登录成功", tokenInfo);
    }

    @SaCheckLogin(type = AdminStpUtil.TYPE)
    @Operation(summary = "登出")
    @PostMapping(value = "/auth/logout")
    public ApiResult<Void> logout() {
        AdminStpUtil.logout();
        UserContextHolder.clear();
        TenantContextHolder.clear();

        return ApiResult.success();
    }

    @Operation(summary = "获取验证码")
    @GetMapping(value = "/auth/captcha")
    public ApiResult<AdminCaptchaVO> captcha() {
        // 核验方法：captchaHelper.validate
        AdminCaptchaContainer captchaContainer = captchaHelper.generate();
        return ApiResult.data(new AdminCaptchaVO(captchaContainer));
    }

}
