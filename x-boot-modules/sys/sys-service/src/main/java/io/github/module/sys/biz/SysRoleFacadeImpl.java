package io.github.module.sys.biz;

import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.exception.BusinessException;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.module.sys.facade.SysRoleFacade;
import io.github.module.sys.model.request.AdminBindRoleMenuRelationDTO;
import io.github.module.sys.model.request.AdminInsertOrUpdateSysRoleDTO;
import io.github.module.sys.model.request.AdminListSysRoleDTO;
import io.github.module.sys.model.response.SysRoleBO;
import io.github.module.sys.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 后台角色Facade接口实现类
 */
@RequiredArgsConstructor
@DubboService(
        version = BaseConstant.Version.DUBBO_VERSION_V1,
        validation = BaseConstant.Dubbo.ENABLE_VALIDATION,
        timeout = BaseConstant.Dubbo.TIMEOUT
)
@Slf4j
public class SysRoleFacadeImpl implements SysRoleFacade {

    private final SysRoleService sysRoleService;


    @Override
    public PageResult<SysRoleBO> adminList(PageParam pageParam, AdminListSysRoleDTO dto) {
        return sysRoleService.adminList(pageParam, dto);
    }

    @Override
    public SysRoleBO getOneById(Long id) {
        return sysRoleService.getOneById(id);
    }

    @Override
    public SysRoleBO getOneById(Long id, boolean throwIfInvalidId) throws BusinessException {
        return sysRoleService.getOneById(id, throwIfInvalidId);
    }

    @Override
    public Long adminInsert(AdminInsertOrUpdateSysRoleDTO dto) {
        return sysRoleService.adminInsert(dto);
    }

    @Override
    public void adminUpdate(AdminInsertOrUpdateSysRoleDTO dto) {
        sysRoleService.adminUpdate(dto);
    }

    @Override
    public void adminDelete(Collection<Long> ids) {
        sysRoleService.adminDelete(ids);
    }

    @Override
    public Set<String> adminBindMenus(AdminBindRoleMenuRelationDTO dto) {
        return sysRoleService.adminBindMenus(dto);
    }

    @Override
    public List<SysRoleBO> adminSelectOptions() {
        return sysRoleService.adminSelectOptions();
    }
}
