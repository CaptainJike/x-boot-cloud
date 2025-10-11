package io.github.framework.satoken.config;

import io.github.framework.core.props.BaseProperties;
import io.github.framework.satoken.dao.SaTokenLocalCacheDao;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * SA-Token 本地缓存自动配置类
 */
@ConditionalOnExpression(value = "${x.security.saTokenLocalCache.enabled:false} || ${x.security.sa-token-local-cache.enabled:false}")
@AutoConfiguration
public class SaTokenLocalCacheAutoConfiguration {

    @ConditionalOnMissingBean(value = SaTokenLocalCacheDao.class)
    @Primary
    @Bean
    public SaTokenLocalCacheDao saTokenLocalCacheDao(BaseProperties baseProperties) {
        BaseProperties.Security.SaTokenLocalCache props = baseProperties.getSecurity().getSaTokenLocalCache();
        return new SaTokenLocalCacheDao(props.getDuration(), props.getCapacity());
    }
}
