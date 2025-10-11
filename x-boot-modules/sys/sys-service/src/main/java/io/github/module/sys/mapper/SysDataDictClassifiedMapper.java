package io.github.module.sys.mapper;

import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.enums.EnabledStatusEnum;
import io.github.module.sys.entity.SysDataDictClassifiedEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.ibatis.annotations.Mapper;

import java.util.Objects;


/**
 * 数据字典分类
 */
@Mapper
public interface SysDataDictClassifiedMapper extends BaseMapper<SysDataDictClassifiedEntity> {

    default SysDataDictClassifiedEntity selectByCode(@Nonnull String code, @Nullable EnabledStatusEnum status) {
        return selectOne(
                new QueryWrapper<SysDataDictClassifiedEntity>()
                        .lambda()
                        .eq(SysDataDictClassifiedEntity::getCode, code)
                        .eq(Objects.nonNull(status), SysDataDictClassifiedEntity::getStatus, status)
                        .last(BaseConstant.CRUD.SQL_LIMIT_1)
        );
    }

}
