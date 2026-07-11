package io.github.module.learning.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.module.learning.entity.LearnerAccountEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 学习者账号 Mapper.
 */
@Mapper
public interface LearnerAccountMapper extends BaseMapper<LearnerAccountEntity> {

    /**
     * 按 GitHub 用户ID查询学习者账号，忽略行级租户拦截器.
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("""
            SELECT *
            FROM learner_account
            WHERE del_flag = 0
              AND github_user_id = #{githubUserId}
            LIMIT 1
            """)
    LearnerAccountEntity getByGithubUserId(@Param("githubUserId") String githubUserId);
}
