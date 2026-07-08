# RBAC 后台菜单与按钮权限码清单

最后更新：2026-06-24

## 来源与约定

- 本清单来源于 `admin-api` 当前 Controller 中的 `@SaCheckPermission` 注解和 `PERMISSION_PREFIX` 常量。
- 通用动作来自 `BaseConstant.Permission`：`create`、`retrieve`、`update`、`delete`。
- 现有后台权限码格式为 `<资源前缀>:<动作>`，例如 `SysUser:retrieve`。
- `@SaCheckPermission` 均配置 `orRole = "SuperAdmin"`，超级管理员可绕过普通权限码校验。
- `sys_menu.type` 使用 `SysMenuTypeEnum`：`DIR=0`、`MENU=1`、`BUTTON=2`、`EXTERNAL_LINK=3`。
- 建议菜单节点使用对应资源的 `retrieve` 权限码；页内动作创建为 `BUTTON` 节点。
- 当前用户资料、认证、下拉框、侧边菜单、上传下载等接口只要求登录或存在匿名/业务特殊性，不纳入按钮权限码初始化。

## 系统管理

### 用户管理

建议菜单节点：`系统管理 / 用户管理`

菜单权限码：`SysUser:retrieve`

| 权限码 | 类型 | 覆盖接口 | 说明 |
| --- | --- | --- | --- |
| `SysUser:retrieve` | MENU | `GET /sys/users`、`GET /sys/users/{id}`、`GET /sys/users/{userId}/roles` | 用户列表、详情、用户关联角色查询 |
| `SysUser:create` | BUTTON | `POST /sys/users` | 新增后台用户 |
| `SysUser:update` | BUTTON | `PUT /sys/users/{id}` | 编辑后台用户；禁用用户后会触发踢下线 |
| `SysUser:delete` | BUTTON | `DELETE /sys/users` | 删除后台用户；删除后会触发踢下线 |
| `SysUser:resetPassword` | BUTTON | `PUT /sys/users/{userId}/password` | 重置后台用户密码；重置后直接踢下线 |
| `SysUser:bindRoles` | BUTTON | `PUT /sys/users/{userId}/roles` | 绑定用户与角色；角色变更后会触发踢下线 |
| `SysUser:kickOut` | BUTTON | `POST /sys/users/{userId}:kick-out` | 手动踢用户下线 |

### 角色管理

建议菜单节点：`系统管理 / 角色管理`

菜单权限码：`SysRole:retrieve`

| 权限码 | 类型 | 覆盖接口 | 说明 |
| --- | --- | --- | --- |
| `SysRole:retrieve` | MENU | `GET /sys/roles`、`GET /sys/roles/{id}` | 角色列表、详情 |
| `SysRole:create` | BUTTON | `POST /sys/roles` | 新增后台角色 |
| `SysRole:update` | BUTTON | `PUT /sys/roles/{id}` | 编辑后台角色 |
| `SysRole:delete` | BUTTON | `DELETE /sys/roles` | 删除后台角色；删除后删除角色权限缓存 |
| `SysRole:bindMenus` | BUTTON | `PUT /sys/roles/{id}/menus` | 绑定角色与菜单；绑定后刷新角色权限缓存 |

### 菜单管理

建议菜单节点：`系统管理 / 菜单管理`

菜单权限码：`SysMenu:retrieve`

| 权限码 | 类型 | 覆盖接口 | 说明 |
| --- | --- | --- | --- |
| `SysMenu:retrieve` | MENU | `GET /sys/menus`、`GET /sys/menus/{id}` | 后台菜单列表、详情 |
| `SysMenu:create` | BUTTON | `POST /sys/menus` | 新增后台菜单 |
| `SysMenu:update` | BUTTON | `PUT /sys/menus/{id}` | 编辑后台菜单 |
| `SysMenu:delete` | BUTTON | `DELETE /sys/menus` | 删除后台菜单 |

### 部门管理

建议菜单节点：`系统管理 / 部门管理`

菜单权限码：`SysDept:retrieve`

| 权限码 | 类型 | 覆盖接口 | 说明 |
| --- | --- | --- | --- |
| `SysDept:retrieve` | MENU | `GET /sys/depts`、`GET /sys/depts/{id}` | 部门列表、详情 |
| `SysDept:create` | BUTTON | `POST /sys/depts` | 新增部门 |
| `SysDept:update` | BUTTON | `PUT /sys/depts/{id}` | 编辑部门 |
| `SysDept:delete` | BUTTON | `DELETE /sys/depts` | 删除部门 |

### 租户管理

建议菜单节点：`系统管理 / 租户管理`

菜单权限码：`SysTenant:retrieve`

| 权限码 | 类型 | 覆盖接口 | 说明 |
| --- | --- | --- | --- |
| `SysTenant:retrieve` | MENU | `GET /sys/tenants`、`GET /sys/tenants/{id}` | 租户列表、详情 |
| `SysTenant:create` | BUTTON | `POST /sys/tenants` | 新增系统租户 |
| `SysTenant:update` | BUTTON | `PUT /sys/tenants/{id}` | 编辑系统租户；会踢出受影响租户用户 |
| `SysTenant:delete` | BUTTON | `DELETE /sys/tenants` | 删除系统租户；会踢出受影响租户用户 |

### 系统参数

建议菜单节点：`系统管理 / 系统参数`

菜单权限码：`SysParam:retrieve`

| 权限码 | 类型 | 覆盖接口 | 说明 |
| --- | --- | --- | --- |
| `SysParam:retrieve` | MENU | `GET /sys/params`、`GET /sys/params/{id}` | 系统参数列表、详情 |
| `SysParam:create` | BUTTON | `POST /sys/params` | 新增系统参数 |
| `SysParam:update` | BUTTON | `PUT /sys/params/{id}` | 编辑系统参数 |
| `SysParam:delete` | BUTTON | `DELETE /sys/params` | 删除系统参数 |

### 数据字典

建议菜单节点：`系统管理 / 数据字典`

菜单权限码：`SysDataDict:retrieve`

| 权限码 | 类型 | 覆盖接口 | 说明 |
| --- | --- | --- | --- |
| `SysDataDict:retrieve` | MENU | `GET /sys/data-dict/classifieds`、`GET /sys/data-dict/classifieds/{classifiedId}/items` | 字典分类列表、字典项列表 |
| `SysDataDict:create` | BUTTON | `POST /sys/data-dict/classifieds`、`POST /sys/data-dict/classifieds/{classifiedId}/items` | 新增字典分类、字典项 |
| `SysDataDict:update` | BUTTON | `PUT /sys/data-dict/classifieds/{id}`、`PUT /sys/data-dict/classifieds/{classifiedId}/items/{id}` | 编辑字典分类、字典项 |
| `SysDataDict:delete` | BUTTON | `DELETE /sys/data-dict/classifieds`、`DELETE /sys/data-dict/classifieds/{classifiedId}/items` | 删除字典分类、字典项 |

### 系统日志

建议菜单节点：`系统管理 / 系统日志`

菜单权限码：`SysLog:retrieve`

| 权限码 | 类型 | 覆盖接口 | 说明 |
| --- | --- | --- | --- |
| `SysLog:retrieve` | MENU | `GET /sys/logs`、`GET /sys/logs/{id}` | 系统日志列表、详情 |

## AI 管理

### AI 模型配置

建议菜单节点：`AI 管理 / 模型配置`

菜单权限码：`AiModelConfig:retrieve`

| 权限码 | 类型 | 覆盖接口 | 说明 |
| --- | --- | --- | --- |
| `AiModelConfig:retrieve` | MENU | `GET /ai/model-configs`、`GET /ai/model-configs/{id}` | AI 模型配置列表、详情 |
| `AiModelConfig:create` | BUTTON | `POST /ai/model-configs` | 新增 AI 模型配置 |
| `AiModelConfig:update` | BUTTON | `PUT /ai/model-configs/{id}` | 编辑 AI 模型配置 |
| `AiModelConfig:delete` | BUTTON | `DELETE /ai/model-configs` | 删除 AI 模型配置 |
| `AiModelConfig:key` | BUTTON | `GET /ai/model-configs/{id}/api-key` | 查看完整 API Key |
| `AiModelConfig:test` | BUTTON | `POST /ai/model-configs/{id}:test` | 检测模型配置 |
| `AiModelConfig:models` | BUTTON | `POST /ai/model-configs/provider-models` | 查询供应商模型列表 |

### AI 对话

建议菜单节点：`AI 管理 / AI 对话`

菜单权限码：`AiChat:retrieve`

| 权限码 | 类型 | 覆盖接口 | 说明 |
| --- | --- | --- | --- |
| `AiChat:retrieve` | MENU | `GET /ai/chats/model-config-options`、`GET /ai/conversations`、`GET /ai/conversations/{conversationId}`、`GET /ai/conversations/{conversationId}/messages` | 对话可选模型、AI 会话列表、会话详情、消息列表 |
| `AiChat:chat` | BUTTON | `POST /ai/chats` | 后台普通 AI 对话 |
| `AiChat:stream` | BUTTON | `POST /ai/chats/stream` | 后台 SSE 流式 AI 对话 |

### AI 知识库

建议菜单节点：`AI 管理 / AI 知识库`

菜单权限码：`AiKnowledge:retrieve`

| 权限码 | 类型 | 覆盖接口 | 说明 |
| --- | --- | --- | --- |
| `AiKnowledge:retrieve` | MENU | 知识库、文档、切片、基础检索和检索日志查询接口 | 查询知识库、文档、切片和检索日志；执行基础检索 |
| `AiKnowledge:create` | BUTTON | `POST /ai/knowledge-bases`、`POST /ai/knowledge-documents/oss-file-bindings` | 新增知识库；关联 OSS 文件为知识库文档 |
| `AiKnowledge:update` | BUTTON | `PUT /ai/knowledge-bases/{id}` | 编辑知识库 |
| `AiKnowledge:delete` | BUTTON | `DELETE /ai/knowledge-bases`、`DELETE /ai/knowledge-documents` | 删除知识库或知识库文档 |
| `AiKnowledge:enable` | BUTTON | `PUT /ai/knowledge-bases/{id}/status/{status}` | 启停知识库 |
| `AiKnowledge:retry` | BUTTON | `PUT /ai/knowledge-documents/{id}/retry` | 重试知识库文档解析或切片 |

### AI Agent

建议菜单节点：`AI 管理 / AI Agent`

菜单权限码：`AiAgent:retrieve`

| 权限码 | 类型 | 覆盖接口 | 说明 |
| --- | --- | --- | --- |
| `AiAgent:retrieve` | MENU | `GET /ai/agents`、`GET /ai/agents/options`、`GET /ai/agents/{id}` | Agent 列表、启用选项和详情 |
| `AiAgent:create` | BUTTON | `POST /ai/agents` | 新增 Agent |
| `AiAgent:update` | BUTTON | `PUT /ai/agents/{id}` | 编辑 Agent |
| `AiAgent:delete` | BUTTON | `DELETE /ai/agents` | 删除 Agent |
| `AiAgent:enable` | BUTTON | `PUT /ai/agents/{id}/status/{status}` | 启停 Agent |

### AI 工作流

建议菜单节点：`AI 管理 / AI 工作流`

菜单权限码：`AiWorkflow:retrieve`

| 权限码 | 类型 | 覆盖接口 | 说明 |
| --- | --- | --- | --- |
| `AiWorkflow:retrieve` | MENU | 工作流定义、节点和执行记录查询接口 | 工作流列表、详情、节点列表、节点详情、执行记录列表和详情 |
| `AiWorkflow:create` | BUTTON | `POST /ai/workflows`、`POST /ai/workflows/{workflowDefinitionId}/nodes` | 新增工作流或节点 |
| `AiWorkflow:update` | BUTTON | `PUT /ai/workflows/{id}`、`PUT /ai/workflows/{workflowDefinitionId}/nodes/{id}` | 编辑工作流或节点 |
| `AiWorkflow:delete` | BUTTON | `DELETE /ai/workflows`、`DELETE /ai/workflow-nodes` | 删除工作流或节点 |
| `AiWorkflow:enable` | BUTTON | `PUT /ai/workflows/{id}/status/{status}` | 启停工作流 |
| `AiWorkflow:execute` | BUTTON | `POST /ai/workflows/{workflowDefinitionId}:execute` | 执行工作流 |

## 文件管理

### OSS 文件信息

建议菜单节点：`文件管理 / OSS 文件信息`

菜单权限码：`OssFileInfo:retrieve`

| 权限码 | 类型 | 覆盖接口 | 说明 |
| --- | --- | --- | --- |
| `OssFileInfo:retrieve` | MENU | `GET /oss/file/infos`、`GET /oss/file/infos/{id}` | 上传文件信息列表、详情 |
| `OssFileInfo:delete` | BUTTON | `DELETE /oss/file/infos` | 删除上传文件信息 |

## 仅登录即可访问的后台接口

以下接口当前没有 `@SaCheckPermission`，初始化 SQL 不应为它们单独创建按钮权限，除非后续业务明确要求细粒度控制。

| 接口 | 来源 | 说明 |
| --- | --- | --- |
| `/sys/users/me/info`、`/sys/users/info`、`/sys/users/me/password:update`、`/sys/users/me/avatar` | `AdminCurrentSysUserController` | 当前登录用户资料与个人设置 |
| `/sys/menus/side`、`/sys/menus/all` | `AdminSysMenuController` | 当前账号可见菜单查询 |
| `/select-options/roles`、`/select-options/depts`、`/select-options/ai-model-configs` | `AdminSelectOptionsController` | 后台下拉框数据源 |
| `/auth/login`、`/auth/logout` | `AdminAuthController` | 登录与退出 |
| `/oss/upload`、下载相关接口 | `AdminOssUploadDownloadController` | 上传下载能力，需结合文件访问策略单独设计 |

## 初始化 SQL 生成规则建议

- 每个业务页面创建一个 `MENU` 节点，`permission` 写对应 `retrieve` 权限码。
- 每个非查询动作创建一个 `BUTTON` 节点，`permission` 写本清单中的按钮权限码。
- 目录节点如 `系统管理`、`AI 管理`、`文件管理` 使用 `DIR` 类型，`permission` 可以为空字符串。
- 角色绑定菜单时必须同时绑定页面 `MENU` 和需要授权的 `BUTTON`；否则用户可能能进入页面但不能执行页内动作。
- 生成初始化 SQL 后，需要确保超级管理员、租户管理员与基础角色的菜单关系覆盖本清单中的 MVP 权限。
