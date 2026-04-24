# 博客系统DDD重构实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将博客系统从传统三层架构重构为DDD架构，按限界上下文组织代码，引入聚合根、值对象、领域服务和仓储模式。

**Architecture:** 四层架构（Interfaces → Application → Domain → Infrastructure），四个限界上下文（Content、User、Interaction、System），使用Spring ApplicationEvent实现领域事件。

**Tech Stack:** Spring Boot 3.2, MyBatis Plus 3.5, Java 17, Spring ApplicationEvent

---

## Phase 1: 基础设施准备

### Task 1: 创建共享内核基础类

**Files:**
- Create: `blog-server/src/main/java/com/blog/shared/domain/AggregateRoot.java`
- Create: `blog-server/src/main/java/com/blog/shared/domain/DomainEvent.java`
- Create: `blog-server/src/main/java/com/blog/shared/domain/DomainEventPublisher.java`

- [ ] **Step 1: 创建DomainEvent接口**

```java
package com.blog.shared.domain;

import java.time.LocalDateTime;

/**
 * 领域事件基础接口
 */
public interface DomainEvent {
    /**
     * 事件发生时间
     */
    LocalDateTime occurredAt();
}
```

- [ ] **Step 2: 创建AggregateRoot抽象类**

```java
package com.blog.shared.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * 聚合根基类，提供领域事件收集能力
 */
public abstract class AggregateRoot {
    
    private final List<DomainEvent> domainEvents = new ArrayList<>();
    
    /**
     * 注册领域事件
     */
    protected void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }
    
    /**
     * 提取并清空领域事件
     */
    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return events;
    }
    
    /**
     * 清空领域事件
     */
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}
```

- [ ] **Step 3: 创建DomainEventPublisher接口**

```java
package com.blog.shared.domain;

import java.util.List;

/**
 * 领域事件发布器接口
 */
public interface DomainEventPublisher {
    
    /**
     * 发布单个事件
     */
    void publish(DomainEvent event);
    
    /**
     * 批量发布事件
     */
    default void publishAll(List<DomainEvent> events) {
        events.forEach(this::publish);
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add blog-server/src/main/java/com/blog/shared/domain/
git commit -m "feat(shared): add domain base classes (AggregateRoot, DomainEvent, DomainEventPublisher)"
```

---

### Task 2: 实现Spring事件发布器

**Files:**
- Create: `blog-server/src/main/java/com/blog/shared/infrastructure/event/SpringDomainEventPublisher.java`

- [ ] **Step 1: 创建SpringDomainEventPublisher实现**

```java
package com.blog.shared.infrastructure.event;

import com.blog.shared.domain.DomainEvent;
import com.blog.shared.domain.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 基于Spring ApplicationEvent的领域事件发布器
 */
@Component
@RequiredArgsConstructor
public class SpringDomainEventPublisher implements DomainEventPublisher {
    
    private final ApplicationEventPublisher applicationEventPublisher;
    
    @Override
    public void publish(DomainEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add blog-server/src/main/java/com/blog/shared/infrastructure/event/
git commit -m "feat(shared): add SpringDomainEventPublisher implementation"
```

---

### Task 3: 创建通用值对象

**Files:**
- Create: `blog-server/src/main/java/com/blog/shared/domain/valueobject/AuditInfo.java`

- [ ] **Step 1: 创建AuditInfo值对象**

```java
package com.blog.shared.domain.valueobject;

import java.time.LocalDateTime;

/**
 * 审计信息值对象
 */
public record AuditInfo(
    LocalDateTime createTime,
    LocalDateTime updateTime
) {
    public static AuditInfo create() {
        LocalDateTime now = LocalDateTime.now();
        return new AuditInfo(now, now);
    }
    
    public AuditInfo update() {
        return new AuditInfo(this.createTime, LocalDateTime.now());
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add blog-server/src/main/java/com/blog/shared/domain/valueobject/
git commit -m "feat(shared): add AuditInfo value object"
```

---

### Task 4: 迁移通用工具类

**Files:**
- Move: `blog-server/src/main/java/com/blog/common/` → `blog-server/src/main/java/com/blog/shared/common/`

- [ ] **Step 1: 创建shared/common目录结构**

```bash
mkdir -p blog-server/src/main/java/com/blog/shared/common/result
mkdir -p blog-server/src/main/java/com/blog/shared/common/exception
mkdir -p blog-server/src/main/java/com/blog/shared/common/utils
```

- [ ] **Step 2: 复制Result类到新位置**

读取现有的Result类，创建副本到shared包（保持原有类不变，后续逐步迁移）：

```bash
cp blog-server/src/main/java/com/blog/common/result/Result.java \
   blog-server/src/main/java/com/blog/shared/common/result/Result.java
```

- [ ] **Step 3: 更新包名**

编辑新文件，将package从`com.blog.common.result`改为`com.blog.shared.common.result`

- [ ] **Step 4: Commit**

```bash
git add blog-server/src/main/java/com/blog/shared/common/
git commit -m "feat(shared): copy common utilities to shared package"
```

---

## Phase 2: Content Context 重构

### Task 5: 创建Article聚合值对象

**Files:**
- Create: `blog-server/src/main/java/com/blog/content/domain/model/article/ArticleId.java`
- Create: `blog-server/src/main/java/com/blog/content/domain/model/article/ArticleTitle.java`
- Create: `blog-server/src/main/java/com/blog/content/domain/model/article/ArticleContent.java`
- Create: `blog-server/src/main/java/com/blog/content/domain/model/article/ArticleSummary.java`
- Create: `blog-server/src/main/java/com/blog/content/domain/model/article/ArticleStatus.java`
- Create: `blog-server/src/main/java/com/blog/content/domain/model/article/ViewCount.java`
- Create: `blog-server/src/main/java/com/blog/content/domain/model/article/LikeCount.java`
- Create: `blog-server/src/main/java/com/blog/content/domain/model/article/CommentCount.java`
- Create: `blog-server/src/main/java/com/blog/content/domain/model/article/TopFlag.java`
- Create: `blog-server/src/main/java/com/blog/content/domain/model/article/PublishTime.java`
- Create: `blog-server/src/main/java/com/blog/content/domain/model/article/CoverImage.java`
- Create: `blog-server/src/main/java/com/blog/content/domain/model/article/AuthorId.java`

- [ ] **Step 1: 创建ArticleId值对象**

```java
package com.blog.content.domain.model.article;

/**
 * 文章ID值对象
 */
public record ArticleId(Long value) {
    public ArticleId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("文章ID必须为正数");
        }
    }
}
```

- [ ] **Step 2: 创建ArticleTitle值对象**

```java
package com.blog.content.domain.model.article;

/**
 * 文章标题值对象
 */
public record ArticleTitle(String value) {
    private static final int MAX_LENGTH = 200;
    
    public ArticleTitle {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("文章标题不能为空");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("文章标题不能超过" + MAX_LENGTH + "字符");
        }
    }
}
```

- [ ] **Step 3: 创建ArticleContent值对象**

```java
package com.blog.content.domain.model.article;

/**
 * 文章内容值对象
 */
public record ArticleContent(String value) {
    public ArticleContent {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("文章内容不能为空");
        }
    }
}
```

- [ ] **Step 4: 创建ArticleSummary值对象**

```java
package com.blog.content.domain.model.article;

/**
 * 文章摘要值对象
 */
public record ArticleSummary(String value) {
    private static final int MAX_LENGTH = 500;
    
    public ArticleSummary {
        if (value != null && value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("文章摘要不能超过" + MAX_LENGTH + "字符");
        }
    }
    
    public static ArticleSummary empty() {
        return new ArticleSummary(null);
    }
}
```

- [ ] **Step 5: 创建ArticleStatus枚举**

```java
package com.blog.content.domain.model.article;

/**
 * 文章状态枚举
 */
public enum ArticleStatus {
    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    TRASH(2, "回收站");
    
    private final int code;
    private final String description;
    
    ArticleStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static ArticleStatus fromCode(int code) {
        for (ArticleStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的文章状态码: " + code);
    }
}
```

- [ ] **Step 6: 创建ViewCount值对象**

```java
package com.blog.content.domain.model.article;

/**
 * 浏览量值对象
 */
public record ViewCount(Long value) {
    public ViewCount {
        if (value == null || value < 0) {
            throw new IllegalArgumentException("浏览量不能为负数");
        }
    }
    
    public static ViewCount zero() {
        return new ViewCount(0L);
    }
    
    public ViewCount increment() {
        return new ViewCount(this.value + 1);
    }
}
```

- [ ] **Step 7: 创建LikeCount值对象**

```java
package com.blog.content.domain.model.article;

/**
 * 点赞数值对象
 */
public record LikeCount(Long value) {
    public LikeCount {
        if (value == null || value < 0) {
            throw new IllegalArgumentException("点赞数不能为负数");
        }
    }
    
    public static LikeCount zero() {
        return new LikeCount(0L);
    }
    
    public LikeCount increment() {
        return new LikeCount(this.value + 1);
    }
    
    public LikeCount decrement() {
        return new LikeCount(Math.max(0, this.value - 1));
    }
}
```

- [ ] **Step 8: 创建CommentCount值对象**

```java
package com.blog.content.domain.model.article;

/**
 * 评论数值对象
 */
public record CommentCount(Integer value) {
    public CommentCount {
        if (value == null || value < 0) {
            throw new IllegalArgumentException("评论数不能为负数");
        }
    }
    
    public static CommentCount zero() {
        return new CommentCount(0);
    }
    
    public CommentCount increment() {
        return new CommentCount(this.value + 1);
    }
    
    public CommentCount decrement() {
        return new CommentCount(Math.max(0, this.value - 1));
    }
}
```

- [ ] **Step 9: 创建TopFlag值对象**

```java
package com.blog.content.domain.model.article;

/**
 * 置顶标记值对象
 */
public record TopFlag(boolean isTop) {
    
    public static TopFlag notTop() {
        return new TopFlag(false);
    }
    
    public static TopFlag top() {
        return new TopFlag(true);
    }
    
    public static TopFlag of(int value) {
        return new TopFlag(value == 1);
    }
    
    public int toInt() {
        return isTop ? 1 : 0;
    }
    
    public TopFlag toggle() {
        return new TopFlag(!this.isTop);
    }
}
```

- [ ] **Step 10: 创建PublishTime值对象**

```java
package com.blog.content.domain.model.article;

import java.time.LocalDateTime;

/**
 * 发布时间值对象
 */
public record PublishTime(LocalDateTime value) {
    
    public static PublishTime now() {
        return new PublishTime(LocalDateTime.now());
    }
    
    public static PublishTime of(LocalDateTime value) {
        return new PublishTime(value);
    }
}
```

- [ ] **Step 11: 创建CoverImage值对象**

```java
package com.blog.content.domain.model.article;

/**
 * 封面图片值对象
 */
public record CoverImage(String url) {
    
    public static CoverImage empty() {
        return new CoverImage(null);
    }
    
    public static CoverImage of(String url) {
        return new CoverImage(url);
    }
    
    public boolean hasImage() {
        return url != null && !url.isBlank();
    }
}
```

- [ ] **Step 12: 创建AuthorId值对象**

```java
package com.blog.content.domain.model.article;

/**
 * 作者ID值对象
 */
public record AuthorId(Long value) {
    public AuthorId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("作者ID必须为正数");
        }
    }
}
```

- [ ] **Step 13: Commit**

```bash
git add blog-server/src/main/java/com/blog/content/domain/model/article/
git commit -m "feat(content): add Article value objects"
```

---

### Task 6: 创建Category和Tag值对象

**Files:**
- Create: `blog-server/src/main/java/com/blog/content/domain/model/category/CategoryId.java`
- Create: `blog-server/src/main/java/com/blog/content/domain/model/category/CategoryName.java`
- Create: `blog-server/src/main/java/com/blog/content/domain/model/tag/TagId.java`
- Create: `blog-server/src/main/java/com/blog/content/domain/model/tag/TagName.java`
- Create: `blog-server/src/main/java/com/blog/content/domain/model/tag/TagColor.java`

- [ ] **Step 1: 创建CategoryId值对象**

```java
package com.blog.content.domain.model.category;

/**
 * 分类ID值对象
 */
public record CategoryId(Long value) {
    public CategoryId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("分类ID必须为正数");
        }
    }
}
```

- [ ] **Step 2: 创建CategoryName值对象**

```java
package com.blog.content.domain.model.category;

/**
 * 分类名称值对象
 */
public record CategoryName(String value) {
    private static final int MAX_LENGTH = 50;
    
    public CategoryName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("分类名称不能为空");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("分类名称不能超过" + MAX_LENGTH + "字符");
        }
    }
}
```

- [ ] **Step 3: 创建TagId值对象**

```java
package com.blog.content.domain.model.tag;

/**
 * 标签ID值对象
 */
public record TagId(Long value) {
    public TagId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("标签ID必须为正数");
        }
    }
}
```

- [ ] **Step 4: 创建TagName值对象**

```java
package com.blog.content.domain.model.tag;

/**
 * 标签名称值对象
 */
public record TagName(String value) {
    private static final int MAX_LENGTH = 50;
    
    public TagName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("标签名称不能为空");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("标签名称不能超过" + MAX_LENGTH + "字符");
        }
    }
}
```

- [ ] **Step 5: 创建TagColor值对象**

```java
package com.blog.content.domain.model.tag;

/**
 * 标签颜色值对象
 */
public record TagColor(String value) {
    private static final String DEFAULT_COLOR = "#409EFF";
    
    public TagColor {
        if (value == null || value.isBlank()) {
            value = DEFAULT_COLOR;
        }
    }
    
    public static TagColor defaultColor() {
        return new TagColor(DEFAULT_COLOR);
    }
}
```

- [ ] **Step 6: Commit**

```bash
git add blog-server/src/main/java/com/blog/content/domain/model/category/
git add blog-server/src/main/java/com/blog/content/domain/model/tag/
git commit -m "feat(content): add Category and Tag value objects"
```

---

### Task 7: 创建Article聚合根

**Files:**
- Create: `blog-server/src/main/java/com/blog/content/domain/model/article/Article.java`

- [ ] **Step 1: 创建Article聚合根**

```java
package com.blog.content.domain.model.article;

import com.blog.content.domain.model.category.CategoryId;
import com.blog.shared.domain.AggregateRoot;
import com.blog.shared.domain.valueobject.AuditInfo;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 文章聚合根
 */
@Getter
public class Article extends AggregateRoot {
    
    private ArticleId id;
    private ArticleTitle title;
    private ArticleContent content;
    private ArticleSummary summary;
    private CoverImage coverImage;
    private CategoryId categoryId;
    private AuthorId authorId;
    private ViewCount viewCount;
    private LikeCount likeCount;
    private CommentCount commentCount;
    private ArticleStatus status;
    private TopFlag isTop;
    private PublishTime publishTime;
    private AuditInfo auditInfo;
    
    // 私有构造器
    private Article() {}
    
    /**
     * 创建新文章
     */
    public static Article create(ArticleTitle title, ArticleContent content,
                                 ArticleSummary summary, CoverImage coverImage,
                                 CategoryId categoryId, AuthorId authorId) {
        Article article = new Article();
        article.title = title;
        article.content = content;
        article.summary = summary;
        article.coverImage = coverImage;
        article.categoryId = categoryId;
        article.authorId = authorId;
        article.viewCount = ViewCount.zero();
        article.likeCount = LikeCount.zero();
        article.commentCount = CommentCount.zero();
        article.isTop = TopFlag.notTop();
        article.status = ArticleStatus.DRAFT;
        article.auditInfo = AuditInfo.create();
        return article;
    }
    
    /**
     * 从持久化层重建聚合
     */
    public static Article reconstruct(ArticleId id, ArticleTitle title,
                                      ArticleContent content, ArticleSummary summary,
                                      CoverImage coverImage, CategoryId categoryId,
                                      AuthorId authorId, ViewCount viewCount,
                                      LikeCount likeCount, CommentCount commentCount,
                                      TopFlag isTop, ArticleStatus status,
                                      PublishTime publishTime, AuditInfo auditInfo) {
        Article article = new Article();
        article.id = id;
        article.title = title;
        article.content = content;
        article.summary = summary;
        article.coverImage = coverImage;
        article.categoryId = categoryId;
        article.authorId = authorId;
        article.viewCount = viewCount;
        article.likeCount = likeCount;
        article.commentCount = commentCount;
        article.isTop = isTop;
        article.status = status;
        article.publishTime = publishTime;
        article.auditInfo = auditInfo;
        return article;
    }
    
    /**
     * 发布文章
     */
    public void publish() {
        if (this.status == ArticleStatus.PUBLISHED) {
            throw new IllegalStateException("文章已发布");
        }
        if (this.status == ArticleStatus.TRASH) {
            throw new IllegalStateException("回收站中的文章不能发布");
        }
        this.status = ArticleStatus.PUBLISHED;
        this.publishTime = PublishTime.now();
        this.registerEvent(new ArticlePublishedEvent(this.id, this.title, this.authorId));
    }
    
    /**
     * 移入回收站
     */
    public void moveToTrash() {
        if (this.status == ArticleStatus.TRASH) {
            throw new IllegalStateException("文章已在回收站中");
        }
        this.status = ArticleStatus.TRASH;
    }
    
    /**
     * 恢复文章
     */
    public void restore() {
        if (this.status != ArticleStatus.TRASH) {
            throw new IllegalStateException("只有回收站中的文章可以恢复");
        }
        this.status = ArticleStatus.DRAFT;
    }
    
    /**
     * 置顶/取消置顶
     */
    public void toggleTop() {
        this.isTop = this.isTop.toggle();
    }
    
    /**
     * 增加浏览量
     */
    public void incrementViewCount() {
        this.viewCount = this.viewCount.increment();
    }
    
    /**
     * 增加点赞数
     */
    public void incrementLikeCount() {
        this.likeCount = this.likeCount.increment();
    }
    
    /**
     * 减少点赞数
     */
    public void decrementLikeCount() {
        this.likeCount = this.likeCount.decrement();
    }
    
    /**
     * 增加评论数
     */
    public void incrementCommentCount() {
        this.commentCount = this.commentCount.increment();
    }
    
    /**
     * 减少评论数
     */
    public void decrementCommentCount() {
        this.commentCount = this.commentCount.decrement();
    }
    
    /**
     * 更新内容
     */
    public void updateContent(ArticleTitle title, ArticleContent content,
                              ArticleSummary summary, CoverImage coverImage,
                              CategoryId categoryId) {
        this.title = title;
        this.content = content;
        this.summary = summary;
        this.coverImage = coverImage;
        this.categoryId = categoryId;
        this.auditInfo = this.auditInfo.update();
    }
    
    /**
     * 设置ID（仅用于新建后设置）
     */
    void setId(ArticleId id) {
        this.id = id;
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add blog-server/src/main/java/com/blog/content/domain/model/article/Article.java
git commit -m "feat(content): add Article aggregate root with domain behaviors"
```

---

### Task 8: 创建Article领域事件

**Files:**
- Create: `blog-server/src/main/java/com/blog/content/domain/event/ArticlePublishedEvent.java`
- Create: `blog-server/src/main/java/com/blog/content/domain/event/ArticleDeletedEvent.java`

- [ ] **Step 1: 创建ArticlePublishedEvent**

```java
package com.blog.content.domain.event;

import com.blog.content.domain.model.article.ArticleId;
import com.blog.content.domain.model.article.ArticleTitle;
import com.blog.content.domain.model.article.AuthorId;
import com.blog.shared.domain.DomainEvent;

import java.time.LocalDateTime;

/**
 * 文章发布事件
 */
public record ArticlePublishedEvent(
    ArticleId articleId,
    ArticleTitle title,
    AuthorId authorId,
    LocalDateTime occurredAt
) implements DomainEvent {
    
    public ArticlePublishedEvent(ArticleId articleId, ArticleTitle title, AuthorId authorId) {
        this(articleId, title, authorId, LocalDateTime.now());
    }
    
    @Override
    public LocalDateTime occurredAt() {
        return occurredAt;
    }
}
```

- [ ] **Step 2: 创建ArticleDeletedEvent**

```java
package com.blog.content.domain.event;

import com.blog.content.domain.model.article.ArticleId;
import com.blog.shared.domain.DomainEvent;

import java.time.LocalDateTime;

/**
 * 文章删除事件
 */
public record ArticleDeletedEvent(
    ArticleId articleId,
    LocalDateTime occurredAt
) implements DomainEvent {
    
    public ArticleDeletedEvent(ArticleId articleId) {
        this(articleId, LocalDateTime.now());
    }
    
    @Override
    public LocalDateTime occurredAt() {
        return occurredAt;
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add blog-server/src/main/java/com/blog/content/domain/event/
git commit -m "feat(content): add Article domain events"
```

---

### Task 9: 创建Category聚合根

**Files:**
- Create: `blog-server/src/main/java/com/blog/content/domain/model/category/Category.java`
- Create: `blog-server/src/main/java/com/blog/content/domain/model/category/CategoryDescription.java`
- Create: `blog-server/src/main/java/com/blog/content/domain/model/category/CategorySort.java`
- Create: `blog-server/src/main/java/com/blog/content/domain/model/category/CategoryStatus.java`

- [ ] **Step 1: 创建CategoryDescription值对象**

```java
package com.blog.content.domain.model.category;

/**
 * 分类描述值对象
 */
public record CategoryDescription(String value) {
    private static final int MAX_LENGTH = 200;
    
    public CategoryDescription {
        if (value != null && value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("分类描述不能超过" + MAX_LENGTH + "字符");
        }
    }
    
    public static CategoryDescription empty() {
        return new CategoryDescription(null);
    }
}
```

- [ ] **Step 2: 创建CategorySort值对象**

```java
package com.blog.content.domain.model.category;

/**
 * 分类排序值对象
 */
public record CategorySort(int value) {
    public static CategorySort of(int value) {
        return new CategorySort(value);
    }
    
    public static CategorySort defaultSort() {
        return new CategorySort(0);
    }
}
```

- [ ] **Step 3: 创建CategoryStatus枚举**

```java
package com.blog.content.domain.model.category;

/**
 * 分类状态枚举
 */
public enum CategoryStatus {
    DISABLED(0, "禁用"),
    ENABLED(1, "启用");
    
    private final int code;
    private final String description;
    
    CategoryStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public int getCode() {
        return code;
    }
    
    public static CategoryStatus fromCode(int code) {
        for (CategoryStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的分类状态码: " + code);
    }
}
```

- [ ] **Step 4: 创建Category聚合根**

```java
package com.blog.content.domain.model.category;

import com.blog.shared.domain.AggregateRoot;
import com.blog.shared.domain.valueobject.AuditInfo;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 分类聚合根
 */
@Getter
public class Category extends AggregateRoot {
    
    private CategoryId id;
    private CategoryName name;
    private CategoryDescription description;
    private CategorySort sort;
    private CategoryStatus status;
    private AuditInfo auditInfo;
    
    private Category() {}
    
    public static Category create(CategoryName name, CategoryDescription description, CategorySort sort) {
        Category category = new Category();
        category.name = name;
        category.description = description;
        category.sort = sort;
        category.status = CategoryStatus.ENABLED;
        category.auditInfo = AuditInfo.create();
        return category;
    }
    
    public static Category reconstruct(CategoryId id, CategoryName name,
                                       CategoryDescription description, CategorySort sort,
                                       CategoryStatus status, AuditInfo auditInfo) {
        Category category = new Category();
        category.id = id;
        category.name = name;
        category.description = description;
        category.sort = sort;
        category.status = status;
        category.auditInfo = auditInfo;
        return category;
    }
    
    public void update(CategoryName name, CategoryDescription description, CategorySort sort) {
        this.name = name;
        this.description = description;
        this.sort = sort;
        this.auditInfo = this.auditInfo.update();
    }
    
    public void disable() {
        this.status = CategoryStatus.DISABLED;
    }
    
    public void enable() {
        this.status = CategoryStatus.ENABLED;
    }
    
    void setId(CategoryId id) {
        this.id = id;
    }
}
```

- [ ] **Step 5: Commit**

```bash
git add blog-server/src/main/java/com/blog/content/domain/model/category/
git commit -m "feat(content): add Category aggregate root"
```

---

### Task 10: 创建Tag聚合根

**Files:**
- Create: `blog-server/src/main/java/com/blog/content/domain/model/tag/Tag.java`

- [ ] **Step 1: 创建Tag聚合根**

```java
package com.blog.content.domain.model.tag;

import com.blog.shared.domain.AggregateRoot;
import com.blog.shared.domain.valueobject.AuditInfo;
import lombok.Getter;

/**
 * 标签聚合根
 */
@Getter
public class Tag extends AggregateRoot {
    
    private TagId id;
    private TagName name;
    private TagColor color;
    private AuditInfo auditInfo;
    
    private Tag() {}
    
    public static Tag create(TagName name, TagColor color) {
        Tag tag = new Tag();
        tag.name = name;
        tag.color = color;
        tag.auditInfo = AuditInfo.create();
        return tag;
    }
    
    public static Tag create(TagName name) {
        return create(name, TagColor.defaultColor());
    }
    
    public static Tag reconstruct(TagId id, TagName name, TagColor color, AuditInfo auditInfo) {
        Tag tag = new Tag();
        tag.id = id;
        tag.name = name;
        tag.color = color;
        tag.auditInfo = auditInfo;
        return tag;
    }
    
    public void update(TagName name, TagColor color) {
        this.name = name;
        this.color = color;
        this.auditInfo = this.auditInfo.update();
    }
    
    void setId(TagId id) {
        this.id = id;
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add blog-server/src/main/java/com/blog/content/domain/model/tag/Tag.java
git commit -m "feat(content): add Tag aggregate root"
```

---

### Task 11: 创建仓储接口

**Files:**
- Create: `blog-server/src/main/java/com/blog/content/domain/repository/ArticleRepository.java`
- Create: `blog-server/src/main/java/com/blog/content/domain/repository/CategoryRepository.java`
- Create: `blog-server/src/main/java/com/blog/content/domain/repository/TagRepository.java`
- Create: `blog-server/src/main/java/com/blog/content/domain/repository/ArticleQueryCriteria.java`

- [ ] **Step 1: 创建ArticleQueryCriteria**

```java
package com.blog.content.domain.repository;

import com.blog.content.domain.model.article.ArticleStatus;
import com.blog.content.domain.model.category.CategoryId;
import lombok.Builder;
import lombok.Getter;

/**
 * 文章查询条件
 */
@Getter
@Builder
public class ArticleQueryCriteria {
    private String title;
    private CategoryId categoryId;
    private ArticleStatus status;
}
```

- [ ] **Step 2: 创建ArticleRepository接口**

```java
package com.blog.content.domain.repository;

import com.blog.content.domain.model.article.Article;
import com.blog.content.domain.model.article.ArticleId;
import com.blog.content.domain.model.category.CategoryId;
import com.blog.content.domain.model.tag.TagId;

import java.util.List;
import java.util.Optional;

/**
 * 文章仓储接口
 */
public interface ArticleRepository {
    
    void save(Article article);
    
    Optional<Article> findById(ArticleId id);
    
    void delete(ArticleId id);
    
    List<Article> findAll();
    
    List<Article> findByCriteria(ArticleQueryCriteria criteria);
    
    List<Article> findByCategory(CategoryId categoryId);
    
    List<Article> findByTag(TagId tagId);
    
    List<Article> search(String keyword);
    
    List<Article> findHotArticles(int limit);
    
    List<Article> findTopArticles();
    
    List<Article> findPublished();
    
    boolean existsById(ArticleId id);
}
```

- [ ] **Step 3: 创建CategoryRepository接口**

```java
package com.blog.content.domain.repository;

import com.blog.content.domain.model.category.Category;
import com.blog.content.domain.model.category.CategoryId;

import java.util.List;
import java.util.Optional;

/**
 * 分类仓储接口
 */
public interface CategoryRepository {
    
    void save(Category category);
    
    Optional<Category> findById(CategoryId id);
    
    List<Category> findAll();
    
    void delete(CategoryId id);
    
    boolean existsById(CategoryId id);
    
    boolean existsByName(String name);
}
```

- [ ] **Step 4: 创建TagRepository接口**

```java
package com.blog.content.domain.repository;

import com.blog.content.domain.model.tag.Tag;
import com.blog.content.domain.model.tag.TagId;

import java.util.List;
import java.util.Optional;

/**
 * 标签仓储接口
 */
public interface TagRepository {
    
    void save(Tag tag);
    
    Optional<Tag> findById(TagId id);
    
    List<Tag> findAll();
    
    void delete(TagId id);
    
    boolean existsByName(String name);
}
```

- [ ] **Step 5: Commit**

```bash
git add blog-server/src/main/java/com/blog/content/domain/repository/
git commit -m "feat(content): add repository interfaces"
```

---

### Task 12: 创建持久化对象（PO）

**Files:**
- Create: `blog-server/src/main/java/com/blog/content/infrastructure/po/ArticlePO.java`
- Create: `blog-server/src/main/java/com/blog/content/infrastructure/po/CategoryPO.java`
- Create: `blog-server/src/main/java/com/blog/content/infrastructure/po/TagPO.java`

- [ ] **Step 1: 创建ArticlePO**

```java
package com.blog.content.infrastructure.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章持久化对象
 */
@Data
@TableName("article")
public class ArticlePO {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    
    private String summary;
    
    private String content;
    
    private String coverImage;
    
    private Long categoryId;
    
    private Long authorId;
    
    private Long viewCount;
    
    private Long likeCount;
    
    private Integer commentCount;
    
    private Integer isTop;
    
    private Integer status;
    
    private LocalDateTime publishTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
```

- [ ] **Step 2: 创建CategoryPO**

```java
package com.blog.content.infrastructure.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分类持久化对象
 */
@Data
@TableName("category")
public class CategoryPO {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private String description;
    
    private Integer sort;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

- [ ] **Step 3: 创建TagPO**

```java
package com.blog.content.infrastructure.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签持久化对象
 */
@Data
@TableName("tag")
public class TagPO {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private String color;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

- [ ] **Step 4: Commit**

```bash
git add blog-server/src/main/java/com/blog/content/infrastructure/po/
git commit -m "feat(content): add persistence objects (PO)"
```

---

### Task 13: 创建PO转换器

**Files:**
- Create: `blog-server/src/main/java/com/blog/content/infrastructure/converter/ArticleConverter.java`
- Create: `blog-server/src/main/java/com/blog/content/infrastructure/converter/CategoryConverter.java`
- Create: `blog-server/src/main/java/com/blog/content/infrastructure/converter/TagConverter.java`

- [ ] **Step 1: 创建ArticleConverter**

```java
package com.blog.content.infrastructure.converter;

import com.blog.content.domain.model.article.*;
import com.blog.content.domain.model.category.CategoryId;
import com.blog.content.infrastructure.po.ArticlePO;
import com.blog.shared.domain.valueobject.AuditInfo;
import org.springframework.stereotype.Component;

/**
 * Article PO转换器
 */
@Component
public class ArticleConverter {
    
    public ArticlePO toPO(Article article) {
        ArticlePO po = new ArticlePO();
        if (article.getId() != null) {
            po.setId(article.getId().getValue());
        }
        po.setTitle(article.getTitle().value());
        po.setSummary(article.getSummary().value());
        po.setContent(article.getContent().value());
        po.setCoverImage(article.getCoverImage() != null ? article.getCoverImage().url() : null);
        po.setCategoryId(article.getCategoryId() != null ? article.getCategoryId().getValue() : null);
        po.setAuthorId(article.getAuthorId().getValue());
        po.setViewCount(article.getViewCount().value());
        po.setLikeCount(article.getLikeCount().value());
        po.setCommentCount(article.getCommentCount().value());
        po.setIsTop(article.getIsTop().toInt());
        po.setStatus(article.getStatus().getCode());
        po.setPublishTime(article.getPublishTime() != null ? article.getPublishTime().value() : null);
        if (article.getAuditInfo() != null) {
            po.setCreateTime(article.getAuditInfo().createTime());
            po.setUpdateTime(article.getAuditInfo().updateTime());
        }
        return po;
    }
    
    public Article toDomain(ArticlePO po) {
        return Article.reconstruct(
            new ArticleId(po.getId()),
            new ArticleTitle(po.getTitle()),
            new ArticleContent(po.getContent()),
            new ArticleSummary(po.getSummary()),
            po.getCoverImage() != null ? new CoverImage(po.getCoverImage()) : CoverImage.empty(),
            po.getCategoryId() != null ? new CategoryId(po.getCategoryId()) : null,
            new AuthorId(po.getAuthorId()),
            new ViewCount(po.getViewCount()),
            new LikeCount(po.getLikeCount()),
            new CommentCount(po.getCommentCount()),
            TopFlag.of(po.getIsTop()),
            ArticleStatus.fromCode(po.getStatus()),
            po.getPublishTime() != null ? PublishTime.of(po.getPublishTime()) : null,
            AuditInfo.of(po.getCreateTime(), po.getUpdateTime())
        );
    }
}
```

- [ ] **Step 2: 创建CategoryConverter**

```java
package com.blog.content.infrastructure.converter;

import com.blog.content.domain.model.category.*;
import com.blog.content.infrastructure.po.CategoryPO;
import com.blog.shared.domain.valueobject.AuditInfo;
import org.springframework.stereotype.Component;

/**
 * Category PO转换器
 */
@Component
public class CategoryConverter {
    
    public CategoryPO toPO(Category category) {
        CategoryPO po = new CategoryPO();
        if (category.getId() != null) {
            po.setId(category.getId().getValue());
        }
        po.setName(category.getName().value());
        po.setDescription(category.getDescription() != null ? category.getDescription().value() : null);
        po.setSort(category.getSort().value());
        po.setStatus(category.getStatus().getCode());
        if (category.getAuditInfo() != null) {
            po.setCreateTime(category.getAuditInfo().createTime());
            po.setUpdateTime(category.getAuditInfo().updateTime());
        }
        return po;
    }
    
    public Category toDomain(CategoryPO po) {
        return Category.reconstruct(
            new CategoryId(po.getId()),
            new CategoryName(po.getName()),
            po.getDescription() != null ? new CategoryDescription(po.getDescription()) : CategoryDescription.empty(),
            CategorySort.of(po.getSort()),
            CategoryStatus.fromCode(po.getStatus()),
            AuditInfo.of(po.getCreateTime(), po.getUpdateTime())
        );
    }
}
```

- [ ] **Step 3: 创建TagConverter**

```java
package com.blog.content.infrastructure.converter;

import com.blog.content.domain.model.tag.*;
import com.blog.content.infrastructure.po.TagPO;
import com.blog.shared.domain.valueobject.AuditInfo;
import org.springframework.stereotype.Component;

/**
 * Tag PO转换器
 */
@Component
public class TagConverter {
    
    public TagPO toPO(Tag tag) {
        TagPO po = new TagPO();
        if (tag.getId() != null) {
            po.setId(tag.getId().getValue());
        }
        po.setName(tag.getName().value());
        po.setColor(tag.getColor().value());
        if (tag.getAuditInfo() != null) {
            po.setCreateTime(tag.getAuditInfo().createTime());
            po.setUpdateTime(tag.getAuditInfo().updateTime());
        }
        return po;
    }
    
    public Tag toDomain(TagPO po) {
        return Tag.reconstruct(
            new TagId(po.getId()),
            new TagName(po.getName()),
            new TagColor(po.getColor()),
            AuditInfo.of(po.getCreateTime(), po.getUpdateTime())
        );
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add blog-server/src/main/java/com/blog/content/infrastructure/converter/
git commit -m "feat(content): add PO converters"
```

---

### Task 14: 创建Mapper接口

**Files:**
- Create: `blog-server/src/main/java/com/blog/content/infrastructure/mapper/ArticlePOMapper.java`
- Create: `blog-server/src/main/java/com/blog/content/infrastructure/mapper/CategoryPOMapper.java`
- Create: `blog-server/src/main/java/com/blog/content/infrastructure/mapper/TagPOMapper.java`

- [ ] **Step 1: 创建ArticlePOMapper**

```java
package com.blog.content.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.content.infrastructure.po.ArticlePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 文章Mapper
 */
@Mapper
public interface ArticlePOMapper extends BaseMapper<ArticlePO> {
    
    @Update("UPDATE article SET view_count = view_count + 1 WHERE id = #{id} AND deleted = 0")
    void incrementViewCount(@Param("id") Long id);
    
    @Update("UPDATE article SET comment_count = comment_count + 1 WHERE id = #{id} AND deleted = 0")
    void incrementCommentCount(@Param("id") Long id);
    
    @Update("UPDATE article SET comment_count = GREATEST(0, comment_count - 1) WHERE id = #{id} AND deleted = 0")
    void decrementCommentCount(@Param("id") Long id);
}
```

- [ ] **Step 2: 创建CategoryPOMapper**

```java
package com.blog.content.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.content.infrastructure.po.CategoryPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 分类Mapper
 */
@Mapper
public interface CategoryPOMapper extends BaseMapper<CategoryPO> {
}
```

- [ ] **Step 3: 创建TagPOMapper**

```java
package com.blog.content.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.content.infrastructure.po.TagPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 标签Mapper
 */
@Mapper
public interface TagPOMapper extends BaseMapper<TagPO> {
}
```

- [ ] **Step 4: Commit**

```bash
git add blog-server/src/main/java/com/blog/content/infrastructure/mapper/
git commit -m "feat(content): add PO mappers"
```

---

### Task 15: 实现仓储

**Files:**
- Create: `blog-server/src/main/java/com/blog/content/infrastructure/persistence/ArticleRepositoryImpl.java`
- Create: `blog-server/src/main/java/com/blog/content/infrastructure/persistence/CategoryRepositoryImpl.java`
- Create: `blog-server/src/main/java/com/blog/content/infrastructure/persistence/TagRepositoryImpl.java`

- [ ] **Step 1: 实现ArticleRepositoryImpl**

```java
package com.blog.content.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.content.domain.model.article.Article;
import com.blog.content.domain.model.article.ArticleId;
import com.blog.content.domain.model.article.ArticleStatus;
import com.blog.content.domain.model.category.CategoryId;
import com.blog.content.domain.model.tag.TagId;
import com.blog.content.domain.repository.ArticleQueryCriteria;
import com.blog.content.domain.repository.ArticleRepository;
import com.blog.content.infrastructure.converter.ArticleConverter;
import com.blog.content.infrastructure.mapper.ArticlePOMapper;
import com.blog.content.infrastructure.po.ArticlePO;
import com.blog.shared.domain.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 文章仓储实现
 */
@Repository
@RequiredArgsConstructor
public class ArticleRepositoryImpl implements ArticleRepository {
    
    private final ArticlePOMapper mapper;
    private final ArticleConverter converter;
    private final DomainEventPublisher eventPublisher;
    
    @Override
    public void save(Article article) {
        ArticlePO po = converter.toPO(article);
        if (po.getId() == null) {
            mapper.insert(po);
            // 设置生成的ID
            article.setId(new ArticleId(po.getId()));
        } else {
            mapper.updateById(po);
        }
        // 发布领域事件
        article.pullDomainEvents().forEach(eventPublisher::publish);
    }
    
    @Override
    public Optional<Article> findById(ArticleId id) {
        ArticlePO po = mapper.selectById(id.getValue());
        return Optional.ofNullable(po).map(converter::toDomain);
    }
    
    @Override
    public void delete(ArticleId id) {
        mapper.deleteById(id.getValue());
    }
    
    @Override
    public List<Article> findAll() {
        return mapper.selectList(
            new LambdaQueryWrapper<ArticlePO>()
                .eq(ArticlePO::getDeleted, 0)
                .orderByDesc(ArticlePO::getCreateTime)
        ).stream().map(converter::toDomain).collect(Collectors.toList());
    }
    
    @Override
    public List<Article> findByCriteria(ArticleQueryCriteria criteria) {
        LambdaQueryWrapper<ArticlePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticlePO::getDeleted, 0);
        
        if (StringUtils.hasText(criteria.getTitle())) {
            wrapper.like(ArticlePO::getTitle, criteria.getTitle());
        }
        if (criteria.getCategoryId() != null) {
            wrapper.eq(ArticlePO::getCategoryId, criteria.getCategoryId().getValue());
        }
        if (criteria.getStatus() != null) {
            wrapper.eq(ArticlePO::getStatus, criteria.getStatus().getCode());
        }
        
        wrapper.orderByDesc(ArticlePO::getIsTop)
               .orderByDesc(ArticlePO::getCreateTime);
        
        return mapper.selectList(wrapper).stream()
            .map(converter::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Article> findByCategory(CategoryId categoryId) {
        return mapper.selectList(
            new LambdaQueryWrapper<ArticlePO>()
                .eq(ArticlePO::getDeleted, 0)
                .eq(ArticlePO::getStatus, ArticleStatus.PUBLISHED.getCode())
                .eq(ArticlePO::getCategoryId, categoryId.getValue())
                .orderByDesc(ArticlePO::getPublishTime)
        ).stream().map(converter::toDomain).collect(Collectors.toList());
    }
    
    @Override
    public List<Article> findByTag(TagId tagId) {
        // 简化实现，实际需要联表查询article_tag
        throw new UnsupportedOperationException("待实现");
    }
    
    @Override
    public List<Article> search(String keyword) {
        return mapper.selectList(
            new LambdaQueryWrapper<ArticlePO>()
                .eq(ArticlePO::getDeleted, 0)
                .eq(ArticlePO::getStatus, ArticleStatus.PUBLISHED.getCode())
                .and(w -> w.like(ArticlePO::getTitle, keyword)
                    .or().like(ArticlePO::getSummary, keyword)
                    .or().like(ArticlePO::getContent, keyword))
                .orderByDesc(ArticlePO::getPublishTime)
        ).stream().map(converter::toDomain).collect(Collectors.toList());
    }
    
    @Override
    public List<Article> findHotArticles(int limit) {
        return mapper.selectList(
            new LambdaQueryWrapper<ArticlePO>()
                .eq(ArticlePO::getDeleted, 0)
                .eq(ArticlePO::getStatus, ArticleStatus.PUBLISHED.getCode())
                .orderByDesc(ArticlePO::getViewCount)
                .last("LIMIT " + limit)
        ).stream().map(converter::toDomain).collect(Collectors.toList());
    }
    
    @Override
    public List<Article> findTopArticles() {
        return mapper.selectList(
            new LambdaQueryWrapper<ArticlePO>()
                .eq(ArticlePO::getDeleted, 0)
                .eq(ArticlePO::getStatus, ArticleStatus.PUBLISHED.getCode())
                .eq(ArticlePO::getIsTop, 1)
                .orderByDesc(ArticlePO::getPublishTime)
        ).stream().map(converter::toDomain).collect(Collectors.toList());
    }
    
    @Override
    public List<Article> findPublished() {
        return mapper.selectList(
            new LambdaQueryWrapper<ArticlePO>()
                .eq(ArticlePO::getDeleted, 0)
                .eq(ArticlePO::getStatus, ArticleStatus.PUBLISHED.getCode())
                .orderByDesc(ArticlePO::getPublishTime)
        ).stream().map(converter::toDomain).collect(Collectors.toList());
    }
    
    @Override
    public boolean existsById(ArticleId id) {
        return mapper.selectCount(
            new LambdaQueryWrapper<ArticlePO>()
                .eq(ArticlePO::getId, id.getValue())
                .eq(ArticlePO::getDeleted, 0)
        ) > 0;
    }
}
```

- [ ] **Step 2: 实现CategoryRepositoryImpl**

```java
package com.blog.content.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.content.domain.model.category.Category;
import com.blog.content.domain.model.category.CategoryId;
import com.blog.content.domain.repository.CategoryRepository;
import com.blog.content.infrastructure.converter.CategoryConverter;
import com.blog.content.infrastructure.mapper.CategoryPOMapper;
import com.blog.content.infrastructure.po.CategoryPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 分类仓储实现
 */
@Repository
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {
    
    private final CategoryPOMapper mapper;
    private final CategoryConverter converter;
    
    @Override
    public void save(Category category) {
        CategoryPO po = converter.toPO(category);
        if (po.getId() == null) {
            mapper.insert(po);
            category.setId(new CategoryId(po.getId()));
        } else {
            mapper.updateById(po);
        }
    }
    
    @Override
    public Optional<Category> findById(CategoryId id) {
        CategoryPO po = mapper.selectById(id.getValue());
        return Optional.ofNullable(po).map(converter::toDomain);
    }
    
    @Override
    public List<Category> findAll() {
        return mapper.selectList(
            new LambdaQueryWrapper<CategoryPO>()
                .orderByAsc(CategoryPO::getSort)
        ).stream().map(converter::toDomain).collect(Collectors.toList());
    }
    
    @Override
    public void delete(CategoryId id) {
        mapper.deleteById(id.getValue());
    }
    
    @Override
    public boolean existsById(CategoryId id) {
        return mapper.selectById(id.getValue()) != null;
    }
    
    @Override
    public boolean existsByName(String name) {
        return mapper.selectCount(
            new LambdaQueryWrapper<CategoryPO>()
                .eq(CategoryPO::getName, name)
        ) > 0;
    }
}
```

- [ ] **Step 3: 实现TagRepositoryImpl**

```java
package com.blog.content.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.content.domain.model.tag.Tag;
import com.blog.content.domain.model.tag.TagId;
import com.blog.content.domain.repository.TagRepository;
import com.blog.content.infrastructure.converter.TagConverter;
import com.blog.content.infrastructure.mapper.TagPOMapper;
import com.blog.content.infrastructure.po.TagPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 标签仓储实现
 */
@Repository
@RequiredArgsConstructor
public class TagRepositoryImpl implements TagRepository {
    
    private final TagPOMapper mapper;
    private final TagConverter converter;
    
    @Override
    public void save(Tag tag) {
        TagPO po = converter.toPO(tag);
        if (po.getId() == null) {
            mapper.insert(po);
            tag.setId(new TagId(po.getId()));
        } else {
            mapper.updateById(po);
        }
    }
    
    @Override
    public Optional<Tag> findById(TagId id) {
        TagPO po = mapper.selectById(id.getValue());
        return Optional.ofNullable(po).map(converter::toDomain);
    }
    
    @Override
    public List<Tag> findAll() {
        return mapper.selectList(null).stream()
            .map(converter::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public void delete(TagId id) {
        mapper.deleteById(id.getValue());
    }
    
    @Override
    public boolean existsByName(String name) {
        return mapper.selectCount(
            new LambdaQueryWrapper<TagPO>()
                .eq(TagPO::getName, name)
        ) > 0;
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add blog-server/src/main/java/com/blog/content/infrastructure/persistence/
git commit -m "feat(content): implement repositories"
```

---

### Task 16: 创建应用服务

**Files:**
- Create: `blog-server/src/main/java/com/blog/content/application/ArticleApplicationService.java`
- Create: `blog-server/src/main/java/com/blog/content/application/CategoryApplicationService.java`
- Create: `blog-server/src/main/java/com/blog/content/application/TagApplicationService.java`
- Create: `blog-server/src/main/java/com/blog/content/application/command/CreateArticleCommand.java`
- Create: `blog-server/src/main/java/com/blog/content/application/command/UpdateArticleCommand.java`
- Create: `blog-server/src/main/java/com/blog/content/application/command/PublishArticleCommand.java`
- Create: `blog-server/src/main/java/com/blog/content/application/query/PageArticleQuery.java`

- [ ] **Step 1: 创建命令类**

```java
package com.blog.content.application.command;

import java.util.List;

/**
 * 创建文章命令
 */
public record CreateArticleCommand(
    String title,
    String content,
    String summary,
    String coverImage,
    Long categoryId,
    List<Long> tagIds,
    Integer status
) {}
```

```java
package com.blog.content.application.command;

import java.util.List;

/**
 * 更新文章命令
 */
public record UpdateArticleCommand(
    Long articleId,
    String title,
    String content,
    String summary,
    String coverImage,
    Long categoryId,
    List<Long> tagIds
) {}
```

```java
package com.blog.content.application.command;

/**
 * 发布文章命令
 */
public record PublishArticleCommand(Long articleId) {}
```

- [ ] **Step 2: 创建查询类**

```java
package com.blog.content.application.query;

/**
 * 分页查询文章
 */
public record PageArticleQuery(
    String title,
    Long categoryId,
    Integer status,
    int pageNum,
    int pageSize
) {}
```

- [ ] **Step 3: 创建ArticleApplicationService**

```java
package com.blog.content.application;

import com.blog.content.application.command.CreateArticleCommand;
import com.blog.content.application.command.PublishArticleCommand;
import com.blog.content.application.command.UpdateArticleCommand;
import com.blog.content.application.query.PageArticleQuery;
import com.blog.content.domain.model.article.*;
import com.blog.content.domain.model.category.CategoryId;
import com.blog.content.domain.repository.ArticleQueryCriteria;
import com.blog.content.domain.repository.ArticleRepository;
import com.blog.content.domain.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 文章应用服务
 */
@Service
@RequiredArgsConstructor
public class ArticleApplicationService {
    
    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    
    @Transactional
    public ArticleId createArticle(CreateArticleCommand command) {
        // 校验分类存在
        CategoryId categoryId = command.categoryId() != null ? 
            new CategoryId(command.categoryId()) : null;
        if (categoryId != null && !categoryRepository.existsById(categoryId)) {
            throw new IllegalArgumentException("分类不存在");
        }
        
        // 创建文章聚合根
        Article article = Article.create(
            new ArticleTitle(command.title()),
            new ArticleContent(command.content()),
            new ArticleSummary(command.summary()),
            new CoverImage(command.coverImage()),
            categoryId,
            new AuthorId(1L) // TODO: 从安全上下文获取当前用户
        );
        
        // 处理发布状态
        if (command.status() != null && command.status() == ArticleStatus.PUBLISHED.getCode()) {
            article.publish();
        }
        
        // 保存
        articleRepository.save(article);
        
        return article.getId();
    }
    
    @Transactional
    public void updateArticle(UpdateArticleCommand command) {
        Article article = articleRepository.findById(new ArticleId(command.articleId()))
            .orElseThrow(() -> new IllegalArgumentException("文章不存在"));
        
        CategoryId categoryId = command.categoryId() != null ? 
            new CategoryId(command.categoryId()) : null;
        
        article.updateContent(
            new ArticleTitle(command.title()),
            new ArticleContent(command.content()),
            new ArticleSummary(command.summary()),
            new CoverImage(command.coverImage()),
            categoryId
        );
        
        articleRepository.save(article);
    }
    
    @Transactional
    public void publishArticle(PublishArticleCommand command) {
        Article article = articleRepository.findById(new ArticleId(command.articleId()))
            .orElseThrow(() -> new IllegalArgumentException("文章不存在"));
        
        article.publish();
        articleRepository.save(article);
    }
    
    @Transactional
    public void deleteArticle(Long articleId) {
        Article article = articleRepository.findById(new ArticleId(articleId))
            .orElseThrow(() -> new IllegalArgumentException("文章不存在"));
        
        article.moveToTrash();
        articleRepository.save(article);
    }
    
    @Transactional
    public void toggleTop(Long articleId) {
        Article article = articleRepository.findById(new ArticleId(articleId))
            .orElseThrow(() -> new IllegalArgumentException("文章不存在"));
        
        article.toggleTop();
        articleRepository.save(article);
    }
    
    @Transactional(readOnly = true)
    public List<Article> pageArticle(PageArticleQuery query) {
        ArticleQueryCriteria criteria = ArticleQueryCriteria.builder()
            .title(query.title())
            .categoryId(query.categoryId() != null ? new CategoryId(query.categoryId()) : null)
            .status(query.status() != null ? ArticleStatus.fromCode(query.status()) : null)
            .build();
        
        return articleRepository.findByCriteria(criteria);
    }
    
    @Transactional(readOnly = true)
    public Article getArticle(Long articleId) {
        Article article = articleRepository.findById(new ArticleId(articleId))
            .orElseThrow(() -> new IllegalArgumentException("文章不存在"));
        
        // 增加浏览量
        article.incrementViewCount();
        articleRepository.save(article);
        
        return article;
    }
    
    @Transactional(readOnly = true)
    public List<Article> getHotArticles(int limit) {
        return articleRepository.findHotArticles(limit);
    }
    
    @Transactional(readOnly = true)
    public List<Article> getTopArticles() {
        return articleRepository.findTopArticles();
    }
}
```

- [ ] **Step 4: 创建CategoryApplicationService**

```java
package com.blog.content.application;

import com.blog.content.domain.model.category.*;
import com.blog.content.domain.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 分类应用服务
 */
@Service
@RequiredArgsConstructor
public class CategoryApplicationService {
    
    private final CategoryRepository categoryRepository;
    
    @Transactional
    public CategoryId createCategory(String name, String description, int sort) {
        if (categoryRepository.existsByName(name)) {
            throw new IllegalArgumentException("分类名称已存在");
        }
        
        Category category = Category.create(
            new CategoryName(name),
            new CategoryDescription(description),
            CategorySort.of(sort)
        );
        
        categoryRepository.save(category);
        return category.getId();
    }
    
    @Transactional
    public void updateCategory(Long id, String name, String description, int sort) {
        Category category = categoryRepository.findById(new CategoryId(id))
            .orElseThrow(() -> new IllegalArgumentException("分类不存在"));
        
        category.update(
            new CategoryName(name),
            new CategoryDescription(description),
            CategorySort.of(sort)
        );
        
        categoryRepository.save(category);
    }
    
    @Transactional
    public void deleteCategory(Long id) {
        categoryRepository.delete(new CategoryId(id));
    }
    
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}
```

- [ ] **Step 5: 创建TagApplicationService**

```java
package com.blog.content.application;

import com.blog.content.domain.model.tag.*;
import com.blog.content.domain.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 标签应用服务
 */
@Service
@RequiredArgsConstructor
public class TagApplicationService {
    
    private final TagRepository tagRepository;
    
    @Transactional
    public TagId createTag(String name, String color) {
        if (tagRepository.existsByName(name)) {
            throw new IllegalArgumentException("标签名称已存在");
        }
        
        Tag tag = Tag.create(new TagName(name), new TagColor(color));
        tagRepository.save(tag);
        
        return tag.getId();
    }
    
    @Transactional
    public void updateTag(Long id, String name, String color) {
        Tag tag = tagRepository.findById(new TagId(id))
            .orElseThrow(() -> new IllegalArgumentException("标签不存在"));
        
        tag.update(new TagName(name), new TagColor(color));
        tagRepository.save(tag);
    }
    
    @Transactional
    public void deleteTag(Long id) {
        tagRepository.delete(new TagId(id));
    }
    
    @Transactional(readOnly = true)
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }
}

- [ ] **Step 6: Commit**

```bash
git add blog-server/src/main/java/com/blog/content/application/
git commit -m "feat(content): add application services with CQRS-style commands/queries"
```

---

### Task 17: 创建接口层控制器

**Files:**
- Create: `blog-server/src/main/java/com/blog/content/interfaces/controller/admin/AdminArticleController.java`
- Create: `blog-server/src/main/java/com/blog/content/interfaces/controller/admin/AdminCategoryController.java`
- Create: `blog-server/src/main/java/com/blog/content/interfaces/controller/admin/AdminTagController.java`
- Create: `blog-server/src/main/java/com/blog/content/interfaces/controller/portal/PortalArticleController.java`
- Create: `blog-server/src/main/java/com/blog/content/interfaces/dto/ArticleResponse.java`

- [ ] **Step 1: 创建ArticleResponse DTO**

```java
package com.blog.content.interfaces.dto;

import com.blog.content.domain.model.article.Article;
import com.blog.content.domain.model.article.ArticleStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章响应DTO
 */
@Data
public class ArticleResponse {
    private Long id;
    private String title;
    private String content;
    private String summary;
    private String coverImage;
    private Long categoryId;
    private String categoryName;
    private Long viewCount;
    private Long likeCount;
    private Integer commentCount;
    private Integer status;
    private Integer isTop;
    private LocalDateTime publishTime;
    private Long authorId;
    
    public static ArticleResponse from(Article article) {
        ArticleResponse response = new ArticleResponse();
        response.setId(article.getId().getValue());
        response.setTitle(article.getTitle().value());
        response.setContent(article.getContent().value());
        response.setSummary(article.getSummary().value());
        response.setCoverImage(article.getCoverImage() != null ? article.getCoverImage().url() : null);
        response.setCategoryId(article.getCategoryId() != null ? article.getCategoryId().getValue() : null);
        response.setViewCount(article.getViewCount().value());
        response.setLikeCount(article.getLikeCount().value());
        response.setCommentCount(article.getCommentCount().value());
        response.setStatus(article.getStatus().getCode());
        response.setIsTop(article.getIsTop().toInt());
        response.setPublishTime(article.getPublishTime() != null ? article.getPublishTime().value() : null);
        response.setAuthorId(article.getAuthorId().getValue());
        return response;
    }
}
```

- [ ] **Step 2: 创建AdminArticleController**

```java
package com.blog.content.interfaces.controller.admin;

import com.blog.content.application.ArticleApplicationService;
import com.blog.content.application.command.CreateArticleCommand;
import com.blog.content.application.command.PublishArticleCommand;
import com.blog.content.application.command.UpdateArticleCommand;
import com.blog.content.application.query.PageArticleQuery;
import com.blog.content.domain.model.article.Article;
import com.blog.content.domain.model.article.ArticleId;
import com.blog.content.interfaces.dto.ArticleResponse;
import com.blog.shared.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端文章控制器
 */
@RestController
@RequestMapping("/api/admin/articles")
@RequiredArgsConstructor
public class AdminArticleController {
    
    private final ArticleApplicationService articleService;
    
    @PostMapping
    public Result<ArticleId> create(@RequestBody CreateArticleRequest request) {
        CreateArticleCommand command = new CreateArticleCommand(
            request.getTitle(),
            request.getContent(),
            request.getSummary(),
            request.getCoverImage(),
            request.getCategoryId(),
            request.getTagIds(),
            request.getStatus()
        );
        ArticleId articleId = articleService.createArticle(command);
        return Result.success(articleId);
    }
    
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody UpdateArticleRequest request) {
        UpdateArticleCommand command = new UpdateArticleCommand(
            id,
            request.getTitle(),
            request.getContent(),
            request.getSummary(),
            request.getCoverImage(),
            request.getCategoryId(),
            request.getTagIds()
        );
        articleService.updateArticle(command);
        return Result.success();
    }
    
    @PostMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        articleService.publishArticle(new PublishArticleCommand(id));
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return Result.success();
    }
    
    @PostMapping("/{id}/toggle-top")
    public Result<Void> toggleTop(@PathVariable Long id) {
        articleService.toggleTop(id);
        return Result.success();
    }
    
    @GetMapping
    public Result<List<ArticleResponse>> page(PageArticleRequest request) {
        PageArticleQuery query = new PageArticleQuery(
            request.getTitle(),
            request.getCategoryId(),
            request.getStatus(),
            request.getPageNum(),
            request.getPageSize()
        );
        List<Article> articles = articleService.pageArticle(query);
        List<ArticleResponse> responses = articles.stream()
            .map(ArticleResponse::from)
            .toList();
        return Result.success(responses);
    }
}

// 简化的请求类
@lombok.Data
class CreateArticleRequest {
    private String title;
    private String content;
    private String summary;
    private String coverImage;
    private Long categoryId;
    private java.util.List<Long> tagIds;
    private Integer status;
}

@lombok.Data
class UpdateArticleRequest {
    private String title;
    private String content;
    private String summary;
    private String coverImage;
    private Long categoryId;
    private java.util.List<Long> tagIds;
}

@lombok.Data
class PageArticleRequest {
    private String title;
    private Long categoryId;
    private Integer status;
    private int pageNum = 1;
    private int pageSize = 10;
}
```

- [ ] **Step 3: 创建PortalArticleController**

```java
package com.blog.content.interfaces.controller.portal;

import com.blog.content.application.ArticleApplicationService;
import com.blog.content.domain.model.article.Article;
import com.blog.content.interfaces.dto.ArticleResponse;
import com.blog.shared.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 门户端文章控制器
 */
@RestController
@RequestMapping("/api/portal/articles")
@RequiredArgsConstructor
public class PortalArticleController {
    
    private final ArticleApplicationService articleService;
    
    @GetMapping("/{id}")
    public Result<ArticleResponse> detail(@PathVariable Long id) {
        Article article = articleService.getArticle(id);
        return Result.success(ArticleResponse.from(article));
    }
    
    @GetMapping("/hot")
    public Result<List<ArticleResponse>> hotArticles(
            @RequestParam(defaultValue = "10") int limit) {
        List<Article> articles = articleService.getHotArticles(limit);
        List<ArticleResponse> responses = articles.stream()
            .map(ArticleResponse::from)
            .toList();
        return Result.success(responses);
    }
    
    @GetMapping("/top")
    public Result<List<ArticleResponse>> topArticles() {
        List<Article> articles = articleService.getTopArticles();
        List<ArticleResponse> responses = articles.stream()
            .map(ArticleResponse::from)
            .toList();
        return Result.success(responses);
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add blog-server/src/main/java/com/blog/content/interfaces/
git commit -m "feat(content): add controllers for admin and portal"
```

---

### Task 18: 验证Content Context编译

**Files:**
- Modify: `blog-server/pom.xml` (if needed)

- [ ] **Step 1: 编译项目**

```bash
cd blog-server && mvn compile -DskipTests
```

Expected: BUILD SUCCESS

- [ ] **Step 2: 检查是否有编译错误**

如果有编译错误，根据错误信息修复。

- [ ] **Step 3: Commit**

```bash
git add .
git commit -m "fix(content): resolve compilation issues"
```

---

## Phase 3: User Context 重构

> **说明：** 以下为User上下文的关键任务概要，详细步骤参照Content Context的模式执行。

### Task 22-28: User Context 实现要点

| 任务 | 文件 | 说明 |
|------|------|------|
| Task 22 | `user/domain/model/user/*.java` | 创建UserId、Username、Password、Email等值对象 |
| Task 23 | `user/domain/model/user/User.java` | 创建User聚合根，实现register/updateProfile/changePassword等行为 |
| Task 24 | `user/domain/model/userfollow/*.java` | 创建UserFollow聚合根 |
| Task 25 | `user/domain/model/notification/*.java` | 创建Notification聚合根 |
| Task 26 | `user/domain/event/*.java` | 创建UserRegisteredEvent、UserFollowedEvent |
| Task 27 | `user/domain/repository/*.java` | 创建UserRepository、UserFollowRepository、NotificationRepository |
| Task 28 | `user/infrastructure/`, `user/application/`, `user/interfaces/` | 实现基础设施、应用服务、控制器 |

---

## Phase 4: Interaction Context 重构

> **说明：** Interaction上下文包含Comment、Message、PrivateMessage、Favorite、ReadingHistory聚合。

### Task 29-35: Interaction Context 实现要点

| 任务 | 聚合根 | 关键领域行为 |
|------|--------|-------------|
| Task 29 | Comment | approve(), reject(), reply() |
| Task 30 | Message | approve(), reject() |
| Task 31 | PrivateMessage | sendMessage(), markAsRead() |
| Task 32 | Favorite | 静态工厂方法create() |
| Task 33 | ReadingHistory | updateDuration() |
| Task 34 | 领域事件 | CommentApprovedEvent |
| Task 35 | 基础设施、应用服务、控制器 | 完整实现 |

---

## Phase 5: System Context 重构

> **说明：** System上下文包含Announcement、SensitiveWord、Media、SysConfig聚合。

### Task 36-40: System Context 实现要点

| 任务 | 聚合根 | 关键领域行为 |
|------|--------|-------------|
| Task 36 | Announcement | publish(), unpublish() |
| Task 37 | SensitiveWord | enable(), disable() |
| Task 38 | Media | 媒体文件管理 |
| Task 39 | SysConfig | 配置项管理 |
| Task 40 | 领域事件 | AnnouncementPublishedEvent |

---

## Phase 3-5: 其他上下文重构（概要）

由于篇幅限制，User、Interaction、System上下文的重构步骤与Content上下文类似：

### User Context 重构步骤

1. 创建值对象：`UserId`, `Username`, `Password`, `Nickname`, `Email`, `UserRole`, `UserStatus`
2. 创建User聚合根，实现领域行为（注册、更新资料、修改密码、禁用/启用）
3. 创建UserFollow聚合根，实现关注关系
4. 创建Notification聚合根，实现通知功能
5. 创建领域事件：`UserRegisteredEvent`, `UserFollowedEvent`
6. 创建仓储接口和实现
7. 创建应用服务和控制器

### Interaction Context 重构步骤

1. 创建值对象：`CommentId`, `CommentContent`, `CommentStatus`, `Commenter`
2. 创建Comment聚合根，实现领域行为（审核、回复）
3. 创建PrivateMessage聚合根，实现会话管理
4. 创建领域事件：`CommentApprovedEvent`
5. 创建仓储接口和实现
6. 创建应用服务和控制器

### System Context 重构步骤

1. 创建Announcement、SensitiveWord、Media、SysConfig聚合
2. 创建领域事件：`AnnouncementPublishedEvent`
3. 创建仓储和应用服务

---

## Phase 6: 集成测试与清理

### Task 19: 运行集成测试

- [ ] **Step 1: 启动应用**

```bash
cd blog-server && mvn spring-boot:run
```

- [ ] **Step 2: 测试API端点**

```bash
# 测试创建文章
curl -X POST http://localhost:8080/api/admin/articles \
  -H "Content-Type: application/json" \
  -d '{"title":"测试文章","content":"测试内容","summary":"测试摘要","categoryId":1,"status":0}'

# 测试发布文章
curl -X POST http://localhost:8080/api/admin/articles/1/publish

# 测试获取文章详情
curl http://localhost:8080/api/portal/articles/1

# 测试获取热门文章
curl http://localhost:8080/api/portal/articles/hot?limit=5
```

Expected: 所有API返回正常响应

- [ ] **Step 3: Commit**

```bash
git add .
git commit -m "test: verify DDD refactored APIs"
```

---

### Task 20: 清理旧代码

**Files:**
- Delete: `blog-server/src/main/java/com/blog/domain/` (旧的entity目录)
- Delete: `blog-server/src/main/java/com/blog/service/` (旧的service目录)
- Delete: `blog-server/src/main/java/com/blog/controller/` (旧的controller目录)
- Delete: `blog-server/src/main/java/com/blog/repository/` (旧的mapper目录)

- [ ] **Step 1: 确认新代码正常工作后，删除旧代码**

```bash
# 谨慎操作！确保已备份或提交
rm -rf blog-server/src/main/java/com/blog/domain/
rm -rf blog-server/src/main/java/com/blog/service/
rm -rf blog-server/src/main/java/com/blog/controller/
rm -rf blog-server/src/main/java/com/blog/repository/
```

- [ ] **Step 2: 验证编译**

```bash
cd blog-server && mvn compile -DskipTests
```

- [ ] **Step 3: Commit**

```bash
git add .
git commit -m "refactor: remove old architecture code"
```

---

### Task 21: 更新文档

**Files:**
- Update: `CLAUDE.md`

- [ ] **Step 1: 更新架构说明**

在CLAUDE.md中更新架构描述，反映DDD重构后的结构。

- [ ] **Step 2: Commit**

```bash
git add CLAUDE.md
git commit -m "docs: update architecture documentation for DDD"
```

---

## 文件结构总览

```
blog-server/src/main/java/com/blog/
├── BlogApplication.java
├── shared/                              # 共享内核
│   ├── domain/
│   │   ├── AggregateRoot.java
│   │   ├── DomainEvent.java
│   │   ├── DomainEventPublisher.java
│   │   └── valueobject/
│   │       └── AuditInfo.java
│   ├── common/
│   │   ├── result/
│   │   │   └── Result.java
│   │   └── exception/
│   └── infrastructure/
│       └── event/
│           └── SpringDomainEventPublisher.java
├── content/                             # 内容上下文
│   ├── domain/
│   │   ├── model/
│   │   │   ├── article/
│   │   │   │   ├── Article.java
│   │   │   │   ├── ArticleId.java
│   │   │   │   ├── ArticleTitle.java
│   │   │   │   ├── ArticleContent.java
│   │   │   │   ├── ArticleSummary.java
│   │   │   │   ├── ArticleStatus.java
│   │   │   │   ├── ViewCount.java
│   │   │   │   ├── LikeCount.java
│   │   │   │   ├── CommentCount.java
│   │   │   │   ├── TopFlag.java
│   │   │   │   ├── PublishTime.java
│   │   │   │   ├── CoverImage.java
│   │   │   │   └── AuthorId.java
│   │   │   ├── category/
│   │   │   │   ├── Category.java
│   │   │   │   ├── CategoryId.java
│   │   │   │   ├── CategoryName.java
│   │   │   │   ├── CategoryDescription.java
│   │   │   │   ├── CategorySort.java
│   │   │   │   └── CategoryStatus.java
│   │   │   └── tag/
│   │   │       ├── Tag.java
│   │   │       ├── TagId.java
│   │   │       ├── TagName.java
│   │   │       └── TagColor.java
│   │   ├── repository/
│   │   │   ├── ArticleRepository.java
│   │   │   ├── CategoryRepository.java
│   │   │   └── TagRepository.java
│   │   └── event/
│   │       ├── ArticlePublishedEvent.java
│   │       └── ArticleDeletedEvent.java
│   ├── application/
│   │   ├── ArticleApplicationService.java
│   │   ├── CategoryApplicationService.java
│   │   ├── TagApplicationService.java
│   │   ├── command/
│   │   │   ├── CreateArticleCommand.java
│   │   │   ├── UpdateArticleCommand.java
│   │   │   └── PublishArticleCommand.java
│   │   └── query/
│   │       └── PageArticleQuery.java
│   ├── interfaces/
│   │   ├── controller/
│   │   │   ├── admin/
│   │   │   │   ├── AdminArticleController.java
│   │   │   │   ├── AdminCategoryController.java
│   │   │   │   └── AdminTagController.java
│   │   │   └── portal/
│   │   │       └── PortalArticleController.java
│   │   └── dto/
│   │       └── ArticleResponse.java
│   └── infrastructure/
│       ├── persistence/
│       │   ├── ArticleRepositoryImpl.java
│       │   ├── CategoryRepositoryImpl.java
│       │   └── TagRepositoryImpl.java
│       ├── mapper/
│       │   ├── ArticlePOMapper.java
│       │   ├── CategoryPOMapper.java
│       │   └── TagPOMapper.java
│       ├── po/
│       │   ├── ArticlePO.java
│       │   ├── CategoryPO.java
│       │   └── TagPO.java
│       └── converter/
│           ├── ArticleConverter.java
│           ├── CategoryConverter.java
│           └── TagConverter.java
├── user/                                # 用户上下文 (Phase 3)
├── interaction/                         # 互动上下文 (Phase 4)
└── system/                              # 系统上下文 (Phase 5)
```
