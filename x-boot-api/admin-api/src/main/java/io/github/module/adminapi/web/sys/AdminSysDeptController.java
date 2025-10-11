package io.github.module.adminapi.web.sys;

import io.github.framework.core.constant.BaseConstant;
import io.github.framework.web.model.request.IdsDTO;
import io.github.framework.web.model.response.ApiResult;
import io.github.module.sys.annotation.SysLog;
import io.github.module.sys.facade.SysDeptFacade;
import io.github.module.sys.model.request.AdminInsertOrUpdateSysDeptDTO;
import io.github.module.sys.model.response.SysDeptBO;
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
import java.util.List;


@SaCheckLogin(type = AdminStpUtil.TYPE)
@Tag(name = "部门管理接口")
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@RestController
@Slf4j
public class AdminSysDeptController {

    private static final String PERMISSION_PREFIX = "SysDept:";

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private SysDeptFacade sysDeptFacade;


    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.RETRIEVE)
    @Operation(summary = "列表")
    @GetMapping(value = "/sys/depts")
    public ApiResult<List<SysDeptBO>> list() {
        return ApiResult.data(sysDeptFacade.adminList());
    }

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.RETRIEVE)
    @Operation(summary = "详情")
    @GetMapping(value = "/sys/depts/{id}")
    public ApiResult<SysDeptBO> getById(@PathVariable("id") Long id) {
        return ApiResult.data(sysDeptFacade.getOneById(id));
    }

    @SysLog(value = "新增部门")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.CREATE)
    @Operation(summary = "新增")
    @PostMapping(value = "/sys/depts")
    public ApiResult<Void> insert(@RequestBody @Valid AdminInsertOrUpdateSysDeptDTO dto) {
        sysDeptFacade.adminInsert(dto);

        return ApiResult.success();
    }

    @SysLog(value = "编辑部门")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.UPDATE)
    @Operation(summary = "编辑")
    @PutMapping(value = "/sys/depts/{id}")
    public ApiResult<Void> update(@PathVariable("id") Long id, @RequestBody @Valid AdminInsertOrUpdateSysDeptDTO dto) {
        dto.setId(id);
        sysDeptFacade.adminUpdate(dto);

        return ApiResult.success();
    }

    @SysLog(value = "删除部门")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.DELETE)
    @Operation(summary = "删除")
    @DeleteMapping(value = "/sys/depts")
    public ApiResult<Void> delete(@RequestBody @Valid IdsDTO<Long> dto) {
        sysDeptFacade.adminDelete(dto.getIds());

        return ApiResult.success();
    }

}
