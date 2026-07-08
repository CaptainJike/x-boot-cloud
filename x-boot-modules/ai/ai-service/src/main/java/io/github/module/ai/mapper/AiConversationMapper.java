package io.github.module.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.module.ai.entity.AiConversationEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 会话.
 */
@Mapper
public interface AiConversationMapper extends BaseMapper<AiConversationEntity> {
}
