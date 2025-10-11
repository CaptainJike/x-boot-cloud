package io.github.framework.tenant.config;

import io.github.framework.crud.dynamicdatasource.BaseDynamicDataSourceRegistry;
import io.github.framework.tenant.tenantdatasource.GlobalTenantDataSourceAdvisor;
import io.github.framework.tenant.tenantdatasource.GlobalTenantDataSourceInterceptor;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;

import java.util.List;

/**
 * 基于全局 AOP 的数据源级多租户配置类
 * <a href="https://blog.csdn.net/w57685321/article/details/106823660">参考文章</a>
 */
public class GlobalTenantDataSourceConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DynamicRoutingDataSource dynamicRoutingDataSource(DynamicDataSourceProvider ymlDynamicDataSourceProvider) {
        return new DynamicRoutingDataSource(List.of(ymlDynamicDataSourceProvider));
    }


    @Bean
    @ConditionalOnMissingBean
    @Role(value = BeanDefinition.ROLE_INFRASTRUCTURE)
    public GlobalTenantDataSourceInterceptor interceptor(
            BaseDynamicDataSourceRegistry dataSourceRegistry
    ) {
        return new GlobalTenantDataSourceInterceptor(dataSourceRegistry);
    }


    @Bean
    @ConditionalOnMissingBean
    @Role(value = BeanDefinition.ROLE_INFRASTRUCTURE)
    public GlobalTenantDataSourceAdvisor tenantDataSourceGlobalAdvisor(
            GlobalTenantDataSourceInterceptor interceptor,
            DynamicDataSourceProperties properties
    ) {
        GlobalTenantDataSourceAdvisor advisor = new GlobalTenantDataSourceAdvisor(interceptor);
        // 数值越高，优先度越低
        advisor.setOrder(properties.getAop().getOrder() + 1);
        return advisor;
    }

}
