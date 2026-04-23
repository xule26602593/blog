# 博客系统性能优化设计

## 背景

项目是一个 Spring Boot 3 + Vue 3 的个人博客系统，当前运行正常但存在以下可优化点：
- Redis 已配置但完全未使用
- 存在 N+1 查询问题
- 文章详情查询次数过多
- 前端缺少加载状态和请求取消机制

## 优化目标

- 减少数据库查询次数
- 启用 Redis 缓存减少重复查询
- 提升前端用户体验

---

## 一、N+1 查询问题修复

### 问题位置

`ArticleServiceImpl.convertToVOList()` 方法（约第 378-403 行）

### 当前问题

每篇文章单独查询：
- 分类信息：`categoryMapper.selectById()`
- 标签ID：`articleTagMapper.selectTagIdsByArticleId()`
- 标签详情：`tagMapper.selectBatchIds()`

一页 10 篇文章产生 30+ 次查询。

### 解决方案

1. **批量查询分类**
   - 收集所有 article 的 categoryId
   - 一次性 `categoryMapper.selectBatchIds(categoryIds)`
   - 结果存入 Map<Long, Category> 供后续使用

2. **批量查询文章-标签关联**
   - 新增 Mapper 方法：`selectTagIdsByArticleIds(List<Long> articleIds)`
   - 返回 `Map<Long, List<Long>>` (articleId -> tagIds)

3. **批量查询标签**
   - 收集所有 tagId
   - 一次性 `tagMapper.selectBatchIds(tagIds)`
   - 结果存入 Map<Long, Tag>

### 改动文件

- `ArticleServiceImpl.java` - 重构 `convertToVOList()` 方法
- `ArticleTagMapper.java` - 新增批量查询方法
- `ArticleTagMapper.xml` - 新增 SQL（如需要）

---

## 二、Redis 缓存策略

### 缓存场景

| 数据 | 缓存Key | TTL | 失效策略 |
|------|---------|-----|----------|
| 分类列表 | `blog:category:list` | 永久 | Admin 更新/删除时清除 |
| 标签列表 | `blog:tag:list` | 永久 | Admin 更新/删除时清除 |
| 热门文章 | `blog:article:hot:{limit}` | 5分钟 | 自然过期 |
| 置顶文章 | `blog:article:top` | 10分钟 | 自然过期 |

### 实现方式

在 Service 层调用 `RedisUtils`：

```java
// 查询时
String cacheKey = "blog:category:list";
Object cached = redisUtils.get(cacheKey);
if (cached != null) {
    return (List<CategoryVO>) cached;
}
// 查数据库
List<CategoryVO> result = ...;
redisUtils.set(cacheKey, result);
return result;

// 更新时清除缓存
redisUtils.delete("blog:category:list");
```

### 改动文件

- `CategoryServiceImpl.java` - 添加缓存逻辑
- `TagServiceImpl.java` - 添加缓存逻辑
- `ArticleServiceImpl.java` - 热门/置顶文章缓存

---

## 三、文章详情查询优化

### 当前问题

`getArticleById()` 执行 7+ 次查询：
1. 文章基本信息
2. 增加浏览量
3. 分类信息
4. 作者信息
5. 标签ID列表
6. 标签详情
7. 用户操作检查（点赞/收藏）
8. 所属系列
9. 上一篇/下一篇

### 优化方案

1. **合并文章+分类+作者查询**
   - 新增 Mapper 方法 `selectArticleDetailById()`
   - 使用 JOIN 一次查询获取文章、分类名、作者信息

2. **用户操作批量检查**
   - 新增 Mapper 方法 `selectUserActionsByArticle(userId, articleId)`
   - 一次查询返回用户对该文章的所有操作类型

### 优化后查询次数

从 7+ 次减少到 4-5 次。

### 改动文件

- `ArticleMapper.java` - 新增查询方法
- `ArticleServiceImpl.java` - 重构 `getArticleById()`

---

## 四、前端优化

### 4.1 骨架屏

在以下页面添加加载占位：
- 文章列表页 (`Home.vue`)
- 文章详情页 (`ArticleDetail.vue`)

使用 Vant 的 `Skeleton` 组件。

### 4.2 请求取消机制

在 `request.js` 中添加：
- 使用 `AbortController` 管理请求
- 路由切换时取消未完成请求

### 4.3 全局数据预加载

分类、标签在应用启动时加载到 Pinia Store，避免每个页面重复请求。

### 改动文件

- `request.js` - 添加请求取消
- `Home.vue` - 添加骨架屏
- `ArticleDetail.vue` - 添加骨架屏
- `stores/app.js` - 预加载分类/标签

---

## 五、实施优先级

| 优先级 | 任务 | 改动文件数 | 风险 |
|--------|------|------------|------|
| P0 | 修复 N+1 查询 | 2-3 | 低 |
| P1 | 分类/标签缓存 | 2 | 低 |
| P2 | 热门/置顶文章缓存 | 1 | 低 |
| P3 | 文章详情查询优化 | 2 | 中 |
| P4 | 前端骨架屏 | 3 | 低 |
| P5 | 请求取消机制 | 1 | 中 |

---

## 六、测试要点

- [ ] 分页列表数据正确，分类、标签显示正常
- [ ] 缓存命中时数据正确，缓存失效后数据更新正确
- [ ] 文章详情页所有字段显示正常
- [ ] 上一篇/下一篇导航正确
- [ ] 点赞/收藏状态正确
- [ ] 前端骨架屏显示正常
