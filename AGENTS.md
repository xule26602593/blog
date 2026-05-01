# AGENTS.md - Blog System Development Guide

Essential repo-specific guidance for OpenCode agents.

## 🌐 Language Preference

**使用中文回复** - All responses and explanations should be in Chinese (中文).

## 🏗️ Architecture & Structure

**Two-project monorepo:**
- `blog-server/` - Spring Boot 3.5.14 + Java 17 backend
- `blog-web/` - Vue 3.5 + Vite 8 frontend

**Services orchestrated via Docker Compose:**
- MySQL 8.0 (3306), Redis 7 (6379) - required for backend
- Backend (8080), Frontend (19999)

## 🚀 Core Commands

### Docker (Makefile shortcuts)
```bash
make up        # Build and start all services (preferred)
make down      # Stop all services
make logs      # Tail logs
make restart   # Restart all services
make clean     # Remove containers, volumes, images
make rebuild   # Force rebuild and recreate
```

### Backend Development
```bash
cd blog-server
mvn spring-boot:run              # Dev mode (needs local MySQL+Redis)
mvn clean package -DskipTests    # Build JAR (no tests in project)
java -jar target/blog-server-1.0.0.jar
```

### Frontend Development
```bash
cd blog-web
pnpm install                     # Install dependencies (pnpm preferred)
pnpm dev                         # Dev server on port 3000
pnpm build                       # Production build
```

## ⚙️ Configuration Notes

### Backend
- **Database**: MySQL with `utf8mb4` charset required
- **Soft delete**: `deleted` field (0=active, 1=deleted) - MyBatis Plus global config
- **JWT**: Bearer token with 24-hour expiration (HS256)
- **Profiles**: `application-dev.yml` (local), `application-prod.yml` (Docker)
- **File upload**: Max 10MB, stored in `/app/uploads/` (Docker) or `/home/demo/blog-server/uploads/` (dev)
- **AI Integration**: Spring AI with OpenAI-compatible API, default model `glm-5.1`
- **Rate Limiting**: Sentinel configured for traffic control

### Frontend
- **Auto-imports**: Vue APIs, Router, Pinia enabled via `unplugin-auto-import`
- **Vant UI**: Components auto-registered via `unplugin-vue-components`
- **Proxy**: `/api` and `/uploads` → `http://localhost:8080` in dev
- **State**: Pinia stores in `stores/`
- **Path alias**: `@` → `src/`

## 🔐 Security & Authentication

**Public endpoints (no auth):**
- `/api/portal/**` - All portal APIs
- `/api/auth/login`, `/api/auth/register`
- `/uploads/**`, `/swagger-ui/**`

**Admin endpoints require `ROLE_ADMIN` role:**
- `/api/admin/**`

**Default credentials:**
| Role | Username | Password |
|------|----------|----------|
| Admin | admin | test123 |
| Test user | test | test123 |

## 📝 Development Conventions

- **Service layer**: Interface in `service/`, implementation in `service/impl/`
- **Response wrapper**: `Result<T>` for unified API responses
- **Entities**: Use `@TableName`, `@TableId(type = IdType.AUTO)` (MyBatis Plus)
- **Database scripts**: `blog-server/src/main/resources/db/schema.sql`
- **Route guards**: Check `userStore.isLoggedIn` and `userStore.roleCode` in frontend

## 🔧 Tech Stack Versions

| Component | Version |
|-----------|---------|
| Spring Boot | 3.5.14 |
| Java | 17 |
| MyBatis Plus | 3.5.5 |
| Vue | 3.5.32 |
| Vite | 8.0.8 |
| Vant | 4.9.24 |
| Pinia | 3.0.4 |
| Vue Router | 5.0.4 |
| Redisson | 3.27.0 |
| Spring AI | 1.1.5 |

## 🔍 Reference Documentation

- Database schema: `blog-server/src/main/resources/db/schema.sql`
- Design specs: `docs/superpowers/specs/`
- Implementation plans: `docs/superpowers/plans/`
- Feature docs: `docs/features/` (checkin, achievement systems)
- Migrations: `docs/migrations/`

## ⚠️ Common Gotchas

1. **Database charset must be `utf8mb4`** (configured in Docker and schema)
2. **Redis required** for caching (configured via Redisson)
3. **File uploads** stored in `upload_data/` volume (mapped in Docker)
4. **Admin routes** require `ROLE_ADMIN` role, not just authentication
5. **Vite dev server** runs on port 3000, Docker serves on port 19999
6. **No test suite** - project has test dependency but no test cases
7. **No CI/CD** - no GitHub Actions or other automation configured
8. **AI API key** required in environment: `AI_API_KEY` for Spring AI features

## 🌿 Environment Variables

**Backend (Docker):**
```
AI_API_KEY=your-api-key
AI_API_ENDPOINT=https://dashscope.aliyuncs.com/compatible-mode/v1
AI_MODEL=glm-5.1
```

**Frontend:**
```
VITE_API_BASE_URL=/
VITE_APP_TITLE=我的博客
```
