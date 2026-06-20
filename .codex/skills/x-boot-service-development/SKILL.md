---
name: x-boot-service-development
description: 在 x-boot-cloud Maven 多模块 Spring Boot/Spring Cloud/Dubbo 项目中新增、修改或评审服务时使用。本 Skill 约束 API + Facade + Service provider 架构、服务职责描述、模块边界、调用链路、租户/用户上下文传递、配置规范和版本搭配。
---

# X-Boot Cloud 服务开发规范

当任务涉及新增、修改或评审服务、模块、Facade 契约、Controller、Dubbo provider/consumer、实体、Mapper、服务配置或服务间调用时，使用本 Skill。

## 开始前必须先做

在改代码前，先完成这些判断：

1. 阅读根 `pom.xml`、目标模块 `pom.xml` 和相邻同类模块。
2. 判断改动应该落在 `api` 应用、`facade` 模块、`service` 模块，还是 `starter` 基础设施模块。
3. 找一个现有相似调用链路作为参考。
4. 默认保持现有架构模式，除非用户明确要求调整架构。

标准调用链路：

`HTTP Controller -> @DubboReference Facade -> @DubboService FacadeImpl -> Spring Service -> Mapper/MyBatis-Plus -> DB`

不要把业务规则写在 Controller 里。Controller 只负责 HTTP 入参出参、鉴权注解、API 层适配和必要的接口编排。核心业务规则应放在业务服务模块。

## 当前服务职责

新增或修改服务时，必须理解并保持以下职责边界。

### `admin-api`

作用：后台管理端 HTTP API 入口。

主要职责：

- 对外提供后台管理 REST API。
- 解析 Sa-Token Session，并恢复 `UserContextHolder` / `TenantContextHolder`。
- 通过 `@SaCheckLogin`、`@SaCheckPermission` 等注解完成后台登录和权限校验。
- 通过 `@DubboReference` 调用业务 Facade。
- 处理后台管理特有的横切逻辑，例如操作日志、角色权限缓存刷新、用户踢下线事件。
- 统一返回 `ApiResult`。

不能做的事：

- 不拥有业务数据持久化。
- 不直接依赖 `sys-service`、`oss-service` 等服务实现类。
- 不绕过 Facade 契约直接跨模块调用业务实现。

### `app-api`

作用：C 端、移动端或应用端 HTTP API 入口。

主要职责：

- 对外提供 app 侧 REST API。
- 使用 app 侧 Sa-Token 登录类型。
- 从请求中恢复 app 用户上下文和租户上下文。
- 聚合调用 app 侧需要的业务 Facade。

当前状态：

- 该模块目前还是骨架状态。
- 现有登录逻辑是示例性质，生产使用前必须替换为真实 app 用户登录逻辑。
- `AppAiChatController` 当前也只是占位。

### `sys-facade`

作用：系统管理领域的 Dubbo RPC 契约模块。

主要职责：

- 定义系统管理领域对外暴露的 Facade 接口。
- 存放跨模块使用的 `DTO`、`BO`、`VO`。
- 存放系统领域消费者需要使用的注解、枚举、常量和扩展点。
- 保持 RPC 契约稳定、清晰、可版本化。

不能做的事：

- 不依赖 `sys-service`。
- 不包含数据库持久化逻辑。
- 不把 `Entity` 暴露给 API 层或其他服务。

### `sys-service`

作用：系统管理领域的业务服务提供者。

主要职责：

- 通过 `@DubboService` 发布 `sys-facade` 中定义的接口。
- 实现用户、角色、菜单、部门、租户、系统参数、数据字典、系统日志等业务规则。
- 拥有系统领域的 `Entity`、`Mapper`、Mapper XML、关系服务和事务。
- 负责数据权限校验、租户校验、角色边界校验、密码规则等核心规则。
- 通过 `SysLogFacade` 写入系统操作日志。

典型实现结构：

`SysXxxFacadeImpl -> SysXxxService -> SysXxxMapper -> SysXxxEntity`

### `oss-facade`

作用：文件/对象存储领域的 Dubbo RPC 契约模块。

主要职责：

- 定义文件信息、上传、下载相关 Facade 接口。
- 存放 OSS 领域跨模块使用的 `DTO`、`BO`、`VO`。
- 存放上传校验、错误枚举等消费者需要使用的公共能力。
- 保持文件传输和文件元数据接口稳定。

不能做的事：

- 不依赖 `oss-service`。
- 不泄露具体存储实现细节，除非这些字段已经是明确的契约内容。

### `oss-service`

作用：文件/对象存储领域的业务服务提供者。

主要职责：

- 通过 `@DubboService` 发布 `oss-facade` 中定义的接口。
- 保存文件元数据。
- 对接 `x-file-storage`、MinIO、本地存储或其他配置的平台。
- 处理上传、MD5 秒传、删除、服务端代理下载、对象存储直链跳转等逻辑。
- 查询文件元数据时遵守租户上下文。

特别注意：

- 匿名下载和租户隔离可能冲突。如果下载接口不要求登录，必须明确设计租户上下文如何确定，或如何安全绕过租户隔离。

### `x-boot-starters`

作用：项目基础设施能力集合。

各 starter 职责：

- `x-boot-core`：常量、上下文 Holder、基础配置、通用异常、分页模型。
- `x-boot-starter-web`：Web MVC 默认配置、Jackson、全局异常处理、XSS、Web 访问日志。
- `x-boot-starter-cloud`：Nacos Discovery / Config 集成。
- `x-boot-starter-dubbo`：Dubbo starter、consumer/provider 上下文 Filter、Dubbo 异常 Filter。
- `x-boot-starter-crud`：MyBatis-Plus、动态数据源注册、ID 生成、审计字段自动填充。
- `x-boot-starter-tenant`：行级租户或数据源级租户支持。
- `x-boot-starter-redis`：`RedisTemplate`、Redisson 分布式锁、缓存 key 生成。
- `x-boot-starter-satoken`：Sa-Token Redis 集成和可选本地缓存。
- `x-boot-starter-rate-limit-redis`：基于 Redis 的 AOP 限流。
- `x-boot-starter-ai`：Spring AI 多模型 provider 抽象。
- `x-boot-job-core`：XXL-Job 执行器核心。

只有当能力可复用、跨业务域、属于基础设施时，才新增或修改 starter。单个业务域的逻辑不要放进 starter。

### `x-boot-job-admin`

作用：独立 XXL-Job Admin 管理端。

主要职责：

- 管理定时任务、执行器、任务日志、用户和注册信息。
- 使用 Spring MVC、Freemarker、MyBatis、Hikari 和 `x-boot-job-core`。
- 通过 XXL-Job 自身 HTTP/RPC 机制调用执行器。

注意：

- 它不是普通业务服务 provider。
- 它不走主业务的 `api -> facade -> service` Dubbo 调用链路。

## 新增业务服务规范

新增一个业务域时，例如 `order`，优先使用以下结构：

```text
x-boot-modules/order/
├── pom.xml
├── order-facade/
│   ├── pom.xml
│   └── src/main/java/.../facade, model, enums, constants
└── order-service/
    ├── pom.xml
    └── src/main/java/.../biz, service, mapper, entity
```

新增服务前，必须先写清楚服务职责。示例：

```markdown
## 服务职责：order-service

服务作用：
- 负责订单生命周期和订单数据持久化。

提供能力：
- `OrderFacade`：提供订单创建、修改、查询、取消等能力。

拥有数据：
- `order_main`
- `order_item`

调用其他服务：
- `sys-facade`：仅在需要用户、租户或系统基础数据时调用。
- `oss-facade`：仅在需要订单附件或文件元数据时调用。

不负责：
- 不负责用户账号、角色、菜单、租户。
- 不负责文件二进制存储。
- 不负责 HTTP 路由。
```

如果该服务需要暴露后台或 app 接口，应优先把 Controller 放到 `admin-api` 或 `app-api`。业务服务模块默认只作为 Dubbo provider，除非明确设计为同时暴露服务本地 REST 接口。

## Facade 契约规范

Facade 模块是服务之间的公开 RPC 边界。

必须遵守：

- 跨服务接口放在 `*-facade`。
- 请求模型放在 `model/request`。
- 响应模型放在 `model/response`。
- 请求对象使用 `DTO` 后缀。
- 响应对象按现有习惯使用 `BO` 或 `VO`。
- Facade 方法应表达业务动作，不要暴露数据库细节。
- 需要业务失败语义时，显式声明 `throws BusinessException`。

避免：

- 不返回 `Entity`。
- 不把 `Mapper`、`QueryWrapper` 等持久化对象传出 Facade 边界。
- 不让 Facade 模块依赖 service 模块。
- 不设计过于细碎的 RPC 方法，避免 consumer 循环远程调用。

Provider 推荐模式：

```java
@DubboService(
        version = BaseConstant.Version.DUBBO_VERSION_V1,
        validation = BaseConstant.Dubbo.ENABLE_VALIDATION,
        timeout = BaseConstant.Dubbo.TIMEOUT
)
@RequiredArgsConstructor
@Slf4j
public class XxxFacadeImpl implements XxxFacade {

    private final XxxService xxxService;

    @Override
    public XxxBO getOneById(Long id, boolean throwIfInvalidId) {
        return xxxService.getOneById(id, throwIfInvalidId);
    }
}
```

Consumer 推荐模式：

```java
@DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
private XxxFacade xxxFacade;
```

## Service 实现规范

Service 模块拥有业务规则和数据库访问。

必须遵守：

- Dubbo provider 适配类放在 `biz`。
- 业务逻辑放在 Spring `@Service` 类。
- 数据实体放在 `entity`。
- MyBatis Mapper 放在 `mapper`。
- 自定义 XML SQL 遵循当前 Mapper XML 目录习惯。
- 多写操作使用 `@Transactional(rollbackFor = Exception.class)`。
- Facade 返回前应把 `Entity` 转换成 `BO` 或 `VO`。
- 分页统一使用 `PageParam` 和 `PageResult`。

避免：

- 不在 `FacadeImpl` 中堆业务规则。
- 不在 Controller 中堆业务规则。
- 不跨领域直接访问其他服务的 Mapper。
- 已有模式要求 `throwIfInvalidId` 时，不要静默返回 null。

## 上下文、租户和安全规范

用户上下文和租户上下文是本项目架构的一部分。

必须遵守：

- HTTP 入口必须先从 Sa-Token Session 恢复上下文，再进入权限校验和业务调用。
- Dubbo 跨服务调用依赖现有 consumer/provider Filter 传递上下文。
- MyBatis 租户隔离依赖 `TenantContextHolder`。
- 新增和更新审计字段依赖现有 MyBatis-Plus 自动填充器。
- 如果临时切换租户上下文，必须在 `finally` 中恢复或清理。

设计登录或匿名访问时，必须说明：

- `UserContext` 如何创建。
- `TenantContext` 如何选择。
- 匿名访问是否兼容租户隔离。
- 不允许让租户行为保持隐式。

## 配置规范

本地 `application.yml` 应保持最小化。

共享运行时配置通过 Nacos 引入：

- `COMMON.yml`
- `DATASOURCE.yml`
- `REDIS.yml`
- `SA-TOKEN.yml`
- `DUBBO.yml`
- `${spring.application.name}.yml`

业务服务应用如果同时存在 Spring Cloud HTTP 注册和 Dubbo 应用级注册，应遵循当前 `*-rest` 命名方式，避免注册名冲突。

禁止提交：

- 数据库密码。
- 云服务密钥。
- 生产 token。
- 其他敏感配置。

## 版本基线

以根 `pom.xml` 为版本来源，不要在子模块随意覆盖版本。

当前基线：

- Java `21`
- Spring Boot `3.5.9`
- Spring Cloud `2025.0.1`
- Spring Cloud Alibaba `2025.0.0.0`
- Dubbo `3.3.6`
- Nacos Client `2.5.2`
- MyBatis-Plus `3.5.16`
- Sa-Token `1.44.0`
- Spring AI `1.1.2`
- MySQL Connector/J `9.5.0`
- Redisson `3.52.0`

只有存在明确兼容性原因时，才允许在子模块覆盖依赖版本。

## 每个新服务必须写清楚的职责说明

每个新增服务都必须在 PR 描述、代码评审说明或模块文档中补充以下内容：

```markdown
## 服务：<service-name>

服务作用：
- <一句话说明该服务为什么存在>

业务能力：
- <能力 1>
- <能力 2>

拥有数据：
- <表或聚合 1>
- <表或聚合 2>

提供的 Facade：
- `<XxxFacade>`：<说明对外提供什么能力>

消费的 Facade：
- `<OtherFacade>`：<说明为什么调用>
- 如果没有跨服务调用，写“无”。

HTTP 入口：
- `admin-api`：<后台接口 Controller，若没有则写“无”>
- `app-api`：<app 接口 Controller，若没有则写“无”>

租户行为：
- 行级租户、数据源级租户、特权租户，或无租户数据。
- 如果支持匿名访问，说明如何处理租户上下文。

安全行为：
- 是否要求登录。
- 权限码前缀。
- 角色约束或数据权限约束。

不负责：
- <明确说明该服务不拥有的能力或数据>
```

判断服务职责说明是否合格：

- 能看出服务为什么存在。
- 能看出它拥有哪些数据。
- 能看出谁会调用它。
- 能看出它会调用谁。
- 能看出它明确不负责什么。
- 能看出租户和权限规则。


## 基础建表模板

新增业务表时，默认包含以下基础字段，并根据业务表名和注释替换 `数据表模板`、`数据表注释`。完整业务 SQL 后续统一参考 `docs` 目录下各服务 SQL 文件；Skill 只记录基础建表字段模板，不维护完整项目 SQL。

```sql
-- x_boot.`数据表模板` definition

CREATE TABLE `数据表模板` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
  `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  `created_at` datetime NOT NULL COMMENT '创建时刻',
  `created_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建者',
  `updated_at` datetime NOT NULL COMMENT '更新时刻',
  `updated_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='数据表注释';
```

## 验证清单

完成服务开发后，优先运行目标模块相关检查：

- `mvn -pl <changed-module> -am test`
- `mvn -pl <changed-module> -am -DskipTests package`
- 如涉及 Java 风格敏感代码，运行 `mvn checkstyle:check`

人工检查：

- Controller 是否只调用 Facade，而不是直接调用 service 实现。
- Facade 模块是否没有依赖 service 模块。
- Provider 是否使用项目统一的 `@DubboService` 版本常量。
- Consumer 是否使用项目统一的 `@DubboReference` 版本常量。
- `DTO`、`BO`、`VO` 命名是否符合现有习惯。
- 多写操作是否有事务。
- 租户上下文行为是否明确。
- 权限码前缀是否一致。
- 修改角色、权限、会话状态时，是否处理缓存失效或用户踢下线。
- 是否没有新增敏感配置。
