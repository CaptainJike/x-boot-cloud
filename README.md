# x-boot-cloud

<div align="center">

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-brightgreen.svg)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.1.0-blue.svg)
![License](https://img.shields.io/badge/license-MIT-green.svg)

**企业级 Java 微服务脚手架**

[快速开始](#快速开始) • [功能特性](#功能特性) • [项目架构](#项目架构) • [相关文档](#相关文档)

</div>

---

## 简介

x-boot-cloud 是一个面向生产级场景的 Spring Boot / Spring Cloud 微服务脚手架，集合了服务治理、配置管理、分布式任务、通用 starter、API 分层模板、AI 能力集成等常见企业中台能力，帮助团队在短时间内搭建起可观测、可扩展、可演进的微服务系统。

### 技术栈

- **Java**: 17
- **Spring Boot**: 3.5.9
- **Spring Cloud**: 2025.1.0
- **Spring Cloud Alibaba**: 2025.0.0.0
- **主要中间件**:
  - Nacos (服务发现/配置中心)
  - Dubbo 3.3.6 (RPC)
  - MyBatis-Plus 3.5.16 (ORM)
  - Redisson 4.1.0 (分布式缓存/锁)
  - RocketMQ ONS (消息队列)
  - XXL-Job (分布式任务调度)
  - Sa-Token 1.44.0 (权限认证)
  - Knife4j + SpringDoc (API 文档)

### 快速索引

- **仓库地址**: https://github.com/CaptainJike/x-boot-cloud
- **Java 版本**: 17
- **Spring Boot**: 3.5.9
- **Spring Cloud**: 2025.1.0
- **主要模块**:
    - `x-boot-starters` - 基础能力 starter 集合
    - `x-boot-api` - API 接口定义（admin-api, app-api）
    - `x-boot-modules` - 业务模块集合
    - `x-boot-job-admin` - 任务调度管理

## 功能特性

### 核心能力

- ✅ **集中化的 Starter 套件**: 统一日志、异常处理、AOP、鉴权、缓存、限流等基础能力，减少重复实现
- ✅ **多 AI 模型支持**: 集成 Spring AI，支持 Ollama（本地模型）、OpenAI、DashScope（通义千问）、DeepSeek 等多种 AI 模型
- ✅ **主流中间件集成**: Nacos（注册/配置）、Dubbo、MyBatis-Plus、Redisson、XXL-Job、RocketMQ（阿里云 ONS）等
- ✅ **现代运维能力**: Actuator、Prometheus 指标端点、SpringDoc/Knife4j API 文档、结构化日志
- ✅ **企业级特性**: 多数据源、动态库表、分布式任务、多租户、审计与权限（sa-token）
- ✅ **国际化支持**: 内置 i18n 能力，支持多语言切换
- ✅ **API 分层设计**: 清晰的 API 分层（admin-api、app-api），便于前后端协作

### Starter 模块列表

| Starter 模块 | 功能说明 |
|-------------|---------|
| `x-boot-core` | 核心工具类、枚举、常量定义 |
| `x-boot-starter-web` | Web 统一响应、异常处理、XSS 防护 |
| `x-boot-starter-satoken` | 基于 Sa-Token 的权限认证 |
| `x-boot-starter-redis` | Redis/Redisson 集成 |
| `x-boot-starter-rate-limit-redis` | 基于 Redis 的限流能力 |
| `x-boot-starter-crud` | MyBatis-Plus CRUD 增强 |
| `x-boot-starter-cloud` | Spring Cloud 配置增强 |
| `x-boot-starter-dubbo` | Dubbo RPC 集成 |
| `x-boot-starter-aop` | AOP 切面能力 |
| `x-boot-starter-i18n` | 国际化支持 |
| `x-boot-starter-tenant` | 多租户支持 |
| `x-boot-starter-rocketmq-aliyun` | RocketMQ 阿里云 ONS 集成 |
| `x-boot-starter-ai` | AI 模型集成（Ollama、OpenAI、DashScope、DeepSeek） |
| `x-boot-job-core` | XXL-Job 任务调度核心 |
| `x-boot-starter-test` | 测试工具类 |

## 项目架构

### 架构概览

```
x-boot-cloud
├── x-boot-starters/          # 基础能力 starter 集合
│   ├── x-boot-core           # 核心工具类
│   ├── x-boot-starter-web    # Web 能力
│   ├── x-boot-starter-ai     # AI 模型集成
│   └── ...                    # 其他 starter
├── x-boot-api/               # API 接口定义层
│   ├── admin-api             # 管理后台 API
│   └── app-api               # 移动端/对外 API
├── x-boot-modules/           # 业务模块集合
│   ├── ai/                   # AI 服务模块
│   ├── oss/                  # 对象存储服务模块
│   └── sys/                  # 系统管理模块
└── x-boot-job-admin/         # 任务调度管理（XXL-Job）
```

### 模块说明

#### x-boot-starters
项目的基础 starter 包集合，提供统一的基础能力封装：
- 日志、异常处理、统一响应格式
- AOP 切面能力
- 鉴权、缓存、限流
- 数据库操作增强
- AI 模型集成
- 消息队列、任务调度等

#### x-boot-api
接口契约定义层，只包含接口与 DTO：
- **admin-api**: 管理后台接口定义
- **app-api**: 移动端/对外接口定义
- 便于前后端、客户端与服务间解耦

#### x-boot-modules
业务模块集合，每个子模块为独立的微服务：
- **ai-service**: AI 服务，支持多种 AI 模型调用
- **oss-service**: 对象存储服务，文件上传下载管理
- **sys-service**: 系统管理服务，用户、权限等基础功能

#### x-boot-job-admin
基于 XXL-Job 的分布式任务调度管理平台：
- 任务管理、监控与调度
- 支持动态添加、修改、删除任务
- 任务执行日志查看

### 运行时依赖

- **服务发现/配置中心**: Nacos
- **数据库**: MySQL / PostgreSQL
- **缓存/分布式锁**: Redis + Redisson
- **消息队列**: RocketMQ (阿里云 ONS)
- **RPC**: Dubbo（可选）
- **监控**: Prometheus + Grafana
- **链路追踪**: Zipkin / SkyWalking（可选）
- **日志**: ELK / OpenSearch（可选）

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+ / PostgreSQL 12+（可选）
- Redis 6.0+（可选）
- Nacos 2.0+（可选，用于服务发现和配置中心）

### 1. 克隆项目

```bash
git clone https://github.com/CaptainJike/x-boot-cloud.git
cd x-boot-cloud
```

### 2. 构建项目

```bash
# 完整构建（包含测试）
mvn clean install

# 跳过测试快速构建
mvn clean install -DskipTests
```

### 3. 初始化数据库

数据库初始化脚本位于 `config/db/MySQL/` 目录：

- `x_boot_sys.sql` - 系统管理模块（必须）
- `x_boot_oss.sql` - 对象存储模块（建议）
- `x_boot_ai.sql` - AI 服务模块（可选）

```bash
# 使用 MySQL 客户端执行
mysql -u root -p < config/db/MySQL/x_boot_sys.sql
mysql -u root -p < config/db/MySQL/x_boot_oss.sql
mysql -u root -p < config/db/MySQL/x_boot_ai.sql
```

### 4. 配置 Nacos（可选）

如果使用 Nacos 作为配置中心，需要：
1. 启动 Nacos Server
2. 在 Nacos 控制台创建相应的配置文件
3. 配置文件示例参考 `config/nacos/` 目录

### 5. 启动依赖服务

推荐使用 Docker Compose 启动依赖服务：

```bash
# 示例：启动 MySQL、Redis、Nacos
docker-compose up -d mysql redis nacos
```

或手动启动：
- MySQL: 默认端口 3306
- Redis: 默认端口 6379
- Nacos: 默认端口 8848

### 6. 运行应用

#### 启动任务调度管理

```bash
cd x-boot-job-admin
mvn spring-boot:run
```

访问地址: http://localhost:8080/xxl-job-admin

#### 启动业务模块

```bash
# 启动 AI 服务
cd x-boot-modules/ai/ai-service
mvn spring-boot:run

# 启动 OSS 服务
cd x-boot-modules/oss/oss-service
mvn spring-boot:run

# 启动系统管理服务
cd x-boot-modules/sys/sys-service
mvn spring-boot:run
```

### 7. 访问服务

- **健康检查**: `GET http://localhost:8080/actuator/health`
- **API 文档**: 
  - Swagger UI: `http://localhost:8080/swagger-ui.html`
  - Knife4j: `http://localhost:8080/doc.html`
- **Prometheus 指标**: `http://localhost:8080/actuator/prometheus`

### 配置说明

- **Java 版本**: 17（在 pom.xml 中配置）
- **Profile 配置**: 使用 Spring Boot profiles（dev/test/prod）
- **配置中心**: 建议使用 Nacos 作为配置中心
- **敏感信息**: 生产环境建议使用 Vault 或 CI/CD secrets 管理

## AI 功能使用

x-boot-cloud 集成了 Spring AI，支持多种 AI 模型：

### 支持的 AI 模型

| 模型类型 | 说明 | 配置项 |
|---------|------|--------|
| **Ollama** | 本地部署的 AI 模型 | `spring.ai.ollama.*` |
| **OpenAI** | OpenAI GPT 系列模型 | `spring.ai.openai.*` |
| **DashScope** | 阿里云通义千问 | `spring.ai.alibaba.dashscope.*` |
| **DeepSeek** | DeepSeek AI 模型 | `spring.ai.deepseek.*` |

### 使用示例

```java
@Autowired
private XBootAiFactory aiFactory;

// 同步调用
String response = aiFactory.chat("你好", ModelTypeEnum.OPENAI);

// 流式调用
Flux<String> stream = aiFactory.streamChat("你好", ModelTypeEnum.DASHSCOPE);
```

详细配置和使用说明请参考各业务模块的文档。

## 容器化与部署

### Docker 镜像构建

每个服务都应该有独立的 Dockerfile（基于 JDK 17 的轻量基础镜像）：

```dockerfile
FROM openjdk:17-jre-slim
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

镜像 Tag 格式建议：`${artifactId}:${version}-${commitSha}`

### Kubernetes 部署

推荐使用 Helm Charts 或 Kustomize 管理部署和配置：

```bash
# 使用 Helm 部署
helm install my-service ./helm-charts/my-service

# 使用 Kustomize
kubectl apply -k kustomize/overlays/prod
```

### CI/CD 建议

- 构建阶段：`mvn clean package -DskipTests`
- 镜像构建：使用多阶段构建优化镜像大小
- 部署策略：金丝雀发布或蓝绿发布
- 自动化回滚：配置自动回滚策略

## 可观测性

### 指标监控

- **Prometheus**: `/actuator/prometheus` 端点暴露指标
- **Grafana**: 配置仪表盘监控服务健康状态
- **关键指标**: QPS、延迟、错误率、DB 连接数等

### 链路追踪

- **Spring Sleuth**: 自动生成追踪 ID
- **Zipkin / SkyWalking**: 分布式追踪可视化
- **配置示例**: 在 `application.yml` 中配置追踪采样率

### 日志管理

- **结构化日志**: 使用 JSON 格式输出
- **日志收集**: Filebeat -> Logstash -> ELK / OpenSearch
- **日志级别**: 生产环境建议使用 INFO，开发环境使用 DEBUG

## 安全策略

### 认证与鉴权

- **Sa-Token**: 已集成，提供完整的权限认证能力
- **OAuth2/JWT**: 可选，用于更复杂的认证场景
- **网关层鉴权**: 建议在 API 网关统一验证 token、做限流和黑名单

### 服务间安全

- **mTLS**: 服务间通信使用 mTLS 加密
- **内部鉴权**: 使用内部 token 或签名机制
- **网络隔离**: 使用 Kubernetes NetworkPolicy 限制服务间访问

### 配置安全

- **敏感信息**: 使用 Vault / KMS 管理密钥
- **CI/CD**: 不在源码中硬编码密钥
- **配置加密**: 对敏感配置进行加密存储

## 开发指南

### 代码规范

- 遵循阿里巴巴 Java 开发手册
- 使用 Checkstyle 进行代码检查（配置文件：`checkstyle-v1.xml`）
- 提交前运行 `mvn checkstyle:check` 检查代码规范

### 模块开发

1. **创建新业务模块**:
   - 在 `x-boot-modules` 下创建新的模块目录
   - 参考现有模块（如 `ai-service`）的结构
   - 实现对应的 facade 接口

2. **添加新的 Starter**:
   - 在 `x-boot-starters` 下创建新的 starter 模块
   - 实现自动配置类
   - 在 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 中注册

3. **API 接口定义**:
   - 在 `x-boot-api` 中定义接口和 DTO
   - 区分 `admin-api` 和 `app-api`

## 贡献指南

我们欢迎所有形式的贡献！

### 贡献流程

1. **Fork 仓库**并克隆到本地
2. **创建功能分支**: `git checkout -b feature/your-feature`
3. **编写代码**并添加单元测试
4. **提交代码**: `git commit -m "feat: add some feature"`
5. **推送分支**: `git push origin feature/your-feature`
6. **创建 Pull Request**

### 提交规范

遵循 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

- `feat`: 新功能
- `fix`: 修复 bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 构建/工具相关

### PR 检查清单

- [ ] 代码通过 Checkstyle 检查
- [ ] 包含单元测试且测试通过
- [ ] 更新相关文档
- [ ] 提交信息遵循规范

## 常见问题

### Q: 如何切换不同的 AI 模型？

A: 在配置文件中设置相应的模型配置，调用时指定 `ModelTypeEnum` 即可。默认使用 Ollama。

### Q: 如何配置多数据源？

A: 使用 `dynamic-datasource-spring-boot3starter`，在配置文件中配置多个数据源，使用 `@DS` 注解切换。

### Q: 如何自定义异常处理？

A: 在 `x-boot-starter-web` 中已提供全局异常处理，可通过实现 `GlobalExceptionHandler` 自定义。

### Q: 任务调度如何配置？

A: 参考 `x-boot-job-admin` 模块，配置 XXL-Job 的执行器地址和注册中心。

## 相关文档

- [架构说明文档](./ARCHITECTURE.md) - 详细的架构设计说明
- [配置说明](./config/README.md) - 数据库和 Nacos 配置说明
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [Spring Cloud 官方文档](https://spring.io/projects/spring-cloud)

## 版本历史

- **1.0.0** (当前版本)
  - 初始版本发布
  - 集成 Spring Boot 3.5.9、Spring Cloud 2025.1.0
  - 支持多种 AI 模型集成
  - 完整的微服务基础能力

## 许可证

本项目采用 MIT 许可证，详情请查看 [LICENSE](./LICENSE) 文件。

## 致谢

本项目参考并集成了许多业界优秀组件与最佳实践：

- [Spring Boot](https://spring.io/projects/spring-boot) - 应用框架
- [Spring Cloud](https://spring.io/projects/spring-cloud) - 微服务框架
- [MyBatis-Plus](https://baomidou.com/) - ORM 框架
- [Nacos](https://nacos.io/) - 服务发现和配置管理
- [XXL-Job](https://www.xuxueli.com/xxl-job/) - 分布式任务调度
- [Dubbo](https://dubbo.apache.org/) - RPC 框架
- [RocketMQ](https://rocketmq.apache.org/) - 消息队列
- [Sa-Token](https://sa-token.cc/) - 权限认证框架
- [Spring AI](https://spring.io/projects/spring-ai) - AI 模型集成框架

## 联系方式

- **Issues**: [GitHub Issues](https://github.com/CaptainJike/x-boot-cloud/issues)
- **讨论**: [GitHub Discussions](https://github.com/CaptainJike/x-boot-cloud/discussions)

---

<div align="center">

**如果这个项目对你有帮助，请给一个 ⭐ Star！**

Made with ❤️ by x-boot-cloud team

</div>