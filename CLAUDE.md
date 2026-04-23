# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

### Full Stack (Docker)
```bash
make up        # Build and start all services (MySQL + Redis + backend + frontend)
make down      # Stop all services
make logs      # Tail logs
make restart   # Restart all services
make clean     # Remove containers, volumes, and images
make rebuild   # Force rebuild and recreate containers
```

Docker ports: MySQL 3306, Redis 6379, Backend 8080, Frontend 19999

### Backend (blog-server)
```bash
cd blog-server
mvn clean package -DskipTests     # Build JAR
mvn spring-boot:run               # Run in dev mode (requires local MySQL + Redis)
java -jar target/blog-server-1.0.0.jar  # Run JAR directly
```

### Frontend (blog-web)
```bash
cd blog-web
pnpm install                      # Install dependencies (pnpm preferred)
pnpm dev                          # Dev server on port 3000
pnpm build                        # Production build to dist/
```

### Database Initialization
```bash
mysql -u root -p
CREATE DATABASE blog_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
SOURCE blog-server/src/main/resources/db/schema.sql;
```

## Architecture Overview

This is a full-stack blog system with Spring Boot 3 backend and Vue 3 frontend.

### Backend (blog-server)
- **Framework**: Spring Boot 3.2.0 + Java 17
- **Security**: Spring Security + JWT authentication
- **ORM**: MyBatis Plus 3.5.5
- **Database**: MySQL 8.0 + Redis (caching)
- **API Docs**: Swagger UI at `/swagger-ui.html`

**Package Structure**:
```
com.blog
├── common/          # Shared utilities, exceptions, result wrapper
├── config/          # Spring configurations (Security, Redis, etc.)
├── controller/      # REST endpoints
│   ├── admin/       # Admin APIs (requires ADMIN role)
│   └── portal/      # Public APIs
├── domain/          # Entities, DTOs, VOs
├── repository/      # MyBatis Plus mappers
├── security/        # JWT filter, UserDetailsService
└── service/         # Business logic (interface + impl/)
```

**Key Patterns**:
- Service layer: Interface in `service/`, implementation in `service/impl/`
- Response wrapper: `Result<T>` in `common/result/` for unified API responses
- Global exception handler in `common/exception/`

### Frontend (blog-web)
- **Framework**: Vue 3.5 + Vite 8
- **UI**: Vant 4 (mobile-first component library)
- **Markdown**: md-editor-v3 with syntax highlighting
- **State**: Pinia stores in `stores/`
- **HTTP**: Axios with request wrapper in `utils/request.js`

**Directory Structure**:
```
src/
├── api/             # Axios API modules (auth.js, article.js, admin.js, etc.)
├── components/      # Reusable Vue components
├── router/          # Vue Router with auth guards
├── stores/          # Pinia stores (user.js, app.js, theme.js)
├── utils/           # request.js (axios instance with interceptors)
└── views/
    ├── admin/       # Admin panel (Dashboard, ArticleManage, ArticleEdit, etc.)
    └── portal/      # Public pages (Home, ArticleDetail, Login, UserCenter, etc.)
```

**Routing**:
- `/` → Portal pages (public)
- `/admin` → Admin panel (requires `ADMIN` role)
- Route guards in `router/index.js` check `userStore.isLoggedIn` and `userStore.roleCode`

### API Endpoints

**Public (Portal)**:
- `POST /api/auth/login`, `/api/auth/register` - Authentication
- `GET /api/portal/articles` - Paginated article list
- `GET /api/portal/article/{id}` - Article detail
- `GET /api/portal/articles/hot`, `/api/portal/articles/search` - Hot articles, search

**Admin (requires JWT + ADMIN role)**:
- `GET/POST/PUT/DELETE /api/admin/articles` - Article CRUD
- `GET/POST/PUT/DELETE /api/admin/categories` - Category CRUD
- `GET/POST/PUT/DELETE /api/admin/tags` - Tag CRUD
- `GET /api/admin/comments` - Comment management

### Configuration

- Backend: `application.yml` (main), `application-dev.yml`, `application-prod.yml`
- Frontend: `.env.development`, `.env.production` for API base URL
- Docker: `docker-compose.yml` orchestrates MySQL, Redis, backend, frontend

### Default Credentials

| Role | Username | Password |
|------|----------|----------|
| Admin | admin | admin123 |
| Test User | test | test123 |

## Development Notes

### Context7 Usage Rule
**IMPORTANT**: Whenever the user mentions libraries, frameworks, or code generation, ALWAYS use the context7 MCP tool to fetch up-to-date documentation before proceeding. This ensures accurate, current information about:
- Library APIs and usage patterns
- Framework features and best practices
- Code generation tools and their capabilities

### Backend Conventions
- Entities use `@TableName`, `@TableId(type = IdType.AUTO)` from MyBatis Plus
- Soft delete via `deleted` field (0=active, 1=deleted)
- JWT secret configured in `application.yml` under `jwt.secret`
- File uploads stored in path configured by `file.upload-path`

### Frontend Conventions
- Auto-import enabled for Vue APIs, Vue Router, Pinia (see `vite.config.js`)
- Vant components auto-registered via unplugin with VantResolver
- API base URL proxied through Vite dev server (`/api` → `http://localhost:8080`)
- User state persisted in Pinia `userStore` with `token`, `userInfo`, `roleCode`
- Router uses hash history mode (`createWebHashHistory`)

### Database Schema
- Schema initialization: `blog-server/src/main/resources/db/schema.sql`
- Tables: `sys_role`, `sys_user`, `category`, `tag`, `article`, `article_tag`, `comment`, `message`, `user_action`, `visit_log`, `sys_config`
- Character set: utf8mb4

### Security White List
Public endpoints (no auth required):
- `/api/portal/**` - All public portal APIs
- `/api/auth/login`, `/api/auth/register` - Authentication
- `/uploads/**` - Static file uploads
- `/swagger-ui.html`, `/swagger-ui/**`, `/v3/api-docs/**` - API documentation

Admin endpoints require `ROLE_ADMIN`: `/api/admin/**`
