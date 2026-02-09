package io.github.framework.web.config;

import io.github.framework.core.props.BaseProperties;
import io.github.framework.web.xss.XssFilter;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * 反 XSS 注入自动配置类
 */
@AutoConfiguration
public class XssAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnExpression(value = "${x.security.xss.enabled:true}")
    public FilterRegistrationBean<XssFilter> xssFilterRegistration(BaseProperties baseProperties) {
        FilterRegistrationBean<XssFilter> registration = new FilterRegistrationBean<>();
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        registration.setFilter(new XssFilter(baseProperties.getSecurity().getXss().getExcludedRoutes()));
        registration.addUrlPatterns("/*");
        registration.setName("xssFilter");
        registration.setOrder(Ordered.LOWEST_PRECEDENCE);
        return registration;
    }
}
