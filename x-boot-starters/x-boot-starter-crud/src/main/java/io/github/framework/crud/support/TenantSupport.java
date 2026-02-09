package io.github.framework.crud.support;

import io.github.framework.core.props.BaseProperties;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;

/**
 * Mybatis-Plus 多租户支持接口
 */
public interface TenantSupport {

    /**
     * 不同的多租户隔离级别分别实现本方法，按需添加 SQL 拦截器
     * @param baseProperties 配置属性
     * @param interceptor Mybatis-Plus 拦截器
     */
    void support(BaseProperties baseProperties, MybatisPlusInterceptor interceptor);

}
