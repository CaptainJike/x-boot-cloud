# x-boot-cloud 架构说明

本文件用于向架构师、开发与运维工程师清晰呈现 x-boot-cloud 的架构思想、模块边界、通信模式、非功能需求与运维建议。内容基于项目 pom 与模块结构自动生成并结合行业最佳实践。

目录
- 总体观念
- 模块划分与职责
- 运行时组件与依赖
- 服务通信与契约管理
- 数据策略与一致性
- 弹性、容错与安全
- 监控、日志与追踪
- 构建 / 部署 / CI-CD
- 常见故障排查建议

一、总体观念
- 每个模块职责单一（单一职责），对外只暴露 HTTP/REST 或 RPC 接口
- 所有对外流量通过 API 网关（推荐单独模块）统一入口
- 配置与服务发现集中化（Nacos）
- 强制可观测化（metrics + tracing + logs）
- 优先采用异步解耦（消息队列）降低强耦合

二、模块划分与职责（基于仓库 pom）
- x-boot-starters
    - 提供统一基础能力的 starter 集合（core, web, redis, satoken, rate-limit, cloud, knife4j 等），标准化日志、异常、统一响应、常用注解与 AOP 攻略
- x-boot-api
    - 仅做接口契约（admin-api、app-api），便于前后端、客户端与服务间解耦
- x-boot-modules
    - 业务微服务集合（每个子模块为独立服务）
- x-boot-job-admin
    - 基于 xxl-job 的调度管理控制台与管理端，实现定时任务、手动触发及任务监控

三、运行时组件与外部依赖
- 配置与发现：Nacos（pom 中包含 nacos-client 与 spring-cloud-alibaba）
- RPC：Dubbo（可选，pom 中包含 dubbo starter）
- 数据库：MySQL / PostgreSQL（pom 包含驱动）
- ORM/持久层：MyBatis-Plus
- 缓存/分布式锁：Redis + Redisson
- 消息：RocketMQ (ONS) 或其他（pom 包含 ons-client）
- 权限：sa-token
- 文档：SpringDoc + Knife4j
- 调度：XXL-Job（x-boot-job-core）
- 监控：Micrometer + Prometheus、Zipkin/SkyWalking（tracing）
- 日志：Logback + JSON 输出 -> Logstash/ELK/OpenSearch

四、服务通信与契约管理
- 对外：Gateway -> 后端微服务（REST + JSON）
- 微服务间：
    - 首选异步事件（MQ）实现跨服务可靠通信（领域事件）
    - 必要同步调用使用 OpenFeign 或 Dubbo（短超时、断路器/限流保护）
- API 契约管理：
    - 接口定义放在 x-boot-api（admin-api / app-api），消费方编译依赖接口���以避免重复定义
    - 使用 SpringDoc/Knife4j 自动生成 API 文档，接口变更需更新契约并按语义版本控制

五、数据策略与一致性
- 每服务独立数据库（避免跨服务事务）
- 采用 Saga 或补偿事务模式处理跨服务事务（基于事件驱动）
- 对于强一致场景，使用分布式事务慎重（优先业务补偿）
- 使用 Redis 做缓存层，指定合理过期与缓存穿透策略

六、弹性、容错与容量管理
- 熔断/降级/限流：Resilience4j 或 Sentinel（可在网关和服务内部均做限流）
- 超时与重试：统一设置超时、指数退避与最大重试次数
- Bulkhead（隔离）：对关键依赖使用线程池隔离
- 健康探针：实现 /actuator/health、readiness 与 liveness
- 扩展策略：Kubernetes HPA + PodDisruptionBudget

七、监控、日志与追踪（Observability）
- 指标：Micrometer -> Prometheus（服务级 QPS、延迟、错误率、DB 连接等）
- 链路追踪：Spring Sleuth + Zipkin / SkyWalking（事务级追踪）
- 日志：结构化 JSON 日志输出 -> Filebeat -> ELK / OpenSearch
- 告警：Prometheus Alertmanager 配合钉钉/Slack/邮件告警
- 建议：为每个 API 与关键操作定义 SLIs 与 SLOs

八、构建、镜像与部署
- 构建：
    - mvn -U clean package -DskipTests（在 CI 里运行单元与集成测试）
- 镜像化：
    - Dockerfile：使用多阶段构建，运行阶段使用 jlink 或 jre 精简镜像（GraalVM 可选）
    - 镜像仓库：私有仓库或 GitHub Packages / Harbor
- 部署：
    - Kubernetes + Helm（每个微服务独立 Chart 或使用 umbrella chart）
    - 发布策略：金丝雀或蓝绿发布，自动化回滚策略
- 数据库变更管理：Flyway 或 Liquibase（所有变更通过 CI 管理）

九、安全（建议）
- 身份认证：sa-token / OAuth2（access token + refresh token）
- 服务间安全：考虑使用 mTLS 或内部鉴权 token
- 配置安全：机密内容使用 Vault / KMS 管理，CI 不将密钥写入源码
- 输入校验与速率控制：防止 DDOS 与注入攻击

十、常见故障排查流程
- 服务无法启动：查看应用日志（检查配置中心是否可达，数据库连接是否成功）
- 服务不可达/高延迟：查看 Prometheus 指标，trace 抽样，检查线程池与连接池
- 任务失败（x-boot-job）：查看 xxl-job 控制台与目标服务日志
- 消息堆积：检查消费端实例数、消费速率、死信队列

常见演进建议（路线图）
- 引入 Service Mesh（Istio / Linkerd）以实现更细粒度流量控制与安全策略
- 增强多租户支持（按需）
- 建立事件总线（Kafka）作为核心异步通信平台（适合高并发/事件驱动场景）
- 引入 CI 静态分析（SCA）、依赖漏洞扫描并自动创建依赖升级 PR

结束语
x-boot-cloud 已整合大量企业常用组件并提供起步能力。结合具体业务场景，可以在本基础上逐步增强：例如替换消息中间件、拆分模块、增加 SRE 自动化能力。若你愿意，我可以基于仓库源码自动：
- 生成每个模块的 README、启动示例与 environment variable 列表
- 生成基线 Dockerfile、docker-compose.yml 与 Helm Chart 草案
- 生成 GitHub Actions CI workflow 模板（构建 -> 单元测试 -> 镜像构建 -> 部署到 staging）
