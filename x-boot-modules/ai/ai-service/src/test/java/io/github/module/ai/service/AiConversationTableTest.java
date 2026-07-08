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
import io.github.module.ai.entity.AiConversationEntity;
import io.github.module.ai.mapper.AiConversationMapper;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AiConversationTableTest {

    @AfterEach
    void clearTenantContext() {
        TenantContextHolder.clear();
    }

    @Test
    void aiConversationEntityAndMapperUseTenantAwareDefaults() {
        assertThat(BaseEntity.class.isAssignableFrom(AiConversationEntity.class)).isTrue();
        assertThat(BaseMapper.class.isAssignableFrom(AiConversationMapper.class)).isTrue();
        assertThat(AiConversationMapper.class.getAnnotation(InterceptorIgnore.class)).isNull();
        assertThat(AiConversationEntity.class.getAnnotation(TableName.class).value()).isEqualTo("ai_conversation");
        assertField("conversationId", "conversation_id");
        assertField("userId", "user_id");
        assertField("title", "title");
        assertField("modelConfigId", "model_config_id");
        assertField("modelConfigCode", "model_config_code");
        assertField("providerType", "provider_type");
        assertField("modelName", "model_name");
        assertField("status", "status");
        assertField("messageCount", "message_count");
        assertField("lastMessageAt", "last_message_at");
        assertField("lastMessagePreview", "last_message_preview");
    }

    @Test
    void normalTenantAddsTenantLineConditionForConversationTable() {
        TenantContextHolder.setTenantContext(new TenantContext(1001L, "租户A"));
        TenantLineInnerInterceptor interceptor = tenantLineInnerInterceptor();

        Expression expression = interceptor.buildTableExpression(
                new Table("ai_conversation"),
                null,
                null
        );

        assertThat(expression).hasToString("tenant_id = 1001");
    }

    @Test
    void privilegedTenantCanBypassConversationTenantLineCondition() {
        TenantContextHolder.setTenantContext(new TenantContext(
                BaseConstant.Tenant.DEFAULT_PRIVILEGED_TENANT_ID,
                "特权租户"
        ));
        TenantLineInnerInterceptor interceptor = tenantLineInnerInterceptor();

        Expression expression = interceptor.buildTableExpression(
                new Table("ai_conversation"),
                null,
                null
        );

        assertThat(expression).isNull();
    }

    @Test
    void moduleAndAggregateSqlContainConversationTableDefinition() throws IOException {
        String moduleSql = Files.readString(Path.of("src/main/resources/sql/x_boot_ai.sql"));
        String aggregateSql = Files.readString(Path.of("..", "..", "..", "docs", "x_boot_all.sql"));

        assertConversationSql(moduleSql);
        assertConversationSql(aggregateSql);
    }

    private void assertConversationSql(String sql) {
        assertThat(sql).contains(
                "CREATE TABLE `ai_conversation`",
                "`tenant_id` bigint DEFAULT NULL COMMENT '租户ID'",
                "`revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁'",
                "`del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识'",
                "`conversation_id` varchar(64)",
                "`user_id` bigint NOT NULL COMMENT '后台用户ID'",
                "`title` varchar(200)",
                "`model_config_id` bigint DEFAULT NULL COMMENT '模型配置ID'",
                "`model_config_code` varchar(64)",
                "`provider_type` varchar(50)",
                "`model_name` varchar(100)",
                "`status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0=归档 1=活跃)'",
                "`message_count` int NOT NULL DEFAULT '0' COMMENT '消息数量'",
                "`last_message_at` datetime DEFAULT NULL COMMENT '最近消息时刻'",
                "`last_message_preview` varchar(500)",
                "UNIQUE KEY `uk_ai_conversation_tenant_conversation`",
                "KEY `idx_ai_conversation_tenant_user_status_updated`",
                "KEY `idx_ai_conversation_tenant_model`",
                "KEY `idx_ai_conversation_tenant_last_message`"
        );
    }

    private void assertField(String fieldName, String columnName) {
        try {
            TableField tableField = AiConversationEntity.class.getDeclaredField(fieldName)
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
        assertThat(tenantHandler.ignoreTable("ai_conversation"))
                .isEqualTo(BaseConstant.Tenant.DEFAULT_PRIVILEGED_TENANT_ID.equals(TenantContextHolder.getTenantId()));

        return new TenantLineInnerInterceptor(tenantHandler);
    }
}
