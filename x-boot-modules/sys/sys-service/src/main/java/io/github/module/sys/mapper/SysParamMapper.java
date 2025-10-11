package io.github.module.sys.mapper;

import io.github.module.sys.entity.SysParamEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统参数
 */
@Mapper
public interface SysParamMapper extends BaseMapper<SysParamEntity> {
	
}
