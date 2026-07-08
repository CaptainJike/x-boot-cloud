-- x-boot-cloud AI model config bootstrap sample data.
-- Target database: MySQL 8.x.
-- Scope: sample rows for x_boot_ai.ai_model_config only.
--
-- Usage:
--   1. Run x-boot-modules/ai/ai-service/src/main/resources/sql/x_boot_ai.sql first.
--   2. Set @ai_tenant_id to the tenant that should own these model configs.
--   3. Import this file.
--   4. For cloud providers, edit the config in the backend and enter the real API Key.
--
-- Security note:
--   This file intentionally does not contain real API Keys.
--   Disabled cloud-provider samples keep api_key NULL and must not be enabled before
--   a tenant operator stores a valid key through the backend UI or a secure import process.
--
-- ID ranges:
--   220000001-220000099  AI model config sample rows

SET @seed_time := NOW();
SET @seed_by := 'ai-model-config-init';
SET @ai_tenant_id := 0;
SET @ai_model_config_base_id := 220000000;

INSERT INTO `ai_model_config` (
    `id`, `tenant_id`, `revision`, `del_flag`, `created_at`, `created_by`, `updated_at`, `updated_by`,
    `code`, `name`, `provider_type`, `base_url`, `api_key`, `model_name`,
    `supported_modalities`, `supported_capabilities`, `temperature`, `timeout_seconds`, `status`, `default_flag`, `description`
) VALUES
    (@ai_model_config_base_id + 1, @ai_tenant_id, 1, 0, @seed_time, @seed_by, @seed_time, @seed_by,
     'local_ollama_default', '本地 Ollama 默认模型', 'OLLAMA', 'http://localhost:11434', NULL, 'llama3.2',
     'text', 'chat', 0.70, 60, 1, 1, '默认启用的本地 Ollama 示例配置，无需 API Key。'),
    (@ai_model_config_base_id + 2, @ai_tenant_id, 1, 0, @seed_time, @seed_by, @seed_time, @seed_by,
     'deepseek_chat', 'DeepSeek Chat 示例', 'DEEPSEEK', 'https://api.deepseek.com', NULL, 'deepseek-chat',
     'text', 'chat', 0.70, 60, 0, 0, '禁用的 DeepSeek 示例配置，启用前请在后台录入 API Key。'),
    (@ai_model_config_base_id + 3, @ai_tenant_id, 1, 0, @seed_time, @seed_by, @seed_time, @seed_by,
     'qwen_plus', '通义千问 OpenAI 兼容示例', 'OPENAI_COMPATIBLE',
     'https://dashscope.aliyuncs.com/compatible-mode/v1', NULL, 'qwen-plus',
     'text', 'chat', 0.70, 60, 0, 0, '禁用的 OpenAI 兼容接口示例，启用前请在后台录入 API Key。'),
    (@ai_model_config_base_id + 4, @ai_tenant_id, 1, 0, @seed_time, @seed_by, @seed_time, @seed_by,
     'openai_gpt4o_mini', 'OpenAI GPT-4o mini 示例', 'OPENAI', 'https://api.openai.com/v1', NULL, 'gpt-4o-mini',
     'text,image', 'chat', 0.70, 60, 0, 0, '禁用的 OpenAI 示例配置，启用前请在后台录入 API Key。')
ON DUPLICATE KEY UPDATE
    `revision` = VALUES(`revision`),
    `del_flag` = VALUES(`del_flag`),
    `updated_at` = VALUES(`updated_at`),
    `updated_by` = VALUES(`updated_by`),
    `name` = VALUES(`name`),
    `provider_type` = VALUES(`provider_type`),
    `base_url` = VALUES(`base_url`),
    `api_key` = COALESCE(VALUES(`api_key`), `api_key`),
    `model_name` = VALUES(`model_name`),
    `supported_modalities` = VALUES(`supported_modalities`),
    `supported_capabilities` = VALUES(`supported_capabilities`),
    `temperature` = VALUES(`temperature`),
    `timeout_seconds` = VALUES(`timeout_seconds`),
    `status` = VALUES(`status`),
    `default_flag` = VALUES(`default_flag`),
    `description` = VALUES(`description`);

-- Verification query after import:
-- SELECT `tenant_id`, `code`, `provider_type`, `model_name`, `status`, `default_flag`
-- FROM `ai_model_config`
-- WHERE `tenant_id` = @ai_tenant_id
-- ORDER BY `default_flag` DESC, `code` ASC;
