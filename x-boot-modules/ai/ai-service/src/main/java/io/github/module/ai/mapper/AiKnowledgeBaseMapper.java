package io.github.module.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.module.ai.entity.AiKnowledgeBaseEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 知识库.
 */
@Mapper
public interface AiKnowledgeBaseMapper extends BaseMapper<AiKnowledgeBaseEntity> {
}
