package io.github.module.sys.biz;

import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.module.sys.facade.SysDataDictFacade;
import io.github.module.sys.model.request.AdminSysDataDictClassifiedInsertOrUpdateDTO;
import io.github.module.sys.model.request.AdminSysDataDictClassifiedListDTO;
import io.github.module.sys.model.request.AdminSysDataDictItemInsertOrUpdateDTO;
import io.github.module.sys.model.request.AdminSysDataDictItemListDTO;
import io.github.module.sys.model.response.SysDataDictClassifiedBO;
import io.github.module.sys.model.response.SysDataDictItemBO;
import io.github.module.sys.service.SysDataDictService;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.Collection;
import java.util.List;


/**
 * 数据字典Facade接口实现类
 */
@RequiredArgsConstructor
@DubboService(
        version = BaseConstant.Version.DUBBO_VERSION_V1,
        validation = BaseConstant.Dubbo.ENABLE_VALIDATION,
        timeout = BaseConstant.Dubbo.TIMEOUT
)
@Slf4j
public class SysDataDictFacadeImpl implements SysDataDictFacade {

    private final SysDataDictService sysDataDictService;


    @Override
    public PageResult<SysDataDictClassifiedBO> adminListClassified(PageParam pageParam, AdminSysDataDictClassifiedListDTO dto) {
        return sysDataDictService.adminListClassified(pageParam, dto);
    }

    @Override
    public Long adminInsertClassified(AdminSysDataDictClassifiedInsertOrUpdateDTO dto) {
        return sysDataDictService.adminInsertClassified(dto);
    }

    @Override
    public void adminUpdateClassified(AdminSysDataDictClassifiedInsertOrUpdateDTO dto) {
        sysDataDictService.adminUpdateClassified(dto);
    }

    @Override
    public void adminDeleteClassified(Collection<Long> ids) {
        sysDataDictService.adminDeleteClassified(ids);
    }

    @Override
    public PageResult<SysDataDictItemBO> adminListItem(PageParam pageParam, AdminSysDataDictItemListDTO dto) {
        return sysDataDictService.adminListItem(pageParam, dto);
    }

    @Override
    public Long adminInsertItem(AdminSysDataDictItemInsertOrUpdateDTO dto) {
        return sysDataDictService.adminInsertItem(dto);
    }

    @Override
    public void adminUpdateItem(AdminSysDataDictItemInsertOrUpdateDTO dto) {
        sysDataDictService.adminUpdateItem(dto);
    }

    @Override
    public void adminDeleteItem(Collection<Long> ids, Long classifiedId) {
        sysDataDictService.adminDeleteItem(ids, classifiedId);
    }

    @Override
    public List<SysDataDictItemBO> listEnabledItemsByClassifiedCode(@Nonnull String classifiedCode) {
        return sysDataDictService.listEnabledItemsByClassifiedCode(classifiedCode);
    }
}
