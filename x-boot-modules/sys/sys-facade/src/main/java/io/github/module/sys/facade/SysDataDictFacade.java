package io.github.module.sys.facade;

import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.module.sys.model.request.AdminSysDataDictClassifiedInsertOrUpdateDTO;
import io.github.module.sys.model.request.AdminSysDataDictClassifiedListDTO;
import io.github.module.sys.model.request.AdminSysDataDictItemInsertOrUpdateDTO;
import io.github.module.sys.model.request.AdminSysDataDictItemListDTO;
import io.github.module.sys.model.response.SysDataDictClassifiedBO;
import io.github.module.sys.model.response.SysDataDictItemBO;
import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.List;


/**
 * 数据字典Facade接口
 */
public interface SysDataDictFacade {

    /**
     * 后台管理-分页列表数据字典分类
     */
    PageResult<SysDataDictClassifiedBO> adminListClassified(PageParam pageParam, AdminSysDataDictClassifiedListDTO dto);

    /**
     * 后台管理-新增数据字典分类
     *
     * @return 主键ID
     */
    Long adminInsertClassified(AdminSysDataDictClassifiedInsertOrUpdateDTO dto);

    /**
     * 后台管理-编辑数据字典分类
     */
    void adminUpdateClassified(AdminSysDataDictClassifiedInsertOrUpdateDTO dto);

    /**
     * 后台管理-删除数据字典分类
     */
    void adminDeleteClassified(Collection<Long> ids);

    /**
     * 后台管理-分页列表数据字典分类下的字典项
     */
    PageResult<SysDataDictItemBO> adminListItem(PageParam pageParam, AdminSysDataDictItemListDTO dto);

    /**
     * 后台管理-新增数据字典项
     *
     * @return 主键ID
     */
    Long adminInsertItem(AdminSysDataDictItemInsertOrUpdateDTO dto);

    /**
     * 后台管理-编辑数据字典项
     */
    void adminUpdateItem(AdminSysDataDictItemInsertOrUpdateDTO dto);

    /**
     * 后台管理-删除数据字典项
     */
    void adminDeleteItem(Collection<Long> ids, Long classifiedId);

    /**
     * 列举指定分类编码下的所有启用的字典项
     *
     * @return 存在则返回字典项列表；不存在或没有符合的字典项，均返回空列表
     */
    List<SysDataDictItemBO> listEnabledItemsByClassifiedCode(@Nonnull String classifiedCode);

}
