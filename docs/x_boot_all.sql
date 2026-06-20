-- x_boot.oss_file_info definition

CREATE TABLE `oss_file_info` (
                                 `id` bigint NOT NULL COMMENT '主键ID',
                                 `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
                                 `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
                                 `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
                                 `created_at` datetime NOT NULL COMMENT '创建时刻',
                                 `created_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建者',
                                 `updated_at` datetime NOT NULL COMMENT '更新时刻',
                                 `updated_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新者',
                                 `storage_platform` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '存储平台',
                                 `storage_base_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '基础存储路径',
                                 `storage_path` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '存储路径',
                                 `storage_filename` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '存储文件名',
                                 `original_filename` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '原始文件名',
                                 `extend_name` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '扩展名',
                                 `file_size` bigint NOT NULL COMMENT '文件大小',
                                 `md5` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'MD5',
                                 `classified` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '文件类别',
                                 `direct_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '对象存储直链',
                                 PRIMARY KEY (`id`)
                                     USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC COMMENT = '上传文件信息';
-- x_boot.sys_data_dict_classified definition

CREATE TABLE `sys_data_dict_classified` (
                                            `id` bigint NOT NULL COMMENT '主键ID',
                                            `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
                                            `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
                                            `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
                                            `created_at` datetime NOT NULL COMMENT '创建时刻',
                                            `created_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建者',
                                            `updated_at` datetime NOT NULL COMMENT '更新时刻',
                                            `updated_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新者',
                                            `code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类编码',
                                            `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
                                            `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态',
                                            `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '分类描述',
                                            PRIMARY KEY (`id`)
                                                USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC COMMENT = '数据字典分类';
-- x_boot.sys_data_dict_item definition

CREATE TABLE `sys_data_dict_item` (
                                      `id` bigint NOT NULL COMMENT '主键ID',
                                      `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
                                      `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
                                      `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
                                      `created_at` datetime NOT NULL COMMENT '创建时刻',
                                      `created_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建者',
                                      `updated_at` datetime NOT NULL COMMENT '更新时刻',
                                      `updated_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新者',
                                      `classified_id` bigint NOT NULL COMMENT '所属分类ID',
                                      `code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典项编码',
                                      `label` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典项标签',
                                      `value` varchar(4096) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典项值',
                                      `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态',
                                      `sort` int NOT NULL DEFAULT '1' COMMENT '排序',
                                      `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '描述',
                                      PRIMARY KEY (`id`)
                                          USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC COMMENT = '数据字典项';
-- x_boot.sys_dept definition

CREATE TABLE `sys_dept` (
                            `id` bigint NOT NULL COMMENT '主键ID',
                            `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
                            `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
                            `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
                            `created_at` datetime NOT NULL COMMENT '创建时刻',
                            `created_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建者',
                            `updated_at` datetime NOT NULL COMMENT '更新时刻',
                            `updated_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新者',
                            `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '名称',
                            `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '上级ID(根节点设置为0)',
                            `sort` int NOT NULL DEFAULT '1' COMMENT '排序',
                            `status` int NOT NULL DEFAULT '0' COMMENT '状态(0=禁用 1=启用)',
                            PRIMARY KEY (`id`)
                                USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC COMMENT = '部门';
-- x_boot.sys_log definition

CREATE TABLE `sys_log` (
                           `id` bigint NOT NULL COMMENT '主键ID',
                           `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
                           `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
                           `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
                           `created_at` datetime NOT NULL COMMENT '创建时刻',
                           `created_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建者',
                           `updated_at` datetime NOT NULL COMMENT '更新时刻',
                           `updated_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新者',
                           `user_id` bigint DEFAULT NULL COMMENT '用户ID',
                           `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '用户账号',
                           `operation` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '操作内容',
                           `method` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '请求方法',
                           `params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '请求参数',
                           `ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'IP地址',
                           `status` int NOT NULL DEFAULT '0' COMMENT '状态(参考SysLogStatusEnum)',
                           `error_stacktrace` varchar(3000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '错误原因堆栈',
                           `user_agent` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '用户UA',
                           `ip_location_region_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'IP地址属地-国家或地区名',
                           `ip_location_province_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'IP地址属地-省级行政区名',
                           `ip_location_city_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'IP地址属地-市级行政区名',
                           `ip_location_district_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'IP地址属地-县级行政区名',
                           PRIMARY KEY (`id`)
                               USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC COMMENT = '系统日志';
-- x_boot.sys_menu definition

CREATE TABLE `sys_menu` (
                            `id` bigint NOT NULL COMMENT '主键ID',
                            `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
                            `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
                            `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
                            `created_at` datetime NOT NULL COMMENT '创建时刻',
                            `created_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建者',
                            `updated_at` datetime NOT NULL COMMENT '更新时刻',
                            `updated_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新者',
                            `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '名称',
                            `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '上级菜单ID(根节点设置为0)',
                            `type` int NOT NULL DEFAULT '1' COMMENT '菜单类型(参考MenuTypeEnum)',
                            `permission` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '权限标识',
                            `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '图标',
                            `sort` int DEFAULT '1' COMMENT '排序',
                            `status` int NOT NULL DEFAULT '0' COMMENT '状态(0=禁用 1=启用)',
                            `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '组件',
                            `external_link` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '外链地址',
                            PRIMARY KEY (`id`)
                                USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC COMMENT = '后台菜单';
-- x_boot.sys_param definition

CREATE TABLE `sys_param` (
                             `id` bigint NOT NULL COMMENT '主键ID',
                             `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
                             `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
                             `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
                             `created_at` datetime NOT NULL COMMENT '创建时刻',
                             `created_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建者',
                             `updated_at` datetime NOT NULL COMMENT '更新时刻',
                             `updated_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新者',
                             `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '键名',
                             `value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '键值',
                             `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '描述',
                             PRIMARY KEY (`id`)
                                 USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC COMMENT = '系统参数';
-- x_boot.sys_role definition

CREATE TABLE `sys_role` (
                            `id` bigint NOT NULL COMMENT '主键ID',
                            `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
                            `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
                            `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
                            `created_at` datetime NOT NULL COMMENT '创建时刻',
                            `created_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建者',
                            `updated_at` datetime NOT NULL COMMENT '更新时刻',
                            `updated_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新者',
                            `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '名称',
                            `value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '值',
                            PRIMARY KEY (`id`)
                                USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC COMMENT = '后台角色';
-- x_boot.sys_role_menu_relation definition

CREATE TABLE `sys_role_menu_relation` (
                                          `id` bigint NOT NULL COMMENT '主键ID',
                                          `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
                                          `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
                                          `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
                                          `created_at` datetime NOT NULL COMMENT '创建时刻',
                                          `created_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建者',
                                          `updated_at` datetime NOT NULL COMMENT '更新时刻',
                                          `updated_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新者',
                                          `role_id` bigint NOT NULL COMMENT '角色ID',
                                          `menu_id` bigint NOT NULL COMMENT '菜单ID',
                                          PRIMARY KEY (`id`)
                                              USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC COMMENT = '后台角色-可见菜单关联';
-- x_boot.sys_tenant definition

CREATE TABLE `sys_tenant` (
                              `id` bigint NOT NULL COMMENT '主键ID',
                              `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
                              `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
                              `created_at` datetime NOT NULL COMMENT '创建时刻',
                              `created_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建者',
                              `updated_at` datetime NOT NULL COMMENT '更新时刻',
                              `updated_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新者',
                              `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
                              `tenant_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '租户名',
                              `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
                              `status` int NOT NULL DEFAULT '0' COMMENT '状态(0=禁用 1=启用)',
                              `tenant_admin_user_id` bigint DEFAULT NULL COMMENT '租户管理员用户ID',
                              PRIMARY KEY (`id`)
                                  USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC COMMENT = '系统租户';
-- x_boot.sys_user definition

CREATE TABLE `sys_user` (
                            `id` bigint NOT NULL COMMENT '主键ID',
                            `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
                            `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
                            `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
                            `created_at` datetime NOT NULL COMMENT '创建时刻',
                            `created_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建者',
                            `updated_at` datetime NOT NULL COMMENT '更新时刻',
                            `updated_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新者',
                            `pin` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账号',
                            `pwd` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
                            `salt` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '盐',
                            `nickname` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '昵称',
                            `status` int NOT NULL DEFAULT '0' COMMENT '状态(0=禁用 1=启用)',
                            `gender` int DEFAULT '0' COMMENT '性别',
                            `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '邮箱',
                            `phone_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '手机号',
                            `last_login_at` datetime DEFAULT NULL COMMENT '最后登录时刻',
                            `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '头像URL',
                            PRIMARY KEY (`id`)
                                USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC COMMENT = '后台用户';
-- x_boot.sys_user_dept_relation definition

CREATE TABLE `sys_user_dept_relation` (
                                          `id` bigint NOT NULL COMMENT '主键ID',
                                          `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
                                          `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
                                          `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
                                          `created_at` datetime NOT NULL COMMENT '创建时刻',
                                          `created_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建者',
                                          `updated_at` datetime NOT NULL COMMENT '更新时刻',
                                          `updated_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新者',
                                          `user_id` bigint NOT NULL COMMENT '用户ID',
                                          `dept_id` bigint NOT NULL COMMENT '部门ID',
                                          PRIMARY KEY (`id`)
                                              USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC COMMENT = '后台用户-部门关联';
-- x_boot.sys_user_role_relation definition

CREATE TABLE `sys_user_role_relation` (
                                          `id` bigint NOT NULL COMMENT '主键ID',
                                          `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
                                          `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
                                          `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
                                          `created_at` datetime NOT NULL COMMENT '创建时刻',
                                          `created_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建者',
                                          `updated_at` datetime NOT NULL COMMENT '更新时刻',
                                          `updated_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新者',
                                          `user_id` bigint NOT NULL COMMENT '用户ID',
                                          `role_id` bigint NOT NULL COMMENT '角色ID',
                                          PRIMARY KEY (`id`)
                                              USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC COMMENT = '后台用户-角色关联';
-- x_boot.`数据表模板` definition

CREATE TABLE `数据表模板` (
                              `id` bigint NOT NULL COMMENT '主键ID',
                              `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
                              `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
                              `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
                              `created_at` datetime NOT NULL COMMENT '创建时刻',
                              `created_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建者',
                              `updated_at` datetime NOT NULL COMMENT '更新时刻',
                              `updated_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新者',
                              PRIMARY KEY (`id`)
                                  USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC COMMENT = '数据表注释';

-- x_boot.ai_model_config definition

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
                                   `temperature` decimal(4, 2) DEFAULT NULL COMMENT '温度参数',
                                   `timeout_seconds` bigint DEFAULT NULL COMMENT '调用超时时间，单位秒',
                                   `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0=禁用 1=启用)',
                                   `default_flag` tinyint NOT NULL DEFAULT '0' COMMENT '是否默认配置(0=否 1=是)',
                                   `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '描述',
                                   PRIMARY KEY (`id`)
                                       USING BTREE,
                                   UNIQUE KEY `uk_ai_model_config_tenant_code` (`tenant_id`,
                                                                                `code`)
                                       USING BTREE,
                                   KEY `idx_ai_model_config_tenant_default` (`tenant_id`,
                                                                             `default_flag`,
                                                                             `status`)
                                       USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC COMMENT = 'AI模型配置';
