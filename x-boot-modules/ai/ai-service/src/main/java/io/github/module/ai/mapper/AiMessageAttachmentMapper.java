package io.github.module.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.module.ai.entity.AiMessageAttachmentEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 消息附件.
 */
@Mapper
public interface AiMessageAttachmentMapper extends BaseMapper<AiMessageAttachmentEntity> {
}
