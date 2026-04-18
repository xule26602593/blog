# 博客详情页上一篇/下一篇功能设计

## 需求概述

在博客文章详情页面增加"上一篇"和"下一篇"导航功能，帮助用户快速浏览相邻文章。

## 功能规则

### 导航优先级

查找上一篇/下一篇时，按以下优先级顺序：

1. **同分类优先**：查找同一分类下按发布时间相邻的已发布文章
2. **同标签次之**：若同分类无结果，查找有相同标签的已发布文章
3. **全局兜底**：若以上都没有，返回全局按发布时间相邻的已发布文章

### 边界情况

- 第一篇文章：上一篇为空，下一篇有值
- 最后一篇文章：上一篇有值，下一篇为空
- 只有一篇文章：上一篇和下一篇都为空

## 技术设计

### 后端改动

#### 1. 新增 VO 类

在 `ArticleVO` 中添加相邻文章信息字段：

```java
// ArticleVO.java
public class ArticleVO {
    // ... 现有字段 ...

    /**
     * 上一篇文章
     */
    private ArticleNavVO prevArticle;

    /**
     * 下一篇文章
     */
    private ArticleNavVO nextArticle;

    /**
     * 相邻文章摘要信息
     */
    @Data
    public static class ArticleNavVO {
        private Long id;
        private String title;
        private String categoryName;
    }
}
```

#### 2. 新增 Service 方法

```java
// ArticleService.java
/**
 * 获取上一篇文章
 * @param currentId 当前文章ID
 * @param categoryId 当前文章分类ID
 * @param tagIds 当前文章标签ID列表
 * @return 上一篇文章导航信息，没有返回null
 */
ArticleNavVO getPrevArticle(Long currentId, Long categoryId, List<Long> tagIds);

/**
 * 获取下一篇文章
 * @param currentId 当前文章ID
 * @param categoryId 当前文章分类ID
 * @param tagIds 当前文章标签ID列表
 * @return 下一篇文章导航信息，没有返回null
 */
ArticleNavVO getNextArticle(Long currentId, Long categoryId, List<Long> tagIds);
```

#### 3. Service 实现逻辑

```java
// ArticleServiceImpl.java

@Override
public ArticleNavVO getPrevArticle(Long currentId, Long categoryId, List<Long> tagIds) {
    // 1. 查找同分类上一篇
    ArticleNavVO article = findPrevByCategory(currentId, categoryId);
    if (article != null) return article;

    // 2. 查找同标签上一篇
    article = findPrevByTags(currentId, tagIds);
    if (article != null) return article;

    // 3. 全局上一篇
    return findPrevGlobal(currentId);
}

@Override
public ArticleNavVO getNextArticle(Long currentId, Long categoryId, List<Long> tagIds) {
    // 同上逻辑，查找下一篇
}
```

#### 4. 修改 getArticleById 方法

在 `ArticleServiceImpl.getArticleById()` 中填充相邻文章信息：

```java
@Override
public ArticleVO getArticleById(Long id) {
    // 现有逻辑：获取文章详情
    ArticleVO articleVO = ...;

    // 新增：填充相邻文章
    List<Long> tagIds = articleVO.getTags() != null
        ? articleVO.getTags().stream().map(TagVO::getId).collect(Collectors.toList())
        : Collections.emptyList();

    articleVO.setPrevArticle(getPrevArticle(id, articleVO.getCategoryId(), tagIds));
    articleVO.setNextArticle(getNextArticle(id, articleVO.getCategoryId(), tagIds));

    return articleVO;
}
```

#### 5. Mapper 新增查询方法

```java
// ArticleMapper.java

/**
 * 查找同分类上一篇（发布时间小于当前文章，按时间倒序取第一条）
 */
ArticleNavVO selectPrevByCategory(@Param("currentId") Long currentId,
                                   @Param("categoryId") Long categoryId);

/**
 * 查找同分类下一篇（发布时间大于当前文章，按时间正序取第一条）
 */
ArticleNavVO selectNextByCategory(@Param("currentId") Long currentId,
                                   @Param("categoryId") Long categoryId);

/**
 * 查找同标签上一篇
 */
ArticleNavVO selectPrevByTags(@Param("currentId") Long currentId,
                               @Param("tagIds") List<Long> tagIds);

/**
 * 查找同标签下一篇
 */
ArticleNavVO selectNextByTags(@Param("currentId") Long currentId,
                               @Param("tagIds") List<Long> tagIds);

/**
 * 查找全局上一篇
 */
ArticleNavVO selectPrevGlobal(@Param("currentId") Long currentId);

/**
 * 查找全局下一篇
 */
ArticleNavVO selectNextGlobal(@Param("currentId") Long currentId);
```

### 前端改动

#### 1. 修改 ArticleDetail.vue

在文章内容底部（`.article-footer` 上方）添加导航组件：

```vue
<!-- 文章导航 -->
<nav v-if="article.prevArticle || article.nextArticle" class="article-nav">
  <router-link
    v-if="article.prevArticle"
    :to="`/article/${article.prevArticle.id}`"
    class="nav-btn nav-prev"
  >
    <svg class="nav-arrow" viewBox="0 0 24 24">
      <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 19.5L8.25 12l7.5-7.5" />
    </svg>
    <div class="nav-content">
      <span class="nav-label">上一篇</span>
      <span class="nav-title">{{ article.prevArticle.title }}</span>
    </div>
  </router-link>
  <div v-else class="nav-btn nav-empty"></div>

  <router-link
    v-if="article.nextArticle"
    :to="`/article/${article.nextArticle.id}`"
    class="nav-btn nav-next"
  >
    <div class="nav-content">
      <span class="nav-label">下一篇</span>
      <span class="nav-title">{{ article.nextArticle.title }}</span>
    </div>
    <svg class="nav-arrow" viewBox="0 0 24 24">
      <path stroke-linecap="round" stroke-linejoin="round" d="M8.25 4.5l7.5 7.5-7.5 7.5" />
    </svg>
  </router-link>
  <div v-else class="nav-btn nav-empty"></div>
</nav>
```

#### 2. 样式设计

```scss
.article-nav {
  display: flex;
  gap: var(--space-4);
  padding: var(--space-6) var(--space-10);
  border-top: 1px solid var(--border-color);
  background: var(--bg-secondary);
}

.nav-btn {
  flex: 1;
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
  border-radius: var(--radius-lg);
  text-decoration: none;
  transition: all var(--transition-fast);

  &:hover {
    background: var(--bg-hover);
    transform: translateY(-2px);
  }

  &.nav-prev {
    text-align: left;
  }

  &.nav-next {
    text-align: right;
    flex-direction: row-reverse;
  }

  &.nav-empty {
    visibility: hidden;
  }
}

.nav-arrow {
  width: 24px;
  height: 24px;
  stroke: var(--color-primary);
  stroke-width: 2;
  fill: none;
  flex-shrink: 0;
}

.nav-content {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  min-width: 0;
}

.nav-label {
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.nav-title {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
```

### 数据流

```
用户访问文章详情页
         ↓
前端调用 GET /api/portal/article/{id}
         ↓
后端 ArticleService.getArticleById(id)
         ↓
    ┌────────────────────────────────┐
    │ 1. 查询文章详情                 │
    │ 2. 查询文章标签                 │
    │ 3. 调用 getPrevArticle()       │
    │ 4. 调用 getNextArticle()       │
    └────────────────────────────────┘
         ↓
返回 ArticleVO（包含 prevArticle/nextArticle）
         ↓
前端渲染导航按钮
         ↓
用户点击 → 路由跳转到新文章详情页
```

## 文件改动清单

### 后端

| 文件 | 改动类型 | 说明 |
|------|----------|------|
| `ArticleVO.java` | 修改 | 添加 prevArticle/nextArticle 字段和内部类 ArticleNavVO |
| `ArticleService.java` | 修改 | 添加 getPrevArticle/getNextArticle 方法声明 |
| `ArticleServiceImpl.java` | 修改 | 实现相邻文章查询逻辑，修改 getArticleById |
| `ArticleMapper.java` | 修改 | 添加6个查询方法声明 |
| `ArticleMapper.xml` | 修改 | 添加6个SQL查询 |

### 前端

| 文件 | 改动类型 | 说明 |
|------|----------|------|
| `ArticleDetail.vue` | 修改 | 添加导航组件模板和样式 |

## 测试要点

1. **正常导航**：中间文章有上篇和下篇
2. **边界情况**：第一篇文章无上篇，最后一篇文章无下篇
3. **单篇文章**：上篇和下篇都为空
4. **分类降级**：同分类无文章时降级到同标签
5. **标签降级**：同标签无文章时降级到全局
6. **无标签文章**：标签列表为空时直接走全局
7. **路由跳转**：点击导航按钮正确跳转到目标文章
