# CLAUDE.md

Comprehensive guide for Claude Code when working with this repository.

## Build & Run Commands

```bash
make up        # Build and start all services (preferred)
make down      # Stop all services
make logs      # Tail logs
make rebuild   # Force rebuild and recreate
```

Docker ports: MySQL 3306, Redis 6379, Backend 8080, Frontend 19999

## Tech Stack

| Layer | Technology |
|-------|------------|
| Backend | Spring Boot 3.5.14 + Java 17 + MyBatis Plus 3.5.5 |
| Frontend | Vue 3.5 + Vite 8 + Vant 4 + Pinia |
| Database | MySQL 8.0 + Redis (Redisson) |
| AI | Spring AI 1.1.5 (OpenAI-compatible, default: Alibaba Qwen) |

## Architecture

**Backend Package Structure (`com.blog`):**
```
com.blog
├── common/          # Utilities, exceptions, result wrapper, config
├── controller/      # REST endpoints (admin/ + portal/)
├── domain/          # Entities, DTOs, VOs
│   └── enums/       # All enum classes
├── repository/
│   └── mapper/      # MyBatis Plus mappers
├── security/        # JWT filter, UserDetailsService
└── service/
    └── ai/          # AI services (summary, tags, writing assistant)
```

**Frontend Directory Structure (`src/`):**
```
src/
├── api/             # Axios API modules
├── components/      # Reusable Vue components
├── router/          # Vue Router with auth guards
├── stores/          # Pinia stores (user, app, theme, reading)
└── views/
    ├── admin/       # Admin panel (requires ADMIN role)
    └── portal/      # Public pages
```

## Key Patterns

- **Service layer**: Interface in `service/`, implementation in `service/impl/`
- **Response wrapper**: `Result<T>` in `common/result/` for unified API responses
- **Soft delete**: `deleted` field with `@TableLogic` annotation
- **Enum conversion**: All enums in `domain/enums/` with `nameFromCode()` for frontend strings

## Conventions

**Backend:**
- Spring Boot 3 uses `jakarta.*` (not `javax.*`)
- All configs in `common/config/`
- Entities use `@TableName`, `@TableId(type = IdType.AUTO)` from MyBatis Plus

**Frontend:**
- Auto-import enabled for Vue APIs, Vue Router, Pinia
- Vant components auto-registered via unplugin
- Router uses hash history mode (`createWebHashHistory`)

## API Endpoints

**Public (no auth):**
- `POST /api/auth/login`, `/api/auth/register`
- `GET /api/portal/articles`, `/api/portal/article/{id}`
- `GET /api/portal/series`, `/api/portal/series/{id}`

**Admin (requires `ROLE_ADMIN`):**
- Article/Category/Tag/Series/Topic CRUD under `/api/admin/`
- AI endpoints under `/api/admin/ai/`

**Security Whitelist:**
- `/api/portal/**`, `/api/auth/login`, `/api/auth/register`
- `/uploads/**`, `/swagger-ui.html`, `/swagger-ui/**`, `/v3/api-docs/**`

## Enum Reference (`domain/enums/`)

| Enum | Values |
|------|--------|
| `TopicStatus` | PENDING/WRITING/PUBLISHED/ABANDONED |
| `TopicPriority` | HIGH/MEDIUM/LOW |
| `AnalysisStatus` | PENDING/ANALYZING/COMPLETED/FAILED |
| `ArticleStatus` | DRAFT/PUBLISHED/RECYCLE |
| `CommentStatus` | PENDING/APPROVED/REJECTED |
| `NotificationType` | FOLLOW/COMMENT/REPLY/ANNOUNCEMENT |
| `UserRole` | ADMIN/VISITOR |
| `ActionType` | LIKE/FAVORITE |

## AI Services (`service/ai/`)

| Service | Purpose |
|---------|---------|
| `AiService` | Core AI client wrapper |
| `SummaryService` | Article summary generation |
| `TagExtractService` | Auto tag extraction |
| `WritingAssistantService` | Outline generation, content polishing |
| `PromptTemplateService` | Manage prompt templates from database |

**Note:** Spring AI uses StringTemplate (ST4). JSON with `{...}` must go in `system_prompt`, not `user_template`.

## Database

Schema: `blog-server/src/main/resources/db/schema.sql`

**Key tables:**
- Core: `sys_user`, `category`, `tag`, `article`, `article_tag`, `comment`
- Extended: `series`, `series_article`, `user_follow`, `notification`, `announcement`
- AI: `prompt_template`, `article_ai_meta`, `user_reading_profile`
- Topic: `topic` - Topic inspiration library

Character set: **utf8mb4** (required for Chinese)

## Configuration

| Component | Config File |
|-----------|-------------|
| Backend | `application.yml`, `application-dev.yml`, `application-prod.yml` |
| Frontend | `.env.development`, `.env.production` |
| Docker | `docker-compose.yml` |

**AI Environment Variables:**
```
AI_API_KEY=your-api-key
AI_API_ENDPOINT=https://dashscope.aliyuncs.com/compatible-mode/v1
AI_MODEL=qwen-plus
```

**Default Credentials:**
| Role | Username | Password |
|------|----------|----------|
| Admin | admin | test123 |
| Test User | test | test123 |

## Reference Documentation

- Database schema: `blog-server/src/main/resources/db/schema.sql`
- Design specs: `docs/superpowers/specs/`
- Implementation plans: `docs/superpowers/plans/`
- Documented solutions: `docs/solutions/` (bugs, best practices, patterns)

## Testing

No automated tests currently. When adding:
```bash
cd blog-server && mvn test
cd blog-web && pnpm test
```
