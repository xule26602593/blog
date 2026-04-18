# 博客详情页上一篇/下一篇功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在博客文章详情页添加上一篇/下一篇导航功能，按分类>标签>全局优先级查找相邻文章

**Architecture:** 后端在 ArticleVO 中扩展 prevArticle/nextArticle 字段，ArticleService 实现三级查找逻辑，前端在文章底部添加导航按钮组件

**Tech Stack:** Spring Boot 3 + MyBatis Plus + Vue 3 + Vant 4

---

## 文件结构

| 文件 | 责任 |
|------|------|
| `ArticleVO.java` | 文章详情VO，新增相邻文章字段和内部类 |
| `ArticleService.java` | Service接口，新增相邻文章查询方法声明 |
| `ArticleServiceImpl.java` | Service实现，实现三级查找逻辑 |
| `ArticleMapper.java` | Mapper接口，新增相邻文章查询方法 |
| `ArticleDetail.vue` | 文章详情页，新增导航组件 |

---

### Task 1: 后端 - 扩展 ArticleVO 添加相邻文章字段

**Files:**
- Modify: `blog-server/src/main/java/com/blog/domain/vo/ArticleVO.java`

- [ ] **Step 1: 在 ArticleVO 中添加 ArticleNavVO 内部类和相邻文章字段**

```java
package com.blog.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleVO {
    
    private Long id;
    
    private String title;
    
    private String summary;
    
    private String content;
    
    private String coverImage;
    
    private Long categoryId;
    
    private String categoryName;
    
    private Long authorId;
    
    private String authorName;
    
    private String authorAvatar;
    
    private Long viewCount;
    
    private Long likeCount;
    
    private Integer commentCount;
    
    private Integer isTop;
    
    private Integer status;
    
    private LocalDateTime publishTime;
    
    private LocalDateTime createTime;
    
    private List<TagVO> tags;
    
    private Boolean isLiked;
    
    private Boolean isFavorited;
    
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

- [ ] **Step 2: 验证编译通过**

Run: `cd blog-server && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add blog-server/src/main/java/com/blog/domain/vo/ArticleVO.java
git commit -m "feat: ArticleVO 添加 prevArticle/nextArticle 字段"
```

---

### Task 2: 后端 - ArticleMapper 添加相邻文章查询方法

**Files:**
- Modify: `blog-server/src/main/java/com/blog/repository/mapper/ArticleMapper.java`

- [ ] **Step 1: 在 ArticleMapper 中添加相邻文章查询方法**

```java
package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.Article;
import com.blog.domain.vo.ArticleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
    
    @Update("UPDATE article SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(@Param("id") Long id);
    
    @Update("UPDATE article SET like_count = like_count + #{delta} WHERE id = #{id}")
    int updateLikeCount(@Param("id") Long id, @Param("delta") int delta);
    
    @Update("UPDATE article SET comment_count = comment_count + #{delta} WHERE id = #{id}")
    int updateCommentCount(@Param("id") Long id, @Param("delta") int delta);
    
    @Select("SELECT COUNT(*) FROM article WHERE deleted = 0 AND status = 1")
    Long countPublished();
    
    @Select("SELECT COUNT(*) FROM article WHERE deleted = 0 AND status = 1 AND DATE(publish_time) = CURDATE()")
    Long countTodayPublished();
    
    @Select("SELECT IFNULL(SUM(view_count), 0) FROM article WHERE deleted = 0")
    Long sumViewCount();
    
    /**
     * 查找同分类上一篇（发布时间早于当前文章，按时间倒序取第一条）
     */
    @Select("SELECT a.id, a.title, c.name as category_name " +
            "FROM article a LEFT JOIN category c ON a.category_id = c.id " +
            "WHERE a.deleted = 0 AND a.status = 1 " +
            "AND a.category_id = #{categoryId} " +
            "AND a.publish_time < (SELECT publish_time FROM article WHERE id = #{currentId}) " +
            "ORDER BY a.publish_time DESC LIMIT 1")
    ArticleVO.ArticleNavVO selectPrevByCategory(@Param("currentId") Long currentId, 
                                                 @Param("categoryId") Long categoryId);
    
    /**
     * 查找同分类下一篇（发布时间晚于当前文章，按时间正序取第一条）
     */
    @Select("SELECT a.id, a.title, c.name as category_name " +
            "FROM article a LEFT JOIN category c ON a.category_id = c.id " +
            "WHERE a.deleted = 0 AND a.status = 1 " +
            "AND a.category_id = #{categoryId} " +
            "AND a.publish_time > (SELECT publish_time FROM article WHERE id = #{currentId}) " +
            "ORDER BY a.publish_time ASC LIMIT 1")
    ArticleVO.ArticleNavVO selectNextByCategory(@Param("currentId") Long currentId, 
                                                 @Param("categoryId") Long categoryId);
    
    /**
     * 查找同标签上一篇
     */
    @Select("<script>" +
            "SELECT a.id, a.title, c.name as category_name " +
            "FROM article a " +
            "LEFT JOIN category c ON a.category_id = c.id " +
            "INNER JOIN article_tag at ON a.id = at.article_id " +
            "WHERE a.deleted = 0 AND a.status = 1 " +
            "AND at.tag_id IN <foreach collection='tagIds' item='tagId' open='(' separator=',' close=')'>#{tagId}</foreach> " +
            "AND a.id != #{currentId} " +
            "AND a.publish_time < (SELECT publish_time FROM article WHERE id = #{currentId}) " +
            "ORDER BY a.publish_time DESC LIMIT 1" +
            "</script>")
    ArticleVO.ArticleNavVO selectPrevByTags(@Param("currentId") Long currentId, 
                                             @Param("tagIds") List<Long> tagIds);
    
    /**
     * 查找同标签下一篇
     */
    @Select("<script>" +
            "SELECT a.id, a.title, c.name as category_name " +
            "FROM article a " +
            "LEFT JOIN category c ON a.category_id = c.id " +
            "INNER JOIN article_tag at ON a.id = at.article_id " +
            "WHERE a.deleted = 0 AND a.status = 1 " +
            "AND at.tag_id IN <foreach collection='tagIds' item='tagId' open='(' separator=',' close=')'>#{tagId}</foreach> " +
            "AND a.id != #{currentId} " +
            "AND a.publish_time > (SELECT publish_time FROM article WHERE id = #{currentId}) " +
            "ORDER BY a.publish_time ASC LIMIT 1" +
            "</script>")
    ArticleVO.ArticleNavVO selectNextByTags(@Param("currentId") Long currentId, 
                                             @Param("tagIds") List<Long> tagIds);
    
    /**
     * 查找全局上一篇
     */
    @Select("SELECT a.id, a.title, c.name as category_name " +
            "FROM article a LEFT JOIN category c ON a.category_id = c.id " +
            "WHERE a.deleted = 0 AND a.status = 1 " +
            "AND a.publish_time < (SELECT publish_time FROM article WHERE id = #{currentId}) " +
            "ORDER BY a.publish_time DESC LIMIT 1")
    ArticleVO.ArticleNavVO selectPrevGlobal(@Param("currentId") Long currentId);
    
    /**
     * 查找全局下一篇
     */
    @Select("SELECT a.id, a.title, c.name as category_name " +
            "FROM article a LEFT JOIN category c ON a.category_id = c.id " +
            "WHERE a.deleted = 0 AND a.status = 1 " +
            "AND a.publish_time > (SELECT publish_time FROM article WHERE id = #{currentId}) " +
            "ORDER BY a.publish_time ASC LIMIT 1")
    ArticleVO.ArticleNavVO selectNextGlobal(@Param("currentId") Long currentId);
}
```

- [ ] **Step 2: 验证编译通过**

Run: `cd blog-server && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add blog-server/src/main/java/com/blog/repository/mapper/ArticleMapper.java
git commit -m "feat: ArticleMapper 添加相邻文章查询方法"
```

---

### Task 3: 后端 - ArticleService 添加方法声明

**Files:**
- Modify: `blog-server/src/main/java/com/blog/service/ArticleService.java`

- [ ] **Step 1: 在 ArticleService 接口中添加方法声明**

```java
package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.domain.dto.ArticleDTO;
import com.blog.domain.dto.ArticleQueryDTO;
import com.blog.domain.vo.ArticleListVO;
import com.blog.domain.vo.ArticleVO;

import java.util.List;

public interface ArticleService {

    Page<ArticleListVO> pageArticle(ArticleQueryDTO query);

    ArticleVO getArticleById(Long id);

    List<ArticleListVO> getHotArticles(int limit);

    List<ArticleListVO> getTopArticles();

    void saveOrUpdateArticle(ArticleDTO dto);

    void deleteArticle(Long id);

    void updateStatus(Long id, Integer status);

    void toggleTop(Long id);

    Page<ArticleListVO> searchArticle(String keyword, int pageNum, int pageSize);

    Page<ArticleListVO> getArticlesByCategory(Long categoryId, int pageNum, int pageSize);

    Page<ArticleListVO> getArticlesByTag(Long tagId, int pageNum, int pageSize);

    List<ArticleListVO> getArchiveList();
    
    /**
     * 获取上一篇文章
     * @param currentId 当前文章ID
     * @param categoryId 当前文章分类ID
     * @param tagIds 当前文章标签ID列表
     * @return 上一篇文章导航信息，没有返回null
     */
    ArticleVO.ArticleNavVO getPrevArticle(Long currentId, Long categoryId, List<Long> tagIds);
    
    /**
     * 获取下一篇文章
     * @param currentId 当前文章ID
     * @param categoryId 当前文章分类ID
     * @param tagIds 当前文章标签ID列表
     * @return 下一篇文章导航信息，没有返回null
     */
    ArticleVO.ArticleNavVO getNextArticle(Long currentId, Long categoryId, List<Long> tagIds);
}
```

- [ ] **Step 2: 验证编译通过**

Run: `cd blog-server && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add blog-server/src/main/java/com/blog/service/ArticleService.java
git commit -m "feat: ArticleService 添加 getPrevArticle/getNextArticle 方法声明"
```

---

### Task 4: 后端 - ArticleServiceImpl 实现相邻文章查询逻辑

**Files:**
- Modify: `blog-server/src/main/java/com/blog/service/impl/ArticleServiceImpl.java`

- [ ] **Step 1: 在 ArticleServiceImpl 中实现三级查找逻辑并修改 getArticleById**

在文件末尾 `checkUserAction` 方法之后添加以下内容，并修改 `getArticleById` 方法：

```java
@Override
public ArticleVO.ArticleNavVO getPrevArticle(Long currentId, Long categoryId, List<Long> tagIds) {
    // 1. 查找同分类上一篇
    if (categoryId != null) {
        ArticleVO.ArticleNavVO article = articleMapper.selectPrevByCategory(currentId, categoryId);
        if (article != null) {
            return article;
        }
    }
    
    // 2. 查找同标签上一篇
    if (tagIds != null && !tagIds.isEmpty()) {
        ArticleVO.ArticleNavVO article = articleMapper.selectPrevByTags(currentId, tagIds);
        if (article != null) {
            return article;
        }
    }
    
    // 3. 全局上一篇
    return articleMapper.selectPrevGlobal(currentId);
}

@Override
public ArticleVO.ArticleNavVO getNextArticle(Long currentId, Long categoryId, List<Long> tagIds) {
    // 1. 查找同分类下一篇
    if (categoryId != null) {
        ArticleVO.ArticleNavVO article = articleMapper.selectNextByCategory(currentId, categoryId);
        if (article != null) {
            return article;
        }
    }
    
    // 2. 查找同标签下一篇
    if (tagIds != null && !tagIds.isEmpty()) {
        ArticleVO.ArticleNavVO article = articleMapper.selectNextByTags(currentId, tagIds);
        if (article != null) {
            return article;
        }
    }
    
    // 3. 全局下一篇
    return articleMapper.selectNextGlobal(currentId);
}
```

同时修改 `getArticleById` 方法，在返回前添加相邻文章查询：

```java
@Override
public ArticleVO getArticleById(Long id) {
    Article article = articleMapper.selectById(id);
    if (article == null || article.getDeleted() == 1) {
        throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
    }
    
    // 增加浏览量
    articleMapper.incrementViewCount(id);
    article.setViewCount(article.getViewCount() + 1);
    
    ArticleVO vo = BeanCopyUtils.copy(article, ArticleVO.class);
    
    // 设置分类名称
    if (article.getCategoryId() != null) {
        Category category = categoryMapper.selectById(article.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }
    }
    
    // 设置作者信息
    if (article.getAuthorId() != null) {
        User author = userMapper.selectById(article.getAuthorId());
        if (author != null) {
            vo.setAuthorName(author.getNickname());
            vo.setAuthorAvatar(author.getAvatar());
        }
    }
    
    // 设置标签
    List<Long> tagIds = articleTagMapper.selectTagIdsByArticleId(id);
    if (!tagIds.isEmpty()) {
        List<Tag> tags = tagMapper.selectBatchIds(tagIds);
        vo.setTags(tags.stream()
                .map(tag -> BeanCopyUtils.copy(tag, TagVO.class))
                .collect(Collectors.toList()));
    }
    
    // 检查当前用户是否点赞/收藏
    Long userId = getCurrentUserId();
    if (userId != null) {
        vo.setIsLiked(checkUserAction(userId, id, 1));
        vo.setIsFavorited(checkUserAction(userId, id, 2));
    }
    
    // 设置上一篇和下一篇
    vo.setPrevArticle(getPrevArticle(id, article.getCategoryId(), tagIds));
    vo.setNextArticle(getNextArticle(id, article.getCategoryId(), tagIds));
    
    return vo;
}
```

- [ ] **Step 2: 验证编译通过**

Run: `cd blog-server && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add blog-server/src/main/java/com/blog/service/impl/ArticleServiceImpl.java
git commit -m "feat: ArticleServiceImpl 实现相邻文章三级查找逻辑"
```

---

### Task 5: 前端 - ArticleDetail.vue 添加导航组件

**Files:**
- Modify: `blog-web/src/views/portal/ArticleDetail.vue`

- [ ] **Step 1: 在模板中添加导航组件**

在 `<footer class="article-footer">` 之前，`</article>` 之前添加导航组件：

```vue
      <!-- 文章导航 -->
      <nav v-if="article.prevArticle || article.nextArticle" class="article-nav">
        <router-link
          v-if="article.prevArticle"
          :to="`/article/${article.prevArticle.id}`"
          class="nav-btn nav-prev"
        >
          <svg class="nav-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
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
          <svg class="nav-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M8.25 4.5l7.5 7.5-7.5 7.5" />
          </svg>
        </router-link>
        <div v-else class="nav-btn nav-empty"></div>
      </nav>
```

- [ ] **Step 2: 在样式部分添加导航组件样式**

在 `.article-footer` 样式之前添加：

```scss
// ========================================
// Article Navigation
// ========================================
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
  min-height: 60px;

  &:hover:not(.nav-empty) {
    background: var(--bg-hover);
    transform: translateY(-2px);
    box-shadow: var(--shadow-sm);
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
    pointer-events: none;
  }
}

.nav-arrow {
  width: 24px;
  height: 24px;
  stroke: var(--color-primary);
  flex-shrink: 0;
}

.nav-content {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  min-width: 0;
  flex: 1;
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

- [ ] **Step 3: 添加移动端响应式样式**

在 `@media (max-width: 768px)` 中添加：

```scss
  .article-nav {
    padding: var(--space-5) var(--space-6);
    flex-direction: column;
    gap: var(--space-3);
  }

  .nav-btn {
    &:not(.nav-empty) {
      min-height: 50px;
    }
  }

  .nav-empty {
    display: none;
  }
```

在 `@media (max-width: 480px)` 中添加：

```scss
  .article-nav {
    padding: var(--space-4) var(--space-5);
  }
```

- [ ] **Step 4: 验证前端编译通过**

Run: `cd blog-web && pnpm build`
Expected: 构建成功

- [ ] **Step 5: 提交**

```bash
git add blog-web/src/views/portal/ArticleDetail.vue
git commit -m "feat: ArticleDetail 添加上一篇/下一篇导航组件"
```

---

### Task 6: 集成测试

- [ ] **Step 1: 启动后端服务**

Run: `cd blog-server && mvn spring-boot:run`
Expected: 服务启动成功，端口 8080

- [ ] **Step 2: 启动前端服务**

Run: `cd blog-web && pnpm dev`
Expected: 开发服务器启动，端口 3000

- [ ] **Step 3: 手动测试导航功能**

测试用例：
1. 访问任意文章详情页，确认底部显示上一篇/下一篇导航
2. 点击上一篇，确认跳转到正确文章
3. 点击下一篇，确认跳转到正确文章
4. 访问第一篇文章，确认上一篇为空，下一篇有值
5. 访问最后一篇文章，确认上一篇有值，下一篇为空
6. 测试移动端响应式布局

- [ ] **Step 4: 最终提交（如有遗漏）**

```bash
git status
# 如有未提交的改动，执行提交
```

---

## 实现总结

| 任务 | 文件 | 改动 |
|------|------|------|
| Task 1 | ArticleVO.java | 添加 ArticleNavVO 内部类和 prevArticle/nextArticle 字段 |
| Task 2 | ArticleMapper.java | 添加 6 个相邻文章查询方法 |
| Task 3 | ArticleService.java | 添加 getPrevArticle/getNextArticle 方法声明 |
| Task 4 | ArticleServiceImpl.java | 实现三级查找逻辑，修改 getArticleById |
| Task 5 | ArticleDetail.vue | 添加导航组件模板和样式 |
| Task 6 | - | 集成测试 |
