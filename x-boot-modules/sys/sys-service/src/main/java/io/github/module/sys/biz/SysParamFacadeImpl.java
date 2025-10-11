package io.github.module.sys.biz;

import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.exception.BusinessException;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.module.sys.facade.SysParamFacade;
import io.github.module.sys.model.request.AdminInsertOrUpdateSysParamDTO;
import io.github.module.sys.model.request.AdminListSysParamDTO;
import io.github.module.sys.model.response.SysParamBO;
import io.github.module.sys.service.SysParamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.Collection;

/**
 * 系统参数Facade接口实现类
 */
@RequiredArgsConstructor
@DubboService(
        version = BaseConstant.Version.DUBBO_VERSION_V1,
        validation = BaseConstant.Dubbo.ENABLE_VALIDATION,
        timeout = BaseConstant.Dubbo.TIMEOUT
)
@Slf4j
public class SysParamFacadeImpl implements SysParamFacade {

    private final SysParamService sysParamService;


    @Override
    public PageResult<SysParamBO> adminList(PageParam pageParam, AdminListSysParamDTO dto) {
        return sysParamService.adminList(pageParam, dto);
    }

    @Override
    public SysParamBO getOneById(Long id) {
        return sysParamService.getOneById(id);
    }

    @Override
    public SysParamBO getOneById(Long id, boolean throwIfInvalidId) throws BusinessException {
        return sysParamService.getOneById(id, throwIfInvalidId);
    }

    @Override
    public Long adminInsert(AdminInsertOrUpdateSysParamDTO dto) {
        return sysParamService.adminInsert(dto);
    }

    @Override
    public void adminUpdate(AdminInsertOrUpdateSysParamDTO dto) {
        sysParamService.adminUpdate(dto);
    }

    @Override
    public void adminDelete(Collection<Long> ids) {
        sysParamService.adminDelete(ids);
    }

    @Override
    public String getValueByName(String name) {
        return sysParamService.getParamValueByName(name);
    }

    @Override
    public String getValueByName(String name, String defaultValue) {
        return sysParamService.getParamValueByName(name, defaultValue);
    }
}
