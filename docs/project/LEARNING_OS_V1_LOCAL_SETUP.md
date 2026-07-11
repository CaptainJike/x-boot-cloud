# Learning OS V1 local setup

This guide turns the current `x-boot-cloud` backend into a runnable Learning OS V1 local stack for the `learn-os` web prototype.

## Scope

Learning OS V1 uses these services:

- `sys-service`
- `ai-service`
- `learning-service`
- `app-api`

V1 does not require:

- `admin-api`
- `oss-service`
- Qdrant
- RAG knowledge indexing
- multi-agent runtime

## 1. Prepare local dependencies

Required:

- Java 21
- Maven 3.9+
- MySQL 8.x
- Redis 6.x+
- Nacos 2.x

Optional but recommended:

- Ollama on `http://127.0.0.1:11434`
- a local model such as `llama3.2`

## 2. Copy local env

```bash
cd /Users/zhuxucai/workspace/github/x-boot-cloud
cp scripts/learning-os/env.local.example scripts/learning-os/env.local
```

Edit `scripts/learning-os/env.local` and fill:

- MySQL username/password
- Redis password if needed
- GitHub OAuth `LEARNING_GITHUB_CLIENT_ID`
- GitHub OAuth `LEARNING_GITHUB_CLIENT_SECRET`
- `NACOS_ACCESS_TOKEN` if your Nacos instance requires token-based OpenAPI auth

Important:

- `LEARNING_GITHUB_REDIRECT_URI` should point to the web prototype callback page:
  `http://localhost:5173/auth/callback`
- The frontend callback page receives `code/state` from GitHub, then calls `app-api` callback exchange.

## 3. Initialize database

```bash
bash scripts/learning-os/init-db.sh
```

What gets imported:

- `x_boot_sys.sql`
- `x_boot_ai.sql`
- `x_boot_learning.sql`
- `AI_MODEL_CONFIG_INIT_SQL.sql`

Database name defaults to `x_boot_learning_os`.

If you prefer manual import, run from repo root:

```bash
mysql -u root -p < scripts/learning-os/sql/init_learning_os_v1.sql
```

## 4. Publish Nacos config

```bash
bash scripts/learning-os/publish-nacos-config.sh
```

If your Nacos instance enables auth, the script will try this order:

1. use `NACOS_ACCESS_TOKEN` directly if present
2. otherwise call `/nacos/v1/auth/login` with `NACOS_USERNAME` / `NACOS_PASSWORD`
3. otherwise publish without auth parameters

This publishes:

- `COMMON.yml`
- `DATASOURCE.yml`
- `REDIS.yml`
- `SA-TOKEN.yml`
- `DUBBO.yml`
- `sys-service.yml`
- `ai-service.yml`
- `learning-service.yml`
- `app-api.yml`

Local defaults shipped in this repo:

- `app-api` on `8080`
- `sys-service` on `7101`
- `ai-service` on `7102`
- `learning-service` on `7103`
- `x.ai.knowledge.vector-store.enabled=false`

That last setting is intentional for V1 local run, because Learning OS V1 does not need Qdrant.

## 5. Start backend services

```bash
bash scripts/learning-os/run-local.sh
```

Logs are written to:

```text
.logs/learning-os/
```

Stop them with:

```bash
bash scripts/learning-os/stop-local.sh
```

## 6. Start the web prototype

In the `learn-os` repo:

```bash
cd /Users/zhuxucai/workspace/github/learn-os
npm install
npm run dev
```

If you want explicit local env:

```bash
cat > .env.local <<'EOF'
VITE_API_BASE_URL=/app/v1
VITE_PROXY_TARGET=http://127.0.0.1:8080
EOF
```

Then open:

- web: `http://127.0.0.1:5173`
- backend app-api: `http://127.0.0.1:8080`

## 7. GitHub OAuth setup

Create a GitHub OAuth App with:

- Homepage URL: `http://localhost:5173`
- Authorization callback URL: `http://localhost:5173/auth/callback`

Then place its client id / secret into `scripts/learning-os/env.local`.

## 8. First integration flow

Recommended smoke test:

1. Open `http://127.0.0.1:5173`
2. Click GitHub login
3. Finish OAuth callback
4. Create a goal with `学习 Spring AI`
5. Verify `Today`, `Map`, `Tutor`, `Reflection`, `Growth`

Expected behavior:

1. First login creates or binds `sys_user`
2. First login also creates an empty `learner_profile`
3. Goal creation generates a map with objectives, verification method, completion criteria
4. Tutor starts with diagnosis before explanation
5. Reflection creates a daily digest and growth snapshot

## 9. AI fallback behavior

Learning OS V1 can still run without a healthy model provider, but with degraded intelligence.

If one of these fails:

- `ai-service` default model config missing
- model request timeout
- invalid structured JSON from the model

the backend falls back to:

- built-in learning templates
- conservative tutor diagnosis
- default reflection summary

This is enough for local UI integration and full-loop smoke testing.

## 10. Useful checks

Backend compile:

```bash
mvn -pl x-boot-api/app-api,x-boot-modules/learning/learning-service,x-boot-modules/sys/sys-service -am -DskipTests compile
```

Frontend build:

```bash
cd /Users/zhuxucai/workspace/github/learn-os
npm run typecheck
npm run build
```
