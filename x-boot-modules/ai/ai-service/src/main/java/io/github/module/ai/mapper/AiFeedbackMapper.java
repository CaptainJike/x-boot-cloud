package io.github.module.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.module.ai.entity.AiFeedbackEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 反馈.
 */
@Mapper
public interface AiFeedbackMapper extends BaseMapper<AiFeedbackEntity> {
}
