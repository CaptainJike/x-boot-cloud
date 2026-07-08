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
import io.github.module.ai.entity.AiFeedbackEntity;
import io.github.module.ai.mapper.AiFeedbackMapper;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AiFeedbackTableTest {

    @AfterEach
    void clearTenantContext() {
        TenantContextHolder.clear();
    }

    @Test
    void aiFeedbackEntityAndMapperUseTenantAwareDefaults() {
        assertThat(BaseEntity.class.isAssignableFrom(AiFeedbackEntity.class)).isTrue();
        assertThat(BaseMapper.class.isAssignableFrom(AiFeedbackMapper.class)).isTrue();
        assertThat(AiFeedbackMapper.class.getAnnotation(InterceptorIgnore.class)).isNull();
        assertThat(AiFeedbackEntity.class.getAnnotation(TableName.class).value()).isEqualTo("ai_feedback");
        assertField("feedbackId", "feedback_id");
        assertField("conversationId", "conversation_id");
        assertField("messageId", "message_id");
        assertField("userId", "user_id");
        assertField("modelConfigId", "model_config_id");
        assertField("modelConfigCode", "model_config_code");
        assertField("providerType", "provider_type");
        assertField("modelName", "model_name");
        assertField("feedbackType", "feedback_type");
        assertField("score", "score");
        assertField("reasonCode", "reason_code");
        assertField("content", "content");
        assertField("status", "status");
        assertField("handledBy", "handled_by");
        assertField("handledAt", "handled_at");
        assertField("remark", "remark");
        assertField("submittedAt", "submitted_at");
    }

    @Test
    void normalTenantAddsTenantLineConditionForFeedbackTable() {
        TenantContextHolder.setTenantContext(new TenantContext(1001L, "租户A"));
        TenantLineInnerInterceptor interceptor = tenantLineInnerInterceptor();

        Expression expression = interceptor.buildTableExpression(
                new Table("ai_feedback"),
                null,
                null
        );

        assertThat(expression).hasToString("tenant_id = 1001");
    }

    @Test
    void privilegedTenantCanBypassFeedbackTenantLineCondition() {
        TenantContextHolder.setTenantContext(new TenantContext(
                BaseConstant.Tenant.DEFAULT_PRIVILEGED_TENANT_ID,
                "特权租户"
        ));
        TenantLineInnerInterceptor interceptor = tenantLineInnerInterceptor();

        Expression expression = interceptor.buildTableExpression(
                new Table("ai_feedback"),
                null,
                null
        );

        assertThat(expression).isNull();
    }

    @Test
    void moduleAndAggregateSqlContainFeedbackTableDefinition() throws IOException {
        String moduleSql = Files.readString(Path.of("src/main/resources/sql/x_boot_ai.sql"));
        String aggregateSql = Files.readString(Path.of("..", "..", "..", "docs", "x_boot_all.sql"));

        assertFeedbackSql(moduleSql);
        assertFeedbackSql(aggregateSql);
    }

    private void assertFeedbackSql(String sql) {
        assertThat(sql).contains(
                "CREATE TABLE `ai_feedback`",
                "`tenant_id` bigint DEFAULT NULL COMMENT '租户ID'",
                "`revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁'",
                "`del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识'",
                "`feedback_id` varchar(64)",
                "`conversation_id` varchar(64)",
                "`message_id` varchar(64)",
                "`user_id` bigint NOT NULL COMMENT '后台用户ID'",
                "`model_config_id` bigint DEFAULT NULL COMMENT '模型配置ID'",
                "`model_config_code` varchar(64)",
                "`provider_type` varchar(50)",
                "`model_name` varchar(100)",
                "`feedback_type` varchar(32)",
                "`score` tinyint DEFAULT NULL COMMENT '评分(1-5)'",
                "`reason_code` varchar(64)",
                "`content` varchar(1000)",
                "`status` tinyint NOT NULL DEFAULT '0' COMMENT '处理状态(0=待处理 1=已处理 2=忽略)'",
                "`handled_by` varchar(255)",
                "`handled_at` datetime DEFAULT NULL COMMENT '处理时刻'",
                "`remark` varchar(500)",
                "`submitted_at` datetime NOT NULL COMMENT '反馈提交时刻'",
                "UNIQUE KEY `uk_ai_feedback_tenant_feedback`",
                "KEY `idx_ai_feedback_tenant_conversation_submitted`",
                "KEY `idx_ai_feedback_tenant_message`",
                "KEY `idx_ai_feedback_tenant_user_submitted`",
                "KEY `idx_ai_feedback_tenant_model`",
                "KEY `idx_ai_feedback_tenant_status_submitted`"
        );
    }

    private void assertField(String fieldName, String columnName) {
        try {
            TableField tableField = AiFeedbackEntity.class.getDeclaredField(fieldName)
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
        assertThat(tenantHandler.ignoreTable("ai_feedback"))
                .isEqualTo(BaseConstant.Tenant.DEFAULT_PRIVILEGED_TENANT_ID.equals(TenantContextHolder.getTenantId()));

        return new TenantLineInnerInterceptor(tenantHandler);
    }
}
