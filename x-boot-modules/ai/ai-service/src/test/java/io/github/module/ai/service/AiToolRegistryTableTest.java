package io.github.module.ai.service;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.context.TenantContext;
import io.github.framework.core.context.TenantContextHolder;
import io.github.framework.crud.entity.BaseEntity;
import io.github.framework.tenant.support.TenantLineSupport;
import io.github.module.ai.entity.AiToolRegistryEntity;
import io.github.module.ai.mapper.AiToolRegistryMapper;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

class AiToolRegistryTableTest {

    @AfterEach
    void clearTenantContext() {
        TenantContextHolder.clear();
    }

    @Test
    void aiToolRegistryEntityAndMapperUseTenantAwareDefaults() {
        assertThat(BaseEntity.class.isAssignableFrom(AiToolRegistryEntity.class)).isTrue();
        assertThat(BaseMapper.class.isAssignableFrom(AiToolRegistryMapper.class)).isTrue();
        assertThat(AiToolRegistryMapper.class.getAnnotation(InterceptorIgnore.class)).isNull();
        assertThat(AiToolRegistryEntity.class.getAnnotation(TableName.class).value())
                .isEqualTo("ai_tool_registry");

        Map.ofEntries(
                entry("toolCode", "tool_code"),
                entry("name", "name"),
                entry("description", "description"),
                entry("toolType", "tool_type"),
                entry("protocol", "protocol"),
                entry("endpointUrl", "endpoint_url"),
                entry("httpMethod", "http_method"),
                entry("requestSchema", "request_schema"),
                entry("responseSchema", "response_schema"),
                entry("configSchema", "config_schema"),
                entry("allowedHosts", "allowed_hosts"),
                entry("sensitiveFields", "sensitive_fields"),
                entry("timeoutMs", "timeout_ms"),
                entry("versionNo", "version_no"),
                entry("status", "status"),
                entry("remark", "remark")
        ).forEach(this::assertField);
    }

    @Test
    void normalTenantAddsTenantLineConditionForToolRegistryTable() {
        TenantContextHolder.setTenantContext(new TenantContext(1001L, "租户A"));
        TenantLineInnerInterceptor interceptor = tenantLineInnerInterceptor();

        Expression expression = interceptor.buildTableExpression(
                new Table("ai_tool_registry"),
                null,
                null
        );

        assertThat(expression).hasToString("tenant_id = 1001");
    }

    @Test
    void privilegedTenantCanBypassToolRegistryTenantLineCondition() {
        TenantContextHolder.setTenantContext(new TenantContext(
                BaseConstant.Tenant.DEFAULT_PRIVILEGED_TENANT_ID,
                "特权租户"
        ));
        TenantLineInnerInterceptor interceptor = tenantLineInnerInterceptor();

        Expression expression = interceptor.buildTableExpression(
                new Table("ai_tool_registry"),
                null,
                null
        );

        assertThat(expression).isNull();
    }

    @Test
    void moduleAndAggregateSqlContainToolRegistryTableDefinition() throws IOException {
        String moduleSql = Files.readString(Path.of("src/main/resources/sql/x_boot_ai.sql"));
        String aggregateSql = Files.readString(Path.of("..", "..", "..", "docs", "x_boot_all.sql"));

        assertToolRegistrySql(moduleSql);
        assertToolRegistrySql(aggregateSql);
    }

    private void assertToolRegistrySql(String sql) {
        assertThat(sql).contains(
                "CREATE TABLE `ai_tool_registry`",
                "`tenant_id` bigint DEFAULT NULL COMMENT '租户ID'",
                "`revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁'",
                "`del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识'",
                "`tool_code` varchar(64)",
                "`name` varchar(100)",
                "`description` varchar(500)",
                "`tool_type` varchar(32)",
                "`protocol` varchar(32)",
                "`endpoint_url` varchar(1000)",
                "`http_method` varchar(16)",
                "`request_schema` longtext",
                "`response_schema` longtext",
                "`config_schema` longtext",
                "`allowed_hosts` varchar(1000)",
                "`sensitive_fields` varchar(500)",
                "`timeout_ms` bigint DEFAULT NULL COMMENT '默认超时时间，单位毫秒'",
                "`version_no` int NOT NULL DEFAULT '1' COMMENT '版本号'",
                "`status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0=禁用 1=启用)'",
                "`remark` varchar(500)",
                "UNIQUE KEY `uk_ai_tool_registry_tenant_code`",
                "KEY `idx_ai_tool_registry_tenant_type_status`",
                "KEY `idx_ai_tool_registry_tenant_protocol_status`",
                "KEY `idx_ai_tool_registry_tenant_updated`"
        );
    }

    private void assertField(String fieldName, String columnName) {
        try {
            TableField tableField = AiToolRegistryEntity.class.getDeclaredField(fieldName)
                    .getAnnotation(TableField.class);
            assertThat(tableField).isNotNull();
            assertThat(tableField.value()).isEqualTo(columnName);
        } catch (NoSuchFieldException e) {
            throw new AssertionError("missing field: " + fieldName, e);
        }
    }

    private TenantLineInnerInterceptor tenantLineInnerInterceptor() {
        TenantLineSupport.XBootLineTenantHandler tenantHandler =
                new TenantLineSupport.XBootLineTenantHandler(
                        BaseConstant.Tenant.DEFAULT_PRIVILEGED_TENANT_ID,
                        Set.of("sys_tenant")
                );

        assertThat(tenantHandler.getTenantIdColumn()).isEqualTo(BaseConstant.CRUD.COLUMN_TENANT_ID);
        assertThat(tenantHandler.ignoreTable("ai_tool_registry"))
                .isEqualTo(BaseConstant.Tenant.DEFAULT_PRIVILEGED_TENANT_ID.equals(TenantContextHolder.getTenantId()));

        return new TenantLineInnerInterceptor(tenantHandler);
    }
}
