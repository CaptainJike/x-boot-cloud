package io.github.framework.core.config;

import io.github.framework.core.props.BaseProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * properties 解析自动配置类
 */
@EnableConfigurationProperties(value = {BaseProperties.class})
@AutoConfiguration
public class BasePropertiesAutoConfiguration {

}
