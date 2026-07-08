package io.github.module.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.module.ai.entity.AiWorkflowDefinitionEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 工作流定义.
 */
@Mapper
public interface AiWorkflowDefinitionMapper extends BaseMapper<AiWorkflowDefinitionEntity> {
}
