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
import io.github.module.ai.entity.AiWorkflowNodeEntity;
import io.github.module.ai.mapper.AiWorkflowNodeMapper;
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

class AiWorkflowNodeTableTest {

    @AfterEach
    void clearTenantContext() {
        TenantContextHolder.clear();
    }

    @Test
    void aiWorkflowNodeEntityAndMapperUseTenantAwareDefaults() {
        assertThat(BaseEntity.class.isAssignableFrom(AiWorkflowNodeEntity.class)).isTrue();
        assertThat(BaseMapper.class.isAssignableFrom(AiWorkflowNodeMapper.class)).isTrue();
        assertThat(AiWorkflowNodeMapper.class.getAnnotation(InterceptorIgnore.class)).isNull();
        assertThat(AiWorkflowNodeEntity.class.getAnnotation(TableName.class).value())
                .isEqualTo("ai_workflow_node");

        Map.ofEntries(
                entry("workflowDefinitionId", "workflow_definition_id"),
                entry("workflowCode", "workflow_code"),
                entry("versionNo", "version_no"),
                entry("nodeKey", "node_key"),
                entry("nodeName", "node_name"),
                entry("nodeType", "node_type"),
                entry("description", "description"),
                entry("nodeConfig", "node_config"),
                entry("inputMapping", "input_mapping"),
                entry("outputMapping", "output_mapping"),
                entry("nextNodeKeys", "next_node_keys"),
                entry("conditionExpression", "condition_expression"),
                entry("errorStrategy", "error_strategy"),
                entry("retryCount", "retry_count"),
                entry("timeoutSeconds", "timeout_seconds"),
                entry("sortOrder", "sort_order"),
                entry("status", "status")
        ).forEach(this::assertField);
    }

    @Test
    void normalTenantAddsTenantLineConditionForWorkflowNodeTable() {
        TenantContextHolder.setTenantContext(new TenantContext(1001L, "租户A"));
        TenantLineInnerInterceptor interceptor = tenantLineInnerInterceptor();

        Expression expression = interceptor.buildTableExpression(
                new Table("ai_workflow_node"),
                null,
                null
        );

        assertThat(expression).hasToString("tenant_id = 1001");
    }

    @Test
    void privilegedTenantCanBypassWorkflowNodeTenantLineCondition() {
        TenantContextHolder.setTenantContext(new TenantContext(
                BaseConstant.Tenant.DEFAULT_PRIVILEGED_TENANT_ID,
                "特权租户"
        ));
        TenantLineInnerInterceptor interceptor = tenantLineInnerInterceptor();

        Expression expression = interceptor.buildTableExpression(
                new Table("ai_workflow_node"),
                null,
                null
        );

        assertThat(expression).isNull();
    }

    @Test
    void moduleAndAggregateSqlContainWorkflowNodeTableDefinition() throws IOException {
        String moduleSql = Files.readString(Path.of("src/main/resources/sql/x_boot_ai.sql"));
        String aggregateSql = Files.readString(Path.of("..", "..", "..", "docs", "x_boot_all.sql"));

        assertWorkflowNodeSql(moduleSql);
        assertWorkflowNodeSql(aggregateSql);
    }

    private void assertWorkflowNodeSql(String sql) {
        assertThat(sql).contains(
                "CREATE TABLE `ai_workflow_node`",
                "`tenant_id` bigint DEFAULT NULL COMMENT '租户ID'",
                "`revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁'",
                "`del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识'",
                "`workflow_definition_id` bigint NOT NULL COMMENT '工作流定义ID'",
                "`workflow_code` varchar(64)",
                "`version_no` int NOT NULL DEFAULT '1' COMMENT '版本号'",
                "`node_key` varchar(64)",
                "`node_name` varchar(100)",
                "`node_type` varchar(32)",
                "`description` varchar(500)",
                "`node_config` longtext",
                "`input_mapping` longtext",
                "`output_mapping` longtext",
                "`next_node_keys` varchar(500)",
                "`condition_expression` varchar(1000)",
                "`error_strategy` varchar(32)",
                "`retry_count` int NOT NULL DEFAULT '0' COMMENT '重试次数'",
                "`timeout_seconds` bigint DEFAULT NULL COMMENT '超时时间，单位秒'",
                "`sort_order` int NOT NULL DEFAULT '0' COMMENT '排序'",
                "`status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0=禁用 1=启用)'",
                "UNIQUE KEY `uk_ai_workflow_node_tenant_workflow_node`",
                "KEY `idx_ai_workflow_node_tenant_workflow_sort`",
                "KEY `idx_ai_workflow_node_tenant_type`",
                "KEY `idx_ai_workflow_node_tenant_workflow_type`"
        );
    }

    private void assertField(String fieldName, String columnName) {
        try {
            TableField tableField = AiWorkflowNodeEntity.class.getDeclaredField(fieldName)
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
        assertThat(tenantHandler.ignoreTable("ai_workflow_node"))
                .isEqualTo(BaseConstant.Tenant.DEFAULT_PRIVILEGED_TENANT_ID.equals(TenantContextHolder.getTenantId()));

        return new TenantLineInnerInterceptor(tenantHandler);
    }
}
