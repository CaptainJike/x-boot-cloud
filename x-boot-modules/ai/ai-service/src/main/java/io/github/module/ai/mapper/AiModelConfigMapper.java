package io.github.module.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.module.ai.entity.AiModelConfigEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 模型配置.
 */
@Mapper
public interface AiModelConfigMapper extends BaseMapper<AiModelConfigEntity> {
}
