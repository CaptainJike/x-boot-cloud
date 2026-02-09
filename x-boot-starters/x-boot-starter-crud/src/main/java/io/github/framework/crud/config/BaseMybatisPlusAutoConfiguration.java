package io.github.framework.crud.config;

import io.github.framework.core.enums.IdGeneratorStrategyEnum;
import io.github.framework.core.props.BaseProperties;
import io.github.framework.crud.handler.BaseSequenceIdGenerateHandler;
import io.github.framework.crud.handler.BaseSnowflakeIdGenerateHandler;
import io.github.framework.crud.handler.MybatisPlusAutoFillColumnHandler;
import io.github.framework.crud.support.TenantSupport;
import io.github.framework.crud.support.impl.DefaultTenantSupport;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * XBoot Mybatis-Plus 自动配置类
 */
@EnableTransactionManagement(
        proxyTargetClass = true
)
@RequiredArgsConstructor
@AutoConfiguration
@Slf4j
public class BaseMybatisPlusAutoConfiguration {

    private final BaseProperties baseProperties;


    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor(
            TenantSupport tenantSupport
    ) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        /*
        https://baomidou.com/pages/2976a3/#%E5%B1%9E%E6%80%A7
        使用多个功能需要注意顺序关系,建议使用如下顺序

        多租户,动态表名
        分页,乐观锁
        sql性能规范,防止全表更新与删除
         */
        if (Boolean.TRUE.equals(baseProperties.getTenant().getEnabled())) {
            // 配置文件中启用了多租户功能，注入对应支持 bean
            tenantSupport.support(baseProperties, interceptor);
        }

        /*
        分页插件
         */
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        // 设置sql的limit为无限制
        paginationInnerInterceptor.setMaxLimit(-1L);
        interceptor.addInnerInterceptor(paginationInnerInterceptor);

        /*
        乐观锁
         */
        if (Boolean.TRUE.equals(baseProperties.getCrud().getOptimisticLock().getEnabled())) {
            interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        }

        /*
        防止全表更新与删除
         */
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        return interceptor;
    }

    /**
     * 自定义ID生成器 - 雪花ID
     */
    @Bean
    @ConditionalOnMissingBean
    public IdentifierGenerator baseIdentifierGenerator() {
        IdGeneratorStrategyEnum strategy = baseProperties.getCrud().getIdGenerator().getStrategy();

        if (strategy == IdGeneratorStrategyEnum.SNOWFLAKE) {
            return new BaseSnowflakeIdGenerateHandler(baseProperties);
        }

        if (strategy == IdGeneratorStrategyEnum.SEQUENCE) {
            return new BaseSequenceIdGenerateHandler();
        }

        throw new IllegalArgumentException("Value of 'x.crud.idGenerator.strategy' is illegal");
    }

    /**
     * 字段自动填充
     */
    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusAutoFillColumnHandler mybatisPlusAutoFillColumnHandler() {
        return new MybatisPlusAutoFillColumnHandler();
    }

    /**
     * 默认租户支持类
     */
    @Bean
    @ConditionalOnMissingBean
    public TenantSupport defaultTenantSupport() {
        return new DefaultTenantSupport();
    }

}
