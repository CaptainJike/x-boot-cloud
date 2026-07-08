# x-boot-cloud AI Platform Tasks

最后更新：2026-06-24

## 执行规则

- 每次开发开始先读取本文件和 `docs/project/STATUS.md`。
- 每次只选择第一个依赖满足的未完成任务。
- 完成代码和测试后，才能把任务从 `[ ]` 改为 `[x]`。
- 测试失败时不得勾选任务，只在 `STATUS.md` 记录失败命令、失败原因和下一步。
- 新增服务或接口必须遵循 `HTTP API -> facade -> service -> mapper`。
- AI 平台 MVP 面向后台企业用户，统一使用 `admin-api -> ai-facade -> ai-service -> mapper`；`app-api` 不作为本轮 Pig AI 对标入口。

## M0：文档与流程治理

- [x] 创建 `docs/project/PRD.md`，记录项目目标、Pig AI 对标差距、阶段里程碑和验收标准。
- [x] 创建 `docs/project/TASKS.md`，记录可持续推进的任务清单。
- [x] 创建 `docs/project/STATUS.md`，记录当前进度、测试结果和下一任务。
- [x] 将后续每次开发结束后的文档更新作为固定检查项执行。

## M1：RBAC/系统管理

- [x] RBAC 基础表已存在：`sys_role`、`sys_menu`、`sys_role_menu_relation`、`sys_user_role_relation`。
- [x] 用户管理链路已存在：`AdminSysUserController -> SysUserFacade -> SysUserService -> SysUserMapper`。
- [x] 角色管理链路已存在：`AdminSysRoleController -> SysRoleFacade -> SysRoleService -> SysRoleMapper`。
- [x] 菜单权限链路已存在：`AdminSysMenuController -> SysMenuFacade -> SysMenuService -> SysMenuMapper`。
- [x] 用户绑定角色能力已存在。
- [x] 角色绑定菜单能力已存在。
- [x] 角色权限缓存能力已存在。
- [x] 用户禁用、删除、重置密码、绑定角色后的踢下线链路已存在。
- [x] 修复 `sys-service` 当前 `ExampleUnitTest` 因缺少数据源配置导致的测试失败。
- [x] 补充用户管理服务测试：新增、编辑、删除、重置密码、禁用用户、绑定角色。
- [x] 补充角色管理服务测试：新增、编辑、删除、绑定菜单、权限集合返回。
- [x] 补充菜单管理服务测试：新增、编辑、删除、侧边菜单、可见菜单。
- [x] 补充权限缓存测试：角色菜单变更后缓存刷新，角色删除后缓存删除。
- [x] 补充踢下线回归测试：用户禁用、删除、重置密码、角色变更。
- [x] 整理后台菜单、按钮权限码清单。
- [x] 整理 RBAC 初始化 SQL，覆盖超级管理员、租户管理员、基础菜单和按钮权限。
- [x] 确认 `sys_menu.permission` 是否满足后续扩展；默认不新增独立 `sys_permission` 表。

## M2：AI 模型配置

- [x] `ai_model_config` 表已存在。
- [x] `AiModelConfigFacade` 已存在。
- [x] `AiModelConfigService` 已存在。
- [x] 后台 AI 模型配置接口已存在。
- [x] 模型配置查询、新增、编辑、删除、测试和供应商模型查询已有基础测试。
- [x] 补充模型配置租户隔离测试。
- [x] 补充 API Key 脱敏和完整 Key 查看权限测试。
- [x] 整理 AI 模型配置初始化样例 SQL。
- [x] 评估 API Key 明文保存风险，设计后续密文/KMS/配置中心方案。

## M3：后台 AI 对话

- [x] 迁移现有 `app-api` 示例对话能力到 `admin-api + ai-service`，作为会话持久化前置任务。
- [x] 新增后台普通对话接口。
- [x] 新增后台 SSE 流式对话接口。
- [x] 新增后台 SSE 内部代理链路：`AdminAiChatStreamClient -> AiChatInternalController -> AiChatService`。
- [x] 后台对话服务复用默认模型配置和指定模型配置调用逻辑。
- [x] 新增后台对话基础测试。
- [x] 新增 AI 会话表。
- [x] 新增 AI 消息表。
- [x] 新增 AI 调用日志表。
- [x] 新增 AI 反馈表。
- [x] 扩展后台对话接口，保存会话、消息、调用结果和错误。
- [x] 新增后台会话列表、会话详情、消息列表接口。
- [x] 补充对话持久化、失败记录和流式错误测试。
- [x] 将 `app-api` 现有 AI 对话标记为非 MVP 示例或移出 AI 平台任务口径。

## M4：后台知识库/RAG

- [x] 新增知识库模块职责说明。
- [x] 新增知识库 Facade 契约。
- [x] 新增知识库表：知识库、文档、文档切片、检索日志。
- [x] 新增知识库后台接口：列表、详情、新增、编辑、删除、启停。
- [x] 新增文档管理接口：上传关联、解析状态、切片状态、失败原因、重试。
- [x] 接入 OSS 文件作为知识库文档来源。
- [x] 设计文档解析与切片策略。
- [x] 设计向量化和检索抽象，预留不同 embedding provider。
- [x] 新增文档索引服务，串联 OSS 来源加载、解析、切片、embedding 和向量库写入。
- [x] 新增 OpenAI/OpenAI 兼容/DeepSeek embedding provider 默认实现。
- [x] 新增 Qdrant 向量库适配、配置和租户过滤。
- [x] 新增基础检索接口。
- [x] 扩展后台 AI 对话支持选择知识库并返回引用片段。
- [x] 补充文档索引、embedding、Qdrant 向量库配置和检索测试。
- [x] 补充知识库、文档、切片、检索和 RAG 回答测试。

## M5：Agent/工作流

- [x] 新增 Agent/工作流模块职责说明。
- [x] 新增 Agent 配置表。
- [x] 新增工作流定义表。
- [x] 新增工作流节点表。
- [x] 新增工作流执行记录表。
- [x] 支持 LLM 节点。
- [x] 支持 HTTP 工具节点。
- [x] 支持条件节点。
- [x] 支持结束节点。
- [x] 新增后台 Agent/工作流管理接口。
- [x] 新增执行接口和执行记录查询接口。
- [x] 补充节点执行、失败状态和执行记录测试。

## M6：工具/MCP

- [x] 新增工具注册表。
- [ ] 新增工具鉴权配置。
- [ ] 新增工具调用审计日志。
- [ ] 支持 HTTP 工具调用。
- [ ] 预留 MCP Client/Server 适配层。
- [ ] 设计工具调用权限隔离规则。
- [ ] 补充工具调用成功、失败、鉴权和审计测试。

## M7：审计监控

- [ ] 记录 AI 请求耗时、模型、供应商、配置编码和调用结果。
- [ ] 记录 token 用量字段，预留不同供应商统计差异。
- [ ] 新增后台 AI 调用日志查询。
- [ ] 新增模型配置使用统计。
- [ ] 新增错误率、慢调用和失败原因统计。
- [ ] 补充审计日志查询和统计测试。

## M8：后台管理前端

- [x] 将 `x-boot-admin-vue-vben` 纳入项目文档，确认为 `x-boot-cloud` 后台管理前端。
- [x] 新增前端标准开发文档：`x-boot-admin-vue-vben/docs/DEVELOPMENT.md`。
- [x] 在 `x-boot-admin-vue-vben/README.md` 补充标准开发文档入口和后端对接说明。
- [x] 盘点前端技术基线：Node.js `26.3.0`、npm `11.16.0`、Vue `3.5.38`、Vite `8.0.16`、TypeScript `5.9.3`。
- [x] 盘点前端已接入页面：系统管理、OSS 文件、AI 模型配置、AI 对话、知识库/RAG、Agent 和工作流。
- [x] 明确前端开发环境通过 Vite `/admin-api` 代理到 `admin-api`，API 前缀为 `/admin-api/v1`。
- [x] 明确前端正式菜单来自后端 `/sys/menus/side`，按钮权限通过登录返回的 `permissions` 和 `hasPermission` 控制。
- [ ] 工具/MCP 后端接口稳定后，新增前端工具注册、工具鉴权配置、调用测试和审计页面。
- [ ] 为 AI 对话、知识库、工作流等核心页面补充最小前端回归测试或 E2E 验证。

## Backlog：Pig AI 后续增强

- [ ] AI 问数和自然语言 SQL。
- [ ] 智能图表生成。
- [ ] 多模态图片理解。
- [ ] 图片生成和编辑。
- [ ] OCR 文档识别。
- [ ] AI 文档写作。
- [ ] AI 文档审校。
- [ ] AI 报告生成。
- [ ] 代码评审。
- [ ] 智能巡检。
- [ ] Skills 支持。
- [ ] 业务表单联动、审批流程集成和结果回写。
