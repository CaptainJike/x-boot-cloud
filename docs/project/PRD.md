# x-boot-cloud AI Platform PRD

最后更新：2026-06-24

## 1. 项目目标

本项目以 `x-boot-cloud` 当前微服务脚手架为基础，配套 `x-boot-admin-vue-vben` 后台管理前端，分阶段对标 Pig AI，建设一个面向企业后台用户的 Java AI 应用平台。主要用户是后台企业用户、租户管理员、运营管理人员和平台管理员；近期目标不是一次性复刻完整 Pig AI，而是先完成能稳定交付的后台产品 MVP：RBAC 权限闭环、AI 模型配置、后台 AI 对话、后台会话管理、知识库/RAG、文档管理、基础工作流和后台管理台入口。

后续开发必须遵循 `docs/project/TASKS.md` 与 `docs/project/STATUS.md` 驱动：

1. 读取 `TASKS.md` 和 `STATUS.md`。
2. 选择第一个依赖满足的未完成任务。
3. 开发并测试该任务。
4. 更新 `TASKS.md` 和 `STATUS.md`。
5. 停止，等待下一轮继续。

## 2. 对标来源

Pig AI 对标能力参考：

- Pig AI 白皮书：https://paper.pig4cloud.com/ai.html
- Pig AI 方案博客：https://blog.pig4cloud.com/blogs/2025/pig-ai-0908

从公开资料抽取的对标能力包括：多模型接入、深度推理、联网搜索、RAG 知识库、文档清洗与切片、混合检索、Agent/工作流、MCP/工具调用、AI 问数、多模态、OCR、文档写作、审校、业务联动和运行监控。

## 3. 当前完成度基线

| 能力域 | 当前状态 | 说明 |
| --- | --- | --- |
| RBAC/系统管理 | 已完成 MVP 闭环 | 已有用户、角色、菜单、租户、部门、数据字典、参数、日志等链路；权限缓存刷新、用户踢下线和关键服务/接口测试已补齐。 |
| 权限模型 | 已完成 MVP 口径 | 继续使用 `sys_menu.permission` 承载后台权限码；当前权限清单和初始化 SQL 均覆盖 62 个唯一后台权限码，暂不新增独立 `sys_permission` 表。 |
| 权限缓存与会话失效 | 已完成 MVP 闭环 | 已覆盖角色绑定菜单后刷新权限缓存、角色删除后删除权限缓存，以及用户禁用/删除/重置密码/改角色后踢下线。 |
| AI 模型配置 | 已完成 MVP 闭环 | 已有 `ai_model_config`、`AiModelConfigFacade`、后台管理接口、模型测试、供应商模型查询、租户隔离和 API Key 脱敏/专用权限测试。 |
| AI 对话 | 已完成后台链路 | 后台普通对话、SSE 流式对话、内部 SSE 代理、会话/消息/调用日志/反馈持久化和会话查询已接入 `admin-api -> ai-facade -> ai-service`；`app-api` 仅保留历史示例入口。 |
| OSS 文件 | 已有基础能力 | 已有文件信息、上传、下载、MD5 秒传和后台文件管理接口。 |
| 后台管理前端 | 已有配套项目 | `x-boot-admin-vue-vben` 已作为后台管理前端接入系统管理、OSS 文件和 AI 平台页面；开发规范见 `docs/project/FRONTEND_X_BOOT_ADMIN_VUE_VBEN.md` 和前端仓库 `docs/DEVELOPMENT.md`。 |
| 测试体系 | 部分完成 | `admin-api`、`app-api`、`ai-service`、`sys-service` 有可运行测试；Checkstyle 存在历史基线问题，暂不作为稳定质量门禁。 |
| 知识库/RAG | 已完成 MVP 闭环 | 已有知识库、文档、切片、检索日志、OSS 来源加载、解析切片、OpenAI 兼容 embedding、Qdrant 向量库、基础检索和 RAG 引用回答。 |
| Agent/工作流 | 已完成基础闭环 | 已有 Agent、工作流定义、节点、执行记录、LLM/HTTP 工具/条件/结束节点、后台管理接口、执行接口和执行记录查询。 |
| 工具/MCP | 进行中 | M6 已完成工具注册表、实体、Mapper、SQL 和表结构测试；工具鉴权配置、调用审计、MCP 适配和权限隔离规则待推进。 |
| 审计监控 | 待推进 | 对话、检索和工作流已有部分调用/执行日志；统一后台 AI 调用日志查询、token 统计、错误率和慢调用统计仍在 M7。 |

## 4. Pig AI 缺失需求

### P0：MVP 已补齐能力

- RBAC 测试治理：修复 `sys-service` 测试基线，补充角色、菜单、用户角色、角色菜单、权限缓存和踢下线回归测试。
- 权限初始化：整理后台菜单、按钮权限码和初始化 SQL，确保新环境可直接导入基础权限数据。
- 后台 AI 对话：已将当前 app 侧示例对话能力迁移到后台链路，提供后台普通对话、SSE 流式对话和内部 SSE 代理。
- AI 会话持久化：基于后台用户保存会话、消息、模型配置、调用结果、错误信息、耗时和反馈。
- 知识库基础：支持知识库、文档、文档切片、上传文件关联、文档索引、向量化、Qdrant 写入和检索入口。
- RAG 回答：支持后台 AI 对话基于知识库检索上下文生成回答，并返回引用来源。
- 文档管理：通过后台接口支持文档上传、解析状态、切片状态、失败原因和重试。
- 基础工作流：支持 Agent/工作流配置、节点定义、执行记录和失败状态。
- 后台管理前端：已有 `x-boot-admin-vue-vben` 承载系统管理、OSS 文件、AI 模型配置、AI 对话、知识库、Agent 和工作流页面。

### P1：MVP 后增强

- 混合检索：向量检索、关键词检索、重排序策略和召回参数配置。
- 多租户知识库隔离：知识库、文档、会话和执行记录均遵守租户上下文。
- Prompt 管理：模板、变量、版本、启停状态和使用场景。
- 工具/MCP 注册：工具元数据、鉴权配置、调用日志、失败重试和权限隔离。
- 审计监控：AI 调用量、token 用量、错误率、模型配置使用分布和慢调用。

### P2：后续 Backlog

- AI 问数与智能图表。
- 多模态图片理解、图片生成、OCR。
- 文档写作、审校、报告生成。
- 代码评审、智能巡检、Skills 支持。
- 业务表单联动、审批流程集成和结果回写。

## 5. 阶段里程碑

### M0：文档与流程基线

- 创建 `docs/project/PRD.md`、`TASKS.md`、`STATUS.md`。
- 跑一次基线测试并记录结果。
- 固化后续单任务闭环规则。

验收标准：

- 三份文档存在且内容可直接指导下一轮开发。
- `STATUS.md` 记录最近测试命令、结果和下一任务。

### M1：RBAC 权限模块闭环

- 修复 `sys-service` 测试基线。
- 补 RBAC 关键服务和接口测试。
- 整理权限码和初始化 SQL。
- 保持现有 `sys_menu.permission` 模型，不新增独立权限表，除非后续需求明确要求权限资源独立管理。

验收标准：

- `mvn -pl x-boot-modules/sys/sys-service -am test` 通过。
- 用户、角色、菜单、角色绑定菜单、用户绑定角色、权限缓存、踢下线都有回归测试。
- 新环境可通过 SQL 初始化基础角色和菜单权限。

### M2：AI 模型配置与后台对话入口

- 保持 `admin-api -> AiModelConfigFacade -> AiModelConfigService` 的模型配置链路。
- 将当前 `app-api` 示例对话迁移为后台对话能力。
- 后台普通对话和后台 SSE 流式对话必须通过 `admin-api` 暴露，业务编排落在 `ai-service`。

验收标准：

- `admin-api` 提供后台普通对话和 SSE 流式对话接口。
- Controller 只调用 Facade，不直接调用 AI 基础设施或 service 实现。
- 后台对话遵守后台登录态、权限码和租户上下文。

### M3：后台 AI 对话产品化

- 新增 AI 会话、消息、反馈和调用日志。
- 后台 AI 对话接口写入会话和消息。
- 后台可查看会话、消息、调用状态和错误。

验收标准：

- 对话结果可持久化查询。
- 流式对话失败时有错误事件和调用日志。
- 多租户数据隔离清晰。

### M4：知识库/RAG MVP

- 新增知识库、文档、切片表和 Facade/Service。
- 接入 OSS 文件作为文档来源。
- 支持文档解析状态流转、切片管理和基础检索。
- 后台 AI 对话可选择知识库并返回引用。

验收标准：

- 后台可创建知识库、上传文档、生成切片、执行检索。
- 后台 RAG 对话返回答案和引用片段。
- 失败状态可查询并可重试。

### M5：Agent/工作流基础

- 新增 Agent 配置、工作流定义、节点、执行记录。
- 支持 LLM 节点、HTTP 工具节点、条件节点和结束节点的最小闭环。
- 记录输入、输出、状态、耗时和错误。

验收标准：

- 可创建一个简单工作流并执行。
- 执行记录可追踪每个节点状态。
- 工具调用有权限和审计边界。

### M6：工具/MCP 基础

- 新增工具注册表、实体、Mapper、SQL 同步和表结构测试。
- 后续继续补工具鉴权配置、工具调用审计、MCP Client/Server 适配和权限隔离规则。

验收标准：

- `ai_tool_registry` 遵守租户隔离、审计字段、逻辑删除和乐观锁。
- 工具注册表只保存非密钥元数据，不保存 API Key、Token 或完整 Header。

### M7：审计监控

- 统一补齐后台 AI 调用日志查询、token 用量统计、模型配置使用统计、错误率和慢调用统计。

验收标准：

- 后台可查询模型、对话、RAG、工作流和工具调用的关键审计信息。
- 统计能力不暴露模型 API Key、工具密钥或外部系统敏感响应。

## 6. 架构约束

- 后台 HTTP 入口放在 `x-boot-api/admin-api`。
- 后台管理前端使用 `x-boot-admin-vue-vben`，本地开发默认通过 Vite `/admin-api` 代理到 `admin-api`。
- app HTTP 入口放在 `x-boot-api/app-api`，但不承载本轮 Pig AI 对标能力。
- AI 平台 MVP 的 HTTP 入口统一放在 `admin-api`，标准链路为 `admin-api -> ai-facade -> ai-service -> mapper`。
- 跨模块契约放在 `*-facade`，请求模型用 `DTO`，响应模型用 `BO` 或 `VO`。
- 业务规则放在 `*-service` 的 Spring Service 中，不放在 Controller。
- 标准调用链路为 `HTTP Controller -> @DubboReference Facade -> @DubboService FacadeImpl -> Spring Service -> Mapper/MyBatis-Plus -> DB`。
- 新增业务表默认包含租户、审计、逻辑删除和乐观锁字段，并同步模块 SQL 与 `docs/x_boot_all.sql`。
- 涉及匿名访问时必须显式说明用户上下文和租户上下文来源。
- 前端正式后台菜单以 `admin-api` 的 `/sys/menus/side` 返回为准，按钮权限码必须与后端 `@SaCheckPermission` 和 RBAC 初始化 SQL 保持一致。

## 7. 非目标

- 当前 MVP 不做完整 Pig AI 平台复刻。
- 当前 MVP 不新建第二套后台前端；继续使用并演进现有 `x-boot-admin-vue-vben`。
- 当前 MVP 不面向 C 端用户提供 AI 平台能力，`app-api` 中现有 AI 对话只作为历史/示例实现，后续不作为任务完成依据。
- 当前 RBAC 阶段不新增 `sys_permission` 表，除非后续确认要将权限资源从菜单按钮中独立出来。
- “客户列表、客户详情、标签系统”当前仅作为流程示例，不进入本轮 MVP；如后续确认客户业务优先，再单独纳入 PRD 和任务拆解。
