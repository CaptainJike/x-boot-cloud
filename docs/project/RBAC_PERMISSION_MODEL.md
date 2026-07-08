# RBAC 权限模型确认

最后更新：2026-06-24

## 结论

MVP 阶段继续使用 `sys_menu.permission` 作为后台权限码唯一承载字段，不新增独立 `sys_permission` 表。

当前权限模型已经满足后台菜单、按钮、角色授权、登录权限缓存和 Sa-Token 注解校验的闭环：

`@SaCheckPermission` -> `sys_menu.permission` -> `sys_role_menu_relation` -> `SysMenuService#getRoleIdPermissionMap` -> `rolePermissionCacheHelper`

## 现有证据

- `admin-api` 的后台接口使用 `@SaCheckPermission` 声明权限码，权限码格式为 `<资源前缀>:<动作>`，例如 `SysUser:retrieve`。
- `docs/project/RBAC_PERMISSION_CODES.md` 已从当前 Controller 整理出 62 个唯一后台权限码。
- `docs/project/RBAC_INIT_SQL.sql` 已为这些权限码生成菜单和按钮节点，目录节点不配置权限码。
- `SysMenuService#getRoleIdPermissionMap` 使用角色关联菜单读取 `sys_menu.permission`，并过滤空权限码。
- `SysRoleService#adminBindMenus` 绑定角色菜单时通过 `SysMenuService#listPermissionsByMenuIds` 返回新权限集合，供 API 层刷新角色权限缓存。
- `AdminAuthController` 登录成功后把 `SysUserLoginBO.roleIdPermissionMap` 写入 Sa-Token 权限缓存。

## MVP 扩展规则

- 新增后台页面时，新增 `MENU` 节点，`permission` 使用该资源的 `retrieve` 权限码。
- 新增页内操作时，新增 `BUTTON` 节点，`permission` 使用对应动作权限码。
- 新增无侧边菜单入口的后台动作时，仍建模为 `BUTTON` 节点，挂在最接近的 `MENU` 节点下；侧边菜单不会展示 `BUTTON`。
- 新增权限码时必须同步更新 Controller `@SaCheckPermission`、`docs/project/RBAC_PERMISSION_CODES.md` 和初始化 SQL。
- 角色授权必须同时绑定页面 `MENU` 和需要使用的 `BUTTON`，否则用户可能能进入页面但无法执行页内动作。
- 数据范围、租户边界、超级管理员/租户管理员不可操作规则继续放在 service 层，不放进 `sys_menu.permission`。

## 当前约束

- 一个 `sys_menu` 节点只承载一个权限码；同一页面需要多个动作权限时，用多个 `BUTTON` 节点表达。
- 权限码唯一性当前由 `SysMenuService#checkExistence` 在写入时校验；数据库层暂未增加唯一索引。
- 非超级管理员只从已绑定且启用的菜单节点读取权限；超级管理员角色仍通过 `orRole = "SuperAdmin"` 绕过普通权限码校验。
- `sys_menu` 继承租户字段；初始化 SQL 只覆盖默认特权租户 `tenant_id = 0` 的基础权限数据，新租户仍需要由超级管理员授权或后续复制基础菜单关系。
- 权限码不承载字段级权限、数据权限表达式、审批策略或动态 ABAC 条件。

## 暂不新增 `sys_permission` 表的原因

- 当前权限数量较小，62 个唯一权限码可以直接由菜单和按钮节点表达。
- 后台权限与 UI 菜单/按钮强相关，拆表会引入额外同步关系和初始化复杂度。
- 当前链路已经支持角色-菜单绑定、权限缓存刷新、登录权限返回和 `SuperAdmin` 兜底。
- MVP 后续重点是 AI 会话、知识库/RAG、Agent/工作流和工具/MCP，过早拆 RBAC 权限表会增加迁移成本但收益有限。

## 后续拆表触发条件

只有出现以下需求时，再设计独立 `sys_permission` 表：

- 权限大量脱离后台菜单和按钮，需要独立生命周期、分类、上下架或批量导入导出。
- 同一个菜单节点需要绑定多个底层权限，且多个角色需要复用不同权限组合。
- 需要权限元数据，例如风险等级、审计类别、国际化名称、外部系统映射或可见范围。
- 需要支持跨端权限市场，例如后台、app、开放 API、MCP 工具共用同一套权限实体。
- 需要表达字段级权限、数据范围策略、ABAC 条件或复杂 `AND/OR` 权限组合。

## 验收状态

- `SysMenuServiceTest` 已覆盖超级管理员读取所有非空菜单权限、普通角色读取绑定菜单权限、菜单 ID 反查权限集合。
- `docs/project/RBAC_INIT_SQL.sql` 已覆盖当前 62 个后台权限码、65 个菜单节点，其中 62 个权限节点和 3 个目录节点。
- MVP 阶段结论：`sys_menu.permission` 满足后续阶段基础扩展，暂不新增独立权限表。
