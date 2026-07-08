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
import io.github.module.ai.entity.AiWorkflowDefinitionEntity;
import io.github.module.ai.mapper.AiWorkflowDefinitionMapper;
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

class AiWorkflowDefinitionTableTest {

    @AfterEach
    void clearTenantContext() {
        TenantContextHolder.clear();
    }

    @Test
    void aiWorkflowDefinitionEntityAndMapperUseTenantAwareDefaults() {
        assertThat(BaseEntity.class.isAssignableFrom(AiWorkflowDefinitionEntity.class)).isTrue();
        assertThat(BaseMapper.class.isAssignableFrom(AiWorkflowDefinitionMapper.class)).isTrue();
        assertThat(AiWorkflowDefinitionMapper.class.getAnnotation(InterceptorIgnore.class)).isNull();
        assertThat(AiWorkflowDefinitionEntity.class.getAnnotation(TableName.class).value())
                .isEqualTo("ai_workflow_definition");

        Map.ofEntries(
                entry("workflowCode", "workflow_code"),
                entry("name", "name"),
                entry("description", "description"),
                entry("agentId", "agent_id"),
                entry("versionNo", "version_no"),
                entry("entryNodeKey", "entry_node_key"),
                entry("definitionSnapshot", "definition_snapshot"),
                entry("publishedSnapshot", "published_snapshot"),
                entry("status", "status"),
                entry("publishStatus", "publish_status"),
                entry("publishedAt", "published_at"),
                entry("lastExecutedAt", "last_executed_at"),
                entry("executionCount", "execution_count")
        ).forEach(this::assertField);
    }

    @Test
    void normalTenantAddsTenantLineConditionForWorkflowDefinitionTable() {
        TenantContextHolder.setTenantContext(new TenantContext(1001L, "租户A"));
        TenantLineInnerInterceptor interceptor = tenantLineInnerInterceptor();

        Expression expression = interceptor.buildTableExpression(
                new Table("ai_workflow_definition"),
                null,
                null
        );

        assertThat(expression).hasToString("tenant_id = 1001");
    }

    @Test
    void privilegedTenantCanBypassWorkflowDefinitionTenantLineCondition() {
        TenantContextHolder.setTenantContext(new TenantContext(
                BaseConstant.Tenant.DEFAULT_PRIVILEGED_TENANT_ID,
                "特权租户"
        ));
        TenantLineInnerInterceptor interceptor = tenantLineInnerInterceptor();

        Expression expression = interceptor.buildTableExpression(
                new Table("ai_workflow_definition"),
                null,
                null
        );

        assertThat(expression).isNull();
    }

    @Test
    void moduleAndAggregateSqlContainWorkflowDefinitionTableDefinition() throws IOException {
        String moduleSql = Files.readString(Path.of("src/main/resources/sql/x_boot_ai.sql"));
        String aggregateSql = Files.readString(Path.of("..", "..", "..", "docs", "x_boot_all.sql"));

        assertWorkflowDefinitionSql(moduleSql);
        assertWorkflowDefinitionSql(aggregateSql);
    }

    private void assertWorkflowDefinitionSql(String sql) {
        assertThat(sql).contains(
                "CREATE TABLE `ai_workflow_definition`",
                "`tenant_id` bigint DEFAULT NULL COMMENT '租户ID'",
                "`revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁'",
                "`del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识'",
                "`workflow_code` varchar(64)",
                "`name` varchar(100)",
                "`description` varchar(500)",
                "`agent_id` bigint DEFAULT NULL COMMENT '关联Agent ID'",
                "`version_no` int NOT NULL DEFAULT '1' COMMENT '版本号'",
                "`entry_node_key` varchar(64)",
                "`definition_snapshot` longtext",
                "`published_snapshot` longtext",
                "`status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0=禁用 1=启用)'",
                "`publish_status` tinyint NOT NULL DEFAULT '0' COMMENT '发布状态(0=草稿 1=已发布)'",
                "`published_at` datetime DEFAULT NULL COMMENT '发布时刻'",
                "`last_executed_at` datetime DEFAULT NULL COMMENT '最近执行时刻'",
                "`execution_count` int NOT NULL DEFAULT '0' COMMENT '执行次数'",
                "UNIQUE KEY `uk_ai_workflow_definition_tenant_code_version`",
                "KEY `idx_ai_workflow_definition_tenant_status_updated`",
                "KEY `idx_ai_workflow_definition_tenant_publish_status`",
                "KEY `idx_ai_workflow_definition_tenant_agent`",
                "KEY `idx_ai_workflow_definition_tenant_last_executed`"
        );
    }

    private void assertField(String fieldName, String columnName) {
        try {
            TableField tableField = AiWorkflowDefinitionEntity.class.getDeclaredField(fieldName)
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
        assertThat(tenantHandler.ignoreTable("ai_workflow_definition"))
                .isEqualTo(BaseConstant.Tenant.DEFAULT_PRIVILEGED_TENANT_ID.equals(TenantContextHolder.getTenantId()));

        return new TenantLineInnerInterceptor(tenantHandler);
    }
}
