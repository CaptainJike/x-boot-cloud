package io.github.module.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.module.ai.entity.AiKnowledgeRetrievalLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 知识库检索日志.
 */
@Mapper
public interface AiKnowledgeRetrievalLogMapper extends BaseMapper<AiKnowledgeRetrievalLogEntity> {
}
