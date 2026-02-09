package io.github.framework.tenant.annotation;

import io.github.framework.tenant.config.GlobalTenantDataSourceConfiguration;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * 启用基于全局 AOP 的数据源级多租户
 */
@Import(value = GlobalTenantDataSourceConfiguration.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableGlobalTenantDataSource {

}
