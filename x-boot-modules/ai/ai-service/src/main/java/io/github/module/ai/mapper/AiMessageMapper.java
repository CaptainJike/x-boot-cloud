package io.github.module.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.module.ai.entity.AiMessageEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 消息.
 */
@Mapper
public interface AiMessageMapper extends BaseMapper<AiMessageEntity> {
}
