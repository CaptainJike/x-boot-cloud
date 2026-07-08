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
import io.github.module.ai.entity.AiAgentEntity;
import io.github.module.ai.mapper.AiAgentMapper;
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

class AiAgentTableTest {

    @AfterEach
    void clearTenantContext() {
        TenantContextHolder.clear();
    }

    @Test
    void aiAgentEntityAndMapperUseTenantAwareDefaults() {
        assertThat(BaseEntity.class.isAssignableFrom(AiAgentEntity.class)).isTrue();
        assertThat(BaseMapper.class.isAssignableFrom(AiAgentMapper.class)).isTrue();
        assertThat(AiAgentMapper.class.getAnnotation(InterceptorIgnore.class)).isNull();
        assertThat(AiAgentEntity.class.getAnnotation(TableName.class).value()).isEqualTo("ai_agent");

        Map.ofEntries(
                entry("agentCode", "agent_code"),
                entry("name", "name"),
                entry("description", "description"),
                entry("avatar", "avatar"),
                entry("systemPrompt", "system_prompt"),
                entry("modelConfigId", "model_config_id"),
                entry("modelConfigCode", "model_config_code"),
                entry("providerType", "provider_type"),
                entry("modelName", "model_name"),
                entry("knowledgeBaseIds", "knowledge_base_ids"),
                entry("temperature", "temperature"),
                entry("maxTokens", "max_tokens"),
                entry("executionConfig", "execution_config"),
                entry("status", "status"),
                entry("publishStatus", "publish_status"),
                entry("publishedAt", "published_at"),
                entry("lastExecutedAt", "last_executed_at"),
                entry("executionCount", "execution_count")
        ).forEach(this::assertField);
    }

    @Test
    void normalTenantAddsTenantLineConditionForAgentTable() {
        TenantContextHolder.setTenantContext(new TenantContext(1001L, "租户A"));
        TenantLineInnerInterceptor interceptor = tenantLineInnerInterceptor();

        Expression expression = interceptor.buildTableExpression(
                new Table("ai_agent"),
                null,
                null
        );

        assertThat(expression).hasToString("tenant_id = 1001");
    }

    @Test
    void privilegedTenantCanBypassAgentTenantLineCondition() {
        TenantContextHolder.setTenantContext(new TenantContext(
                BaseConstant.Tenant.DEFAULT_PRIVILEGED_TENANT_ID,
                "特权租户"
        ));
        TenantLineInnerInterceptor interceptor = tenantLineInnerInterceptor();

        Expression expression = interceptor.buildTableExpression(
                new Table("ai_agent"),
                null,
                null
        );

        assertThat(expression).isNull();
    }

    @Test
    void moduleAndAggregateSqlContainAgentTableDefinition() throws IOException {
        String moduleSql = Files.readString(Path.of("src/main/resources/sql/x_boot_ai.sql"));
        String aggregateSql = Files.readString(Path.of("..", "..", "..", "docs", "x_boot_all.sql"));

        assertAgentSql(moduleSql);
        assertAgentSql(aggregateSql);
    }

    private void assertAgentSql(String sql) {
        assertThat(sql).contains(
                "CREATE TABLE `ai_agent`",
                "`tenant_id` bigint DEFAULT NULL COMMENT '租户ID'",
                "`revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁'",
                "`del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识'",
                "`agent_code` varchar(64)",
                "`name` varchar(100)",
                "`description` varchar(500)",
                "`avatar` varchar(500)",
                "`system_prompt` longtext",
                "`model_config_id` bigint DEFAULT NULL COMMENT '默认模型配置ID'",
                "`model_config_code` varchar(64)",
                "`provider_type` varchar(50)",
                "`model_name` varchar(100)",
                "`knowledge_base_ids` varchar(500)",
                "`temperature` decimal(4",
                "`max_tokens` int DEFAULT NULL COMMENT '最大回复Token数'",
                "`execution_config` longtext",
                "`status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0=禁用 1=启用)'",
                "`publish_status` tinyint NOT NULL DEFAULT '0' COMMENT '发布状态(0=草稿 1=已发布)'",
                "`published_at` datetime DEFAULT NULL COMMENT '发布时刻'",
                "`last_executed_at` datetime DEFAULT NULL COMMENT '最近执行时刻'",
                "`execution_count` int NOT NULL DEFAULT '0' COMMENT '执行次数'",
                "UNIQUE KEY `uk_ai_agent_tenant_code`",
                "KEY `idx_ai_agent_tenant_status_updated`",
                "KEY `idx_ai_agent_tenant_publish_status`",
                "KEY `idx_ai_agent_tenant_model`",
                "KEY `idx_ai_agent_tenant_last_executed`"
        );
    }

    private void assertField(String fieldName, String columnName) {
        try {
            TableField tableField = AiAgentEntity.class.getDeclaredField(fieldName).getAnnotation(TableField.class);
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
        assertThat(tenantHandler.ignoreTable("ai_agent"))
                .isEqualTo(BaseConstant.Tenant.DEFAULT_PRIVILEGED_TENANT_ID.equals(TenantContextHolder.getTenantId()));

        return new TenantLineInnerInterceptor(tenantHandler);
    }
}
