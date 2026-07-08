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
import io.github.module.ai.entity.AiMessageAttachmentEntity;
import io.github.module.ai.mapper.AiMessageAttachmentMapper;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AiMessageAttachmentTableTest {

    @AfterEach
    void clearTenantContext() {
        TenantContextHolder.clear();
    }

    @Test
    void aiMessageAttachmentEntityAndMapperUseTenantAwareDefaults() {
        assertThat(BaseEntity.class.isAssignableFrom(AiMessageAttachmentEntity.class)).isTrue();
        assertThat(BaseMapper.class.isAssignableFrom(AiMessageAttachmentMapper.class)).isTrue();
        assertThat(AiMessageAttachmentMapper.class.getAnnotation(InterceptorIgnore.class)).isNull();
        assertThat(AiMessageAttachmentEntity.class.getAnnotation(TableName.class).value()).isEqualTo("ai_message_attachment");
        assertField("messageId", "message_id");
        assertField("conversationId", "conversation_id");
        assertField("ossFileId", "oss_file_id");
        assertField("attachmentType", "attachment_type");
        assertField("fileName", "file_name");
        assertField("mimeType", "mime_type");
        assertField("fileSize", "file_size");
        assertField("sortNo", "sort_no");
    }

    @Test
    void normalTenantAddsTenantLineConditionForMessageAttachmentTable() {
        TenantContextHolder.setTenantContext(new TenantContext(1001L, "租户A"));
        TenantLineInnerInterceptor interceptor = tenantLineInnerInterceptor();

        Expression expression = interceptor.buildTableExpression(
                new Table("ai_message_attachment"),
                null,
                null
        );

        assertThat(expression).hasToString("tenant_id = 1001");
    }

    @Test
    void privilegedTenantCanBypassMessageAttachmentTenantLineCondition() {
        TenantContextHolder.setTenantContext(new TenantContext(
                BaseConstant.Tenant.DEFAULT_PRIVILEGED_TENANT_ID,
                "特权租户"
        ));
        TenantLineInnerInterceptor interceptor = tenantLineInnerInterceptor();

        Expression expression = interceptor.buildTableExpression(
                new Table("ai_message_attachment"),
                null,
                null
        );

        assertThat(expression).isNull();
    }

    @Test
    void moduleAndAggregateSqlContainMessageAttachmentTableDefinition() throws IOException {
        String moduleSql = Files.readString(Path.of("src/main/resources/sql/x_boot_ai.sql"));
        String aggregateSql = Files.readString(Path.of("..", "..", "..", "docs", "x_boot_all.sql"));

        assertMessageAttachmentSql(moduleSql);
        assertMessageAttachmentSql(aggregateSql);
    }

    private void assertMessageAttachmentSql(String sql) {
        assertThat(sql).contains(
                "CREATE TABLE `ai_message_attachment`",
                "`tenant_id` bigint DEFAULT NULL COMMENT '租户ID'",
                "`revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁'",
                "`del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识'",
                "`message_id` varchar(64)",
                "`conversation_id` varchar(64)",
                "`oss_file_id` bigint NOT NULL COMMENT 'OSS文件ID'",
                "`attachment_type` varchar(32)",
                "`file_name` varchar(255)",
                "`mime_type` varchar(100)",
                "`file_size` bigint DEFAULT NULL COMMENT '文件大小'",
                "`sort_no` int NOT NULL DEFAULT '0' COMMENT '排序号'",
                "KEY `idx_ai_message_attachment_tenant_message`",
                "KEY `idx_ai_message_attachment_tenant_conversation`",
                "KEY `idx_ai_message_attachment_tenant_oss_file`"
        );
    }

    private void assertField(String fieldName, String columnName) {
        try {
            TableField tableField = AiMessageAttachmentEntity.class.getDeclaredField(fieldName)
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
        assertThat(tenantHandler.ignoreTable("ai_message_attachment"))
                .isEqualTo(BaseConstant.Tenant.DEFAULT_PRIVILEGED_TENANT_ID.equals(TenantContextHolder.getTenantId()));

        return new TenantLineInnerInterceptor(tenantHandler);
    }
}
