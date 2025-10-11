package io.github.framework.crud.support.impl;

import io.github.framework.core.props.BaseProperties;
import io.github.framework.crud.support.TenantSupport;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;

/**
 * 多租户支持-默认处理
 */
public class DefaultTenantSupport implements TenantSupport {

    @Override
    public void support(BaseProperties baseProperties, MybatisPlusInterceptor interceptor) {
        System.err.println("\n[多租户支持] >> 您启用了多租户，但未引入 x-boot-starter-tenant，无法对 SQL 进行拦截处理\n");
    }
}
