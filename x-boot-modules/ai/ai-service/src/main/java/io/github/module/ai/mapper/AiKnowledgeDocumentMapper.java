package io.github.module.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.module.ai.entity.AiKnowledgeDocumentEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 知识库文档.
 */
@Mapper
public interface AiKnowledgeDocumentMapper extends BaseMapper<AiKnowledgeDocumentEntity> {
}
