package io.github.framework.tenant.support;

import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.context.TenantContextHolder;
import io.github.framework.core.props.BaseProperties;
import io.github.framework.crud.support.TenantSupport;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;

import java.util.Collection;
import java.util.Objects;

/**
 * 多租户支持-行级
 */
@Slf4j
public class TenantLineSupport implements TenantSupport {

    @Override
    public void support(BaseProperties baseProperties, MybatisPlusInterceptor interceptor) {
        Collection<String> ignoredTables = baseProperties.getTenant().getIgnoredTables();

        // 添加行级租户内联拦截器
        interceptor.addInnerInterceptor(
                new TenantLineInnerInterceptor(new XBootLineTenantHandler(
                        baseProperties.getTenant().getPrivilegedTenantId(), ignoredTables)
                )
        );

        log.info("\n\n[多租户支持] >> 隔离级别: 行级");

        System.err.println("以下数据表不参与租户隔离: " + ignoredTables);
    }


    /**
     * 行级租户mybatis-plus拦截器实现类
     */
    @AllArgsConstructor
    public static class XBootLineTenantHandler implements TenantLineHandler {

        /**
         * 特权租户ID
         */
        private final Long privilegedTenantId;

        /**
         * 忽略租户隔离的表
         */
        private final Collection<String> ignoredTables;


        @Override
        public Expression getTenantId() {
            Long currentTenantId = TenantContextHolder.getTenantId();
            if (currentTenantId == null) {
                return null;
            }

            return new LongValue(currentTenantId);
        }

        @Override
        public String getTenantIdColumn() {
            return BaseConstant.CRUD.COLUMN_TENANT_ID;
        }

        @Override
        public boolean ignoreTable(String tableName) {
            Long currentTenantId = TenantContextHolder.getTenantId();

            if (Objects.nonNull(privilegedTenantId)
                    && Objects.nonNull(currentTenantId)
                    && privilegedTenantId.equals(currentTenantId)) {
                return true;
            }

            return ignoredTables.contains(tableName);
        }

    }

}
