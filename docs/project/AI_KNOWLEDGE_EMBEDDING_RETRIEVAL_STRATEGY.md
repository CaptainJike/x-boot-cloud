# AI Knowledge Embedding And Retrieval Strategy

最后更新：2026-06-24

## 目标

为知识库/RAG 建立向量化和检索抽象，并提供可运行的 OpenAI 兼容 embedding 与 Qdrant 向量库适配。

当前已完成内部模型、SPI、编排服务、默认保护实现、OpenAI 兼容 embedding provider、Qdrant vector store、后台基础检索接口和检索日志写入。

## 分层边界

知识库检索保持标准链路：

`admin-api -> AiKnowledgeRetrievalFacade -> ai-service -> mapper/vector store`

内部拆分为：

`chunk -> AiKnowledgeEmbeddingProvider -> AiKnowledgeVectorStore -> retrieval hits`

其中：

- `AiKnowledgeEmbeddingContext` 描述一次向量化所需的模型配置快照。
- `AiKnowledgeEmbeddingProvider` 负责把文本转换为向量。
- `AiKnowledgeEmbeddingProviderService` 负责选择 provider、校验请求、补齐维度和向量哈希。
- `AiKnowledgeVectorStore` 负责向量写入、删除和相似度搜索。
- `AiKnowledgeVectorRetrievalService` 负责编排 query embedding 和向量库检索。

## Embedding Provider 抽象与实现

`AiKnowledgeEmbeddingProvider` 是 provider 扩展点：

- 已提供 `OpenAiCompatibleAiKnowledgeEmbeddingProvider`，支持 OpenAI、OpenAI 兼容供应商和 DeepSeek 类型，通过 `baseUrl` 区分官方、百炼兼容代理或私有网关。
- Ollama 本地 embedding 后续可新增独立 provider。
- 其它供应商继续通过 `AiKnowledgeEmbeddingProvider` 扩展。

provider 选择规则：

- 由 `AiKnowledgeEmbeddingContext.providerType` 决定候选 provider。
- `modelName` 必须存在，避免同一 provider 下混用不同维度的向量。
- provider 输出空向量时视为无效向量化配置。
- provider 未匹配时抛出“不支持的知识库向量化供应商”。

## Vector Store 抽象与实现

`AiKnowledgeVectorStore` 是向量库扩展点：

- `upsert`：写入或更新切片向量。
- `deleteByDocumentId`：删除某个文档的全部向量。
- `search`：按 query vector、知识库范围、召回数量和相似度阈值检索。

当前提供两类实现：

- `UnavailableAiKnowledgeVectorStore`：未启用真实向量库时，所有写入、删除和搜索都会抛出“知识库向量存储不可用”。
- `QdrantAiKnowledgeVectorStore`：在存在租户上下文时按租户、知识库、文档和切片写入 payload，并按 `tenantId + knowledgeBaseId` 过滤检索；未启用租户或上下文为空时退化为按知识库、文档和切片处理，并按模型配置和维度隔离 collection。

后续可以继续接入：

- Spring AI `VectorStore` 适配器。
- Milvus、PGVector、Elasticsearch dense vector、Redis Vector 或其它专用向量库。
- 测试环境内存向量库，但不能作为生产默认实现。

Qdrant 配置来源：

- `x-boot-modules/ai/ai-service/src/main/resources/qdrant.yml`。
- `x.ai.knowledge.vector-store.*` 配置项。
- 环境变量：`AI_KNOWLEDGE_VECTOR_STORE_ENABLED`、`AI_KNOWLEDGE_VECTOR_STORE_TYPE`、`QDRANT_HOST`、`QDRANT_GRPC_PORT`、`QDRANT_API_KEY`、`QDRANT_USE_TLS`、`QDRANT_COLLECTION_PREFIX`、`QDRANT_DISTANCE`、`QDRANT_INITIALIZE_SCHEMA`、`QDRANT_TIMEOUT`。

## 多 Provider 检索策略

不同 embedding provider 或不同模型名生成的向量不能混合检索。

当前基础检索服务要求同一次请求内多个知识库使用相同 `embeddingModelConfigCode`，否则返回无效向量化配置。后续如果要支持跨 provider 混合检索，应按以下方式扩展：

- 加载启用知识库，并读取其 embedding 快照。
- 按 `providerType + baseUrl + modelName` 分组。
- 每组单独生成 query embedding。
- 每组分别调用向量库检索。
- 合并命中后按相似度降序截断到 `topK`。
- 如果同一次请求包含多个不兼容 embedding 分组，应在检索日志里记录每组耗时和命中数量。

## 状态流转

切片向量化任务按以下状态流转：

- 开始向量化：`embedding_status=2`，清空 `error_message`。
- 向量化成功：`embedding_status=1`，写入模型快照、`vector_id` 和 `vector_hash`。
- 向量化失败：`embedding_status=0`，写入 `error_message`。

文档级 `embedding_status` 由索引服务按执行结果更新：

- 全部切片成功：文档为成功。
- 存在失败切片：文档为失败。
- 存在处理中切片：文档为处理中。
- 没有切片或全是待处理：文档为待处理。

## 租户与安全

- 多租户启用且存在租户上下文时，向量写入和检索必须沿用当前租户上下文执行。
- 向量库 metadata 至少包含 `knowledgeBaseId`、`documentId` 和 `chunkId`；存在租户上下文时额外包含 `tenantId`。
- 检索过滤至少按知识库 ID 过滤；存在租户上下文时再叠加租户过滤。
- API Key 只来自服务端 `AiModelConfigBO` 内部字段，不通过 Facade 或 HTTP 响应返回。
- 不允许 Controller 直接传入任意 provider 凭证或向量库过滤条件。

## 验收范围

已完成验收项：

- 已新增 embedding 上下文、请求、响应和向量检索模型。
- 已新增 `AiKnowledgeEmbeddingProvider` 和 `AiKnowledgeVectorStore` 两类 SPI。
- 已新增 provider 选择服务、OpenAI 兼容 provider 和向量检索编排服务。
- 已新增未配置向量库时的保护性默认实现。
- 已新增 Qdrant vector store、配置开关、collection/payload/filter 规则，以及兼容多租户和无租户上下文的过滤行为。
- 已新增基础检索接口和检索日志写入。
- 已补充单元测试覆盖 provider 选择、无 provider、无效配置、查询向量化、默认检索参数、向量库未配置失败、Qdrant 配置和 Qdrant 检索过滤。

非本任务范围：

- 不实现 Ollama 或其它非 OpenAI 兼容 embedding provider。
- 不实现 Milvus、PGVector、Elasticsearch、Redis Vector 等其它向量库。
- 不实现跨 provider 混合检索和重排序。
