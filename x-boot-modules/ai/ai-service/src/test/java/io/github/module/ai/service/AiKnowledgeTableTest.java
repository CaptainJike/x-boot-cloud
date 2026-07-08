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
import io.github.module.ai.entity.AiKnowledgeBaseEntity;
import io.github.module.ai.entity.AiKnowledgeDocumentChunkEntity;
import io.github.module.ai.entity.AiKnowledgeDocumentEntity;
import io.github.module.ai.entity.AiKnowledgeRetrievalLogEntity;
import io.github.module.ai.mapper.AiKnowledgeBaseMapper;
import io.github.module.ai.mapper.AiKnowledgeDocumentChunkMapper;
import io.github.module.ai.mapper.AiKnowledgeDocumentMapper;
import io.github.module.ai.mapper.AiKnowledgeRetrievalLogMapper;
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

class AiKnowledgeTableTest {

    private static final Set<String> KNOWLEDGE_TABLES = Set.of(
            "ai_knowledge_base",
            "ai_knowledge_document",
            "ai_knowledge_document_chunk",
            "ai_knowledge_retrieval_log"
    );

    @AfterEach
    void clearTenantContext() {
        TenantContextHolder.clear();
    }

    @Test
    void knowledgeBaseEntityAndMapperUseTenantAwareDefaults() {
        assertEntityAndMapper(
                AiKnowledgeBaseEntity.class,
                AiKnowledgeBaseMapper.class,
                "ai_knowledge_base",
                Map.ofEntries(
                        entry("name", "name"),
                        entry("description", "description"),
                        entry("embeddingModelConfigId", "embedding_model_config_id"),
                        entry("embeddingModelConfigCode", "embedding_model_config_code"),
                        entry("embeddingProviderType", "embedding_provider_type"),
                        entry("embeddingModelName", "embedding_model_name"),
                        entry("retrievalTopK", "retrieval_top_k"),
                        entry("similarityThreshold", "similarity_threshold"),
                        entry("status", "status"),
                        entry("documentCount", "document_count"),
                        entry("chunkCount", "chunk_count"),
                        entry("lastParsedAt", "last_parsed_at"),
                        entry("lastRetrievedAt", "last_retrieved_at")
                )
        );
    }

    @Test
    void knowledgeDocumentEntityAndMapperUseTenantAwareDefaults() {
        assertEntityAndMapper(
                AiKnowledgeDocumentEntity.class,
                AiKnowledgeDocumentMapper.class,
                "ai_knowledge_document",
                Map.ofEntries(
                        entry("knowledgeBaseId", "knowledge_base_id"),
                        entry("ossFileId", "oss_file_id"),
                        entry("documentName", "document_name"),
                        entry("description", "description"),
                        entry("originalFilename", "original_filename"),
                        entry("extendName", "extend_name"),
                        entry("fileSize", "file_size"),
                        entry("md5", "md5"),
                        entry("storagePlatform", "storage_platform"),
                        entry("parseStatus", "parse_status"),
                        entry("chunkStatus", "chunk_status"),
                        entry("embeddingStatus", "embedding_status"),
                        entry("status", "status"),
                        entry("chunkCount", "chunk_count"),
                        entry("parseErrorMessage", "parse_error_message"),
                        entry("chunkErrorMessage", "chunk_error_message"),
                        entry("retryCount", "retry_count"),
                        entry("parsedAt", "parsed_at"),
                        entry("chunkedAt", "chunked_at"),
                        entry("lastRetryAt", "last_retry_at")
                )
        );
    }

    @Test
    void knowledgeDocumentChunkEntityAndMapperUseTenantAwareDefaults() {
        assertEntityAndMapper(
                AiKnowledgeDocumentChunkEntity.class,
                AiKnowledgeDocumentChunkMapper.class,
                "ai_knowledge_document_chunk",
                Map.ofEntries(
                        entry("knowledgeBaseId", "knowledge_base_id"),
                        entry("documentId", "document_id"),
                        entry("chunkNo", "chunk_no"),
                        entry("content", "content"),
                        entry("contentPreview", "content_preview"),
                        entry("sourcePage", "source_page"),
                        entry("sourcePosition", "source_position"),
                        entry("tokenCount", "token_count"),
                        entry("status", "status"),
                        entry("embeddingStatus", "embedding_status"),
                        entry("embeddingModelConfigId", "embedding_model_config_id"),
                        entry("embeddingModelConfigCode", "embedding_model_config_code"),
                        entry("embeddingProviderType", "embedding_provider_type"),
                        entry("embeddingModelName", "embedding_model_name"),
                        entry("vectorId", "vector_id"),
                        entry("vectorHash", "vector_hash"),
                        entry("errorMessage", "error_message")
                )
        );
    }

    @Test
    void knowledgeRetrievalLogEntityAndMapperUseTenantAwareDefaults() {
        assertEntityAndMapper(
                AiKnowledgeRetrievalLogEntity.class,
                AiKnowledgeRetrievalLogMapper.class,
                "ai_knowledge_retrieval_log",
                Map.ofEntries(
                        entry("retrievalId", "retrieval_id"),
                        entry("userId", "user_id"),
                        entry("knowledgeBaseIds", "knowledge_base_ids"),
                        entry("conversationId", "conversation_id"),
                        entry("messageId", "message_id"),
                        entry("queryText", "query_text"),
                        entry("topK", "top_k"),
                        entry("similarityThreshold", "similarity_threshold"),
                        entry("hitCount", "hit_count"),
                        entry("hitsSummary", "hits_summary"),
                        entry("elapsedMs", "elapsed_ms"),
                        entry("status", "status"),
                        entry("errorCode", "error_code"),
                        entry("errorMessage", "error_message"),
                        entry("retrievedAt", "retrieved_at")
                )
        );
    }

    @Test
    void normalTenantAddsTenantLineConditionForKnowledgeTables() {
        TenantContextHolder.setTenantContext(new TenantContext(1001L, "租户A"));
        TenantLineInnerInterceptor interceptor = tenantLineInnerInterceptor();

        for (String tableName : KNOWLEDGE_TABLES) {
            Expression expression = interceptor.buildTableExpression(new Table(tableName), null, null);
            assertThat(expression).hasToString("tenant_id = 1001");
        }
    }

    @Test
    void privilegedTenantCanBypassKnowledgeTenantLineCondition() {
        TenantContextHolder.setTenantContext(new TenantContext(
                BaseConstant.Tenant.DEFAULT_PRIVILEGED_TENANT_ID,
                "特权租户"
        ));
        TenantLineInnerInterceptor interceptor = tenantLineInnerInterceptor();

        for (String tableName : KNOWLEDGE_TABLES) {
            Expression expression = interceptor.buildTableExpression(new Table(tableName), null, null);
            assertThat(expression).isNull();
        }
    }

    @Test
    void moduleAndAggregateSqlContainKnowledgeTableDefinitions() throws IOException {
        String moduleSql = Files.readString(Path.of("src/main/resources/sql/x_boot_ai.sql"));
        String aggregateSql = Files.readString(Path.of("..", "..", "..", "docs", "x_boot_all.sql"));

        assertKnowledgeBaseSql(moduleSql);
        assertKnowledgeBaseSql(aggregateSql);
        assertKnowledgeDocumentSql(moduleSql);
        assertKnowledgeDocumentSql(aggregateSql);
        assertKnowledgeDocumentChunkSql(moduleSql);
        assertKnowledgeDocumentChunkSql(aggregateSql);
        assertKnowledgeRetrievalLogSql(moduleSql);
        assertKnowledgeRetrievalLogSql(aggregateSql);
    }

    private void assertEntityAndMapper(
            Class<?> entityType,
            Class<?> mapperType,
            String tableName,
            Map<String, String> fields
    ) {
        assertThat(BaseEntity.class.isAssignableFrom(entityType)).isTrue();
        assertThat(BaseMapper.class.isAssignableFrom(mapperType)).isTrue();
        assertThat(mapperType.getAnnotation(InterceptorIgnore.class)).isNull();
        assertThat(entityType.getAnnotation(TableName.class).value()).isEqualTo(tableName);
        fields.forEach((fieldName, columnName) -> assertField(entityType, fieldName, columnName));
    }

    private void assertKnowledgeBaseSql(String sql) {
        assertThat(sql).contains(
                "CREATE TABLE `ai_knowledge_base`",
                "`tenant_id` bigint DEFAULT NULL COMMENT '租户ID'",
                "`revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁'",
                "`del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识'",
                "`name` varchar(100)",
                "`embedding_model_config_id` bigint DEFAULT NULL COMMENT '向量化模型配置ID'",
                "`embedding_model_config_code` varchar(64)",
                "`retrieval_top_k` int NOT NULL DEFAULT '5' COMMENT '默认召回数量'",
                "`similarity_threshold` decimal(5",
                "`status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0=禁用 1=启用)'",
                "`document_count` int NOT NULL DEFAULT '0' COMMENT '文档数量'",
                "`chunk_count` int NOT NULL DEFAULT '0' COMMENT '切片数量'",
                "`last_parsed_at` datetime DEFAULT NULL COMMENT '最近文档解析时刻'",
                "`last_retrieved_at` datetime DEFAULT NULL COMMENT '最近检索时刻'",
                "UNIQUE KEY `uk_ai_knowledge_base_tenant_name`",
                "KEY `idx_ai_knowledge_base_tenant_status_updated`",
                "KEY `idx_ai_knowledge_base_tenant_embedding`",
                "KEY `idx_ai_knowledge_base_tenant_last_retrieved`"
        );
    }

    private void assertKnowledgeDocumentSql(String sql) {
        assertThat(sql).contains(
                "CREATE TABLE `ai_knowledge_document`",
                "`knowledge_base_id` bigint NOT NULL COMMENT '知识库ID'",
                "`oss_file_id` bigint NOT NULL COMMENT 'OSS文件ID'",
                "`document_name` varchar(200)",
                "`original_filename` varchar(255)",
                "`extend_name` varchar(50)",
                "`file_size` bigint DEFAULT NULL COMMENT '文件大小'",
                "`md5` varchar(64)",
                "`storage_platform` varchar(64)",
                "`parse_status` tinyint NOT NULL DEFAULT '3'",
                "`chunk_status` tinyint NOT NULL DEFAULT '3'",
                "`embedding_status` tinyint NOT NULL DEFAULT '3'",
                "`chunk_count` int NOT NULL DEFAULT '0' COMMENT '切片数量'",
                "`parse_error_message` varchar(1000)",
                "`chunk_error_message` varchar(1000)",
                "`retry_count` int NOT NULL DEFAULT '0' COMMENT '重试次数'",
                "`parsed_at` datetime DEFAULT NULL COMMENT '最近解析时刻'",
                "`chunked_at` datetime DEFAULT NULL COMMENT '最近切片时刻'",
                "`last_retry_at` datetime DEFAULT NULL COMMENT '最近重试时刻'",
                "UNIQUE KEY `uk_ai_knowledge_document_tenant_base_oss`",
                "KEY `idx_ai_knowledge_document_tenant_base_status`",
                "KEY `idx_ai_knowledge_document_tenant_oss`",
                "KEY `idx_ai_knowledge_document_tenant_parse`",
                "KEY `idx_ai_knowledge_document_tenant_chunk`"
        );
    }

    private void assertKnowledgeDocumentChunkSql(String sql) {
        assertThat(sql).contains(
                "CREATE TABLE `ai_knowledge_document_chunk`",
                "`knowledge_base_id` bigint NOT NULL COMMENT '知识库ID'",
                "`document_id` bigint NOT NULL COMMENT '文档ID'",
                "`chunk_no` int NOT NULL COMMENT '切片序号'",
                "`content` longtext",
                "`content_preview` varchar(500)",
                "`source_page` int DEFAULT NULL COMMENT '来源页码'",
                "`source_position` varchar(255)",
                "`token_count` int DEFAULT NULL COMMENT '预估Token数'",
                "`status` tinyint NOT NULL DEFAULT '1' COMMENT '切片状态(0=失败 1=成功 2=处理中)'",
                "`embedding_status` tinyint NOT NULL DEFAULT '3'",
                "`embedding_model_config_id` bigint DEFAULT NULL COMMENT '向量化模型配置ID'",
                "`vector_id` varchar(128)",
                "`vector_hash` varchar(64)",
                "`error_message` varchar(1000)",
                "UNIQUE KEY `uk_ai_knowledge_chunk_tenant_document_no`",
                "KEY `idx_ai_knowledge_chunk_tenant_base_status`",
                "KEY `idx_ai_knowledge_chunk_tenant_document`",
                "KEY `idx_ai_knowledge_chunk_tenant_embedding`",
                "KEY `idx_ai_knowledge_chunk_tenant_vector`"
        );
    }

    private void assertKnowledgeRetrievalLogSql(String sql) {
        assertThat(sql).contains(
                "CREATE TABLE `ai_knowledge_retrieval_log`",
                "`retrieval_id` varchar(64)",
                "`user_id` bigint DEFAULT NULL COMMENT '后台用户ID'",
                "`knowledge_base_ids` varchar(500)",
                "`conversation_id` varchar(64)",
                "`message_id` varchar(64)",
                "`query_text` varchar(4000)",
                "`top_k` int NOT NULL DEFAULT '5' COMMENT '召回数量'",
                "`similarity_threshold` decimal(5",
                "`hit_count` int NOT NULL DEFAULT '0' COMMENT '命中数量'",
                "`hits_summary` longtext",
                "`elapsed_ms` bigint DEFAULT NULL COMMENT '耗时，单位毫秒'",
                "`status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0=失败 1=成功)'",
                "`error_code` varchar(64)",
                "`error_message` varchar(1000)",
                "`retrieved_at` datetime NOT NULL COMMENT '检索时刻'",
                "UNIQUE KEY `uk_ai_knowledge_retrieval_tenant_retrieval`",
                "KEY `idx_ai_knowledge_retrieval_tenant_conversation`",
                "KEY `idx_ai_knowledge_retrieval_tenant_message`",
                "KEY `idx_ai_knowledge_retrieval_tenant_user`",
                "KEY `idx_ai_knowledge_retrieval_tenant_status`"
        );
    }

    private void assertField(Class<?> entityType, String fieldName, String columnName) {
        try {
            TableField tableField = entityType.getDeclaredField(fieldName).getAnnotation(TableField.class);
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
        return new TenantLineInnerInterceptor(tenantHandler);
    }
}
