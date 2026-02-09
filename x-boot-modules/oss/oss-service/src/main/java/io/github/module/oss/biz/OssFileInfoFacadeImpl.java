package io.github.module.oss.biz;

import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.exception.BusinessException;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.module.oss.facade.OssFileInfoFacade;
import io.github.module.oss.model.request.AdminListOssFileInfoDTO;
import io.github.module.oss.model.response.OssFileInfoBO;
import io.github.module.oss.service.OssFileInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.Collection;

/**
 * 上传文件信息 Facade 接口实现类
 */
@RequiredArgsConstructor
@DubboService(
        version = BaseConstant.Version.DUBBO_VERSION_V1,
        
        timeout = BaseConstant.Dubbo.TIMEOUT
)
@Slf4j
public class OssFileInfoFacadeImpl implements OssFileInfoFacade {

    private final OssFileInfoService ossFileInfoService;


    @Override
    public PageResult<OssFileInfoBO> adminList(PageParam pageParam, AdminListOssFileInfoDTO dto) {
        return ossFileInfoService.adminList(pageParam, dto);
    }

    @Override
    public OssFileInfoBO getOneById(Long id) {
        return ossFileInfoService.getOneById(id);
    }

    @Override
    public OssFileInfoBO getOneById(Long id, boolean throwIfInvalidId) throws BusinessException {
        return ossFileInfoService.getOneById(id, throwIfInvalidId);
    }

    @Override
    public void adminDelete(Collection<Long> ids) {
        ossFileInfoService.adminDelete(ids);
    }

    @Override
    public OssFileInfoBO getOneByMd5(String md5) {
        return ossFileInfoService.getOneByMd5(md5);
    }

}
