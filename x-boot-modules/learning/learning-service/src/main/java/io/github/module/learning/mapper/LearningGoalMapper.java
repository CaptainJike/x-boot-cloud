package io.github.module.learning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.module.learning.entity.LearningGoalEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LearningGoalMapper extends BaseMapper<LearningGoalEntity> {
}
