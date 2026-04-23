# 博客系统性能优化实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 通过批量查询和 Redis 缓存优化，减少数据库查询次数，提升系统响应速度

**Architecture:** 在现有 Service 层添加批量查询逻辑，启用 Redis 缓存分类/标签/热门文章等高频访问数据

**Tech Stack:** Spring Boot 3, MyBatis Plus, Redis, Vue 3, Axios

---

## 文件结构

**后端改动：**
- `blog-server/src/main/java/com/blog/repository/mapper/ArticleTagMapper.java` - 新增批量查询方法
- `blog-server/src/main/java/com/blog/service/impl/ArticleServiceImpl.java` - 重构 convertToVOList 方法，添加热门/置顶文章缓存
- `blog-server/src/main/java/com/blog/service/impl/CategoryServiceImpl.java` - 添加分类列表缓存
- `blog-server/src/main/java/com/blog/service/impl/TagServiceImpl.java` - 添加标签列表缓存

**前端改动：**
- `blog-web/src/views/portal/Home.vue` - 已有加载状态，无需修改
- `blog-web/src/views/portal/ArticleDetail.vue` - 已有加载状态，无需修改

---

## Task 1: 批量查询文章标签关联

**Files:**
- Modify: `blog-server/src/main/java/com/blog/repository/mapper/ArticleTagMapper.java`

- [ ] **Step 1: 新增批量查询方法**

在 `ArticleTagMapper.java` 中添加批量查询方法：

```java
package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.ArticleTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ArticleTagMapper extends BaseMapper<ArticleTag> {
    
    @Select("SELECT tag_id FROM article_tag WHERE article_id = #{articleId}")
    List<Long> selectTagIdsByArticleId(@Param("articleId") Long articleId);
    
    void deleteByArticleId(@Param("articleId") Long articleId);

    /**
     * 批量查询多篇文章的标签ID
     * @param articleIds 文章ID列表
     * @return 文章标签关联列表
     */
    @Select("<script>" +
            "SELECT article_id, tag_id FROM article_tag " +
            "WHERE article_id IN " +
            "<foreach collection='articleIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<ArticleTag> selectByArticleIds(@Param("articleIds") List<Long> articleIds);
}
```

- [ ] **Step 2: 编译验证**

Run: `cd blog-server && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add blog-server/src/main/java/com/blog/repository/mapper/ArticleTagMapper.java
git commit -m "feat(mapper): add batch query method for article tags"
```

---

## Task 2: 重构 ArticleServiceImpl 的 convertToVOList 方法

**Files:**
- Modify: `blog-server/src/main/java/com/blog/service/impl/ArticleServiceImpl.java`

- [ ] **Step 1: 添加批量查询导入和方法**

在 `ArticleServiceImpl.java` 中，首先添加需要的导入：

```java
// 在文件顶部导入区域添加
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
```

- [ ] **Step 2: 重构 convertToVOList 方法**

替换原有的 `convertToVOList` 方法（约第 378-403 行）：

```java
private List<ArticleListVO> convertToVOList(List<Article> articles) {
    if (articles.isEmpty()) {
        return new ArrayList<>();
    }

    // 1. 收集所有 categoryId
    Set<Long> categoryIds = articles.stream()
            .map(Article::getCategoryId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    // 2. 批量查询分类
    Map<Long, Category> categoryMap = new HashMap<>();
    if (!categoryIds.isEmpty()) {
        List<Category> categories = categoryMapper.selectBatchIds(categoryIds);
        categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, c -> c));
    }

    // 3. 收集所有 articleId
    List<Long> articleIds = articles.stream()
            .map(Article::getId)
            .collect(Collectors.toList());

    // 4. 批量查询文章-标签关联
    List<ArticleTag> articleTags = articleTagMapper.selectByArticleIds(articleIds);
    
    // 5. 构建 articleId -> tagIds 映射
    Map<Long, List<Long>> articleTagMap = articleTags.stream()
            .collect(Collectors.groupingBy(
                    ArticleTag::getArticleId,
                    Collectors.mapping(ArticleTag::getTagId, Collectors.toList())
            ));

    // 6. 收集所有 tagId 并批量查询标签
    Set<Long> tagIds = articleTags.stream()
            .map(ArticleTag::getTagId)
            .collect(Collectors.toSet());
    
    Map<Long, Tag> tagMap = new HashMap<>();
    if (!tagIds.isEmpty()) {
        List<Tag> tags = tagMapper.selectBatchIds(tagIds);
        tagMap = tags.stream()
                .collect(Collectors.toMap(Tag::getId, t -> t));
    }

    // 7. 组装结果
    Map<Long, Category> finalCategoryMap = categoryMap;
    Map<Long, Tag> finalTagMap = tagMap;
    
    return articles.stream().map(article -> {
        ArticleListVO vo = BeanCopyUtils.copy(article, ArticleListVO.class);
        
        // 设置分类
        if (article.getCategoryId() != null) {
            Category category = finalCategoryMap.get(article.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }
        
        // 设置标签
        List<Long> tagIdList = articleTagMap.get(article.getId());
        if (tagIdList != null && !tagIdList.isEmpty()) {
            List<TagVO> tagVOList = tagIdList.stream()
                    .map(finalTagMap::get)
                    .filter(Objects::nonNull)
                    .map(tag -> BeanCopyUtils.copy(tag, TagVO.class))
                    .collect(Collectors.toList());
            vo.setTags(tagVOList);
        }
        
        if (article.getPublishTime() != null) {
            vo.setPublishTime(article.getPublishTime().toString());
        }
        
        return vo;
    }).collect(Collectors.toList());
}
```

- [ ] **Step 3: 添加 Objects 导入**

在文件顶部添加：

```java
import java.util.Objects;
```

- [ ] **Step 4: 编译验证**

Run: `cd blog-server && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: 提交**

```bash
git add blog-server/src/main/java/com/blog/service/impl/ArticleServiceImpl.java
git commit -m "perf(article): optimize N+1 queries in article list conversion"
```

---

## Task 3: 为分类列表添加 Redis 缓存

**Files:**
- Modify: `blog-server/src/main/java/com/blog/service/impl/CategoryServiceImpl.java`

- [ ] **Step 1: 添加 RedisUtils 依赖**

在 `CategoryServiceImpl.java` 中添加 RedisUtils 注入：

```java
package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.exception.BusinessException;
import com.blog.common.result.ErrorCode;
import com.blog.common.utils.BeanCopyUtils;
import com.blog.common.utils.RedisUtils;
import com.blog.domain.dto.CategoryDTO;
import com.blog.domain.entity.Category;
import com.blog.domain.vo.CategoryVO;
import com.blog.repository.mapper.CategoryMapper;
import com.blog.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final RedisUtils redisUtils;

    private static final String CACHE_KEY_CATEGORY_LIST = "blog:category:list";

    @Override
    @SuppressWarnings("unchecked")
    public List<CategoryVO> listAll() {
        // 尝试从缓存获取
        Object cached = redisUtils.get(CACHE_KEY_CATEGORY_LIST);
        if (cached != null) {
            return (List<CategoryVO>) cached;
        }

        // 查询数据库
        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getStatus, 1)
                        .orderByAsc(Category::getSort));

        List<CategoryVO> result = categories.stream().map(category -> {
            CategoryVO vo = BeanCopyUtils.copy(category, CategoryVO.class);
            vo.setArticleCount(categoryMapper.countArticles(category.getId()));
            return vo;
        }).collect(Collectors.toList());

        // 存入缓存（永久）
        redisUtils.set(CACHE_KEY_CATEGORY_LIST, result);

        return result;
    }

    @Override
    public Page<CategoryVO> pageCategory(int pageNum, int pageSize) {
        Page<Category> page = new Page<>(pageNum, pageSize);
        Page<Category> categoryPage = categoryMapper.selectPage(page,
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSort));

        Page<CategoryVO> voPage = new Page<>(pageNum, pageSize, categoryPage.getTotal());
        voPage.setRecords(categoryPage.getRecords().stream().map(category -> {
            CategoryVO vo = BeanCopyUtils.copy(category, CategoryVO.class);
            vo.setArticleCount(categoryMapper.countArticles(id));
            return vo;
        }).collect(Collectors.toList()));

        return voPage;
    }

    @Override
    public CategoryVO getById(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        CategoryVO vo = BeanCopyUtils.copy(category, CategoryVO.class);
        vo.setArticleCount(categoryMapper.countArticles(id));
        return vo;
    }

    @Override
    @Transactional
    public void saveOrUpdate(CategoryDTO dto) {
        Category category;
        if (dto.getId() == null) {
            category = new Category();
        } else {
            category = categoryMapper.selectById(dto.getId());
            if (category == null) {
                throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
            }
        }

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setSort(dto.getSort() != null ? dto.getSort() : 0);
        category.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);

        if (dto.getId() == null) {
            categoryMapper.insert(category);
        } else {
            categoryMapper.updateById(category);
        }

        // 清除缓存
        redisUtils.delete(CACHE_KEY_CATEGORY_LIST);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Integer count = categoryMapper.countArticles(id);
        if (count > 0) {
            throw new BusinessException("该分类下存在文章，无法删除");
        }
        categoryMapper.deleteById(id);
        
        // 清除缓存
        redisUtils.delete(CACHE_KEY_CATEGORY_LIST);
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `cd blog-server && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add blog-server/src/main/java/com/blog/service/impl/CategoryServiceImpl.java
git commit -m "feat(cache): add Redis cache for category list"
```

---

## Task 4: 为标签列表添加 Redis 缓存

**Files:**
- Modify: `blog-server/src/main/java/com/blog/service/impl/TagServiceImpl.java`

- [ ] **Step 1: 添加 RedisUtils 依赖和缓存逻辑**

替换 `TagServiceImpl.java` 的全部内容：

```java
package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.exception.BusinessException;
import com.blog.common.result.ErrorCode;
import com.blog.common.utils.BeanCopyUtils;
import com.blog.common.utils.RedisUtils;
import com.blog.domain.dto.TagDTO;
import com.blog.domain.entity.ArticleTag;
import com.blog.domain.entity.Tag;
import com.blog.domain.vo.TagVO;
import com.blog.repository.mapper.ArticleTagMapper;
import com.blog.repository.mapper.TagMapper;
import com.blog.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;
    private final RedisUtils redisUtils;

    private static final String CACHE_KEY_TAG_LIST = "blog:tag:list";

    @Override
    @SuppressWarnings("unchecked")
    public List<TagVO> listAll() {
        // 尝试从缓存获取
        Object cached = redisUtils.get(CACHE_KEY_TAG_LIST);
        if (cached != null) {
            return (List<TagVO>) cached;
        }

        // 查询数据库
        List<Tag> tags = tagMapper.selectList(null);
        List<TagVO> result = tags.stream()
                .map(tag -> BeanCopyUtils.copy(tag, TagVO.class))
                .collect(Collectors.toList());

        // 存入缓存（永久）
        redisUtils.set(CACHE_KEY_TAG_LIST, result);

        return result;
    }

    @Override
    public Page<TagVO> pageTag(int pageNum, int pageSize) {
        Page<Tag> page = new Page<>(pageNum, pageSize);
        Page<Tag> tagPage = tagMapper.selectPage(page, null);
        
        Page<TagVO> voPage = new Page<>(pageNum, pageSize, tagPage.getTotal());
        voPage.setRecords(tagPage.getRecords().stream()
                .map(tag -> BeanCopyUtils.copy(tag, TagVO.class))
                .collect(Collectors.toList()));
        
        return voPage;
    }

    @Override
    @Transactional
    public void saveOrUpdate(TagDTO dto) {
        Tag tag;
        if (dto.getId() == null) {
            tag = new Tag();
            // 检查标签名是否已存在
            Long count = tagMapper.selectCount(new LambdaQueryWrapper<Tag>()
                    .eq(Tag::getName, dto.getName()));
            if (count > 0) {
                throw new BusinessException("标签名称已存在");
            }
        } else {
            tag = tagMapper.selectById(dto.getId());
            if (tag == null) {
                throw new BusinessException(ErrorCode.TAG_NOT_FOUND);
            }
        }
        
        tag.setName(dto.getName());
        tag.setColor(dto.getColor() != null ? dto.getColor() : "#409EFF");
        
        if (dto.getId() == null) {
            tagMapper.insert(tag);
        } else {
            tagMapper.updateById(tag);
        }

        // 清除缓存
        redisUtils.delete(CACHE_KEY_TAG_LIST);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // 删除标签与文章的关联
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>()
                .eq(ArticleTag::getTagId, id));
        tagMapper.deleteById(id);

        // 清除缓存
        redisUtils.delete(CACHE_KEY_TAG_LIST);
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `cd blog-server && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add blog-server/src/main/java/com/blog/service/impl/TagServiceImpl.java
git commit -m "feat(cache): add Redis cache for tag list"
```

---

## Task 5: 为热门文章和置顶文章添加缓存

**Files:**
- Modify: `blog-server/src/main/java/com/blog/service/impl/ArticleServiceImpl.java`

- [ ] **Step 1: 添加 RedisUtils 依赖**

在 `ArticleServiceImpl.java` 的构造函数中添加 `RedisUtils`：

```java
// 在类字段声明区域添加
private final RedisUtils redisUtils;

// 在 @RequiredArgsConstructor 注解下，Lombok 会自动生成构造函数
// 只需确保字段已声明即可
```

同时添加缓存 key 常量：

```java
// 在类中添加常量
private static final String CACHE_KEY_HOT_ARTICLES = "blog:article:hot:";
private static final String CACHE_KEY_TOP_ARTICLES = "blog:article:top";
```

- [ ] **Step 2: 修改 getHotArticles 方法**

替换 `getHotArticles` 方法（约第 149-158 行）：

```java
@Override
@SuppressWarnings("unchecked")
public List<ArticleListVO> getHotArticles(int limit) {
    String cacheKey = CACHE_KEY_HOT_ARTICLES + limit;
    
    // 尝试从缓存获取
    Object cached = redisUtils.get(cacheKey);
    if (cached != null) {
        return (List<ArticleListVO>) cached;
    }

    // 查询数据库
    LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(Article::getDeleted, 0)
           .eq(Article::getStatus, 1)
           .orderByDesc(Article::getViewCount)
           .last("LIMIT " + limit);
    
    List<Article> articles = articleMapper.selectList(wrapper);
    List<ArticleListVO> result = convertToVOList(articles);

    // 存入缓存，5分钟过期
    redisUtils.set(cacheKey, result, 300);

    return result;
}
```

- [ ] **Step 3: 修改 getTopArticles 方法**

替换 `getTopArticles` 方法（约第 161-170 行）：

```java
@Override
@SuppressWarnings("unchecked")
public List<ArticleListVO> getTopArticles() {
    // 尝试从缓存获取
    Object cached = redisUtils.get(CACHE_KEY_TOP_ARTICLES);
    if (cached != null) {
        return (List<ArticleListVO>) cached;
    }

    // 查询数据库
    LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(Article::getDeleted, 0)
           .eq(Article::getStatus, 1)
           .eq(Article::getIsTop, 1)
           .orderByDesc(Article::getPublishTime);
    
    List<Article> articles = articleMapper.selectList(wrapper);
    List<ArticleListVO> result = convertToVOList(articles);

    // 存入缓存，10分钟过期
    redisUtils.set(CACHE_KEY_TOP_ARTICLES, result, 600);

    return result;
}
```

- [ ] **Step 4: 添加 RedisUtils 导入**

在文件顶部确保有以下导入：

```java
import com.blog.common.utils.RedisUtils;
```

- [ ] **Step 5: 更新字段声明**

确保 `ArticleServiceImpl` 类的开头包含：

```java
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;
    private final UserMapper userMapper;
    private final UserActionMapper userActionMapper;
    private final SeriesMapper seriesMapper;
    private final SeriesArticleMapper seriesArticleMapper;
    private final RedisUtils redisUtils;

    private static final String CACHE_KEY_HOT_ARTICLES = "blog:article:hot:";
    private static final String CACHE_KEY_TOP_ARTICLES = "blog:article:top";
    
    // ... 其余代码
}
```

- [ ] **Step 6: 编译验证**

Run: `cd blog-server && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 7: 提交**

```bash
git add blog-server/src/main/java/com/blog/service/impl/ArticleServiceImpl.java
git commit -m "feat(cache): add Redis cache for hot and top articles"
```

---

## Task 6: 验证和测试

**Files:**
- 无文件修改，仅测试验证

- [ ] **Step 1: 启动服务**

确保 Docker 环境运行：

```bash
make up
# 或
docker-compose up -d
```

Run: `make up`
Expected: 所有服务正常启动

- [ ] **Step 2: 验证缓存效果**

访问以下接口验证功能正常：
- GET `http://localhost:8080/api/portal/articles` - 文章列表
- GET `http://localhost:8080/api/portal/articles/hot` - 热门文章
- GET `http://localhost:8080/api/portal/articles/top` - 置顶文章
- GET `http://localhost:8080/api/portal/categories` - 分类列表
- GET `http://localhost:8080/api/portal/tags` - 标签列表

- [ ] **Step 3: 检查 Redis 缓存**

连接 Redis 检查缓存 key：

```bash
docker exec -it blog-redis redis-cli
> KEYS blog:*
```

Expected: 看到以下 keys：
- `blog:category:list`
- `blog:tag:list`
- `blog:article:hot:5`
- `blog:article:top`

- [ ] **Step 4: 最终提交**

如果所有测试通过，推送代码：

```bash
git push origin master
```

---

## 测试要点

- [ ] 分页列表数据正确，分类、标签显示正常
- [ ] 缓存命中时数据正确，缓存失效后数据更新正确
- [ ] 文章详情页所有字段显示正常
- [ ] 上一篇/下一篇导航正确
- [ ] 点赞/收藏状态正确
- [ ] 管理后台更新分类/标签后缓存正确清除
