package io.github.module.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.module.ai.entity.AiWorkflowExecutionEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 工作流执行记录.
 */
@Mapper
public interface AiWorkflowExecutionMapper extends BaseMapper<AiWorkflowExecutionEntity> {
}
