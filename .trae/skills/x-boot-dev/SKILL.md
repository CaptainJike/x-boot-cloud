---
name: "x-boot-dev"
description: "Development guide for x-boot-cloud. Invoke when writing code to ensure adherence to BFF, Dubbo, Nacos, and project-specific patterns."
---

# x-boot-cloud Development Skill

This skill provides the standard development patterns and architectural guidelines for the x-boot-cloud project.

## 1. Architecture Overview
The project follows a **BFF (Backend for Frontend) + Facade** microservice architecture.
- **API Layer (`x-boot-api`)**:
  - `admin-api`: For management background (PC).
  - `app-api`: For C-end users (App/H5).
  - **Role**: Route requests, validate parameters, call Facade services via Dubbo.
- **Module Layer (`x-boot-modules`)**:
  - Contains specific business modules (e.g., `ai`, `sys`).
  - **Structure**:
    - `*-facade`: Defines Dubbo interfaces, DTOs (Request), BOs (Response). Shared library.
    - `*-service`: Implements interfaces, interacts with DB, provides Dubbo services.
- **Infrastructure**:
  - `x-boot-starters`: Common dependencies and configs.
  - **Middleware**: Nacos (Discovery/Config), Dubbo (RPC), Sentinel (Flow Control).

## 2. Coding Standards

### 2.1 API Layer (Controller)
- **Location**: `io.github.module.{adminapi|appapi}.web.*`
- **Dependency**: Must include the corresponding `*-facade` module in `pom.xml`.
- **Injection**: Use `@DubboReference` to inject Facade interfaces.
  ```java
  @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
  private XBootExampleFacade xBootExampleFacade;
  ```
- **Response**: Always wrap return values in `ApiResult<T>`.
- **Validation**: Use `@Valid` and JSR-303 annotations on DTOs.

### 2.2 Facade Layer (Interface & DTO/BO)
- **Location**: `x-boot-modules/*-facade`
- **Naming**:
  - Interface: `XBoot{Module}Facade`
  - Request: `*DTO` (in `model.request` package)
  - Response: `*BO` (in `model.response` package)
- **Rules**:
  - DTOs/BOs must implement `Serializable`.
  - Use Lombok (`@Data`, `@Accessors(chain=true)`).
  - Use `@Schema` for API documentation.

### 2.3 Service Layer (Implementation)
- **Location**: `x-boot-modules/*-service`
- **Annotation**: Use `@DubboService` on the implementation class.
  ```java
  @DubboService(version = BaseConstant.Version.DUBBO_VERSION_V1)
  public class XBootExampleFacadeImpl implements XBootExampleFacade { ... }
  ```
- **DB Access**: Extends `XBootBaseServiceImpl<Mapper, Entity>`.
- **Context**: Use `UserContextHolder.getUserId()` to get current user ID.
- **Service-to-Service Call**:
  - Must include the target `*-facade` dependency in `pom.xml`.
  - Use `@DubboReference` to inject the target Facade interface.


## 3. Key Technologies
- **Spring Boot**: 3.5.9
- **Dubbo**: 3.3.6 (Triple Protocol)
- **Nacos**: 2.5.2
- **ORM**: MyBatis-Plus
- **Auth**: Sa-Token
- **AI**: Spring AI 1.x (OpenAiChatModel)

## 4. Common Patterns
- **Pagination**: Use `PageParam` and `PageResult`.
- **Transactions**: Use `@Transactional(rollbackFor = Exception.class)`.
- **Id Generation**: `IdUtil.fastSimpleUUID()` or `Snowflake`.

## 5. Example Workflow (Add New Feature)
1. Define `*DTO` and `*BO` in `*-facade`.
2. Define interface method in `XBoot*Facade`.
3. Implement method in `XBoot*FacadeImpl` (in `*-service`).
   - Call local Service logic or Mapper.
4. Create/Update Controller in `admin-api`/`app-api`.
   - Call Facade method.
