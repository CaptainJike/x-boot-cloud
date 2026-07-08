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
import io.github.module.ai.entity.AiCallLogEntity;
import io.github.module.ai.mapper.AiCallLogMapper;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AiCallLogTableTest {

    @AfterEach
    void clearTenantContext() {
        TenantContextHolder.clear();
    }

    @Test
    void aiCallLogEntityAndMapperUseTenantAwareDefaults() {
        assertThat(BaseEntity.class.isAssignableFrom(AiCallLogEntity.class)).isTrue();
        assertThat(BaseMapper.class.isAssignableFrom(AiCallLogMapper.class)).isTrue();
        assertThat(AiCallLogMapper.class.getAnnotation(InterceptorIgnore.class)).isNull();
        assertThat(AiCallLogEntity.class.getAnnotation(TableName.class).value()).isEqualTo("ai_call_log");
        assertField("callId", "call_id");
        assertField("conversationId", "conversation_id");
        assertField("messageId", "message_id");
        assertField("userId", "user_id");
        assertField("modelConfigId", "model_config_id");
        assertField("modelConfigCode", "model_config_code");
        assertField("providerType", "provider_type");
        assertField("modelName", "model_name");
        assertField("requestType", "request_type");
        assertField("streamFlag", "stream_flag");
        assertField("requestPreview", "request_preview");
        assertField("responsePreview", "response_preview");
        assertField("status", "status");
        assertField("durationMs", "duration_ms");
        assertField("promptTokens", "prompt_tokens");
        assertField("completionTokens", "completion_tokens");
        assertField("totalTokens", "total_tokens");
        assertField("finishReason", "finish_reason");
        assertField("providerRequestId", "provider_request_id");
        assertField("traceId", "trace_id");
        assertField("errorCode", "error_code");
        assertField("errorMessage", "error_message");
        assertField("startedAt", "started_at");
        assertField("finishedAt", "finished_at");
    }

    @Test
    void normalTenantAddsTenantLineConditionForCallLogTable() {
        TenantContextHolder.setTenantContext(new TenantContext(1001L, "租户A"));
        TenantLineInnerInterceptor interceptor = tenantLineInnerInterceptor();

        Expression expression = interceptor.buildTableExpression(
                new Table("ai_call_log"),
                null,
                null
        );

        assertThat(expression).hasToString("tenant_id = 1001");
    }

    @Test
    void privilegedTenantCanBypassCallLogTenantLineCondition() {
        TenantContextHolder.setTenantContext(new TenantContext(
                BaseConstant.Tenant.DEFAULT_PRIVILEGED_TENANT_ID,
                "特权租户"
        ));
        TenantLineInnerInterceptor interceptor = tenantLineInnerInterceptor();

        Expression expression = interceptor.buildTableExpression(
                new Table("ai_call_log"),
                null,
                null
        );

        assertThat(expression).isNull();
    }

    @Test
    void moduleAndAggregateSqlContainCallLogTableDefinition() throws IOException {
        String moduleSql = Files.readString(Path.of("src/main/resources/sql/x_boot_ai.sql"));
        String aggregateSql = Files.readString(Path.of("..", "..", "..", "docs", "x_boot_all.sql"));

        assertCallLogSql(moduleSql);
        assertCallLogSql(aggregateSql);
    }

    private void assertCallLogSql(String sql) {
        assertThat(sql).contains(
                "CREATE TABLE `ai_call_log`",
                "`tenant_id` bigint DEFAULT NULL COMMENT '租户ID'",
                "`revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁'",
                "`del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识'",
                "`call_id` varchar(64)",
                "`conversation_id` varchar(64)",
                "`message_id` varchar(64)",
                "`user_id` bigint DEFAULT NULL COMMENT '后台用户ID'",
                "`model_config_id` bigint DEFAULT NULL COMMENT '模型配置ID'",
                "`model_config_code` varchar(64)",
                "`provider_type` varchar(50)",
                "`model_name` varchar(100)",
                "`request_type` varchar(32)",
                "`stream_flag` tinyint NOT NULL DEFAULT '0' COMMENT '是否流式调用(0=否 1=是)'",
                "`request_preview` varchar(1000)",
                "`response_preview` varchar(1000)",
                "`status` tinyint NOT NULL DEFAULT '2' COMMENT '状态(0=失败 1=成功 2=调用中)'",
                "`duration_ms` bigint DEFAULT NULL COMMENT '耗时，单位毫秒'",
                "`prompt_tokens` int DEFAULT NULL COMMENT '提示词Token数'",
                "`completion_tokens` int DEFAULT NULL COMMENT '回复Token数'",
                "`total_tokens` int DEFAULT NULL COMMENT '总Token数'",
                "`finish_reason` varchar(50)",
                "`provider_request_id` varchar(128)",
                "`trace_id` varchar(128)",
                "`error_code` varchar(64)",
                "`error_message` varchar(1000)",
                "`started_at` datetime NOT NULL COMMENT '调用开始时刻'",
                "`finished_at` datetime DEFAULT NULL COMMENT '调用结束时刻'",
                "UNIQUE KEY `uk_ai_call_log_tenant_call`",
                "KEY `idx_ai_call_log_tenant_conversation_started`",
                "KEY `idx_ai_call_log_tenant_message`",
                "KEY `idx_ai_call_log_tenant_model_started`",
                "KEY `idx_ai_call_log_tenant_status_started`"
        );
    }

    private void assertField(String fieldName, String columnName) {
        try {
            TableField tableField = AiCallLogEntity.class.getDeclaredField(fieldName)
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
        assertThat(tenantHandler.ignoreTable("ai_call_log"))
                .isEqualTo(BaseConstant.Tenant.DEFAULT_PRIVILEGED_TENANT_ID.equals(TenantContextHolder.getTenantId()));

        return new TenantLineInnerInterceptor(tenantHandler);
    }
}
