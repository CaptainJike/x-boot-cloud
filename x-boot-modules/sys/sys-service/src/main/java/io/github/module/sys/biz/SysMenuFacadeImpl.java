package io.github.module.sys.biz;

import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.exception.BusinessException;
import io.github.module.sys.facade.SysMenuFacade;
import io.github.module.sys.model.request.AdminInsertOrUpdateSysMenuDTO;
import io.github.module.sys.model.response.SysMenuBO;
import io.github.module.sys.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.Collection;
import java.util.List;

/**
 * 后台菜单Facade接口实现类
 */
@RequiredArgsConstructor
@DubboService(
        version = BaseConstant.Version.DUBBO_VERSION_V1,
        validation = BaseConstant.Dubbo.ENABLE_VALIDATION,
        timeout = BaseConstant.Dubbo.TIMEOUT
)
@Slf4j
public class SysMenuFacadeImpl implements SysMenuFacade {

    private final SysMenuService sysMenuService;


    @Override
    public List<SysMenuBO> adminList() {
        return sysMenuService.adminList();
    }

    @Override
    public SysMenuBO getOneById(Long id) {
        return sysMenuService.getOneById(id);
    }

    @Override
    public SysMenuBO getOneById(Long id, boolean throwIfInvalidId) throws BusinessException {
        return sysMenuService.getOneById(id, throwIfInvalidId);
    }

    @Override
    public Long adminInsert(AdminInsertOrUpdateSysMenuDTO dto) {
        return sysMenuService.adminInsert(dto);
    }

    @Override
    public void adminUpdate(AdminInsertOrUpdateSysMenuDTO dto) {
        sysMenuService.adminUpdate(dto);
    }

    @Override
    public void adminDelete(Collection<Long> ids) {
        sysMenuService.adminDelete(ids);
    }

    @Override
    public List<SysMenuBO> adminListSideMenu() {
        return sysMenuService.adminListSideMenu();
    }

    @Override
    public List<SysMenuBO> adminListVisibleMenu() {
        return sysMenuService.adminListVisibleMenu();
    }
}
