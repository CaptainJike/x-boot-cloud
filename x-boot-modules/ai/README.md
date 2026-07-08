# AI 服务职责

## 服务：ai-service

服务作用：
- 负责后台企业 AI 平台的模型配置、对话编排、会话持久化、知识库/RAG、工具调用和审计监控等业务能力。

业务能力：
- 管理 AI 模型配置的分页查询、详情、新增、编辑、删除。
- 提供默认启用模型和指定配置编码的启用模型查询能力。
- 提供后台普通对话和后台流式对话片段编排能力，保存会话、消息、调用结果和错误。
- 提供 `AiChatInternalController` 内部 SSE 入口，供 `admin-api` 的 `AdminAiChatStreamClient` 代理后台流式对话，避免 Dubbo 阻塞完整流式响应。
- 提供后台 RAG 对话能力，支持对话时选择知识库、检索引用片段、组装增强提示词并返回引用。
- 提供后台 AI 会话分页列表、会话详情和消息分页列表查询能力。
- 提供知识库文档 OSS 原始文件来源加载能力，为文档索引任务提供标准输入。
- 提供知识库文档解析与切片策略抽象，当前支持纯文本、Markdown、PDF、Word（doc/docx）解析，以及默认重叠切片草稿生成。
- 提供 `AiKnowledgeDocumentIndexService`，串联 OSS 来源加载、解析、切片落库、embedding、向量库写入和状态回写。
- 提供知识库向量化和检索内部抽象，已接入 OpenAI/OpenAI 兼容/DeepSeek embedding provider。
- 提供 Qdrant 向量库适配、配置开关、租户过滤，以及无租户上下文时按知识库范围工作的兼容实现和未启用向量库时的保护性默认实现。
- 提供后台知识库基础检索能力，支持知识库启用校验、向量检索编排、命中片段返回和检索日志查询。
- 已在 `docs/project/AI_KNOWLEDGE_SERVICE_SCOPE.md` 固化知识库/RAG 子域职责。
- 已在 `docs/project/AI_AGENT_WORKFLOW_SERVICE_SCOPE.md` 固化 Agent/工作流子域职责，当前已承接 Agent 配置、工作流定义、节点执行和执行记录。
- 已新增 Agent 配置表，承载 Agent 基础配置、默认模型、默认知识库、系统提示词、执行参数、发布状态和执行统计。
- 已新增工作流定义表，承载工作流编码、名称、关联 Agent、版本号、入口节点、定义快照、发布快照和执行统计。
- 已新增工作流节点表，承载节点类型、节点配置、输入输出映射、下游节点、条件表达式、错误策略、重试、超时、排序和启停状态。
- 已新增工作流执行记录表，承载执行 ID、触发来源、输入输出摘要、状态、当前/失败节点、耗时、错误、链路追踪和起止时刻。
- 已支持 LLM 节点执行边界，覆盖节点配置解析、Prompt 模板变量渲染、模型配置解析、模型调用和失败状态返回。
- 已支持 HTTP 工具节点执行边界，覆盖白名单地址、方法、Header、鉴权变量、超时、响应脱敏和失败状态返回。
- 已支持条件节点执行边界，覆盖表达式路由、默认分支、下游节点校验、上下文变量读取和失败状态返回。
- 已支持结束节点执行边界，覆盖终态输出映射、模板汇总、成功状态、错误摘要和禁止下游节点。
- 提供后台 Agent 管理能力，支持分页、启用选项、详情、新增、编辑、删除和启停。
- 提供后台工作流管理能力，支持工作流定义分页、详情、新增、编辑、删除、启停和节点草稿管理。
- 提供后台工作流执行能力，支持入口节点执行、变量传递、成功/失败状态、耗时和错误摘要落库。
- 提供后台工作流执行记录分页和详情查询能力，支持按执行 ID、工作流、用户、触发来源和状态筛选。
- 已在 `docs/project/AI_TOOL_MCP_SERVICE_SCOPE.md` 固化工具/MCP 子域职责，当前已新增工具注册表作为后续鉴权、审计和 MCP 适配的基础。

拥有数据：
- `ai_model_config`
- `ai_conversation`
- `ai_message`
- `ai_call_log`
- `ai_feedback`
- `ai_knowledge_base`
- `ai_knowledge_document`
- `ai_knowledge_document_chunk`
- `ai_knowledge_retrieval_log`
- `ai_agent`
- `ai_workflow_definition`
- `ai_workflow_node`
- `ai_workflow_execution`
- `ai_tool_registry`

提供的 Facade：
- `AiModelConfigFacade`：提供 AI 模型配置管理和运行时配置查询能力。
- `AiChatFacade`：提供后台普通对话、可序列化流式片段、RAG 引用片段返回、会话列表、会话详情和消息列表能力；HTTP SSE 由 `admin-api` 适配。
- `AiKnowledgeBaseFacade`：提供知识库分页、详情、新增、编辑、删除和启停能力。
- `AiKnowledgeDocumentFacade`：提供知识库文档绑定 OSS 文件、列表、详情、删除、重试和切片列表能力。
- `AiKnowledgeRetrievalFacade`：提供基础检索和检索日志查询能力。
- `AiAgentFacade`：提供 Agent 分页、启用选项、详情、新增、编辑、删除和启停能力。
- `AiWorkflowFacade`：提供工作流定义、节点、执行和执行记录查询能力。

消费的 Facade：
- `OssFileInfoFacade`：校验知识库文档关联的 OSS 文件，并读取文件名、扩展名、大小、MD5 和存储平台等元数据。
- `OssUploadDownloadFacade`：按知识库文档关联的 OSS 文件 ID 加载原始文件字节或内部直链来源。

内部 HTTP 入口：
- `/internal-api/v1/ai/chats/stream`：AI 服务内部流式对话入口，仅供 `admin-api` 代理 SSE 使用，可通过 `x.ai.stream.internal-token` 配置内部令牌；不作为外部开放 API。

内部策略说明：
- `docs/project/AI_KNOWLEDGE_PARSE_CHUNK_STRATEGY.md`：记录知识库文档解析和切片策略边界、默认实现、状态流转建议和后续扩展点。
- `docs/project/AI_KNOWLEDGE_EMBEDDING_RETRIEVAL_STRATEGY.md`：记录知识库向量化、向量库检索和多 embedding provider 扩展边界。
- `docs/project/AI_AGENT_WORKFLOW_SERVICE_SCOPE.md`：记录 Agent/工作流职责、LLM/HTTP/条件/结束节点边界、租户安全和后续 Facade 规划。
- `docs/project/AI_TOOL_MCP_SERVICE_SCOPE.md`：记录工具注册、鉴权、审计和 MCP 适配的职责边界。

HTTP 入口：
- `admin-api`：已接入后台 AI 模型配置、对话、RAG 对话、会话查询、知识库、Agent、工作流管理、执行和记录查询接口。
- 后台 SSE 流式对话由 `admin-api` 调用 `ai-service` 内部 SSE 入口代理输出。
- 后续工具/MCP 和审计监控均从 `admin-api` 暴露。
- `app-api`：当前不属于 AI 平台 MVP 入口；现有 app 侧 AI 对话只作为历史/示例入口，已委托 `AiChatFacade`，不再直接调用 AI 基础设施。

租户行为：
- 行级租户数据，依赖 `TenantContextHolder` 和 MyBatis-Plus 租户插件隔离。
- AI 模型配置、会话、消息、知识库、文档、切片和调用日志均遵守后台登录态恢复出的租户上下文。
- Agent 配置、工作流定义、工作流节点和执行记录已纳入行级租户表；LLM 节点沿用当前租户可见的模型配置。
- 工具注册已纳入行级租户表；后续工作流引用工具配置时必须只读取当前租户可见且启用的工具。
- Qdrant 在存在租户上下文时会写入 `tenantId`、`knowledgeBaseId`、`documentId` 和 `chunkId`，检索时同时按租户和知识库过滤；无租户上下文时退化为按知识库范围过滤。
- HTTP 工具节点只允许通过节点受控配置调用白名单地址，不新增匿名访问入口。
- 条件节点只读取当前工作流执行上下文变量，不访问数据库、跨服务 Facade 或外部系统。
- 结束节点只读取当前工作流执行上下文变量并汇总终态输出，不访问数据库、跨服务 Facade 或外部系统。
- 当前后台 AI 平台不支持匿名访问；如果后续提供匿名能力，必须显式说明用户上下文和租户上下文来源。

安全行为：
- 服务层只暴露 Dubbo Facade，不直接暴露 HTTP。
- 当前兼容期 API Key 保存到 `ai_model_config.api_key`，后台 HTTP 响应仅返回脱敏值 `apiKeyMasked`。
- 后台对话接口要求后台登录态，并使用 `AiChat:chat`、`AiChat:stream` 权限码。
- 后台会话查询接口要求后台登录态，并使用 `AiChat:retrieve` 权限码。
- 后台知识库管理、文档管理和基础检索接口要求后台登录态，并使用 `AiKnowledge:*` 权限码。
- 后台 Agent 管理接口要求后台登录态，并使用 `AiAgent:*` 权限码。
- 后台工作流管理、执行和记录查询接口要求后台登录态，并使用 `AiWorkflow:*` 权限码。
- HTTP 工具节点会限制目标地址、请求方法、Header、超时和响应摘要长度，并对常见敏感响应字段脱敏。
- 条件节点使用受控表达式语法，不执行任意脚本或反射调用。
- 结束节点只生成最终输出、终态状态和错误摘要，不触发模型、工具、脚本或外部网络调用。
- 后续密文/KMS/配置中心方案参考 `docs/project/AI_MODEL_CONFIG_API_KEY_SECURITY_PLAN.md`。
- 后台 AI 菜单和按钮权限继续基于 `sys_menu.permission` 管理。

不负责：
- 不负责后台或 app HTTP 路由。
- 不负责用户账号、角色、菜单和租户主数据。
- 不负责把密钥解密能力下放到 `admin-api`、前端或其他业务域。
- 不负责 C 端 app 用户体系和 C 端 AI 产品入口。
