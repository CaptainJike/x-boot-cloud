package io.github.module.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.module.ai.entity.AiToolRegistryEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 工具注册.
 */
@Mapper
public interface AiToolRegistryMapper extends BaseMapper<AiToolRegistryEntity> {
}
