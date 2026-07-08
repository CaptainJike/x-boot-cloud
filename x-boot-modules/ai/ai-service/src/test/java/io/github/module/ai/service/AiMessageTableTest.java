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
import io.github.module.ai.entity.AiMessageEntity;
import io.github.module.ai.mapper.AiMessageMapper;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AiMessageTableTest {

    @AfterEach
    void clearTenantContext() {
        TenantContextHolder.clear();
    }

    @Test
    void aiMessageEntityAndMapperUseTenantAwareDefaults() {
        assertThat(BaseEntity.class.isAssignableFrom(AiMessageEntity.class)).isTrue();
        assertThat(BaseMapper.class.isAssignableFrom(AiMessageMapper.class)).isTrue();
        assertThat(AiMessageMapper.class.getAnnotation(InterceptorIgnore.class)).isNull();
        assertThat(AiMessageEntity.class.getAnnotation(TableName.class).value()).isEqualTo("ai_message");
        assertField("messageId", "message_id");
        assertField("conversationId", "conversation_id");
        assertField("parentMessageId", "parent_message_id");
        assertField("role", "role");
        assertField("content", "content");
        assertField("contentType", "content_type");
        assertField("modelConfigId", "model_config_id");
        assertField("modelConfigCode", "model_config_code");
        assertField("providerType", "provider_type");
        assertField("modelName", "model_name");
        assertField("status", "status");
        assertField("sequenceNo", "sequence_no");
        assertField("promptTokens", "prompt_tokens");
        assertField("completionTokens", "completion_tokens");
        assertField("totalTokens", "total_tokens");
        assertField("finishReason", "finish_reason");
        assertField("errorCode", "error_code");
        assertField("errorMessage", "error_message");
        assertField("sentAt", "sent_at");
    }

    @Test
    void normalTenantAddsTenantLineConditionForMessageTable() {
        TenantContextHolder.setTenantContext(new TenantContext(1001L, "租户A"));
        TenantLineInnerInterceptor interceptor = tenantLineInnerInterceptor();

        Expression expression = interceptor.buildTableExpression(
                new Table("ai_message"),
                null,
                null
        );

        assertThat(expression).hasToString("tenant_id = 1001");
    }

    @Test
    void privilegedTenantCanBypassMessageTenantLineCondition() {
        TenantContextHolder.setTenantContext(new TenantContext(
                BaseConstant.Tenant.DEFAULT_PRIVILEGED_TENANT_ID,
                "特权租户"
        ));
        TenantLineInnerInterceptor interceptor = tenantLineInnerInterceptor();

        Expression expression = interceptor.buildTableExpression(
                new Table("ai_message"),
                null,
                null
        );

        assertThat(expression).isNull();
    }

    @Test
    void moduleAndAggregateSqlContainMessageTableDefinition() throws IOException {
        String moduleSql = Files.readString(Path.of("src/main/resources/sql/x_boot_ai.sql"));
        String aggregateSql = Files.readString(Path.of("..", "..", "..", "docs", "x_boot_all.sql"));

        assertMessageSql(moduleSql);
        assertMessageSql(aggregateSql);
    }

    private void assertMessageSql(String sql) {
        assertThat(sql).contains(
                "CREATE TABLE `ai_message`",
                "`tenant_id` bigint DEFAULT NULL COMMENT '租户ID'",
                "`revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁'",
                "`del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识'",
                "`message_id` varchar(64)",
                "`conversation_id` varchar(64)",
                "`parent_message_id` varchar(64)",
                "`role` varchar(32)",
                "`content` longtext",
                "`content_type` varchar(32)",
                "`model_config_id` bigint DEFAULT NULL COMMENT '模型配置ID'",
                "`model_config_code` varchar(64)",
                "`provider_type` varchar(50)",
                "`model_name` varchar(100)",
                "`status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0=失败 1=成功 2=生成中)'",
                "`sequence_no` int NOT NULL DEFAULT '0' COMMENT '会话内消息序号'",
                "`prompt_tokens` int DEFAULT NULL COMMENT '提示词Token数'",
                "`completion_tokens` int DEFAULT NULL COMMENT '回复Token数'",
                "`total_tokens` int DEFAULT NULL COMMENT '总Token数'",
                "`finish_reason` varchar(50)",
                "`error_code` varchar(64)",
                "`error_message` varchar(1000)",
                "`sent_at` datetime NOT NULL COMMENT '消息时刻'",
                "UNIQUE KEY `uk_ai_message_tenant_message`",
                "KEY `idx_ai_message_tenant_conversation_sequence`",
                "KEY `idx_ai_message_tenant_conversation_sent`",
                "KEY `idx_ai_message_tenant_model`"
        );
    }

    private void assertField(String fieldName, String columnName) {
        try {
            TableField tableField = AiMessageEntity.class.getDeclaredField(fieldName)
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
        assertThat(tenantHandler.ignoreTable("ai_message"))
                .isEqualTo(BaseConstant.Tenant.DEFAULT_PRIVILEGED_TENANT_ID.equals(TenantContextHolder.getTenantId()));

        return new TenantLineInnerInterceptor(tenantHandler);
    }
}
