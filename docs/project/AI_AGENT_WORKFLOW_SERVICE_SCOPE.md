# AI Agent/工作流模块职责说明

最后更新：2026-06-22

## 服务：ai-service Agent/工作流子域

服务作用：
- 负责后台企业 AI 平台的 Agent 配置、工作流定义、节点编排、执行记录和节点运行上下文。
- MVP 阶段仍承载在 `ai-service`，不新建独立 Maven 模块；标准链路是 `admin-api -> ai-facade -> ai-service -> mapper`。

业务能力：
- 管理 Agent 基础配置，包括名称、描述、启停状态、默认模型配置、关联知识库和执行参数。
- 管理工作流定义，包括名称、版本、启停状态、入口节点、节点拓扑和发布快照。
- 管理工作流节点，包括 LLM 节点、HTTP 工具节点、条件节点和结束节点的配置草稿。
- 支持 LLM 节点执行边界，包括节点配置解析、Prompt 模板变量渲染、模型配置解析、模型调用和失败状态返回。
- 支持 HTTP 工具节点执行边界，包括白名单地址、请求方法、Header、鉴权变量、超时、响应脱敏和失败状态返回。
- 支持条件节点执行边界，包括受控表达式、默认分支、下游节点校验、上下文变量读取和失败状态返回。
- 支持结束节点执行边界，包括终态输出映射、模板汇总、成功状态、错误摘要和禁止下游节点。
- 提供后台 Agent 管理接口，包括分页、启用选项、详情、新增、编辑、删除和启停。
- 提供后台工作流管理接口，包括工作流定义和节点草稿的列表、详情、新增、编辑、删除和启停。
- 已支持执行工作流，按入口节点和节点执行结果构建上下文、调用模型或工具、记录输入输出、状态、耗时和错误。
- 为后续工具/MCP 子域预留工具调用适配边界，MVP 内 HTTP 工具节点已通过受控配置调用。
- 已支持执行记录分页和详情查询，便于后台用户定位失败节点和查看输入输出摘要。

拥有数据：
- `ai_agent`：已新增表，保存 Agent 基础配置、默认模型、默认知识库、启停状态、发布信息和执行统计。
- `ai_workflow_definition`：已新增表，保存工作流名称、版本、入口节点、定义快照、发布快照、状态和发布信息。
- `ai_workflow_node`：已新增表，保存节点类型、节点配置、连接关系、排序、状态和错误策略。
- `ai_workflow_execution`：已新增表，保存执行 ID、触发来源、输入输出摘要、状态、耗时和错误。
- 后续如需要逐节点明细，可扩展 `ai_workflow_node_execution`，但本轮不建表。

提供的 Facade：
- `AiAgentFacade`：已提供 Agent 列表、启用选项、详情、新增、编辑、删除和启停能力。
- `AiWorkflowFacade`：已提供工作流定义、节点草稿、执行、执行记录分页和详情查询能力。
- 工作流发布能力留给后续任务继续扩展。

消费的 Facade：
- 无新增跨服务 Facade 依赖。
- LLM 节点复用 `ai-service` 内部模型配置和对话编排能力，解析模型配置并调用 `XBootAiService`。
- LLM 节点可复用 `ai-service` 内部知识库/RAG 检索能力，为 Prompt 提供可选上下文。
- HTTP 工具节点当前通过 `ai-service` 内部 HTTP 适配器调用受控白名单地址，不新增跨服务 Facade 依赖。
- 后续工具/MCP 子域提供工具注册、鉴权和审计能力后，HTTP 工具节点可迁移为消费工具 Facade。
- 不直接访问 `sys-service`、`oss-service` 或工具实现服务的 Mapper、Entity。

HTTP 入口：
- `admin-api`：已新增 Agent 管理、工作流定义、工作流节点草稿、工作流执行和执行记录查询接口。
- 后续继续新增工作流发布接口。
- `app-api`：无；`app-api` 不作为本轮 Pig AI 后台平台 MVP 入口。
- MVP 标准链路：`admin-api -> ai-facade -> ai-service -> mapper`。

推荐调用链路：
- Agent 管理：`AdminAiAgentController -> AiAgentFacade -> AiAgentService -> AiAgentMapper`。
- 工作流管理：`AdminAiWorkflowController -> AiWorkflowFacade -> AiWorkflowService -> Mapper`。
- 工作流执行：`AdminAiWorkflowController -> AiWorkflowFacade -> AiWorkflowExecutionService -> NodeExecutor -> Mapper`。
- LLM 节点：`AiWorkflowExecutionService -> LlmNodeExecutor -> AiChatService/XBootAiService`。
- HTTP 工具节点：`AiWorkflowExecutionService -> HttpToolNodeExecutor -> HTTP 工具适配层 -> 执行结果`。
- 条件节点：`AiWorkflowExecutionService -> ConditionNodeExecutor -> 上下文变量 -> 下游节点Key`。
- 结束节点：`AiWorkflowExecutionService -> EndNodeExecutor -> 最终输出/状态/错误摘要`。

节点边界：
- LLM 节点已支持编排模型调用、Prompt 输入、模板变量渲染和模型输出摘要；可选知识库上下文后续通过工作流编排接入。
- HTTP 工具节点已支持调用后台配置的白名单地址、方法、Header、鉴权变量和超时配置，不允许运行任意脚本。
- HTTP 工具节点执行结果只保存响应摘要、状态码、耗时和错误摘要，并对常见敏感响应字段脱敏。
- 条件节点已支持基于上游节点输出和显式配置表达式做路由，不直接访问数据库或外部服务。
- 条件节点表达式只支持变量读取、基础比较、`&&` 和 `||`，不执行任意脚本。
- 结束节点已支持汇总最终输出、状态和错误，不再触发模型或工具调用。
- 结束节点不允许配置下游节点，避免终态节点继续产生工作流跳转。

租户行为：
- Agent、工作流定义、节点和执行记录均为行级租户数据，依赖 `TenantContextHolder` 和 MyBatis-Plus 租户插件隔离。
- 执行工作流时必须沿用后台登录态恢复出的租户和用户上下文，并写入执行记录。
- 工作流只能引用当前租户可见且启用的模型配置、知识库和工具配置。
- 当前不支持匿名执行；如后续新增公开执行入口，必须先设计租户来源、访问令牌、限流和审计策略。

安全行为：
- 后台 Agent/工作流接口要求后台登录态。
- Agent 管理权限码前缀为 `AiAgent:`，已覆盖 create、retrieve、update、delete、enable。
- 工作流管理权限码前缀为 `AiWorkflow:`，已覆盖 create、retrieve、update、delete、enable 和 execute。
- 工作流执行记录查询沿用 `AiWorkflow:retrieve`；publish 权限留给后续发布任务补充。
- HTTP 工具节点必须限制目标地址、请求方法、Header、超时、重试次数和敏感响应字段。
- 执行记录可保存输入输出摘要，不保存模型 API Key、工具密钥、完整 Header 或外部系统敏感响应。
- 节点失败必须记录错误码、错误摘要、失败节点、耗时和操作者，便于审计和排障。

不负责：
- 不负责通用工具注册、MCP 协议适配和工具鉴权密钥管理，这些能力归后续工具/MCP 子域。
- 不负责用户账号、角色、菜单、租户主数据或后台权限缓存。
- 不负责 C 端 app 用户体系和 C 端 AI 产品入口。
- 不负责执行任意代码、脚本沙箱或非白名单网络访问。
- 不负责替代 XXL-Job 的定时任务调度；如后续需要定时执行，应通过独立调度边界触发。
