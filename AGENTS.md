# Repository Guidelines

## Project Structure & Module Organization

This is a multi-module Maven Spring Boot/Spring Cloud repository. The root `pom.xml` aggregates `x-boot-starters`, `x-boot-api`, `x-boot-modules`, and `x-boot-job-admin`. Starter code lives in `x-boot-starters/*/src/main/java`; API entry points live in `x-boot-api/admin-api` and `x-boot-api/app-api`; business modules use facade/service splits such as `x-boot-modules/sys/sys-facade` and `x-boot-modules/sys/sys-service`. Application resources are under `src/main/resources`, including `application.yml`, i18n bundles, banners, and logging config. Tests belong in each module's `src/test/java`.

## Service Development Skill

新增、修改或评审服务模块时，优先引用项目内 Skill：`.codex/skills/x-boot-service-development/SKILL.md`。该 Skill 用中文说明 `api -> facade -> service -> mapper` 调用链路、服务职责边界、租户/安全上下文、配置规范和版本基线。

## Build, Test, and Development Commands

- `mvn clean install`: builds all modules and runs tests.
- `mvn clean install -DskipTests`: fast full build without tests.
- `mvn test`: runs all Maven tests.
- `mvn -pl x-boot-job-admin test`: runs tests for one module.
- `mvn -pl x-boot-modules/sys/sys-service -am spring-boot:run`: starts the system service with required dependent modules.
- `mvn checkstyle:check`: runs the repository Checkstyle rules from `checkstyle-v1.xml`.

## Coding Style & Naming Conventions

Use Java 21 as defined in the root POM, UTF-8 source files, and 4-space indentation. Keep packages lowercase under the existing `io.github...` hierarchy. Use PascalCase for classes, camelCase for methods and fields, and UPPER_SNAKE_CASE for constants. Follow existing suffixes: request DTOs end in `DTO`, response/view models in `VO` or `BO`, facade interfaces in `Facade`, and web controllers in `Controller`. Checkstyle limits include 200-character lines, 120-line methods, required braces, no unused imports, and no `System.out.println`.

## Testing Guidelines

Tests use `spring-boot-starter-test` and JUnit 5. Name test classes `*Test.java` and keep them near the module they validate, for example `x-boot-job-admin/src/test/java/.../CronExpressionTest.java`. Prefer focused unit tests for utilities and facades; use `@SpringBootTest` only when Spring context, database, or RPC wiring is needed. There is no repository-wide coverage threshold configured, so cover changed behavior and regression cases explicitly.

## Commit & Pull Request Guidelines

The local Git history is minimal, but the README asks for Conventional Commits. Use messages such as `feat: add sys role export`, `fix: handle missing tenant context`, or `test: cover cron parser`. Pull requests should describe the changed module, list config or database impacts, link related issues, and include the commands run, especially tests and Checkstyle. Add screenshots or API examples when changing controllers or generated documentation.

## Security & Configuration Tips

Do not commit secrets, tokens, database passwords, or cloud keys in `application.yml`. Use Spring profiles, Nacos configuration, environment variables, or CI/CD secret storage for environment-specific values. Document any new external dependency such as MySQL, Redis, Nacos, Dubbo, RocketMQ, or XXL-Job in the PR.
