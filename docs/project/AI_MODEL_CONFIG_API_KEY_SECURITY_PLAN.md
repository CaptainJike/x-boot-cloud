# AI 模型配置 API Key 安全方案

最后更新：2026-06-24

## 背景

当前 `ai_model_config.api_key` 用于保存模型供应商 API Key。后台响应已通过 `AiModelConfigBO#apiKey` 的 `@JsonIgnore` 和 `apiKeyMasked` 做脱敏，完整 Key 仅通过 `GET /ai/model-configs/{id}/api-key` 和 `AiModelConfig:key` 权限查看。

本方案评估当前明文保存风险，并设计后续密文、KMS 和配置中心方案。本轮只做设计闭环，不直接改动运行时代码或数据库结构。

## 当前链路

数据入口：
- `AdminAiModelConfigController#insert/update` 接收 `AdminInsertOrUpdateAiModelConfigDTO#apiKey`。
- `AiModelConfigService#adminInsert/adminUpdate` 清理并校验 API Key。
- `AiModelConfigEntity#apiKey` 映射到 `ai_model_config.api_key`。

数据读取：
- `AiModelConfigService#getOneById/getEnabledConfigByCode/getDefaultEnabledConfig` 从数据库读取完整 Key 并转换为 `AiModelConfigBO`。
- `AiModelConfigService#toRuntimeConfig` 把完整 Key 写入 `io.github.starter.ai.vo.AiModelConfig` 供 `x-boot-starter-ai` 调用模型供应商。
- `AiModelConfigService#adminListProviderModels` 支持临时传入 Key，或按配置 ID 读取已保存 Key 查询供应商模型列表。

对外展示：
- 列表和详情只应展示 `apiKeyMasked`。
- 完整 Key 查看接口已有独立权限码 `AiModelConfig:key`。
- `docs/project/AI_MODEL_CONFIG_INIT_SQL.sql` 不写入真实 Key。

## 明文保存风险评估

风险等级：高。

主要风险：
- 数据库直连、备份、慢查询采样、SQL 审计、误导出的 dump 都可能暴露供应商 API Key。
- `adminInsert/adminUpdate` 曾存在输出 DTO 的日志风险，当前已改为只记录 `code`、`providerType` 和 `id`，避免将入参 API Key 写入应用日志。
- Service、Facade 和 BO 内部仍传递完整 Key，后续新增对话、调用日志或异常上下文时容易误记录。
- `adminGetApiKey` 返回完整 Key，即使有权限控制，也会扩大内部用户、浏览器插件、代理抓包、前端日志的暴露面。
- 租户隔离只能限制数据行访问，不能降低拥有数据库读取权限者看到明文 Key 的风险。
- 轮换、吊销、密钥版本和历史密钥清理没有统一模型，发生泄露后难以确定影响范围。

当前已有缓解：
- HTTP 列表和详情不序列化完整 `apiKey`。
- 完整 Key 使用独立权限码，不与普通详情权限混用。
- 初始化 SQL 不提交真实 Key。
- `ai_model_config` 已接入租户行级隔离。
- `adminInsert/adminUpdate` 已移除包含 DTO 的日志输出。

结论：
- 当前状态适合 MVP 内部验证，但不适合生产长期保存云供应商 Key。
- 下一阶段应继续禁止新增密钥相关日志，并把数据库明文字段改造成“密文或引用”存储。

## 目标原则

- 最小暴露：默认不返回完整 Key；后续优先取消完整 Key 查看能力，只保留“重置/替换 Key”。
- 最小权限：解密能力只在 `ai-service` 内部运行时发生，不下放到 `admin-api`、前端或其他服务。
- 可轮换：支持密钥材料、数据密钥、供应商 API Key 的轮换和迁移。
- 可审计：记录谁在何时创建、替换、查看或测试 Key，但审计内容不得包含明文。
- 租户隔离：密文、KMS Key、配置中心引用都必须带明确租户边界。
- 渐进迁移：不一次性破坏现有 `AiModelConfigFacade` 契约，先兼容再收敛。

## 方案总览

推荐采用三层方案：

1. 短期：应用层加密落库。
2. 中期：KMS 信封加密。
3. 可选：配置中心引用模式。

三种模式在 `ai_model_config` 中用统一字段表达：

```text
api_key_storage_type: PLAIN / CIPHERTEXT / KMS / CONFIG_REF
api_key_ciphertext: 密文或密文引用
api_key_ref: 配置中心 dataId、KMS secret name 或外部 secret path
api_key_kms_key_id: KMS CMK/Key ID
api_key_version: API Key 版本
api_key_last_rotated_at: 最近轮换时间
api_key: 兼容期历史字段，迁移完成后置空或删除
```

## 方案一：应用层密文落库

适用阶段：MVP 进入生产前的最低安全基线。

设计：
- 新增 `AiSecretCryptoService`，只放在 `ai-service`。
- `adminInsert/adminUpdate` 保存前调用 `encryptApiKey`。
- `toRuntimeConfig/adminTest/adminListProviderModels` 使用前调用 `decryptApiKey`。
- `AiModelConfigBO` 不再承载完整 `apiKey`，运行时解密结果只在 Service 局部变量存在。
- `adminGetApiKey` 后续废弃，改为 `apiKeyPresent`、`apiKeyMasked` 和“替换 Key”。

密钥来源：
- 开发环境可从环境变量或 Nacos 加密配置读取主密钥。
- 生产环境不得把主密钥硬编码在 `application.yml`。

优点：
- 改造范围小，能快速降低数据库和备份明文暴露风险。
- 不要求立即接入云 KMS。

限制：
- 主密钥仍需要安全托管。
- 应用实例拥有解密能力，服务被攻破时仍可能泄露。
- 轮换需要自研密钥版本和重加密流程。

## 方案二：KMS 信封加密

适用阶段：生产推荐方案。

设计：
- 新增 `AiSecretManager` 接口，屏蔽具体 KMS 厂商。
- 新增实现：`LocalAesSecretManager`、`AliyunKmsSecretManager`、`AwsKmsSecretManager` 或后续企业自建 KMS 实现。
- 保存 API Key 时生成数据密钥或调用 KMS 加密，数据库只保存密文、KMS Key ID 和密钥版本。
- 读取运行时配置时由 `ai-service` 调用 KMS 解密，返回值只在调用模型前短暂存在。
- KMS 凭证通过环境变量、工作负载身份、机器角色或 CI/CD Secret 注入，不放入业务数据库。

建议接口：

```java
public interface AiSecretManager {
    EncryptedSecret encrypt(String plaintext, SecretContext context);

    String decrypt(EncryptedSecret encryptedSecret, SecretContext context);

    String mask(String plaintextOrCiphertext);
}
```

`SecretContext` 至少包含：
- `tenantId`
- `configId`
- `providerType`
- `code`
- `purpose`

优点：
- 密钥材料由 KMS 管理，可审计、可禁用、可轮换。
- 数据库泄露时无法直接还原 API Key。
- 更适合多租户生产环境。

限制：
- 依赖外部 KMS 可用性和权限配置。
- 需要设计 KMS 调用失败降级策略；默认应失败关闭，不应回退到明文。
- 本地开发和测试需要 mock 或 local profile。

## 方案三：配置中心引用模式

适用阶段：企业部署已有 Nacos 或统一 Secret 管理体系时。

设计：
- `ai_model_config` 不保存 Key 本体，只保存 `api_key_ref`。
- `api_key_ref` 可指向 Nacos 加密配置，例如 `cipher-aes-ai-model-config-${tenantId}-${code}.yml`，也可指向企业 Secret Manager path。
- `ai-service` 运行时根据引用读取 API Key，仍只在 Service 内部短暂持有明文。
- 后台 UI 只展示引用、是否可读取和脱敏预览，不展示完整 Key。

Nacos 注意事项：
- Nacos 配置加密依赖加密插件和 `cipher-` dataId 前缀。
- Nacos 配置加密不是认证、网络隔离、TLS 或企业 KMS 的替代品。
- 使用前必须确认目标 Nacos 版本、插件加载、`encrypted_data_key` 字段和客户端兼容性。

优点：
- 业务库彻底不保存 API Key。
- 便于企业统一配置发布、审批和回滚。

限制：
- 配置中心变成运行时依赖。
- 多租户场景需要严格 dataId/group/namespace 命名规范和权限隔离。
- 配置中心引用模式不适合需要租户自行在后台 UI 输入并立即生效的轻量部署，除非后台同时具备写入配置中心的能力。

## 推荐实施顺序

### P0：立即补强

- 已移除 `adminInsert/adminUpdate` 中包含 DTO 的日志输出，避免写入 API Key。
- 禁止新增任何包含 `apiKey`、`Authorization`、`Bearer` 的业务日志。
- 保持列表和详情脱敏测试。
- 完整 Key 查看接口保留兼容，但默认只给 `SuperAdmin` 或更细粒度安全角色。

验收：
- 单元测试覆盖新增/编辑不会记录明文 Key。
- 搜索源码确认没有业务日志输出 API Key。

### P1：应用层密文落库

- 新增密文相关字段，保留旧 `api_key` 兼容。
- 新增 `AiSecretCryptoService` 和测试。
- 新增迁移脚本：读取旧明文、写入密文、置空旧字段。
- `entity2BO` 不再把完整 Key 放入 BO。
- `toRuntimeConfig` 改为按配置 ID 或内部实体解密，不依赖 BO 携带明文。

验收：
- 数据库 `api_key` 可为空，密文字段非空。
- 列表、详情、测试模型、供应商模型查询均通过。
- 数据库 dump 中看不到可直接使用的云供应商 Key。

### P2：KMS 接入

- 引入 `AiSecretManager` 抽象。
- 支持按 profile 选择 local/kms/config-ref。
- 添加 KMS Key ID、版本、轮换时间。
- 新增轮换任务或后台操作：重新加密密文，不改变供应商 Key。
- 新增供应商 Key 替换操作：写入新密文并记录审计。

验收：
- KMS 不可用时，启用云供应商模型调用失败关闭，并返回明确错误。
- KMS 解密审计可追踪到租户、模型配置和操作类型。
- 完整 Key 查看接口下线或默认关闭。

### P3：配置中心引用

- 支持 `CONFIG_REF` 存储类型。
- 定义 Nacos namespace/group/dataId 命名规范。
- 支持只读引用模式和后台写入配置中心模式。
- 对 Nacos 加密插件加载、dataId 前缀和读取结果做启动期/健康检查。

验收：
- `ai_model_config` 只保存引用，不保存 Key 本体。
- 配置中心权限按租户隔离。
- 修改配置中心 Key 后，模型调用按刷新策略生效。

## 数据迁移策略

迁移步骤：

1. 增加新字段，保持旧逻辑可运行。
2. 部署支持双读的代码：优先读密文/引用，缺失时读旧 `api_key`。
3. 执行迁移任务，把旧 `api_key` 加密写入新字段。
4. 验证所有启用配置均可解密并测试连通。
5. 置空旧 `api_key`。
6. 后续版本删除旧字段或保留为空并禁止写入。

回滚原则：
- 迁移期间不要删除旧字段。
- 不允许把密文批量回写成明文。
- 若 KMS 或配置中心不可用，回滚到上一版本代码，但不恢复明文导出。

## 审计与权限

必须记录的操作：
- 新增 API Key。
- 替换 API Key。
- 测试模型配置。
- 查询供应商模型列表时使用已保存 Key。
- 完整 Key 查看，若兼容期仍保留。
- 密钥迁移、重加密和轮换。

审计字段：
- `tenantId`
- `userId`
- `configId`
- `configCode`
- `providerType`
- `operation`
- `success`
- `failureReason`
- `traceId`

禁止记录：
- API Key 明文。
- Authorization Header。
- KMS 明文数据密钥。
- 完整配置对象的默认 `toString()` 输出。

## 测试清单

应用层密文落库测试：
- 新增配置时 Mapper 接收到的是密文，不是输入明文。
- 编辑配置时输入空 Key 保留已有密文。
- 输入脱敏 Key 不覆盖已有密文。
- `toRuntimeConfig` 能解密并构造运行时配置。
- 列表和详情仍不序列化完整 Key。
- 数据库迁移脚本幂等。

KMS 测试：
- KMS 加密、解密、失败关闭。
- KMS Key ID 或版本缺失时报错。
- KMS 不可用时不会回退到明文。
- 轮换后旧密文可重加密，新调用使用新版本。

配置中心测试：
- `CONFIG_REF` 能读取引用。
- 引用不存在或无权限时报错。
- Nacos 加密配置缺少插件时启动检查失败或健康检查失败。

## 当前不做

- 不在本轮引入 KMS SDK 或具体云厂商依赖。
- 不在本轮变更 `ai_model_config` 表结构。
- 不在本轮删除 `adminGetApiKey`，只记录后续下线方向。
- 不把 API Key 移入 `admin-api` 或前端处理。
- 不把密钥能力放进 `x-boot-starter-ai`，除非后续有跨业务域复用需求。

## 参考

- [OWASP Secrets Management Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html)
- [OWASP Cryptographic Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Cryptographic_Storage_Cheat_Sheet.html)
- [Nacos Configuration Encryption Plugin](https://nacos.io/en/docs/next/plugin/config-encryption-plugin/)
