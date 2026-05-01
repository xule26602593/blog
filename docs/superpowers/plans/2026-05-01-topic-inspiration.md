# 话题灵感库功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为博客系统添加话题灵感库功能，帮助管理员记录灵感、AI分析话题价值、追踪创作进度。

**Architecture:** 后端使用 Spring Boot 3 + MyBatis Plus，新增 Topic 实体、Service、Controller；前端使用 Vue 3 + Vant，新增话题列表页和详情页。AI分析复用现有 AiService。

**Tech Stack:** Spring Boot 3, MyBatis Plus, Vue 3, Vant 4, Spring AI

---

## 文件结构

### 后端新增文件

```
blog-server/src/main/java/com/blog/
├── domain/
│   ├── entity/
│   │   └── Topic.java                    # 话题实体
│   └── dto/
│       ├── TopicCreateRequest.java       # 创建请求DTO
│       ├── TopicUpdateRequest.java       # 更新请求DTO
│       ├── TopicQueryRequest.java        # 查询请求DTO
│       └── TopicVO.java                  # 响应VO
├── repository/
│   └── TopicMapper.java                  # MyBatis Mapper
├── service/
│   ├── TopicService.java                 # 服务接口
│   └── impl/
│       └── TopicServiceImpl.java         # 服务实现
└── controller/
    └── admin/
        └── TopicController.java          # 管理端Controller
```

### 前端新增/修改文件

```
blog-web/src/
├── api/
│   └── topic.js                          # API请求模块
├── views/
│   └── admin/
│       ├── TopicList.vue                 # 话题列表页
│       └── TopicDetail.vue               # 话题详情页
└── router/
    └── index.js                          # 添加路由（修改）
```

### 数据库

```
blog-server/src/main/resources/db/
└── migration/
    └── V20260501__create_topic_table.sql  # 数据库迁移脚本
```

---

## Task 1: 创建数据库表

**Files:**
- Create: `blog-server/src/main/resources/db/migration/V20260501__create_topic_table.sql`

- [ ] **Step 1: 创建数据库迁移脚本**

```sql
-- 话题灵感表
CREATE TABLE `topic` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title` VARCHAR(200) NOT NULL COMMENT '话题标题',
    `description` TEXT COMMENT '话题描述/灵感记录',
    `source` VARCHAR(100) COMMENT '来源(知乎/微博/掘金/灵感/读书/工作)',
    `source_url` VARCHAR(500) COMMENT '来源链接',

    -- AI分析结果
    `analysis` TEXT COMMENT 'AI分析结果(JSON)',
    `analysis_status` TINYINT DEFAULT 0 COMMENT '0:待分析 1:分析中 2:已完成 3:失败',

    -- 创作状态追踪
    `status` TINYINT DEFAULT 0 COMMENT '0:待写 1:写作中 2:已发布 3:放弃',
    `article_id` BIGINT COMMENT '关联文章ID',

    -- 优先级与时间
    `priority` TINYINT DEFAULT 2 COMMENT '优先级 1:高 2:中 3:低',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='话题灵感表';
```

- [ ] **Step 2: 执行SQL创建表**

```bash
# 连接MySQL执行SQL
mysql -u root -p blog_db < blog-server/src/main/resources/db/migration/V20260501__create_topic_table.sql
```

或者手动在MySQL中执行上述SQL。

- [ ] **Step 3: 验证表创建成功**

```bash
mysql -u root -p -e "DESCRIBE blog_db.topic;"
```

Expected: 显示topic表的字段结构

- [ ] **Step 4: 提交**

```bash
git add blog-server/src/main/resources/db/migration/V20260501__create_topic_table.sql
git commit -m "feat(db): add topic table for inspiration management"
```

---

## Task 2: 创建后端实体类

**Files:**
- Create: `blog-server/src/main/java/com/blog/domain/entity/Topic.java`

- [ ] **Step 1: 创建Topic实体类**

```java
package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("topic")
public class Topic implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    private String source;

    private String sourceUrl;

    private String analysis;

    private Integer analysisStatus;

    private Integer status;

    private Long articleId;

    private Integer priority;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
```

- [ ] **Step 2: 提交**

```bash
git add blog-server/src/main/java/com/blog/domain/entity/Topic.java
git commit -m "feat(entity): add Topic entity"
```

---

## Task 3: 创建DTO类

**Files:**
- Create: `blog-server/src/main/java/com/blog/domain/dto/TopicCreateRequest.java`
- Create: `blog-server/src/main/java/com/blog/domain/dto/TopicUpdateRequest.java`
- Create: `blog-server/src/main/java/com/blog/domain/dto/TopicQueryRequest.java`
- Create: `blog-server/src/main/java/com/blog/domain/dto/TopicVO.java`

- [ ] **Step 1: 创建TopicCreateRequest**

```java
package com.blog.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TopicCreateRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200")
    private String title;

    @Size(max = 5000, message = "描述长度不能超过5000")
    private String description;

    @Size(max = 100, message = "来源长度不能超过100")
    private String source;

    @Size(max = 500, message = "来源链接长度不能超过500")
    private String sourceUrl;

    private Integer priority = 2;

    private Boolean autoAnalyze = false;
}
```

- [ ] **Step 2: 创建TopicUpdateRequest**

```java
package com.blog.domain.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TopicUpdateRequest {

    @Size(max = 200, message = "标题长度不能超过200")
    private String title;

    @Size(max = 5000, message = "描述长度不能超过5000")
    private String description;

    @Size(max = 100, message = "来源长度不能超过100")
    private String source;

    @Size(max = 500, message = "来源链接长度不能超过500")
    private String sourceUrl;

    private Integer priority;
}
```

- [ ] **Step 3: 创建TopicQueryRequest**

```java
package com.blog.domain.dto;

import lombok.Data;

@Data
public class TopicQueryRequest {

    private Integer status;

    private Integer priority;

    private Integer analysisStatus;

    private String keyword;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
```

- [ ] **Step 4: 创建TopicVO**

```java
package com.blog.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TopicVO {

    private Long id;

    private String title;

    private String description;

    private String source;

    private String sourceUrl;

    private String analysis;

    private Integer analysisStatus;

    private Integer status;

    private Long articleId;

    private Integer priority;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
```

- [ ] **Step 5: 提交**

```bash
git add blog-server/src/main/java/com/blog/domain/dto/TopicCreateRequest.java \
        blog-server/src/main/java/com/blog/domain/dto/TopicUpdateRequest.java \
        blog-server/src/main/java/com/blog/domain/dto/TopicQueryRequest.java \
        blog-server/src/main/java/com/blog/domain/dto/TopicVO.java
git commit -m "feat(dto): add Topic DTOs"
```

---

## Task 4: 创建Mapper

**Files:**
- Create: `blog-server/src/main/java/com/blog/repository/TopicMapper.java`

- [ ] **Step 1: 创建TopicMapper接口**

```java
package com.blog.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.Topic;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TopicMapper extends BaseMapper<Topic> {
}
```

- [ ] **Step 2: 提交**

```bash
git add blog-server/src/main/java/com/blog/repository/TopicMapper.java
git commit -m "feat(mapper): add TopicMapper"
```

---

## Task 5: 创建Service接口

**Files:**
- Create: `blog-server/src/main/java/com/blog/service/TopicService.java`

- [ ] **Step 1: 创建TopicService接口**

```java
package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.domain.dto.*;

public interface TopicService {

    Page<TopicVO> listTopics(TopicQueryRequest request);

    TopicVO getTopicDetail(Long id);

    Long createTopic(TopicCreateRequest request);

    void updateTopic(Long id, TopicUpdateRequest request);

    void deleteTopic(Long id);

    void analyzeTopic(Long id);

    void updateStatus(Long id, Integer status);

    void linkArticle(Long id, Long articleId);
}
```

- [ ] **Step 2: 提交**

```bash
git add blog-server/src/main/java/com/blog/service/TopicService.java
git commit -m "feat(service): add TopicService interface"
```

---

## Task 6: 创建Service实现

**Files:**
- Create: `blog-server/src/main/java/com/blog/service/impl/TopicServiceImpl.java`

- [ ] **Step 1: 创建TopicServiceImpl**

```java
package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.exception.BusinessException;
import com.blog.domain.dto.*;
import com.blog.domain.entity.Topic;
import com.blog.repository.TopicMapper;
import com.blog.service.TopicService;
import com.blog.service.ai.AiService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TopicServiceImpl implements TopicService {

    private final TopicMapper topicMapper;
    private final AiService aiService;
    private final ObjectMapper objectMapper;

    @Override
    public Page<TopicVO> listTopics(TopicQueryRequest request) {
        Page<Topic> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<Topic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(request.getStatus() != null, Topic::getStatus, request.getStatus())
               .eq(request.getPriority() != null, Topic::getPriority, request.getPriority())
               .eq(request.getAnalysisStatus() != null, Topic::getAnalysisStatus, request.getAnalysisStatus())
               .like(request.getKeyword() != null && !request.getKeyword().isBlank(),
                     Topic::getTitle, request.getKeyword())
               .orderByDesc(Topic::getPriority)
               .orderByDesc(Topic::getCreateTime);

        Page<Topic> result = topicMapper.selectPage(page, wrapper);

        return result.convert(this::toVO);
    }

    @Override
    public TopicVO getTopicDetail(Long id) {
        Topic topic = topicMapper.selectById(id);
        if (topic == null) {
            throw new BusinessException("话题不存在");
        }
        return toVO(topic);
    }

    @Override
    public Long createTopic(TopicCreateRequest request) {
        Topic topic = new Topic();
        BeanUtils.copyProperties(request, topic);
        topic.setStatus(0);
        topic.setAnalysisStatus(0);
        topic.setPriority(request.getPriority() != null ? request.getPriority() : 2);

        topicMapper.insert(topic);

        // 如果选择了自动分析，异步触发AI分析
        if (Boolean.TRUE.equals(request.getAutoAnalyze())) {
            triggerAnalysis(topic.getId());
        }

        return topic.getId();
    }

    @Override
    public void updateTopic(Long id, TopicUpdateRequest request) {
        Topic topic = topicMapper.selectById(id);
        if (topic == null) {
            throw new BusinessException("话题不存在");
        }

        if (request.getTitle() != null) topic.setTitle(request.getTitle());
        if (request.getDescription() != null) topic.setDescription(request.getDescription());
        if (request.getSource() != null) topic.setSource(request.getSource());
        if (request.getSourceUrl() != null) topic.setSourceUrl(request.getSourceUrl());
        if (request.getPriority() != null) topic.setPriority(request.getPriority());

        topicMapper.updateById(topic);
    }

    @Override
    public void deleteTopic(Long id) {
        topicMapper.deleteById(id);
    }

    @Override
    public void analyzeTopic(Long id) {
        triggerAnalysis(id);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Topic topic = topicMapper.selectById(id);
        if (topic == null) {
            throw new BusinessException("话题不存在");
        }
        topic.setStatus(status);
        topicMapper.updateById(topic);
    }

    @Override
    public void linkArticle(Long id, Long articleId) {
        Topic topic = topicMapper.selectById(id);
        if (topic == null) {
            throw new BusinessException("话题不存在");
        }
        topic.setArticleId(articleId);
        topic.setStatus(2); // 已发布
        topicMapper.updateById(topic);
    }

    @Async
    protected void triggerAnalysis(Long topicId) {
        Topic topic = topicMapper.selectById(topicId);
        if (topic == null) {
            log.error("话题不存在: {}", topicId);
            return;
        }

        // 更新状态为分析中
        topic.setAnalysisStatus(1);
        topicMapper.updateById(topic);

        try {
            // 构建AI请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("title", topic.getTitle() != null ? topic.getTitle() : "");
            params.put("description", topic.getDescription() != null ? topic.getDescription() : "");

            // 调用AI服务（使用自定义prompt）
            String analysis = aiService.generate("topic_analysis", params);

            // 存储分析结果
            topic.setAnalysis(analysis);
            topic.setAnalysisStatus(2);
            topicMapper.updateById(topic);

            log.info("话题分析完成: {}", topicId);
        } catch (Exception e) {
            log.error("话题分析失败: {}", topicId, e);
            topic.setAnalysisStatus(3);
            topicMapper.updateById(topic);
        }
    }

    private TopicVO toVO(Topic topic) {
        TopicVO vo = new TopicVO();
        BeanUtils.copyProperties(topic, vo);
        return vo;
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add blog-server/src/main/java/com/blog/service/impl/TopicServiceImpl.java
git commit -m "feat(service): add TopicServiceImpl with AI analysis"
```

---

## Task 7: 创建Controller

**Files:**
- Create: `blog-server/src/main/java/com/blog/controller/admin/TopicController.java`

- [ ] **Step 1: 创建TopicController**

```java
package com.blog.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.Result;
import com.blog.domain.dto.*;
import com.blog.service.TopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "后台话题管理")
@RestController
@RequestMapping("/api/admin/topics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class TopicController {

    private final TopicService topicService;

    @Operation(summary = "分页查询话题")
    @GetMapping
    public Result<Page<TopicVO>> listTopics(TopicQueryRequest request) {
        return Result.success(topicService.listTopics(request));
    }

    @Operation(summary = "获取话题详情")
    @GetMapping("/{id}")
    public Result<TopicVO> getTopic(@PathVariable Long id) {
        return Result.success(topicService.getTopicDetail(id));
    }

    @Operation(summary = "创建话题")
    @PostMapping
    public Result<Long> createTopic(@Valid @RequestBody TopicCreateRequest request) {
        Long id = topicService.createTopic(request);
        return Result.success(id);
    }

    @Operation(summary = "更新话题")
    @PutMapping("/{id}")
    public Result<Void> updateTopic(@PathVariable Long id, @RequestBody TopicUpdateRequest request) {
        topicService.updateTopic(id, request);
        return Result.success();
    }

    @Operation(summary = "删除话题")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTopic(@PathVariable Long id) {
        topicService.deleteTopic(id);
        return Result.success();
    }

    @Operation(summary = "触发AI分析")
    @PostMapping("/{id}/analyze")
    public Result<Void> analyzeTopic(@PathVariable Long id) {
        topicService.analyzeTopic(id);
        return Result.success();
    }

    @Operation(summary = "更新话题状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        topicService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "关联文章")
    @PostMapping("/{id}/link")
    public Result<Void> linkArticle(@PathVariable Long id, @RequestParam Long articleId) {
        topicService.linkArticle(id, articleId);
        return Result.success();
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add blog-server/src/main/java/com/blog/controller/admin/TopicController.java
git commit -m "feat(controller): add TopicController for admin"
```

---

## Task 8: 添加AI分析Prompt模板

**Files:**
- Modify: `blog-server/src/main/resources/db/schema.sql` (添加INSERT语句)

- [ ] **Step 1: 添加Prompt模板数据**

在 `schema.sql` 的初始化数据部分添加：

```sql
-- 话题分析Prompt模板
INSERT INTO `prompt_template` (`template_key`, `template_name`, `category`, `system_prompt`, `user_template`, `variables`, `is_default`) VALUES
('topic_analysis', '话题分析模板', 'analysis',
 '你是内容策划专家。请分析以下话题的创作价值。请以JSON格式返回分析结果，不要包含其他内容。',
 '<user_content>
话题标题：{title}
话题描述：{description}
</user_content>

请以JSON格式返回以下字段：
{
  "writingAngles": ["推荐的3个写作角度"],
  "targetAudience": "目标受众描述",
  "difficulty": 2,
  "estimatedReads": "预期阅读量级",
  "keywords": ["关键词1", "关键词2"],
  "value": "内容价值评估",
  "suggestions": "写作建议"
}',
 '{"variables": ["title", "description"]}', 1)
ON DUPLICATE KEY UPDATE
    `template_name` = VALUES(`template_name`),
    `system_prompt` = VALUES(`system_prompt`),
    `user_template` = VALUES(`user_template`);
```

- [ ] **Step 2: 执行SQL插入模板**

```bash
# 登录MySQL执行上述INSERT语句
mysql -u root -p blog_db
```

- [ ] **Step 3: 提交**

```bash
git add blog-server/src/main/resources/db/schema.sql
git commit -m "feat(ai): add topic_analysis prompt template"
```

---

## Task 9: 创建前端API模块

**Files:**
- Create: `blog-web/src/api/topic.js`

- [ ] **Step 1: 创建topic.js API模块**

```javascript
import request from '@/utils/request'

// 获取话题列表
export function getTopics(params) {
  return request.get('/api/admin/topics', { params })
}

// 获取话题详情
export function getTopic(id) {
  return request.get(`/api/admin/topics/${id}`)
}

// 创建话题
export function createTopic(data) {
  return request.post('/api/admin/topics', data)
}

// 更新话题
export function updateTopic(id, data) {
  return request.put(`/api/admin/topics/${id}`, data)
}

// 删除话题
export function deleteTopic(id) {
  return request.delete(`/api/admin/topics/${id}`)
}

// 触发AI分析
export function analyzeTopic(id) {
  return request.post(`/api/admin/topics/${id}/analyze`)
}

// 更新话题状态
export function updateTopicStatus(id, status) {
  return request.put(`/api/admin/topics/${id}/status`, null, {
    params: { status }
  })
}

// 关联文章
export function linkArticle(topicId, articleId) {
  return request.post(`/api/admin/topics/${topicId}/link`, null, {
    params: { articleId }
  })
}
```

- [ ] **Step 2: 提交**

```bash
git add blog-web/src/api/topic.js
git commit -m "feat(api): add topic API module"
```

---

## Task 10: 创建话题列表页

**Files:**
- Create: `blog-web/src/views/admin/TopicList.vue`

- [ ] **Step 1: 创建TopicList.vue**

```vue
<template>
  <div class="topic-list">
    <!-- Header -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">💡 话题灵感库</h1>
        <p class="page-desc">记录灵感，规划创作</p>
      </div>
      <div class="header-right">
        <van-button type="primary" size="small" @click="showCreateDialog = true">
          + 新建话题
        </van-button>
      </div>
    </div>

    <!-- Status Tabs -->
    <div class="status-tabs">
      <van-tag
        v-for="tab in statusTabs"
        :key="tab.value"
        :type="currentStatus === tab.value ? 'primary' : 'default'"
        size="medium"
        round
        class="status-tab"
        @click="currentStatus = tab.value"
      >
        {{ tab.label }} ({{ tab.count }})
      </van-tag>
    </div>

    <!-- Topic List -->
    <div class="topic-cards">
      <van-loading v-if="loading" class="loading" />
      <van-empty v-else-if="topics.length === 0" description="暂无话题" />
      <div
        v-else
        v-for="topic in topics"
        :key="topic.id"
        class="topic-card"
        @click="goToDetail(topic.id)"
      >
        <div class="card-header">
          <span class="status-dot" :class="getStatusClass(topic.status)"></span>
          <span class="topic-title">{{ topic.title }}</span>
          <van-tag :type="getPriorityType(topic.priority)" size="small">
            {{ getPriorityLabel(topic.priority) }}
          </van-tag>
        </div>
        <div class="card-meta">
          <span>来源：{{ topic.source || '未设置' }}</span>
          <span>状态：{{ getStatusLabel(topic.status) }}</span>
          <span>创建于 {{ formatDate(topic.createTime) }}</span>
        </div>
        <div class="card-actions" @click.stop>
          <van-tag v-if="topic.analysisStatus === 2" type="success" plain>AI分析 ✅</van-tag>
          <van-tag v-else-if="topic.analysisStatus === 1" type="warning" plain>分析中...</van-tag>
          <van-tag v-else type="default" plain>待分析</van-tag>
          <van-button
            v-if="topic.status === 0"
            size="mini"
            type="primary"
            plain
            @click="startWriting(topic)"
          >
            开始写作
          </van-button>
          <van-button
            v-if="topic.analysisStatus !== 1"
            size="mini"
            plain
            @click="handleAnalyze(topic.id)"
          >
            {{ topic.analysisStatus === 2 ? '重新分析' : 'AI分析' }}
          </van-button>
          <van-button size="mini" plain @click="goToDetail(topic.id)">详情</van-button>
          <van-button size="mini" type="danger" plain @click="handleDelete(topic.id)">删除</van-button>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div class="pagination">
      <van-pagination
        v-model="pageNum"
        :total-items="total"
        :items-per-page="pageSize"
        @change="loadTopics"
      />
    </div>

    <!-- Create Dialog -->
    <van-dialog
      v-model:show="showCreateDialog"
      title="新建话题"
      show-cancel-button
      :before-close="handleCreateSubmit"
    >
      <van-form ref="createForm">
        <van-cell-group inset>
          <van-field
            v-model="createForm.title"
            label="话题标题"
            placeholder="请输入话题标题"
            required
            :rules="[{ required: true, message: '请输入话题标题' }]"
          />
          <van-field
            v-model="createForm.description"
            label="话题描述"
            type="textarea"
            rows="3"
            placeholder="请输入话题描述（可选）"
          />
          <van-field
            v-model="createForm.source"
            is-link
            readonly
            label="来源"
            placeholder="请选择来源"
            @click="showSourcePicker = true"
          />
          <van-field
            v-model="createForm.sourceUrl"
            label="来源链接"
            placeholder="请输入来源链接（可选）"
          />
          <van-field label="优先级">
            <template #input>
              <van-radio-group v-model="createForm.priority" direction="horizontal">
                <van-radio :name="1">高</van-radio>
                <van-radio :name="2">中</van-radio>
                <van-radio :name="3">低</van-radio>
              </van-radio-group>
            </template>
          </van-field>
          <van-field label="自动分析">
            <template #input>
              <van-switch v-model="createForm.autoAnalyze" />
            </template>
          </van-field>
        </van-cell-group>
      </van-form>
    </van-dialog>

    <!-- Source Picker -->
    <van-popup v-model:show="showSourcePicker" position="bottom" round>
      <van-picker
        :columns="sourceOptions"
        @confirm="onSourceConfirm"
        @cancel="showSourcePicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { getTopics, createTopic, deleteTopic, analyzeTopic, updateTopicStatus } from '@/api/topic'

const router = useRouter()

// State
const loading = ref(false)
const topics = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const currentStatus = ref(null)

const showCreateDialog = ref(false)
const showSourcePicker = ref(false)
const createForm = reactive({
  title: '',
  description: '',
  source: '',
  sourceUrl: '',
  priority: 2,
  autoAnalyze: true
})

const sourceOptions = ['知乎', '微博', '掘金', '灵感', '读书', '工作', '其他']

const statusTabs = computed(() => [
  { label: '全部', value: null, count: total.value },
  { label: '待写', value: 0, count: topics.value.filter(t => t.status === 0).length },
  { label: '写作中', value: 1, count: topics.value.filter(t => t.status === 1).length },
  { label: '已发布', value: 2, count: topics.value.filter(t => t.status === 2).length },
  { label: '放弃', value: 3, count: topics.value.filter(t => t.status === 3).length }
])

// Methods
const loadTopics = async () => {
  loading.value = true
  try {
    const res = await getTopics({
      status: currentStatus.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    topics.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    showToast('加载失败')
  } finally {
    loading.value = false
  }
}

const handleCreateSubmit = async (action) => {
  if (action === 'cancel') {
    resetCreateForm()
    return true
  }
  try {
    const res = await createTopic(createForm)
    showToast('创建成功')
    showCreateDialog.value = false
    resetCreateForm()
    loadTopics()
    // 如果开启了自动分析，跳转到详情页查看
    if (createForm.autoAnalyze) {
      router.push(`/admin/topics/${res.data}`)
    }
    return true
  } catch (e) {
    showToast(e.message || '创建失败')
    return false
  }
}

const resetCreateForm = () => {
  createForm.title = ''
  createForm.description = ''
  createForm.source = ''
  createForm.sourceUrl = ''
  createForm.priority = 2
  createForm.autoAnalyze = true
}

const onSourceConfirm = ({ selectedOptions }) => {
  createForm.source = selectedOptions[0]?.text
  showSourcePicker.value = false
}

const handleAnalyze = async (id) => {
  try {
    await analyzeTopic(id)
    showToast('分析已启动')
    loadTopics()
  } catch (e) {
    showToast('分析失败')
  }
}

const handleDelete = async (id) => {
  try {
    await showConfirmDialog({ title: '确认删除', message: '删除后无法恢复' })
    await deleteTopic(id)
    showToast('删除成功')
    loadTopics()
  } catch (e) {
    // 取消删除
  }
}

const startWriting = (topic) => {
  // 更新状态为写作中
  updateTopicStatus(topic.id, 1)
  // 跳转到文章编辑器
  router.push({ path: '/admin/articles/edit', query: { topicId: topic.id, title: topic.title } })
}

const goToDetail = (id) => {
  router.push(`/admin/topics/${id}`)
}

const getStatusClass = (status) => {
  const classes = ['pending', 'writing', 'published', 'abandoned']
  return classes[status] || 'pending'
}

const getStatusLabel = (status) => {
  const labels = ['待写', '写作中', '已发布', '放弃']
  return labels[status] || '未知'
}

const getPriorityType = (priority) => {
  const types = { 1: 'danger', 2: 'primary', 3: 'default' }
  return types[priority] || 'default'
}

const getPriorityLabel = (priority) => {
  const labels = { 1: '高', 2: '中', 3: '低' }
  return labels[priority] || '中'
}

const formatDate = (date) => {
  if (!date) return ''
  return date.split(' ')[0]
}

// Watch
watch(currentStatus, () => {
  pageNum.value = 1
  loadTopics()
})

// Lifecycle
onMounted(() => {
  loadTopics()
})
</script>

<style scoped>
.topic-list {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
}

.page-desc {
  color: #666;
  margin: 4px 0 0;
}

.status-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.status-tab {
  cursor: pointer;
}

.topic-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.topic-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: transform 0.2s;
}

.topic-card:hover {
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-dot.pending { background: #999; }
.status-dot.writing { background: #ff976a; }
.status-dot.published { background: #07c160; }
.status-dot.abandoned { background: #ccc; }

.topic-title {
  flex: 1;
  font-size: 16px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-meta {
  font-size: 12px;
  color: #666;
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
}

.card-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.loading {
  padding: 40px;
  text-align: center;
}
</style>
```

- [ ] **Step 2: 提交**

```bash
git add blog-web/src/views/admin/TopicList.vue
git commit -m "feat(frontend): add TopicList page"
```

---

## Task 11: 创建话题详情页

**Files:**
- Create: `blog-web/src/views/admin/TopicDetail.vue`

- [ ] **Step 1: 创建TopicDetail.vue**

```vue
<template>
  <div class="topic-detail">
    <van-loading v-if="loading" class="loading" />
    <template v-else-if="topic">
      <!-- Header -->
      <div class="detail-header">
        <van-nav-bar
          title="话题详情"
          left-arrow
          @click-left="$router.back()"
        >
          <template #right>
            <van-button size="small" @click="showEditDialog = true">编辑</van-button>
          </template>
        </van-nav-bar>
      </div>

      <!-- Basic Info -->
      <div class="info-card">
        <h2 class="topic-title">{{ topic.title }}</h2>
        <div class="meta-row">
          <van-tag :type="getPriorityType(topic.priority)">{{ getPriorityLabel(topic.priority) }}</van-tag>
          <van-tag plain>{{ getStatusLabel(topic.status) }}</van-tag>
          <span class="source">来源：{{ topic.source || '未设置' }}</span>
        </div>
        <p v-if="topic.description" class="description">{{ topic.description }}</p>
        <div v-if="topic.sourceUrl" class="source-link">
          <a :href="topic.sourceUrl" target="_blank">查看来源 →</a>
        </div>
      </div>

      <!-- AI Analysis -->
      <div class="analysis-card">
        <div class="analysis-header">
          <h3>🤖 AI分析结果</h3>
          <van-button
            size="small"
            :loading="analyzing"
            @click="handleAnalyze"
          >
            {{ topic.analysisStatus === 2 ? '重新分析' : '开始分析' }}
          </van-button>
        </div>

        <div v-if="topic.analysisStatus === 0" class="analysis-empty">
          <p>尚未进行分析</p>
          <van-button type="primary" size="small" @click="handleAnalyze">开始分析</van-button>
        </div>

        <div v-else-if="topic.analysisStatus === 1" class="analysis-loading">
          <van-loading>AI分析中...</van-loading>
        </div>

        <div v-else-if="topic.analysisStatus === 3" class="analysis-error">
          <p>分析失败，请重试</p>
          <van-button type="primary" size="small" @click="handleAnalyze">重新分析</van-button>
        </div>

        <div v-else-if="analysis" class="analysis-content">
          <div class="analysis-section">
            <h4>📌 推荐写作角度</h4>
            <ol>
              <li v-for="(angle, i) in analysis.writingAngles" :key="i">{{ angle }}</li>
            </ol>
          </div>

          <div class="analysis-section">
            <h4>👥 目标受众</h4>
            <p>{{ analysis.targetAudience }}</p>
          </div>

          <div class="analysis-section">
            <h4>📊 难度与预期</h4>
            <p>难度：{{ getDifficultyLabel(analysis.difficulty) }} | 预期阅读量：{{ analysis.estimatedReads }}</p>
          </div>

          <div class="analysis-section">
            <h4>🔍 关键词</h4>
            <div class="keywords">
              <van-tag v-for="kw in analysis.keywords" :key="kw" plain>{{ kw }}</van-tag>
            </div>
          </div>

          <div class="analysis-section">
            <h4>💡 价值评估</h4>
            <p>{{ analysis.value }}</p>
          </div>

          <div class="analysis-section">
            <h4>📝 写作建议</h4>
            <p>{{ analysis.suggestions }}</p>
          </div>
        </div>
      </div>

      <!-- Actions -->
      <div class="action-bar">
        <van-button
          v-if="topic.status === 0"
          type="primary"
          block
          @click="startWriting"
        >
          开始写作
        </van-button>
        <van-button
          v-else-if="topic.status === 1"
          type="primary"
          block
          @click="continueWriting"
        >
          继续写作
        </van-button>
        <van-button
          v-else-if="topic.status === 2"
          type="default"
          block
          @click="viewArticle"
        >
          查看文章
        </van-button>
        <div v-if="topic.status < 2" class="status-actions">
          <van-button size="small" @click="updateStatus(1)" v-if="topic.status === 0">标记为写作中</van-button>
          <van-button size="small" type="danger" plain @click="updateStatus(3)">放弃</van-button>
        </div>
      </div>
    </template>

    <!-- Edit Dialog -->
    <van-dialog
      v-model:show="showEditDialog"
      title="编辑话题"
      show-cancel-button
      :before-close="handleEditSubmit"
    >
      <van-form>
        <van-cell-group inset>
          <van-field
            v-model="editForm.title"
            label="话题标题"
            required
          />
          <van-field
            v-model="editForm.description"
            label="话题描述"
            type="textarea"
            rows="3"
          />
          <van-field
            v-model="editForm.source"
            label="来源"
          />
          <van-field
            v-model="editForm.sourceUrl"
            label="来源链接"
          />
          <van-field label="优先级">
            <template #input>
              <van-radio-group v-model="editForm.priority" direction="horizontal">
                <van-radio :name="1">高</van-radio>
                <van-radio :name="2">中</van-radio>
                <van-radio :name="3">低</van-radio>
              </van-radio-group>
            </template>
          </van-field>
        </van-cell-group>
      </van-form>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getTopic, updateTopic, analyzeTopic, updateTopicStatus } from '@/api/topic'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const analyzing = ref(false)
const topic = ref(null)
const showEditDialog = ref(false)
const editForm = reactive({
  title: '',
  description: '',
  source: '',
  sourceUrl: '',
  priority: 2
})

const analysis = computed(() => {
  if (!topic.value?.analysis) return null
  try {
    return JSON.parse(topic.value.analysis)
  } catch {
    return null
  }
})

const loadTopic = async () => {
  loading.value = true
  try {
    const res = await getTopic(route.params.id)
    topic.value = res.data
    // 填充编辑表单
    editForm.title = topic.value.title
    editForm.description = topic.value.description || ''
    editForm.source = topic.value.source || ''
    editForm.sourceUrl = topic.value.sourceUrl || ''
    editForm.priority = topic.value.priority || 2
  } catch (e) {
    showToast('加载失败')
  } finally {
    loading.value = false
  }
}

const handleAnalyze = async () => {
  analyzing.value = true
  try {
    await analyzeTopic(topic.value.id)
    showToast('分析已启动')
    // 轮询等待分析完成
    pollAnalysis()
  } catch (e) {
    showToast('分析失败')
    analyzing.value = false
  }
}

const pollAnalysis = async () => {
  const interval = setInterval(async () => {
    try {
      const res = await getTopic(topic.value.id)
      if (res.data.analysisStatus !== 1) {
        topic.value = res.data
        analyzing.value = false
        clearInterval(interval)
      }
    } catch (e) {
      clearInterval(interval)
      analyzing.value = false
    }
  }, 2000)
}

const handleEditSubmit = async (action) => {
  if (action === 'cancel') return true
  try {
    await updateTopic(topic.value.id, editForm)
    showToast('更新成功')
    loadTopic()
    return true
  } catch (e) {
    showToast('更新失败')
    return false
  }
}

const updateStatus = async (status) => {
  try {
    await updateTopicStatus(topic.value.id, status)
    showToast('状态已更新')
    loadTopic()
  } catch (e) {
    showToast('更新失败')
  }
}

const startWriting = () => {
  updateTopicStatus(topic.value.id, 1)
  router.push({ path: '/admin/articles/edit', query: { topicId: topic.value.id, title: topic.value.title } })
}

const continueWriting = () => {
  router.push({ path: '/admin/articles/edit', query: { topicId: topic.value.id } })
}

const viewArticle = () => {
  if (topic.value.articleId) {
    router.push(`/admin/articles/${topic.value.articleId}`)
  }
}

const getStatusLabel = (status) => {
  const labels = ['待写', '写作中', '已发布', '放弃']
  return labels[status] || '未知'
}

const getPriorityType = (priority) => {
  const types = { 1: 'danger', 2: 'primary', 3: 'default' }
  return types[priority] || 'default'
}

const getPriorityLabel = (priority) => {
  const labels = { 1: '高优先级', 2: '中优先级', 3: '低优先级' }
  return labels[priority] || '中优先级'
}

const getDifficultyLabel = (difficulty) => {
  const labels = { 1: '入门', 2: '进阶', 3: '高级' }
  return labels[difficulty] || '进阶'
}

onMounted(() => {
  loadTopic()
})
</script>

<style scoped>
.topic-detail {
  min-height: 100vh;
  background: #f7f8fa;
}

.loading {
  padding: 100px;
  text-align: center;
}

.info-card {
  background: #fff;
  padding: 16px;
  margin-bottom: 12px;
}

.topic-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 12px;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.source {
  font-size: 12px;
  color: #666;
}

.description {
  color: #333;
  line-height: 1.6;
  margin: 12px 0;
}

.source-link a {
  color: #1989fa;
  font-size: 14px;
}

.analysis-card {
  background: #fff;
  padding: 16px;
  margin-bottom: 12px;
}

.analysis-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.analysis-header h3 {
  margin: 0;
  font-size: 16px;
}

.analysis-empty,
.analysis-loading,
.analysis-error {
  text-align: center;
  padding: 40px 0;
  color: #999;
}

.analysis-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.analysis-section h4 {
  font-size: 14px;
  color: #333;
  margin: 0 0 8px;
}

.analysis-section p {
  color: #666;
  margin: 0;
  line-height: 1.5;
}

.analysis-section ol {
  margin: 0;
  padding-left: 20px;
  color: #666;
}

.analysis-section li {
  margin-bottom: 4px;
}

.keywords {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.action-bar {
  background: #fff;
  padding: 16px;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
}

.status-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  justify-content: center;
}
</style>
```

- [ ] **Step 2: 提交**

```bash
git add blog-web/src/views/admin/TopicDetail.vue
git commit -m "feat(frontend): add TopicDetail page with AI analysis"
```

---

## Task 12: 添加路由配置

**Files:**
- Modify: `blog-web/src/router/index.js`

- [ ] **Step 1: 添加话题路由**

在路由配置中添加话题相关路由：

```javascript
// 在 admin 路由组中添加
{
  path: '/admin/topics',
  name: 'TopicList',
  component: () => import('@/views/admin/TopicList.vue'),
  meta: { title: '话题灵感', requiresAuth: true, requiresAdmin: true }
},
{
  path: '/admin/topics/:id',
  name: 'TopicDetail',
  component: () => import('@/views/admin/TopicDetail.vue'),
  meta: { title: '话题详情', requiresAuth: true, requiresAdmin: true }
}
```

- [ ] **Step 2: 提交**

```bash
git add blog-web/src/router/index.js
git commit -m "feat(router): add topic routes"
```

---

## Task 13: 添加导航菜单项

**Files:**
- Modify: `blog-web/src/views/admin/Layout.vue`

- [ ] **Step 1: 在导航中添加话题入口**

在 `Layout.vue` 的内容管理分组中，系列管理后面添加：

```vue
<router-link
  to="/admin/topics"
  class="nav-item"
  :class="{ active: isActive('/admin/topics') }"
>
  <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
    <path stroke-linecap="round" stroke-linejoin="round" d="M12 18v-5.25m0 0a6.01 6.01 0 001.5-.189m-1.5.189a6.01 6.01 0 01-1.5-.189m3.75 7.478a12.06 12.06 0 01-4.5 0m3.75 2.383a14.406 14.406 0 01-3 0M14.25 18v-.192c0-.983.658-1.823 1.508-2.316a7.5 7.5 0 10-7.517 0c.85.493 1.509 1.333 1.509 2.316V18" />
  </svg>
  <span class="nav-label">话题灵感</span>
</router-link>
```

- [ ] **Step 2: 提交**

```bash
git add blog-web/src/views/admin/Layout.vue
git commit -m "feat(navigation): add topic menu item"
```

---

## Task 14: 验证与测试

**Files:**
- 无新增文件

- [ ] **Step 1: 启动后端服务**

```bash
cd blog-server
mvn spring-boot:run
```

- [ ] **Step 2: 启动前端服务**

```bash
cd blog-web
pnpm dev
```

- [ ] **Step 3: 功能验证清单**

手动测试以下功能：

1. **话题列表**
   - [ ] 访问 `/admin/topics` 能正常显示列表
   - [ ] 状态筛选正常工作
   - [ ] 分页正常工作

2. **创建话题**
   - [ ] 点击"新建话题"弹出对话框
   - [ ] 填写表单并保存成功
   - [ ] 自动分析选项正常工作

3. **AI分析**
   - [ ] 点击"AI分析"触发分析
   - [ ] 分析中状态正确显示
   - [ ] 分析结果正确展示

4. **话题详情**
   - [ ] 点击话题卡片进入详情页
   - [ ] 编辑功能正常
   - [ ] 状态更新正常

5. **开始写作**
   - [ ] 点击"开始写作"跳转到文章编辑器
   - [ ] 标题预填充正确

- [ ] **Step 4: 最终提交**

```bash
git add -A
git commit -m "feat: complete topic inspiration feature"
```

---

## 自检清单

**1. 规格覆盖检查：**
- ✅ 数据库表创建 - Task 1
- ✅ 后端实体、DTO、Mapper - Task 2-4
- ✅ 后端Service和Controller - Task 5-7
- ✅ AI分析功能 - Task 8
- ✅ 前端API模块 - Task 9
- ✅ 前端话题列表页 - Task 10
- ✅ 前端话题详情页 - Task 11
- ✅ 路由配置 - Task 12
- ✅ 导航菜单 - Task 13

**2. 占位符检查：**
- ✅ 无 "TBD"、"TODO"、"implement later" 等
- ✅ 所有代码步骤都有完整代码

**3. 类型一致性检查：**
- ✅ Topic 实体字段与数据库一致
- ✅ DTO 字段与实体匹配
- ✅ 前后端API字段名称一致

---

## 执行选项

计划已完成并保存到 `docs/superpowers/plans/2026-05-01-topic-inspiration.md`。

**两种执行方式：**

**1. Subagent-Driven（推荐）** - 每个任务派发一个新的子代理，任务间可审查，快速迭代

**2. Inline Execution** - 在当前会话中执行，批量执行带检查点

选择哪种方式？
