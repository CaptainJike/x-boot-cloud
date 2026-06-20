# AI 服务职责

## 服务：ai-service

服务作用：
- 负责 AI 模型供应商、模型参数和租户可用配置的持久化管理。

业务能力：
- 管理 AI 模型配置的分页查询、详情、新增、编辑、删除。
- 提供默认启用模型和指定配置编码的启用模型查询能力。

拥有数据：
- `ai_model_config`

提供的 Facade：
- `AiModelConfigFacade`：提供 AI 模型配置管理和运行时配置查询能力。

消费的 Facade：
- 无。

HTTP 入口：
- `admin-api`：暂未接入 Controller，后续后台管理接口应通过 `AiModelConfigFacade` 调用。
- `app-api`：暂未接入 Controller，后续对话接口应通过 Facade 获取启用配置后再调用 AI 基础设施能力。

租户行为：
- 行级租户数据，依赖 `TenantContextHolder` 和 MyBatis-Plus 租户插件隔离。
- 暂不支持匿名访问；如果后续提供匿名 AI 对话，需要显式说明租户上下文来源。

安全行为：
- 服务层只暴露 Dubbo Facade，不直接暴露 HTTP。
- API Key 保存到 `ai_model_config.api_key`，后台 HTTP 响应仅返回脱敏值 `apiKeyMasked`。

不负责：
- 不负责后台或 app HTTP 路由。
- 不负责用户账号、角色、菜单和租户主数据。
- 不负责保存 API Key 明文或替代 KMS/配置中心密文能力。
