-- x-boot-cloud RBAC bootstrap data.
-- Target database: MySQL 8.x.
-- Scope: default privileged tenant(tenant_id = 0), SuperAdmin role, tenant Admin role,
-- base backend menus and button permissions from docs/project/RBAC_PERMISSION_CODES.md.
--
-- Security note:
--   Seed users are only for first-run bootstrap. Change passwords immediately after import.
--   Password hash rule follows current PwdUtil usage:
--   sha256(salt + md5(md5(raw_password)) + salt).
--   admin       / admin123456
--   tenantadmin / tenant123456
--
-- ID ranges:
--   1-99       tenant, users, roles
--   1000-1999  system management menus/buttons
--   2000-2999  AI management menus/buttons
--   3000-3999  file management menus/buttons
--   910000+    SuperAdmin role-menu relations
--   920000+    tenant Admin role-menu relations

SET @seed_time := NOW();

INSERT INTO `sys_tenant` (
    `id`, `revision`, `del_flag`, `created_at`, `created_by`, `updated_at`, `updated_by`,
    `remark`, `tenant_name`, `tenant_id`, `status`, `tenant_admin_user_id`
) VALUES
    (1, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     'RBAC bootstrap privileged tenant', '平台默认租户', 0, 1, 2)
ON DUPLICATE KEY UPDATE
    `revision` = VALUES(`revision`),
    `del_flag` = VALUES(`del_flag`),
    `updated_at` = VALUES(`updated_at`),
    `updated_by` = VALUES(`updated_by`),
    `remark` = VALUES(`remark`),
    `tenant_name` = VALUES(`tenant_name`),
    `tenant_id` = VALUES(`tenant_id`),
    `status` = VALUES(`status`),
    `tenant_admin_user_id` = VALUES(`tenant_admin_user_id`);

INSERT INTO `sys_role` (
    `id`, `tenant_id`, `revision`, `del_flag`, `created_at`, `created_by`, `updated_at`, `updated_by`,
    `title`, `value`
) VALUES
    (1, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init', '超级管理员', 'SuperAdmin'),
    (2, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init', '租户管理员', 'Admin')
ON DUPLICATE KEY UPDATE
    `tenant_id` = VALUES(`tenant_id`),
    `revision` = VALUES(`revision`),
    `del_flag` = VALUES(`del_flag`),
    `updated_at` = VALUES(`updated_at`),
    `updated_by` = VALUES(`updated_by`),
    `title` = VALUES(`title`),
    `value` = VALUES(`value`);

INSERT INTO `sys_user` (
    `id`, `tenant_id`, `revision`, `del_flag`, `created_at`, `created_by`, `updated_at`, `updated_by`,
    `pin`, `pwd`, `salt`, `nickname`, `status`, `gender`, `email`, `phone_no`, `last_login_at`, `avatar_url`
) VALUES
    (1, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     'admin',
     'f391860d7f041658988b033a77b33ff60d7590b76830e4a3cbf3f2ddb040682f',
     'xbootcloudadminsalt0000000000000001',
     '超级管理员', 1, 0, 'admin@example.com', '13800000000', NULL, NULL),
    (2, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     'tenantadmin',
     '3816a2b9ad3f9ed26ef50250aac3dc07c41c55c8afabdbccb48b755988974be9',
     'xbootcloudtenantsalt000000000000001',
     '租户管理员', 1, 0, 'tenantadmin@example.com', '13800000001', NULL, NULL)
ON DUPLICATE KEY UPDATE
    `tenant_id` = VALUES(`tenant_id`),
    `revision` = VALUES(`revision`),
    `del_flag` = VALUES(`del_flag`),
    `updated_at` = VALUES(`updated_at`),
    `updated_by` = VALUES(`updated_by`),
    `pin` = VALUES(`pin`),
    `pwd` = VALUES(`pwd`),
    `salt` = VALUES(`salt`),
    `nickname` = VALUES(`nickname`),
    `status` = VALUES(`status`),
    `gender` = VALUES(`gender`),
    `email` = VALUES(`email`),
    `phone_no` = VALUES(`phone_no`);

INSERT INTO `sys_user_role_relation` (
    `id`, `tenant_id`, `revision`, `del_flag`, `created_at`, `created_by`, `updated_at`, `updated_by`,
    `user_id`, `role_id`
) VALUES
    (101, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init', 1, 1),
    (102, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init', 2, 2)
ON DUPLICATE KEY UPDATE
    `tenant_id` = VALUES(`tenant_id`),
    `revision` = VALUES(`revision`),
    `del_flag` = VALUES(`del_flag`),
    `updated_at` = VALUES(`updated_at`),
    `updated_by` = VALUES(`updated_by`),
    `user_id` = VALUES(`user_id`),
    `role_id` = VALUES(`role_id`);

INSERT INTO `sys_menu` (
    `id`, `tenant_id`, `revision`, `del_flag`, `created_at`, `created_by`, `updated_at`, `updated_by`,
    `title`, `parent_id`, `type`, `permission`, `icon`, `sort`, `status`, `component`, `external_link`
) VALUES
    (1000, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '系统管理', 0, 0, '', 'ion:settings-outline', 10, 1, 'LAYOUT', ''),
    (1100, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '用户管理', 1000, 1, 'SysUser:retrieve', 'ri:user-settings-line', 10, 1, 'sys/user/index', ''),
    (1101, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '新增用户', 1100, 2, 'SysUser:create', NULL, 10, 1, NULL, ''),
    (1102, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '编辑用户', 1100, 2, 'SysUser:update', NULL, 20, 1, NULL, ''),
    (1103, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '删除用户', 1100, 2, 'SysUser:delete', NULL, 30, 1, NULL, ''),
    (1104, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '重置密码', 1100, 2, 'SysUser:resetPassword', NULL, 40, 1, NULL, ''),
    (1105, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '绑定角色', 1100, 2, 'SysUser:bindRoles', NULL, 50, 1, NULL, ''),
    (1106, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '踢下线', 1100, 2, 'SysUser:kickOut', NULL, 60, 1, NULL, ''),
    (1200, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '角色管理', 1000, 1, 'SysRole:retrieve', 'ri:shield-user-line', 20, 1, 'sys/role/index', ''),
    (1201, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '新增角色', 1200, 2, 'SysRole:create', NULL, 10, 1, NULL, ''),
    (1202, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '编辑角色', 1200, 2, 'SysRole:update', NULL, 20, 1, NULL, ''),
    (1203, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '删除角色', 1200, 2, 'SysRole:delete', NULL, 30, 1, NULL, ''),
    (1204, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '绑定菜单', 1200, 2, 'SysRole:bindMenus', NULL, 40, 1, NULL, ''),
    (1300, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '菜单管理', 1000, 1, 'SysMenu:retrieve', 'ri:menu-search-line', 30, 1, 'sys/menu/index', ''),
    (1301, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '新增菜单', 1300, 2, 'SysMenu:create', NULL, 10, 1, NULL, ''),
    (1302, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '编辑菜单', 1300, 2, 'SysMenu:update', NULL, 20, 1, NULL, ''),
    (1303, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '删除菜单', 1300, 2, 'SysMenu:delete', NULL, 30, 1, NULL, ''),
    (1400, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '部门管理', 1000, 1, 'SysDept:retrieve', 'ri:organization-chart', 40, 1, 'sys/dept/index', ''),
    (1401, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '新增部门', 1400, 2, 'SysDept:create', NULL, 10, 1, NULL, ''),
    (1402, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '编辑部门', 1400, 2, 'SysDept:update', NULL, 20, 1, NULL, ''),
    (1403, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '删除部门', 1400, 2, 'SysDept:delete', NULL, 30, 1, NULL, ''),
    (1500, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '租户管理', 1000, 1, 'SysTenant:retrieve', 'ri:building-4-line', 50, 1, 'sys/tenant/index', ''),
    (1501, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '新增租户', 1500, 2, 'SysTenant:create', NULL, 10, 1, NULL, ''),
    (1502, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '编辑租户', 1500, 2, 'SysTenant:update', NULL, 20, 1, NULL, ''),
    (1503, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '删除租户', 1500, 2, 'SysTenant:delete', NULL, 30, 1, NULL, ''),
    (1600, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '系统参数', 1000, 1, 'SysParam:retrieve', 'ri:settings-3-line', 60, 1, 'sys/param/index', ''),
    (1601, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '新增参数', 1600, 2, 'SysParam:create', NULL, 10, 1, NULL, ''),
    (1602, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '编辑参数', 1600, 2, 'SysParam:update', NULL, 20, 1, NULL, ''),
    (1603, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '删除参数', 1600, 2, 'SysParam:delete', NULL, 30, 1, NULL, ''),
    (1700, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '数据字典', 1000, 1, 'SysDataDict:retrieve', 'ri:book-2-line', 70, 1, 'sys/data-dict/index', ''),
    (1701, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '新增字典', 1700, 2, 'SysDataDict:create', NULL, 10, 1, NULL, ''),
    (1702, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '编辑字典', 1700, 2, 'SysDataDict:update', NULL, 20, 1, NULL, ''),
    (1703, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '删除字典', 1700, 2, 'SysDataDict:delete', NULL, 30, 1, NULL, ''),
    (1800, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '系统日志', 1000, 1, 'SysLog:retrieve', 'ri:file-list-3-line', 80, 1, 'sys/log/index', ''),
    (2000, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     'AI 管理', 0, 0, '', 'ri:robot-2-line', 20, 1, 'LAYOUT', ''),
    (2100, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '模型配置', 2000, 1, 'AiModelConfig:retrieve', 'ri:openai-line', 10, 1, 'ai/model-config/index', ''),
    (2101, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '新增模型配置', 2100, 2, 'AiModelConfig:create', NULL, 10, 1, NULL, ''),
    (2102, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '编辑模型配置', 2100, 2, 'AiModelConfig:update', NULL, 20, 1, NULL, ''),
    (2103, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '删除模型配置', 2100, 2, 'AiModelConfig:delete', NULL, 30, 1, NULL, ''),
    (2104, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '查看 API Key', 2100, 2, 'AiModelConfig:key', NULL, 40, 1, NULL, ''),
    (2105, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '检测模型配置', 2100, 2, 'AiModelConfig:test', NULL, 50, 1, NULL, ''),
    (2106, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '查询供应商模型', 2100, 2, 'AiModelConfig:models', NULL, 60, 1, NULL, ''),
    (2200, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     'AI 对话', 2000, 1, 'AiChat:retrieve', 'ri:message-3-line', 20, 1, 'ai/conversation/index', ''),
    (2201, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '普通对话', 2200, 2, 'AiChat:chat', NULL, 10, 1, NULL, ''),
    (2202, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '流式对话', 2200, 2, 'AiChat:stream', NULL, 20, 1, NULL, ''),
    (2300, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     'AI 知识库', 2000, 1, 'AiKnowledge:retrieve', 'ri:book-read-line', 30, 1, 'ai/knowledge/index', ''),
    (2301, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '新增知识库', 2300, 2, 'AiKnowledge:create', NULL, 10, 1, NULL, ''),
    (2302, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '编辑知识库', 2300, 2, 'AiKnowledge:update', NULL, 20, 1, NULL, ''),
    (2303, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '删除知识库', 2300, 2, 'AiKnowledge:delete', NULL, 30, 1, NULL, ''),
    (2304, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '启停知识库', 2300, 2, 'AiKnowledge:enable', NULL, 40, 1, NULL, ''),
    (2305, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '重试知识库文档', 2300, 2, 'AiKnowledge:retry', NULL, 50, 1, NULL, ''),
    (2400, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     'AI Agent', 2000, 1, 'AiAgent:retrieve', 'ri:robot-line', 40, 1, 'ai/agent/index', ''),
    (2401, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '新增 Agent', 2400, 2, 'AiAgent:create', NULL, 10, 1, NULL, ''),
    (2402, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '编辑 Agent', 2400, 2, 'AiAgent:update', NULL, 20, 1, NULL, ''),
    (2403, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '删除 Agent', 2400, 2, 'AiAgent:delete', NULL, 30, 1, NULL, ''),
    (2404, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '启停 Agent', 2400, 2, 'AiAgent:enable', NULL, 40, 1, NULL, ''),
    (2500, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     'AI 工作流', 2000, 1, 'AiWorkflow:retrieve', 'ri:flow-chart', 50, 1, 'ai/workflow/index', ''),
    (2501, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '新增工作流', 2500, 2, 'AiWorkflow:create', NULL, 10, 1, NULL, ''),
    (2502, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '编辑工作流', 2500, 2, 'AiWorkflow:update', NULL, 20, 1, NULL, ''),
    (2503, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '删除工作流', 2500, 2, 'AiWorkflow:delete', NULL, 30, 1, NULL, ''),
    (2504, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '启停工作流', 2500, 2, 'AiWorkflow:enable', NULL, 40, 1, NULL, ''),
    (2505, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '执行工作流', 2500, 2, 'AiWorkflow:execute', NULL, 50, 1, NULL, ''),
    (3000, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '文件管理', 0, 0, '', 'ri:folder-upload-line', 30, 1, 'LAYOUT', ''),
    (3100, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     'OSS 文件信息', 3000, 1, 'OssFileInfo:retrieve', 'ri:file-cloud-line', 10, 1, 'oss/file-info/index', ''),
    (3101, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
     '删除 OSS 文件信息', 3100, 2, 'OssFileInfo:delete', NULL, 10, 1, NULL, '')
ON DUPLICATE KEY UPDATE
    `tenant_id` = VALUES(`tenant_id`),
    `revision` = VALUES(`revision`),
    `del_flag` = VALUES(`del_flag`),
    `updated_at` = VALUES(`updated_at`),
    `updated_by` = VALUES(`updated_by`),
    `title` = VALUES(`title`),
    `parent_id` = VALUES(`parent_id`),
    `type` = VALUES(`type`),
    `permission` = VALUES(`permission`),
    `icon` = VALUES(`icon`),
    `sort` = VALUES(`sort`),
    `status` = VALUES(`status`),
    `component` = VALUES(`component`),
    `external_link` = VALUES(`external_link`);

-- SuperAdmin bypasses permission annotation by role, but binding all menus keeps
-- visible-menu data complete for bootstrap inspection and future UI assumptions.
INSERT INTO `sys_role_menu_relation` (
    `id`, `tenant_id`, `revision`, `del_flag`, `created_at`, `created_by`, `updated_at`, `updated_by`,
    `role_id`, `menu_id`
)
SELECT
    910000 + `id`, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
    1, `id`
FROM `sys_menu`
WHERE `id` IN (
    1000, 1100, 1101, 1102, 1103, 1104, 1105, 1106,
    1200, 1201, 1202, 1203, 1204,
    1300, 1301, 1302, 1303,
    1400, 1401, 1402, 1403,
    1500, 1501, 1502, 1503,
    1600, 1601, 1602, 1603,
    1700, 1701, 1702, 1703,
    1800,
    2000, 2100, 2101, 2102, 2103, 2104, 2105, 2106,
    2200, 2201, 2202,
    2300, 2301, 2302, 2303, 2304, 2305,
    2400, 2401, 2402, 2403, 2404,
    2500, 2501, 2502, 2503, 2504, 2505,
    3000, 3100, 3101
)
ON DUPLICATE KEY UPDATE
    `tenant_id` = VALUES(`tenant_id`),
    `revision` = VALUES(`revision`),
    `del_flag` = VALUES(`del_flag`),
    `updated_at` = VALUES(`updated_at`),
    `updated_by` = VALUES(`updated_by`),
    `role_id` = VALUES(`role_id`),
    `menu_id` = VALUES(`menu_id`);

-- Tenant Admin gets tenant-operational menus by default. Platform tenant
-- management(SysTenant:*) remains SuperAdmin-only in the bootstrap baseline.
INSERT INTO `sys_role_menu_relation` (
    `id`, `tenant_id`, `revision`, `del_flag`, `created_at`, `created_by`, `updated_at`, `updated_by`,
    `role_id`, `menu_id`
)
SELECT
    920000 + `id`, 0, 1, 0, @seed_time, 'rbac-init', @seed_time, 'rbac-init',
    2, `id`
FROM `sys_menu`
WHERE `id` IN (
    1000, 1100, 1101, 1102, 1103, 1104, 1105, 1106,
    1200, 1201, 1202, 1203, 1204,
    1300, 1301, 1302, 1303,
    1400, 1401, 1402, 1403,
    1600, 1601, 1602, 1603,
    1700, 1701, 1702, 1703,
    1800,
    2000, 2100, 2101, 2102, 2103, 2104, 2105, 2106,
    2200, 2201, 2202,
    2300, 2301, 2302, 2303, 2304, 2305,
    2400, 2401, 2402, 2403, 2404,
    2500, 2501, 2502, 2503, 2504, 2505,
    3000, 3100, 3101
)
ON DUPLICATE KEY UPDATE
    `tenant_id` = VALUES(`tenant_id`),
    `revision` = VALUES(`revision`),
    `del_flag` = VALUES(`del_flag`),
    `updated_at` = VALUES(`updated_at`),
    `updated_by` = VALUES(`updated_by`),
    `role_id` = VALUES(`role_id`),
    `menu_id` = VALUES(`menu_id`);
