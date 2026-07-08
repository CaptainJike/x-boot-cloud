package io.github.module.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.module.ai.entity.AiCallLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 调用日志.
 */
@Mapper
public interface AiCallLogMapper extends BaseMapper<AiCallLogEntity> {
}
