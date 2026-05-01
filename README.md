# 博客系统

一个基于 Spring Boot 3 + Vue 3 的全栈博客系统，包含管理后台和用户前台。

## 技术栈

### 后端
- Spring Boot 3.5.14
- Spring Security + JWT
- MyBatis Plus 3.27.0
- MySQL 8.0
- Redis
- Spring AI（AI内容智能系统）

### 前端
- Vue 3.5
- Vite 8
- Vant 4（移动端组件库）
- Pinia 3
- Axios

## 项目结构

```
.
├── blog-server/          # 后端项目
│   ├── src/
│   │   ├── main/java/com/blog/
│   │   │   ├── common/          # 公共模块
│   │   │   ├── domain/          # 实体/DTO/VO
│   │   │   ├── repository/      # 数据访问层
│   │   │   ├── service/         # 业务逻辑层
│   │   │   ├── controller/      # 控制器层
│   │   │   └── security/        # 安全模块
│   │   └── main/resources/
│   │       ├── application.yml
│   │       └── db/schema.sql    # 数据库脚本
│   └── Dockerfile
│
├── blog-web/             # 前端项目
│   ├── src/
│   │   ├── api/                # API接口
│   │   ├── components/         # 组件
│   │   ├── router/             # 路由
│   │   ├── stores/             # 状态管理
│   │   ├── styles/             # 样式
│   │   ├── utils/              # 工具函数
│   │   └── views/              # 页面
│   │       ├── admin/          # 管理后台
│   │       └── portal/         # 用户前台
│   └── Dockerfile
│
└── docker-compose.yml    # Docker 编排
```

## 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 6+
- Maven 3.8+

### 本地开发

#### 1. 创建数据库
```bash
# 登录 MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE blog_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

# 导入表结构
USE blog_db;
SOURCE blog-server/src/main/resources/db/schema.sql;
```

#### 2. 启动后端
```bash
cd blog-server

# 修改 application-dev.yml 中的数据库和Redis配置

# 编译运行
mvn clean package -DskipTests
java -jar target/blog-server-1.0.0.jar
```

#### 3. 启动前端
```bash
cd blog-web

# 安装依赖
npm install

# 开发模式运行
npm run dev
```

#### 4. 访问
- 前台地址: http://localhost:3000
- 后台地址: http://localhost:3000/admin
- API文档: http://localhost:8080/swagger-ui.html

### Docker 部署

```bash
# 一键启动
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

## 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | test123 |
| 测试用户 | test | test123 |

## 功能模块

### 管理后台
- 仪表盘 - 数据统计概览、知识图谱可视化
- 文章管理 - CRUD、发布/撤回、置顶
- 分类管理 - 文章分类维护
- 标签管理 - 标签维护
- 评论管理 - 评论审核
- 公告管理 - 系统公告发布
- AI内容智能系统 - 文章摘要生成、内容分析

### 用户前台
- 首页 - 文章列表、热门文章、公告展示
- 文章详情 - Markdown渲染、代码高亮
- 归档 - 按时间线展示
- 搜索 - 关键词搜索
- 用户中心 - 个人信息管理
- 点赞收藏 - 文章互动
- 评论 - 发表评论
- 关注系统 - 关注用户、消息通知

## API 接口

### 认证模块
| 接口 | 方法 | 说明 |
|------|------|------|
| /api/auth/login | POST | 登录 |
| /api/auth/register | POST | 注册 |
| /api/auth/current | GET | 获取当前用户 |

### 文章模块
| 接口 | 方法 | 说明 |
|------|------|------|
| /api/portal/articles | GET | 分页查询文章 |
| /api/portal/article/{id} | GET | 获取文章详情 |
| /api/portal/articles/hot | GET | 热门文章 |
| /api/portal/articles/search | GET | 搜索文章 |

### 公告模块
| 接口 | 方法 | 说明 |
|------|------|------|
| /api/portal/announcements | GET | 获取已发布公告 |

### 关注与通知模块
| 接口 | 方法 | 说明 |
|------|------|------|
| /api/follow/{userId} | POST | 关注用户 |
| /api/unfollow/{userId} | POST | 取消关注 |
| /api/notifications | GET | 获取通知列表 |

### 管理后台
| 接口 | 方法 | 说明 |
|------|------|------|
| /api/admin/articles | GET/POST | 文章管理 |
| /api/admin/categories | GET/POST | 分类管理 |
| /api/admin/tags | GET/POST | 标签管理 |
| /api/admin/comments | GET | 评论管理 |
| /api/admin/announcements | GET/POST | 公告管理 |

## 后期拓展建议

1. **全文搜索** - 接入 Elasticsearch 实现文章全文搜索
2. **消息队列** - 使用 RabbitMQ 异步处理评论通知
3. **文件存储** - 对接阿里云OSS/MinIO存储图片
4. **监控告警** - 集成 Prometheus + Grafana 监控
5. **日志分析** - ELK 收集分析访问日志
6. **缓存优化** - 文章详情页静态化
7. **AI功能增强** - 更多AI辅助写作功能

## 更新日志

### v1.1.0
- 升级 Spring Boot 至 3.5.14
- 新增 AI 内容智能系统
- 新增关注与通知系统
- 新增公告管理功能
- 新增知识图谱可视化
- 前端升级 Vue 3.5 + Vite 8
- 修复若干已知问题

## License

MIT
