package io.github.module.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.module.ai.entity.AiKnowledgeDocumentChunkEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 知识库文档切片.
 */
@Mapper
public interface AiKnowledgeDocumentChunkMapper extends BaseMapper<AiKnowledgeDocumentChunkEntity> {
}
