# x-boot-cloud AI Platform Status

最后更新：2026-06-24

## 当前开发

- 当前模块：工具/MCP 模块
- 当前完成度：M6 工具/MCP 约 14%（1/7）
- 当前流程：AI 平台 MVP 已明确面向后台企业用户，后端统一走 `admin-api -> ai-facade -> ai-service -> mapper`，后台管理前端使用 `x-boot-admin-vue-vben`
- 当前任务：已完成新增工具注册表；本轮已将前端项目纳入项目文档并新增前端标准开发文档
- 下一任务：工具/MCP：新增工具鉴权配置

## 当前已完成

- 已创建项目目标文档、任务拆解文档和当前进度文档。
- 已明确后续采用单任务闭环：读任务、开发、测试、更新文档、停止。
- 已明确 Pig AI 对标采用分阶段 MVP，而不是一次性复刻完整平台。
- 已将 `x-boot-admin-vue-vben` 纳入整体项目文档，确认为 `x-boot-cloud` 的后台管理前端。
- 已新增 `docs/project/FRONTEND_X_BOOT_ADMIN_VUE_VBEN.md`，记录前端项目定位、技术基线、前后端对接约定、已接入页面和后续建议。
- 已新增 `x-boot-admin-vue-vben/docs/DEVELOPMENT.md`，作为前端标准开发文档，覆盖本地启动、环境变量、接口规范、权限菜单、模块现状、开发流程、构建部署和排障清单。
- 已更新 `x-boot-admin-vue-vben/README.md`，补充标准开发文档入口和后端 `admin-api` 对接说明。
- 已明确“客户列表、客户详情、标签系统”暂作为流程示例，不进入当前 MVP 主线。
- 已执行一次 Maven 基线测试，并记录失败原因。
- 已将 `sys-service` 示例 SpringBootTest 改为轻量 Facade 单元测试，解除缺少数据源配置导致的测试基线失败。
- 已按固定检查项在本轮开发结束后更新项目文档。
- 已补充 `SysUserServiceTest`，覆盖用户新增、编辑、删除、重置密码、禁用用户、绑定角色。
- 已补充 `SysRoleServiceTest`，覆盖角色新增、编辑、删除、绑定菜单和权限集合返回。
- 已补充 `SysMenuServiceTest`，覆盖菜单新增、编辑、删除、侧边菜单和可见菜单。
- 已补充 `AdminSysRoleControllerTest`，覆盖角色菜单变更后权限缓存刷新、角色删除后权限缓存删除，并断言缓存动作发生在 Facade 成功执行之后。
- 已补充 `AdminSysUserControllerTest` 和 `AdminApiEventListenerTest`，覆盖用户禁用、删除、重置密码、角色变更后的踢下线事件或直接踢下线调用。
- 已新增 `docs/project/RBAC_PERMISSION_CODES.md`，从 `admin-api` 当前 `@SaCheckPermission` 注解整理后台菜单与按钮权限码清单，当前覆盖源码中 62 个唯一权限码。
- 已新增 `docs/project/RBAC_INIT_SQL.sql`，整理默认特权租户、超级管理员、租户管理员、基础菜单、按钮权限和角色授权关系初始化 SQL；当前覆盖 65 个菜单节点，其中 62 个带权限码节点、3 个目录节点。
- 已新增 `docs/project/RBAC_PERMISSION_MODEL.md`，确认 MVP 阶段继续使用 `sys_menu.permission` 承载后台权限码，暂不新增独立 `sys_permission` 表，并明确后续拆表触发条件。
- 已扩展 `SysMenuServiceTest`，覆盖超级管理员权限读取、普通角色绑定菜单权限读取、菜单 ID 反查权限集合，固定“权限来源于 `sys_menu.permission`”的回归行为。
- 已明确 AI 平台产品定位：对标 Pig AI 的后台企业产品，不面向 C 端 app 用户提供当前 MVP 能力。
- 已明确后续 AI 对话、会话持久化、知识库/RAG、Agent/工作流、工具/MCP、审计监控都走后台链路。
- 已为 `ai-service` 引入 `x-boot-starter-tenant`，让 `ai_model_config` 行级租户字段真正进入 MyBatis-Plus 租户拦截链路。
- 已新增 `AiModelConfigTenantIsolationTest`，覆盖 `ai_model_config` 参与租户行级条件、特权租户绕过、实体和 Mapper 未绕过租户默认规则。
- 已补充 API Key 安全回归测试：后台列表和详情序列化只输出 `apiKeyMasked`，不输出完整 `apiKey`；完整 Key 仅允许通过 `AiModelConfig:key` 专用权限接口查看。
- 已新增 `docs/project/AI_MODEL_CONFIG_INIT_SQL.sql`，整理 `ai_model_config` 初始化样例数据；默认启用本地 Ollama，其它云供应商保持禁用且不写入真实 API Key，重复导入时保留已有 `api_key`。
- 已新增 `docs/project/AI_MODEL_CONFIG_API_KEY_SECURITY_PLAN.md`，评估 API Key 明文保存风险，设计应用层密文落库、KMS 信封加密和配置中心引用三阶段方案。
- 已移除 `AiModelConfigService#adminInsert/adminUpdate` 中直接输出 DTO 的日志，避免 API Key 进入应用日志。
- 已新增 `AiChatFacade`、`AdminAiChatDTO`、`AdminAiChatBO` 和 `AdminAiChatStreamChunkBO`，作为后台对话 RPC 契约。
- 已新增 `AiChatService` 和 `AiChatFacadeImpl`，把普通对话、SSE 流式对话、默认模型配置和指定配置编码解析统一下沉到 `ai-service`。
- 已新增 `AdminAiChatController`，通过 `admin-api` 暴露后台普通对话和 SSE 流式对话接口。
- 已新增 `AdminAiChatStreamClient`、`AiChatInternalController` 和 `WebClientConfiguration`，由 `admin-api` 代理调用 `ai-service` 内部 SSE 接口 `/internal-api/v1/ai/chats/stream`，避免 Dubbo 阻塞完整流式响应。
- 已新增 `DispatcherTypeSkippingInterceptor`，跳过 Servlet 异步/错误二次派发，避免后台 SSE 或错误派发时重复触发 Sa-Token ThreadLocal 鉴权。
- 已将 `AppAiChatService` 调整为历史示例入口适配层，委托 `AiChatFacade`，不再直接调用 `x-boot-starter-ai` 或模型配置 Facade。
- 已补充 `AiChatServiceTest`、`AdminAiChatControllerTest` 和 `AppAiChatServiceTest`，覆盖后台对话编排、权限码、SSE 事件映射和 app 历史入口委托。
- 已新增 `ai_conversation` 表、`AiConversationEntity` 和 `AiConversationMapper`，用于承载后台用户 AI 会话主记录。
- 已同步 `x-boot-modules/ai/ai-service/src/main/resources/sql/x_boot_ai.sql` 与 `docs/x_boot_all.sql`，会话表包含租户、审计、逻辑删除、乐观锁、业务会话 ID、后台用户 ID、模型配置快照、消息计数和最近消息预览字段。
- 已新增 `AiConversationTableTest`，覆盖会话表实体/Mapper、租户拦截默认行为以及模块/聚合 SQL 同步。
- 已新增 `ai_message` 表、`AiMessageEntity` 和 `AiMessageMapper`，用于承载后台 AI 会话中的用户、助手和系统消息记录。
- 已同步 `x-boot-modules/ai/ai-service/src/main/resources/sql/x_boot_ai.sql` 与 `docs/x_boot_all.sql`，消息表包含租户、审计、逻辑删除、乐观锁、业务消息 ID、业务会话 ID、父消息、角色、内容、模型快照、状态、序号、Token 预留、错误摘要和消息时刻字段。
- 已新增 `AiMessageTableTest`，覆盖消息表实体/Mapper、租户拦截默认行为以及模块/聚合 SQL 同步。
- 已新增 `ai_call_log` 表、`AiCallLogEntity` 和 `AiCallLogMapper`，用于承载后台 AI 调用审计、耗时统计、模型统计和错误统计基础数据。
- 已同步 `x-boot-modules/ai/ai-service/src/main/resources/sql/x_boot_ai.sql` 与 `docs/x_boot_all.sql`，调用日志表包含租户、审计、逻辑删除、乐观锁、业务调用 ID、会话/消息关联、后台用户 ID、模型配置快照、调用类型、流式标识、请求/响应摘要、状态、耗时、Token 用量、结束原因、供应商请求 ID、链路追踪 ID、错误摘要和调用起止时刻字段。
- 已新增 `AiCallLogTableTest`，覆盖调用日志表实体/Mapper、租户拦截默认行为以及模块/聚合 SQL 同步。
- 已新增 `ai_feedback` 表、`AiFeedbackEntity` 和 `AiFeedbackMapper`，用于承载后台用户对 AI 回复消息的点赞、点踩、评分和处理状态。
- 已同步 `x-boot-modules/ai/ai-service/src/main/resources/sql/x_boot_ai.sql` 与 `docs/x_boot_all.sql`，反馈表包含租户、审计、逻辑删除、乐观锁、业务反馈 ID、会话/消息关联、后台用户 ID、模型配置快照、反馈类型、评分、原因编码、反馈内容、处理状态、处理人、处理时刻、处理备注和提交时刻字段。
- 已新增 `AiFeedbackTableTest`，覆盖反馈表实体/Mapper、租户拦截默认行为以及模块/聚合 SQL 同步。
- 已新增 `AiChatPersistenceService`，将后台 AI 对话的会话起始、成功完成和失败完成拆成事务化持久化步骤。
- 已扩展 `AiChatService`，普通对话和 SSE 流式对话会保存会话、用户消息、助手消息、调用结果和错误；普通对话响应返回助手消息 ID，流式事件沿用同一助手消息 ID。
- 已扩展 `AdminAiChatBO` 和 `AppAiChatBO`，普通对话响应可携带 `messageId`，为后续反馈和消息追踪提供关联字段。
- 已补充 `AiChatServiceTest` 和 `AiChatPersistenceServiceTest`，覆盖普通对话成功、普通对话失败落库、流式完成落库和流式错误落库。
- 已新增 `AdminListAiConversationDTO`、`AdminListAiMessageDTO`、`AdminAiConversationBO`、`AdminAiConversationDetailBO` 和 `AdminAiMessageBO`，作为后台会话查询 Facade 契约。
- 已扩展 `AiChatFacade` 和 `AiChatFacadeImpl`，提供后台会话分页列表、会话详情和消息分页列表 RPC 能力。
- 已新增 `AiConversationQueryService`，基于 `ai_conversation` 和 `ai_message` 提供租户隔离的会话/消息查询。
- 已扩展 `AdminAiChatController`，通过 `GET /ai/conversations`、`GET /ai/conversations/{conversationId}`、`GET /ai/conversations/{conversationId}/messages` 暴露后台会话查询接口。
- 已同步 `docs/project/RBAC_PERMISSION_CODES.md`、`docs/project/RBAC_PERMISSION_MODEL.md` 和 `docs/project/RBAC_INIT_SQL.sql`，新增 `AiChat:retrieve`、`AiChat:chat`、`AiChat:stream` 权限及 AI 对话菜单/按钮初始化。
- 已补充 `AiConversationQueryServiceTest` 和 `AdminAiChatControllerTest`，覆盖会话分页、会话详情、消息分页、缺失会话异常和查询权限码。
- 已补充 `AiChatPersistenceServiceTest`，覆盖已有会话续写时不重复插入会话、用户消息序号递增、流式调用日志起始状态，以及失败 partial 为空时使用错误摘要保存助手失败消息。
- 已补充 `AiChatServiceTest`，覆盖 SSE 流式持久化启动失败时返回错误事件、停止调用模型流且不误写失败完成记录。
- 已新增 `docs/project/APP_AI_EXAMPLE_SCOPE.md`，明确 `app-api` AI 普通对话和 SSE 流式对话仅作为历史示例入口保留，不作为 Pig AI 后台企业 AI 平台 MVP 验收入口。
- 已将 `AppAiChatController` 和 `AppAiChatService` 标记为历史示例/非 MVP，其中 Controller 的 OpenAPI 标签和操作摘要均标注为非 Pig AI 后台 MVP 入口。
- 已补充 `AppAiChatControllerTest` 和 `AppAiChatServiceTest`，覆盖 app 侧 AI 对话 Controller/Service 的历史示例与非 MVP 标记，避免后续误当正式 AI 平台入口。
- 已新增 `docs/project/AI_KNOWLEDGE_SERVICE_SCOPE.md`，明确知识库/RAG 子域仍承载在 `ai-service`，覆盖服务作用、业务能力、拥有数据、Facade 边界、OSS 依赖、后台 HTTP 入口、租户行为、安全行为和不负责事项。
- 已在 `x-boot-modules/ai/README.md` 中引用知识库/RAG 职责说明，后续知识库 Facade、表、后台接口和 RAG 对话扩展均以该职责边界为准。
- 已新增 `AiKnowledgeBaseFacade`、`AiKnowledgeDocumentFacade` 和 `AiKnowledgeRetrievalFacade`，固定后台知识库、文档、切片、基础检索和检索日志 RPC 契约。
- 已新增知识库/RAG 请求 DTO 和响应 BO，覆盖知识库、文档、文档切片、检索命中、检索结果和检索日志等 Facade 边界模型。
- 已新增 `AiKnowledgeFacadeContractTest`，用反射固定三类知识库 Facade 方法签名、分页返回、业务异常语义和引用片段返回模型。
- 已更新 `x-boot-modules/ai/README.md`，将知识库/RAG Facade 从后续规划调整为已提供的模块能力。
- 已新增 `ai_knowledge_base` 表、`AiKnowledgeBaseEntity` 和 `AiKnowledgeBaseMapper`，用于承载知识库名称、描述、启停状态、检索配置、向量化模型快照和统计字段。
- 已新增 `ai_knowledge_document` 表、`AiKnowledgeDocumentEntity` 和 `AiKnowledgeDocumentMapper`，用于承载知识库文档、OSS 文件关联、解析状态、切片状态、向量化状态、失败原因和重试信息。
- 已新增 `ai_knowledge_document_chunk` 表、`AiKnowledgeDocumentChunkEntity` 和 `AiKnowledgeDocumentChunkMapper`，用于承载文档切片内容、来源定位、Token 预估和向量化预留字段。
- 已新增 `ai_knowledge_retrieval_log` 表、`AiKnowledgeRetrievalLogEntity` 和 `AiKnowledgeRetrievalLogMapper`，用于承载知识库检索查询、召回摘要、耗时、状态、错误和会话/消息关联。
- 已同步 `x-boot-modules/ai/ai-service/src/main/resources/sql/x_boot_ai.sql` 与 `docs/x_boot_all.sql`，新增知识库、文档、文档切片和检索日志四张表建表 SQL。
- 已新增 `AiKnowledgeTableTest`，覆盖四张知识库/RAG 表实体/Mapper、租户拦截默认行为以及模块/聚合 SQL 同步。
- 已新增 `AiKnowledgeBaseService`，覆盖后台知识库分页列表、启用选项、详情、新增、编辑、删除和启停状态更新。
- 知识库服务已包含名称去空格、重复名称校验、状态校验和可选 embedding 模型配置快照。
- 已新增 `AiKnowledgeBaseFacadeImpl`，按 `DubboService -> Spring Service -> Mapper` 链路实现 `AiKnowledgeBaseFacade` 后台知识库 RPC 契约。
- 已新增 `AdminAiKnowledgeBaseController`，通过 `admin-api` 暴露知识库列表、启用选项、详情、新增、编辑、删除和启停接口。
- 知识库后台接口已接入 `AiKnowledge:*` 权限码。
- 已补充 `AiKnowledgeBaseServiceTest` 和 `AdminAiKnowledgeBaseControllerTest`，覆盖服务规则、调用委托、入参透传和权限注解。
- 已为 `ai-service` 引入 `oss-facade`，文档关联时通过 `OssFileInfoFacade` 校验 OSS 文件并读取文件元数据。
- 已新增 `AiKnowledgeDocumentService`，覆盖文档分页、详情、OSS 文件关联、删除、重试和切片分页。
- 文档管理服务已支持解析状态、切片状态、向量化状态、失败原因和重试次数的保存与返回。
- 已新增 `AiKnowledgeDocumentFacadeImpl`，按 `DubboService -> Spring Service -> Mapper` 链路实现文档管理 RPC 契约。
- 已新增 `AdminAiKnowledgeDocumentController`，通过 `admin-api` 暴露知识库文档列表、详情、关联、删除、重试和切片列表接口。
- 知识库文档接口沿用 `AiKnowledge:*` 权限码，重试动作使用 `AiKnowledge:retry`。
- 已补充 `AiKnowledgeDocumentServiceTest` 和 `AdminAiKnowledgeDocumentControllerTest`，覆盖文档服务规则和 HTTP 权限注解。
- 已更新 `x-boot-modules/ai/README.md`，同步文档管理消费的 `OssFileInfoFacade` 和已接入的后台入口。
- 已新增 `AiKnowledgeDocumentSourceService`，通过 `OssUploadDownloadFacade` 加载知识库文档关联的 OSS 原始文件来源。
- 已新增内部来源模型 `AiKnowledgeDocumentSource`，统一封装文档 ID、知识库 ID、OSS 文件 ID、文件名、扩展名、大小、MD5、存储平台、直链和文件字节。
- 已新增知识库文档来源错误语义：源文件不可用、源文件为空。
- 已补充 `AiKnowledgeDocumentSourceServiceTest`，覆盖代理下载字节、对象存储直链、无效文档、空字节和直链缺失场景。
- 已更新 `x-boot-modules/ai/README.md`，同步 `OssUploadDownloadFacade` 消费关系和 OSS 原始文件来源能力。
- 已新增 `AiKnowledgeDocumentParseStrategy` 和 `AiKnowledgeDocumentChunkStrategy`，固定文档解析与切片扩展点。
- 已新增 `AiKnowledgeParsedDocument`、`AiKnowledgeParsedSection`、`AiKnowledgeDocumentChunkConfig` 和 `AiKnowledgeDocumentChunkDraft` 内部模型。
- 已新增 `PlainTextAiKnowledgeDocumentParseStrategy`，支持 UTF-8 纯文本、Text、Markdown 文档解析，并按空行生成段落。
- 已新增 `DefaultAiKnowledgeDocumentChunkStrategy`，支持默认 Token/字符比例、重叠窗口、段落/换行/句号优先切分和预览生成。
- 已新增 `AiKnowledgeDocumentParseStrategyService`，统一选择解析策略并生成切片草稿。
- 已新增 `AiKnowledgeDocumentIndexService`，串联 OSS 来源加载、解析、切片落库、embedding、向量库写入、文档/切片状态回写和知识库统计刷新。
- 已新增 `docs/project/AI_KNOWLEDGE_PARSE_CHUNK_STRATEGY.md`，记录解析切片边界、默认策略、状态流转建议、租户安全和后续扩展点。
- 已补充 `AiKnowledgeDocumentParseStrategyServiceTest` 和 `AiKnowledgeDocumentIndexServiceTest`，覆盖支持格式、空内容、未知格式、重叠切片、非法配置、索引成功和向量库失败。
- 已更新 `x-boot-modules/ai/README.md`，同步知识库文档解析与切片策略能力。
- 已新增知识库向量化内部模型，覆盖 embedding 上下文、向量化请求、向量化响应、向量文档、向量检索请求和命中结果。
- 已新增 `AiKnowledgeEmbeddingProvider`，作为 OpenAI、OpenAI 兼容、Ollama 等 embedding provider 的内部 SPI。
- 已新增 `AiKnowledgeEmbeddingProviderService`，负责 provider 选择、请求校验、维度补齐和向量哈希补齐。
- 已新增 `OpenAiCompatibleAiKnowledgeEmbeddingProvider`，支持 OpenAI、OpenAI 兼容供应商和 DeepSeek 类型的 embeddings 调用。
- 已新增 `AiKnowledgeVectorStore`，作为 Milvus、PGVector、Elasticsearch、Redis Vector 或 Spring AI VectorStore 的内部 SPI。
- 已新增 `UnavailableAiKnowledgeVectorStore`，未配置真实向量库时所有写入、删除和搜索都返回明确业务错误。
- 已新增 `QdrantAiKnowledgeVectorStore`、`AiKnowledgeVectorStoreConfiguration`、`AiKnowledgeVectorStoreProperties` 和 `qdrant.yml`，支持 Qdrant collection 初始化、向量写入、按租户/知识库过滤检索和文档向量删除。
- 已新增 `AiKnowledgeVectorRetrievalService`，编排 query embedding 与向量库搜索，为后续基础检索接口提供内部接缝。
- 已新增 `docs/project/AI_KNOWLEDGE_EMBEDDING_RETRIEVAL_STRATEGY.md`，记录向量化、检索、多 provider 分组、状态流转、租户和安全边界。
- 已补充 `AiKnowledgeEmbeddingRetrievalServiceTest`、`AiKnowledgeVectorStoreConfigurationTest` 和 `QdrantAiKnowledgeVectorStoreTest`，覆盖 provider 选择、无 provider、无效配置、查询向量化、默认检索参数、向量库未配置失败、Qdrant 配置和 Qdrant 检索过滤。
- 已更新 `x-boot-modules/ai/README.md`，同步知识库向量化和检索内部抽象能力。
- 已新增 `AiKnowledgeRetrievalService`，编排启用知识库校验、embedding 配置解析、向量检索调用、命中片段转换、检索日志写入和最近检索时间更新。
- 已新增 `AiKnowledgeRetrievalFacadeImpl`，按 `DubboService -> Spring Service -> Mapper` 链路实现基础检索和检索日志 RPC 契约。
- 已新增 `AdminAiKnowledgeRetrievalController`，通过 `POST /ai/knowledge-retrievals`、`GET /ai/knowledge-retrieval-logs` 和日志详情接口暴露后台基础检索能力。
- 知识库检索接口沿用 `AiKnowledge:retrieve` 权限码；已同步 RBAC 权限清单和初始化 SQL 中的 AI 知识库菜单/按钮节点。
- 已补充 `AiKnowledgeRetrievalServiceTest` 和 `AdminAiKnowledgeRetrievalControllerTest`，覆盖成功检索、失败日志、禁用知识库校验、日志查询和权限注解。
- 已更新 `x-boot-modules/ai/README.md`，同步知识库基础检索、检索日志和后台 HTTP 入口能力。
- 已扩展 `AdminAiChatDTO`，后台普通对话和 SSE 流式对话均支持传入知识库 ID 列表启用 RAG 检索。
- 已扩展 `AdminAiChatBO` 和 `AdminAiChatStreamChunkBO`，返回知识库检索日志 ID 和引用片段列表。
- 已扩展 `AiChatService`，选择知识库后会先执行基础检索，再基于引用片段组装 RAG 提示词调用模型。
- RAG 对话检索失败时会按对话失败路径写入调用日志；流式对话在 `done` 事件返回引用片段。
- 已补充 `AiChatServiceTest` 和 `AiKnowledgeFacadeContractTest`，覆盖 RAG 普通对话、流式引用返回、检索失败和契约字段。
- 已补充知识库/RAG 回归测试，覆盖知识库更新快照、文档绑定统计、切片元数据、空切片段落兜底、多知识库检索过滤和 RAG 无命中回答提示。
- 已新增 `docs/project/AI_AGENT_WORKFLOW_SERVICE_SCOPE.md`，明确 Agent/工作流子域仍承载在 `ai-service`。
- Agent/工作流职责说明已覆盖 Agent 配置、工作流定义、节点边界、执行记录、租户行为、安全行为和后续 Facade 规划。
- 已更新 `x-boot-modules/ai/README.md`，同步 Agent/工作流规划表、规划 Facade、后台入口和权限边界。
- 已新增 `ai_agent` 表，承载 Agent 编码、名称、描述、头像、系统提示词、默认模型、默认知识库、执行参数、发布状态和执行统计。
- 已同步 `x-boot-modules/ai/ai-service/src/main/resources/sql/x_boot_ai.sql` 与 `docs/x_boot_all.sql` 中的 `ai_agent` 建表 SQL。
- 已新增 `AiAgentEntity` 和 `AiAgentMapper`，按 `ai-service -> mapper` 边界承载 Agent 配置持久化模型。
- 已新增 `AiAgentTableTest`，覆盖 Agent 表实体/Mapper、租户拦截默认行为以及模块/聚合 SQL 同步。
- 已新增 `ai_workflow_definition` 表，承载工作流编码、名称、关联 Agent、版本号、入口节点、定义快照、发布快照和执行统计。
- 已同步 `x-boot-modules/ai/ai-service/src/main/resources/sql/x_boot_ai.sql` 与 `docs/x_boot_all.sql` 中的工作流定义建表 SQL。
- 已新增 `AiWorkflowDefinitionEntity` 和 `AiWorkflowDefinitionMapper`，承载工作流定义持久化模型。
- 已新增 `AiWorkflowDefinitionTableTest`，覆盖工作流定义表实体/Mapper、租户拦截默认行为以及模块/聚合 SQL 同步。
- 已新增 `ai_workflow_node` 表，承载工作流定义关联、节点 Key、节点类型、节点配置、输入输出映射、下游节点、条件表达式、错误策略、重试、超时、排序和启停状态。
- 已同步 `x-boot-modules/ai/ai-service/src/main/resources/sql/x_boot_ai.sql` 与 `docs/x_boot_all.sql` 中的工作流节点建表 SQL。
- 已新增 `AiWorkflowNodeEntity` 和 `AiWorkflowNodeMapper`，承载工作流节点持久化模型。
- 已新增 `AiWorkflowNodeTableTest`，覆盖工作流节点表实体/Mapper、租户拦截默认行为以及模块/聚合 SQL 同步。
- 已新增 `ai_workflow_execution` 表，承载执行 ID、工作流快照引用、触发来源、输入输出摘要、状态、当前/失败节点、耗时、错误、链路追踪和起止时刻。
- 已同步 `x-boot-modules/ai/ai-service/src/main/resources/sql/x_boot_ai.sql` 与 `docs/x_boot_all.sql` 中的工作流执行记录建表 SQL。
- 已新增 `AiWorkflowExecutionEntity` 和 `AiWorkflowExecutionMapper`，承载工作流执行记录持久化模型。
- 已新增 `AiWorkflowExecutionTableTest`，覆盖工作流执行记录表实体/Mapper、租户拦截默认行为以及模块/聚合 SQL 同步。
- 已新增 `AiWorkflowNodeExecutor` 执行器接口，固定节点执行器支持判断和执行结果边界。
- 已新增 LLM 节点执行内部模型：`AiWorkflowLlmNodeConfig`、`AiWorkflowNodeExecutionContext`、`AiWorkflowNodeExecutionResult`。
- 已新增 `AiWorkflowLlmNodeExecutor`，支持 LLM 节点配置解析、Prompt 模板变量渲染、指定/默认模型配置解析、模型调用和失败状态返回。
- 已新增 `AiWorkflowLlmNodeExecutorTest`，覆盖 LLM 节点类型识别、模板 Prompt、默认模型输入变量、模型失败和变量缺失失败状态。
- 已新增 HTTP 工具节点执行内部模型：`AiWorkflowHttpToolNodeConfig`、`AiWorkflowHttpToolRequest`、`AiWorkflowHttpToolResponse`。
- 已新增 `AiWorkflowHttpToolClient` 和 `HutoolAiWorkflowHttpToolClient`，固定 HTTP 工具调用适配边界。
- 已新增 `AiWorkflowHttpToolNodeExecutor`，支持 HTTP 工具节点白名单地址、方法、Header、鉴权变量、超时、响应脱敏和失败状态返回。
- 已扩展 `AiWorkflowNodeExecutionResult`，支持记录 HTTP 方法、请求 URL 和响应状态码摘要。
- 已新增 `AiWorkflowHttpToolNodeExecutorTest`，覆盖 HTTP 节点类型识别、请求渲染、响应脱敏、白名单拦截、非成功状态和超时失败。
- 已新增条件节点执行内部模型：`AiWorkflowConditionNodeConfig`、`AiWorkflowConditionBranchConfig`。
- 已新增 `AiWorkflowConditionNodeExecutor`，支持受控表达式、默认分支、下游节点校验、上下文变量读取和失败状态返回。
- 已扩展 `AiWorkflowNodeExecutionResult`，支持记录条件表达式、是否命中、命中分支和下游节点 Key。
- 已新增 `AiWorkflowConditionNodeExecutorTest`，覆盖条件节点类型识别、表达式路由、默认分支、实体表达式、缺失变量和未声明下游节点失败。
- 已新增结束节点执行内部模型：`AiWorkflowEndNodeConfig`。
- 已新增 `AiWorkflowEndNodeExecutor`，支持终态输出映射、模板汇总、成功状态、错误摘要和禁止下游节点。
- 已扩展 `AiWorkflowNodeExecutionResult`，支持记录终止节点标识、工作流成功标识和最终输出摘要。
- 已新增 `AiWorkflowEndNodeExecutorTest`，覆盖结束节点类型识别、输出汇总、缺失变量、失败状态和下游节点非法配置。
- 已新增 `AiAgentFacade` 和 `AiWorkflowFacade`，固定后台 Agent 和工作流管理 RPC 契约。
- 已新增 Agent/工作流管理 DTO 和 BO，覆盖列表、详情、新增、编辑、启停和节点草稿模型。
- 已新增 `AiAgentService`，覆盖后台 Agent 分页、启用选项、详情、新增、编辑、删除和启停。
- Agent 管理已支持编码唯一性、状态校验、默认草稿发布状态、执行计数初始化和模型配置快照。
- 已新增 `AiWorkflowService`，覆盖工作流定义分页、详情、新增、编辑、删除、启停和节点草稿管理。
- 工作流管理已支持版本唯一性、关联 Agent 校验、节点类型校验、结束节点禁止下游和节点快照同步。
- 已新增 `AiAgentFacadeImpl` 和 `AiWorkflowFacadeImpl`，按 Dubbo Provider -> Spring Service 链路发布能力。
- 已新增 `AdminAiAgentController` 和 `AdminAiWorkflowController`，通过 `admin-api` 暴露后台管理接口。
- Agent/工作流后台接口已接入 `AiAgent:*` 和 `AiWorkflow:*` 权限码。
- 已补充 `AiAgentServiceTest`、`AiWorkflowServiceTest` 和对应 Controller 测试，覆盖服务规则和权限注解。
- 已同步 RBAC 权限清单、权限模型和初始化 SQL，新增 AI Agent 与 AI 工作流菜单/按钮节点。
- 已新增 `AdminExecuteAiWorkflowDTO`、`AdminListAiWorkflowExecutionDTO` 和工作流执行结果/记录 BO。
- 已扩展 `AiWorkflowFacade`，提供后台执行工作流、执行记录分页列表和执行记录详情能力。
- 已新增 `AiWorkflowExecutionService`，编排入口节点、节点执行器、上下文变量、运行中记录、成功/失败状态和统计刷新。
- 已扩展 `AdminAiWorkflowController`，通过 `POST /ai/workflows/{workflowDefinitionId}:execute` 暴露执行接口。
- 已扩展 `AdminAiWorkflowController`，通过 `GET /ai/workflow-executions` 和详情接口暴露执行记录查询。
- 已同步 RBAC 权限清单、权限模型和初始化 SQL，新增 `AiWorkflow:execute` 按钮权限。
- 已补充 `AiWorkflowExecutionServiceTest` 和 `AdminAiWorkflowControllerTest`，覆盖执行落库、成功/失败状态、记录查询和权限注解。
- 已补充工作流执行编排回归测试，覆盖多节点变量传递、终止失败保留最终输出和无执行器失败落库。
- 已调整工作流执行编排顺序，终止节点结果会优先进入终态处理，避免终止失败时丢失最终输出。
- 已新增 `docs/project/AI_TOOL_MCP_SERVICE_SCOPE.md`，明确工具/MCP 子域仍承载在 `ai-service`，覆盖工具注册、鉴权、审计、MCP 适配、租户和安全边界。
- 已新增 `ai_tool_registry` 表、`AiToolRegistryEntity` 和 `AiToolRegistryMapper`，用于承载工具编码、名称、类型、协议、非密钥入口、Schema、允许主机、敏感字段、默认超时、版本和启停状态。
- 已同步 `x-boot-modules/ai/ai-service/src/main/resources/sql/x_boot_ai.sql` 与 `docs/x_boot_all.sql`，新增工具注册表建表 SQL。
- 已新增 `AiToolRegistryTableTest`，覆盖工具注册表实体/Mapper、租户拦截默认行为以及模块/聚合 SQL 同步。

## 代码盘点结论

- `sys-service` 已有用户、角色、菜单、部门、租户、数据字典、参数、日志等领域对象和 Mapper。
- `admin-api` 已有用户、角色、菜单、部门、租户等后台管理 Controller。
- RBAC 已有角色表、菜单权限字段、角色菜单关系、用户角色关系、权限注解、权限缓存和踢下线链路。
- `ai-service` 已有 AI 模型配置管理、后台对话编排、对话持久化、错误记录、会话/消息查询和行级租户拦截支持。
- 后台 SSE 流式对话已通过 `admin-api` 代理内部 SSE 入口调用 `ai-service`，内部接口 `/internal-api/v1/ai/chats/stream` 不是外部开放 API。
- 知识库/RAG 职责说明、Facade 契约、四张核心表、知识库管理、文档管理、OSS 文档来源、解析切片策略、文档索引服务、OpenAI 兼容 embedding、Qdrant 向量库和基础检索接口已完成。
- 后台 AI 对话已支持选择知识库、执行 RAG 检索、组装增强提示词并返回引用片段。
- `admin-api` 已接入后台 AI 模型配置、普通对话、SSE 流式对话、会话查询、知识库管理、知识库文档管理和知识库基础检索接口。
- `x-boot-admin-vue-vben` 是当前后台管理前端，技术基线为 Node.js `26.3.0`、npm `11.16.0`、Vue `3.5.38`、Vite `8.0.16` 和 TypeScript `5.9.3`。
- 前端开发环境默认通过 Vite `/admin-api` 代理到 `http://127.0.0.1:7001`，接口前缀为 `/admin-api/v1`，普通请求走 `defHttp`，SSE 对话走 `fetch + ReadableStream`。
- 前端正式菜单使用后台动态菜单模式，来自 `/sys/menus/side`；按钮权限通过登录返回的 `permissions` 和 `hasPermission` 控制。
- 前端已接入系统管理、OSS 文件、AI 模型配置、AI 对话、知识库/RAG、Agent 和工作流页面。
- 工具/MCP 前端页面尚未接入，应等待后端 M6 工具鉴权、调用审计和调用接口稳定后推进。
- `app-api` 中现有 AI 普通对话和 SSE 流式对话属于历史/示例入口。
- 该入口已通过代码注解、OpenAPI 描述、测试和 `docs/project/APP_AI_EXAMPLE_SCOPE.md` 固定为非 MVP 口径。
- `app-api` 不再直接调用 AI 基础设施，不作为 Pig AI 后台产品 MVP 的完成项。
- 知识库/RAG 已完成职责边界说明、Facade 契约、数据表、知识库后台接口、文档管理接口、OSS 文档来源、解析切片策略、文档索引、embedding provider 和 Qdrant 向量库适配。
- 知识库/RAG 已完成基础检索接口、检索日志、RAG 对话扩展和重点回归测试补强。
- Agent/工作流已完成职责边界说明、Agent 配置表、工作流定义表、工作流节点表、执行记录表、LLM 节点、HTTP 工具节点、条件节点和结束节点执行边界。
- Agent/工作流管理 Facade 和后台接口已完成。
- Agent/工作流执行接口和执行记录查询接口已完成。
- 节点执行、失败状态和执行记录测试补强已完成。
- 工具/MCP 已完成工具注册表；工具鉴权配置、调用审计、HTTP 工具调用、MCP 适配和权限隔离规则待后续推进。

## 最近测试

- 命令：`rg` 前端项目旧口径关键词检查
- 时间：2026-06-24 14:00:00 +08:00
- 结果：通过
- 结论：项目文档和前端 README/开发文档中未再出现计划中列出的前端旧口径。

- 命令：`npm run type:check`
- 目录：`/Users/zhuxucai/workspace/github/x-boot-admin-vue-vben`
- 时间：2026-06-24 14:00:00 +08:00
- 结果：失败
- 覆盖模块：前端 Vue/TypeScript 类型检查。
- 失败原因：当前前端存在历史类型基线问题，集中在 Vben 组件 props 类型、demo 页面、系统管理表单 `Recordable` 到具体 DTO 的类型收窄、已移除的 `setRoleList/changePermissionCode` 调用等。
- 结论：本轮只更新文档，未修改前端业务代码；后续若要建立前端质量门禁，应先单独修复现有类型基线。

- 命令：`npm run build`
- 目录：`/Users/zhuxucai/workspace/github/x-boot-admin-vue-vben`
- 时间：2026-06-24 14:00:00 +08:00
- 结果：通过
- 覆盖模块：前端生产构建。
- 结论：构建成功生成 `dist/`；构建过程中存在依赖侧警告，包括 `@vueuse/core` 的 pure annotation 位置警告和 `mockjs` 直接 `eval` 警告。

- 命令：`rg -n '[ \t]+$' README.md docs/DEVELOPMENT.md`
- 目录：`/Users/zhuxucai/workspace/github/x-boot-admin-vue-vben`
- 时间：2026-06-24 14:00:00 +08:00
- 结果：通过
- 结论：前端 README 和标准开发文档没有行尾空白。

- 命令：`rg -n '[ \t]+$' docs/project/FRONTEND_X_BOOT_ADMIN_VUE_VBEN.md docs/project/PRD.md docs/project/TASKS.md docs/project/STATUS.md`
- 目录：`/Users/zhuxucai/workspace/github/x-boot-cloud`
- 时间：2026-06-24 14:00:00 +08:00
- 结果：通过
- 结论：本轮更新的项目文档没有行尾空白。

- 命令：`git diff --check`
- 时间：2026-06-24 14:00:00 +08:00
- 结果：通过
- 结论：后端仓库当前差异没有行尾空白问题。

- 命令：只读 Node 校验脚本，对比 `docs/project/RBAC_PERMISSION_CODES.md` 与 `docs/project/RBAC_INIT_SQL.sql` 权限码，并统计菜单节点数量
- 时间：2026-06-24 13:17:25 +08:00
- 结果：通过
- 结论：权限码清单与初始化 SQL 均覆盖 62 个唯一权限码，无缺失无多余；初始化 SQL 包含 65 个菜单节点、62 个权限节点和 3 个目录节点。

- 命令：`rg` 关键词一致性检查
- 时间：2026-06-24 13:17:25 +08:00
- 结果：通过
- 结论：核心文档不再出现“AI 对话待迁移”“知识库/RAG 未开始”“Agent/工作流 未开始”“只完成策略不落库”等与当前状态冲突的旧描述。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am`
- 参数：`-Dtest=AiKnowledgeDocumentIndexServiceTest,AiKnowledgeEmbeddingRetrievalServiceTest,AiKnowledgeVectorStoreConfigurationTest,QdrantAiKnowledgeVectorStoreTest,AiKnowledgeRetrievalServiceTest,AiChatInternalControllerTest -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false test`
- 时间：2026-06-24 13:16:30 +08:00
- 结果：失败
- 覆盖模块：`ai-service` 及其依赖模块。
- 覆盖测试：本次指定组合共运行 27 个用例，其中 25 个通过，`AiKnowledgeDocumentIndexServiceTest` 2 个用例报错。
- 失败原因：`AiKnowledgeDocumentIndexServiceTest` 在该目标组合下触发 MyBatis-Plus `can not find lambda cache for this entity [io.github.module.ai.entity.AiKnowledgeDocumentEntity]`，属于测试隔离/实体 lambda cache 初始化问题。
- 结论：本轮文档同步不修改业务代码；后续如需让该指定组合稳定通过，应优先补齐测试中的 MyBatis-Plus 实体元数据初始化。

- 命令：`mvn -pl x-boot-api/admin-api -am`
- 参数：`-Dtest=AdminAiChatStreamClientTest,WebClientConfigurationTest,DispatcherTypeSkippingInterceptorTest,AdminAiChatControllerTest -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false test`
- 时间：2026-06-24 13:17:04 +08:00
- 结果：通过
- 覆盖模块：`admin-api` 及其依赖模块。
- 覆盖测试：`WebClientConfigurationTest` 1 个用例，`AdminAiChatControllerTest` 8 个用例，`AdminAiChatStreamClientTest` 1 个用例，`DispatcherTypeSkippingInterceptorTest` 2 个用例，共 12 个用例。
- 结论：后台 SSE 代理客户端、WebClient Builder、异步/错误二次派发跳过拦截器和后台对话 Controller 测试通过。

- 命令：`git diff --check`
- 时间：2026-06-24 13:17:25 +08:00
- 结果：通过
- 结论：当前差异没有行尾空白问题。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am test`
- 时间：2026-06-22 13:26:43 +08:00
- 结果：通过
- 覆盖模块：`ai-service` 及其依赖模块。
- 覆盖测试：`ai-service` 166 个用例。
- 结论：新增工具注册表后，AI 服务模块完整测试通过。

- 命令：`git diff --check`
- 命令：当前任务新增 Java/Markdown/SQL 行 200 字符检查
- 时间：2026-06-22 13:26:53 +08:00
- 结果：通过
- 结论：当前差异没有行尾空白或新增行超过 200 字符问题。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am`
- 参数：`-Dtest=AiToolRegistryTableTest -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false test`
- 时间：2026-06-22 13:25:10 +08:00
- 结果：通过
- 覆盖模块：`ai-service` 及其依赖模块。
- 覆盖测试：`AiToolRegistryTableTest` 4 个用例。
- 结论：工具注册表实体/Mapper、租户隔离默认行为以及模块/聚合 SQL 同步测试通过。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service`
- 参数：`-am test`
- 时间：2026-06-22 13:10:09 +08:00
- 结果：通过
- 覆盖模块：`admin-api`、`app-api`、`ai-service`、`sys-service` 及其依赖模块。
- 覆盖测试：`ai-service` 162 个用例，`sys-service` 19 个用例，admin/app 相关测试通过。
- 结论：工作流执行编排测试补强后，admin/app/ai/sys 组合基线通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am test`
- 时间：2026-06-22 13:06:48 +08:00
- 结果：通过
- 覆盖模块：`ai-service` 及其依赖模块。
- 覆盖测试：`ai-service` 162 个用例。
- 结论：工作流节点执行、终止失败状态和执行记录测试补强后，AI 服务模块基线通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am test`
- 参数：`-Dtest=AiWorkflowExecutionServiceTest -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false`
- 时间：2026-06-22 13:06:34 +08:00
- 结果：通过
- 覆盖模块：`ai-service` 及其依赖模块。
- 覆盖测试：`AiWorkflowExecutionServiceTest` 8 个用例。
- 结论：多节点变量传递、终止失败输出保留、无执行器失败落库和执行记录摘要测试通过。

- 命令：`git diff --check`
- 命令：当前任务关键 Java/Markdown/SQL 行尾空白、200 字符行长和首个未完成项检查
- 时间：2026-06-22 10:11:41 +08:00
- 结果：通过
- 覆盖文件：工作流执行 Facade、DTO、BO、Service、Provider、Controller 和测试。
- 覆盖文档：`README.md`、Agent/工作流职责、RBAC 文档、`TASKS.md`、`STATUS.md`。
- 结论：本轮差异无空白错误；当前任务关键 Java/Markdown/SQL 未发现 200 字符长行。
- 结论：第一个未完成项已推进为“补充节点执行、失败状态和执行记录测试”。
- 备注：`STATUS.md` 存在历史长行，本轮新增记录保持短行，不重排历史内容。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-22 10:09:38 +08:00
- 结果：通过
- 覆盖模块：`admin-api`、`app-api`、`ai-service`、`sys-service` 及其依赖模块。
- 覆盖测试：`admin-api` 59 个用例，`app-api` 6 个用例，`ai-service` 159 个用例，`sys-service` 19 个用例。
- 结论：工作流执行接口和执行记录查询接口接入后，admin/app/ai/sys 组合基线通过。
- 备注：该命令需允许 Mockito/ByteBuddy agent 自附加；已按沙箱规则提权运行。

- 命令：`mvn -pl x-boot-api/admin-api -am -Dtest=AdminAiWorkflowControllerTest -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false test`
- 时间：2026-06-22 10:07:07 +08:00
- 结果：通过
- 覆盖模块：`admin-api` 及其依赖模块。
- 覆盖测试：`AdminAiWorkflowControllerTest` 11 个用例。
- 结论：工作流执行入口、执行记录查询入口和 `AiWorkflow:execute` 权限注解测试通过。
- 备注：受限沙箱内目标测试曾因 Mockito/ByteBuddy agent 自附加限制失败；提权重跑通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am -Dtest=AiWorkflowExecutionServiceTest -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false test`
- 时间：2026-06-22 10:03:07 +08:00
- 结果：通过
- 覆盖模块：`ai-service` 及其依赖模块。
- 覆盖测试：`AiWorkflowExecutionServiceTest` 5 个用例。
- 结论：工作流执行成功落库、节点失败落库、禁用工作流拦截、执行记录分页和详情查询测试通过。
- 备注：受限沙箱内目标测试曾因 Mockito/ByteBuddy agent 自附加限制失败；提权重跑通过。

- 命令：`git diff --check` + 当前任务 Java/Markdown/SQL 行长、权限码和首个未完成项检查
- 时间：2026-06-22 09:46:42 +08:00
- 结果：通过
- 覆盖文件：Agent/工作流 Facade、DTO、BO、Service、Provider、Controller 和测试。
- 覆盖文档：`README.md`、Agent/工作流职责、RBAC 文档、`TASKS.md`、`STATUS.md`。
- 结论：本轮差异无空白错误；新增 Java/Markdown/SQL 未发现 200 字符长行。
- 结论：第一个未完成项已推进为“新增执行接口和执行记录查询接口”。
- 备注：`STATUS.md` 和 `PRD.md` 存在历史长行，本轮新增记录保持短行，不重排历史内容。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-22 09:43:27 +08:00
- 结果：通过
- 覆盖模块：`admin-api`、`app-api`、`ai-service`、`sys-service` 及其依赖模块。
- 覆盖测试：`admin-api` 56 个用例，`app-api` 6 个用例，`ai-service` 154 个用例，`sys-service` 19 个用例。
- 结论：Agent/工作流管理接口接入后，admin/app/ai/sys 组合基线通过。
- 备注：受限沙箱内目标测试曾因 Mockito/ByteBuddy agent 自附加限制失败；批准 Maven 前缀后重跑通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am test`
- 时间：2026-06-22 09:43:05 +08:00
- 结果：通过
- 覆盖模块：`ai-service` 及其依赖模块。
- 覆盖测试：`ai-service` 154 个用例。
- 结论：Agent/工作流管理服务规则接入后，AI 服务模块测试通过。

- 命令：`git diff --check` + 当前任务 Java/Markdown 空白、行长、关键边界和首个未完成项检查
- 时间：2026-06-22 09:24:10 +08:00
- 结果：通过
- 覆盖文件：`AiWorkflowEndNodeConfig.java`、`AiWorkflowNodeExecutionResult.java`
- 覆盖文件：`AiWorkflowEndNodeExecutor.java`、`AiWorkflowEndNodeExecutorTest.java`
- 覆盖文档：`README.md`、`AI_AGENT_WORKFLOW_SERVICE_SCOPE.md`、`TASKS.md`、`STATUS.md`
- 结论：本轮差异无空白错误；当前任务 Java/Markdown 未发现 200 字符长行。
- 结论：第一个未完成项已推进为“新增后台 Agent/工作流管理接口”。
- 备注：`STATUS.md` 存在历史长行，本轮新增记录保持短行，不重排历史内容。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-22 09:22:06 +08:00
- 结果：通过
- 覆盖模块：`admin-api`、`app-api`、`ai-service`、`sys-service` 及其依赖模块。
- 覆盖测试：`admin-api` 41 个用例，`app-api` 6 个用例，`ai-service` 139 个用例，`sys-service` 19 个用例。
- 结论：结束节点执行边界接入后，admin/app/ai/sys 组合基线通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am test`
- 时间：2026-06-22 09:21:47 +08:00
- 结果：通过
- 覆盖模块：`ai-service` 及其依赖模块。
- 覆盖测试：`ai-service` 139 个用例。
- 结论：结束节点执行边界接入后，AI 服务模块测试通过。
- 备注：受限沙箱内完整模块测试曾因 Mockito/ByteBuddy agent 自附加限制失败；批准 Maven 前缀后重跑通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am -Dtest=AiWorkflowEndNodeExecutorTest -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false test`
- 时间：2026-06-22 09:21:45 +08:00
- 结果：通过
- 覆盖模块：`ai-service` 及其依赖模块。
- 覆盖测试：`AiWorkflowEndNodeExecutorTest` 6 个用例。
- 结论：结束节点输出汇总、默认上下文汇总、缺失变量、失败状态和下游节点非法配置测试通过。

- 命令：`git diff --check` + 当前任务 Java/Markdown 空白、行长、关键边界和首个未完成项检查
- 时间：2026-06-21 17:53:06 +08:00
- 结果：通过
- 覆盖文件：`AiWorkflowConditionBranchConfig.java`、`AiWorkflowConditionNodeConfig.java`
- 覆盖文件：`AiWorkflowNodeExecutionResult.java`、`AiWorkflowConditionNodeExecutor.java`
- 覆盖文件：`AiWorkflowConditionNodeExecutorTest.java`
- 覆盖文档：`README.md`、`AI_AGENT_WORKFLOW_SERVICE_SCOPE.md`、`TASKS.md`、`STATUS.md`
- 结论：本轮差异无空白错误；当前任务 Java/Markdown 未发现 200 字符长行。
- 结论：第一个未完成项已推进为“支持结束节点”。
- 备注：`STATUS.md` 存在历史长行，本轮新增记录保持短行，不重排历史内容。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-21 17:51:09 +08:00
- 结果：通过
- 覆盖模块：`admin-api`、`app-api`、`ai-service`、`sys-service` 及其依赖模块。
- 覆盖测试：`admin-api` 41 个用例，`app-api` 6 个用例，`ai-service` 133 个用例，`sys-service` 19 个用例。
- 结论：条件节点执行边界接入后，admin/app/ai/sys 组合基线通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am test`
- 时间：2026-06-21 17:50:54 +08:00
- 结果：通过
- 覆盖模块：`ai-service` 及其依赖模块。
- 覆盖测试：`ai-service` 133 个用例。
- 结论：条件节点执行边界接入后，AI 服务模块测试通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am -Dtest=AiWorkflowConditionNodeExecutorTest -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false test`
- 时间：2026-06-21 17:50:27 +08:00
- 结果：通过
- 覆盖模块：`ai-service` 及其依赖模块。
- 覆盖测试：`AiWorkflowConditionNodeExecutorTest` 6 个用例。
- 结论：条件节点表达式路由、默认分支、实体表达式、缺失变量和未声明下游节点失败测试通过。

- 命令：`git diff --check` + 当前任务 Java/Markdown 空白、行长、关键边界和首个未完成项检查
- 时间：2026-06-21 17:43:34 +08:00
- 结果：通过
- 覆盖文件：`AiWorkflowHttpToolNodeConfig.java`、`AiWorkflowHttpToolRequest.java`
- 覆盖文件：`AiWorkflowHttpToolResponse.java`、`AiWorkflowNodeExecutionResult.java`
- 覆盖文件：`AiWorkflowHttpToolClient.java`、`HutoolAiWorkflowHttpToolClient.java`
- 覆盖文件：`AiWorkflowHttpToolNodeExecutor.java`、`AiWorkflowHttpToolNodeExecutorTest.java`
- 覆盖文档：`README.md`、`AI_AGENT_WORKFLOW_SERVICE_SCOPE.md`、`TASKS.md`、`STATUS.md`
- 结论：本轮差异无空白错误；当前任务 Java/Markdown 未发现 200 字符长行。
- 结论：第一个未完成项已推进为“支持条件节点”。
- 备注：`STATUS.md` 存在历史长行，本轮新增记录保持短行，不重排历史内容。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-21 17:41:25 +08:00
- 结果：通过
- 覆盖模块：`admin-api`、`app-api`、`ai-service`、`sys-service` 及其依赖模块。
- 覆盖测试：`admin-api` 41 个用例，`app-api` 6 个用例，`ai-service` 127 个用例，`sys-service` 19 个用例。
- 结论：HTTP 工具节点执行边界接入后，admin/app/ai/sys 组合基线通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am test`
- 时间：2026-06-21 17:41:09 +08:00
- 结果：通过
- 覆盖模块：`ai-service` 及其依赖模块。
- 覆盖测试：`ai-service` 127 个用例。
- 结论：HTTP 工具节点执行边界接入后，AI 服务模块测试通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am -Dtest=AiWorkflowHttpToolNodeExecutorTest -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false test`
- 时间：2026-06-21 17:40:55 +08:00
- 结果：通过
- 覆盖模块：`ai-service` 及其依赖模块。
- 覆盖测试：`AiWorkflowHttpToolNodeExecutorTest` 5 个用例。
- 结论：HTTP 工具节点请求渲染、鉴权 Header、响应脱敏、白名单拦截、非成功状态和超时失败测试通过。

- 命令：`git diff --check` + 当前任务 Java/Markdown 空白、行长、关键边界和首个未完成项检查
- 时间：2026-06-21 17:32:14 +08:00
- 结果：通过
- 覆盖文件：`AiWorkflowLlmNodeConfig.java`、`AiWorkflowNodeExecutionContext.java`
- 覆盖文件：`AiWorkflowNodeExecutionResult.java`、`AiWorkflowNodeExecutor.java`
- 覆盖文件：`AiWorkflowLlmNodeExecutor.java`、`AiWorkflowLlmNodeExecutorTest.java`
- 覆盖文档：`README.md`、`AI_AGENT_WORKFLOW_SERVICE_SCOPE.md`、`TASKS.md`、`STATUS.md`
- 结论：本轮差异无空白错误；当前任务 Java/Markdown 未发现 200 字符长行。
- 结论：第一个未完成项已推进为“支持 HTTP 工具节点”。
- 备注：`STATUS.md` 存在历史长行，本轮新增记录保持短行，不重排历史内容。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-21 17:29:33 +08:00
- 结果：通过
- 覆盖模块：`admin-api`、`app-api`、`ai-service`、`sys-service` 及其依赖模块。
- 覆盖测试：`admin-api` 41 个用例，`app-api` 6 个用例，`ai-service` 122 个用例，`sys-service` 19 个用例。
- 结论：LLM 节点执行边界接入后，admin/app/ai/sys 组合基线通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am test`
- 时间：2026-06-21 17:29:19 +08:00
- 结果：通过
- 覆盖模块：`ai-service` 及其依赖模块。
- 覆盖测试：`ai-service` 122 个用例。
- 结论：LLM 节点执行边界接入后，AI 服务模块测试通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am -Dtest=AiWorkflowLlmNodeExecutorTest -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false test`
- 时间：2026-06-21 17:27:06 +08:00
- 结果：通过
- 覆盖模块：`ai-service` 及其依赖模块。
- 覆盖测试：`AiWorkflowLlmNodeExecutorTest` 5 个用例。
- 结论：LLM 节点类型识别、模板 Prompt、默认模型输入变量、模型失败和变量缺失失败状态测试通过。

- 命令：`git diff --check` + 当前任务 Java/Markdown/SQL 空白、行长、关键边界和首个未完成项检查
- 时间：2026-06-21 17:18:56 +08:00
- 结果：通过
- 覆盖文件：`AiWorkflowExecutionEntity.java`、`AiWorkflowExecutionMapper.java`、`AiWorkflowExecutionTableTest.java`
- 覆盖 SQL：`x_boot_ai.sql`、`x_boot_all.sql`
- 覆盖文档：`README.md`、`AI_AGENT_WORKFLOW_SERVICE_SCOPE.md`、`TASKS.md`、`STATUS.md`
- 结论：本轮差异无空白错误；当前任务 Java/Markdown 和新增 SQL 块未发现 200 字符长行。
- 结论：第一个未完成项已推进为“支持 LLM 节点”。
- 备注：`STATUS.md` 存在历史长行，本轮新增记录保持短行，不重排历史内容。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-21 17:17:46 +08:00
- 结果：通过
- 覆盖模块：`admin-api`、`app-api`、`ai-service`、`sys-service` 及其依赖模块。
- 覆盖测试：`admin-api` 41 个用例，`app-api` 6 个用例，`ai-service` 117 个用例，`sys-service` 19 个用例。
- 结论：工作流执行记录表接入后，admin/app/ai/sys 组合基线通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am test`
- 时间：2026-06-21 17:17:18 +08:00
- 结果：通过
- 覆盖模块：`ai-service` 及其依赖模块。
- 覆盖测试：`ai-service` 117 个用例。
- 结论：工作流执行记录表接入后，AI 服务模块测试通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am -Dtest=AiWorkflowExecutionTableTest -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false test`
- 时间：2026-06-21 17:15:26 +08:00
- 结果：通过
- 覆盖模块：`ai-service` 及其依赖模块。
- 覆盖测试：`AiWorkflowExecutionTableTest` 4 个用例。
- 结论：工作流执行记录表实体/Mapper、租户拦截和模块/聚合 SQL 同步测试通过。

- 命令：`git diff --check` + 当前任务 Java/Markdown/SQL 空白、行长、关键边界和首个未完成项检查
- 时间：2026-06-21 17:08:27 +08:00
- 结果：通过
- 覆盖文件：`AiWorkflowNodeEntity.java`、`AiWorkflowNodeMapper.java`、`AiWorkflowNodeTableTest.java`
- 覆盖 SQL：`x_boot_ai.sql`、`x_boot_all.sql`
- 覆盖文档：`README.md`、`AI_AGENT_WORKFLOW_SERVICE_SCOPE.md`、`TASKS.md`、`STATUS.md`
- 结论：本轮差异无空白错误；当前任务 Java/Markdown 和新增 SQL 块未发现 200 字符长行。
- 结论：第一个未完成项已推进为“新增工作流执行记录表”。
- 备注：`STATUS.md` 存在历史长行，本轮新增记录保持短行，不重排历史内容。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-21 17:06:49 +08:00
- 结果：通过
- 覆盖模块：`admin-api`、`app-api`、`ai-service`、`sys-service` 及其依赖模块。
- 覆盖测试：`admin-api` 41 个用例，`app-api` 6 个用例，`ai-service` 113 个用例，`sys-service` 19 个用例。
- 结论：工作流节点表接入后，admin/app/ai/sys 组合基线通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am test`
- 时间：2026-06-21 17:06:36 +08:00
- 结果：通过
- 覆盖模块：`ai-service` 及其依赖模块。
- 覆盖测试：`ai-service` 113 个用例。
- 结论：工作流节点表接入后，AI 服务模块测试通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am -Dtest=AiWorkflowNodeTableTest -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false test`
- 时间：2026-06-21 17:04:50 +08:00
- 结果：通过
- 覆盖模块：`ai-service` 及其依赖模块。
- 覆盖测试：`AiWorkflowNodeTableTest` 4 个用例。
- 结论：工作流节点表实体/Mapper、租户拦截和模块/聚合 SQL 同步测试通过。

- 命令：`git diff --check` + 当前任务 Java/Markdown/SQL 空白、行长、关键边界和首个未完成项检查
- 时间：2026-06-21 16:57:03 +08:00
- 结果：通过
- 覆盖文件：`AiWorkflowDefinitionEntity.java`、`AiWorkflowDefinitionMapper.java`、`AiWorkflowDefinitionTableTest.java`
- 覆盖 SQL：`x_boot_ai.sql`、`x_boot_all.sql`
- 覆盖文档：`README.md`、`AI_AGENT_WORKFLOW_SERVICE_SCOPE.md`、`TASKS.md`、`STATUS.md`
- 结论：本轮差异无空白错误；当前任务 Java/Markdown 和新增 SQL 块未发现 200 字符长行。
- 结论：第一个未完成项已推进为“新增工作流节点表”。
- 备注：`STATUS.md` 存在历史长行，本轮新增记录保持短行，不重排历史内容。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-21 16:56:12 +08:00
- 结果：通过
- 覆盖模块：`admin-api`、`app-api`、`ai-service`、`sys-service` 及其依赖模块。
- 覆盖测试：`admin-api` 41 个用例，`app-api` 6 个用例，`ai-service` 109 个用例，`sys-service` 19 个用例。
- 结论：工作流定义表接入后，admin/app/ai/sys 组合基线通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am test`
- 时间：2026-06-21 16:55:56 +08:00
- 结果：通过
- 覆盖模块：`ai-service` 及其依赖模块。
- 覆盖测试：`ai-service` 109 个用例。
- 结论：工作流定义表接入后，AI 服务模块测试通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am -Dtest=AiWorkflowDefinitionTableTest -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false test`
- 时间：2026-06-21 16:53:54 +08:00
- 结果：通过
- 覆盖模块：`ai-service` 及其依赖模块。
- 覆盖测试：`AiWorkflowDefinitionTableTest` 4 个用例。
- 结论：工作流定义表实体/Mapper、租户拦截和模块/聚合 SQL 同步测试通过。

- 命令：`git diff --check` + 当前任务 Java/Markdown/SQL 空白、行长、关键边界和首个未完成项检查
- 时间：2026-06-21 16:48:07 +08:00
- 结果：通过
- 覆盖文件：`AiAgentEntity.java`、`AiAgentMapper.java`、`AiAgentTableTest.java`、`x_boot_ai.sql`、`x_boot_all.sql`
- 覆盖文档：`README.md`、`AI_AGENT_WORKFLOW_SERVICE_SCOPE.md`、`TASKS.md`、`STATUS.md`
- 结论：本轮差异无空白错误；当前任务 Java/Markdown 和新增 SQL 块未发现 200 字符长行。
- 结论：第一个未完成项已推进为“新增工作流定义表”。
- 备注：`STATUS.md` 存在历史长行，本轮新增记录保持短行，不重排历史内容。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-21 16:46:31 +08:00
- 结果：通过
- 覆盖模块：`admin-api`、`app-api`、`ai-service`、`sys-service` 及其依赖模块。
- 覆盖测试：`admin-api` 41 个用例，`app-api` 6 个用例，`ai-service` 105 个用例，`sys-service` 19 个用例。
- 结论：Agent 配置表接入后，admin/app/ai/sys 组合基线通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am test`
- 时间：2026-06-21 16:46:09 +08:00
- 结果：通过
- 覆盖模块：`ai-service` 及其依赖模块。
- 覆盖测试：`ai-service` 105 个用例。
- 结论：Agent 配置表接入后，AI 服务模块测试通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am -Dtest=AiAgentTableTest -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false test`
- 时间：2026-06-21 16:43:38 +08:00
- 结果：通过
- 覆盖模块：`ai-service` 及其依赖模块。
- 覆盖测试：`AiAgentTableTest` 4 个用例。
- 结论：Agent 配置表实体/Mapper、租户拦截和模块/聚合 SQL 同步测试通过。

- 命令：`git diff --check` + 当前任务文档空白、行长、关键边界和首个未完成项检查
- 时间：2026-06-21 16:37:36 +08:00
- 结果：通过
- 覆盖文件：`AI_AGENT_WORKFLOW_SERVICE_SCOPE.md`、`README.md`、`TASKS.md`、`STATUS.md`
- 结论：本轮文档无空白错误；职责文档、README 和 TASKS 未发现 200 字符长行。
- 结论：`STATUS.md` 本轮新增行未超过 200 字符，下一个未完成项已推进为“新增 Agent 配置表”。
- 备注：`STATUS.md` 存在历史长行，本轮不重排历史测试记录。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-21 16:27:43 +08:00
- 结果：通过
- 覆盖模块：`admin-api`、`app-api`、`ai-service`、`sys-service` 及其依赖模块。
- 覆盖测试：`admin-api` 41 个用例，`app-api` 6 个用例，`ai-service` 101 个用例，`sys-service` 19 个用例。
- 结论：知识库/RAG 测试补强后，admin/app/ai/sys 组合基线通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am test`
- 测试类：`AiKnowledgeBaseServiceTest,AiKnowledgeDocumentServiceTest,AiKnowledgeDocumentParseStrategyServiceTest,AiKnowledgeRetrievalServiceTest,AiChatServiceTest`
- 参数：`-DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false`
- 时间：2026-06-21 16:27:17 +08:00
- 结果：通过
- 覆盖模块：`ai-service` 及其依赖模块。
- 覆盖测试：知识库/RAG 目标测试 43 个用例。
- 结论：知识库更新、文档绑定、切片、检索过滤和 RAG 无命中回答测试通过。

- 命令：`git diff --check` + 当前任务 Java/README/TASKS 文件 200 字符行长检查
- 时间：2026-06-21 16:18:40 +08:00
- 结果：通过
- 结论：本轮差异未发现空白错误；当前任务 Java、README 和 TASKS 文件未发现超过 200 字符的行。
- 备注：`STATUS.md` 存在历史长行，本轮新增记录保持短行，不重排历史内容。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-21 16:18:28 +08:00
- 结果：通过
- 覆盖模块：`admin-api`、`app-api`、`ai-service`、`sys-service` 及其依赖模块。
- 覆盖测试：`admin-api` 41 个用例，`app-api` 6 个用例，`ai-service` 96 个用例，`sys-service` 19 个用例。
- 结论：RAG 对话扩展接入后，admin/app/ai/sys 组合基线通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am -Dtest=AiChatServiceTest,AiKnowledgeFacadeContractTest -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false test`
- 时间：2026-06-21 16:17:27 +08:00
- 结果：通过
- 覆盖模块：`ai-service` 及其依赖模块。
- 覆盖测试：`AiChatServiceTest` 12 个用例，`AiKnowledgeFacadeContractTest` 6 个用例。
- 结论：RAG 普通对话、流式引用返回、检索失败持久化和契约字段测试通过。

- 命令：`mvn -pl x-boot-api/admin-api -am -Dtest=AdminAiChatControllerTest -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false test`
- 时间：2026-06-21 16:16:35 +08:00
- 结果：通过
- 覆盖模块：`admin-api` 及其依赖模块。
- 覆盖测试：`AdminAiChatControllerTest` 6 个用例。
- 结论：后台对话 Controller 委托、SSE 映射和权限注解测试通过。

- 命令：`git diff --check` + 当前任务 Java/README/RBAC/TASKS 文件 200 字符行长检查
- 时间：2026-06-21 16:06:54 +08:00
- 结果：通过
- 结论：本轮差异未发现空白错误；基础检索相关 Java、README、RBAC 和任务文档未发现超过 200 字符的行。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-21 16:06:34 +08:00
- 结果：通过
- 覆盖模块：`admin-api`、`app-api`、`ai-service`、`sys-service` 及其依赖模块。
- 覆盖测试：`admin-api` 41 个用例，`app-api` 6 个用例，`ai-service` 91 个用例，`sys-service` 19 个用例。
- 结论：知识库基础检索接口接入后，admin/app/ai/sys 组合基线通过。

- 命令：`mvn -pl x-boot-api/admin-api -am -Dtest=AdminAiKnowledgeRetrievalControllerTest -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false test`
- 时间：2026-06-21 16:00:41 +08:00
- 结果：通过
- 覆盖模块：`admin-api` 及其依赖模块。
- 覆盖测试：`AdminAiKnowledgeRetrievalControllerTest` 4 个用例。
- 结论：知识库检索后台 Controller 委托、返回结果和权限注解测试通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am -Dtest=AiKnowledgeRetrievalServiceTest -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false test`
- 时间：2026-06-21 16:00:05 +08:00
- 结果：通过
- 覆盖模块：`ai-service` 及其依赖模块。
- 覆盖测试：`AiKnowledgeRetrievalServiceTest` 7 个用例。
- 结论：基础检索成功、向量检索失败日志、禁用知识库校验、日志分页和日志详情测试通过。

- 命令：`git diff --check` + 当前任务 Java/README/策略文档文件 200 字符行长检查
- 时间：2026-06-21 15:44:02 +08:00
- 结果：通过
- 结论：本轮差异未发现空白错误；向量检索抽象相关 Java、README 和策略设计文档未发现超过 200 字符的行。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-21 15:43:09 +08:00
- 结果：通过
- 覆盖模块：`admin-api`、`app-api`、`ai-service`、`sys-service` 及其依赖模块。
- 覆盖测试：`admin-api` 37 个用例，`app-api` 6 个用例，`ai-service` 84 个用例，`sys-service` 19 个用例。
- 结论：知识库向量化和检索抽象接入后，admin/app/ai/sys 组合基线通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am test`
- 时间：2026-06-21 15:41:54 +08:00
- 结果：通过
- 覆盖模块：`oss-facade`、`ai-facade`、`ai-service` 及其依赖模块。
- 覆盖测试：`AiKnowledgeEmbeddingRetrievalServiceTest` 5 个用例，`ai-service` 目标模块合计 84 个用例。
- 结论：provider 选择、无 provider、无效配置、查询向量化、默认检索参数、向量库未配置失败和目标 AI 模块基线通过。

- 命令：`git diff --check` + 当前任务 Java/README/策略文档/TASKS 文件 200 字符行长检查
- 时间：2026-06-21 15:34:09 +08:00
- 结果：通过
- 结论：本轮差异未发现空白错误；解析切片相关 Java、README、策略设计文档和任务清单未发现超过 200 字符的行。
- 备注：`STATUS.md` 历史测试记录存在既有长行，本轮不重排历史记录，避免引入无关文档 diff。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-21 15:32:09 +08:00
- 结果：通过
- 覆盖模块：`admin-api`、`app-api`、`ai-service`、`sys-service` 及其依赖模块。
- 覆盖测试：`admin-api` 37 个用例，`app-api` 6 个用例，`ai-service` 79 个用例，`sys-service` 19 个用例。
- 结论：知识库文档解析与切片策略接入后，admin/app/ai/sys 组合基线通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am test`
- 时间：2026-06-21 15:31:46 +08:00
- 结果：通过
- 覆盖模块：`oss-facade`、`ai-facade`、`ai-service` 及其依赖模块。
- 覆盖测试：`AiKnowledgeDocumentParseStrategyServiceTest` 5 个用例，`ai-service` 目标模块合计 79 个用例。
- 结论：纯文本/Markdown 解析、空内容、未知格式、重叠切片、非法切片配置和目标 AI 模块基线通过。

- 命令：`git diff --check` + 当前任务 Java/README 文件 200 字符行长检查
- 时间：2026-06-21 15:23:37 +08:00
- 结果：通过
- 结论：本轮差异未发现空白错误；OSS 文档来源相关 Java/README 文件未发现超过 200 字符的行。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-21 15:22:41 +08:00
- 结果：通过
- 覆盖模块：`admin-api`、`app-api`、`ai-service`、`sys-service` 及其依赖模块。
- 覆盖测试：`admin-api` 37 个用例，`app-api` 6 个用例，`ai-service` 74 个用例，`sys-service` 19 个用例。
- 结论：OSS 文档来源能力接入后，admin/app/ai/sys 组合基线通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am test`
- 时间：2026-06-21 15:22:24 +08:00
- 结果：通过
- 覆盖模块：`oss-facade`、`ai-facade`、`ai-service` 及其依赖模块。
- 覆盖测试：`AiKnowledgeDocumentSourceServiceTest` 5 个用例，`ai-service` 目标模块合计 74 个用例。
- 结论：知识库文档 OSS 来源加载、空源校验、直链来源和目标 AI 模块基线通过。

- 命令：`git diff --check` + 当前任务 Java 文件 200 字符行长检查
- 时间：2026-06-21 15:17:39 +08:00
- 结果：通过
- 结论：本轮差异未发现空白错误；知识库文档接口相关 Java 文件未发现超过 200 字符的行。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-21 15:16:42 +08:00
- 结果：通过
- 覆盖模块：`admin-api`、`app-api`、`ai-service`、`sys-service` 及其依赖模块。
- 覆盖测试：`admin-api` 37 个用例，`app-api` 6 个用例，`ai-service` 69 个用例，`sys-service` 19 个用例。
- 结论：知识库文档管理接口接入后，admin/app/ai/sys 组合基线通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am test`
- 时间：2026-06-21 15:16:12 +08:00
- 结果：通过
- 覆盖模块：`oss-facade`、`ai-facade`、`ai-service` 及其依赖模块。
- 覆盖测试：`AiKnowledgeDocumentServiceTest` 7 个用例，`ai-service` 目标模块合计 69 个用例。
- 结论：知识库文档服务、OSS 文件元数据关联和目标 AI 模块基线通过。

- 命令：`git diff --check` + 当前任务 Java 文件 200 字符行长检查
- 时间：2026-06-21 15:03:04 +08:00
- 结果：通过
- 结论：本轮差异未发现空白错误；知识库后台接口相关 Java 文件未发现超过 200 字符的行。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-21 15:02:26 +08:00
- 结果：通过
- 覆盖模块：`admin-api`、`app-api`、`ai-service`、`sys-service` 及其依赖模块。
- 覆盖测试：`admin-api` 30 个用例，`app-api` 6 个用例，`ai-service` 62 个用例，`sys-service` 19 个用例。
- 结论：知识库后台管理接口接入后，admin/app/ai/sys 组合基线通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am test`
- 时间：2026-06-21 15:02:09 +08:00
- 结果：通过
- 覆盖模块：`ai-facade`、`ai-service` 及其依赖模块。
- 覆盖测试：`AiKnowledgeBaseServiceTest` 7 个用例，`ai-service` 目标模块合计 62 个用例。
- 结论：知识库后台管理服务、Facade 实现和目标 AI 模块基线通过。

- 命令：`git diff --check` + 当前任务文件行尾空白检查 + 当前任务 Java 文件 200 字符行长检查 + 知识库表 SQL/实体/Mapper 标记检查
- 时间：2026-06-21 14:53:09 +08:00
- 结果：通过
- 结论：本轮差异未发现空白错误；当前任务文件未发现行尾空白；当前任务 Java 文件未发现超过 200 字符的行；四张知识库/RAG 表在模块 SQL、聚合 SQL、Entity 和 Mapper 中均已覆盖。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-21 14:52:26 +08:00
- 结果：通过
- 覆盖模块：`admin-api`、`app-api`、`ai-service`、`sys-service` 及其依赖模块。
- 覆盖测试：`admin-api` 23 个用例，`app-api` 6 个用例，`ai-service` 55 个用例，`sys-service` 19 个用例。
- 结论：知识库/RAG 四张表新增后，admin/app/ai/sys 组合基线通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am test`
- 时间：2026-06-21 14:51:59 +08:00
- 结果：通过
- 覆盖模块：`ai-facade`、`ai-service` 及其依赖模块。
- 覆盖测试：`AiKnowledgeTableTest` 7 个用例，`ai-service` 目标模块合计 55 个用例。
- 结论：知识库/RAG 四张表实体、Mapper、模块 SQL、聚合 SQL 和租户行级拦截结构校验通过。

- 命令：`git diff --check` + 当前任务文件行尾空白检查 + 当前任务 Java 文件 200 字符行长检查 + 知识库 Facade 契约标记检查
- 时间：2026-06-21 14:43:00 +08:00
- 结果：通过
- 结论：本轮差异未发现空白错误；当前任务文件未发现行尾空白；当前任务 Java 文件未发现超过 200 字符的行；契约标记均已覆盖。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-21 14:40:03 +08:00
- 结果：通过
- 覆盖模块：`admin-api`、`app-api`、`ai-service`、`sys-service` 及其依赖模块。
- 覆盖测试：`admin-api` 23 个用例，`app-api` 6 个用例，`ai-service` 48 个用例，`sys-service` 19 个用例。
- 结论：知识库/RAG Facade 契约新增后，admin/app/ai/sys 组合基线通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am test`
- 时间：2026-06-21 14:39:45 +08:00
- 结果：通过
- 覆盖模块：`ai-facade`、`ai-service` 及其依赖模块。
- 覆盖测试：`AiKnowledgeFacadeContractTest` 5 个用例，`ai-service` 目标模块合计 48 个用例。
- 结论：知识库/RAG Facade 接口、请求 DTO、响应 BO 和目标 AI 模块基线通过。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-21 14:27:23 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-redis`、`x-boot-starter-web`、`x-boot-starter-satoken`、`x-boot-starter-tenant`、`x-boot-starter-test`、`x-boot-starter-ai`、`sys-facade`、`oss-facade`、`ai-facade`、`admin-api`、`app-api`、`ai-service`、`sys-service`
- 覆盖测试：`AdminApiEventListenerTest` 2 个用例，`AdminSysUserControllerTest` 4 个用例，`AdminSysRoleControllerTest` 2 个用例，`AdminAiModelConfigControllerTest` 9 个用例，`AdminAiChatControllerTest` 6 个用例，`AppAiChatControllerTest` 3 个用例，`AppAiChatServiceTest` 3 个用例，`AiChatServiceTest` 8 个用例，`AiFeedbackTableTest` 4 个用例，`AiConversationQueryServiceTest` 4 个用例，`AiMessageTableTest` 4 个用例，`AiCallLogTableTest` 4 个用例，`AiChatPersistenceServiceTest` 5 个用例，`AiModelConfigTenantIsolationTest` 3 个用例，`AiModelConfigServiceTest` 7 个用例，`AiConversationTableTest` 4 个用例，`SysMenuServiceTest` 8 个用例，`SysUserServiceTest` 6 个用例，`SysRoleServiceTest` 4 个用例，`ExampleUnitTest` 1 个用例。
- 结论：知识库/RAG 职责说明新增后，admin/app/ai/sys 组合基线通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am test`
- 时间：2026-06-21 14:27:05 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-web`、`x-boot-starter-tenant`、`x-boot-starter-test`、`x-boot-starter-ai`、`ai-facade`、`ai-service`
- 覆盖测试：`AiChatServiceTest` 8 个用例，`AiFeedbackTableTest` 4 个用例，`AiConversationQueryServiceTest` 4 个用例，`AiMessageTableTest` 4 个用例，`AiCallLogTableTest` 4 个用例，`AiChatPersistenceServiceTest` 5 个用例，`AiModelConfigTenantIsolationTest` 3 个用例，`AiModelConfigServiceTest` 7 个用例，`AiConversationTableTest` 4 个用例。
- 结论：知识库/RAG 职责说明新增后，目标 AI 模块基线通过。

- 命令：只读 Node 校验脚本检查 `docs/project/AI_KNOWLEDGE_SERVICE_SCOPE.md` 的职责章节、表名、Facade 名、OSS 依赖、标准链路、租户和权限标记；同时执行 `git diff --check`、行尾空白检查和文档 200 字符行长检查
- 时间：2026-06-21 14:27:38 +08:00
- 结果：通过
- 结论：知识库/RAG 职责说明覆盖 21 个关键标记；本轮差异未发现空白错误；本轮文档未发现行尾空白或超过 200 字符的行。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-21 14:18:28 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-redis`、`x-boot-starter-web`、`x-boot-starter-satoken`、`x-boot-starter-tenant`、`x-boot-starter-test`、`x-boot-starter-ai`、`sys-facade`、`oss-facade`、`ai-facade`、`admin-api`、`app-api`、`ai-service`、`sys-service`
- 覆盖测试：`AdminApiEventListenerTest` 2 个用例，`AdminSysUserControllerTest` 4 个用例，`AdminSysRoleControllerTest` 2 个用例，`AdminAiModelConfigControllerTest` 9 个用例，`AdminAiChatControllerTest` 6 个用例，`AppAiChatControllerTest` 3 个用例，`AppAiChatServiceTest` 3 个用例，`AiChatServiceTest` 8 个用例，`AiFeedbackTableTest` 4 个用例，`AiConversationQueryServiceTest` 4 个用例，`AiMessageTableTest` 4 个用例，`AiCallLogTableTest` 4 个用例，`AiChatPersistenceServiceTest` 5 个用例，`AiModelConfigTenantIsolationTest` 3 个用例，`AiModelConfigServiceTest` 7 个用例，`AiConversationTableTest` 4 个用例，`SysMenuServiceTest` 8 个用例，`SysUserServiceTest` 6 个用例，`SysRoleServiceTest` 4 个用例，`ExampleUnitTest` 1 个用例。
- 结论：`app-api` AI 对话历史示例/非 MVP 标记接入后，admin/app/ai/sys 组合基线通过。

- 命令：`mvn -pl x-boot-api/app-api -am test`
- 时间：2026-06-21 14:17:42 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-redis`、`x-boot-starter-web`、`x-boot-starter-satoken`、`x-boot-starter-test`、`ai-facade`、`app-api`
- 覆盖测试：`AppAiChatControllerTest` 3 个用例，`AppAiChatServiceTest` 3 个用例。
- 结论：app 侧 AI 对话 Controller/Service 的历史示例与非 MVP 标记测试通过。

- 命令：`git diff --check` + 本轮文件行尾空白检查 + 本轮 Java 文件 200 字符行长检查 + 非 MVP 口径检索
- 时间：2026-06-21 14:19:04 +08:00
- 结果：通过
- 结论：本轮差异未发现空白错误；本轮文档和 Java 文件未发现行尾空白；本轮 Java 文件未发现超过 200 字符的行；非 MVP 口径已出现在文档、Controller、Service 和测试中。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-21 14:09:30 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-redis`、`x-boot-starter-web`、`x-boot-starter-satoken`、`x-boot-starter-tenant`、`x-boot-starter-test`、`x-boot-starter-ai`、`sys-facade`、`oss-facade`、`ai-facade`、`admin-api`、`app-api`、`ai-service`、`sys-service`
- 覆盖测试：`AdminApiEventListenerTest` 2 个用例，`AdminSysUserControllerTest` 4 个用例，`AdminSysRoleControllerTest` 2 个用例，`AdminAiModelConfigControllerTest` 9 个用例，`AdminAiChatControllerTest` 6 个用例，`AppAiChatControllerTest` 2 个用例，`AppAiChatServiceTest` 2 个用例，`AiChatServiceTest` 8 个用例，`AiFeedbackTableTest` 4 个用例，`AiConversationQueryServiceTest` 4 个用例，`AiMessageTableTest` 4 个用例，`AiCallLogTableTest` 4 个用例，`AiChatPersistenceServiceTest` 5 个用例，`AiModelConfigTenantIsolationTest` 3 个用例，`AiModelConfigServiceTest` 7 个用例，`AiConversationTableTest` 4 个用例，`SysMenuServiceTest` 8 个用例，`SysUserServiceTest` 6 个用例，`SysRoleServiceTest` 4 个用例，`ExampleUnitTest` 1 个用例。
- 结论：对话持久化、失败记录和流式错误测试补强后，admin/app/ai/sys 组合基线通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am test`
- 时间：2026-06-21 14:09:17 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-web`、`x-boot-starter-tenant`、`x-boot-starter-test`、`x-boot-starter-ai`、`ai-facade`、`ai-service`
- 覆盖测试：`AiChatServiceTest` 8 个用例，`AiFeedbackTableTest` 4 个用例，`AiConversationQueryServiceTest` 4 个用例，`AiMessageTableTest` 4 个用例，`AiCallLogTableTest` 4 个用例，`AiChatPersistenceServiceTest` 5 个用例，`AiModelConfigTenantIsolationTest` 3 个用例，`AiModelConfigServiceTest` 7 个用例，`AiConversationTableTest` 4 个用例。
- 结论：AI 对话持久化续写、失败落库兜底和 SSE 持久化启动失败错误事件测试通过。

- 命令：`git diff --check` + 本轮文件行尾空白检查 + 本轮 Java 测试文件 200 字符行长检查
- 时间：2026-06-21 14:11:04 +08:00
- 结果：通过
- 结论：本轮差异未发现空白错误；`TASKS.md`、`STATUS.md` 和本轮测试文件未发现行尾空白；本轮 Java 测试文件未发现超过 200 字符的行。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-21 14:00:52 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-redis`、`x-boot-starter-web`、`x-boot-starter-satoken`、`x-boot-starter-tenant`、`x-boot-starter-test`、`x-boot-starter-ai`、`sys-facade`、`oss-facade`、`ai-facade`、`admin-api`、`app-api`、`ai-service`、`sys-service`
- 覆盖测试：`AdminApiEventListenerTest` 2 个用例，`AdminSysUserControllerTest` 4 个用例，`AdminSysRoleControllerTest` 2 个用例，`AdminAiModelConfigControllerTest` 9 个用例，`AdminAiChatControllerTest` 6 个用例，`AppAiChatControllerTest` 2 个用例，`AppAiChatServiceTest` 2 个用例，`AiChatServiceTest` 7 个用例，`AiFeedbackTableTest` 4 个用例，`AiConversationQueryServiceTest` 4 个用例，`AiMessageTableTest` 4 个用例，`AiCallLogTableTest` 4 个用例，`AiChatPersistenceServiceTest` 3 个用例，`AiModelConfigTenantIsolationTest` 3 个用例，`AiModelConfigServiceTest` 7 个用例，`AiConversationTableTest` 4 个用例，`SysMenuServiceTest` 8 个用例，`SysUserServiceTest` 6 个用例，`SysRoleServiceTest` 4 个用例，`ExampleUnitTest` 1 个用例。
- 结论：后台会话列表、详情、消息列表接口接入后，admin/app/ai/sys 组合基线通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am test`
- 时间：2026-06-21 13:58:34 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-web`、`x-boot-starter-tenant`、`x-boot-starter-test`、`x-boot-starter-ai`、`ai-facade`、`ai-service`
- 覆盖测试：`AiChatServiceTest` 7 个用例，`AiFeedbackTableTest` 4 个用例，`AiConversationQueryServiceTest` 4 个用例，`AiMessageTableTest` 4 个用例，`AiCallLogTableTest` 4 个用例，`AiChatPersistenceServiceTest` 3 个用例，`AiModelConfigTenantIsolationTest` 3 个用例，`AiModelConfigServiceTest` 7 个用例，`AiConversationTableTest` 4 个用例。
- 结论：AI 会话查询服务、Facade 契约和目标模块测试通过。

- 命令：只读 Node 校验脚本，对比 `docs/project/RBAC_PERMISSION_CODES.md` 与 `docs/project/RBAC_INIT_SQL.sql` 权限码，并检查菜单节点与角色授权数量
- 时间：2026-06-21 14:01:10 +08:00
- 结果：通过
- 结论：权限码清单与初始化 SQL 均覆盖 45 个唯一权限码，无缺失无多余；初始化 SQL 包含 48 个菜单节点；超级管理员绑定 48 个节点，租户管理员绑定 44 个节点并默认排除平台级租户管理节点。

- 命令：`git diff --check` + 行尾空白检查 + 本轮 Java 文件 200 字符行长检查 + 本轮主代码 120 行方法长度轻量检查
- 时间：2026-06-21 14:04:17 +08:00
- 结果：通过
- 结论：本轮新增和修改文件未发现空白错误、行尾空白、超过 200 字符的行或超过 120 行的方法。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-21 13:47:12 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-redis`、`x-boot-starter-web`、`x-boot-starter-satoken`、`x-boot-starter-tenant`、`x-boot-starter-test`、`x-boot-starter-ai`、`sys-facade`、`oss-facade`、`ai-facade`、`admin-api`、`app-api`、`ai-service`、`sys-service`
- 覆盖测试：`AdminApiEventListenerTest` 2 个用例，`AdminSysUserControllerTest` 4 个用例，`AdminSysRoleControllerTest` 2 个用例，`AdminAiModelConfigControllerTest` 9 个用例，`AdminAiChatControllerTest` 3 个用例，`AppAiChatControllerTest` 2 个用例，`AppAiChatServiceTest` 2 个用例，`AiChatServiceTest` 7 个用例，`AiFeedbackTableTest` 4 个用例，`AiMessageTableTest` 4 个用例，`AiCallLogTableTest` 4 个用例，`AiChatPersistenceServiceTest` 3 个用例，`AiModelConfigTenantIsolationTest` 3 个用例，`AiModelConfigServiceTest` 7 个用例，`AiConversationTableTest` 4 个用例，`SysMenuServiceTest` 8 个用例，`SysUserServiceTest` 6 个用例，`SysRoleServiceTest` 4 个用例，`ExampleUnitTest` 1 个用例。
- 结论：后台 AI 对话持久化和错误记录接入后，admin/app/ai/sys 组合基线通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am test`
- 时间：2026-06-21 13:41:12 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-web`、`x-boot-starter-tenant`、`x-boot-starter-test`、`x-boot-starter-ai`、`ai-facade`、`ai-service`
- 覆盖测试：`AiChatServiceTest` 7 个用例，`AiFeedbackTableTest` 4 个用例，`AiMessageTableTest` 4 个用例，`AiCallLogTableTest` 4 个用例，`AiChatPersistenceServiceTest` 3 个用例，`AiModelConfigTenantIsolationTest` 3 个用例，`AiModelConfigServiceTest` 7 个用例，`AiConversationTableTest` 4 个用例。
- 结论：AI 对话持久化服务、普通对话失败记录和流式错误记录目标模块测试通过。

- 命令：`git diff --check`
- 时间：2026-06-21 13:43:58 +08:00
- 结果：通过
- 结论：本轮差异未发现空白错误。

- 命令：行尾空白检查 + 本轮 Java 文件 200 字符行长检查 + `ai-service` 主代码 120 行方法长度轻量检查
- 时间：2026-06-21 13:44:30 +08:00
- 结果：通过
- 结论：本轮新增和修改文件未发现行尾空白、超过 200 字符的行或超过 120 行的方法。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am checkstyle:check`
- 时间：2026-06-21 13:43:58 +08:00
- 结果：未通过（历史基线阻断）
- 原因：Maven Checkstyle 默认使用 `sun_checks.xml`，在未进入本次改动模块前已于 `x-boot-core` 既有代码报出 338 个历史违规。
- 下一步：需要先统一 Maven Checkstyle 插件使用仓库规则或清理既有基线后，再把该命令作为稳定门禁。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am checkstyle:check -Dcheckstyle.config.location=checkstyle-v1.xml`
- 时间：2026-06-21 13:44:11 +08:00
- 结果：未通过（配置初始化阻断）
- 原因：`checkstyle-v1.xml` 与当前 Maven 插件携带的 Checkstyle 9.3 不兼容，启动阶段报 `TreeWalker is not allowed as a parent of LineLength`。
- 下一步：需要升级/调整 `checkstyle-v1.xml` 结构，或固定兼容的 Checkstyle 版本。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-21 13:26:45 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-redis`、`x-boot-starter-web`、`x-boot-starter-satoken`、`x-boot-starter-tenant`、`x-boot-starter-test`、`x-boot-starter-ai`、`sys-facade`、`oss-facade`、`ai-facade`、`admin-api`、`app-api`、`ai-service`、`sys-service`
- 覆盖测试：`AdminApiEventListenerTest` 2 个用例，`AdminSysUserControllerTest` 4 个用例，`AdminSysRoleControllerTest` 2 个用例，`AdminAiModelConfigControllerTest` 9 个用例，`AdminAiChatControllerTest` 3 个用例，`AppAiChatControllerTest` 2 个用例，`AppAiChatServiceTest` 2 个用例，`AiChatServiceTest` 5 个用例，`AiFeedbackTableTest` 4 个用例，`AiMessageTableTest` 4 个用例，`AiCallLogTableTest` 4 个用例，`AiModelConfigTenantIsolationTest` 3 个用例，`AiModelConfigServiceTest` 7 个用例，`AiConversationTableTest` 4 个用例，`SysMenuServiceTest` 8 个用例，`SysUserServiceTest` 6 个用例，`SysRoleServiceTest` 4 个用例，`ExampleUnitTest` 1 个用例。
- 结论：AI 反馈表新增后，admin/app/ai/sys 组合基线通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am test`
- 时间：2026-06-21 13:25:38 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-web`、`x-boot-starter-tenant`、`x-boot-starter-test`、`x-boot-starter-ai`、`ai-facade`、`ai-service`
- 覆盖测试：`AiChatServiceTest` 5 个用例，`AiFeedbackTableTest` 4 个用例，`AiMessageTableTest` 4 个用例，`AiCallLogTableTest` 4 个用例，`AiModelConfigTenantIsolationTest` 3 个用例，`AiModelConfigServiceTest` 7 个用例，`AiConversationTableTest` 4 个用例。
- 结论：AI 反馈表实体、Mapper、模块 SQL、聚合 SQL 和租户行级拦截结构校验通过。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-21 13:21:25 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-redis`、`x-boot-starter-web`、`x-boot-starter-satoken`、`x-boot-starter-tenant`、`x-boot-starter-test`、`x-boot-starter-ai`、`sys-facade`、`oss-facade`、`ai-facade`、`admin-api`、`app-api`、`ai-service`、`sys-service`
- 覆盖测试：`AdminApiEventListenerTest` 2 个用例，`AdminSysUserControllerTest` 4 个用例，`AdminSysRoleControllerTest` 2 个用例，`AdminAiModelConfigControllerTest` 9 个用例，`AdminAiChatControllerTest` 3 个用例，`AppAiChatControllerTest` 2 个用例，`AppAiChatServiceTest` 2 个用例，`AiChatServiceTest` 5 个用例，`AiMessageTableTest` 4 个用例，`AiCallLogTableTest` 4 个用例，`AiModelConfigTenantIsolationTest` 3 个用例，`AiModelConfigServiceTest` 7 个用例，`AiConversationTableTest` 4 个用例，`SysMenuServiceTest` 8 个用例，`SysUserServiceTest` 6 个用例，`SysRoleServiceTest` 4 个用例，`ExampleUnitTest` 1 个用例。
- 结论：AI 调用日志表新增后，admin/app/ai/sys 组合基线通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am test`
- 时间：2026-06-21 13:20:16 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-web`、`x-boot-starter-tenant`、`x-boot-starter-test`、`x-boot-starter-ai`、`ai-facade`、`ai-service`
- 覆盖测试：`AiChatServiceTest` 5 个用例，`AiMessageTableTest` 4 个用例，`AiCallLogTableTest` 4 个用例，`AiModelConfigTenantIsolationTest` 3 个用例，`AiModelConfigServiceTest` 7 个用例，`AiConversationTableTest` 4 个用例。
- 结论：AI 调用日志表实体、Mapper、模块 SQL、聚合 SQL 和租户行级拦截结构校验通过。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-21 13:12:45 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-redis`、`x-boot-starter-web`、`x-boot-starter-satoken`、`x-boot-starter-tenant`、`x-boot-starter-test`、`x-boot-starter-ai`、`sys-facade`、`oss-facade`、`ai-facade`、`admin-api`、`app-api`、`ai-service`、`sys-service`
- 覆盖测试：`AdminApiEventListenerTest` 2 个用例，`AdminSysUserControllerTest` 4 个用例，`AdminSysRoleControllerTest` 2 个用例，`AdminAiModelConfigControllerTest` 9 个用例，`AdminAiChatControllerTest` 3 个用例，`AppAiChatControllerTest` 2 个用例，`AppAiChatServiceTest` 2 个用例，`AiChatServiceTest` 5 个用例，`AiMessageTableTest` 4 个用例，`AiModelConfigTenantIsolationTest` 3 个用例，`AiModelConfigServiceTest` 7 个用例，`AiConversationTableTest` 4 个用例，`SysMenuServiceTest` 8 个用例，`SysUserServiceTest` 6 个用例，`SysRoleServiceTest` 4 个用例，`ExampleUnitTest` 1 个用例。
- 结论：AI 消息表新增后，admin/app/ai/sys 组合基线通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am test`
- 时间：2026-06-21 13:10:47 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-web`、`x-boot-starter-tenant`、`x-boot-starter-test`、`x-boot-starter-ai`、`ai-facade`、`ai-service`
- 覆盖测试：`AiChatServiceTest` 5 个用例，`AiMessageTableTest` 4 个用例，`AiModelConfigTenantIsolationTest` 3 个用例，`AiModelConfigServiceTest` 7 个用例，`AiConversationTableTest` 4 个用例。
- 结论：AI 消息表实体、Mapper、模块 SQL、聚合 SQL 和租户行级拦截结构校验通过。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-21 13:03:04 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-redis`、`x-boot-starter-web`、`x-boot-starter-satoken`、`x-boot-starter-tenant`、`x-boot-starter-test`、`x-boot-starter-ai`、`sys-facade`、`oss-facade`、`ai-facade`、`admin-api`、`app-api`、`ai-service`、`sys-service`
- 覆盖测试：`AdminApiEventListenerTest` 2 个用例，`AdminSysUserControllerTest` 4 个用例，`AdminSysRoleControllerTest` 2 个用例，`AdminAiModelConfigControllerTest` 9 个用例，`AdminAiChatControllerTest` 3 个用例，`AppAiChatControllerTest` 2 个用例，`AppAiChatServiceTest` 2 个用例，`AiChatServiceTest` 5 个用例，`AiModelConfigTenantIsolationTest` 3 个用例，`AiModelConfigServiceTest` 7 个用例，`AiConversationTableTest` 4 个用例，`SysMenuServiceTest` 8 个用例，`SysUserServiceTest` 6 个用例，`SysRoleServiceTest` 4 个用例，`ExampleUnitTest` 1 个用例。
- 结论：AI 会话表新增后，admin/app/ai/sys 组合基线通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am test`
- 时间：2026-06-21 13:01:40 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-web`、`x-boot-starter-tenant`、`x-boot-starter-test`、`x-boot-starter-ai`、`ai-facade`、`ai-service`
- 覆盖测试：`AiChatServiceTest` 5 个用例，`AiModelConfigTenantIsolationTest` 3 个用例，`AiModelConfigServiceTest` 7 个用例，`AiConversationTableTest` 4 个用例。
- 结论：AI 会话表实体、Mapper、模块 SQL、聚合 SQL 和租户行级拦截结构校验通过。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-21 12:54:58 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-redis`、`x-boot-starter-web`、`x-boot-starter-satoken`、`x-boot-starter-tenant`、`x-boot-starter-test`、`x-boot-starter-ai`、`sys-facade`、`oss-facade`、`ai-facade`、`admin-api`、`app-api`、`ai-service`、`sys-service`
- 覆盖测试：`AdminApiEventListenerTest` 2 个用例，`AdminSysUserControllerTest` 4 个用例，`AdminSysRoleControllerTest` 2 个用例，`AdminAiModelConfigControllerTest` 9 个用例，`AdminAiChatControllerTest` 3 个用例，`AppAiChatControllerTest` 2 个用例，`AppAiChatServiceTest` 2 个用例，`AiChatServiceTest` 5 个用例，`AiModelConfigTenantIsolationTest` 3 个用例，`AiModelConfigServiceTest` 7 个用例，`SysMenuServiceTest` 8 个用例，`SysUserServiceTest` 6 个用例，`SysRoleServiceTest` 4 个用例，`ExampleUnitTest` 1 个用例。
- 结论：后台 AI 对话前置迁移完成后，admin/app/ai/sys 组合基线通过。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service -am test`
- 时间：2026-06-21 12:52:38 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-redis`、`x-boot-starter-web`、`x-boot-starter-satoken`、`x-boot-starter-tenant`、`x-boot-starter-test`、`x-boot-starter-ai`、`sys-facade`、`oss-facade`、`ai-facade`、`admin-api`、`app-api`、`ai-service`
- 覆盖测试：`AdminApiEventListenerTest` 2 个用例，`AdminSysUserControllerTest` 4 个用例，`AdminSysRoleControllerTest` 2 个用例，`AdminAiModelConfigControllerTest` 9 个用例，`AdminAiChatControllerTest` 3 个用例，`AppAiChatControllerTest` 2 个用例，`AppAiChatServiceTest` 2 个用例，`AiChatServiceTest` 5 个用例，`AiModelConfigTenantIsolationTest` 3 个用例，`AiModelConfigServiceTest` 7 个用例。
- 结论：后台对话 Facade、ai-service 编排、admin-api 普通/SSE 入口和 app-api 历史入口委托均通过目标链路测试。

- 命令：`rg -n "XBootAiFactory|XBootAiService|x-boot-starter-ai|AiModelConfigFacade|getDefaultEnabledConfig|getEnabledConfigByCode|toRuntimeConfig" x-boot-api/app-api/src/main/java x-boot-api/app-api/pom.xml`（无匹配即通过）
- 时间：2026-06-21 12:53:20 +08:00
- 结果：通过
- 结论：`app-api` 主代码和 POM 不再直接引用 AI 基础设施、模型配置 Facade 或运行时配置转换逻辑。

- 命令：只读 Node 校验脚本，检查 `docs/project/AI_MODEL_CONFIG_API_KEY_SECURITY_PLAN.md` 的风险评估、密文/KMS/配置中心、迁移、审计和测试章节
- 时间：2026-06-21 12:35:05 +08:00
- 结果：通过
- 结论：安全方案文档包含当前链路、明文风险、目标原则、应用层密文落库、KMS 信封加密、配置中心引用、实施顺序、数据迁移、审计权限、测试清单和参考资料；未发现常见真实密钥形态。

- 命令：只读 Node 校验脚本，检查 `AiModelConfigService` 中日志语句是否包含 `apiKey`、`Authorization`、`Bearer` 或直接输出新增/编辑 DTO
- 时间：2026-06-21 12:37:28 +08:00
- 结果：通过
- 结论：`ai-service` 模型配置新增/编辑日志不再输出完整 DTO 或 API Key，仅输出 `id`、`code`、`providerType` 等非密钥字段。

- 命令：`grep -E 'sk-test|sk-full|sk-secret|sk-existing|sk-detail|sk-full-secret' /tmp/x-boot-ai-service-test.log`
- 时间：2026-06-21 12:37:28 +08:00
- 结果：通过
- 结论：重新运行 `ai-service` 测试后的日志输出未出现测试 API Key 字符串。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am test`
- 时间：2026-06-21 12:36:35 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-web`、`x-boot-starter-tenant`、`x-boot-starter-test`、`x-boot-starter-ai`、`ai-facade`、`ai-service`
- 覆盖测试：`AiModelConfigTenantIsolationTest` 3 个用例，`AiModelConfigServiceTest` 7 个用例。
- 结论：API Key 安全方案文档和模型配置日志脱敏修复后，目标模块测试通过。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-21 12:37:28 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-redis`、`x-boot-starter-web`、`x-boot-starter-satoken`、`x-boot-starter-tenant`、`x-boot-starter-test`、`x-boot-starter-ai`、`sys-facade`、`oss-facade`、`ai-facade`、`admin-api`、`app-api`、`ai-service`、`sys-service`
- 覆盖测试：`AdminApiEventListenerTest` 2 个用例，`AdminSysUserControllerTest` 4 个用例，`AdminSysRoleControllerTest` 2 个用例，`AdminAiModelConfigControllerTest` 9 个用例，`AppAiChatControllerTest` 2 个用例，`AppAiChatServiceTest` 5 个用例，`AiModelConfigTenantIsolationTest` 3 个用例，`AiModelConfigServiceTest` 7 个用例，`SysMenuServiceTest` 8 个用例，`SysUserServiceTest` 6 个用例，`SysRoleServiceTest` 4 个用例，`ExampleUnitTest` 1 个用例。
- 结论：AI 模型配置模块全部任务闭环后，admin/app/ai/sys 组合基线通过。

- 命令：只读 Node 校验脚本，对比 `docs/project/AI_MODEL_CONFIG_INIT_SQL.sql` 与 `x-boot-modules/ai/ai-service/src/main/resources/sql/x_boot_ai.sql`
- 时间：2026-06-21 12:25:37 +08:00
- 结果：通过
- 结论：初始化样例 SQL 的 19 个插入字段与 `ai_model_config` 表结构一致；覆盖 `OLLAMA`、`DEEPSEEK`、`OPENAI_COMPATIBLE`、`OPENAI` 4 种当前支持供应商；未发现 `sk-`、`REPLACE_WITH` 等密钥形态；upsert 会在样例值为空时保留已有 `api_key`。

- 命令：`perl -ne 'if(/[ \t]$/){print "$ARGV:$.: trailing whitespace\n"; $bad=1} END{exit($bad ? 1 : 0)}' docs/project/AI_MODEL_CONFIG_INIT_SQL.sql`
- 时间：2026-06-21 12:25:37 +08:00
- 结果：通过
- 结论：新增 SQL 文件无行尾空白。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am test`
- 时间：2026-06-21 12:25:37 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-web`、`x-boot-starter-tenant`、`x-boot-starter-test`、`x-boot-starter-ai`、`ai-facade`、`ai-service`
- 覆盖测试：`AiModelConfigTenantIsolationTest` 3 个用例，`AiModelConfigServiceTest` 7 个用例。
- 结论：整理 AI 模型配置初始化样例 SQL 后，目标模块测试通过。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-21 12:25:53 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-redis`、`x-boot-starter-web`、`x-boot-starter-satoken`、`x-boot-starter-tenant`、`x-boot-starter-test`、`x-boot-starter-ai`、`sys-facade`、`oss-facade`、`ai-facade`、`admin-api`、`app-api`、`ai-service`、`sys-service`
- 覆盖测试：`AdminApiEventListenerTest` 2 个用例，`AdminSysUserControllerTest` 4 个用例，`AdminSysRoleControllerTest` 2 个用例，`AdminAiModelConfigControllerTest` 9 个用例，`AppAiChatControllerTest` 2 个用例，`AppAiChatServiceTest` 5 个用例，`AiModelConfigTenantIsolationTest` 3 个用例，`AiModelConfigServiceTest` 7 个用例，`SysMenuServiceTest` 8 个用例，`SysUserServiceTest` 6 个用例，`SysRoleServiceTest` 4 个用例，`ExampleUnitTest` 1 个用例。
- 结论：AI 模型配置初始化样例 SQL 整理后，admin/app/ai/sys 组合基线通过。

- 命令：`mvn -pl x-boot-api/admin-api -am test`
- 时间：2026-06-21 12:15:59 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-redis`、`x-boot-starter-web`、`x-boot-starter-satoken`、`x-boot-starter-test`、`sys-facade`、`oss-facade`、`ai-facade`、`admin-api`
- 覆盖测试：`AdminApiEventListenerTest` 2 个用例，`AdminSysUserControllerTest` 4 个用例，`AdminSysRoleControllerTest` 2 个用例，`AdminAiModelConfigControllerTest` 9 个用例。
- 结论：后台 AI 模型配置接口新增 API Key 序列化脱敏和专用权限注解回归测试后，`admin-api` 及依赖模块基线通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am test`
- 时间：2026-06-21 12:15:59 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-web`、`x-boot-starter-tenant`、`x-boot-starter-test`、`x-boot-starter-ai`、`ai-facade`、`ai-service`
- 覆盖测试：`AiModelConfigTenantIsolationTest` 3 个用例，`AiModelConfigServiceTest` 7 个用例。
- 结论：`ai-service` 已固定完整 API Key 内部保留和 `apiKeyMasked` 生成边界，目标模块测试通过。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-21 12:16:14 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-redis`、`x-boot-starter-web`、`x-boot-starter-satoken`、`x-boot-starter-tenant`、`x-boot-starter-test`、`x-boot-starter-ai`、`sys-facade`、`oss-facade`、`ai-facade`、`admin-api`、`app-api`、`ai-service`、`sys-service`
- 覆盖测试：`AdminApiEventListenerTest` 2 个用例，`AdminSysUserControllerTest` 4 个用例，`AdminSysRoleControllerTest` 2 个用例，`AdminAiModelConfigControllerTest` 9 个用例，`AppAiChatControllerTest` 2 个用例，`AppAiChatServiceTest` 5 个用例，`AiModelConfigTenantIsolationTest` 3 个用例，`AiModelConfigServiceTest` 7 个用例，`SysMenuServiceTest` 8 个用例，`SysUserServiceTest` 6 个用例，`SysRoleServiceTest` 4 个用例，`ExampleUnitTest` 1 个用例。
- 结论：API Key 脱敏和查看权限测试补充后，admin/app/ai/sys 组合基线通过。

- 命令：`mvn -pl x-boot-modules/ai/ai-service -am test`
- 时间：2026-06-21 12:01:41 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-web`、`x-boot-starter-tenant`、`x-boot-starter-test`、`x-boot-starter-ai`、`ai-facade`、`ai-service`
- 覆盖测试：`AiModelConfigTenantIsolationTest` 3 个用例，`AiModelConfigServiceTest` 6 个用例。
- 结论：`ai-service` 已接入租户 starter；普通租户访问 `ai_model_config` 会生成 `tenant_id` 行级条件，特权租户可按既有规则绕过租户条件。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-21 12:02:38 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-redis`、`x-boot-starter-web`、`x-boot-starter-websocket`、`x-boot-starter-satoken`、`x-boot-starter-tenant`、`x-boot-starter-test`、`x-boot-starter-ai`、`sys-facade`、`oss-facade`、`ai-facade`、`admin-api`、`app-api`、`ai-service`、`sys-service`
- 覆盖测试：`AdminApiEventListenerTest` 2 个用例，`AdminSysUserControllerTest` 4 个用例，`AdminSysRoleControllerTest` 2 个用例，`AdminAiModelConfigControllerTest` 6 个用例，`AppAiChatControllerTest` 2 个用例，`AppAiChatServiceTest` 5 个用例，`AiModelConfigTenantIsolationTest` 3 个用例，`AiModelConfigServiceTest` 6 个用例，`SysMenuServiceTest` 8 个用例，`SysUserServiceTest` 6 个用例，`SysRoleServiceTest` 4 个用例，`ExampleUnitTest` 1 个用例。
- 结论：模型配置租户隔离测试和完整相关基线通过；新增租户 starter 未破坏 admin/app/ai/sys 组合测试。

- 命令：只读 Node 校验脚本，确认 `docs/project/RBAC_PERMISSION_CODES.md`、`docs/project/RBAC_INIT_SQL.sql` 与 `docs/project/RBAC_PERMISSION_MODEL.md` 的 RBAC 权限模型引用关系
- 时间：2026-06-20 22:12:57 +08:00
- 结果：通过
- 结论：42 个后台权限码仍完整覆盖在初始化 SQL 中；权限模型文档已明确引用权限清单、初始化 SQL、`sys_menu.permission`、`SysMenuService#getRoleIdPermissionMap` 和“不新增独立 `sys_permission` 表”的当前结论。

- 命令：`mvn -pl x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-20 22:13:44 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-web`、`x-boot-starter-test`、`sys-facade`、`sys-service`
- 覆盖测试：`SysMenuServiceTest` 8 个用例，`SysUserServiceTest` 6 个用例，`SysRoleServiceTest` 4 个用例，`ExampleUnitTest` 1 个用例。
- 结论：补充 `sys_menu.permission` 权限链路回归测试后，`sys-service` 及依赖模块基线通过。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-20 22:14:05 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-redis`、`x-boot-starter-web`、`x-boot-starter-satoken`、`x-boot-starter-test`、`x-boot-starter-ai`、`sys-facade`、`oss-facade`、`ai-facade`、`admin-api`、`app-api`、`ai-service`、`sys-service`
- 结论：`sys_menu.permission` 扩展性确认后，相关 API、AI 与 sys 模块组合基线通过。

- 命令：只读 Node 校验脚本，对比 `docs/project/RBAC_PERMISSION_CODES.md` 与 `docs/project/RBAC_INIT_SQL.sql` 权限码，并检查菜单节点与角色授权数量
- 时间：2026-06-20 22:06:13 +08:00
- 结果：通过
- 结论：权限码清单与初始化 SQL 均覆盖 42 个唯一权限码，无缺失无多余；初始化 SQL 包含 45 个菜单节点、3 个目录节点、42 个权限节点；超级管理员绑定 45 个节点，租户管理员绑定 41 个节点并默认排除平台级租户管理节点。

- 命令：`mvn -pl x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-20 22:06:33 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-web`、`x-boot-starter-test`、`sys-facade`、`sys-service`
- 覆盖测试：`SysMenuServiceTest` 5 个用例，`SysUserServiceTest` 6 个用例，`SysRoleServiceTest` 4 个用例，`ExampleUnitTest` 1 个用例。
- 结论：RBAC 初始化 SQL 整理后，`sys-service` 及依赖模块基线通过。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-20 22:06:47 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-redis`、`x-boot-starter-web`、`x-boot-starter-satoken`、`x-boot-starter-test`、`x-boot-starter-ai`、`sys-facade`、`oss-facade`、`ai-facade`、`admin-api`、`app-api`、`ai-service`、`sys-service`
- 结论：RBAC 初始化 SQL 整理后，相关 API、AI 与 sys 模块组合基线通过。

- 命令：只读 Node 校验脚本，从 `admin-api` Controller 反向提取 `@SaCheckPermission` 唯一权限码并检查 `docs/project/RBAC_PERMISSION_CODES.md`
- 时间：2026-06-20 21:56:18 +08:00
- 结果：通过
- 结论：源码中 42 个唯一权限码均已在权限码清单文档中覆盖。

- 命令：`mvn -pl x-boot-api/admin-api -am test`
- 时间：2026-06-20 21:56:34 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-redis`、`x-boot-starter-web`、`x-boot-starter-satoken`、`x-boot-starter-test`、`sys-facade`、`oss-facade`、`ai-facade`、`admin-api`
- 覆盖测试：`AdminApiEventListenerTest` 2 个用例，`AdminSysUserControllerTest` 4 个用例，`AdminSysRoleControllerTest` 2 个用例，`AdminAiModelConfigControllerTest` 6 个用例。
- 结论：权限码清单整理后，`admin-api` 及依赖模块基线通过。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-20 21:56:49 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-redis`、`x-boot-starter-web`、`x-boot-starter-satoken`、`x-boot-starter-test`、`x-boot-starter-ai`、`sys-facade`、`oss-facade`、`ai-facade`、`admin-api`、`app-api`、`ai-service`、`sys-service`
- 结论：权限码清单整理后，相关 API、AI 与 sys 模块组合基线通过。

- 命令：`mvn -pl x-boot-api/admin-api -am test`
- 时间：2026-06-20 21:50:30 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-redis`、`x-boot-starter-web`、`x-boot-starter-satoken`、`x-boot-starter-test`、`sys-facade`、`oss-facade`、`ai-facade`、`admin-api`
- 覆盖测试：`AdminApiEventListenerTest` 2 个用例，`AdminSysUserControllerTest` 4 个用例，`AdminSysRoleControllerTest` 2 个用例，`AdminAiModelConfigControllerTest` 6 个用例。
- 结论：踢下线回归测试补充后，`admin-api` 及依赖模块基线通过。

- 命令：`mvn -pl x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-20 21:50:49 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-web`、`x-boot-starter-test`、`sys-facade`、`sys-service`
- 覆盖测试：`SysMenuServiceTest` 5 个用例，`SysUserServiceTest` 6 个用例，`SysRoleServiceTest` 4 个用例，`ExampleUnitTest` 1 个用例。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-20 21:51:02 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-redis`、`x-boot-starter-web`、`x-boot-starter-satoken`、`x-boot-starter-test`、`x-boot-starter-ai`、`sys-facade`、`oss-facade`、`ai-facade`、`admin-api`、`app-api`、`ai-service`、`sys-service`
- 结论：踢下线回归测试补充后，相关 API、AI 与 sys 模块组合基线通过。

- 命令：`mvn -pl x-boot-api/admin-api -am test`
- 时间：2026-06-20 21:44:57 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-redis`、`x-boot-starter-web`、`x-boot-starter-satoken`、`x-boot-starter-test`、`sys-facade`、`oss-facade`、`ai-facade`、`admin-api`
- 覆盖测试：`AdminSysRoleControllerTest` 2 个用例，`AdminAiModelConfigControllerTest` 6 个用例。
- 结论：权限缓存 API 编排测试补充后，`admin-api` 及依赖模块基线通过。

- 命令：`mvn -pl x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-20 21:45:14 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-web`、`x-boot-starter-test`、`sys-facade`、`sys-service`
- 覆盖测试：`SysMenuServiceTest` 5 个用例，`SysUserServiceTest` 6 个用例，`SysRoleServiceTest` 4 个用例，`ExampleUnitTest` 1 个用例。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-20 21:45:26 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-redis`、`x-boot-starter-web`、`x-boot-starter-satoken`、`x-boot-starter-test`、`x-boot-starter-ai`、`sys-facade`、`oss-facade`、`ai-facade`、`admin-api`、`app-api`、`ai-service`、`sys-service`
- 结论：权限缓存测试补充后，相关 API、AI 与 sys 模块组合基线通过。

- 命令：`mvn -pl x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-20 21:40:16 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-web`、`x-boot-starter-test`、`sys-facade`、`sys-service`
- 覆盖测试：`SysMenuServiceTest` 5 个用例，`SysUserServiceTest` 6 个用例，`SysRoleServiceTest` 4 个用例，`ExampleUnitTest` 1 个用例。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-20 21:40:33 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-redis`、`x-boot-starter-web`、`x-boot-starter-satoken`、`x-boot-starter-test`、`x-boot-starter-ai`、`sys-facade`、`oss-facade`、`ai-facade`、`admin-api`、`app-api`、`ai-service`、`sys-service`
- 结论：菜单管理服务测试补充后，相关 API、AI 与 sys 模块组合基线通过。

- 命令：`mvn -pl x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-20 19:37:32 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-web`、`x-boot-starter-test`、`sys-facade`、`sys-service`
- 覆盖测试：`SysUserServiceTest` 6 个用例，`SysRoleServiceTest` 4 个用例，`ExampleUnitTest` 1 个用例。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-20 19:37:46 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-redis`、`x-boot-starter-web`、`x-boot-starter-satoken`、`x-boot-starter-test`、`x-boot-starter-ai`、`sys-facade`、`oss-facade`、`ai-facade`、`admin-api`、`app-api`、`ai-service`、`sys-service`
- 结论：角色管理服务测试补充后，相关 API、AI 与 sys 模块组合基线通过。

- 命令：`mvn -pl x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-20 19:32:12 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-web`、`x-boot-starter-test`、`sys-facade`、`sys-service`
- 覆盖测试：`SysUserServiceTest` 6 个用例，`ExampleUnitTest` 1 个用例。

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-20 19:32:34 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-redis`、`x-boot-starter-web`、`x-boot-starter-satoken`、`x-boot-starter-test`、`x-boot-starter-ai`、`sys-facade`、`oss-facade`、`ai-facade`、`admin-api`、`app-api`、`ai-service`、`sys-service`
- 结论：用户管理服务测试补充后，相关 API、AI 与 sys 模块组合基线通过。

- 命令：`mvn -pl x-boot-modules/sys/sys-service -am checkstyle:check`
- 时间：2026-06-20 19:32:59 +08:00
- 结果：失败
- 失败模块：`x-boot-core`
- 失败原因：既有 `sun_checks.xml` 规则大量历史问题，例如缺少 `package-info.java`、Javadoc、80 字符行宽、`FinalParameters` 等；构建在 `x-boot-core` 阶段失败，未执行到 `sys-service`。

- 命令：`mvn -pl x-boot-modules/sys/sys-service checkstyle:check`
- 时间：2026-06-20 19:33:15 +08:00
- 结果：失败
- 失败模块：`sys-service`
- 失败原因：既有 `src/main` 代码存在大量 `sun_checks.xml` 规则问题，例如 Javadoc、80 字符行宽、`FinalParameters`、`DesignForExtension`、星号导入等；本轮新增测试文件不是输出中的失败来源。

- 命令：`mvn -pl x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-20 19:24:52 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-web`、`x-boot-starter-test`、`sys-facade`、`sys-service`

- 命令：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`
- 时间：2026-06-20 19:25:05 +08:00
- 结果：通过
- 覆盖模块：`x-boot-core`、`x-boot-starter-aop`、`x-boot-starter-cloud`、`x-boot-starter-crud`、`x-boot-starter-dubbo`、`x-boot-starter-i18n`、`x-boot-starter-redis`、`x-boot-starter-web`、`x-boot-starter-satoken`、`x-boot-starter-test`、`x-boot-starter-ai`、`sys-facade`、`oss-facade`、`ai-facade`、`admin-api`、`app-api`、`ai-service`、`sys-service`
- 结论：本轮已解除 `sys-service` 测试基线阻塞，相关 API、AI 与 sys 模块组合基线通过。

## 当前阻塞

- 文档定位已调整为后台企业 AI 平台，暂无文档阻塞。
- 当前指定 `ai-service` 目标测试组合存在测试隔离阻塞：`AiKnowledgeDocumentIndexServiceTest` 单独组合运行时缺少 MyBatis-Plus lambda cache 初始化。
- Checkstyle 基线存在大量历史问题，需后续单独治理后才能作为稳定质量门禁。

## 下一步

1. 下一轮开发从 `docs/project/TASKS.md` 中第一个未完成且依赖满足的任务开始：新增工具鉴权配置。
2. 后续新增服务或接口继续遵循 `admin-api -> ai-facade -> ai-service -> mapper`。
3. 新增工具/MCP 能力时，先明确工具注册、鉴权、审计和工作流 HTTP 工具节点之间的职责边界。
4. 若继续治理验证问题，优先补齐 `AiKnowledgeDocumentIndexServiceTest` 的 MyBatis-Plus 实体元数据初始化，确保指定目标组合可独立通过。
5. 完成目标模块后优先运行相关 ai-service、ai-facade 和 admin-api 测试。
6. 若通过，再运行完整基线：`mvn -pl x-boot-api/admin-api,x-boot-api/app-api,x-boot-modules/ai/ai-service,x-boot-modules/sys/sys-service -am test`。
