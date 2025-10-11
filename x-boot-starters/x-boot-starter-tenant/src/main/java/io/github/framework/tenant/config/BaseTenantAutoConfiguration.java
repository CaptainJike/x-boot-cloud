package io.github.framework.tenant.config;

import io.github.framework.core.enums.TenantIsolateLevelEnum;
import io.github.framework.core.props.BaseProperties;
import io.github.framework.crud.support.TenantSupport;
import io.github.framework.crud.support.impl.DefaultTenantSupport;
import io.github.framework.tenant.support.TenantDataSourceSupport;
import io.github.framework.tenant.support.TenantLineSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * XBoot 多租户自动配置类
 * 当启用多租户功能，且多租户隔离级别配置正确，则向 IoC 容器注入对应处理 Bean
 */
@RequiredArgsConstructor
@AutoConfiguration
public class BaseTenantAutoConfiguration {

    private final BaseProperties baseProperties;


    @Bean
    @Primary
    public TenantSupport tenantSupport() {
        if (!Boolean.TRUE.equals(baseProperties.getTenant().getEnabled())) {
            // 引入了 starter，但未启用多租户
            return new DefaultTenantSupport();
        }

        TenantIsolateLevelEnum isolateLevel = baseProperties.getTenant().getIsolateLevel();
        if (isolateLevel == TenantIsolateLevelEnum.LINE) {
            // 行级
            return new TenantLineSupport();
        } else if (isolateLevel == TenantIsolateLevelEnum.DATASOURCE) {
            // 数据源级
            return new TenantDataSourceSupport();
        }
        throw new IllegalArgumentException("启用多租户功能后，请正确配置对应的多租户隔离级别(x.tenant.isolate-level)");
    }
}
