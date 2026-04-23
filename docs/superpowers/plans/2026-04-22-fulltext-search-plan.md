# 文章全文检索实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为博客系统实现基于 MySQL ngram 全文索引的文章全文检索功能，支持中文分词、关键词高亮、搜索建议和搜索历史。

**Architecture:** 在现有 Spring Boot 架构上扩展，创建独立的 SearchService 处理搜索业务，使用 MySQL FULLTEXT 索引替代现有的 LIKE 查询，应用层实现关键词高亮。

**Tech Stack:** Spring Boot 3.2, MyBatis Plus, MySQL 8.0 (ngram), Vue 3, Vant 4

---

## 文件结构

### 新建文件

| 文件 | 职责 |
|------|------|
| `blog-server/src/main/java/com/blog/controller/portal/SearchController.java` | 搜索 API 入口 |
| `blog-server/src/main/java/com/blog/service/SearchService.java` | 搜索服务接口 |
| `blog-server/src/main/java/com/blog/service/impl/SearchServiceImpl.java` | 搜索服务实现 |
| `blog-server/src/main/java/com/blog/repository/mapper/SearchHistoryMapper.java` | 搜索历史 Mapper |
| `blog-server/src/main/java/com/blog/repository/mapper/SearchSuggestionMapper.java` | 搜索建议 Mapper |
| `blog-server/src/main/java/com/blog/domain/entity/SearchHistory.java` | 搜索历史实体 |
| `blog-server/src/main/java/com/blog/domain/entity/SearchSuggestion.java` | 搜索建议实体 |
| `blog-server/src/main/java/com/blog/domain/dto/SearchDTO.java` | 搜索请求参数 |
| `blog-server/src/main/java/com/blog/domain/vo/SearchVO.java` | 搜索结果 VO |
| `blog-server/src/main/java/com/blog/common/utils/HighlightUtil.java` | 关键词高亮工具 |
| `blog-server/src/main/resources/mapper/SearchMapper.xml` | 搜索相关 SQL |
| `blog-web/src/api/search.js` | 前端搜索 API |
| `blog-web/src/views/portal/Search.vue` | 搜索结果页 |
| `blog-web/src/components/SearchBox.vue` | 搜索框组件 |

### 修改文件

| 文件 | 修改内容 |
|------|---------|
| `blog-server/src/main/resources/db/schema.sql` | 添加全文索引、搜索历史表、搜索建议表 |
| `blog-server/src/main/resources/application.yml` | 添加搜索相关配置 |

---

## Task 1: 数据库表结构与全文索引

**Files:**
- Modify: `blog-server/src/main/resources/db/schema.sql`

- [ ] **Step 1: 添加全文索引 SQL**

在 `schema.sql` 文件末尾添加：

```sql
-- =============================================
-- 全文检索相关表和索引
-- =============================================

-- 为 article 表添加全文索引 (ngram 中文分词)
CREATE FULLTEXT INDEX idx_ft_article 
ON article(title, content) WITH PARSER ngram;

-- =============================================
-- 15. 搜索历史表
-- =============================================
DROP TABLE IF EXISTS `search_history`;
CREATE TABLE `search_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID(登录用户)',
    `keyword` VARCHAR(100) NOT NULL COMMENT '搜索关键词',
    `result_count` INT DEFAULT 0 COMMENT '搜索结果数量',
    `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '搜索时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_time` (`user_id`, `create_time`),
    KEY `idx_keyword` (`keyword`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索历史表';

-- =============================================
-- 16. 搜索建议表
-- =============================================
DROP TABLE IF EXISTS `search_suggestion`;
CREATE TABLE `search_suggestion` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `keyword` VARCHAR(100) NOT NULL COMMENT '关键词',
    `search_count` INT DEFAULT 1 COMMENT '搜索次数',
    `last_search_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最后搜索时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_keyword` (`keyword`),
    KEY `idx_count` (`search_count` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索建议表';
```

- [ ] **Step 2: 提交数据库变更**

```bash
git add blog-server/src/main/resources/db/schema.sql
git commit -m "feat(search): add fulltext index and search tables"
```

---

## Task 2: 搜索相关实体类

**Files:**
- Create: `blog-server/src/main/java/com/blog/domain/entity/SearchHistory.java`
- Create: `blog-server/src/main/java/com/blog/domain/entity/SearchSuggestion.java`

- [ ] **Step 1: 创建 SearchHistory 实体**

```java
package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("search_history")
public class SearchHistory implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String keyword;

    private Integer resultCount;

    private String ipAddress;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
```

- [ ] **Step 2: 创建 SearchSuggestion 实体**

```java
package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("search_suggestion")
public class SearchSuggestion implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String keyword;

    private Integer searchCount;

    private LocalDateTime lastSearchTime;
}
```

- [ ] **Step 3: 提交实体类**

```bash
git add blog-server/src/main/java/com/blog/domain/entity/SearchHistory.java
git add blog-server/src/main/java/com/blog/domain/entity/SearchSuggestion.java
git commit -m "feat(search): add SearchHistory and SearchSuggestion entities"
```

---

## Task 3: 搜索 DTO 和 VO

**Files:**
- Create: `blog-server/src/main/java/com/blog/domain/dto/SearchDTO.java`
- Create: `blog-server/src/main/java/com/blog/domain/vo/SearchVO.java`

- [ ] **Step 1: 创建 SearchDTO**

```java
package com.blog.domain.dto;

import lombok.Data;

@Data
public class SearchDTO {

    private String keyword;

    private Integer page = 1;

    private Integer size = 10;

    private String sortBy = "relevance"; // relevance 或 time
}
```

- [ ] **Step 2: 创建 SearchVO**

```java
package com.blog.domain.vo;

import lombok.Data;

@Data
public class SearchVO {

    private Long id;

    private String title;

    private String summary;

    private String contentHighlight;

    private String coverImage;

    private String categoryName;

    private String publishTime;

    private Double score;
}
```

- [ ] **Step 3: 提交 DTO 和 VO**

```bash
git add blog-server/src/main/java/com/blog/domain/dto/SearchDTO.java
git add blog-server/src/main/java/com/blog/domain/vo/SearchVO.java
git commit -m "feat(search): add SearchDTO and SearchVO"
```

---

## Task 4: 关键词高亮工具类

**Files:**
- Create: `blog-server/src/main/java/com/blog/common/utils/HighlightUtil.java`

- [ ] **Step 1: 创建 HighlightUtil**

```java
package com.blog.common.utils;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

public class HighlightUtil {

    private static final String HIGHLIGHT_PREFIX = "<em class=\"highlight\">";
    private static final String HIGHLIGHT_SUFFIX = "</em>";

    /**
     * 高亮处理关键词
     *
     * @param text      原文本
     * @param keyword   关键词
     * @param maxLength 返回最大长度(截取关键词上下文)
     * @return 高亮后的文本片段
     */
    public static String highlight(String text, String keyword, int maxLength) {
        if (!StringUtils.hasText(text) || !StringUtils.hasText(keyword)) {
            return truncate(text, maxLength);
        }

        // 查找关键词位置(忽略大小写)
        int index = text.toLowerCase().indexOf(keyword.toLowerCase());
        if (index == -1) {
            return truncate(text, maxLength);
        }

        // 截取关键词上下文
        String snippet = extractSnippet(text, index, maxLength);

        // 高亮关键词(忽略大小写)
        return snippet.replaceAll(
                "(?i)(" + Pattern.quote(keyword) + ")",
                HIGHLIGHT_PREFIX + "$1" + HIGHLIGHT_SUFFIX
        );
    }

    /**
     * 仅高亮不截取
     */
    public static String highlightOnly(String text, String keyword) {
        if (!StringUtils.hasText(text) || !StringUtils.hasText(keyword)) {
            return text;
        }
        return text.replaceAll(
                "(?i)(" + Pattern.quote(keyword) + ")",
                HIGHLIGHT_PREFIX + "$1" + HIGHLIGHT_SUFFIX
        );
    }

    private static String extractSnippet(String text, int index, int maxLength) {
        int start = Math.max(0, index - maxLength / 2);
        int end = Math.min(text.length(), start + maxLength);

        // 调整 start 避免截断中文字符
        if (start > 0) {
            start = Math.max(0, start - 1);
        }

        String snippet = text.substring(start, end);
        if (start > 0) {
            snippet = "..." + snippet;
        }
        if (end < text.length()) {
            snippet = snippet + "...";
        }
        return snippet;
    }

    private static String truncate(String text, int maxLength) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}
```

- [ ] **Step 2: 提交工具类**

```bash
git add blog-server/src/main/java/com/blog/common/utils/HighlightUtil.java
git commit -m "feat(search): add HighlightUtil for keyword highlighting"
```

---

## Task 5: Mapper 层

**Files:**
- Create: `blog-server/src/main/java/com/blog/repository/mapper/SearchHistoryMapper.java`
- Create: `blog-server/src/main/java/com/blog/repository/mapper/SearchSuggestionMapper.java`
- Create: `blog-server/src/main/resources/mapper/SearchMapper.xml`

- [ ] **Step 1: 创建 SearchHistoryMapper**

```java
package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.SearchHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SearchHistoryMapper extends BaseMapper<SearchHistory> {

    @Select("SELECT DISTINCT keyword FROM search_history WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{limit}")
    List<String> selectUserHistory(@Param("userId") Long userId, @Param("limit") int limit);
}
```

- [ ] **Step 2: 创建 SearchSuggestionMapper**

```java
package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.SearchSuggestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SearchSuggestionMapper extends BaseMapper<SearchSuggestion> {

    @Select("SELECT keyword FROM search_suggestion WHERE keyword LIKE CONCAT(#{prefix}, '%') ORDER BY search_count DESC, last_search_time DESC LIMIT #{limit}")
    List<String> selectByPrefix(@Param("prefix") String prefix, @Param("limit") int limit);

    @Select("SELECT keyword FROM search_suggestion ORDER BY search_count DESC LIMIT #{limit}")
    List<String> selectHotKeywords(@Param("limit") int limit);

    @Update("INSERT INTO search_suggestion (keyword, search_count, last_search_time) VALUES (#{keyword}, 1, NOW()) ON DUPLICATE KEY UPDATE search_count = search_count + 1, last_search_time = NOW()")
    int upsertKeyword(@Param("keyword") String keyword);
}
```

- [ ] **Step 3: 创建 SearchMapper.xml (全文检索 SQL)**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.blog.repository.mapper.ArticleMapper">

    <select id="searchFulltext" resultType="com.blog.domain.vo.SearchVO">
        SELECT 
            a.id,
            a.title,
            a.summary,
            a.content AS contentHighlight,
            a.cover_image AS coverImage,
            c.name AS categoryName,
            DATE_FORMAT(a.publish_time, '%Y-%m-%d %H:%i:%s') AS publishTime,
            MATCH(a.title, a.content) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE) AS score
        FROM article a
        LEFT JOIN category c ON a.category_id = c.id
        WHERE a.status = 1
          AND a.deleted = 0
          AND MATCH(a.title, a.content) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE)
        ORDER BY 
            <choose>
                <when test="sortBy == 'time'">
                    a.publish_time DESC
                </when>
                <otherwise>
                    score DESC
                </otherwise>
            </choose>
        LIMIT #{offset}, #{size}
    </select>

    <select id="searchFulltextCount" resultType="java.lang.Long">
        SELECT COUNT(*)
        FROM article a
        WHERE a.status = 1
          AND a.deleted = 0
          AND MATCH(a.title, a.content) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE)
    </select>

</mapper>
```

- [ ] **Step 4: 更新 ArticleMapper 接口添加方法声明**

在 `ArticleMapper.java` 中添加：

```java
// 在 ArticleMapper 接口中添加以下方法

List<SearchVO> searchFulltext(@Param("keyword") String keyword, 
                              @Param("sortBy") String sortBy,
                              @Param("offset") int offset, 
                              @Param("size") int size);

Long searchFulltextCount(@Param("keyword") String keyword);
```

- [ ] **Step 5: 提交 Mapper 层**

```bash
git add blog-server/src/main/java/com/blog/repository/mapper/SearchHistoryMapper.java
git add blog-server/src/main/java/com/blog/repository/mapper/SearchSuggestionMapper.java
git add blog-server/src/main/resources/mapper/SearchMapper.xml
git add blog-server/src/main/java/com/blog/repository/mapper/ArticleMapper.java
git commit -m "feat(search): add search mappers and fulltext SQL"
```

---

## Task 6: 搜索服务层

**Files:**
- Create: `blog-server/src/main/java/com/blog/service/SearchService.java`
- Create: `blog-server/src/main/java/com/blog/service/impl/SearchServiceImpl.java`

- [ ] **Step 1: 创建 SearchService 接口**

```java
package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.domain.dto.SearchDTO;
import com.blog.domain.vo.SearchVO;

import java.util.List;

public interface SearchService {

    /**
     * 全文检索文章
     */
    Page<SearchVO> search(SearchDTO dto, String ipAddress);

    /**
     * 获取搜索建议
     */
    List<String> getSuggestions(String prefix);

    /**
     * 获取搜索历史
     */
    List<String> getHistory();

    /**
     * 获取热门搜索词
     */
    List<String> getHotKeywords();
}
```

- [ ] **Step 2: 创建 SearchServiceImpl**

```java
package com.blog.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.utils.HighlightUtil;
import com.blog.common.utils.IpUtils;
import com.blog.domain.dto.SearchDTO;
import com.blog.domain.entity.SearchHistory;
import com.blog.domain.vo.SearchVO;
import com.blog.repository.mapper.ArticleMapper;
import com.blog.repository.mapper.SearchHistoryMapper;
import com.blog.repository.mapper.SearchSuggestionMapper;
import com.blog.security.LoginUser;
import com.blog.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ArticleMapper articleMapper;
    private final SearchHistoryMapper searchHistoryMapper;
    private final SearchSuggestionMapper searchSuggestionMapper;

    @Value("${search.highlight.content-length:200}")
    private int highlightContentLength;

    @Value("${search.suggestion.max-size:10}")
    private int suggestionMaxSize;

    @Override
    @Transactional
    public Page<SearchVO> search(SearchDTO dto, String ipAddress) {
        String keyword = dto.getKeyword();
        if (!StringUtils.hasText(keyword)) {
            return new Page<>(dto.getPage(), dto.getSize(), 0);
        }

        // 计算分页偏移
        int offset = (dto.getPage() - 1) * dto.getSize();

        // 执行全文检索
        List<SearchVO> results = articleMapper.searchFulltext(
                keyword, dto.getSortBy(), offset, dto.getSize());
        Long total = articleMapper.searchFulltextCount(keyword);

        // 高亮处理
        for (SearchVO vo : results) {
            vo.setTitle(HighlightUtil.highlightOnly(vo.getTitle(), keyword));
            vo.setContentHighlight(HighlightUtil.highlight(
                    vo.getContentHighlight(), keyword, highlightContentLength));
        }

        // 记录搜索历史和更新建议统计
        saveSearchHistory(keyword, results.size(), ipAddress);
        searchSuggestionMapper.upsertKeyword(keyword);

        // 构建分页结果
        Page<SearchVO> page = new Page<>(dto.getPage(), dto.getSize(), total);
        page.setRecords(results);
        return page;
    }

    @Override
    public List<String> getSuggestions(String prefix) {
        if (!StringUtils.hasText(prefix)) {
            return getHotKeywords();
        }
        return searchSuggestionMapper.selectByPrefix(prefix, suggestionMaxSize);
    }

    @Override
    public List<String> getHistory() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return List.of();
        }
        return searchHistoryMapper.selectUserHistory(userId, suggestionMaxSize);
    }

    @Override
    public List<String> getHotKeywords() {
        return searchSuggestionMapper.selectHotKeywords(suggestionMaxSize);
    }

    private void saveSearchHistory(String keyword, int resultCount, String ipAddress) {
        Long userId = getCurrentUserId();
        
        SearchHistory history = new SearchHistory();
        history.setUserId(userId);
        history.setKeyword(keyword);
        history.setResultCount(resultCount);
        history.setIpAddress(ipAddress);
        
        searchHistoryMapper.insert(history);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return ((LoginUser) authentication.getPrincipal()).getUserId();
        }
        return null;
    }
}
```

- [ ] **Step 3: 提交服务层**

```bash
git add blog-server/src/main/java/com/blog/service/SearchService.java
git add blog-server/src/main/java/com/blog/service/impl/SearchServiceImpl.java
git commit -m "feat(search): add SearchService implementation"
```

---

## Task 7: 搜索 Controller

**Files:**
- Create: `blog-server/src/main/java/com/blog/controller/portal/SearchController.java`

- [ ] **Step 1: 创建 SearchController**

```java
package com.blog.controller.portal;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.Result;
import com.blog.common.utils.IpUtils;
import com.blog.domain.dto.SearchDTO;
import com.blog.domain.vo.SearchVO;
import com.blog.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "搜索接口")
@RestController
@RequestMapping("/api/portal/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "全文检索文章")
    @GetMapping
    public Result<Page<SearchVO>> search(SearchDTO dto, HttpServletRequest request) {
        String ipAddress = IpUtils.getIpAddress(request);
        return Result.success(searchService.search(dto, ipAddress));
    }

    @Operation(summary = "获取搜索建议")
    @GetMapping("/suggestions")
    public Result<List<String>> getSuggestions(@RequestParam(required = false) String prefix) {
        return Result.success(searchService.getSuggestions(prefix));
    }

    @Operation(summary = "获取搜索历史")
    @GetMapping("/history")
    public Result<List<String>> getHistory() {
        return Result.success(searchService.getHistory());
    }

    @Operation(summary = "获取热门搜索词")
    @GetMapping("/hot")
    public Result<List<String>> getHotKeywords() {
        return Result.success(searchService.getHotKeywords());
    }
}
```

- [ ] **Step 2: 提交 Controller**

```bash
git add blog-server/src/main/java/com/blog/controller/portal/SearchController.java
git commit -m "feat(search): add SearchController with fulltext search API"
```

---

## Task 8: 应用配置

**Files:**
- Modify: `blog-server/src/main/resources/application.yml`

- [ ] **Step 1: 添加搜索配置**

在 `application.yml` 末尾添加：

```yaml
# Search Configuration
search:
  highlight:
    content-length: 200
  history:
    max-size: 10
    expire-days: 30
  suggestion:
    max-size: 10
```

- [ ] **Step 2: 提交配置**

```bash
git add blog-server/src/main/resources/application.yml
git commit -m "feat(search): add search configuration"
```

---

## Task 9: 前端 API 模块

**Files:**
- Create: `blog-web/src/api/search.js`

- [ ] **Step 1: 创建搜索 API**

```javascript
import request from '@/utils/request'

export default {
  // 全文搜索
  search(params) {
    return request.get('/portal/search', { params })
  },

  // 获取搜索建议
  getSuggestions(prefix) {
    return request.get('/portal/search/suggestions', {
      params: { prefix }
    })
  },

  // 获取搜索历史
  getHistory() {
    return request.get('/portal/search/history')
  },

  // 获取热门搜索
  getHotKeywords() {
    return request.get('/portal/search/hot')
  }
}
```

- [ ] **Step 2: 提交前端 API**

```bash
git add blog-web/src/api/search.js
git commit -m "feat(search): add frontend search API module"
```

---

## Task 10: 搜索结果页

**Files:**
- Create: `blog-web/src/views/portal/Search.vue`

- [ ] **Step 1: 创建搜索结果页**

```vue
<template>
  <div class="search-page">
    <!-- 搜索框 -->
    <div class="search-header">
      <van-search
        v-model="keyword"
        placeholder="请输入搜索关键词"
        show-action
        @search="onSearch"
        @cancel="onCancel"
      />
      <!-- 排序选择 -->
      <div class="sort-bar" v-if="total > 0">
        <span>找到 {{ total }} 篇文章</span>
        <van-dropdown-menu>
          <van-dropdown-item v-model="sortBy" :options="sortOptions" @change="onSearch" />
        </van-dropdown-menu>
      </div>
    </div>

    <!-- 搜索结果列表 -->
    <van-list
      v-model:loading="loading"
      :finished="finished"
      finished-text="没有更多了"
      @load="onLoad"
    >
      <div
        class="search-item"
        v-for="item in list"
        :key="item.id"
        @click="goDetail(item.id)"
      >
        <h3 class="title" v-html="item.title"></h3>
        <p class="content" v-html="item.contentHighlight"></p>
        <div class="meta">
          <span>{{ item.categoryName }}</span>
          <span>{{ item.publishTime }}</span>
        </div>
      </div>
    </van-list>

    <!-- 空状态 -->
    <van-empty v-if="!loading && list.length === 0 && keyword" description="未找到相关文章" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import searchApi from '@/api/search'

const route = useRoute()
const router = useRouter()

const keyword = ref('')
const sortBy = ref('relevance')
const sortOptions = [
  { text: '相关度排序', value: 'relevance' },
  { text: '时间排序', value: 'time' }
]

const list = ref([])
const loading = ref(false)
const finished = ref(false)
const total = ref(0)
const page = ref(1)
const size = 10

onMounted(() => {
  if (route.query.keyword) {
    keyword.value = route.query.keyword
    onSearch()
  }
})

const onSearch = () => {
  if (!keyword.value.trim()) return
  
  page.value = 1
  list.value = []
  finished.value = false
  onLoad()
}

const onLoad = async () => {
  try {
    const res = await searchApi.search({
      keyword: keyword.value,
      page: page.value,
      size,
      sortBy: sortBy.value
    })
    
    if (page.value === 1) {
      total.value = res.data.total
    }
    
    list.value.push(...res.data.records)
    loading.value = false
    
    if (list.value.length >= res.data.total) {
      finished.value = true
    } else {
      page.value++
    }
  } catch (e) {
    loading.value = false
    finished.value = true
  }
}

const onCancel = () => {
  router.back()
}

const goDetail = (id) => {
  router.push(`/article/${id}`)
}
</script>

<style scoped>
.search-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.search-header {
  background: #fff;
  padding-bottom: 10px;
}

.sort-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  font-size: 14px;
  color: #666;
}

.search-item {
  background: #fff;
  margin: 10px;
  padding: 15px;
  border-radius: 8px;
}

.title {
  font-size: 16px;
  margin: 0 0 10px 0;
  color: #333;
}

.title :deep(em.highlight) {
  color: #ee0a24;
  font-style: normal;
}

.content {
  font-size: 14px;
  color: #666;
  margin: 0 0 10px 0;
  line-height: 1.6;
}

.content :deep(em.highlight) {
  color: #ee0a24;
  font-style: normal;
}

.meta {
  font-size: 12px;
  color: #999;
  display: flex;
  gap: 15px;
}
</style>
```

- [ ] **Step 2: 提交搜索结果页**

```bash
git add blog-web/src/views/portal/Search.vue
git commit -m "feat(search): add search result page"
```

---

## Task 11: 搜索框组件

**Files:**
- Create: `blog-web/src/components/SearchBox.vue`

- [ ] **Step 1: 创建搜索框组件**

```vue
<template>
  <div class="search-box">
    <van-search
      v-model="keyword"
      :placeholder="placeholder"
      @focus="onFocus"
      @blur="onBlur"
      @update:model-value="onInput"
      @search="onSearch"
    />
    
    <!-- 搜索建议下拉 -->
    <div class="suggestions" v-show="showSuggestions && suggestions.length > 0">
      <div
        class="suggestion-item"
        v-for="item in suggestions"
        :key="item"
        @click="selectSuggestion(item)"
      >
        {{ item }}
      </div>
    </div>
    
    <!-- 搜索历史和热门 -->
    <div class="search-panel" v-show="showPanel">
      <div class="panel-section" v-if="history.length > 0">
        <div class="section-header">
          <span>搜索历史</span>
          <van-icon name="delete-o" @click="clearHistory" />
        </div>
        <div class="tags">
          <van-tag v-for="item in history" :key="item" @click="selectSuggestion(item)">
            {{ item }}
          </van-tag>
        </div>
      </div>
      
      <div class="panel-section" v-if="hotKeywords.length > 0">
        <div class="section-header">
          <span>热门搜索</span>
        </div>
        <div class="tags">
          <van-tag v-for="item in hotKeywords" :key="item" @click="selectSuggestion(item)">
            {{ item }}
          </van-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import searchApi from '@/api/search'

const props = defineProps({
  placeholder: {
    type: String,
    default: '搜索文章'
  }
})

const router = useRouter()

const keyword = ref('')
const showSuggestions = ref(false)
const showPanel = ref(false)
const suggestions = ref([])
const history = ref([])
const hotKeywords = ref([])

let debounceTimer = null

onMounted(async () => {
  try {
    const [historyRes, hotRes] = await Promise.all([
      searchApi.getHistory(),
      searchApi.getHotKeywords()
    ])
    history.value = historyRes.data || []
    hotKeywords.value = hotRes.data || []
  } catch (e) {
    // ignore
  }
})

const onFocus = () => {
  if (keyword.value) {
    showSuggestions.value = true
  } else {
    showPanel.value = true
  }
}

const onBlur = () => {
  setTimeout(() => {
    showSuggestions.value = false
    showPanel.value = false
  }, 200)
}

const onInput = (value) => {
  if (debounceTimer) clearTimeout(debounceTimer)
  
  if (!value) {
    suggestions.value = []
    showSuggestions.value = false
    showPanel.value = true
    return
  }
  
  debounceTimer = setTimeout(async () => {
    try {
      const res = await searchApi.getSuggestions(value)
      suggestions.value = res.data || []
      showSuggestions.value = true
      showPanel.value = false
    } catch (e) {
      suggestions.value = []
    }
  }, 300)
}

const onSearch = () => {
  if (keyword.value.trim()) {
    router.push({ path: '/search', query: { keyword: keyword.value } })
  }
}

const selectSuggestion = (item) => {
  keyword.value = item
  onSearch()
}

const clearHistory = () => {
  history.value = []
}
</script>

<style scoped>
.search-box {
  position: relative;
}

.suggestions {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 0 0 8px 8px;
  z-index: 100;
  max-height: 300px;
  overflow-y: auto;
}

.suggestion-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f5f5f5;
}

.suggestion-item:last-child {
  border-bottom: none;
}

.search-panel {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 0 0 8px 8px;
  z-index: 100;
  padding: 15px;
}

.panel-section {
  margin-bottom: 15px;
}

.panel-section:last-child {
  margin-bottom: 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-size: 14px;
  color: #333;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tags .van-tag {
  cursor: pointer;
}
</style>
```

- [ ] **Step 2: 提交搜索框组件**

```bash
git add blog-web/src/components/SearchBox.vue
git commit -m "feat(search): add SearchBox component with suggestions"
```

---

## Task 12: 路由配置

**Files:**
- Modify: `blog-web/src/router/index.js`

- [ ] **Step 1: 添加搜索页路由**

在路由配置中添加搜索页路由：

```javascript
// 在 portal 路由组中添加
{
  path: '/search',
  name: 'Search',
  component: () => import('@/views/portal/Search.vue'),
  meta: { title: '搜索' }
}
```

- [ ] **Step 2: 提交路由配置**

```bash
git add blog-web/src/router/index.js
git commit -m "feat(search): add search page route"
```

---

## Task 13: 集成测试

- [ ] **Step 1: 启动后端服务**

```bash
cd blog-server
mvn spring-boot:run
```

- [ ] **Step 2: 测试全文检索 API**

```bash
# 测试搜索
curl "http://localhost:8080/api/portal/search?keyword=Spring&page=1&size=10"

# 测试搜索建议
curl "http://localhost:8080/api/portal/search/suggestions?prefix=Sp"

# 测试热门搜索
curl "http://localhost:8080/api/portal/search/hot"

# 测试搜索历史 (需登录)
curl -H "Authorization: Bearer <token>" "http://localhost:8080/api/portal/search/history"
```

- [ ] **Step 3: 启动前端服务**

```bash
cd blog-web
pnpm dev
```

- [ ] **Step 4: 手动测试**

1. 访问搜索页面，输入关键词搜索
2. 验证搜索结果高亮显示
3. 测试搜索建议下拉
4. 测试搜索历史记录
5. 测试热门搜索词
6. 测试排序切换

---

## Self-Review

| 检查项 | 状态 |
|--------|------|
| Spec 覆盖 | ✅ 全文检索、高亮、建议、历史均已覆盖 |
| 占位符扫描 | ✅ 无 TBD/TODO |
| 类型一致性 | ✅ DTO/VO/Mapper 方法签名一致 |
| 文件路径准确 | ✅ 所有路径基于实际代码库结构 |

---

## 实现范围

### 本次实现

- [x] MySQL ngram 全文索引
- [x] 全文搜索 API
- [x] 关键词高亮
- [x] 搜索建议
- [x] 搜索历史
- [x] 热门搜索词
- [x] 相关度/时间排序
- [x] 前端搜索页和搜索框组件

### 未实现（可后续扩展）

- [ ] 拼音搜索
- [ ] 同义词扩展
- [ ] 搜索结果聚合（按分类/标签）
- [ ] 搜索统计报表
