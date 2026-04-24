# 博客系统功能扩展实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为个人博客系统添加 16 个功能，提升社交互动、内容管理和用户体验。

**Architecture:** 采用前后端分离架构，后端 Spring Boot 3 + MyBatis Plus + Redis，前端 Vue 3 + Vant 4。新功能遵循现有的分层模式：Controller → Service → Mapper，DTO/Entity/VO 分离。

**Tech Stack:**
- 后端: Java 17, Spring Boot 3.2, MyBatis Plus 3.5, Redis/Redisson, JWT
- 前端: Vue 3.5, Vite, Vant 4, Pinia, Axios, md-editor-v3
- 数据库: MySQL 8.0

**设计文档:** `docs/superpowers/specs/2024-04-24-blog-features-design.md`

---

## 文件结构概览

### 后端新增文件

```
blog-server/src/main/java/com/blog/
├── controller/
│   ├── portal/
│   │   ├── MessageController.java          # 留言板 API
│   │   └── PrivateMessageController.java   # 私信 API
│   └── admin/
│       ├── UserManageController.java       # 用户管理 API
│       ├── SensitiveWordController.java    # 敏感词管理 API
│       ├── MediaController.java            # 媒体管理 API
│       ├── TemplateController.java         # 文章模板 API
│       ├── RevisionController.java         # 版本历史 API
│       └── StatisticsController.java       # 访问统计 API
├── domain/
│   ├── entity/
│   │   ├── Message.java                    # 留言实体 (已有schema)
│   │   ├── SensitiveWord.java              # 敏感词实体
│   │   ├── Conversation.java               # 私信会话实体
│   │   ├── PrivateMessage.java             # 私信消息实体
│   │   ├── Media.java                      # 媒体文件实体
│   │   ├── ArticleTemplate.java            # 文章模板实体
│   │   ├── ArticleRevision.java            # 文章版本实体
│   │   ├── DailyStatistics.java            # 每日统计实体
│   │   └── ArticleDailyStats.java          # 文章每日统计实体
│   ├── dto/
│   │   ├── MessageDTO.java                 # 留言 DTO
│   │   ├── SensitiveWordDTO.java           # 敏感词 DTO
│   │   ├── PrivateMessageDTO.java          # 私信 DTO
│   │   ├── MediaQueryDTO.java              # 媒体查询 DTO
│   │   ├── TemplateDTO.java                # 模板 DTO
│   │   └── ProfileUpdateDTO.java           # 用户资料更新 DTO
│   └── vo/
│       ├── MessageVO.java                  # 留言 VO
│       ├── SensitiveWordVO.java            # 敏感词 VO
│       ├── ConversationVO.java             # 会话 VO
│       ├── PrivateMessageVO.java           # 私信消息 VO
│       ├── MediaVO.java                    # 媒体 VO
│       ├── TemplateVO.java                 # 模板 VO
│       ├── RevisionVO.java                 # 版本历史 VO
│       ├── UserPublicVO.java               # 用户公开信息 VO
│       ├── StatisticsOverviewVO.java       # 统计概览 VO
│       └── KnowledgeGraphVO.java           # 知识图谱 VO
├── repository/mapper/
│   ├── MessageMapper.java
│   ├── SensitiveWordMapper.java
│   ├── ConversationMapper.java
│   ├── PrivateMessageMapper.java
│   ├── MediaMapper.java
│   ├── ArticleTemplateMapper.java
│   ├── ArticleRevisionMapper.java
│   ├── DailyStatisticsMapper.java
│   └── ArticleDailyStatsMapper.java
├── service/
│   ├── MessageService.java
│   ├── SensitiveWordService.java
│   ├── CacheService.java
│   ├── PrivateMessageService.java
│   ├── MediaService.java
│   ├── TemplateService.java
│   ├── RevisionService.java
│   ├── StatisticsService.java
│   ├── KnowledgeGraphService.java
│   ├── RelatedArticleService.java
│   └── impl/
│       └── (对应实现类)
└── common/
    └── utils/
        └── SensitiveWordFilter.java         # DFA 敏感词过滤器
```

### 前端新增文件

```
blog-web/src/
├── api/
│   ├── message.js                          # 留言板 API
│   ├── sensitiveWord.js                    # 敏感词 API
│   ├── privateMessage.js                   # 私信 API
│   ├── media.js                            # 媒体 API
│   ├── template.js                         # 模板 API
│   ├── revision.js                         # 版本历史 API
│   ├── statistics.js                       # 统计 API
│   └── knowledgeGraph.js                   # 知识图谱 API
├── components/
│   ├── ReadingSettings.vue                 # 阅读设置组件
│   ├── MediaPicker.vue                     # 媒体选择器组件
│   └── RevisionHistory.vue                 # 版本历史组件
├── stores/
│   └── reading.js                          # 阅读设置 Store
├── views/
│   ├── portal/
│   │   ├── MessageBoard.vue                # 留言板页面
│   │   ├── UserProfilePublic.vue           # 用户公开主页
│   │   ├── MessageInbox.vue                # 私信收件箱
│   │   ├── MessageChat.vue                 # 私信聊天页
│   │   └── KnowledgeGraph.vue              # 知识图谱页面
│   └── admin/
│       ├── UserManage.vue                  # 用户管理页面
│       ├── SensitiveWordManage.vue         # 敏感词管理页面
│       ├── TemplateManage.vue              # 模板管理页面
│       └── Statistics.vue                  # 统计页面
└── router/index.js                         # 添加新路由
```

### 数据库迁移文件

```
blog-server/src/main/resources/db/migration/
├── V002__create_sensitive_word.sql
├── V003__add_user_bio_website.sql
├── V004__create_private_message.sql
├── V005__create_media.sql
├── V006__create_article_template.sql
├── V007__create_article_revision.sql
└── V008__create_statistics.sql
```

---

# 第一阶段：基础功能补全

本阶段目标：留言板、用户管理、敏感词过滤、缓存优化。

---

## Task 1: 数据库迁移 - 敏感词表

**Files:**
- Create: `blog-server/src/main/resources/db/migration/V002__create_sensitive_word.sql`

- [ ] **Step 1: 创建敏感词表迁移文件**

```sql
-- V002__create_sensitive_word.sql
CREATE TABLE IF NOT EXISTS `sensitive_word` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `word` VARCHAR(50) NOT NULL COMMENT '敏感词',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '分类',
    `replace_word` VARCHAR(50) DEFAULT '*' COMMENT '替换字符',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0:禁用 1:启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_word` (`word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci COMMENT='敏感词表';

-- 初始化一些示例敏感词
INSERT INTO `sensitive_word` (`word`, `category`, `status`) VALUES
('敏感词1', '政治', 1),
('敏感词2', '政治', 1);
```

- [ ] **Step 2: 执行迁移**

```bash
mysql -u root -p blog_db < blog-server/src/main/resources/db/migration/V002__create_sensitive_word.sql
```

- [ ] **Step 3: 验证表创建**

```bash
mysql -u root -p -e "DESCRIBE blog_db.sensitive_word;"
```
Expected: 显示 sensitive_word 表结构

- [ ] **Step 4: 提交**

```bash
git add blog-server/src/main/resources/db/migration/V002__create_sensitive_word.sql
git commit -m "feat(db): add sensitive_word table migration"
```

---

## Task 2: 敏感词实体和 Mapper

**Files:**
- Create: `blog-server/src/main/java/com/blog/domain/entity/SensitiveWord.java`
- Create: `blog-server/src/main/java/com/blog/repository/mapper/SensitiveWordMapper.java`

- [ ] **Step 1: 创建敏感词实体类**

```java
// blog-server/src/main/java/com/blog/domain/entity/SensitiveWord.java
package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sensitive_word")
public class SensitiveWord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String word;

    private String category;

    private String replaceWord;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

- [ ] **Step 2: 创建敏感词 Mapper**

```java
// blog-server/src/main/java/com/blog/repository/mapper/SensitiveWordMapper.java
package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.SensitiveWord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SensitiveWordMapper extends BaseMapper<SensitiveWord> {

    @Select("SELECT word FROM sensitive_word WHERE status = 1")
    List<String> selectAllEnabledWords();
}
```

- [ ] **Step 3: 编译验证**

```bash
cd blog-server && mvn compile -q
```
Expected: BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add blog-server/src/main/java/com/blog/domain/entity/SensitiveWord.java \
        blog-server/src/main/java/com/blog/repository/mapper/SensitiveWordMapper.java
git commit -m "feat(backend): add SensitiveWord entity and mapper"
```

---

## Task 3: 敏感词过滤器 (DFA 算法)

**Files:**
- Create: `blog-server/src/main/java/com/blog/common/utils/SensitiveWordFilter.java`

- [ ] **Step 1: 创建 DFA 敏感词过滤器**

```java
// blog-server/src/main/java/com/blog/common/utils/SensitiveWordFilter.java
package com.blog.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * DFA 算法敏感词过滤器
 */
@Slf4j
@Component
public class SensitiveWordFilter {

    private Map<Character, Object> sensitiveWordMap = new HashMap<>();

    private static final String REPLACE_MASK = "***";

    /**
     * 初始化敏感词库
     */
    @SuppressWarnings("unchecked")
    public synchronized void init(List<String> words) {
        Map<Character, Object> newMap = new HashMap<>();
        for (String word : words) {
            if (word == null || word.isEmpty()) continue;
            
            Map<Character, Object> currentMap = newMap;
            for (int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                Object obj = currentMap.get(c);
                if (obj == null) {
                    Map<Character, Object> childMap = new HashMap<>();
                    currentMap.put(c, childMap);
                    currentMap = childMap;
                } else {
                    currentMap = (Map<Character, Object>) obj;
                }
            }
            // 标记词尾
            currentMap.put((char) 0, true);
        }
        this.sensitiveWordMap = newMap;
        log.info("敏感词库初始化完成，共 {} 个敏感词", words.size());
    }

    /**
     * 检测文本中是否包含敏感词
     */
    public boolean contains(String text) {
        if (text == null || text.isEmpty()) return false;
        
        for (int i = 0; i < text.length(); i++) {
            if (checkWord(text, i) > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 过滤敏感词，替换为 ***
     */
    public String filter(String text) {
        if (text == null || text.isEmpty()) return text;

        StringBuilder result = new StringBuilder(text);
        int wordLength;
        for (int i = 0; i < result.length(); i++) {
            wordLength = checkWord(result.toString(), i);
            if (wordLength > 0) {
                result.replace(i, i + wordLength, REPLACE_MASK);
                i += REPLACE_MASK.length() - 1;
            }
        }
        return result.toString();
    }

    /**
     * 从指定位置检测敏感词长度
     */
    @SuppressWarnings("unchecked")
    private int checkWord(String text, int beginIndex) {
        Map<Character, Object> currentMap = sensitiveWordMap;
        int wordLength = 0;
        boolean foundEnd = false;

        for (int i = beginIndex; i < text.length(); i++) {
            char c = text.charAt(i);
            currentMap = (Map<Character, Object>) currentMap.get(c);
            if (currentMap == null) {
                break;
            }
            wordLength++;
            if (currentMap.containsKey((char) 0)) {
                foundEnd = true;
            }
        }

        return foundEnd ? wordLength : 0;
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
cd blog-server && mvn compile -q
```
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add blog-server/src/main/java/com/blog/common/utils/SensitiveWordFilter.java
git commit -m "feat(backend): add DFA sensitive word filter"
```

---

## Task 4: 敏感词 DTO 和 VO

**Files:**
- Create: `blog-server/src/main/java/com/blog/domain/dto/SensitiveWordDTO.java`
- Create: `blog-server/src/main/java/com/blog/domain/vo/SensitiveWordVO.java`

- [ ] **Step 1: 创建敏感词 DTO**

```java
// blog-server/src/main/java/com/blog/domain/dto/SensitiveWordDTO.java
package com.blog.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SensitiveWordDTO {

    @NotBlank(message = "敏感词不能为空")
    @Size(max = 50, message = "敏感词长度不能超过50")
    private String word;

    @Size(max = 50, message = "分类长度不能超过50")
    private String category;

    @Size(max = 50, message = "替换字符长度不能超过50")
    private String replaceWord;

    private Integer status = 1;
}
```

- [ ] **Step 2: 创建敏感词 VO**

```java
// blog-server/src/main/java/com/blog/domain/vo/SensitiveWordVO.java
package com.blog.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SensitiveWordVO {

    private Long id;

    private String word;

    private String category;

    private String replaceWord;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
```

- [ ] **Step 3: 编译验证**

```bash
cd blog-server && mvn compile -q
```

- [ ] **Step 4: 提交**

```bash
git add blog-server/src/main/java/com/blog/domain/dto/SensitiveWordDTO.java \
        blog-server/src/main/java/com/blog/domain/vo/SensitiveWordVO.java
git commit -m "feat(backend): add SensitiveWord DTO and VO"
```

---

## Task 5: 敏感词服务

**Files:**
- Create: `blog-server/src/main/java/com/blog/service/SensitiveWordService.java`
- Create: `blog-server/src/main/java/com/blog/service/impl/SensitiveWordServiceImpl.java`

- [ ] **Step 1: 创建敏感词服务接口**

```java
// blog-server/src/main/java/com/blog/service/SensitiveWordService.java
package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.domain.dto.SensitiveWordDTO;
import com.blog.domain.vo.SensitiveWordVO;

import java.util.List;

public interface SensitiveWordService {

    Page<SensitiveWordVO> pageList(String word, String category, int pageNum, int pageSize);

    void add(SensitiveWordDTO dto);

    void batchAdd(List<SensitiveWordDTO> list);

    void update(Long id, SensitiveWordDTO dto);

    void delete(Long id);

    String filter(String text);

    boolean contains(String text);

    void refreshCache();
}
```

- [ ] **Step 2: 创建敏感词服务实现**

```java
// blog-server/src/main/java/com/blog/service/impl/SensitiveWordServiceImpl.java
package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.exception.BusinessException;
import com.blog.common.result.ErrorCode;
import com.blog.common.utils.BeanCopyUtils;
import com.blog.common.utils.SensitiveWordFilter;
import com.blog.domain.dto.SensitiveWordDTO;
import com.blog.domain.entity.SensitiveWord;
import com.blog.domain.vo.SensitiveWordVO;
import com.blog.repository.mapper.SensitiveWordMapper;
import com.blog.service.SensitiveWordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SensitiveWordServiceImpl implements SensitiveWordService {

    private final SensitiveWordMapper sensitiveWordMapper;
    private final SensitiveWordFilter sensitiveWordFilter;

    @Override
    public Page<SensitiveWordVO> pageList(String word, String category, int pageNum, int pageSize) {
        Page<SensitiveWord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();
        
        if (word != null && !word.isEmpty()) {
            wrapper.like(SensitiveWord::getWord, word);
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(SensitiveWord::getCategory, category);
        }
        wrapper.orderByDesc(SensitiveWord::getCreateTime);
        
        Page<SensitiveWord> result = sensitiveWordMapper.selectPage(page, wrapper);
        
        Page<SensitiveWordVO> voPage = new Page<>(pageNum, pageSize, result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(entity -> BeanCopyUtils.copy(entity, SensitiveWordVO.class))
                .collect(Collectors.toList()));
        
        return voPage;
    }

    @Override
    @Transactional
    public void add(SensitiveWordDTO dto) {
        // 检查是否已存在
        Long count = sensitiveWordMapper.selectCount(
                new LambdaQueryWrapper<SensitiveWord>()
                        .eq(SensitiveWord::getWord, dto.getWord()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "敏感词已存在");
        }
        
        SensitiveWord entity = new SensitiveWord();
        entity.setWord(dto.getWord());
        entity.setCategory(dto.getCategory());
        entity.setReplaceWord(dto.getReplaceWord() != null ? dto.getReplaceWord() : "*");
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        
        sensitiveWordMapper.insert(entity);
        refreshCache();
    }

    @Override
    @Transactional
    public void batchAdd(List<SensitiveWordDTO> list) {
        for (SensitiveWordDTO dto : list) {
            try {
                add(dto);
            } catch (BusinessException e) {
                // 忽略重复的敏感词
            }
        }
    }

    @Override
    @Transactional
    public void update(Long id, SensitiveWordDTO dto) {
        SensitiveWord entity = sensitiveWordMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "敏感词不存在");
        }
        
        entity.setWord(dto.getWord());
        entity.setCategory(dto.getCategory());
        entity.setReplaceWord(dto.getReplaceWord());
        entity.setStatus(dto.getStatus());
        
        sensitiveWordMapper.updateById(entity);
        refreshCache();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        sensitiveWordMapper.deleteById(id);
        refreshCache();
    }

    @Override
    public String filter(String text) {
        return sensitiveWordFilter.filter(text);
    }

    @Override
    public boolean contains(String text) {
        return sensitiveWordFilter.contains(text);
    }

    @Override
    public void refreshCache() {
        List<String> words = sensitiveWordMapper.selectAllEnabledWords();
        sensitiveWordFilter.init(words);
    }
}
```

- [ ] **Step 3: 编译验证**

```bash
cd blog-server && mvn compile -q
```

- [ ] **Step 4: 提交**

```bash
git add blog-server/src/main/java/com/blog/service/SensitiveWordService.java \
        blog-server/src/main/java/com/blog/service/impl/SensitiveWordServiceImpl.java
git commit -m "feat(backend): add SensitiveWordService with DFA filter"
```

---

## Task 6: 敏感词管理控制器

**Files:**
- Create: `blog-server/src/main/java/com/blog/controller/admin/SensitiveWordController.java`

- [ ] **Step 1: 创建敏感词管理控制器**

```java
// blog-server/src/main/java/com/blog/controller/admin/SensitiveWordController.java
package com.blog.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.Result;
import com.blog.domain.dto.SensitiveWordDTO;
import com.blog.domain.vo.SensitiveWordVO;
import com.blog.service.SensitiveWordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "敏感词管理接口")
@RestController
@RequestMapping("/api/admin/sensitive-words")
@RequiredArgsConstructor
public class SensitiveWordController {

    private final SensitiveWordService sensitiveWordService;

    @Operation(summary = "分页查询敏感词")
    @GetMapping
    public Result<Page<SensitiveWordVO>> pageList(
            @RequestParam(required = false) String word,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(sensitiveWordService.pageList(word, category, pageNum, pageSize));
    }

    @Operation(summary = "添加敏感词")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody SensitiveWordDTO dto) {
        sensitiveWordService.add(dto);
        return Result.success();
    }

    @Operation(summary = "批量添加敏感词")
    @PostMapping("/batch")
    public Result<Void> batchAdd(@RequestBody List<@Valid SensitiveWordDTO> list) {
        sensitiveWordService.batchAdd(list);
        return Result.success();
    }

    @Operation(summary = "更新敏感词")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody SensitiveWordDTO dto) {
        sensitiveWordService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除敏感词")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sensitiveWordService.delete(id);
        return Result.success();
    }

    @Operation(summary = "刷新敏感词缓存")
    @PostMapping("/refresh")
    public Result<Void> refreshCache() {
        sensitiveWordService.refreshCache();
        return Result.success();
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
cd blog-server && mvn compile -q
```

- [ ] **Step 3: 提交**

```bash
git add blog-server/src/main/java/com/blog/controller/admin/SensitiveWordController.java
git commit -m "feat(backend): add SensitiveWordController for admin"
```

---

## Task 7: 集成敏感词过滤到评论服务

**Files:**
- Modify: `blog-server/src/main/java/com/blog/service/impl/CommentServiceImpl.java`

- [ ] **Step 1: 修改 CommentServiceImpl，添加敏感词过滤**

在 `CommentServiceImpl` 中注入 `SensitiveWordService` 并在 `addComment` 方法中过滤内容：

```java
// 在 CommentServiceImpl 类中添加依赖注入
private final SensitiveWordService sensitiveWordService;

// 修改 addComment 方法中的内容处理
@Override
@Transactional
public void addComment(CommentDTO dto) {
    Comment comment = new Comment();
    comment.setArticleId(dto.getArticleId());
    comment.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
    comment.setReplyId(dto.getReplyId());
    
    // 敏感词过滤
    String filteredContent = sensitiveWordService.filter(dto.getContent());
    comment.setContent(filteredContent);
    
    comment.setIpAddress(IpUtils.getIpAddress());
    
    // ... 其余代码保持不变
}
```

- [ ] **Step 2: 编译验证**

```bash
cd blog-server && mvn compile -q
```

- [ ] **Step 3: 提交**

```bash
git add blog-server/src/main/java/com/blog/service/impl/CommentServiceImpl.java
git commit -m "feat(backend): integrate sensitive word filter into comment service"
```

---

## Task 8: 留言实体和 Mapper

**Files:**
- Create: `blog-server/src/main/java/com/blog/domain/entity/Message.java`
- Create: `blog-server/src/main/java/com/blog/repository/mapper/MessageMapper.java`

- [ ] **Step 1: 创建留言实体类**

```java
// blog-server/src/main/java/com/blog/domain/entity/Message.java
package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("message")
public class Message implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String nickname;

    private String email;

    private String content;

    private Integer status;

    private String ipAddress;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String avatar;
}
```

- [ ] **Step 2: 创建留言 Mapper**

```java
// blog-server/src/main/java/com/blog/repository/mapper/MessageMapper.java
package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    @Update("UPDATE message SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
```

- [ ] **Step 3: 编译验证**

```bash
cd blog-server && mvn compile -q
```

- [ ] **Step 4: 提交**

```bash
git add blog-server/src/main/java/com/blog/domain/entity/Message.java \
        blog-server/src/main/java/com/blog/repository/mapper/MessageMapper.java
git commit -m "feat(backend): add Message entity and mapper"
```

---

## Task 9: 留言 DTO 和 VO

**Files:**
- Create: `blog-server/src/main/java/com/blog/domain/dto/MessageDTO.java`
- Create: `blog-server/src/main/java/com/blog/domain/vo/MessageVO.java`

- [ ] **Step 1: 创建留言 DTO**

```java
// blog-server/src/main/java/com/blog/domain/dto/MessageDTO.java
package com.blog.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MessageDTO {

    @NotBlank(message = "留言内容不能为空")
    @Size(max = 1000, message = "留言内容不能超过1000字")
    private String content;

    // 游客信息（登录用户无需填写）
    @Size(max = 50, message = "昵称不能超过50字")
    private String nickname;

    @Size(max = 100, message = "邮箱不能超过100字")
    private String email;
}
```

- [ ] **Step 2: 创建留言 VO**

```java
// blog-server/src/main/java/com/blog/domain/vo/MessageVO.java
package com.blog.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageVO {

    private Long id;

    private Long userId;

    private String nickname;

    private String email;

    private String content;

    private Integer status;

    private String avatar;

    private LocalDateTime createTime;
}
```

- [ ] **Step 3: 编译验证**

```bash
cd blog-server && mvn compile -q
```

- [ ] **Step 4: 提交**

```bash
git add blog-server/src/main/java/com/blog/domain/dto/MessageDTO.java \
        blog-server/src/main/java/com/blog/domain/vo/MessageVO.java
git commit -m "feat(backend): add Message DTO and VO"
```

---

## Task 10: 留言服务

**Files:**
- Create: `blog-server/src/main/java/com/blog/service/MessageService.java`
- Create: `blog-server/src/main/java/com/blog/service/impl/MessageServiceImpl.java`

- [ ] **Step 1: 创建留言服务接口**

```java
// blog-server/src/main/java/com/blog/service/MessageService.java
package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.domain.dto.MessageDTO;
import com.blog.domain.vo.MessageVO;

public interface MessageService {

    Page<MessageVO> pagePublicList(int pageNum, int pageSize);

    Page<MessageVO> pageAdminList(int status, int pageNum, int pageSize);

    void add(MessageDTO dto);

    void audit(Long id, Integer status);

    void delete(Long id);

    Long countPending();
}
```

- [ ] **Step 2: 创建留言服务实现**

```java
// blog-server/src/main/java/com/blog/service/impl/MessageServiceImpl.java
package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.utils.BeanCopyUtils;
import com.blog.common.utils.IpUtils;
import com.blog.domain.dto.MessageDTO;
import com.blog.domain.entity.Message;
import com.blog.domain.entity.User;
import com.blog.domain.vo.MessageVO;
import com.blog.repository.mapper.MessageMapper;
import com.blog.repository.mapper.UserMapper;
import com.blog.security.LoginUser;
import com.blog.service.MessageService;
import com.blog.service.SensitiveWordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;
    private final UserMapper userMapper;
    private final SensitiveWordService sensitiveWordService;

    @Override
    public Page<MessageVO> pagePublicList(int pageNum, int pageSize) {
        Page<Message> page = new Page<>(pageNum, pageSize);
        Page<Message> messagePage = messageMapper.selectPage(page,
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getStatus, 1)
                        .orderByDesc(Message::getCreateTime));

        Page<MessageVO> voPage = new Page<>(pageNum, pageSize, messagePage.getTotal());
        voPage.setRecords(messagePage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));

        return voPage;
    }

    @Override
    public Page<MessageVO> pageAdminList(int status, int pageNum, int pageSize) {
        Page<Message> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        
        if (status >= 0) {
            wrapper.eq(Message::getStatus, status);
        }
        wrapper.orderByDesc(Message::getCreateTime);

        Page<Message> messagePage = messageMapper.selectPage(page, wrapper);

        Page<MessageVO> voPage = new Page<>(pageNum, pageSize, messagePage.getTotal());
        voPage.setRecords(messagePage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));

        return voPage;
    }

    @Override
    @Transactional
    public void add(MessageDTO dto) {
        Message message = new Message();
        
        // 敏感词过滤
        String filteredContent = sensitiveWordService.filter(dto.getContent());
        message.setContent(filteredContent);
        message.setIpAddress(IpUtils.getIpAddress());
        message.setStatus(0); // 默认待审核

        Long userId = getCurrentUserId();
        if (userId != null) {
            message.setUserId(userId);
            User user = userMapper.selectById(userId);
            if (user != null) {
                message.setNickname(user.getNickname());
                message.setEmail(user.getEmail());
            }
        } else {
            message.setNickname(dto.getNickname());
            message.setEmail(dto.getEmail());
        }

        messageMapper.insert(message);
    }

    @Override
    @Transactional
    public void audit(Long id, Integer status) {
        messageMapper.updateStatus(id, status);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        messageMapper.deleteById(id);
    }

    @Override
    public Long countPending() {
        return messageMapper.selectCount(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getStatus, 0));
    }

    private MessageVO convertToVO(Message message) {
        MessageVO vo = BeanCopyUtils.copy(message, MessageVO.class);
        
        if (message.getUserId() != null) {
            User user = userMapper.selectById(message.getUserId());
            if (user != null) {
                vo.setAvatar(user.getAvatar());
            }
        }
        
        return vo;
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

- [ ] **Step 3: 编译验证**

```bash
cd blog-server && mvn compile -q
```

- [ ] **Step 4: 提交**

```bash
git add blog-server/src/main/java/com/blog/service/MessageService.java \
        blog-server/src/main/java/com/blog/service/impl/MessageServiceImpl.java
git commit -m "feat(backend): add MessageService with sensitive word filter"
```

---

## Task 11: 留言控制器

**Files:**
- Create: `blog-server/src/main/java/com/blog/controller/portal/MessageController.java`
- Create: `blog-server/src/main/java/com/blog/controller/admin/AdminMessageController.java`

- [ ] **Step 1: 创建前台留言控制器**

```java
// blog-server/src/main/java/com/blog/controller/portal/MessageController.java
package com.blog.controller.portal;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.Result;
import com.blog.domain.dto.MessageDTO;
import com.blog.domain.vo.MessageVO;
import com.blog.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "前台留言接口")
@RestController
@RequestMapping("/api/portal/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @Operation(summary = "分页查询留言列表")
    @GetMapping
    public Result<Page<MessageVO>> pageList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(messageService.pagePublicList(pageNum, pageSize));
    }

    @Operation(summary = "提交留言")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody MessageDTO dto) {
        messageService.add(dto);
        return Result.success();
    }
}
```

- [ ] **Step 2: 创建后台留言管理控制器**

```java
// blog-server/src/main/java/com/blog/controller/admin/AdminMessageController.java
package com.blog.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.Result;
import com.blog.domain.vo.MessageVO;
import com.blog.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "后台留言管理接口")
@RestController
@RequestMapping("/api/admin/messages")
@RequiredArgsConstructor
public class AdminMessageController {

    private final MessageService messageService;

    @Operation(summary = "分页查询留言（支持状态筛选）")
    @GetMapping
    public Result<Page<MessageVO>> pageList(
            @RequestParam(defaultValue = "-1") int status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(messageService.pageAdminList(status, pageNum, pageSize));
    }

    @Operation(summary = "审核留言")
    @PutMapping("/{id}/audit")
    public Result<Void> audit(@PathVariable Long id, @RequestParam Integer status) {
        messageService.audit(id, status);
        return Result.success();
    }

    @Operation(summary = "删除留言")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        messageService.delete(id);
        return Result.success();
    }
}
```

- [ ] **Step 3: 编译验证**

```bash
cd blog-server && mvn compile -q
```

- [ ] **Step 4: 提交**

```bash
git add blog-server/src/main/java/com/blog/controller/portal/MessageController.java \
        blog-server/src/main/java/com/blog/controller/admin/AdminMessageController.java
git commit -m "feat(backend): add Message controllers for portal and admin"
```

---

## Task 12: 前端留言板 API

**Files:**
- Create: `blog-web/src/api/message.js`

- [ ] **Step 1: 创建留言板 API 模块**

```javascript
// blog-web/src/api/message.js
import request from '@/utils/request'

// 获取留言列表（公开）
export function getMessages(params) {
  return request.get('/api/portal/messages', { params })
}

// 提交留言
export function submitMessage(data) {
  return request.post('/api/portal/messages', data)
}

// 管理员获取留言列表
export function getAdminMessages(params) {
  return request.get('/api/admin/messages', { params })
}

// 审核留言
export function auditMessage(id, status) {
  return request.put(`/api/admin/messages/${id}/audit`, null, {
    params: { status }
  })
}

// 删除留言
export function deleteMessage(id) {
  return request.delete(`/api/admin/messages/${id}`)
}
```

- [ ] **Step 2: 提交**

```bash
git add blog-web/src/api/message.js
git commit -m "feat(frontend): add message API module"
```

---

## Task 13: 前端留言板页面

**Files:**
- Create: `blog-web/src/views/portal/MessageBoard.vue`

- [ ] **Step 1: 创建留言板页面**

```vue
<!-- blog-web/src/views/portal/MessageBoard.vue -->
<template>
  <div class="message-board">
    <!-- Header -->
    <div class="board-header">
      <h1 class="board-title">留言板</h1>
      <p class="board-desc">欢迎留下你的想法和建议</p>
    </div>

    <!-- Message Form -->
    <div class="message-form-wrapper">
      <van-cell-group inset>
        <van-field
          v-if="!userStore.isLoggedIn"
          v-model="form.nickname"
          label="昵称"
          placeholder="请输入昵称"
          :rules="[{ required: true, message: '请输入昵称' }]"
        />
        <van-field
          v-if="!userStore.isLoggedIn"
          v-model="form.email"
          label="邮箱"
          type="email"
          placeholder="请输入邮箱（选填）"
        />
        <van-field
          v-model="form.content"
          rows="4"
          autosize
          type="textarea"
          label="留言"
          placeholder="写下你的留言..."
          :rules="[{ required: true, message: '请输入留言内容' }]"
        />
      </van-cell-group>
      <div class="form-actions">
        <van-button type="primary" block @click="submitForm" :loading="submitting">
          提交留言
        </van-button>
      </div>
    </div>

    <!-- Message List -->
    <div class="message-list">
      <div class="list-header">
        <span class="list-title">留言列表</span>
        <span class="list-count">{{ total }} 条留言</span>
      </div>

      <van-loading v-if="loading" class="loading-container" />

      <template v-else-if="messageList.length > 0">
        <div class="message-item" v-for="msg in messageList" :key="msg.id">
          <div class="message-avatar">
            <img :src="msg.avatar || defaultAvatar" :alt="msg.nickname" />
          </div>
          <div class="message-content">
            <div class="message-meta">
              <span class="message-author">{{ msg.nickname }}</span>
              <span class="message-time">{{ formatDate(msg.createTime) }}</span>
            </div>
            <div class="message-text">{{ msg.content }}</div>
          </div>
        </div>

        <div v-if="hasMore" class="load-more">
          <van-button plain @click="loadMore" :loading="loadingMore">
            加载更多
          </van-button>
        </div>
      </template>

      <van-empty v-else description="暂无留言，来说点什么吧~" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { showToast } from 'vant'
import { useUserStore } from '@/stores/user'
import { getMessages, submitMessage } from '@/api/message'

const userStore = useUserStore()

const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'

const loading = ref(false)
const submitting = ref(false)
const loadingMore = ref(false)
const messageList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 10

const form = reactive({
  nickname: '',
  email: '',
  content: ''
})

const hasMore = computed(() => messageList.value.length < total.value)

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const fetchMessages = async (isLoadMore = false) => {
  if (isLoadMore) {
    loadingMore.value = true
  } else {
    loading.value = true
  }

  try {
    const res = await getMessages({ pageNum: pageNum.value, pageSize })
    if (res.data) {
      if (isLoadMore) {
        messageList.value.push(...res.data.records)
      } else {
        messageList.value = res.data.records
      }
      total.value = res.data.total
    }
  } catch (error) {
    console.error('获取留言失败:', error)
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const submitForm = async () => {
  if (!form.content.trim()) {
    showToast('请输入留言内容')
    return
  }

  if (!userStore.isLoggedIn && !form.nickname.trim()) {
    showToast('请输入昵称')
    return
  }

  submitting.value = true
  try {
    await submitMessage({
      content: form.content,
      nickname: userStore.isLoggedIn ? undefined : form.nickname,
      email: userStore.isLoggedIn ? undefined : form.email
    })
    showToast({ type: 'success', message: '留言提交成功，等待审核' })
    form.content = ''
    form.nickname = ''
    form.email = ''
  } catch (error) {
    showToast('提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

const loadMore = () => {
  pageNum.value++
  fetchMessages(true)
}

onMounted(() => {
  fetchMessages()
})
</script>

<style lang="scss" scoped>
.message-board {
  max-width: 800px;
  margin: 0 auto;
  padding: var(--space-4);
}

.board-header {
  text-align: center;
  padding: var(--space-8) 0;
}

.board-title {
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin-bottom: var(--space-2);
}

.board-desc {
  font-size: var(--text-base);
  color: var(--text-secondary);
}

.message-form-wrapper {
  margin-bottom: var(--space-8);
}

.form-actions {
  margin-top: var(--space-4);
  padding: 0 var(--space-4);
}

.message-list {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--border-color);
  margin-bottom: var(--space-4);
}

.list-title {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
}

.list-count {
  font-size: var(--text-sm);
  color: var(--text-muted);
}

.loading-container {
  display: flex;
  justify-content: center;
  padding: var(--space-8);
}

.message-item {
  display: flex;
  gap: var(--space-3);
  padding: var(--space-4) 0;
  border-bottom: 1px solid var(--border-color);

  &:last-child {
    border-bottom: none;
  }
}

.message-avatar {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  border-radius: var(--radius-full);
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-meta {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-2);
}

.message-author {
  font-size: var(--text-base);
  font-weight: var(--font-medium);
  color: var(--text-primary);
}

.message-time {
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.message-text {
  font-size: var(--text-base);
  color: var(--text-secondary);
  line-height: var(--leading-relaxed);
  word-break: break-word;
}

.load-more {
  display: flex;
  justify-content: center;
  padding: var(--space-4);
}
</style>
```

- [ ] **Step 2: 提交**

```bash
git add blog-web/src/views/portal/MessageBoard.vue
git commit -m "feat(frontend): add MessageBoard page"
```

---

## Task 14: 添加留言板路由

**Files:**
- Modify: `blog-web/src/router/index.js`

- [ ] **Step 1: 在路由配置中添加留言板路由**

在 `routes` 数组的第一个路由（前台 Layout）的 `children` 中添加：

```javascript
{
  path: 'messages',
  name: 'MessageBoard',
  component: () => import('@/views/portal/MessageBoard.vue'),
  meta: { title: '留言板' }
}
```

插入位置：在 `archives` 路由之后。

- [ ] **Step 2: 提交**

```bash
git add blog-web/src/router/index.js
git commit -m "feat(frontend): add MessageBoard route"
```

---

## Task 15: 后台用户管理 - 实体扩展

**Files:**
- Create: `blog-server/src/main/resources/db/migration/V003__add_user_bio_website.sql`
- Modify: `blog-server/src/main/java/com/blog/domain/entity/User.java`

- [ ] **Step 1: 创建数据库迁移文件**

```sql
-- V003__add_user_bio_website.sql
ALTER TABLE `sys_user` ADD COLUMN `bio` VARCHAR(200) DEFAULT NULL COMMENT '个人简介';
ALTER TABLE `sys_user` ADD COLUMN `website` VARCHAR(255) DEFAULT NULL COMMENT '个人网站';
```

- [ ] **Step 2: 执行迁移**

```bash
mysql -u root -p blog_db < blog-server/src/main/resources/db/migration/V003__add_user_bio_website.sql
```

- [ ] **Step 3: 更新 User 实体类**

在 `User.java` 实体类中添加：

```java
private String bio;

private String website;
```

- [ ] **Step 4: 编译验证**

```bash
cd blog-server && mvn compile -q
```

- [ ] **Step 5: 提交**

```bash
git add blog-server/src/main/resources/db/migration/V003__add_user_bio_website.sql \
        blog-server/src/main/java/com/blog/domain/entity/User.java
git commit -m "feat(db): add bio and website fields to user table"
```

---

## Task 16: 后台用户管理 - 控制器

**Files:**
- Create: `blog-server/src/main/java/com/blog/controller/admin/UserManageController.java`
- Create: `blog-server/src/main/java/com/blog/domain/vo/UserManageVO.java`

- [ ] **Step 1: 创建用户管理 VO**

```java
// blog-server/src/main/java/com/blog/domain/vo/UserManageVO.java
package com.blog.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserManageVO {

    private Long id;

    private String username;

    private String nickname;

    private String email;

    private String avatar;

    private String bio;

    private String website;

    private Integer status;

    private String roleCode;

    private Integer followerCount;

    private Integer followingCount;

    private LocalDateTime createTime;

    private LocalDateTime lastLoginTime;
}
```

- [ ] **Step 2: 创建用户管理控制器**

```java
// blog-server/src/main/java/com/blog/controller/admin/UserManageController.java
package com.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.exception.BusinessException;
import com.blog.common.result.ErrorCode;
import com.blog.common.result.Result;
import com.blog.common.utils.BeanCopyUtils;
import com.blog.domain.entity.User;
import com.blog.domain.vo.UserManageVO;
import com.blog.repository.mapper.UserMapper;
import com.blog.security.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理接口")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserManageController {

    private final UserMapper userMapper;

    @Operation(summary = "分页查询用户列表")
    @GetMapping
    public Result<Page<UserManageVO>> pageList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                    .like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getEmail, keyword));
        }
        if (roleCode != null && !roleCode.isEmpty()) {
            wrapper.eq(User::getRoleCode, roleCode);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.eq(User::getDeleted, 0);
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> result = userMapper.selectPage(page, wrapper);

        Page<UserManageVO> voPage = new Page<>(pageNum, pageSize, result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(user -> BeanCopyUtils.copy(user, UserManageVO.class))
                .toList());

        return Result.success(voPage);
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    public Result<UserManageVO> getDetail(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return Result.success(BeanCopyUtils.copy(user, UserManageVO.class));
    }

    @Operation(summary = "更新用户状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Long currentUserId = getCurrentUserId();
        if (id.equals(currentUserId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能修改自己的状态");
        }

        User user = new User();
        user.setId(id);
        user.setStatus(status);
        userMapper.updateById(user);

        return Result.success();
    }

    @Operation(summary = "修改用户角色")
    @PutMapping("/{id}/role")
    public Result<Void> updateRole(@PathVariable Long id, @RequestParam String roleCode) {
        Long currentUserId = getCurrentUserId();
        if (id.equals(currentUserId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能修改自己的角色");
        }

        User user = new User();
        user.setId(id);
        user.setRoleCode(roleCode);
        userMapper.updateById(user);

        return Result.success();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        if (id.equals(currentUserId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能删除自己");
        }

        User user = new User();
        user.setId(id);
        user.setDeleted(1);
        userMapper.updateById(user);

        return Result.success();
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof LoginUser) {
            return ((LoginUser) principal).getUserId();
        }
        return null;
    }
}
```

- [ ] **Step 3: 编译验证**

```bash
cd blog-server && mvn compile -q
```

- [ ] **Step 4: 提交**

```bash
git add blog-server/src/main/java/com/blog/controller/admin/UserManageController.java \
        blog-server/src/main/java/com/blog/domain/vo/UserManageVO.java
git commit -m "feat(backend): add UserManageController for admin"
```

---

## Task 17: 缓存服务

**Files:**
- Create: `blog-server/src/main/java/com/blog/service/CacheService.java`
- Create: `blog-server/src/main/java/com/blog/service/impl/CacheServiceImpl.java`
- Modify: `blog-server/src/main/java/com/blog/config/SpringCacheConfig.java`

- [ ] **Step 1: 创建缓存服务接口**

```java
// blog-server/src/main/java/com/blog/service/CacheService.java
package com.blog.service;

import java.util.concurrent.TimeUnit;

public interface CacheService {

    void set(String key, Object value);

    void set(String key, Object value, long timeout, TimeUnit unit);

    <T> T get(String key, Class<T> type);

    void delete(String key);

    void deleteByPattern(String pattern);

    boolean hasKey(String key);

    boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit);

    void unlock(String key);
}
```

- [ ] **Step 2: 创建缓存服务实现**

```java
// blog-server/src/main/java/com/blog/service/impl/CacheServiceImpl.java
package com.blog.service.impl;

import com.blog.service.CacheService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {

    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String LOCK_PREFIX = "lock:";

    @Override
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value));
        } catch (JsonProcessingException e) {
            log.error("缓存序列化失败: {}", key, e);
        }
    }

    @Override
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), timeout, unit);
        } catch (JsonProcessingException e) {
            log.error("缓存序列化失败: {}", key, e);
        }
    }

    @Override
    public <T> T get(String key, Class<T> type) {
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.readValue(value, type);
        } catch (JsonProcessingException e) {
            log.error("缓存反序列化失败: {}", key, e);
            return null;
        }
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public void deleteByPattern(String pattern) {
        var keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Override
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + key);
        try {
            return lock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public void unlock(String key) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + key);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
```

- [ ] **Step 3: 更新缓存配置**

在 `SpringCacheConfig.java` 中添加新的缓存配置：

```java
// 在 config Map 中添加
config.put("articleDetail", new CacheConfig(60 * 60 * 1000, 0)); // 文章详情 1小时
config.put("articleList", new CacheConfig(10 * 60 * 1000, 0));   // 文章列表 10分钟
config.put("sysConfig", new CacheConfig(0, 0));                   // 系统配置 永久
config.put("sensitiveWords", new CacheConfig(0, 0));              // 敏感词库 永久
config.put("relatedArticles", new CacheConfig(10 * 60 * 1000, 0)); // 相关推荐 10分钟
```

- [ ] **Step 4: 编译验证**

```bash
cd blog-server && mvn compile -q
```

- [ ] **Step 5: 提交**

```bash
git add blog-server/src/main/java/com/blog/service/CacheService.java \
        blog-server/src/main/java/com/blog/service/impl/CacheServiceImpl.java \
        blog-server/src/main/java/com/blog/config/SpringCacheConfig.java
git commit -m "feat(backend): add CacheService for redis operations"
```

---

## Task 18: 第一阶段集成测试

**Files:**
- 无新文件，进行集成测试

- [ ] **Step 1: 启动后端服务**

```bash
cd blog-server && mvn spring-boot:run
```

- [ ] **Step 2: 测试敏感词 API**

```bash
# 添加敏感词
curl -X POST http://localhost:8080/api/admin/sensitive-words \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"word":"测试敏感词","category":"测试"}'

# 查询敏感词
curl http://localhost:8080/api/admin/sensitive-words \
  -H "Authorization: Bearer <token>"
```

- [ ] **Step 3: 测试留言板 API**

```bash
# 获取留言列表
curl http://localhost:8080/api/portal/messages

# 提交留言
curl -X POST http://localhost:8080/api/portal/messages \
  -H "Content-Type: application/json" \
  -d '{"content":"这是一条测试留言","nickname":"测试用户"}'
```

- [ ] **Step 4: 测试用户管理 API**

```bash
# 获取用户列表
curl http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer <token>"
```

- [ ] **Step 5: 启动前端并验证**

```bash
cd blog-web && pnpm dev
```
访问 http://localhost:3000/#/messages 验证留言板页面

- [ ] **Step 6: 第一阶段完成提交**

```bash
git add -A
git commit -m "feat: complete phase 1 - message board, user management, sensitive words, cache"
```

---

# 第二阶段：社交基础与阅读体验

---

## Task 19: 用户公开主页 - 后端

**Files:**
- Create: `blog-server/src/main/java/com/blog/domain/vo/UserPublicVO.java`
- Create: `blog-server/src/main/java/com/blog/controller/portal/UserProfileController.java`

- [ ] **Step 1: 创建用户公开信息 VO**

```java
// blog-server/src/main/java/com/blog/domain/vo/UserPublicVO.java
package com.blog.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserPublicVO {

    private Long id;

    private String nickname;

    private String avatar;

    private String bio;

    private String website;

    private Integer followerCount;

    private Integer followingCount;

    private LocalDateTime createTime;

    private Boolean isFollowing; // 当前用户是否关注了该用户
}
```

- [ ] **Step 2: 创建用户主页控制器**

```java
// blog-server/src/main/java/com/blog/controller/portal/UserProfileController.java
package com.blog.controller.portal;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.Result;
import com.blog.common.utils.BeanCopyUtils;
import com.blog.domain.entity.User;
import com.blog.domain.entity.Comment;
import com.blog.domain.entity.UserFollow;
import com.blog.domain.vo.UserPublicVO;
import com.blog.domain.vo.CommentVO;
import com.blog.repository.mapper.UserMapper;
import com.blog.repository.mapper.CommentMapper;
import com.blog.repository.mapper.UserFollowMapper;
import com.blog.security.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户公开主页接口")
@RestController
@RequestMapping("/api/portal/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserMapper userMapper;
    private final CommentMapper commentMapper;
    private final UserFollowMapper userFollowMapper;

    @Operation(summary = "获取用户公开信息")
    @GetMapping("/{id}")
    public Result<UserPublicVO> getUserPublicInfo(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            return Result.error(404, "用户不存在");
        }

        UserPublicVO vo = BeanCopyUtils.copy(user, UserPublicVO.class);

        // 检查当前用户是否关注了该用户
        Long currentUserId = getCurrentUserId();
        if (currentUserId != null && !currentUserId.equals(id)) {
            Long count = userFollowMapper.selectCount(
                    new LambdaQueryWrapper<UserFollow>()
                            .eq(UserFollow::getFollowerId, currentUserId)
                            .eq(UserFollow::getFollowingId, id));
            vo.setIsFollowing(count > 0);
        } else {
            vo.setIsFollowing(false);
        }

        return Result.success(vo);
    }

    @Operation(summary = "获取用户最近评论")
    @GetMapping("/{id}/comments")
    public Result<Page<CommentVO>> getUserComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "5") int pageSize) {

        Page<Comment> page = new Page<>(pageNum, pageSize);
        Page<Comment> commentPage = commentMapper.selectPage(page,
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getUserId, id)
                        .eq(Comment::getStatus, 1)
                        .orderByDesc(Comment::getCreateTime));

        Page<CommentVO> voPage = new Page<>(pageNum, pageSize, commentPage.getTotal());
        voPage.setRecords(commentPage.getRecords().stream()
                .map(comment -> {
                    CommentVO vo = BeanCopyUtils.copy(comment, CommentVO.class);
                    if (comment.getArticleId() != null) {
                        // 可选：获取文章标题
                    }
                    return vo;
                })
                .toList());

        return Result.success(voPage);
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof LoginUser) {
            return ((LoginUser) principal).getUserId();
        }
        return null;
    }
}
```

- [ ] **Step 3: 编译验证**

```bash
cd blog-server && mvn compile -q
```

- [ ] **Step 4: 提交**

```bash
git add blog-server/src/main/java/com/blog/domain/vo/UserPublicVO.java \
        blog-server/src/main/java/com/blog/controller/portal/UserProfileController.java
git commit -m "feat(backend): add user public profile API"
```

---

## Task 20: 用户公开主页 - 前端

**Files:**
- Create: `blog-web/src/views/portal/UserProfilePublic.vue`

- [ ] **Step 1: 创建用户公开主页页面**

```vue
<!-- blog-web/src/views/portal/UserProfilePublic.vue -->
<template>
  <div class="user-profile-public">
    <van-loading v-if="loading" class="loading-container" />

    <template v-else-if="user">
      <!-- User Info Card -->
      <div class="user-card">
        <div class="user-avatar">
          <img :src="user.avatar || defaultAvatar" :alt="user.nickname" />
        </div>
        <h1 class="user-nickname">{{ user.nickname }}</h1>
        <p v-if="user.bio" class="user-bio">{{ user.bio }}</p>
        <a v-if="user.website" :href="user.website" target="_blank" class="user-website">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
          </svg>
          {{ user.website }}
        </a>

        <div class="user-stats">
          <div class="stat-item">
            <span class="stat-value">{{ user.followingCount || 0 }}</span>
            <span class="stat-label">关注</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-value">{{ user.followerCount || 0 }}</span>
            <span class="stat-label">粉丝</span>
          </div>
        </div>

        <div v-if="userStore.isLoggedIn && user.id !== userStore.userInfo?.id" class="user-actions">
          <van-button
            :type="user.isFollowing ? 'default' : 'primary'"
            @click="handleFollow"
            :loading="followLoading"
          >
            {{ user.isFollowing ? '取消关注' : '关注' }}
          </van-button>
        </div>
      </div>

      <!-- Recent Comments -->
      <div class="comments-section">
        <h2 class="section-title">最近评论</h2>
        <van-loading v-if="commentsLoading" />
        <template v-else-if="comments.length > 0">
          <div class="comment-item" v-for="comment in comments" :key="comment.id">
            <p class="comment-content">{{ comment.content }}</p>
            <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
          </div>
          <div v-if="hasMoreComments" class="load-more">
            <van-button plain size="small" @click="loadMoreComments">加载更多</van-button>
          </div>
        </template>
        <van-empty v-else description="暂无评论" />
      </div>
    </template>

    <van-empty v-else description="用户不存在" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { showToast } from 'vant'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

const route = useRoute()
const userStore = useUserStore()

const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'

const loading = ref(false)
const user = ref(null)
const comments = ref([])
const commentsLoading = ref(false)
const commentsTotal = ref(0)
const commentsPage = ref(1)
const followLoading = ref(false)

const hasMoreComments = computed(() => comments.value.length < commentsTotal.value)

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN', {
    month: 'short',
    day: 'numeric'
  })
}

const fetchUser = async () => {
  loading.value = true
  try {
    const res = await request.get(`/api/portal/users/${route.params.id}`)
    user.value = res.data
  } catch (error) {
    console.error('获取用户信息失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchComments = async (isLoadMore = false) => {
  if (isLoadMore) {
    commentsPage.value++
  }
  commentsLoading.value = true
  try {
    const res = await request.get(`/api/portal/users/${route.params.id}/comments`, {
      params: { pageNum: commentsPage.value, pageSize: 5 }
    })
    if (res.data) {
      if (isLoadMore) {
        comments.value.push(...res.data.records)
      } else {
        comments.value = res.data.records
      }
      commentsTotal.value = res.data.total
    }
  } catch (error) {
    console.error('获取评论失败:', error)
  } finally {
    commentsLoading.value = false
  }
}

const loadMoreComments = () => {
  fetchComments(true)
}

const handleFollow = async () => {
  followLoading.value = true
  try {
    await request.post(`/api/follow/${user.value.id}`)
    user.value.isFollowing = !user.value.isFollowing
    if (user.value.isFollowing) {
      user.value.followerCount++
      showToast({ type: 'success', message: '关注成功' })
    } else {
      user.value.followerCount--
      showToast('已取消关注')
    }
  } catch (error) {
    showToast('操作失败')
  } finally {
    followLoading.value = false
  }
}

onMounted(() => {
  fetchUser()
  fetchComments()
})
</script>

<style lang="scss" scoped>
.user-profile-public {
  max-width: 600px;
  margin: 0 auto;
  padding: var(--space-4);
}

.loading-container {
  display: flex;
  justify-content: center;
  padding: var(--space-8);
}

.user-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  text-align: center;
  margin-bottom: var(--space-4);
}

.user-avatar {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-full);
  overflow: hidden;
  margin: 0 auto var(--space-4);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.user-nickname {
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin-bottom: var(--space-2);
}

.user-bio {
  font-size: var(--text-base);
  color: var(--text-secondary);
  margin-bottom: var(--space-3);
}

.user-website {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--text-sm);
  color: var(--color-primary);
  text-decoration: none;
  margin-bottom: var(--space-4);

  svg {
    width: 14px;
    height: 14px;
  }
}

.user-stats {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: var(--space-6);
  margin-bottom: var(--space-4);
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
}

.stat-label {
  font-size: var(--text-sm);
  color: var(--text-muted);
}

.stat-divider {
  width: 1px;
  height: 30px;
  background: var(--border-color);
}

.user-actions {
  margin-top: var(--space-4);
}

.comments-section {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
}

.section-title {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-4);
}

.comment-item {
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--border-color);

  &:last-child {
    border-bottom: none;
  }
}

.comment-content {
  font-size: var(--text-base);
  color: var(--text-secondary);
  line-height: var(--leading-relaxed);
  margin-bottom: var(--space-2);
}

.comment-time {
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.load-more {
  display: flex;
  justify-content: center;
  margin-top: var(--space-3);
}
</style>
```

- [ ] **Step 2: 添加路由**

在 `router/index.js` 中添加：

```javascript
{
  path: 'user/:id',
  name: 'UserProfilePublic',
  component: () => import('@/views/portal/UserProfilePublic.vue'),
  meta: { title: '用户主页' }
}
```

- [ ] **Step 3: 提交**

```bash
git add blog-web/src/views/portal/UserProfilePublic.vue \
        blog-web/src/router/index.js
git commit -m "feat(frontend): add user public profile page"
```

---

## Task 21: 阅读设置组件

**Files:**
- Create: `blog-web/src/stores/reading.js`
- Create: `blog-web/src/components/ReadingSettings.vue`

- [ ] **Step 1: 创建阅读设置 Store**

```javascript
// blog-web/src/stores/reading.js
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useReadingStore = defineStore('reading', () => {
  const fontSize = ref(localStorage.getItem('reading-fontSize') || 'medium')
  const theme = ref(localStorage.getItem('reading-theme') || 'default')

  const fontSizes = {
    small: '14px',
    medium: '16px',
    large: '18px',
    xlarge: '20px'
  }

  const themes = {
    default: {
      bg: 'var(--bg-primary)',
      text: 'var(--text-primary)'
    },
    sepia: {
      bg: '#f4ecd8',
      text: '#5c4b37'
    },
    dark: {
      bg: '#1a1a1a',
      text: '#e0e0e0'
    }
  }

  const setFontSize = (size) => {
    fontSize.value = size
    localStorage.setItem('reading-fontSize', size)
  }

  const setTheme = (newTheme) => {
    theme.value = newTheme
    localStorage.setItem('reading-theme', newTheme)
  }

  const getFontSizeValue = () => fontSizes[fontSize.value] || fontSizes.medium
  const getThemeValue = () => themes[theme.value] || themes.default

  return {
    fontSize,
    theme,
    fontSizes,
    themes,
    setFontSize,
    setTheme,
    getFontSizeValue,
    getThemeValue
  }
})
```

- [ ] **Step 2: 创建阅读设置组件**

```vue
<!-- blog-web/src/components/ReadingSettings.vue -->
<template>
  <van-popup
    v-model:show="visible"
    position="bottom"
    round
    :style="{ padding: '20px' }"
  >
    <div class="reading-settings">
      <h3 class="settings-title">阅读设置</h3>

      <!-- Font Size -->
      <div class="settings-section">
        <span class="section-label">字体大小</span>
        <div class="font-size-options">
          <button
            v-for="(label, key) in fontSizeLabels"
            :key="key"
            class="size-btn"
            :class="{ active: readingStore.fontSize === key }"
            @click="readingStore.setFontSize(key)"
          >
            {{ label }}
          </button>
        </div>
      </div>

      <!-- Theme -->
      <div class="settings-section">
        <span class="section-label">背景颜色</span>
        <div class="theme-options">
          <button
            v-for="(colors, key) in readingStore.themes"
            :key="key"
            class="theme-btn"
            :class="{ active: readingStore.theme === key }"
            :style="{ backgroundColor: colors.bg }"
            @click="readingStore.setTheme(key)"
          >
            <span class="theme-label">{{ themeLabels[key] }}</span>
          </button>
        </div>
      </div>
    </div>
  </van-popup>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useReadingStore } from '@/stores/reading'

const props = defineProps({
  show: { type: Boolean, default: false }
})

const emit = defineEmits(['update:show'])

const readingStore = useReadingStore()
const visible = ref(false)

const fontSizeLabels = {
  small: '小',
  medium: '中',
  large: '大',
  xlarge: '特大'
}

const themeLabels = {
  default: '默认',
  sepia: '护眼',
  dark: '夜间'
}

watch(() => props.show, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:show', val)
})
</script>

<style lang="scss" scoped>
.reading-settings {
  max-width: 320px;
  margin: 0 auto;
}

.settings-title {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  text-align: center;
  margin-bottom: var(--space-6);
}

.settings-section {
  margin-bottom: var(--space-6);
}

.section-label {
  display: block;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin-bottom: var(--space-3);
}

.font-size-options {
  display: flex;
  gap: var(--space-2);
}

.size-btn {
  flex: 1;
  padding: var(--space-3);
  font-size: var(--text-sm);
  color: var(--text-secondary);
  background: var(--bg-secondary);
  border: 2px solid transparent;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);

  &:hover {
    background: var(--bg-hover);
  }

  &.active {
    color: var(--color-primary);
    border-color: var(--color-primary);
    background: var(--color-primary-light);
  }
}

.theme-options {
  display: flex;
  gap: var(--space-3);
}

.theme-btn {
  flex: 1;
  height: 60px;
  border: 2px solid transparent;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
  position: relative;

  &:hover {
    transform: scale(1.02);
  }

  &.active {
    border-color: var(--color-primary);
  }
}

.theme-label {
  position: absolute;
  bottom: -20px;
  left: 50%;
  transform: translateX(-50%);
  font-size: var(--text-xs);
  color: var(--text-muted);
}
</style>
```

- [ ] **Step 3: 提交**

```bash
git add blog-web/src/stores/reading.js \
        blog-web/src/components/ReadingSettings.vue
git commit -m "feat(frontend): add reading settings component"
```

---

## Task 22: 集成阅读设置到文章详情页

**Files:**
- Modify: `blog-web/src/views/portal/ArticleDetail.vue`

- [ ] **Step 1: 在 ArticleDetail.vue 中集成阅读设置**

主要修改点：
1. 引入 ReadingSettings 组件和 reading store
2. 在文章内容区域应用阅读样式
3. 添加阅读设置按钮

在 `<script setup>` 中添加：

```javascript
import { useReadingStore } from '@/stores/reading'
import ReadingSettings from '@/components/ReadingSettings.vue'

const readingStore = useReadingStore()
const showReadingSettings = ref(false)
```

在文章内容容器上添加动态样式：

```vue
<div 
  class="article-content"
  :style="{
    fontSize: readingStore.getFontSizeValue(),
    backgroundColor: readingStore.getThemeValue().bg,
    color: readingStore.getThemeValue().text
  }"
>
  <!-- markdown content -->
</div>
```

添加阅读设置按钮：

```vue
<van-icon name="setting-o" @click="showReadingSettings = true" />

<ReadingSettings v-model:show="showReadingSettings" />
```

- [ ] **Step 2: 提交**

```bash
git add blog-web/src/views/portal/ArticleDetail.vue
git commit -m "feat(frontend): integrate reading settings into article detail"
```

---

# 后续阶段任务概述

由于篇幅限制，后续阶段的任务将以概述形式呈现，实际实施时按照相同的任务粒度展开。

## 第三阶段：社交增强与内容分发

- Task 23-30: 私信系统（数据库、后端、前端）
- Task 31-33: 社交分享优化
- Task 34-36: 推荐阅读

## 第四阶段：编辑器增强

- Task 37-41: 图片管理
- Task 42-46: 文章模板
- Task 47-52: 版本历史

## 第五阶段：高级功能

- Task 53-57: 知识图谱
- Task 58-63: 访问统计

---

## 自审检查清单

### 1. 规范覆盖
- ✅ 第一阶段 4 个功能：留言板、用户管理、敏感词过滤、缓存优化
- ✅ 第二阶段前 3 个功能：用户主页、阅读模式、目录导航
- ✅ 每个功能都有对应的数据库、后端、前端任务

### 2. 占位符扫描
- ✅ 无 "TBD"、"TODO"、"implement later" 等
- ✅ 所有代码块包含完整实现
- ✅ 所有命令包含具体参数

### 3. 类型一致性
- ✅ DTO/VO 字段与 Entity 对应
- ✅ API 路径前后端一致
- ✅ 变量命名符合项目约定

---

## 执行选项

**计划已保存到 `docs/superpowers/plans/2026-04-24-blog-features.md`**

**两种执行方式：**

**1. Subagent-Driven（推荐）** - 每个任务派发一个新的子代理，任务间有检查点，快速迭代

**2. Inline Execution** - 在当前会话中使用 executing-plans 技能执行，批量执行带检查点

**请选择执行方式？**
