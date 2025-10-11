package io.github.module.sys.biz;

import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.exception.BusinessException;
import io.github.module.sys.facade.SysDeptFacade;
import io.github.module.sys.model.request.AdminInsertOrUpdateSysDeptDTO;
import io.github.module.sys.model.response.SysDeptBO;
import io.github.module.sys.service.SysDeptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.Collection;
import java.util.List;

/**
 * 部门Facade接口实现类
 */
@RequiredArgsConstructor
@DubboService(
        version = BaseConstant.Version.DUBBO_VERSION_V1,
        validation = BaseConstant.Dubbo.ENABLE_VALIDATION,
        timeout = BaseConstant.Dubbo.TIMEOUT
)
@Slf4j
public class SysDeptFacadeImpl implements SysDeptFacade {

    private final SysDeptService sysDeptService;


    @Override
    public List<SysDeptBO> adminList() {
        return sysDeptService.adminList();
    }

    @Override
    public SysDeptBO getOneById(Long id) {
        return sysDeptService.getOneById(id);
    }

    @Override
    public SysDeptBO getOneById(Long id, boolean throwIfInvalidId) throws BusinessException {
        return sysDeptService.getOneById(id, throwIfInvalidId);
    }

    @Override
    public Long adminInsert(AdminInsertOrUpdateSysDeptDTO dto) {
        return sysDeptService.adminInsert(dto);
    }

    @Override
    public void adminUpdate(AdminInsertOrUpdateSysDeptDTO dto) {
        sysDeptService.adminUpdate(dto);
    }

    @Override
    public void adminDelete(Collection<Long> ids) {
        sysDeptService.adminDelete(ids);
    }

    @Override
    public List<SysDeptBO> adminSelectOptions(boolean inferiorsOnly) {
        return sysDeptService.adminSelectOptions(inferiorsOnly);
    }
}
