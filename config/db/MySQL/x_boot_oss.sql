/*
 Navicat MySQL Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80034 (8.0.34)
 Source Host           : localhost:3306
 Source Schema         : x_boot_oss

 Target Server Type    : MySQL
 Target Server Version : 80034 (8.0.34)
 File Encoding         : 65001

 Date: 11/10/2025 15:33:18
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for oss_file_info
-- ----------------------------
DROP TABLE IF EXISTS `oss_file_info`;
CREATE TABLE `oss_file_info`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  `revision` bigint NOT NULL DEFAULT 1 COMMENT '乐观锁',
  `del_flag` tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
  `created_at` datetime NOT NULL COMMENT '创建时刻',
  `created_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建者',
  `updated_at` datetime NOT NULL COMMENT '更新时刻',
  `updated_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新者',
  `storage_platform` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '存储平台',
  `storage_base_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '基础存储路径',
  `storage_path` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '存储路径',
  `storage_filename` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '存储文件名',
  `original_filename` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '原始文件名',
  `extend_name` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '扩展名',
  `file_size` bigint NOT NULL COMMENT '文件大小',
  `md5` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'MD5',
  `classified` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件类别',
  `direct_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '对象存储直链',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '上传文件信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of oss_file_info
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
