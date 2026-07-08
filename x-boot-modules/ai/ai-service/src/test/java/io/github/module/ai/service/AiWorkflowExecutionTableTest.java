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
import io.github.module.ai.entity.AiWorkflowExecutionEntity;
import io.github.module.ai.mapper.AiWorkflowExecutionMapper;
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

class AiWorkflowExecutionTableTest {

    @AfterEach
    void clearTenantContext() {
        TenantContextHolder.clear();
    }

    @Test
    void aiWorkflowExecutionEntityAndMapperUseTenantAwareDefaults() {
        assertThat(BaseEntity.class.isAssignableFrom(AiWorkflowExecutionEntity.class)).isTrue();
        assertThat(BaseMapper.class.isAssignableFrom(AiWorkflowExecutionMapper.class)).isTrue();
        assertThat(AiWorkflowExecutionMapper.class.getAnnotation(InterceptorIgnore.class)).isNull();
        assertThat(AiWorkflowExecutionEntity.class.getAnnotation(TableName.class).value())
                .isEqualTo("ai_workflow_execution");

        Map.ofEntries(
                entry("executionId", "execution_id"),
                entry("workflowDefinitionId", "workflow_definition_id"),
                entry("workflowCode", "workflow_code"),
                entry("workflowName", "workflow_name"),
                entry("versionNo", "version_no"),
                entry("agentId", "agent_id"),
                entry("userId", "user_id"),
                entry("triggerSource", "trigger_source"),
                entry("triggerId", "trigger_id"),
                entry("inputSummary", "input_summary"),
                entry("outputSummary", "output_summary"),
                entry("status", "status"),
                entry("currentNodeKey", "current_node_key"),
                entry("failedNodeKey", "failed_node_key"),
                entry("durationMs", "duration_ms"),
                entry("errorCode", "error_code"),
                entry("errorMessage", "error_message"),
                entry("traceId", "trace_id"),
                entry("startedAt", "started_at"),
                entry("finishedAt", "finished_at")
        ).forEach(this::assertField);
    }

    @Test
    void normalTenantAddsTenantLineConditionForWorkflowExecutionTable() {
        TenantContextHolder.setTenantContext(new TenantContext(1001L, "租户A"));
        TenantLineInnerInterceptor interceptor = tenantLineInnerInterceptor();

        Expression expression = interceptor.buildTableExpression(
                new Table("ai_workflow_execution"),
                null,
                null
        );

        assertThat(expression).hasToString("tenant_id = 1001");
    }

    @Test
    void privilegedTenantCanBypassWorkflowExecutionTenantLineCondition() {
        TenantContextHolder.setTenantContext(new TenantContext(
                BaseConstant.Tenant.DEFAULT_PRIVILEGED_TENANT_ID,
                "特权租户"
        ));
        TenantLineInnerInterceptor interceptor = tenantLineInnerInterceptor();

        Expression expression = interceptor.buildTableExpression(
                new Table("ai_workflow_execution"),
                null,
                null
        );

        assertThat(expression).isNull();
    }

    @Test
    void moduleAndAggregateSqlContainWorkflowExecutionTableDefinition() throws IOException {
        String moduleSql = Files.readString(Path.of("src/main/resources/sql/x_boot_ai.sql"));
        String aggregateSql = Files.readString(Path.of("..", "..", "..", "docs", "x_boot_all.sql"));

        assertWorkflowExecutionSql(moduleSql);
        assertWorkflowExecutionSql(aggregateSql);
    }

    private void assertWorkflowExecutionSql(String sql) {
        assertThat(sql).contains(
                "CREATE TABLE `ai_workflow_execution`",
                "`tenant_id` bigint DEFAULT NULL COMMENT '租户ID'",
                "`revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁'",
                "`del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识'",
                "`execution_id` varchar(64)",
                "`workflow_definition_id` bigint NOT NULL COMMENT '工作流定义ID'",
                "`workflow_code` varchar(64)",
                "`workflow_name` varchar(100)",
                "`version_no` int NOT NULL DEFAULT '1' COMMENT '版本号'",
                "`agent_id` bigint DEFAULT NULL COMMENT '关联Agent ID'",
                "`user_id` bigint DEFAULT NULL COMMENT '后台用户ID'",
                "`trigger_source` varchar(32)",
                "`trigger_id` varchar(64)",
                "`input_summary` longtext",
                "`output_summary` longtext",
                "`status` tinyint NOT NULL DEFAULT '2' COMMENT '状态(0=失败 1=成功 2=执行中 3=取消)'",
                "`current_node_key` varchar(64)",
                "`failed_node_key` varchar(64)",
                "`duration_ms` bigint DEFAULT NULL COMMENT '耗时，单位毫秒'",
                "`error_code` varchar(64)",
                "`error_message` varchar(1000)",
                "`trace_id` varchar(128)",
                "`started_at` datetime NOT NULL COMMENT '执行开始时刻'",
                "`finished_at` datetime DEFAULT NULL COMMENT '执行结束时刻'",
                "UNIQUE KEY `uk_ai_workflow_execution_tenant_execution`",
                "KEY `idx_ai_workflow_execution_tenant_workflow_started`",
                "KEY `idx_ai_workflow_execution_tenant_user_started`",
                "KEY `idx_ai_workflow_execution_tenant_status_started`",
                "KEY `idx_ai_workflow_execution_tenant_trigger`",
                "KEY `idx_ai_workflow_execution_tenant_failed_node`"
        );
    }

    private void assertField(String fieldName, String columnName) {
        try {
            TableField tableField = AiWorkflowExecutionEntity.class.getDeclaredField(fieldName)
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
        assertThat(tenantHandler.ignoreTable("ai_workflow_execution"))
                .isEqualTo(BaseConstant.Tenant.DEFAULT_PRIVILEGED_TENANT_ID.equals(TenantContextHolder.getTenantId()));

        return new TenantLineInnerInterceptor(tenantHandler);
    }
}
