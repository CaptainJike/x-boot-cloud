# app-api AI 对话示例口径

最后更新：2026-06-21

## 结论

`app-api` 中现有 AI 普通对话和 SSE 流式对话仅作为历史示例入口保留，不属于 Pig AI 后台企业 AI 平台 MVP 的验收入口，也不作为后续 AI 平台任务完成依据。

## 当前保留原因

- 保持历史 app 侧接口兼容，避免直接删除导致已有示例调用断裂。
- 作为 app-api 如何委托 Facade 的示例，不直接调用 `x-boot-starter-ai` 或模型配置实现。
- 便于后续如果确实要建设 C 端 AI 产品时，有一个可迁移的适配层参考。

## 明确边界

- AI 平台 MVP 的正式 HTTP 入口统一是 `admin-api`。
- AI 平台 MVP 的标准链路统一是 `admin-api -> ai-facade -> ai-service -> mapper`。
- `app-api` 现有 AI 对话 Controller 和 Service 已标记为历史示例与非 MVP。
- 后续知识库/RAG、Agent/工作流、工具/MCP、审计监控等能力，不以 `app-api` 为验收入口。

## 后续处理规则

- 如果继续保留：只做兼容性维护，不新增 Pig AI 后台平台能力。
- 如果要产品化 C 端 AI：必须单独补充 PRD、任务清单、登录用户体系、租户上下文、安全权限和验收标准。
- 如果要移除：需要先确认没有调用方依赖，再删除 Controller、Service、DTO/BO 适配和相关测试。
