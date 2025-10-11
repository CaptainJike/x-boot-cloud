package io.github.module.adminapi.web.sys;

import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.framework.web.model.request.IdsDTO;
import io.github.framework.web.model.response.ApiResult;
import io.github.module.adminapi.util.AdminStpUtil;
import io.github.module.sys.annotation.SysLog;
import io.github.module.sys.facade.SysDataDictFacade;
import io.github.module.sys.model.request.AdminSysDataDictClassifiedInsertOrUpdateDTO;
import io.github.module.sys.model.request.AdminSysDataDictClassifiedListDTO;
import io.github.module.sys.model.request.AdminSysDataDictItemInsertOrUpdateDTO;
import io.github.module.sys.model.request.AdminSysDataDictItemListDTO;
import io.github.module.sys.model.response.SysDataDictClassifiedBO;
import io.github.module.sys.model.response.SysDataDictItemBO;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;


@SaCheckLogin(type = AdminStpUtil.TYPE)
@Tag(name = "数据字典管理接口")
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@RestController
@Slf4j
public class AdminSysDataDictController {

    private static final String PERMISSION_PREFIX = "SysDataDict:";

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private SysDataDictFacade sysDataDictFacade;


    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.RETRIEVE)
    @Operation(summary = "分页列表数据字典分类")
    @GetMapping(value = "/sys/data-dict/classifieds")
    public ApiResult<PageResult<SysDataDictClassifiedBO>> list(PageParam pageParam, AdminSysDataDictClassifiedListDTO dto) {
        return ApiResult.data(sysDataDictFacade.adminListClassified(pageParam, dto));
    }

    @SysLog(value = "新增数据字典分类")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.CREATE)
    @Operation(summary = "新增数据字典分类")
    @PostMapping(value = "/sys/data-dict/classifieds")
    public ApiResult<Void> insert(@RequestBody @Valid AdminSysDataDictClassifiedInsertOrUpdateDTO dto) {
        sysDataDictFacade.adminInsertClassified(dto);

        return ApiResult.success();
    }

    @SysLog(value = "编辑数据字典分类")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.UPDATE)
    @Operation(summary = "编辑数据字典分类")
    @PutMapping(value = "/sys/data-dict/classifieds/{id}")
    public ApiResult<Void> update(@PathVariable("id") Long id, @RequestBody @Valid AdminSysDataDictClassifiedInsertOrUpdateDTO dto) {
        dto.setId(id);
        sysDataDictFacade.adminUpdateClassified(dto);

        return ApiResult.success();
    }

    @SysLog(value = "删除数据字典分类")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.DELETE)
    @Operation(summary = "删除数据字典分类")
    @DeleteMapping(value = "/sys/data-dict/classifieds")
    public ApiResult<Void> deleteClassified(@RequestBody @Valid IdsDTO<Long> dto) {
        sysDataDictFacade.adminDeleteClassified(dto.getIds());

        return ApiResult.success();
    }

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.RETRIEVE)
    @Operation(summary = "分页列表数据字典分类下的字典项")
    @GetMapping(value = "/sys/data-dict/classifieds/{classifiedId}/items")
    public ApiResult<PageResult<SysDataDictItemBO>> list(@PathVariable("classifiedId") Long classifiedId, PageParam pageParam, AdminSysDataDictItemListDTO dto) {
        dto.setClassifiedId(classifiedId);
        return ApiResult.data(sysDataDictFacade.adminListItem(pageParam, dto));
    }

    @SysLog(value = "新增数据字典项")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.CREATE)
    @Operation(summary = "新增数据字典项")
    @PostMapping(value = "/sys/data-dict/classifieds/{classifiedId}/items")
    public ApiResult<Void> insert(@PathVariable("classifiedId") Long classifiedId, @RequestBody @Valid AdminSysDataDictItemInsertOrUpdateDTO dto) {
        dto.setClassifiedId(classifiedId);
        sysDataDictFacade.adminInsertItem(dto);

        return ApiResult.success();
    }

    @SysLog(value = "编辑数据字典项")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.UPDATE)
    @Operation(summary = "编辑数据字典项")
    @PutMapping(value = "/sys/data-dict/classifieds/{classifiedId}/items/{id}")
    public ApiResult<Void> update(@PathVariable("classifiedId") Long classifiedId, @PathVariable("id") Long id, @RequestBody @Valid AdminSysDataDictItemInsertOrUpdateDTO dto) {
        dto
                .setId(id)
                .setClassifiedId(classifiedId);
        sysDataDictFacade.adminUpdateItem(dto);

        return ApiResult.success();
    }

    @SysLog(value = "删除数据字典项")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.DELETE)
    @Operation(summary = "删除数据字典项")
    @DeleteMapping(value = "/sys/data-dict/classifieds/{classifiedId}/items")
    public ApiResult<Void> deleteItem(@PathVariable("classifiedId") Long classifiedId, @RequestBody @Valid IdsDTO<Long> dto) {
        sysDataDictFacade.adminDeleteItem(dto.getIds(), classifiedId);

        return ApiResult.success();
    }

}
