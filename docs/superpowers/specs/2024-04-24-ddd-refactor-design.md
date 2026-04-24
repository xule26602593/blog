# 博客系统DDD重构设计文档

## 1. 概述

### 1.1 背景

当前博客系统采用传统三层架构（Controller-Service-Repository），存在以下问题：
- **贫血模型**：Entity只是数据容器，没有业务行为
- **Service层过重**：ArticleServiceImpl超过500行，职责混杂
- **基础设施泄露**：Service直接依赖多个Mapper，业务逻辑与数据访问耦合
- **缺乏领域边界**：模块间依赖不清晰，难以独立演进

### 1.2 重构目标

采用领域驱动设计（DDD）重构系统，实现：
- 丰富的领域模型，业务逻辑封装在聚合根中
- 按限界上下文组织代码，边界清晰
- 领域层独立于基础设施，易于测试和维护
- 通过领域事件实现模块间解耦

### 1.3 设计决策

| 决策项 | 选择 | 理由 |
|--------|------|------|
| 重构范围 | 完整DDD重构 | 按限界上下文重构，引入完整DDD战术模式 |
| 代码组织 | 单体应用 + 模块化 | 保持部署简单，按上下文划分模块包 |
| 聚合粒度 | 小聚合根 | 聚合边界清晰，事务边界小，易于演进 |
| 事件机制 | 内存事件总线 | Spring ApplicationEvent，适合单体应用 |
| 数据访问 | 仓储模式完全封装 | 领域层只依赖Repository接口 |

---

## 2. 限界上下文划分

### 2.1 上下文总览

```
┌─────────────────────────────────────────────────────────────────────┐
│                          Blog System                                 │
├─────────────────┬─────────────────┬─────────────────┬───────────────┤
│  Content        │  User           │  Interaction    │  System       │
│  Context        │  Context        │  Context        │  Context      │
├─────────────────┼─────────────────┼─────────────────┼───────────────┤
│ • Article       │ • User          │ • Comment       │ • Announcement│
│ • Category      │ • UserFollow    │ • Message       │ • SensitiveWord│
│ • Tag           │ • Notification  │ • PrivateMessage│ • Media       │
│ • Series        │                 │ • Favorite      │ • SysConfig   │
│                 │                 │ • ReadingHistory│               │
└─────────────────┴─────────────────┴─────────────────┴───────────────┘
```

### 2.2 Content Context（内容上下文）

**职责**：管理文章创作、分类、标签和系列组织

| 聚合根 | 包含实体 | 说明 |
|--------|---------|------|
| Article | 无 | 文章聚合，通过ID引用Category、Tag、Series |
| Category | 无 | 分类聚合，独立生命周期 |
| Tag | 无 | 标签聚合，独立生命周期 |
| Series | SeriesArticle | 系列聚合，管理文章排序 |

### 2.3 User Context（用户上下文）

**职责**：管理用户信息、社交关系和消息通知

| 聚合根 | 包含实体 | 说明 |
|--------|---------|------|
| User | 无 | 用户聚合，管理用户信息和角色 |
| UserFollow | 无 | 关注关系聚合，独立于User避免大聚合 |
| Notification | 无 | 通知聚合，用户的消息通知 |

### 2.4 Interaction Context（互动上下文）

**职责**：管理用户互动行为（评论、留言、收藏、阅读历史）

| 聚合根 | 包含实体 | 说明 |
|--------|---------|------|
| Comment | 无 | 评论聚合，支持嵌套回复 |
| Message | 无 | 留言板消息聚合 |
| PrivateMessage | Message | 私信聚合，按会话组织 |
| Favorite | 无 | 收藏聚合 |
| ReadingHistory | 无 | 阅读历史聚合 |

### 2.5 System Context（系统上下文）

**职责**：管理系统配置、公告和媒体资源

| 聚合根 | 包含实体 | 说明 |
|--------|---------|------|
| Announcement | 无 | 系统公告聚合 |
| SensitiveWord | 无 | 敏感词聚合 |
| Media | 无 | 媒体文件聚合 |
| SysConfig | 无 | 系统配置聚合 |

---

## 3. 分层架构设计

### 3.1 四层架构

每个限界上下文采用四层架构：

```
┌─────────────────────────────────────────────────────────────────┐
│                      Interfaces Layer                            │
│  (Controller, DTO/VO, 数据转换)                                   │
│  职责：接收请求、参数校验、调用应用服务、返回响应                      │
└─────────────────────────────────────────────────────────────────┘
                              ↓ 调用
┌─────────────────────────────────────────────────────────────────┐
│                      Application Layer                           │
│  (ApplicationService, Command/Query Handler)                     │
│  职责：编排用例、事务控制、发布领域事件、权限校验                      │
└─────────────────────────────────────────────────────────────────┘
                              ↓ 调用
┌─────────────────────────────────────────────────────────────────┐
│                        Domain Layer                              │
│  (AggregateRoot, Entity, ValueObject, DomainService, Repository) │
│  职责：核心业务逻辑、领域规则、领域事件定义                           │
└─────────────────────────────────────────────────────────────────┘
                              ↓ 依赖接口
┌─────────────────────────────────────────────────────────────────┐
│                     Infrastructure Layer                         │
│  (RepositoryImpl, Mapper, EventPublisher, Cache)                 │
│  职责：数据持久化、外部服务集成、技术实现                            │
└─────────────────────────────────────────────────────────────────┘
```

### 3.2 依赖规则

- **Domain层**：不依赖任何外层，只定义Repository接口
- **Infrastructure层**：实现Domain层的接口
- **Application层**：编排Domain对象，通过Repository获取聚合
- **Interfaces层**：负责HTTP请求处理，调用Application层

### 3.3 与传统架构对比

| 传统分层 | DDD分层 | 变化 |
|---------|--------|------|
| Controller | Interfaces | 增加DTO/VO转换职责 |
| Service | Application + Domain | 拆分为应用服务（编排）和领域服务（业务逻辑） |
| Entity | Aggregate + Entity + ValueObject | 增加领域行为，不再是贫血模型 |
| Mapper | Repository + Infrastructure Mapper | Repository封装，领域层只看接口 |

---

## 4. 领域模型设计

### 4.1 Article聚合（Content Context）

```java
public class Article extends AggregateRoot {
    private ArticleId id;
    private ArticleTitle title;
    private ArticleContent content;
    private ArticleSummary summary;
    private CoverImage coverImage;
    private CategoryId categoryId;           // 外部聚合ID引用
    private AuthorId authorId;               // 外部聚合ID引用
    private ViewCount viewCount;
    private LikeCount likeCount;
    private CommentCount commentCount;
    private ArticleStatus status;            // DRAFT / PUBLISHED / TRASH
    private TopFlag isTop;
    private PublishTime publishTime;
    private AuditInfo auditInfo;
    
    // 领域行为
    public void publish() { ... }
    public void moveToTrash() { ... }
    public void toggleTop() { ... }
    public void incrementViewCount() { ... }
    public void updateContent(...) { ... }
}
```

**值对象定义**：
- `ArticleId`：文章ID
- `ArticleTitle`：文章标题，包含校验规则
- `ArticleContent`：文章内容
- `ArticleStatus`：状态枚举（DRAFT/PUBLISHED/TRASH）
- `ViewCount`、`LikeCount`、`CommentCount`：计数器值对象

### 4.2 User聚合（User Context）

```java
public class User extends AggregateRoot {
    private UserId id;
    private Username username;
    private Password password;
    private Nickname nickname;
    private Email email;
    private Avatar avatar;
    private UserRole role;                   // ADMIN / VISITOR
    private UserStatus status;               // ENABLED / DISABLED
    private FollowerCount followerCount;
    private FollowingCount followingCount;
    private LastLoginTime lastLoginTime;
    private AuditInfo auditInfo;
    
    // 领域行为
    public static User register(...) { ... }
    public void updateProfile(...) { ... }
    public void changePassword(...) { ... }
    public void disable() { ... }
    public void assignRole(UserRole role) { ... }
}
```

### 4.3 Comment聚合（Interaction Context）

```java
public class Comment extends AggregateRoot {
    private CommentId id;
    private ArticleId articleId;
    private Commenter commenter;             // 登录用户或游客
    private CommentContent content;
    private ParentCommentId parentId;
    private ReplyToCommentId replyId;
    private CommentStatus status;            // PENDING / APPROVED / REJECTED
    private IpAddress ipAddress;
    private AuditInfo auditInfo;
    
    // 领域行为
    public void approve() { ... }
    public void reject() { ... }
    public boolean isTopLevel() { ... }
}
```

### 4.4 PrivateMessage聚合（Interaction Context）

```java
public class PrivateMessage extends AggregateRoot {
    private ConversationId id;
    private UserId user1Id;                  // 参与者1（ID较小者）
    private UserId user2Id;                  // 参与者2（ID较大者）
    private List<Message> messages;          // 内部实体列表
    private LastMessageInfo lastMessage;
    private AuditInfo auditInfo;
    
    // 领域行为
    public void sendMessage(UserId senderId, MessageContent content) { ... }
    public void markAsRead(UserId readerId) { ... }
    public int getUnreadCount(UserId userId) { ... }
}
```

---

## 5. 领域事件设计

### 5.1 事件定义

```java
// 基础领域事件接口
public interface DomainEvent {
    LocalDateTime occurredAt();
}

// 聚合事件基类
public abstract class AggregateEvent<T> implements DomainEvent {
    private final T aggregateId;
    private final LocalDateTime occurredAt;
}
```

### 5.2 各上下文事件

#### Content Context事件

| 事件名 | 触发时机 | 订阅者 |
|--------|---------|--------|
| `ArticlePublishedEvent` | 文章发布时 | User Context（通知粉丝） |
| `ArticleDeletedEvent` | 文章删除时 | - |

#### User Context事件

| 事件名 | 触发时机 | 订阅者 |
|--------|---------|--------|
| `UserRegisteredEvent` | 用户注册时 | - |
| `UserFollowedEvent` | 用户关注时 | User Context（更新计数）、Interaction Context（创建通知） |
| `UserUnfollowedEvent` | 取消关注时 | User Context（更新计数） |

#### Interaction Context事件

| 事件名 | 触发时机 | 订阅者 |
|--------|---------|--------|
| `CommentApprovedEvent` | 评论审核通过 | Content Context（更新评论数）、User Context（通知作者） |
| `ArticleFavoritedEvent` | 收藏文章时 | - |
| `PrivateMessageSentEvent` | 发送私信时 | User Context（创建通知） |

#### System Context事件

| 事件名 | 触发时机 | 订阅者 |
|--------|---------|--------|
| `AnnouncementPublishedEvent` | 公告发布时 | User Context（批量创建通知） |

### 5.3 事件发布机制

```java
// 使用Spring ApplicationEvent
@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {
    private final ApplicationEventPublisher publisher;
    
    @Override
    public void publish(DomainEvent event) {
        publisher.publishEvent(event);
    }
}

// 聚合根基类
public abstract class AggregateRoot {
    private final List<DomainEvent> domainEvents = new ArrayList<>();
    
    protected void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }
    
    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return events;
    }
}
```

### 5.4 跨上下文调用规则

| 调用方 | 被调用方 | 允许方式 | 说明 |
|--------|---------|---------|------|
| Content | User | 领域服务接口 | 查询用户信息用于文章展示 |
| Content | System | 领域服务接口 | 敏感词过滤 |
| Interaction | Content | 领域服务接口 | 评论时校验文章存在性 |
| Interaction | User | 领域服务接口 | 私信时校验用户存在性 |
| User | Interaction | 事件驱动 | 关注后创建通知 |
| System | User | 事件驱动 | 公告发布后批量通知 |

---

## 6. 仓储设计

### 6.1 仓储接口（Domain层）

```java
// 文章仓储接口
public interface ArticleRepository {
    void save(Article article);
    Optional<Article> findById(ArticleId id);
    void delete(ArticleId id);
    Page<Article> findByCriteria(ArticleQueryCriteria criteria, int page, int size);
    Page<Article> findByCategory(CategoryId categoryId, int page, int size);
    Page<Article> findByTag(TagId tagId, int page, int size);
    Page<Article> search(String keyword, int page, int size);
    List<Article> findHotArticles(int limit);
    List<Article> findTopArticles();
    List<Article> findAllPublished();
}
```

### 6.2 仓储实现（Infrastructure层）

```java
@Repository
public class ArticleRepositoryImpl implements ArticleRepository {
    private final ArticleMapper mapper;
    private final ArticlePOConverter converter;
    private final DomainEventPublisher eventPublisher;
    
    @Override
    public void save(Article article) {
        ArticlePO po = converter.toPO(article);
        if (po.getId() == null) {
            mapper.insert(po);
            article.setId(new ArticleId(po.getId()));
        } else {
            mapper.updateById(po);
        }
        article.pullDomainEvents().forEach(eventPublisher::publish);
    }
    
    @Override
    public Optional<Article> findById(ArticleId id) {
        ArticlePO po = mapper.selectById(id.getValue());
        return Optional.ofNullable(po).map(converter::toDomain);
    }
    // ...
}
```

### 6.3 PO与领域对象转换

```java
@Component
public class ArticlePOConverter {
    public ArticlePO toPO(Article article) { ... }
    public Article toDomain(ArticlePO po) { ... }
}
```

---

## 7. 应用服务设计

### 7.1 CQRS风格命令与查询

```java
// 命令
public record CreateArticleCommand(
    String title, String content, String summary,
    String coverImage, Long categoryId,
    List<Long> tagIds, Integer status
) implements Command {}

// 查询
public record PageArticleQuery(
    String title, Long categoryId, Integer status,
    int pageNum, int pageSize
) implements Query<Page<ArticleListVO>> {}
```

### 7.2 应用服务实现

```java
@Service
@Transactional
public class ArticleApplicationService {
    private final ArticleRepository articleRepository;
    private final ArticleDomainService domainService;
    private final DomainEventPublisher eventPublisher;
    
    public ArticleId createArticle(CreateArticleCommand command) {
        // 1. 校验分类存在
        // 2. 创建文章聚合根
        // 3. 保存文章
        // 4. 设置标签
        // 5. 发布领域事件
    }
    
    public void publishArticle(PublishArticleCommand command) {
        Article article = articleRepository.findById(...)
        article.publish();
        articleRepository.save(article);
        eventPublisher.publishAll(article.pullDomainEvents());
    }
    
    @Transactional(readOnly = true)
    public Page<ArticleListVO> pageArticle(PageArticleQuery query) {
        // 查询并转换为VO
    }
}
```

---

## 8. 目录结构

```
com.blog
├── BlogApplication.java
│
├── shared/                              # 共享内核
│   ├── domain/
│   │   ├── AggregateRoot.java
│   │   ├── DomainEvent.java
│   │   └── DomainEventPublisher.java
│   ├── common/
│   │   ├── result/
│   │   └── exception/
│   └── infrastructure/
│       ├── event/
│       └── security/
│
├── content/                             # 内容上下文
│   ├── domain/
│   │   ├── model/
│   │   │   ├── article/
│   │   │   ├── category/
│   │   │   ├── tag/
│   │   │   └── series/
│   │   ├── repository/
│   │   ├── service/
│   │   └── event/
│   ├── application/
│   ├── interfaces/
│   │   ├── controller/
│   │   │   ├── admin/
│   │   │   └── portal/
│   │   ├── dto/
│   │   └── assembler/
│   └── infrastructure/
│       ├── persistence/
│       ├── mapper/
│       ├── po/
│       └── converter/
│
├── user/                                # 用户上下文
│   ├── domain/
│   ├── application/
│   ├── interfaces/
│   └── infrastructure/
│
├── interaction/                         # 互动上下文
│   ├── domain/
│   ├── application/
│   ├── interfaces/
│   └── infrastructure/
│
└── system/                              # 系统上下文
    ├── domain/
    ├── application/
    ├── interfaces/
    └── infrastructure/
```

---

## 9. 实施计划

### 9.1 阶段划分

| 阶段 | 内容 | 预估时间 |
|------|------|---------|
| Phase 1 | 基础设施准备 | 1-2天 |
| Phase 2 | Content Context重构 | 3-4天 |
| Phase 3 | User Context重构 | 2-3天 |
| Phase 4 | Interaction Context重构 | 2-3天 |
| Phase 5 | System Context重构 | 1-2天 |
| Phase 6 | 集成测试与清理 | 1-2天 |

### 9.2 Phase 1：基础设施准备

- 创建新包结构
- 迁移共享内核（AggregateRoot、DomainEvent、Result等）
- 建立事件发布机制
- 创建基础值对象类

### 9.3 Phase 2：Content Context重构

1. 定义Article聚合及其值对象
2. 实现ArticleRepository
3. 实现ArticleDomainService
4. 实现ArticleApplicationService
5. 适配Controller
6. 同样步骤重构Category、Tag、Series

### 9.4 Phase 3-5：其他上下文重构

按照Phase 2的模式，依次重构User、Interaction、System上下文。

### 9.5 风险与缓解

| 风险 | 缓解措施 |
|------|---------|
| 重构期间系统不可用 | 按上下文渐进重构，完成后立即测试 |
| 遗漏业务逻辑 | 先写测试用例覆盖现有Service逻辑 |
| 性能下降 | 批量查询优化，缓存策略保持不变 |
| 前端API变更 | 保持API路径和响应格式不变 |

---

## 10. 附录

### 10.1 值对象清单

| 上下文 | 值对象 | 说明 |
|--------|--------|------|
| Content | ArticleId, ArticleTitle, ArticleContent, ArticleSummary, ArticleStatus, ViewCount, LikeCount, CommentCount, TopFlag, PublishTime | 文章相关值对象 |
| Content | CategoryId, CategoryName | 分类值对象 |
| Content | TagId, TagName, TagColor | 标签值对象 |
| Content | SeriesId, SeriesName, SeriesMode | 系列值对象 |
| User | UserId, Username, Password, Nickname, Email, Avatar, UserRole, UserStatus | 用户值对象 |
| Interaction | CommentId, CommentContent, CommentStatus, Commenter | 评论值对象 |
| System | AnnouncementId, AnnouncementStatus | 公告值对象 |

### 10.2 领域服务清单

| 上下文 | 领域服务 | 职责 |
|--------|---------|------|
| Content | ArticleDomainService | 文章标签管理、分类校验 |
| User | UserDomainService | 用户认证、用户名/邮箱校验 |
| User | FollowDomainService | 关注关系管理 |
| Interaction | SensitiveWordFilterService | 敏感词过滤 |

### 10.3 仓储清单

| 上下文 | 仓储 | 聚合根 |
|--------|------|--------|
| Content | ArticleRepository | Article |
| Content | CategoryRepository | Category |
| Content | TagRepository | Tag |
| Content | SeriesRepository | Series |
| User | UserRepository | User |
| User | UserFollowRepository | UserFollow |
| User | NotificationRepository | Notification |
| Interaction | CommentRepository | Comment |
| Interaction | MessageRepository | Message |
| Interaction | PrivateMessageRepository | PrivateMessage |
| Interaction | FavoriteRepository | Favorite |
| Interaction | ReadingHistoryRepository | ReadingHistory |
| System | AnnouncementRepository | Announcement |
| System | SensitiveWordRepository | SensitiveWord |
| System | MediaRepository | Media |
| System | SysConfigRepository | SysConfig |
