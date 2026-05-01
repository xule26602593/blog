# Blog System

A full-stack blog system based on Spring Boot 3 + Vue 3, featuring an admin panel and user portal.

## Tech Stack

### Backend
- Spring Boot 3.5.14
- Spring Security + JWT
- MyBatis Plus 3.27.0
- MySQL 8.0
- Redis
- Spring AI (AI Content Intelligence System)

### Frontend
- Vue 3.5
- Vite 8
- Vant 4 (Mobile-first component library)
- Pinia 3
- Axios

## Project Structure

```
.
├── blog-server/          # Backend project
│   ├── src/
│   │   ├── main/java/com/blog/
│   │   │   ├── common/          # Common modules
│   │   │   ├── domain/          # Entities/DTOs/VOs
│   │   │   ├── repository/      # Data access layer
│   │   │   ├── service/         # Business logic layer
│   │   │   ├── controller/      # Controllers
│   │   │   └── security/        # Security modules
│   │   └── main/resources/
│   │       ├── application.yml
│   │       └── db/schema.sql    # Database scripts
│   └── Dockerfile
│
├── blog-web/             # Frontend project
│   ├── src/
│   │   ├── api/                # API interfaces
│   │   ├── components/         # Components
│   │   ├── router/             # Routing
│   │   ├── stores/             # State management
│   │   ├── styles/             # Styles
│   │   ├── utils/              # Utilities
│   │   └── views/              # Pages
│   │       ├── admin/          # Admin panel
│   │       └── portal/         # User portal
│   └── Dockerfile
│
└── docker-compose.yml    # Docker orchestration
```

## Quick Start

### Requirements
- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 6+
- Maven 3.8+

### Local Development

#### 1. Create Database
```bash
# Login to MySQL
mysql -u root -p

# Create database
CREATE DATABASE blog_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

# Import schema
USE blog_db;
SOURCE blog-server/src/main/resources/db/schema.sql;
```

#### 2. Start Backend
```bash
cd blog-server

# Configure database and Redis in application-dev.yml

# Build and run
mvn clean package -DskipTests
java -jar target/blog-server-1.0.0.jar
```

#### 3. Start Frontend
```bash
cd blog-web

# Install dependencies (pnpm recommended)
pnpm install

# Run in development mode
pnpm dev
```

#### 4. Access
- Portal: http://localhost:3000
- Admin: http://localhost:3000/admin
- API Docs: http://localhost:8080/swagger-ui.html

### Docker Deployment

```bash
# Start all services
make up

# View logs
make logs

# Stop services
make down
```

## Default Accounts

| Role | Username | Password |
|------|----------|----------|
| Admin | admin | test123 |
| Test User | test | test123 |

## Features

### Admin Panel
- Dashboard - Statistics overview, knowledge graph visualization
- Article Management - CRUD, publish/unpublish, pin
- Category Management - Article categories
- Tag Management - Tags maintenance
- Comment Management - Comment moderation
- Announcement Management - System announcements
- AI Content Intelligence - Article summary generation, content analysis

### User Portal
- Home - Article list, hot articles, announcements
- Article Detail - Markdown rendering, code highlighting
- Archive - Timeline view
- Search - Keyword search
- User Center - Profile management
- Like & Bookmark - Article interactions
- Comments - Post comments
- Follow System - Follow users, notifications

## API Endpoints

### Authentication
| Endpoint | Method | Description |
|----------|--------|-------------|
| /api/auth/login | POST | Login |
| /api/auth/register | POST | Register |
| /api/auth/current | GET | Get current user |

### Articles
| Endpoint | Method | Description |
|----------|--------|-------------|
| /api/portal/articles | GET | Paginated articles |
| /api/portal/article/{id} | GET | Article detail |
| /api/portal/articles/hot | GET | Hot articles |
| /api/portal/articles/search | GET | Search articles |

### Announcements
| Endpoint | Method | Description |
|----------|--------|-------------|
| /api/portal/announcements | GET | Published announcements |

### Follow & Notifications
| Endpoint | Method | Description |
|----------|--------|-------------|
| /api/follow/{userId} | POST | Follow user |
| /api/unfollow/{userId} | POST | Unfollow user |
| /api/notifications | GET | Notification list |

### Admin Panel
| Endpoint | Method | Description |
|----------|--------|-------------|
| /api/admin/articles | GET/POST | Article management |
| /api/admin/categories | GET/POST | Category management |
| /api/admin/tags | GET/POST | Tag management |
| /api/admin/comments | GET | Comment management |
| /api/admin/announcements | GET/POST | Announcement management |

## Future Enhancements

1. **Full-text Search** - Elasticsearch integration
2. **Message Queue** - RabbitMQ for async notifications
3. **File Storage** - Aliyun OSS/MinIO integration
4. **Monitoring** - Prometheus + Grafana
5. **Log Analysis** - ELK stack
6. **Cache Optimization** - Static page caching
7. **AI Enhancement** - More AI-assisted writing features

## Changelog

### v1.1.0
- Upgrade Spring Boot to 3.5.14
- Add AI Content Intelligence System
- Add Follow & Notification System
- Add Announcement Management
- Add Knowledge Graph Visualization
- Upgrade frontend to Vue 3.5 + Vite 8
- Fix various known issues

## License

MIT
