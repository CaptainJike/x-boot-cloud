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
-- Records of ai_model_config
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
