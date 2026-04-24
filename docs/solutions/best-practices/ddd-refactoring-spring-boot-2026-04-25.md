---
title: DDD Refactoring Pattern for Spring Boot
date: 2026-04-25
category: best-practices
module: blog-server
problem_type: best_practice
component: service_object
severity: low
applies_when:
  - Refactoring from anemic domain models to rich domain models
  - Organizing complex business logic into bounded contexts
  - Improving testability and maintainability of service layers
tags:
  - ddd
  - domain-driven-design
  - spring-boot
  - architecture
  - refactoring
  - bounded-contexts
  - repository-pattern
---

# DDD Refactoring Pattern for Spring Boot

## Context

The blog system originally used a traditional three-layer architecture (controller/service/repository), which served well for simple CRUD operations but became increasingly difficult to maintain as the domain complexity grew. The codebase suffered from:

- **Anemic domain models**: Entities were mere data containers with no behavior
- **Scattered business logic**: Domain rules spread across service classes
- **Tight coupling**: Service layers directly depended on persistence implementations
- **Unclear boundaries**: No clear separation between different business domains

The refactoring to Domain-Driven Design (DDD) addressed these issues by organizing code around business domains with rich domain models and clear bounded contexts.

## Guidance

### Four Bounded Contexts

Organize the system into bounded contexts that align with business domains:

| Context | Responsibility |
|---------|---------------|
| **Content** | Articles, categories, tags, series |
| **User** | User management, authentication, follows, notifications |
| **Interaction** | Comments, favorites, messages, reading history |
| **System** | Announcements, configurations, sensitive words, media |

### Directory Structure Pattern

Each bounded context follows this four-layer structure:

```
com.blog.[context]/
├── domain/                          # Core business logic (no external dependencies)
│   ├── model/
│   │   ├── [aggregate]/            # Aggregate roots with behavior
│   │   └── [valueobject]/          # Immutable value objects (Java records)
│   ├── event/                       # Domain events
│   └── repository/                  # Repository interfaces only
├── infrastructure/                  # Technical implementation details
│   └── persistence/
│       ├── po/                      # Persistence Objects (DB mapping)
│       ├── mapper/                  # MyBatis Plus mappers
│       ├── converter/               # PO ↔ Domain object conversion
│       └── repository/              # Repository interface implementations
├── application/                     # Use case orchestration
│   └── [ServiceName]ApplicationService.java
└── interfaces/                      # External-facing layer
    ├── dto/                         # Request/Response DTOs
    ├── assembler/                   # Domain ↔ DTO conversion
    └── controller/
        ├── portal/                  # Public APIs
        └── admin/                   # Admin APIs
```

### Key Code Patterns

#### 1. Aggregate Root with Domain Behavior

```java
@Getter
public class Article {
    private ArticleId id;
    private ArticleTitle title;
    private ArticleContent content;
    private ArticleStatus status;

    // Factory method for creation
    public static Article create(String title, String content, Long categoryId) {
        Article article = new Article();
        article.title = ArticleTitle.of(title);
        article.content = ArticleContent.of(content);
        article.status = ArticleStatus.DRAFT;
        return article;
    }

    // Domain behavior - publish the article
    public void publish() {
        if (this.status == ArticleStatus.PUBLISHED) {
            throw new DomainException("Article is already published");
        }
        this.status = ArticleStatus.PUBLISHED;
    }

    // Required for repository access
    public void setId(ArticleId id) {
        this.id = id;
    }
}
```

#### 2. Value Objects Using Java Records

```java
public record ArticleId(Long value) {
    public ArticleId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Article ID must be positive");
        }
    }
}

public record ArticleTitle(String value) {
    private static final int MAX_LENGTH = 200;

    public ArticleTitle {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Title exceeds maximum length");
        }
    }
}
```

#### 3. Repository Pattern

```java
// Domain layer - interface only
public interface ArticleRepository {
    Article save(Article article);
    Optional<Article> findById(ArticleId id);
    void delete(Article article);
}

// Infrastructure layer - implementation
@Repository
public class ArticleRepositoryImpl implements ArticleRepository {
    private final ArticleMapper mapper;
    private final ArticleConverter converter;

    @Override
    public Article save(Article article) {
        ArticlePO po = converter.toPO(article);
        if (po.getId() == null) {
            mapper.insert(po);
            article.setId(ArticleId.of(po.getId()));
        } else {
            mapper.updateById(po);
        }
        return article;
    }
}
```

#### 4. Application Service

```java
@Service
public class ArticleApplicationService {
    private final ArticleRepository articleRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ArticleId createArticle(CreateArticleCommand command) {
        Article article = Article.create(
            command.title(),
            command.content(),
            command.categoryId()
        );
        Article saved = articleRepository.save(article);
        eventPublisher.publishEvent(new ArticleCreatedEvent(saved.getId()));
        return saved.getId();
    }
}
```

### Common Pitfalls

#### Pitfall 1: Boolean Accessor Naming

Lombok generates `isRead()` for boolean field `isRead`, not `getIsRead()`.

**Solution**: Rename the field to `read` or use explicit getter.

#### Pitfall 2: Domain Model ID Setter Visibility

Repository implementations need to set IDs on domain objects after persistence.

**Solution**: Make `setId()` public in the aggregate root.

#### Pitfall 3: Value Object Return Types in Controllers

Application services return value objects, but controllers need primitives for JSON.

```java
// Wrong - returns value object directly
return Result.success(commentId);  // Serializes as {"value": 123}

// Correct - extract primitive value
return Result.success(commentId.value());  // Serializes as 123
```

## Why This Matters

1. **Domain Logic Isolation**: Business rules live in domain layer, independent of frameworks
2. **Rich Domain Models**: Entities contain behavior: `article.publish()` is more expressive than `articleService.publishArticle(id)`
3. **Testability**: Domain layer can be tested without Spring, databases, or mocks
4. **Clear Boundaries**: Bounded contexts prevent domain logic from leaking
5. **Maintainability**: Code organized by business domain

## When to Apply

### Good Candidates for DDD

- Complex business domains with rules beyond simple CRUD
- Long-lived projects where architecture investment pays off
- Multiple bounded contexts with clear boundaries
- Team stability for shared understanding

### When Traditional Architecture Suffices

- Simple CRUD applications with minimal domain logic
- Prototype/MVP phase where speed matters
- Short-lived projects where investment won't pay back

### Migration Strategy

1. Start with one bounded context
2. Use git worktrees for isolated development
3. Use subagent-driven development for parallel task execution
4. Keep both architectures running during transition

## Examples

### Complete Bounded Context Structure

```
com.blog.interaction/
├── domain/
│   ├── model/
│   │   ├── comment/Comment, CommentId, Commenter
│   │   ├── favorite/Favorite, FavoriteId
│   │   └── message/Message, MessageId
│   └── repository/
│       ├── CommentRepository.java
│       └── FavoriteRepository.java
├── infrastructure/persistence/
│   ├── po/CommentPO, FavoritePO
│   ├── mapper/CommentMapper, FavoriteMapper
│   ├── converter/CommentConverter
│   └── repository/CommentRepositoryImpl
├── application/
│   └── CommentApplicationService.java
└── interfaces/
    ├── dto/CommentDTO, CreateCommentRequest
    ├── assembler/InteractionAssembler
    └── controller/
        ├── portal/CommentController.java
        └── admin/CommentManageController.java
```

### Domain Event Pattern

```java
// Domain event
public record ArticleCreatedEvent(ArticleId articleId, LocalDateTime occurredAt) {}

// Publish from application service
eventPublisher.publishEvent(new ArticleCreatedEvent(saved.getId()));

// Cross-context listener
@EventListener
public void handleArticleCreated(ArticleCreatedEvent event) {
    // Notify followers - lives in Interaction context
}
```

## Related

- DDD Design Spec: `docs/superpowers/specs/2024-04-24-ddd-refactor-design.md`
- Implementation Plan: `docs/superpowers/plans/2024-04-24-ddd-refactor-plan.md`
