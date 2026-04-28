# AGENTS.md - Blog System Development Guide

Essential repo-specific guidance for OpenCode agents.

## 🏗️ Architecture & Structure

**Two-project monorepo:**
- `blog-server/` - Spring Boot 3.2.0 + Java 17 backend
- `blog-web/` - Vue 3.5 + Vite 5 frontend

**Services orchestrated via Docker Compose:**
- MySQL (3306), Redis (6379) - required for backend
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
mvn clean package -DskipTests    # Build JAR
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
- **Soft delete**: `deleted` field (0=active, 1=deleted)
- **JWT**: `Bearer` token with 24-hour expiration
- **Profiles**: `application-dev.yml` (local), `application-prod.yml` (Docker)
- **File upload**: Configured in `file.upload-path`

### Frontend
- **Auto-imports**: Vue APIs, Router, Pinia enabled via `unplugin-auto-import`
- **Vant UI**: Components auto-registered
- **Proxy**: `/api` → `http://localhost:8080` in dev
- **State**: Pinia stores in `stores/` with persisted `userStore`

## 🔐 Security & Authentication

**Public endpoints (no auth):**
- `/api/portal/**` - All portal APIs
- `/api/auth/login`, `/api/auth/register`
- `/uploads/**`, `/swagger-ui/**`

**Admin endpoints require `ROLE_ADMIN` role:**
- `/api/admin/**`

**Default credentials:**
- Admin: `admin` / `test123`
- Test user: `test` / `test123`

## 📝 Development Conventions

- **Service layer**: Interface in `service/`, implementation in `service/impl/`
- **Response wrapper**: `Result<T>` for unified API responses
- **Entities**: Use `@TableName`, `@TableId(type = IdType.AUTO)` (MyBatis Plus)
- **Database scripts**: `blog-server/src/main/resources/db/schema.sql`
- **Route guards**: Check `userStore.isLoggedIn` and `userStore.roleCode` in frontend

## 🔍 Reference Documentation

- Detailed guidance in `CLAUDE.md`
- Database schema in `blog-server/src/main/resources/db/schema.sql`
- Documented solutions in `docs/solutions/` (YAML frontmatter with `module`, `tags`)

## ⚠️ Common Gotchas

1. **Database charset must be `utf8mb4`** (configured in Docker and schema)
2. **Redis required** for caching (configured via Redisson)
3. **File uploads** stored in `upload_data/` volume (mapped in Docker)
4. **Admin routes** require `ROLE_ADMIN` role, not just authentication
5. **Vite dev server** runs on port 3000, Docker serves on port 19999