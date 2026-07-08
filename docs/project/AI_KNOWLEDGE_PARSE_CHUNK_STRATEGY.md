# AI Knowledge Parse And Chunk Strategy

最后更新：2026-06-24

## 目标

为知识库/RAG 建立稳定的文档解析与切片边界，让向量化、检索和 RAG 对话复用同一输入输出模型。

当前已提供策略抽象、默认实现和 `AiKnowledgeDocumentIndexService` 编排。文档关联或重试后会在事务提交后触发索引，完成 OSS 来源加载、解析、切片落库、embedding、向量库写入和状态回写；暂不提供独立异步调度器。

## 调用链路

标准链路保持：

`admin-api -> AiKnowledgeDocumentFacade -> ai-service -> AiKnowledgeDocumentSourceService -> OssUploadDownloadFacade`

解析与切片在 `ai-service` 内部完成，并由 `AiKnowledgeDocumentIndexService` 写入切片表：

`AiKnowledgeDocumentSource -> AiKnowledgeParsedDocument -> AiKnowledgeDocumentChunkDraft`

其中：

- `AiKnowledgeDocumentSource` 负责封装文档 ID、知识库 ID、OSS 文件 ID、文件名、扩展名、文件字节或直链信息。
- `AiKnowledgeParsedDocument` 负责封装解析后的完整文本和段落列表。
- `AiKnowledgeParsedSection` 负责保留段落标题、页码和来源位置。
- `AiKnowledgeDocumentChunkDraft` 负责封装待落库切片内容、预览、顺序、来源位置和 Token 预估。

## 解析策略

解析入口为 `AiKnowledgeDocumentParseStrategyService`。

默认策略：

- `PlainTextAiKnowledgeDocumentParseStrategy` 支持 `txt`、`text`、`md`、`markdown`。
- 默认按 UTF-8 解码文件字节。
- 默认将 CRLF/CR 规范化为 LF。
- 默认按空行拆分段落，Markdown 标题行会作为段落标题。
- 空字节抛出“知识库文档源文件为空”。
- 空文本抛出“知识库文档解析内容为空”。
- 未命中解析策略抛出“不支持的知识库文档解析类型”。

后续扩展策略：

- PDF 可新增独立 `PdfAiKnowledgeDocumentParseStrategy`。
- Word 可新增独立 `WordAiKnowledgeDocumentParseStrategy`。
- OCR 可新增独立 `OcrAiKnowledgeDocumentParseStrategy`。
- 对象存储直链解析需要显式设计鉴权、租户上下文和下载方式，不在当前 MVP 默认实现中隐式访问外部 URL。

## 切片策略

切片入口为 `AiKnowledgeDocumentChunkStrategy`。

默认策略：

- `DefaultAiKnowledgeDocumentChunkStrategy` 使用最大 Token、重叠 Token 和 Token/字符比例转换为字符切片窗口。
- 默认配置为 `maxTokens=500`、`overlapTokens=80`、`tokenCharRatio=2`。
- 切分优先级为段落、换行、句号，最后按固定长度截断。
- 相邻切片保留配置化重叠文本，减少检索时上下文断裂。
- 每个切片生成 200 字符预览。
- 当前 Token 数按去空白字符长度估算，后续接入真实 tokenizer 时只替换估算逻辑。
- `overlapTokens >= maxTokens` 或非正配置抛出“无效知识库文档切片配置”。

## 状态流转

当前索引服务按以下状态流转：

- 待处理：文档关联或重试后置为 `parse_status=3`、`chunk_status=3`、`embedding_status=3`。
- 开始索引：`parse_status=2`、`chunk_status=2`、`embedding_status=2`，清空解析和切片错误。
- 解析成功：切片进入落库阶段。
- 解析失败：`parse_status=0`，写入 `parse_error_message`。
- 切片成功：批量写入 `ai_knowledge_document_chunk`，等待 embedding 成功后更新 `chunk_count`。
- 切片失败：`chunk_status=0`，写入 `chunk_error_message`。
- embedding 成功：切片 `embedding_status=1`，写入模型快照、`vector_id` 和 `vector_hash`；文档 `parse_status=1`、`chunk_status=1`、`embedding_status=1`。
- embedding 失败：文档 `parse_status=1`、`chunk_status=1`、`embedding_status=0`，切片写入错误摘要。

切片落库应放在事务内执行。重新解析同一文档时，应先删除旧切片，再按 `chunk_no` 顺序写入新切片，避免新旧切片混用。

## 租户与安全

- 解析和切片运行在 `ai-service`，依赖当前请求或任务恢复出的租户上下文。
- 文档来源必须来自已绑定的 OSS 文件 ID，不允许从 Controller 直接传入任意外部 URL。
- 后续如果改为独立异步任务，必须显式恢复租户上下文后再读文档、删旧切片、写新切片。
- 原始文件字节只作为内部处理输入，不通过 Facade 或后台 HTTP 响应返回。

## 验收范围

本任务验收项：

- 已新增解析策略接口和纯文本/Markdown 默认实现。
- 已新增切片策略接口和默认切片实现。
- 已新增解析结果、段落、切片配置和切片草稿模型。
- 已新增策略服务，统一选择解析策略并调用切片策略。
- 已新增 `AiKnowledgeDocumentIndexService`，串联来源加载、解析、切片落库、embedding、向量库写入和状态回写。
- 已补充单元测试覆盖支持格式、空内容、未知格式、重叠切片、非法配置、索引成功和向量库失败。

非本任务范围：

- 不提供独立异步调度器。
- 不接入 PDF、Word、OCR 或真实 tokenizer。
- 不把原始文件字节、模型 API Key 或向量库连接信息暴露给 Facade/HTTP 响应。
