# Learning OS 服务职责边界

## 服务目的

`learning-service` 负责把学习目标、学习地图、Tutor、Practice、Review、Reflection、Mastery、Learner Memory 与成长记录组织成同一条学习闭环，并为 App API 提供稳定的 Dubbo Facade。

## 当前能力

- 创建学习目标并生成学习地图。
- 管理 Tutor 会话、诊断轮次和节点进度。
- 为当前学习节点生成并持久化服务端权威 `PracticeTask`。
- 保存 `PracticeAttempt` 草稿与完成记录，支持 `mutationId` 幂等重试和 `baseVersion` 跨设备冲突检测。
- 生成并持久化服务端权威 `ReviewTask`，按到期时间、掌握度与遗忘风险组织复盘队列。
- 保存 `ReviewAttempt` 草稿与完成记录，支持 `mutationId` 幂等重试和 `baseVersion` 跨设备冲突检测。
- 保存每日反思并生成成长摘要。
- 统一记录 Tutor / Practice / Review / Reflection 事件，形成可审计的最小学习历史。
- 聚合服务端 `MasteryRecord`，优先消费统一事件投影，让节点掌握度可跨设备读取。
- 聚合服务端 `LearnerMemory`，优先消费统一事件投影，让 Dashboard 不再只依赖前端本地推导。
- 聚合服务端 `LearningPlan`，优先消费统一事件投影，为 Today Mission、复习队列、回补建议、恢复模式和计划重排原因提供跨设备一致的读模型。
- 聚合服务端 `LearningRhythm`，优先消费统一事件投影与已持久化学习动作，为周节奏、恢复建议和周计划提供跨设备一致的读模型。
- 聚合服务端 `LearningKnowledgeGraph`，基于学习地图、节点进度、Tutor、Practice 与 Growth 信号生成概念关系、薄弱链路和证据视图。
- 聚合服务端 `LearningAgent`，基于 Today、Plan、Memory、Rhythm、Knowledge 与最新 Tutor/Practice/Review/Reflection 证据生成主动介入建议、续学议程和场景提醒。
- 基于统一事件回放生成服务端 `ReplanTimeline`，输出计划历史条目、变化字段和触发重排的审计信息。

## 拥有的数据

`learning-service` 独占学习域表，包括 `learning_goal`、`learning_map`、`learning_map_node`、`learning_node_progress`、`tutor_session`、`tutor_turn`、`practice_task`、`practice_attempt`、`review_task`、`review_attempt`、`reflection_entry`、`daily_digest`、`growth_snapshot` 与 `learning_event`。

Practice 证据中的文件只保存文件名、MIME、大小等元数据；文件二进制与对象存储生命周期不属于本服务，后续应通过 OSS Facade 接入。

当前 `MasteryRecord / LearnerMemory / LearningPlan / LearningRhythm / LearningKnowledgeGraph` 仍作为读模型，不额外新增规划表；它们现在会优先消费 `learning_event` 的读侧投影，并在历史存量数据上回退到 `tutor_turn / practice_attempt / review_attempt / daily_digest / growth_snapshot / learning_map_node / learning_node_progress` 聚合结果。`Tutor / Practice / Review / Reflection` 已开始写入统一 `learning_event` 事件表，用于最小版本历史和时间线整合，其中 `LearningPlan` 已可按事件回放重建 `ReplanTimeline` 和计划历史条目，并让 Review 结果影响计划重排与复盘优先级；`LearningRhythm` 已可聚合最近 7 天的学习节奏；`LearningKnowledgeGraph` 已可输出概念节点、前置关系、薄弱路径和包含 Review 的证据摘要。后续重点是继续增强定时调度、细粒度排程依据与更长期的分析能力，而不是重新引入新的本地权威状态。

## 对外提供

- `LearningGoalFacade`
- `LearningMapFacade`
- `LearningTutorFacade`
- `LearningPracticeFacade`
- `LearningReviewFacade`
- `LearningReflectionFacade`
- `LearningGrowthFacade`
- `LearningAgentFacade`
- `LearningLearnerFacade`
- `LearningPlanFacade`

## 外部依赖

当前学习域消费 AI Facade 生成学习地图、Tutor 决策和反思摘要。Practice P0 使用确定性规则生成基础任务，不直接依赖外部 AI，保证核心练习在模型不可用时仍可工作。

## HTTP 入口

HTTP 只由 `app-api` 暴露，Practice / Review 入口为：

- `GET /app/v1/learning/practice/workspace?goalId={goalId}`
- `PUT /app/v1/learning/practice/attempts/{taskKey}`
- `GET /app/v1/learning/review/workspace?goalId={goalId}`
- `PUT /app/v1/learning/review/attempts/{taskKey}`
- `GET /app/v1/learning/mastery/records?goalId={goalId}`
- `GET /app/v1/learning/memory?goalId={goalId}`
- `GET /app/v1/learning/knowledge?goalId={goalId}`
- `GET /app/v1/learning/rhythm?goalId={goalId}`
- `GET /app/v1/learning/agent?goalId={goalId}`
- `GET /app/v1/learning/plans/current?goalId={goalId}`
- `GET /app/v1/learning/plans/replan-timeline?goalId={goalId}`
- `GET /app/v1/learning/growth/timeline`

Controller 仅负责协议适配和参数校验，业务规则位于 `LearningPracticeService`、`LearningReviewService`、`LearningLearnerService`、`LearningPlanService` 等领域服务。

## 租户与用户隔离

学习域沿用框架行级租户隔离，所有表包含 `tenant_id`。在此基础上，Practice / Review 的每次查询与写入还必须显式匹配 `UserContextHolder` 中的 `userId`、目标归属和任务归属，不能依赖客户端传入用户身份。

## 安全与并发

- App HTTP 入口要求 App 登录态。
- `mutationId` 相同的重试返回已保存记录，不重复写入。
- 更新必须携带当前 `baseVersion`，服务端以条件更新保证原子性；版本不一致返回 409，客户端必须重新拉取工作区后再合并。
- Practice 完成态提交必须包含回答、证据与规则评测；Review 完成态提交必须包含回答和自评；草稿可以不完整。

## 不负责

- 不保存文件二进制。
- 不在 Controller 或 Facade 实现中编排领域规则。
- 不接受前端自行创建权威任务；前端生成任务仅作为后端不可用时的离线降级。
- 不负责认证签发、OSS 存储、通知投递和通用 AI 模型配置。
