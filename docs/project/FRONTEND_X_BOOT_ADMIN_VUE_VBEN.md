# x-boot-admin-vue-vben Frontend Project

最后更新：2026-06-24

## 项目定位

`x-boot-admin-vue-vben` 是 `x-boot-cloud` 的后台管理前端，基于 Vue Vben Admin 改造，面向后台管理员、租户管理员和运营管理人员。该项目不是新的后端模块，也不承载业务规则；它负责后台管理页面、权限菜单展示、表单/表格交互、文件上传入口和 AI 平台后台操作界面。

前端项目路径：

```text
/Users/zhuxucai/workspace/github/x-boot-admin-vue-vben
```

标准开发文档：

```text
/Users/zhuxucai/workspace/github/x-boot-admin-vue-vben/docs/DEVELOPMENT.md
```

## 当前技术基线

| 项目 | 当前基线 |
| --- | --- |
| Node.js | `>=26.3.0`，推荐 `26.3.0` |
| npm | `>=11.16.0`，推荐 `11.16.0` |
| Vue | `3.5.38` |
| Vite | `8.0.16` |
| TypeScript | `5.9.3` |
| UI | `ant-design-vue 4.2.1`、`vxe-table` |
| 状态管理 | `pinia`、`pinia-plugin-persistedstate` |
| 权限模式 | `PermissionModeEnum.BACK` 后台动态菜单 |

## 前后端对接约定

- 本地开发默认访问 `http://127.0.0.1:5173`。
- Vite 通过 `/admin-api` 代理到后端 `http://127.0.0.1:7001`。
- 环境变量 `VITE_GLOB_API_URL_PREFIX` 默认为 `/admin-api/v1`。
- 普通 HTTP 请求统一使用 `src/utils/http/axios` 中的 `defHttp`。
- 后端统一响应结构按 `{ code, data, msg }` 解析，页面代码通常直接获得 `data`。
- SSE 流式对话使用 `fetch + ReadableStream`，不走 Axios。
- 登录后缓存 `tokenValue`、`roles` 和 `permissions`；按钮权限通过 `hasPermission(permissionCode)` 控制。
- 正式菜单来自后端 `/sys/menus/side`，前端不以静态路由作为后台业务菜单来源。

## 已接入页面能力

| 能力域 | 前端目录 | 当前状态 |
| --- | --- | --- |
| 系统管理 | `src/views/sys`、`src/api/sys` | 已接入用户、角色、菜单、部门、租户、数据字典、参数、日志、个人资料和修改密码。 |
| OSS 文件 | `src/views/oss`、`src/api/oss` | 已接入文件信息列表、详情、删除和下载；上传统一走全局上传接口。 |
| AI 模型配置 | `src/views/ai/AiModelConfig`、`src/api/ai/AiModelConfigApi.ts` | 已接入列表、新增、编辑、删除、检测、查看完整 API Key。 |
| AI 对话 | `src/views/ai/conversation`、`src/api/ai/AiChatApi.ts` | 已接入会话列表、消息查看、普通对话、SSE 流式对话和知识库引用片段。 |
| 知识库/RAG | `src/views/ai/knowledge`、`src/api/ai/AiKnowledgeApi.ts` | 已接入知识库管理、文档绑定、切片列表、检索测试和检索日志。 |
| Agent | `src/views/ai/agent`、`src/api/ai/AiAgentApi.ts` | 已接入 Agent 管理、详情、新增、编辑、删除和启停。 |
| 工作流 | `src/views/ai/workflow`、`src/api/ai/AiWorkflowApi.ts` | 已接入工作流定义、节点管理、执行、执行记录和详情。 |
| 工具/MCP | 待新增 | 后端 M6 当前仅完成工具注册表；前端页面应等待工具鉴权、调用审计和调用接口稳定后接入。 |

## 与后端文档的关系

- `docs/project/PRD.md` 记录整体产品目标和前后端职责边界。
- `docs/project/TASKS.md` 记录后端 AI 平台任务和前端文档/接入任务。
- `docs/project/STATUS.md` 记录当前完成状态、测试结果和前端项目盘点结论。
- 前端详细开发规范以 `x-boot-admin-vue-vben/docs/DEVELOPMENT.md` 为准。

## 后续建议

- 工具/MCP 后端接口稳定后，再新增前端工具注册、鉴权配置、调用测试和审计页面。
- 为 AI 对话、知识库、工作流等核心页面补充最小 E2E 或组件回归测试。
- 生产部署前确认 `.env.production`、`nginx.conf`、SSE 代理缓冲、上传地址和后端网关路径一致。
