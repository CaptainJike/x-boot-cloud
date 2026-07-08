package io.github.module.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.module.ai.entity.AiWorkflowNodeEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 工作流节点.
 */
@Mapper
public interface AiWorkflowNodeMapper extends BaseMapper<AiWorkflowNodeEntity> {
}
