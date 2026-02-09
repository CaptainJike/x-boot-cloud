package io.github.module.sys.biz;

import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.exception.BusinessException;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.module.sys.facade.SysLogFacade;
import io.github.module.sys.model.request.AdminInsertSysLogDTO;
import io.github.module.sys.model.request.AdminListSysLogDTO;
import io.github.module.sys.model.response.SysLogBO;
import io.github.module.sys.service.SysLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 系统日志Facade接口实现类
 */
@RequiredArgsConstructor
@DubboService(
        version = BaseConstant.Version.DUBBO_VERSION_V1,
        validation = BaseConstant.Dubbo.ENABLE_VALIDATION,
        timeout = BaseConstant.Dubbo.TIMEOUT
)
@Slf4j
public class SysLogFacadeImpl implements SysLogFacade {

    private final SysLogService sysLogService;


    @Override
    public PageResult<SysLogBO> adminList(PageParam pageParam, AdminListSysLogDTO dto) {
        return sysLogService.adminList(pageParam, dto);
    }

    @Override
    public SysLogBO getOneById(Long id) {
        return sysLogService.getOneById(id);
    }

    @Override
    public SysLogBO getOneById(Long id, boolean throwIfInvalidId) throws BusinessException {
        return sysLogService.getOneById(id, throwIfInvalidId);
    }

    @Override
    public Long adminInsert(AdminInsertSysLogDTO dto) {
        return sysLogService.adminInsert(dto);
    }
}
