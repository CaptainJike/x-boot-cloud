# AI 知识库/RAG 模块职责说明

最后更新：2026-06-24

## 服务：ai-service 知识库/RAG 子域

服务作用：
- 负责后台企业 AI 平台的知识库、知识库文档、文档切片、检索日志和 RAG 上下文编排。

业务能力：
- 管理知识库的列表、详情、新增、编辑、删除、启停和基础配置。
- 管理知识库文档与 OSS 文件的关联、解析状态、切片状态、失败原因和重试入口。
- 通过 `AiKnowledgeDocumentIndexService` 执行文档解析、切片、embedding、向量库写入和状态回写。
- 提供基础检索能力，按知识库、查询内容和召回参数返回可用于 AI 回答的引用片段。
- 为后台 AI 对话提供 RAG 上下文，由 `AiChatService` 组合模型回答和引用来源。
- 记录检索日志，保留查询内容、召回片段、耗时、状态、错误摘要和会话/消息关联。

拥有数据：
- `ai_knowledge_base`：知识库主表，保存名称、描述、启停状态、检索配置和模型配置快照。
- `ai_knowledge_document`：知识库文档表，保存 OSS 文件关联、解析状态、切片状态、失败原因和重试信息。
- `ai_knowledge_document_chunk`：文档切片表，保存切片内容、序号、来源定位、token 预估和向量化预留字段。
- `ai_knowledge_retrieval_log`：检索日志表，保存查询、召回、耗时、状态、错误和会话/消息关联。

提供的 Facade：
- `AiKnowledgeBaseFacade`：提供知识库分页、详情、新增、编辑、删除和启停能力。
- `AiKnowledgeDocumentFacade`：提供文档关联、文档列表、详情、解析状态、切片状态、失败原因和重试能力。
- `AiKnowledgeRetrievalFacade`：提供基础检索、RAG 上下文召回和检索日志查询能力。

消费的 Facade：
- `OssFileInfoFacade`：校验后台选择的 OSS 文件是否存在，并读取文件名、扩展名、大小、MD5 和存储平台等元数据。
- `OssUploadDownloadFacade`：在文档索引任务中按文件 ID 下载原始文件内容。
- 不直接访问 `oss-service` 的 Mapper、Entity 或存储实现。

HTTP 入口：
- `admin-api`：已新增知识库管理、文档管理、切片查询、基础检索、检索日志和 RAG 对话选择知识库等后台接口。
- `app-api`：无；`app-api` 不作为本轮 Pig AI 后台平台 MVP 入口。
- MVP 标准链路：`admin-api -> ai-facade -> ai-service -> mapper`。

推荐调用链路：
- 知识库管理：`AdminAiKnowledgeBaseController -> AiKnowledgeBaseFacade -> AiKnowledgeBaseService -> AiKnowledgeBaseMapper`。
- 文档管理：`AdminAiKnowledgeDocumentController -> AiKnowledgeDocumentFacade -> AiKnowledgeDocumentService -> Mapper -> OssFileInfoFacade/OssUploadDownloadFacade`。
- 文档索引：`AiKnowledgeDocumentService -> AiKnowledgeDocumentIndexService -> ParseStrategy -> EmbeddingProvider -> AiKnowledgeVectorStore`。
- 基础检索：`AdminAiKnowledgeRetrievalController -> AiKnowledgeRetrievalFacade -> AiKnowledgeRetrievalService -> AiKnowledgeVectorRetrievalService -> AiKnowledgeVectorStore`。
- RAG 对话：`AdminAiChatController -> AiChatFacade -> AiChatService -> AiKnowledgeRetrievalService -> AiKnowledgeVectorStore -> XBootAiService`。

租户行为：
- 知识库、文档、切片和检索日志均为行级租户数据，依赖 `TenantContextHolder` 和 MyBatis-Plus 租户插件隔离。
- 后台上传或选择 OSS 文件时，必须确保 OSS 文件归属与当前租户上下文一致；特权租户绕过规则沿用现有租户插件能力。
- 检索和 RAG 对话只能读取当前租户可见且启用、解析成功、切片成功的知识库内容。
- 当前不支持匿名知识库访问；如后续新增匿名能力，必须先明确用户上下文和租户上下文来源。

安全行为：
- 后台知识库接口要求后台登录态。
- 当前权限码前缀为 `AiKnowledge:`，覆盖 `create`、`retrieve`、`update`、`delete`、`enable` 和 `retry`。
- 文档索引、切片重试、基础检索和 RAG 召回必须记录操作者、租户、状态和错误摘要。
- 不在 HTTP 响应中返回文件直链密钥、对象存储凭证、模型 API Key 或内部向量索引连接信息。

不负责：
- 不负责文件二进制存储、对象存储直链生成或 OSS 元数据持久化，这些能力归 `oss-service`。
- 不负责用户账号、角色、菜单、租户主数据或后台权限缓存。
- 不负责 C 端 app 用户体系和 C 端 AI 产品入口。
- 不负责把知识库能力直接暴露为匿名公网检索接口。
- 不负责供应商私有向量库的底层连接细节；后续如接入外部向量库，应通过独立适配层隔离。
