package io.github.module.ai.mapper;

import io.github.module.sys.entity.SysTenantEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统租户
 */
@Mapper
public interface SysTenantMapper extends BaseMapper<SysTenantEntity> {
	
}
