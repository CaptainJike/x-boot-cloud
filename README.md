# x-boot-cloud

企业级 Java 微服务脚手架 — x-boot-cloud

x-boot-cloud 是一个面向生产级场景的 Spring Boot / Spring Cloud 微服务脚手架，集合了服务治理、配置管理、分布式任务、通用 starter、API 分层模板等常见企业中台能力，帮助团队在短时间内搭建起可观测、可扩展、可演进的微服务系统。

基于项目根 pom（Java 17 / Spring Boot 4.0.1 / Spring Cloud 2025.1.0）和仓库模块结构生成。

快速索引
- 仓库：https://github.com/CaptainJike/x-boot-cloud
- Java 版本：17（pom 中 <java.version>17</java.version>）
- Spring Boot：4.0.1
- Spring Cloud：2025.1.0
- 主要模块（Root pom modules）：
    - x-boot-starters
    - x-boot-api (包含 admin-api, app-api)
    - x-boot-modules
    - x-boot-job-admin

为什么选择 x-boot-cloud
- 集中化的 starter 套件（x-boot-starters）：统一日志、异常、AOP、鉴权、缓存等能力，减少重复实现
- 与主流中间件兼容：Nacos（注册/配置）、Dubbo、MyBatis-Plus、Redisson、XXL-Job、RocketMQ（阿里云 ons-client）等
- 支持现代运维能力：Actuator、Prometheus 指标端点、SpringDoc/Knife4j API 文档、结构化日志
- 面向企业场景：多数据源、动态库表、分布式任务、审计与权限（sa-token）

项目架构概览
- API 层：x-boot-api 包含对外接口定义（admin-api、app-api），用于定义 DTO / 规范
- 公共能力：x-boot-starters 提供一系列 starter（core、web、satoken、redis、rate-limit-redis、cloud、knife4j、crud 等）
- 业务模块：x-boot-modules（业务服务集合，可能包括用户/订单/账单等）
- 调度/运维：x-boot-job-admin（内嵌 xxl-job 管理面板集成）
- 运行时依赖（典型）：Nacos for discovery/config, MySQL/Postgres, Redis/Redisson, RocketMQ/ONS, Prometheus/Grafana, ELK/OpenSearch

模块说明
- x-boot-starters：项目的基础 starter 包集合，包含常用基础能力（日志、工具、AOP、鉴权、缓存、限流等）。
- x-boot-api：只包含接口与 DTO，分 admin-api（管理后台）与 app-api（对外/移动端）两套契约。
- x-boot-modules：业务模块集（可包含微服务实现），实现具体业务逻辑。
- x-boot-job-admin：调度管理服务（基于 xxl-job），用于任务管理、监控与调度。

主要依赖点（摘自根 pom）
- Spring Boot 4.0.1、Spring Cloud 2025.1.0
- Spring Cloud Alibaba Nacos (discovery/config)
- Dubbo 3.3.6
- MyBatis-Plus 3.5.x
- Redisson、Redis
- Sa-Token（权限鉴权）
- Knife4j + SpringDoc（API 文档）
- RocketMQ (ONS 客户端)
- XXL-Job（任务调度）
- Micrometer / Actuator（生产监控）

快速开始（开发者本地）
1. 克隆仓库
   git clone https://github.com/CaptainJike/x-boot-cloud.git
   cd x-boot-cloud

2. 构建（跳过测试加速）
   mvn -U clean install -DskipTests

3. 启动依赖（示例）
    - 推荐使用 docker-compose 管理依赖：
        - MySQL
        - Nacos
        - Redis
        - RocketMQ/Ons 或 Kafka（按需）
        - Prometheus / Grafana / Zipkin（观测）
    - 如果只跑单模块也可在本地启动相应 DB/Redis 实例

4. 运行模块（示例）
    - 启动调度管理：
      cd x-boot-job-admin
      mvn spring-boot:run

    - 启动业务模块（示例）：
      cd x-boot-modules/<your-module>
      mvn spring-boot:run

5. 访问（示例）
    - 健康检查：GET http://localhost:8080/actuator/health
    - API 文档（Knife4j/SpringDoc）：/swagger-ui.html 或 /doc.html（根据模块配置）

配置要点
- Java 17（pom 指定）
- 使用 Spring Boot profiles（dev/test/prod）
- 建议接入 Nacos 作为服务发现与配置中心（pom 中包含 nacos-client）
- 为敏感配置使用 Vault 或 CI secrets 注入

容器化与部署
- 每个服务都应该有独立 Dockerfile（基于 JDK17 的轻量基础镜像）
- 镜像 Tag 格式建议：${artifactId}:${commitSha}-${buildTimestamp}
- Kubernetes 推荐用 Helm Charts / Kustomize 管理部署和配置

可观测性（建议）
- /actuator/prometheus 端点 -> Prometheus
- Sleuth + Zipkin / SkyWalking 做分布式追踪
- 结构化日志输出（JSON）并集中到 ELK / OpenSearch
- 建立基础仪表盘（服务级别的 error rate、latency、QPS、DB connections）

安全策略（建议）
- 接入 sa-token（已在依赖）或 OAuth2/JWT 实现认证与鉴权
- 将鉴权放在 Gateway 层（网关统一验证 token、做限流和黑名单）
- 服务间通信可选 mTLS 或内部签名机制

贡献指南
1. Fork 仓库并创建 feature 分支：feature/your-feature
2. 提交包含单元测试的 PR
3. PR 会触发 CI 校验（构建、单元测试、静态检查）
4. 通过后合并到主分支

致谢
- 本项目参考并集成了许多业界优秀组件与最佳实践：Spring Boot / Spring Cloud、MyBatis-Plus、Nacos、XXL-Job、Dubbo、RocketMQ 等。