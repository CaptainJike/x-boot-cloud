package io.github.module.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.module.ai.entity.AiAgentEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI Agent 配置.
 */
@Mapper
public interface AiAgentMapper extends BaseMapper<AiAgentEntity> {
}
