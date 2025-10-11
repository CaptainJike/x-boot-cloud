package io.github.module.adminapi.web.common;


import io.github.framework.core.constant.BaseConstant;
import io.github.framework.web.model.response.ApiResult;
import io.github.module.adminapi.model.response.AdminSelectOptionItemVO;
import io.github.module.adminapi.util.AdminStpUtil;
import io.github.module.sys.facade.SysDeptFacade;
import io.github.module.sys.facade.SysRoleFacade;
import io.github.module.sys.model.response.SysDeptBO;
import io.github.module.sys.model.response.SysRoleBO;
import cn.dev33.satoken.annotation.SaCheckLogin;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 约束：登录后才能使用   👇 后台管理对应的鉴权工具类
@SaCheckLogin(type = AdminStpUtil.TYPE)
@Tag(name = "后台管理-下拉框数据源接口")
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@RestController
@Slf4j
public class AdminSelectOptionsController {

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private SysRoleFacade sysRoleFacade;

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private SysDeptFacade sysDeptFacade;


    /*
    这里统一存放所有用于后台管理的下拉框数据源接口
    避免多人协作时，不知道原来是否已经有了，或者写在某个边边角角里，造成重复开发
    */

    @Operation(summary = "后台角色下拉框")
    @GetMapping(value = "/select-options/roles")
    public ApiResult<List<AdminSelectOptionItemVO>> roles() {
        return ApiResult.data(
                AdminSelectOptionItemVO.listOf(sysRoleFacade.adminSelectOptions(), SysRoleBO::getId, SysRoleBO::getTitle)
        );
    }

    @Operation(summary = "部门下拉框（前端负责转为树状数据）")
    @GetMapping(value = "/select-options/depts")
    public ApiResult<List<AdminSelectOptionItemVO>> depts() {
        return ApiResult.data(
                AdminSelectOptionItemVO.listOf(sysDeptFacade.adminSelectOptions(true), SysDeptBO::getId, SysDeptBO::getTitle, SysDeptBO::getParentId)
        );
    }

}
