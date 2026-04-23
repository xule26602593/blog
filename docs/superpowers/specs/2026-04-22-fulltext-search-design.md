# 文章全文检索设计方案

## 概述

为博客系统实现文章全文检索功能，基于 MySQL ngram 全文索引，支持中文分词、关键词高亮、搜索建议和搜索历史功能。

## 技术选型

| 项目 | 选择 | 说明 |
|------|------|------|
| 搜索引擎 | MySQL FULLTEXT + ngram | 无需额外组件，内存友好 |
| 中文分词 | ngram parser | MySQL 内置，token_size=2 |
| 高亮实现 | 应用层 HighlightUtil | 正则匹配 + HTML 标签包裹 |
| 搜索建议 | MySQL 表存储 | 基于搜索词频统计 |
| 搜索历史 | MySQL 表存储 | 按用户记录 |

## 架构设计

```
┌─────────────────────────────────────────────────────────┐
│                      前端 (Vue 3)                        │
│    搜索框 → 输入关键词 → 显示结果列表(高亮) + 搜索建议    │
└─────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────┐
│                   后端 (Spring Boot)                     │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐      │
│  │ SearchAPI   │  │ SearchService│  │ HighlightUtil│     │
│  │ (Controller)│→ │ (业务逻辑)   │→ │ (高亮处理)   │      │
│  └─────────────┘  └─────────────┘  └─────────────┘      │
└─────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────┐
│                   MySQL 8.0                             │
│  ┌─────────────────────────────────────────────────┐    │
│  │ article 表                                       │    │
│  │ - FULLTEXT INDEX (title, content) WITH ngram    │    │
│  └─────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────┐    │
│  │ search_history 表 (搜索历史)                      │    │
│  └─────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────┐    │
│  │ search_suggestion 表 (搜索建议)                   │    │
│  └─────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────┘
```

## 数据库设计

### 1. 全文索引

```sql
-- 为 article 表添加全文索引
CREATE FULLTEXT INDEX idx_ft_article 
ON article(title, content) WITH PARSER ngram;
```

### 2. 搜索历史表

```sql
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
```

### 3. 搜索建议表

```sql
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

## 后端服务设计

### 1. 目录结构

```
com.blog
├── controller/portal/
│   └── SearchController.java        # 搜索API入口
├── service/
│   ├── SearchService.java           # 搜索服务接口
│   └── impl/SearchServiceImpl.java  # 搜索服务实现
├── repository/
│   ├── SearchHistoryMapper.java     # 搜索历史Mapper
│   └── SearchSuggestionMapper.java  # 搜索建议Mapper
├── domain/
│   ├── dto/
│   │   └── SearchDTO.java           # 搜索请求参数
│   └── vo/
│       └── SearchVO.java            # 搜索结果VO
└── common/util/
    └── HighlightUtil.java           # 关键词高亮工具类
```

### 2. API 设计

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/portal/search` | GET | 全文检索文章 |
| `/api/portal/search/suggestions` | GET | 获取搜索建议 |
| `/api/portal/search/history` | GET | 获取搜索历史 |
| `/api/portal/search/hot` | GET | 获取热门搜索词 |

### 3. 请求/响应模型

**SearchDTO - 搜索请求参数**：
```java
public class SearchDTO {
    private String keyword;      // 搜索关键词
    private Integer page = 1;    // 页码
    private Integer size = 10;   // 每页数量
    private String sortBy;       // 排序方式: relevance(相关度), time(时间)
}
```

**SearchVO - 搜索结果**：
```java
public class SearchVO {
    private Long id;
    private String title;           // 标题(已高亮)
    private String summary;         // 摘要
    private String contentHighlight;// 内容片段(已高亮)
    private String coverImage;
    private String categoryName;
    private LocalDateTime publishTime;
    private Double score;           // 相关度分数
}
```

## 核心功能实现

### 1. 全文搜索 SQL

```sql
SELECT 
    a.id, a.title, a.summary, a.content, 
    a.cover_image, a.publish_time,
    c.name as category_name,
    MATCH(a.title, a.content) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE) AS score
FROM article a
LEFT JOIN category c ON a.category_id = c.id
WHERE a.status = 1                    -- 已发布
  AND a.deleted = 0                   -- 未删除
  AND MATCH(a.title, a.content) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE)
ORDER BY 
  CASE WHEN #{sortBy} = 'relevance' THEN score END DESC,
  CASE WHEN #{sortBy} = 'time' THEN a.publish_time END DESC
LIMIT #{offset}, #{size};
```

### 2. 关键词高亮工具类

```java
public class HighlightUtil {
    
    private static final String HIGHLIGHT_PREFIX = "<em class=\"highlight\">";
    private static final String HIGHLIGHT_SUFFIX = "</em>";
    
    /**
     * 高亮处理
     * @param text 原文本
     * @param keyword 关键词
     * @param maxLength 返回最大长度(截取关键词上下文)
     */
    public static String highlight(String text, String keyword, int maxLength) {
        if (StringUtils.isEmpty(text) || StringUtils.isEmpty(keyword)) {
            return text;
        }
        
        // 1. 查找关键词位置
        int index = text.toLowerCase().indexOf(keyword.toLowerCase());
        if (index == -1) {
            return truncate(text, maxLength);
        }
        
        // 2. 截取关键词上下文
        String snippet = extractSnippet(text, index, maxLength);
        
        // 3. 高亮关键词(忽略大小写)
        return snippet.replaceAll(
            "(?i)(" + Pattern.quote(keyword) + ")", 
            HIGHLIGHT_PREFIX + "$1" + HIGHLIGHT_SUFFIX
        );
    }
    
    private static String extractSnippet(String text, int index, int maxLength) {
        int start = Math.max(0, index - maxLength / 2);
        int end = Math.min(text.length(), start + maxLength);
        
        String snippet = text.substring(start, end);
        if (start > 0) snippet = "..." + snippet;
        if (end < text.length()) snippet = snippet + "...";
        return snippet;
    }
}
```

### 3. 搜索建议

**前缀匹配查询**：
```sql
SELECT keyword 
FROM search_suggestion 
WHERE keyword LIKE CONCAT(#{prefix}, '%')
ORDER BY search_count DESC, last_search_time DESC
LIMIT 10;
```

**热门搜索词**：
```sql
SELECT keyword 
FROM search_suggestion 
ORDER BY search_count DESC 
LIMIT 10;
```

### 4. 搜索历史

**记录搜索历史**：
```sql
INSERT INTO search_history (user_id, keyword, result_count, ip_address)
VALUES (#{userId}, #{keyword}, #{resultCount}, #{ipAddress});
```

**更新搜索建议统计**：
```sql
INSERT INTO search_suggestion (keyword, search_count, last_search_time)
VALUES (#{keyword}, 1, NOW())
ON DUPLICATE KEY UPDATE 
    search_count = search_count + 1,
    last_search_time = NOW();
```

**获取用户搜索历史**：
```sql
SELECT DISTINCT keyword 
FROM search_history 
WHERE user_id = #{userId} 
ORDER BY create_time DESC 
LIMIT 10;
```

## 前端集成

### 1. API 模块

```javascript
// api/search.js
export default {
  search(params) {
    return request.get('/portal/search', { params })
  },
  getSuggestions(prefix) {
    return request.get('/portal/search/suggestions', { params: { prefix } })
  },
  getHistory() {
    return request.get('/portal/search/history')
  },
  getHotKeywords() {
    return request.get('/portal/search/hot')
  }
}
```

### 2. 组件结构

```
src/views/portal/
└── Search.vue              # 搜索结果页

src/components/
└── SearchBox.vue           # 搜索框组件(含建议下拉)
```

### 3. 交互流程

1. 用户输入关键词时，实时请求搜索建议
2. 点击搜索或回车，跳转搜索结果页
3. 结果页展示高亮的标题和内容片段
4. 支持按相关度/时间排序切换
5. 记录搜索历史，下次访问可快速选择

## 配置

### MySQL 配置

```ini
# my.cnf
[mysqld]
ngram_token_size = 2          # 分词长度
ft_min_word_len = 2           # 最小索引词长度
innodb_ft_cache_size = 32M    # 全文索引缓存(可选)
```

### 应用配置

```yaml
# application.yml
search:
  highlight:
    content-length: 200       # 高亮内容片段最大长度
  history:
    max-size: 10              # 用户历史记录最大条数
    expire-days: 30           # 历史记录保留天数
  suggestion:
    max-size: 10              # 搜索建议最大条数
```

## 测试用例

| 测试场景 | 输入 | 预期结果 |
|---------|------|---------|
| 基础搜索 | "Spring" | 返回标题/内容含 Spring 的文章 |
| 中文搜索 | "全文检索" | 正确分词并匹配相关文章 |
| 高亮显示 | 搜索 "Java" | 结果中 Java 显示为 `<em>Java</em>` |
| 无结果 | "xyzabc123" | 返回空列表，提示未找到 |
| 搜索建议 | 输入 "Sp" | 下拉显示 Spring、Spring Boot 等 |
| 搜索历史 | 登录用户搜索后 | 历史记录中出现该关键词 |
| 排序-相关度 | 搜索后选择相关度排序 | 按匹配分数降序 |
| 排序-时间 | 搜索后选择时间排序 | 按发布时间降序 |

## 实现范围

### 本次实现

- [x] MySQL ngram 全文索引
- [x] 全文搜索 API
- [x] 关键词高亮
- [x] 搜索建议
- [x] 搜索历史
- [x] 热门搜索词
- [x] 相关度/时间排序

### 未实现（可后续扩展）

- [ ] 拼音搜索
- [ ] 同义词扩展
- [ ] 搜索结果聚合（按分类/标签）
- [ ] 搜索统计报表
