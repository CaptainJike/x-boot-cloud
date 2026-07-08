/*
 Navicat MySQL Data Transfer

 Source Schema         : x_boot_ai

 Target Server Type    : MySQL
 File Encoding         : 65001
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_model_config
-- ----------------------------
DROP TABLE IF EXISTS `ai_model_config`;
CREATE TABLE `ai_model_config` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
  `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  `created_at` datetime NOT NULL COMMENT '创建时刻',
  `created_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建者',
  `updated_at` datetime NOT NULL COMMENT '更新时刻',
  `updated_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新者',
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置编码',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置名称',
  `provider_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '供应商类型',
  `base_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '模型服务地址',
  `api_key` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'API Key',
  `model_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模型名称',
  `supported_modalities` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'text' COMMENT '支持的模态(text,image)',
  `supported_capabilities` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'chat' COMMENT '支持的能力(chat,embedding,image)',
  `temperature` decimal(4,2) DEFAULT NULL COMMENT '温度参数',
  `timeout_seconds` bigint DEFAULT NULL COMMENT '调用超时时间，单位秒',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0=禁用 1=启用)',
  `default_flag` tinyint NOT NULL DEFAULT '0' COMMENT '是否默认配置(0=否 1=是)',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '描述',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_ai_model_config_tenant_code` (`tenant_id`, `code`) USING BTREE,
  KEY `idx_ai_model_config_tenant_default` (`tenant_id`, `default_flag`, `status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='AI模型配置';

-- ----------------------------
-- Table structure for ai_agent
-- ----------------------------
DROP TABLE IF EXISTS `ai_agent`;
CREATE TABLE `ai_agent` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
  `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  `created_at` datetime NOT NULL COMMENT '创建时刻',
  `created_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建者',
  `updated_at` datetime NOT NULL COMMENT '更新时刻',
  `updated_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新者',
  `agent_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Agent编码',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Agent名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Agent描述',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'Agent头像',
  `system_prompt` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '系统提示词',
  `model_config_id` bigint DEFAULT NULL COMMENT '默认模型配置ID',
  `model_config_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '默认模型配置编码',
  `provider_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '默认供应商类型',
  `model_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '默认模型名称',
  `knowledge_base_ids` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '默认知识库ID列表',
  `temperature` decimal(4,2) DEFAULT NULL COMMENT '温度参数',
  `max_tokens` int DEFAULT NULL COMMENT '最大回复Token数',
  `execution_config` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '执行参数JSON',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0=禁用 1=启用)',
  `publish_status` tinyint NOT NULL DEFAULT '0' COMMENT '发布状态(0=草稿 1=已发布)',
  `published_at` datetime DEFAULT NULL COMMENT '发布时刻',
  `last_executed_at` datetime DEFAULT NULL COMMENT '最近执行时刻',
  `execution_count` int NOT NULL DEFAULT '0' COMMENT '执行次数',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_ai_agent_tenant_code` (`tenant_id`, `agent_code`) USING BTREE,
  KEY `idx_ai_agent_tenant_status_updated` (`tenant_id`, `status`, `updated_at`) USING BTREE,
  KEY `idx_ai_agent_tenant_publish_status` (`tenant_id`, `publish_status`, `updated_at`) USING BTREE,
  KEY `idx_ai_agent_tenant_model` (`tenant_id`, `model_config_id`) USING BTREE,
  KEY `idx_ai_agent_tenant_last_executed` (`tenant_id`, `last_executed_at`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='AI Agent配置';

-- ----------------------------
-- Table structure for ai_workflow_definition
-- ----------------------------
DROP TABLE IF EXISTS `ai_workflow_definition`;
CREATE TABLE `ai_workflow_definition` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
  `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  `created_at` datetime NOT NULL COMMENT '创建时刻',
  `created_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建者',
  `updated_at` datetime NOT NULL COMMENT '更新时刻',
  `updated_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新者',
  `workflow_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工作流编码',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工作流名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '工作流描述',
  `agent_id` bigint DEFAULT NULL COMMENT '关联Agent ID',
  `version_no` int NOT NULL DEFAULT '1' COMMENT '版本号',
  `entry_node_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '入口节点Key',
  `definition_snapshot` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '定义快照JSON',
  `published_snapshot` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '发布快照JSON',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0=禁用 1=启用)',
  `publish_status` tinyint NOT NULL DEFAULT '0' COMMENT '发布状态(0=草稿 1=已发布)',
  `published_at` datetime DEFAULT NULL COMMENT '发布时刻',
  `last_executed_at` datetime DEFAULT NULL COMMENT '最近执行时刻',
  `execution_count` int NOT NULL DEFAULT '0' COMMENT '执行次数',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_ai_workflow_definition_tenant_code_version` (`tenant_id`, `workflow_code`, `version_no`) USING BTREE,
  KEY `idx_ai_workflow_definition_tenant_status_updated` (`tenant_id`, `status`, `updated_at`) USING BTREE,
  KEY `idx_ai_workflow_definition_tenant_publish_status` (`tenant_id`, `publish_status`, `updated_at`) USING BTREE,
  KEY `idx_ai_workflow_definition_tenant_agent` (`tenant_id`, `agent_id`) USING BTREE,
  KEY `idx_ai_workflow_definition_tenant_last_executed` (`tenant_id`, `last_executed_at`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='AI工作流定义';

-- ----------------------------
-- Table structure for ai_workflow_node
-- ----------------------------
DROP TABLE IF EXISTS `ai_workflow_node`;
CREATE TABLE `ai_workflow_node` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
  `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  `created_at` datetime NOT NULL COMMENT '创建时刻',
  `created_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建者',
  `updated_at` datetime NOT NULL COMMENT '更新时刻',
  `updated_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新者',
  `workflow_definition_id` bigint NOT NULL COMMENT '工作流定义ID',
  `workflow_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工作流编码',
  `version_no` int NOT NULL DEFAULT '1' COMMENT '版本号',
  `node_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '节点Key',
  `node_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '节点名称',
  `node_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '节点类型(llm/http_tool/condition/end)',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '节点描述',
  `node_config` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '节点配置JSON',
  `input_mapping` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '输入映射JSON',
  `output_mapping` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '输出映射JSON',
  `next_node_keys` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '下游节点Key列表',
  `condition_expression` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '条件表达式',
  `error_strategy` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'fail' COMMENT '错误策略(fail/continue/retry)',
  `retry_count` int NOT NULL DEFAULT '0' COMMENT '重试次数',
  `timeout_seconds` bigint DEFAULT NULL COMMENT '超时时间，单位秒',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0=禁用 1=启用)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_ai_workflow_node_tenant_workflow_node` (`tenant_id`, `workflow_definition_id`, `node_key`) USING BTREE,
  KEY `idx_ai_workflow_node_tenant_workflow_sort` (`tenant_id`, `workflow_definition_id`, `sort_order`) USING BTREE,
  KEY `idx_ai_workflow_node_tenant_type` (`tenant_id`, `node_type`, `status`) USING BTREE,
  KEY `idx_ai_workflow_node_tenant_workflow_type` (`tenant_id`, `workflow_definition_id`, `node_type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='AI工作流节点';

-- ----------------------------
-- Table structure for ai_workflow_execution
-- ----------------------------
DROP TABLE IF EXISTS `ai_workflow_execution`;
CREATE TABLE `ai_workflow_execution` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
  `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  `created_at` datetime NOT NULL COMMENT '创建时刻',
  `created_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建者',
  `updated_at` datetime NOT NULL COMMENT '更新时刻',
  `updated_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新者',
  `execution_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务执行ID',
  `workflow_definition_id` bigint NOT NULL COMMENT '工作流定义ID',
  `workflow_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工作流编码',
  `workflow_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '工作流名称',
  `version_no` int NOT NULL DEFAULT '1' COMMENT '版本号',
  `agent_id` bigint DEFAULT NULL COMMENT '关联Agent ID',
  `user_id` bigint DEFAULT NULL COMMENT '后台用户ID',
  `trigger_source` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'admin' COMMENT '触发来源(admin/schedule/api)',
  `trigger_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '触发业务ID',
  `input_summary` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '输入摘要JSON',
  `output_summary` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '输出摘要JSON',
  `status` tinyint NOT NULL DEFAULT '2' COMMENT '状态(0=失败 1=成功 2=执行中 3=取消)',
  `current_node_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '当前节点Key',
  `failed_node_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '失败节点Key',
  `duration_ms` bigint DEFAULT NULL COMMENT '耗时，单位毫秒',
  `error_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '错误编码',
  `error_message` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '错误信息',
  `trace_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '链路追踪ID',
  `started_at` datetime NOT NULL COMMENT '执行开始时刻',
  `finished_at` datetime DEFAULT NULL COMMENT '执行结束时刻',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_ai_workflow_execution_tenant_execution` (`tenant_id`, `execution_id`) USING BTREE,
  KEY `idx_ai_workflow_execution_tenant_workflow_started` (`tenant_id`, `workflow_definition_id`, `started_at`) USING BTREE,
  KEY `idx_ai_workflow_execution_tenant_user_started` (`tenant_id`, `user_id`, `started_at`) USING BTREE,
  KEY `idx_ai_workflow_execution_tenant_status_started` (`tenant_id`, `status`, `started_at`) USING BTREE,
  KEY `idx_ai_workflow_execution_tenant_trigger` (`tenant_id`, `trigger_source`, `trigger_id`) USING BTREE,
  KEY `idx_ai_workflow_execution_tenant_failed_node` (`tenant_id`, `failed_node_key`, `started_at`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='AI工作流执行记录';

-- ----------------------------
-- Table structure for ai_tool_registry
-- ----------------------------
DROP TABLE IF EXISTS `ai_tool_registry`;
CREATE TABLE `ai_tool_registry` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
  `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  `created_at` datetime NOT NULL COMMENT '创建时刻',
  `created_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建者',
  `updated_at` datetime NOT NULL COMMENT '更新时刻',
  `updated_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新者',
  `tool_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工具编码',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工具名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '工具描述',
  `tool_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工具类型(http/mcp)',
  `protocol` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'http' COMMENT '调用协议(http/mcp)',
  `endpoint_url` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '工具入口地址',
  `http_method` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'HTTP请求方法',
  `request_schema` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '请求参数Schema JSON',
  `response_schema` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '响应Schema JSON',
  `config_schema` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '工具配置Schema JSON',
  `allowed_hosts` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '允许访问主机列表',
  `sensitive_fields` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '敏感字段列表',
  `timeout_ms` bigint DEFAULT NULL COMMENT '默认超时时间，单位毫秒',
  `version_no` int NOT NULL DEFAULT '1' COMMENT '版本号',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0=禁用 1=启用)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_ai_tool_registry_tenant_code` (`tenant_id`, `tool_code`) USING BTREE,
  KEY `idx_ai_tool_registry_tenant_type_status` (`tenant_id`, `tool_type`, `status`) USING BTREE,
  KEY `idx_ai_tool_registry_tenant_protocol_status` (`tenant_id`, `protocol`, `status`) USING BTREE,
  KEY `idx_ai_tool_registry_tenant_updated` (`tenant_id`, `updated_at`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='AI工具注册';

-- ----------------------------
-- Table structure for ai_conversation
-- ----------------------------
DROP TABLE IF EXISTS `ai_conversation`;
CREATE TABLE `ai_conversation` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
  `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  `created_at` datetime NOT NULL COMMENT '创建时刻',
  `created_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建者',
  `updated_at` datetime NOT NULL COMMENT '更新时刻',
  `updated_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新者',
  `conversation_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务会话ID',
  `user_id` bigint NOT NULL COMMENT '后台用户ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '会话标题',
  `model_config_id` bigint DEFAULT NULL COMMENT '模型配置ID',
  `model_config_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '模型配置编码',
  `provider_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '供应商类型',
  `model_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '模型名称',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0=归档 1=活跃)',
  `message_count` int NOT NULL DEFAULT '0' COMMENT '消息数量',
  `last_message_at` datetime DEFAULT NULL COMMENT '最近消息时刻',
  `last_message_preview` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '最近消息预览',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_ai_conversation_tenant_conversation` (`tenant_id`, `conversation_id`) USING BTREE,
  KEY `idx_ai_conversation_tenant_user_status_updated` (`tenant_id`, `user_id`, `status`, `updated_at`) USING BTREE,
  KEY `idx_ai_conversation_tenant_model` (`tenant_id`, `model_config_id`) USING BTREE,
  KEY `idx_ai_conversation_tenant_last_message` (`tenant_id`, `last_message_at`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='AI会话';

-- ----------------------------
-- Table structure for ai_message
-- ----------------------------
DROP TABLE IF EXISTS `ai_message`;
CREATE TABLE `ai_message` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
  `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  `created_at` datetime NOT NULL COMMENT '创建时刻',
  `created_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建者',
  `updated_at` datetime NOT NULL COMMENT '更新时刻',
  `updated_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新者',
  `message_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务消息ID',
  `conversation_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务会话ID',
  `parent_message_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '父消息ID',
  `role` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息角色(user/assistant/system)',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `content_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'text' COMMENT '内容类型',
  `model_config_id` bigint DEFAULT NULL COMMENT '模型配置ID',
  `model_config_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '模型配置编码',
  `provider_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '供应商类型',
  `model_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '模型名称',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0=失败 1=成功 2=生成中)',
  `sequence_no` int NOT NULL DEFAULT '0' COMMENT '会话内消息序号',
  `prompt_tokens` int DEFAULT NULL COMMENT '提示词Token数',
  `completion_tokens` int DEFAULT NULL COMMENT '回复Token数',
  `total_tokens` int DEFAULT NULL COMMENT '总Token数',
  `finish_reason` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '结束原因',
  `error_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '错误编码',
  `error_message` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '错误信息',
  `sent_at` datetime NOT NULL COMMENT '消息时刻',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_ai_message_tenant_message` (`tenant_id`, `message_id`) USING BTREE,
  KEY `idx_ai_message_tenant_conversation_sequence` (`tenant_id`, `conversation_id`, `sequence_no`) USING BTREE,
  KEY `idx_ai_message_tenant_conversation_sent` (`tenant_id`, `conversation_id`, `sent_at`) USING BTREE,
  KEY `idx_ai_message_tenant_model` (`tenant_id`, `model_config_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='AI消息';

-- ----------------------------
-- Table structure for ai_message_attachment
-- ----------------------------
DROP TABLE IF EXISTS `ai_message_attachment`;
CREATE TABLE `ai_message_attachment` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
  `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  `created_at` datetime NOT NULL COMMENT '创建时刻',
  `created_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建者',
  `updated_at` datetime NOT NULL COMMENT '更新时刻',
  `updated_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新者',
  `message_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务消息ID',
  `conversation_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务会话ID',
  `oss_file_id` bigint NOT NULL COMMENT 'OSS文件ID',
  `attachment_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '附件类型(image/file)',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '文件名',
  `mime_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'MIME类型',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小',
  `sort_no` int NOT NULL DEFAULT '0' COMMENT '排序号',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_ai_message_attachment_tenant_message` (`tenant_id`, `message_id`) USING BTREE,
  KEY `idx_ai_message_attachment_tenant_conversation` (`tenant_id`, `conversation_id`, `message_id`) USING BTREE,
  KEY `idx_ai_message_attachment_tenant_oss_file` (`tenant_id`, `oss_file_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='AI消息附件';

-- ----------------------------
-- Table structure for ai_call_log
-- ----------------------------
DROP TABLE IF EXISTS `ai_call_log`;
CREATE TABLE `ai_call_log` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
  `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  `created_at` datetime NOT NULL COMMENT '创建时刻',
  `created_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建者',
  `updated_at` datetime NOT NULL COMMENT '更新时刻',
  `updated_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新者',
  `call_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务调用ID',
  `conversation_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '业务会话ID',
  `message_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '业务消息ID',
  `user_id` bigint DEFAULT NULL COMMENT '后台用户ID',
  `model_config_id` bigint DEFAULT NULL COMMENT '模型配置ID',
  `model_config_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '模型配置编码',
  `provider_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '供应商类型',
  `model_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '模型名称',
  `request_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调用类型(chat/stream/embedding)',
  `stream_flag` tinyint NOT NULL DEFAULT '0' COMMENT '是否流式调用(0=否 1=是)',
  `request_preview` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '请求内容摘要',
  `response_preview` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '响应内容摘要',
  `status` tinyint NOT NULL DEFAULT '2' COMMENT '状态(0=失败 1=成功 2=调用中)',
  `duration_ms` bigint DEFAULT NULL COMMENT '耗时，单位毫秒',
  `prompt_tokens` int DEFAULT NULL COMMENT '提示词Token数',
  `completion_tokens` int DEFAULT NULL COMMENT '回复Token数',
  `total_tokens` int DEFAULT NULL COMMENT '总Token数',
  `finish_reason` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '结束原因',
  `provider_request_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '供应商请求ID',
  `trace_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '链路追踪ID',
  `error_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '错误编码',
  `error_message` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '错误信息',
  `started_at` datetime NOT NULL COMMENT '调用开始时刻',
  `finished_at` datetime DEFAULT NULL COMMENT '调用结束时刻',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_ai_call_log_tenant_call` (`tenant_id`, `call_id`) USING BTREE,
  KEY `idx_ai_call_log_tenant_conversation_started` (`tenant_id`, `conversation_id`, `started_at`) USING BTREE,
  KEY `idx_ai_call_log_tenant_message` (`tenant_id`, `message_id`) USING BTREE,
  KEY `idx_ai_call_log_tenant_model_started` (`tenant_id`, `model_config_id`, `started_at`) USING BTREE,
  KEY `idx_ai_call_log_tenant_status_started` (`tenant_id`, `status`, `started_at`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='AI调用日志';

-- ----------------------------
-- Table structure for ai_feedback
-- ----------------------------
DROP TABLE IF EXISTS `ai_feedback`;
CREATE TABLE `ai_feedback` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
  `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  `created_at` datetime NOT NULL COMMENT '创建时刻',
  `created_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建者',
  `updated_at` datetime NOT NULL COMMENT '更新时刻',
  `updated_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新者',
  `feedback_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务反馈ID',
  `conversation_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务会话ID',
  `message_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务消息ID',
  `user_id` bigint NOT NULL COMMENT '后台用户ID',
  `model_config_id` bigint DEFAULT NULL COMMENT '模型配置ID',
  `model_config_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '模型配置编码',
  `provider_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '供应商类型',
  `model_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '模型名称',
  `feedback_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '反馈类型(like/dislike/rating)',
  `score` tinyint DEFAULT NULL COMMENT '评分(1-5)',
  `reason_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '反馈原因编码',
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '反馈内容',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '处理状态(0=待处理 1=已处理 2=忽略)',
  `handled_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '处理人',
  `handled_at` datetime DEFAULT NULL COMMENT '处理时刻',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '处理备注',
  `submitted_at` datetime NOT NULL COMMENT '反馈提交时刻',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_ai_feedback_tenant_feedback` (`tenant_id`, `feedback_id`) USING BTREE,
  KEY `idx_ai_feedback_tenant_conversation_submitted` (`tenant_id`, `conversation_id`, `submitted_at`) USING BTREE,
  KEY `idx_ai_feedback_tenant_message` (`tenant_id`, `message_id`) USING BTREE,
  KEY `idx_ai_feedback_tenant_user_submitted` (`tenant_id`, `user_id`, `submitted_at`) USING BTREE,
  KEY `idx_ai_feedback_tenant_model` (`tenant_id`, `model_config_id`) USING BTREE,
  KEY `idx_ai_feedback_tenant_status_submitted` (`tenant_id`, `status`, `submitted_at`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='AI反馈';

-- ----------------------------
-- Table structure for ai_knowledge_base
-- ----------------------------
DROP TABLE IF EXISTS `ai_knowledge_base`;
CREATE TABLE `ai_knowledge_base` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
  `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  `created_at` datetime NOT NULL COMMENT '创建时刻',
  `created_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建者',
  `updated_at` datetime NOT NULL COMMENT '更新时刻',
  `updated_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新者',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '知识库名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '知识库描述',
  `embedding_model_config_id` bigint DEFAULT NULL COMMENT '向量化模型配置ID',
  `embedding_model_config_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '向量化模型配置编码',
  `embedding_provider_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '向量化供应商类型',
  `embedding_model_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '向量化模型名称',
  `retrieval_top_k` int NOT NULL DEFAULT '5' COMMENT '默认召回数量',
  `similarity_threshold` decimal(5,4) NOT NULL DEFAULT '0.0000' COMMENT '默认相似度阈值',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0=禁用 1=启用)',
  `document_count` int NOT NULL DEFAULT '0' COMMENT '文档数量',
  `chunk_count` int NOT NULL DEFAULT '0' COMMENT '切片数量',
  `last_parsed_at` datetime DEFAULT NULL COMMENT '最近文档解析时刻',
  `last_retrieved_at` datetime DEFAULT NULL COMMENT '最近检索时刻',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_ai_knowledge_base_tenant_name` (`tenant_id`, `name`) USING BTREE,
  KEY `idx_ai_knowledge_base_tenant_status_updated` (`tenant_id`, `status`, `updated_at`) USING BTREE,
  KEY `idx_ai_knowledge_base_tenant_embedding` (`tenant_id`, `embedding_model_config_id`) USING BTREE,
  KEY `idx_ai_knowledge_base_tenant_last_retrieved` (`tenant_id`, `last_retrieved_at`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='AI知识库';

-- ----------------------------
-- Table structure for ai_knowledge_document
-- ----------------------------
DROP TABLE IF EXISTS `ai_knowledge_document`;
CREATE TABLE `ai_knowledge_document` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
  `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  `created_at` datetime NOT NULL COMMENT '创建时刻',
  `created_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建者',
  `updated_at` datetime NOT NULL COMMENT '更新时刻',
  `updated_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新者',
  `knowledge_base_id` bigint NOT NULL COMMENT '知识库ID',
  `oss_file_id` bigint NOT NULL COMMENT 'OSS文件ID',
  `document_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文档名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '文档描述',
  `original_filename` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '原始文件名',
  `extend_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '扩展名',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小',
  `md5` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'MD5',
  `storage_platform` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '存储平台',
  `parse_status` tinyint NOT NULL DEFAULT '3' COMMENT '解析状态(0=失败 1=成功 2=处理中 3=待处理)',
  `chunk_status` tinyint NOT NULL DEFAULT '3' COMMENT '切片状态(0=失败 1=成功 2=处理中 3=待处理)',
  `embedding_status` tinyint NOT NULL DEFAULT '3' COMMENT '向量化状态(0=失败 1=成功 2=处理中 3=待处理)',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0=禁用 1=启用)',
  `chunk_count` int NOT NULL DEFAULT '0' COMMENT '切片数量',
  `parse_error_message` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '解析失败原因',
  `chunk_error_message` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '切片失败原因',
  `retry_count` int NOT NULL DEFAULT '0' COMMENT '重试次数',
  `parsed_at` datetime DEFAULT NULL COMMENT '最近解析时刻',
  `chunked_at` datetime DEFAULT NULL COMMENT '最近切片时刻',
  `last_retry_at` datetime DEFAULT NULL COMMENT '最近重试时刻',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_ai_knowledge_document_tenant_base_oss` (`tenant_id`, `knowledge_base_id`, `oss_file_id`) USING BTREE,
  KEY `idx_ai_knowledge_document_tenant_base_status` (`tenant_id`, `knowledge_base_id`, `status`) USING BTREE,
  KEY `idx_ai_knowledge_document_tenant_oss` (`tenant_id`, `oss_file_id`) USING BTREE,
  KEY `idx_ai_knowledge_document_tenant_parse` (`tenant_id`, `parse_status`, `updated_at`) USING BTREE,
  KEY `idx_ai_knowledge_document_tenant_chunk` (`tenant_id`, `chunk_status`, `updated_at`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='AI知识库文档';

-- ----------------------------
-- Table structure for ai_knowledge_document_chunk
-- ----------------------------
DROP TABLE IF EXISTS `ai_knowledge_document_chunk`;
CREATE TABLE `ai_knowledge_document_chunk` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
  `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  `created_at` datetime NOT NULL COMMENT '创建时刻',
  `created_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建者',
  `updated_at` datetime NOT NULL COMMENT '更新时刻',
  `updated_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新者',
  `knowledge_base_id` bigint NOT NULL COMMENT '知识库ID',
  `document_id` bigint NOT NULL COMMENT '文档ID',
  `chunk_no` int NOT NULL COMMENT '切片序号',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '切片内容',
  `content_preview` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '切片内容预览',
  `source_page` int DEFAULT NULL COMMENT '来源页码',
  `source_position` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '来源定位',
  `token_count` int DEFAULT NULL COMMENT '预估Token数',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '切片状态(0=失败 1=成功 2=处理中)',
  `embedding_status` tinyint NOT NULL DEFAULT '3' COMMENT '向量化状态(0=失败 1=成功 2=处理中 3=待处理)',
  `embedding_model_config_id` bigint DEFAULT NULL COMMENT '向量化模型配置ID',
  `embedding_model_config_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '向量化模型配置编码',
  `embedding_provider_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '向量化供应商类型',
  `embedding_model_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '向量化模型名称',
  `vector_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '向量ID',
  `vector_hash` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '向量内容哈希',
  `error_message` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '错误信息',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_ai_knowledge_chunk_tenant_document_no` (`tenant_id`, `document_id`, `chunk_no`) USING BTREE,
  KEY `idx_ai_knowledge_chunk_tenant_base_status` (`tenant_id`, `knowledge_base_id`, `status`) USING BTREE,
  KEY `idx_ai_knowledge_chunk_tenant_document` (`tenant_id`, `document_id`) USING BTREE,
  KEY `idx_ai_knowledge_chunk_tenant_embedding` (`tenant_id`, `embedding_status`, `updated_at`) USING BTREE,
  KEY `idx_ai_knowledge_chunk_tenant_vector` (`tenant_id`, `vector_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='AI知识库文档切片';

-- ----------------------------
-- Table structure for ai_knowledge_retrieval_log
-- ----------------------------
DROP TABLE IF EXISTS `ai_knowledge_retrieval_log`;
CREATE TABLE `ai_knowledge_retrieval_log` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
  `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  `created_at` datetime NOT NULL COMMENT '创建时刻',
  `created_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建者',
  `updated_at` datetime NOT NULL COMMENT '更新时刻',
  `updated_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新者',
  `retrieval_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务检索ID',
  `user_id` bigint DEFAULT NULL COMMENT '后台用户ID',
  `knowledge_base_ids` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '知识库ID列表',
  `conversation_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '业务会话ID',
  `message_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '业务消息ID',
  `query_text` varchar(4000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '查询内容',
  `top_k` int NOT NULL DEFAULT '5' COMMENT '召回数量',
  `similarity_threshold` decimal(5,4) DEFAULT NULL COMMENT '相似度阈值',
  `hit_count` int NOT NULL DEFAULT '0' COMMENT '命中数量',
  `hits_summary` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '召回摘要',
  `elapsed_ms` bigint DEFAULT NULL COMMENT '耗时，单位毫秒',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0=失败 1=成功)',
  `error_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '错误编码',
  `error_message` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '错误信息',
  `retrieved_at` datetime NOT NULL COMMENT '检索时刻',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_ai_knowledge_retrieval_tenant_retrieval` (`tenant_id`, `retrieval_id`) USING BTREE,
  KEY `idx_ai_knowledge_retrieval_tenant_conversation` (`tenant_id`, `conversation_id`, `retrieved_at`) USING BTREE,
  KEY `idx_ai_knowledge_retrieval_tenant_message` (`tenant_id`, `message_id`) USING BTREE,
  KEY `idx_ai_knowledge_retrieval_tenant_user` (`tenant_id`, `user_id`, `retrieved_at`) USING BTREE,
  KEY `idx_ai_knowledge_retrieval_tenant_status` (`tenant_id`, `status`, `retrieved_at`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='AI知识库检索日志';

-- ----------------------------
-- Records of ai_model_config
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
