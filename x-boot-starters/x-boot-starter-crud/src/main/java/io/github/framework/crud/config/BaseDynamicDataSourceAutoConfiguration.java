package io.github.framework.crud.config;

import io.github.framework.crud.dynamicdatasource.DataSourceDefinitionProvider;
import io.github.framework.crud.dynamicdatasource.BaseDynamicDataSourceRegistry;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.hikaricp.HikariDataSourceCreator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/**
 * 动态数据源自动配置类
 */
@ConditionalOnClass(name = "com.baomidou.dynamic.datasource.DynamicRoutingDataSource")
public class BaseDynamicDataSourceAutoConfiguration {

    @ConditionalOnBean(value = DynamicRoutingDataSource.class)
    @Bean
    public BaseDynamicDataSourceRegistry baseDynamicDataSourceRegistry(
            DynamicRoutingDataSource dynamicRoutingDataSource,
            HikariDataSourceCreator dataSourceCreator,
            ObjectProvider<DataSourceDefinitionProvider> dataSourceDefinitionProviders
    ) {
        return new BaseDynamicDataSourceRegistry(
                dynamicRoutingDataSource, dataSourceCreator, dataSourceDefinitionProviders);
    }
}
