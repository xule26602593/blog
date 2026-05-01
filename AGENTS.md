# AGENTS.md

Essential quick reference for AI agents. See `CLAUDE.md` for detailed documentation.

## 🌐 Language Preference

**使用中文回复** - All responses and explanations should be in Chinese (中文).

## 🚀 Quick Commands

```bash
make up        # Build and start all services
make down      # Stop all services
make logs      # Tail logs
make rebuild   # Force rebuild and recreate
```

## ⚠️ Critical Gotchas

1. **Database charset must be `utf8mb4`**
2. **Spring Boot 3 uses `jakarta.*`** (not `javax.*`)
3. **Admin routes require `ROLE_ADMIN`**
4. **All configs in `common/config/`**
5. **All enums in `domain/enums/`** - use `nameFromCode()` for frontend
6. **AI API key required**: `AI_API_KEY` env var

## 📖 Documentation

See `CLAUDE.md` for: Architecture, API endpoints, conventions, enum reference, AI services, database schema, configuration.
