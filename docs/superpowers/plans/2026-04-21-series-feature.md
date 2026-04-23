# 文章系列功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为博客系统添加文章系列（专栏）功能，支持有序/无序两种模式，包含管理后台和门户前端完整实现。

**Architecture:** 采用标准关联表设计，新建 `series` 表存储系列信息，`series_article` 表存储系列与文章的关联关系。后端遵循现有分层架构（Controller → Service → Mapper），前端遵循现有 Vue 3 + Vant 组件风格。

**Tech Stack:** Spring Boot 3 + MyBatis Plus + MySQL (后端), Vue 3 + Vant 4 + Vue Router (前端)

---

## 文件结构

### 后端新增文件
- `blog-server/src/main/java/com/blog/domain/entity/Series.java` - 系列实体
- `blog-server/src/main/java/com/blog/domain/entity/SeriesArticle.java` - 系列文章关联实体
- `blog-server/src/main/java/com/blog/domain/dto/SeriesDTO.java` - 系列数据传输对象
- `blog-server/src/main/java/com/blog/domain/dto/SeriesQueryDTO.java` - 系列查询条件
- `blog-server/src/main/java/com/blog/domain/vo/SeriesVO.java` - 系列视图对象
- `blog-server/src/main/java/com/blog/domain/vo/SeriesListVO.java` - 系列列表视图对象
- `blog-server/src/main/java/com/blog/repository/mapper/SeriesMapper.java` - 系列 Mapper
- `blog-server/src/main/java/com/blog/repository/mapper/SeriesArticleMapper.java` - 系列文章关联 Mapper
- `blog-server/src/main/java/com/blog/service/SeriesService.java` - 系列服务接口
- `blog-server/src/main/java/com/blog/service/impl/SeriesServiceImpl.java` - 系列服务实现
- `blog-server/src/main/java/com/blog/controller/admin/AdminSeriesController.java` - 管理端系列控制器
- `blog-server/src/main/java/com/blog/controller/portal/PortalSeriesController.java` - 门户端系列控制器

### 后端修改文件
- `blog-server/src/main/resources/db/schema.sql` - 添加系列表 SQL

### 前端新增文件
- `blog-web/src/api/series.js` - 系列 API 模块
- `blog-web/src/views/admin/SeriesManage.vue` - 系列管理页面
- `blog-web/src/views/admin/SeriesEdit.vue` - 系列编辑页面
- `blog-web/src/views/portal/SeriesList.vue` - 系列列表页面（门户）
- `blog-web/src/views/portal/SeriesDetail.vue` - 系列详情页面（门户）

### 前端修改文件
- `blog-web/src/router/index.js` - 添加系列相关路由
- `blog-web/src/views/portal/Home.vue` - 首页侧边栏添加热门系列
- `blog-web/src/views/portal/ArticleDetail.vue` - 文章详情页显示所属系列
- `blog-web/src/views/admin/Layout.vue` - 添加系列管理菜单项

---

## Task 1: 数据库表结构

**Files:**
- Modify: `blog-server/src/main/resources/db/schema.sql`

- [ ] **Step 1: 添加系列表 SQL 到 schema.sql**

在 `schema.sql` 文件末尾（初始化数据之前）添加以下表定义：

```sql
-- =============================================
-- 12. 文章系列表
-- =============================================
DROP TABLE IF EXISTS `series`;
CREATE TABLE `series` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '系列名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '系列介绍',
    `cover_image` VARCHAR(255) DEFAULT NULL COMMENT '封面图片',
    `mode` TINYINT DEFAULT 0 COMMENT '模式 0:有序(章节式) 1:无序(主题式)',
    `article_count` INT DEFAULT 0 COMMENT '文章数量(冗余字段)',
    `view_count` BIGINT DEFAULT 0 COMMENT '浏览量',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0:禁用 1:启用',
    `author_id` BIGINT DEFAULT NULL COMMENT '创建者ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除',
    PRIMARY KEY (`id`),
    KEY `idx_status_sort` (`status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章系列表';

-- =============================================
-- 13. 系列文章关联表
-- =============================================
DROP TABLE IF EXISTS `series_article`;
CREATE TABLE `series_article` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `series_id` BIGINT NOT NULL COMMENT '系列ID',
    `article_id` BIGINT NOT NULL COMMENT '文章ID',
    `chapter_order` INT DEFAULT 0 COMMENT '章节顺序(有序模式下使用)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_series_article` (`series_id`, `article_id`),
    KEY `idx_article_id` (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系列文章关联表';
```

- [ ] **Step 2: 提交数据库表结构**

```bash
git add blog-server/src/main/resources/db/schema.sql
git commit -m "feat(series): add series and series_article tables to schema"
```

---

## Task 2: 后端实体类

**Files:**
- Create: `blog-server/src/main/java/com/blog/domain/entity/Series.java`
- Create: `blog-server/src/main/java/com/blog/domain/entity/SeriesArticle.java`

- [ ] **Step 1: 创建 Series 实体类**

```java
package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("series")
public class Series implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private String coverImage;

    private Integer mode;

    private Integer articleCount;

    private Long viewCount;

    private Integer sort;

    private Integer status;

    private Long authorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
```

- [ ] **Step 2: 创建 SeriesArticle 实体类**

```java
package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("series_article")
public class SeriesArticle implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long seriesId;

    private Long articleId;

    private Integer chapterOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
```

- [ ] **Step 3: 提交实体类**

```bash
git add blog-server/src/main/java/com/blog/domain/entity/Series.java
git add blog-server/src/main/java/com/blog/domain/entity/SeriesArticle.java
git commit -m "feat(series): add Series and SeriesArticle entities"
```

---

## Task 3: 后端 DTO 和 VO

**Files:**
- Create: `blog-server/src/main/java/com/blog/domain/dto/SeriesDTO.java`
- Create: `blog-server/src/main/java/com/blog/domain/dto/SeriesQueryDTO.java`
- Create: `blog-server/src/main/java/com/blog/domain/vo/SeriesVO.java`
- Create: `blog-server/src/main/java/com/blog/domain/vo/SeriesListVO.java`

- [ ] **Step 1: 创建 SeriesDTO**

```java
package com.blog.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class SeriesDTO {
    
    private Long id;
    
    @NotBlank(message = "系列名称不能为空")
    private String name;
    
    private String description;
    
    private String coverImage;
    
    private Integer mode;
    
    private Integer sort;
    
    private Integer status;
    
    private List<Long> articleIds;
}
```

- [ ] **Step 2: 创建 SeriesQueryDTO**

```java
package com.blog.domain.dto;

import lombok.Data;

@Data
public class SeriesQueryDTO {
    
    private Integer pageNum = 1;
    
    private Integer pageSize = 10;
    
    private String name;
    
    private Integer mode;
    
    private Integer status;
}
```

- [ ] **Step 3: 创建 SeriesVO**

```java
package com.blog.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SeriesVO {
    
    private Long id;
    
    private String name;
    
    private String description;
    
    private String coverImage;
    
    private Integer mode;
    
    private Integer articleCount;
    
    private Long viewCount;
    
    private Integer status;
    
    private LocalDateTime createTime;
    
    private List<SeriesArticleVO> articles;
    
    @Data
    public static class SeriesArticleVO {
        private Long id;
        private String title;
        private String summary;
        private String coverImage;
        private Long viewCount;
        private Integer chapterOrder;
    }
}
```

- [ ] **Step 4: 创建 SeriesListVO**

```java
package com.blog.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SeriesListVO {
    
    private Long id;
    
    private String name;
    
    private String description;
    
    private String coverImage;
    
    private Integer mode;
    
    private Integer articleCount;
    
    private Long viewCount;
    
    private Integer status;
    
    private LocalDateTime createTime;
}
```

- [ ] **Step 5: 提交 DTO 和 VO**

```bash
git add blog-server/src/main/java/com/blog/domain/dto/SeriesDTO.java
git add blog-server/src/main/java/com/blog/domain/dto/SeriesQueryDTO.java
git add blog-server/src/main/java/com/blog/domain/vo/SeriesVO.java
git add blog-server/src/main/java/com/blog/domain/vo/SeriesListVO.java
git commit -m "feat(series): add Series DTOs and VOs"
```

---

## Task 4: 后端 Mapper

**Files:**
- Create: `blog-server/src/main/java/com/blog/repository/mapper/SeriesMapper.java`
- Create: `blog-server/src/main/java/com/blog/repository/mapper/SeriesArticleMapper.java`

- [ ] **Step 1: 创建 SeriesMapper**

```java
package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.Series;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SeriesMapper extends BaseMapper<Series> {
    
    @Update("UPDATE series SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(@Param("id") Long id);
    
    @Update("UPDATE series SET article_count = article_count + 1 WHERE id = #{id}")
    void incrementArticleCount(@Param("id") Long id);
    
    @Update("UPDATE series SET article_count = article_count - 1 WHERE id = #{id} AND article_count > 0")
    void decrementArticleCount(@Param("id") Long id);
}
```

- [ ] **Step 2: 创建 SeriesArticleMapper**

```java
package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.SeriesArticle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SeriesArticleMapper extends BaseMapper<SeriesArticle> {
    
    @Select("SELECT series_id FROM series_article WHERE article_id = #{articleId}")
    List<Long> selectSeriesIdsByArticleId(@Param("articleId") Long articleId);
    
    @Select("SELECT MAX(chapter_order) FROM series_article WHERE series_id = #{seriesId}")
    Integer selectMaxChapterOrder(@Param("seriesId") Long seriesId);
}
```

- [ ] **Step 3: 提交 Mapper**

```bash
git add blog-server/src/main/java/com/blog/repository/mapper/SeriesMapper.java
git add blog-server/src/main/java/com/blog/repository/mapper/SeriesArticleMapper.java
git commit -m "feat(series): add SeriesMapper and SeriesArticleMapper"
```

---

## Task 5: 后端 Service

**Files:**
- Create: `blog-server/src/main/java/com/blog/service/SeriesService.java`
- Create: `blog-server/src/main/java/com/blog/service/impl/SeriesServiceImpl.java`

- [ ] **Step 1: 创建 SeriesService 接口**

```java
package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.domain.dto.SeriesDTO;
import com.blog.domain.dto.SeriesQueryDTO;
import com.blog.domain.vo.SeriesListVO;
import com.blog.domain.vo.SeriesVO;

import java.util.List;

public interface SeriesService {
    
    Page<SeriesListVO> pageSeries(SeriesQueryDTO query);
    
    SeriesVO getSeriesById(Long id);
    
    List<SeriesListVO> getHotSeries(int limit);
    
    void saveOrUpdateSeries(SeriesDTO dto);
    
    void deleteSeries(Long id);
    
    void addArticlesToSeries(Long seriesId, List<Long> articleIds);
    
    void removeArticleFromSeries(Long seriesId, Long articleId);
    
    void updateArticlesOrder(Long seriesId, List<Long> articleIds);
    
    List<SeriesListVO> getSeriesByArticleId(Long articleId);
}
```

- [ ] **Step 2: 创建 SeriesServiceImpl 实现**

```java
package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.exception.BusinessException;
import com.blog.common.result.ErrorCode;
import com.blog.common.utils.BeanCopyUtils;
import com.blog.domain.dto.SeriesDTO;
import com.blog.domain.dto.SeriesQueryDTO;
import com.blog.domain.entity.Article;
import com.blog.domain.entity.Series;
import com.blog.domain.entity.SeriesArticle;
import com.blog.domain.vo.SeriesListVO;
import com.blog.domain.vo.SeriesVO;
import com.blog.repository.mapper.ArticleMapper;
import com.blog.repository.mapper.SeriesArticleMapper;
import com.blog.repository.mapper.SeriesMapper;
import com.blog.security.LoginUser;
import com.blog.service.SeriesService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeriesServiceImpl implements SeriesService {

    private final SeriesMapper seriesMapper;
    private final SeriesArticleMapper seriesArticleMapper;
    private final ArticleMapper articleMapper;

    @Override
    public Page<SeriesListVO> pageSeries(SeriesQueryDTO query) {
        Page<Series> page = new Page<>(query.getPageNum(), query.getPageSize());
        
        LambdaQueryWrapper<Series> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Series::getDeleted, 0);
        
        if (StringUtils.hasText(query.getName())) {
            wrapper.like(Series::getName, query.getName());
        }
        if (query.getMode() != null) {
            wrapper.eq(Series::getMode, query.getMode());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Series::getStatus, query.getStatus());
        }
        
        wrapper.orderByDesc(Series::getSort)
               .orderByDesc(Series::getCreateTime);
        
        Page<Series> seriesPage = seriesMapper.selectPage(page, wrapper);
        
        Page<SeriesListVO> voPage = new Page<>(seriesPage.getCurrent(), seriesPage.getSize(), seriesPage.getTotal());
        voPage.setRecords(seriesPage.getRecords().stream()
                .map(series -> BeanCopyUtils.copy(series, SeriesListVO.class))
                .collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public SeriesVO getSeriesById(Long id) {
        Series series = seriesMapper.selectById(id);
        if (series == null || series.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "系列不存在");
        }
        
        seriesMapper.incrementViewCount(id);
        series.setViewCount(series.getViewCount() + 1);
        
        SeriesVO vo = BeanCopyUtils.copy(series, SeriesVO.class);
        
        LambdaQueryWrapper<SeriesArticle> saWrapper = new LambdaQueryWrapper<>();
        saWrapper.eq(SeriesArticle::getSeriesId, id);
        if (series.getMode() == 0) {
            saWrapper.orderByAsc(SeriesArticle::getChapterOrder);
        } else {
            saWrapper.orderByDesc(SeriesArticle::getCreateTime);
        }
        
        List<SeriesArticle> seriesArticles = seriesArticleMapper.selectList(saWrapper);
        if (!seriesArticles.isEmpty()) {
            List<Long> articleIds = seriesArticles.stream()
                    .map(SeriesArticle::getArticleId)
                    .collect(Collectors.toList());
            
            List<Article> articles = articleMapper.selectBatchIds(articleIds);
            
            List<SeriesVO.SeriesArticleVO> articleVOs = new ArrayList<>();
            for (SeriesArticle sa : seriesArticles) {
                Article article = articles.stream()
                        .filter(a -> a.getId().equals(sa.getArticleId()))
                        .findFirst()
                        .orElse(null);
                if (article != null && article.getDeleted() == 0 && article.getStatus() == 1) {
                    SeriesVO.SeriesArticleVO articleVO = new SeriesVO.SeriesArticleVO();
                    articleVO.setId(article.getId());
                    articleVO.setTitle(article.getTitle());
                    articleVO.setSummary(article.getSummary());
                    articleVO.setCoverImage(article.getCoverImage());
                    articleVO.setViewCount(article.getViewCount());
                    articleVO.setChapterOrder(sa.getChapterOrder());
                    articleVOs.add(articleVO);
                }
            }
            vo.setArticles(articleVOs);
        }
        
        return vo;
    }

    @Override
    public List<SeriesListVO> getHotSeries(int limit) {
        LambdaQueryWrapper<Series> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Series::getDeleted, 0)
               .eq(Series::getStatus, 1)
               .gt(Series::getArticleCount, 0)
               .orderByDesc(Series::getViewCount)
               .last("LIMIT " + limit);
        
        List<Series> seriesList = seriesMapper.selectList(wrapper);
        return seriesList.stream()
                .map(series -> BeanCopyUtils.copy(series, SeriesListVO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void saveOrUpdateSeries(SeriesDTO dto) {
        Series series;
        
        if (dto.getId() == null) {
            series = new Series();
            series.setViewCount(0L);
            series.setArticleCount(0);
            series.setAuthorId(getCurrentUserId());
        } else {
            series = seriesMapper.selectById(dto.getId());
            if (series == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "系列不存在");
            }
        }
        
        series.setName(dto.getName());
        series.setDescription(dto.getDescription());
        series.setCoverImage(dto.getCoverImage());
        series.setMode(dto.getMode() != null ? dto.getMode() : 0);
        series.setSort(dto.getSort() != null ? dto.getSort() : 0);
        series.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        
        if (dto.getId() == null) {
            seriesMapper.insert(series);
        } else {
            seriesMapper.updateById(series);
        }
        
        if (dto.getArticleIds() != null && !dto.getArticleIds().isEmpty()) {
            addArticlesToSeriesInternal(series.getId(), dto.getArticleIds(), series.getMode() == 0);
        }
    }

    @Override
    @Transactional
    public void deleteSeries(Long id) {
        Series series = seriesMapper.selectById(id);
        if (series == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "系列不存在");
        }
        
        seriesArticleMapper.delete(new LambdaQueryWrapper<SeriesArticle>()
                .eq(SeriesArticle::getSeriesId, id));
        
        seriesMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void addArticlesToSeries(Long seriesId, List<Long> articleIds) {
        Series series = seriesMapper.selectById(seriesId);
        if (series == null || series.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "系列不存在");
        }
        
        addArticlesToSeriesInternal(seriesId, articleIds, series.getMode() == 0);
    }

    private void addArticlesToSeriesInternal(Long seriesId, List<Long> articleIds, boolean ordered) {
        int maxOrder = 0;
        if (ordered) {
            Integer currentMax = seriesArticleMapper.selectMaxChapterOrder(seriesId);
            maxOrder = currentMax != null ? currentMax : 0;
        }
        
        for (Long articleId : articleIds) {
            Long exists = seriesArticleMapper.selectCount(new LambdaQueryWrapper<SeriesArticle>()
                    .eq(SeriesArticle::getSeriesId, seriesId)
                    .eq(SeriesArticle::getArticleId, articleId));
            
            if (exists == 0) {
                SeriesArticle sa = new SeriesArticle();
                sa.setSeriesId(seriesId);
                sa.setArticleId(articleId);
                if (ordered) {
                    sa.setChapterOrder(++maxOrder);
                }
                seriesArticleMapper.insert(sa);
                seriesMapper.incrementArticleCount(seriesId);
            }
        }
    }

    @Override
    @Transactional
    public void removeArticleFromSeries(Long seriesId, Long articleId) {
        int deleted = seriesArticleMapper.delete(new LambdaQueryWrapper<SeriesArticle>()
                .eq(SeriesArticle::getSeriesId, seriesId)
                .eq(SeriesArticle::getArticleId, articleId));
        
        if (deleted > 0) {
            seriesMapper.decrementArticleCount(seriesId);
        }
    }

    @Override
    @Transactional
    public void updateArticlesOrder(Long seriesId, List<Long> articleIds) {
        for (int i = 0; i < articleIds.size(); i++) {
            SeriesArticle sa = seriesArticleMapper.selectOne(new LambdaQueryWrapper<SeriesArticle>()
                    .eq(SeriesArticle::getSeriesId, seriesId)
                    .eq(SeriesArticle::getArticleId, articleIds.get(i)));
            
            if (sa != null) {
                sa.setChapterOrder(i + 1);
                seriesArticleMapper.updateById(sa);
            }
        }
    }

    @Override
    public List<SeriesListVO> getSeriesByArticleId(Long articleId) {
        List<Long> seriesIds = seriesArticleMapper.selectSeriesIdsByArticleId(articleId);
        if (seriesIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Series> seriesList = seriesMapper.selectBatchIds(seriesIds);
        return seriesList.stream()
                .filter(s -> s.getDeleted() == 0 && s.getStatus() == 1)
                .map(series -> BeanCopyUtils.copy(series, SeriesListVO.class))
                .collect(Collectors.toList());
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

- [ ] **Step 3: 提交 Service**

```bash
git add blog-server/src/main/java/com/blog/service/SeriesService.java
git add blog-server/src/main/java/com/blog/service/impl/SeriesServiceImpl.java
git commit -m "feat(series): add SeriesService interface and implementation"
```

---

## Task 6: 后端 Controller

**Files:**
- Create: `blog-server/src/main/java/com/blog/controller/admin/AdminSeriesController.java`
- Create: `blog-server/src/main/java/com/blog/controller/portal/PortalSeriesController.java`

- [ ] **Step 1: 创建 AdminSeriesController**

```java
package com.blog.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.Result;
import com.blog.domain.dto.SeriesDTO;
import com.blog.domain.dto.SeriesQueryDTO;
import com.blog.domain.vo.SeriesListVO;
import com.blog.domain.vo.SeriesVO;
import com.blog.service.SeriesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "后台系列管理")
@RestController
@RequestMapping("/api/admin/series")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSeriesController {

    private final SeriesService seriesService;

    @Operation(summary = "分页查询系列")
    @GetMapping
    public Result<Page<SeriesListVO>> pageSeries(SeriesQueryDTO query) {
        return Result.success(seriesService.pageSeries(query));
    }

    @Operation(summary = "获取系列详情")
    @GetMapping("/{id}")
    public Result<SeriesVO> getSeries(@PathVariable Long id) {
        return Result.success(seriesService.getSeriesById(id));
    }

    @Operation(summary = "保存或更新系列")
    @PostMapping
    public Result<Void> saveSeries(@Valid @RequestBody SeriesDTO dto) {
        seriesService.saveOrUpdateSeries(dto);
        return Result.success();
    }

    @Operation(summary = "删除系列")
    @DeleteMapping("/{id}")
    public Result<Void> deleteSeries(@PathVariable Long id) {
        seriesService.deleteSeries(id);
        return Result.success();
    }

    @Operation(summary = "添加文章到系列")
    @PostMapping("/{id}/articles")
    public Result<Void> addArticles(@PathVariable Long id, @RequestBody List<Long> articleIds) {
        seriesService.addArticlesToSeries(id, articleIds);
        return Result.success();
    }

    @Operation(summary = "从系列移除文章")
    @DeleteMapping("/{id}/articles/{articleId}")
    public Result<Void> removeArticle(@PathVariable Long id, @PathVariable Long articleId) {
        seriesService.removeArticleFromSeries(id, articleId);
        return Result.success();
    }

    @Operation(summary = "调整文章顺序")
    @PutMapping("/{id}/articles/order")
    public Result<Void> updateArticlesOrder(@PathVariable Long id, @RequestBody List<Long> articleIds) {
        seriesService.updateArticlesOrder(id, articleIds);
        return Result.success();
    }
}
```

- [ ] **Step 2: 创建 PortalSeriesController**

```java
package com.blog.controller.portal;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.Result;
import com.blog.domain.dto.SeriesQueryDTO;
import com.blog.domain.vo.SeriesListVO;
import com.blog.domain.vo.SeriesVO;
import com.blog.service.SeriesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "门户系列")
@RestController
@RequestMapping("/api/portal/series")
@RequiredArgsConstructor
public class PortalSeriesController {

    private final SeriesService seriesService;

    @Operation(summary = "分页查询系列")
    @GetMapping
    public Result<Page<SeriesListVO>> pageSeries(SeriesQueryDTO query) {
        query.setStatus(1);
        return Result.success(seriesService.pageSeries(query));
    }

    @Operation(summary = "获取系列详情")
    @GetMapping("/{id}")
    public Result<SeriesVO> getSeries(@PathVariable Long id) {
        return Result.success(seriesService.getSeriesById(id));
    }

    @Operation(summary = "获取热门系列")
    @GetMapping("/hot")
    public Result<List<SeriesListVO>> getHotSeries(@RequestParam(defaultValue = "5") int limit) {
        return Result.success(seriesService.getHotSeries(limit));
    }
}
```

- [ ] **Step 3: 提交 Controller**

```bash
git add blog-server/src/main/java/com/blog/controller/admin/AdminSeriesController.java
git add blog-server/src/main/java/com/blog/controller/portal/PortalSeriesController.java
git commit -m "feat(series): add AdminSeriesController and PortalSeriesController"
```

---

## Task 7: 前端 API 模块

**Files:**
- Create: `blog-web/src/api/series.js`

- [ ] **Step 1: 创建 series.js API 模块**

```javascript
import request from '@/utils/request'

// ========== 门户端 ==========

// 分页查询系列列表
export function getSeries(params) {
  return request.get('/api/portal/series', { params })
}

// 获取系列详情
export function getSeriesDetail(id) {
  return request.get(`/api/portal/series/${id}`)
}

// 获取热门系列
export function getHotSeries(limit = 5) {
  return request.get('/api/portal/series/hot', { params: { limit } })
}

// ========== 管理后台 ==========

// 分页查询系列（管理）
export function getAdminSeries(params) {
  return request.get('/api/admin/series', { params })
}

// 获取系列详情（管理）
export function getAdminSeriesDetail(id) {
  return request.get(`/api/admin/series/${id}`)
}

// 保存或更新系列
export function saveSeries(data) {
  return request.post('/api/admin/series', data)
}

// 删除系列
export function deleteSeries(id) {
  return request.delete(`/api/admin/series/${id}`)
}

// 添加文章到系列
export function addArticlesToSeries(seriesId, articleIds) {
  return request.post(`/api/admin/series/${seriesId}/articles`, articleIds)
}

// 从系列移除文章
export function removeArticleFromSeries(seriesId, articleId) {
  return request.delete(`/api/admin/series/${seriesId}/articles/${articleId}`)
}

// 调整文章顺序
export function updateArticlesOrder(seriesId, articleIds) {
  return request.put(`/api/admin/series/${seriesId}/articles/order`, articleIds)
}
```

- [ ] **Step 2: 提交 API 模块**

```bash
git add blog-web/src/api/series.js
git commit -m "feat(series): add series API module"
```

---

## Task 8: 前端路由配置

**Files:**
- Modify: `blog-web/src/router/index.js`

- [ ] **Step 1: 添加系列相关路由**

在 `router/index.js` 中：

1. 在门户路由 children 数组中添加（在 `user` 路由之前）：

```javascript
      {
        path: 'series',
        name: 'SeriesList',
        component: () => import('@/views/portal/SeriesList.vue'),
        meta: { title: '系列' }
      },
      {
        path: 'series/:id',
        name: 'SeriesDetail',
        component: () => import('@/views/portal/SeriesDetail.vue'),
        meta: { title: '系列详情' }
      },
```

2. 在管理后台路由 children 数组中添加（在 `comments` 路由之后）：

```javascript
      {
        path: 'series',
        name: 'SeriesManage',
        component: () => import('@/views/admin/SeriesManage.vue'),
        meta: { title: '系列管理' }
      },
      {
        path: 'series/edit/:id?',
        name: 'SeriesEdit',
        component: () => import('@/views/admin/SeriesEdit.vue'),
        meta: { title: '系列编辑' }
      }
```

- [ ] **Step 2: 提交路由配置**

```bash
git add blog-web/src/router/index.js
git commit -m "feat(series): add series routes to router"
```

---

## Task 9: 管理后台 - 系列管理页面

**Files:**
- Create: `blog-web/src/views/admin/SeriesManage.vue`

- [ ] **Step 1: 创建 SeriesManage.vue**

```vue
<template>
  <div class="series-manage">
    <div class="page-header">
      <h2 class="page-title">系列管理</h2>
      <van-button type="primary" size="small" @click="router.push('/admin/series/edit')">
        新建系列
      </van-button>
    </div>

    <van-search
      v-model="searchName"
      placeholder="搜索系列名称"
      @search="handleSearch"
    />

    <div class="series-list">
      <div v-for="series in seriesList" :key="series.id" class="series-item">
        <div class="series-info">
          <img v-if="series.coverImage" :src="series.coverImage" class="series-cover" />
          <div v-else class="series-cover-placeholder">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 12.75V12A2.25 2.25 0 014.5 9.75h15A2.25 2.25 0 0121.75 12v.75m-8.69-6.44l-2.12-2.12a1.5 1.5 0 00-1.061-.44H4.5A2.25 2.25 0 002.25 6v12a2.25 2.25 0 002.25 2.25h15A2.25 2.25 0 0021.75 18V9a2.25 2.25 0 00-2.25-2.25h-5.379a1.5 1.5 0 01-1.06-.44z" />
            </svg>
          </div>
          <div class="series-content">
            <h3 class="series-name">{{ series.name }}</h3>
            <div class="series-meta">
              <span class="meta-item">
                <van-tag :type="series.mode === 0 ? 'primary' : 'success'" size="small">
                  {{ series.mode === 0 ? '有序' : '无序' }}
                </van-tag>
              </span>
              <span class="meta-item">{{ series.articleCount }} 篇文章</span>
              <span class="meta-item">{{ series.viewCount }} 次浏览</span>
            </div>
          </div>
        </div>
        <div class="series-actions">
          <van-button size="small" @click="router.push('/admin/series/edit/' + series.id)">编辑</van-button>
          <van-popconfirm title="确定删除该系列吗？" @confirm="handleDelete(series.id)">
            <template #reference>
              <van-button size="small" type="danger">删除</van-button>
            </template>
          </van-popconfirm>
        </div>
      </div>

      <van-empty v-if="!loading && seriesList.length === 0" description="暂无系列数据" />
    </div>

    <div v-if="hasMore" class="load-more">
      <van-button block @click="loadMore" :loading="loading">加载更多</van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getAdminSeries, deleteSeries } from '@/api/series'

const router = useRouter()

const loading = ref(false)
const searchName = ref('')
const seriesList = ref([])
const pageNum = ref(1)
const hasMore = ref(true)

const fetchSeries = async () => {
  loading.value = true
  try {
    const res = await getAdminSeries({
      pageNum: pageNum.value,
      pageSize: 10,
      name: searchName.value || undefined
    })
    if (pageNum.value === 1) {
      seriesList.value = res.data?.records || []
    } else {
      seriesList.value.push(...(res.data?.records || []))
    }
    hasMore.value = seriesList.value.length < (res.data?.total || 0)
  } catch (error) {
    console.error('获取系列失败', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  fetchSeries()
}

const loadMore = () => {
  pageNum.value++
  fetchSeries()
}

const handleDelete = async (id) => {
  try {
    await deleteSeries(id)
    showToast({ type: 'success', message: '删除成功' })
    pageNum.value = 1
    fetchSeries()
  } catch (error) {
    console.error('删除失败', error)
    showToast('删除失败')
  }
}

onMounted(() => {
  fetchSeries()
})
</script>

<style lang="scss" scoped>
.series-manage {
  max-width: 900px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-6);
}

.page-title {
  font-size: var(--text-xl);
  font-weight: var(--font-semibold);
}

.series-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  margin-top: var(--space-4);
}

.series-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
}

.series-info {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.series-cover {
  width: 60px;
  height: 40px;
  object-fit: cover;
  border-radius: var(--radius-sm);
}

.series-cover-placeholder {
  width: 60px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-secondary);
  border-radius: var(--radius-sm);
  
  svg {
    width: 20px;
    height: 20px;
    color: var(--text-muted);
  }
}

.series-content {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.series-name {
  font-size: var(--text-base);
  font-weight: var(--font-medium);
}

.series-meta {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  font-size: var(--text-sm);
  color: var(--text-muted);
}

.meta-item {
  display: flex;
  align-items: center;
}

.series-actions {
  display: flex;
  gap: var(--space-2);
}

.load-more {
  margin-top: var(--space-6);
}
</style>
```

- [ ] **Step 2: 提交系列管理页面**

```bash
git add blog-web/src/views/admin/SeriesManage.vue
git commit -m "feat(series): add SeriesManage page"
```

---

## Task 10: 管理后台 - 系列编辑页面

**Files:**
- Create: `blog-web/src/views/admin/SeriesEdit.vue`

- [ ] **Step 1: 创建 SeriesEdit.vue**

```vue
<template>
  <div class="series-edit">
    <div class="edit-card">
      <div class="edit-header">
        <h2 class="edit-title">{{ isEdit ? '编辑系列' : '新建系列' }}</h2>
      </div>

      <van-form ref="formRef" @submit="handleSubmit" class="edit-form">
        <van-cell-group inset>
          <van-field
            v-model="form.name"
            label="名称"
            placeholder="请输入系列名称"
            :rules="[{ required: true, message: '请输入系列名称' }]"
            maxlength="100"
            show-word-limit
          />

          <van-field
            v-model="form.description"
            label="介绍"
            type="textarea"
            rows="3"
            autosize
            placeholder="请输入系列介绍"
            maxlength="500"
            show-word-limit
          />
        </van-cell-group>

        <div class="form-section">
          <label class="form-label">封面</label>
          <div class="cover-upload" @click="triggerCoverUpload">
            <img v-if="form.coverImage" :src="form.coverImage" class="cover-preview" />
            <div v-else class="cover-placeholder">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
              </svg>
              <span>上传封面</span>
            </div>
          </div>
          <input ref="coverInputRef" type="file" accept="image/*" hidden @change="handleCoverChange" />
        </div>

        <van-cell-group inset>
          <van-field name="radio" label="模式">
            <template #input>
              <van-radio-group v-model="form.mode" direction="horizontal">
                <van-radio :value="0">有序（章节式）</van-radio>
                <van-radio :value="1">无序（主题式）</van-radio>
              </van-radio-group>
            </template>
          </van-field>

          <van-field name="switch" label="状态">
            <template #input>
              <van-switch v-model="form.status" :active-value="1" :inactive-value="0" size="small" />
            </template>
          </van-field>
        </van-cell-group>

        <div class="form-section">
          <label class="form-label">文章管理</label>
          <div class="article-list">
            <div v-for="(article, index) in seriesArticles" :key="article.id" class="article-item">
              <div v-if="form.mode === 0" class="chapter-order">
                <van-button size="small" :disabled="index === 0" @click="moveArticle(index, -1)">↑</van-button>
                <span class="order-num">第{{ index + 1 }}章</span>
                <van-button size="small" :disabled="index === seriesArticles.length - 1" @click="moveArticle(index, 1)">↓</van-button>
              </div>
              <div class="article-info">
                <span class="article-title">{{ article.title }}</span>
              </div>
              <van-button size="small" type="danger" @click="removeArticle(index)">移除</van-button>
            </div>
            <van-empty v-if="seriesArticles.length === 0" description="暂无文章" image-size="60" />
          </div>
          <van-button block type="default" @click="showArticlePicker = true" class="add-article-btn">
            添加文章
          </van-button>
        </div>

        <div class="form-actions">
          <van-button @click="$router.back()">取消</van-button>
          <van-button type="primary" native-type="submit">保存</van-button>
        </div>
      </van-form>
    </div>

    <!-- Article Picker -->
    <van-popup v-model:show="showArticlePicker" position="bottom" round style="height: 60%">
      <div class="article-picker">
        <div class="picker-header">
          <span>选择文章</span>
          <van-button size="small" @click="showArticlePicker = false">关闭</van-button>
        </div>
        <van-search v-model="articleSearchKeyword" placeholder="搜索文章" @search="searchArticles" />
        <div class="picker-content">
          <div
            v-for="article in availableArticles"
            :key="article.id"
            class="picker-item"
            @click="addArticle(article)"
          >
            <span>{{ article.title }}</span>
            <van-icon name="plus" />
          </div>
          <van-empty v-if="availableArticles.length === 0" description="暂无可添加的文章" />
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, shallowRef } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getAdminSeriesDetail, saveSeries, addArticlesToSeries, removeArticleFromSeries, updateArticlesOrder } from '@/api/series'
import { getAdminArticles } from '@/api/article'
import { uploadImage } from '@/api/admin'

const route = useRoute()
const router = useRouter()

const formRef = ref()
const coverInputRef = ref()
const showArticlePicker = ref(false)
const articleSearchKeyword = ref('')
const availableArticles = ref([])
const seriesArticles = ref([])

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  id: null,
  name: '',
  description: '',
  coverImage: '',
  mode: 0,
  sort: 0,
  status: 1
})

const triggerCoverUpload = () => {
  coverInputRef.value?.click()
}

const handleCoverChange = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  try {
    const res = await uploadImage(file)
    form.coverImage = res.data.url
    showToast({ type: 'success', message: '封面上传成功' })
  } catch (error) {
    console.error('上传封面失败', error)
    showToast('上传封面失败')
  }
  event.target.value = ''
}

const fetchSeries = async () => {
  if (!route.params.id) return
  try {
    const res = await getAdminSeriesDetail(route.params.id)
    const data = res.data
    form.id = data.id
    form.name = data.name
    form.description = data.description
    form.coverImage = data.coverImage
    form.mode = data.mode
    form.status = data.status
    seriesArticles.value = data.articles || []
  } catch (error) {
    console.error('获取系列失败', error)
  }
}

const searchArticles = async () => {
  try {
    const res = await getAdminArticles({
      pageNum: 1,
      pageSize: 20,
      title: articleSearchKeyword.value || undefined,
      status: 1
    })
    const existingIds = new Set(seriesArticles.value.map(a => a.id))
    availableArticles.value = (res.data?.records || []).filter(a => !existingIds.has(a.id))
  } catch (error) {
    console.error('搜索文章失败', error)
  }
}

const addArticle = async (article) => {
  if (isEdit.value && form.id) {
    try {
      await addArticlesToSeries(form.id, [article.id])
      seriesArticles.value.push({
        id: article.id,
        title: article.title,
        chapterOrder: seriesArticles.value.length + 1
      })
      showToast({ type: 'success', message: '添加成功' })
    } catch (error) {
      console.error('添加文章失败', error)
      showToast('添加失败')
    }
  } else {
    seriesArticles.value.push({
      id: article.id,
      title: article.title,
      chapterOrder: seriesArticles.value.length + 1
    })
  }
  showArticlePicker.value = false
}

const removeArticle = async (index) => {
  if (isEdit.value && form.id) {
    try {
      await removeArticleFromSeries(form.id, seriesArticles.value[index].id)
      seriesArticles.value.splice(index, 1)
      showToast({ type: 'success', message: '移除成功' })
    } catch (error) {
      console.error('移除文章失败', error)
      showToast('移除失败')
    }
  } else {
    seriesArticles.value.splice(index, 1)
  }
}

const moveArticle = async (index, direction) => {
  const newIndex = index + direction
  const temp = seriesArticles.value[index]
  seriesArticles.value[index] = seriesArticles.value[newIndex]
  seriesArticles.value[newIndex] = temp
  
  if (isEdit.value && form.id) {
    try {
      await updateArticlesOrder(form.id, seriesArticles.value.map(a => a.id))
    } catch (error) {
      console.error('更新顺序失败', error)
    }
  }
}

const handleSubmit = async () => {
  try {
    const data = {
      ...form,
      articleIds: isEdit.value ? undefined : seriesArticles.value.map(a => a.id)
    }
    await saveSeries(data)
    showToast({ type: 'success', message: isEdit.value ? '更新成功' : '创建成功' })
    router.push('/admin/series')
  } catch (error) {
    console.error('保存失败', error)
    showToast('保存失败')
  }
}

onMounted(() => {
  searchArticles()
  if (isEdit.value) {
    fetchSeries()
  }
})
</script>

<style lang="scss" scoped>
.series-edit {
  max-width: 900px;
}

.edit-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-2xl);
  overflow: hidden;
}

.edit-header {
  padding: var(--space-6);
  border-bottom: 1px solid var(--border-light);
}

.edit-title {
  font-size: var(--text-xl);
  font-weight: var(--font-semibold);
}

.edit-form {
  padding: var(--space-4);
}

.form-section {
  padding: var(--space-4);
  margin-bottom: var(--space-4);
}

.form-label {
  display: block;
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-primary);
  margin-bottom: var(--space-2);
}

.cover-upload {
  width: 200px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-secondary);
  border: 1px dashed var(--border-color);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);
  overflow: hidden;

  &:hover {
    border-color: var(--text-muted);
  }
}

.cover-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  color: var(--text-muted);

  svg {
    width: 24px;
    height: 24px;
  }

  span {
    font-size: var(--text-sm);
  }
}

.article-list {
  margin-bottom: var(--space-4);
}

.article-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-3);
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  margin-bottom: var(--space-2);
}

.chapter-order {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.order-num {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  min-width: 50px;
  text-align: center;
}

.article-info {
  flex: 1;
  padding: 0 var(--space-3);
}

.article-title {
  font-size: var(--text-sm);
}

.add-article-btn {
  margin-top: var(--space-2);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  margin-top: var(--space-6);
  padding: var(--space-4);
  border-top: 1px solid var(--border-light);
}

.article-picker {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.picker-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4);
  border-bottom: 1px solid var(--border-light);
}

.picker-content {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-4);
}

.picker-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-3);
  border-radius: var(--radius-md);
  cursor: pointer;
  
  &:hover {
    background: var(--bg-hover);
  }
}

:deep(.van-cell-group--inset) {
  margin: 0;
  margin-bottom: var(--space-4);
}

:deep(.van-field__label) {
  width: auto;
}
</style>
```

- [ ] **Step 2: 提交系列编辑页面**

```bash
git add blog-web/src/views/admin/SeriesEdit.vue
git commit -m "feat(series): add SeriesEdit page"
```

---

## Task 11: 门户前端 - 系列列表页

**Files:**
- Create: `blog-web/src/views/portal/SeriesList.vue`

- [ ] **Step 1: 创建 SeriesList.vue**

```vue
<template>
  <div class="series-list-page">
    <div class="page-header">
      <h1 class="page-title">系列文章</h1>
      <p class="page-desc">系统化的知识整理，按系列学习更高效</p>
    </div>

    <div v-if="loading" class="loading-container">
      <van-loading type="spinner" size="24px" color="var(--color-primary)" vertical>加载中...</van-loading>
    </div>

    <template v-else>
      <div class="series-grid">
        <router-link
          v-for="series in seriesList"
          :key="series.id"
          :to="'/series/' + series.id"
          class="series-card"
        >
          <div class="card-cover">
            <img v-if="series.coverImage" :src="series.coverImage" :alt="series.name" />
            <div v-else class="cover-placeholder">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 12.75V12A2.25 2.25 0 014.5 9.75h15A2.25 2.25 0 0121.75 12v.75m-8.69-6.44l-2.12-2.12a1.5 1.5 0 00-1.061-.44H4.5A2.25 2.25 0 002.25 6v12a2.25 2.25 0 002.25 2.25h15A2.25 2.25 0 0021.75 18V9a2.25 2.25 0 00-2.25-2.25h-5.379a1.5 1.5 0 01-1.06-.44z" />
              </svg>
            </div>
            <div class="mode-badge" :class="{ ordered: series.mode === 0 }">
              {{ series.mode === 0 ? '有序' : '无序' }}
            </div>
          </div>
          <div class="card-content">
            <h3 class="card-title">{{ series.name }}</h3>
            <p class="card-desc">{{ series.description || '暂无介绍' }}</p>
            <div class="card-meta">
              <span>{{ series.articleCount }} 篇文章</span>
              <span>{{ series.viewCount }} 次浏览</span>
            </div>
          </div>
        </router-link>
      </div>

      <van-empty v-if="seriesList.length === 0" description="暂无系列" />

      <div v-if="hasMore" class="load-more">
        <van-button block @click="loadMore" :loading="loadingMore">加载更多</van-button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getSeries } from '@/api/series'

const loading = ref(true)
const loadingMore = ref(false)
const seriesList = ref([])
const pageNum = ref(1)
const hasMore = ref(true)

const fetchSeries = async () => {
  try {
    const res = await getSeries({ pageNum: pageNum.value, pageSize: 12 })
    if (pageNum.value === 1) {
      seriesList.value = res.data?.records || []
    } else {
      seriesList.value.push(...(res.data?.records || []))
    }
    hasMore.value = seriesList.value.length < (res.data?.total || 0)
  } catch (error) {
    console.error('获取系列失败', error)
  }
}

const loadMore = async () => {
  loadingMore.value = true
  pageNum.value++
  await fetchSeries()
  loadingMore.value = false
}

onMounted(async () => {
  await fetchSeries()
  loading.value = false
})
</script>

<style lang="scss" scoped>
.series-list-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: var(--space-6);
}

.page-header {
  text-align: center;
  margin-bottom: var(--space-10);
}

.page-title {
  font-size: var(--text-3xl);
  font-weight: var(--font-bold);
  margin-bottom: var(--space-3);
}

.page-desc {
  font-size: var(--text-base);
  color: var(--text-secondary);
}

.loading-container {
  display: flex;
  justify-content: center;
  padding: var(--space-10);
}

.series-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--space-6);
}

.series-card {
  display: flex;
  flex-direction: column;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  overflow: hidden;
  text-decoration: none;
  transition: all var(--transition-base);

  &:hover {
    box-shadow: var(--shadow-lg);
    transform: translateY(-4px);
  }
}

.card-cover {
  position: relative;
  height: 160px;
  background: var(--bg-secondary);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  
  svg {
    width: 48px;
    height: 48px;
    color: var(--text-muted);
  }
}

.mode-badge {
  position: absolute;
  top: var(--space-3);
  right: var(--space-3);
  padding: var(--space-1) var(--space-2);
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
  color: white;
  background: var(--color-primary);
  border-radius: var(--radius-sm);

  &.ordered {
    background: #10b981;
  }
}

.card-content {
  flex: 1;
  padding: var(--space-4);
}

.card-title {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  margin-bottom: var(--space-2);
  color: var(--text-primary);
}

.card-desc {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: var(--leading-relaxed);
  margin-bottom: var(--space-3);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-meta {
  display: flex;
  gap: var(--space-4);
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.load-more {
  margin-top: var(--space-8);
}

@media (max-width: 768px) {
  .series-list-page {
    padding: var(--space-4);
  }

  .series-grid {
    grid-template-columns: 1fr;
  }
}
</style>
```

- [ ] **Step 2: 提交系列列表页**

```bash
git add blog-web/src/views/portal/SeriesList.vue
git commit -m "feat(series): add SeriesList portal page"
```

---

## Task 12: 门户前端 - 系列详情页

**Files:**
- Create: `blog-web/src/views/portal/SeriesDetail.vue`

- [ ] **Step 1: 创建 SeriesDetail.vue**

```vue
<template>
  <div class="series-detail-page">
    <div v-if="loading" class="loading-container">
      <van-loading type="spinner" size="24px" color="var(--color-primary)" vertical>加载中...</van-loading>
    </div>

    <template v-else-if="series">
      <!-- Series Header -->
      <div class="series-header">
        <div class="header-cover">
          <img v-if="series.coverImage" :src="series.coverImage" :alt="series.name" />
          <div v-else class="cover-placeholder">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 12.75V12A2.25 2.25 0 014.5 9.75h15A2.25 2.25 0 0121.75 12v.75m-8.69-6.44l-2.12-2.12a1.5 1.5 0 00-1.061-.44H4.5A2.25 2.25 0 002.25 6v12a2.25 2.25 0 002.25 2.25h15A2.25 2.25 0 0021.75 18V9a2.25 2.25 0 00-2.25-2.25h-5.379a1.5 1.5 0 01-1.06-.44z" />
            </svg>
          </div>
        </div>
        <div class="header-content">
          <div class="header-meta">
            <van-tag :type="series.mode === 0 ? 'success' : 'primary'">
              {{ series.mode === 0 ? '有序系列' : '无序系列' }}
            </van-tag>
            <span class="meta-item">{{ series.articleCount }} 篇文章</span>
            <span class="meta-item">{{ series.viewCount }} 次浏览</span>
          </div>
          <h1 class="header-title">{{ series.name }}</h1>
          <p class="header-desc">{{ series.description || '暂无介绍' }}</p>
        </div>
      </div>

      <!-- Articles List -->
      <div class="articles-section">
        <h2 class="section-title">{{ series.mode === 0 ? '章节目录' : '文章列表' }}</h2>
        <div class="articles-list">
          <router-link
            v-for="(article, index) in series.articles"
            :key="article.id"
            :to="'/article/' + article.id"
            class="article-item"
          >
            <div v-if="series.mode === 0" class="chapter-num">
              {{ index + 1 }}
            </div>
            <div class="article-content">
              <h3 class="article-title">{{ article.title }}</h3>
              <p class="article-summary">{{ article.summary }}</p>
              <div class="article-meta">
                <span>{{ article.viewCount }} 次阅读</span>
              </div>
            </div>
            <div class="article-arrow">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7" />
              </svg>
            </div>
          </router-link>
        </div>
        <van-empty v-if="!series.articles?.length" description="该系列暂无文章" />
      </div>
    </template>

    <van-empty v-else description="系列不存在" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getSeriesDetail } from '@/api/series'

const route = useRoute()
const loading = ref(true)
const series = ref(null)

const fetchSeries = async () => {
  try {
    const res = await getSeriesDetail(route.params.id)
    series.value = res.data
  } catch (error) {
    console.error('获取系列详情失败', error)
  }
}

onMounted(async () => {
  await fetchSeries()
  loading.value = false
})
</script>

<style lang="scss" scoped>
.series-detail-page {
  max-width: 800px;
  margin: 0 auto;
  padding: var(--space-6);
}

.loading-container {
  display: flex;
  justify-content: center;
  padding: var(--space-10);
}

.series-header {
  display: flex;
  gap: var(--space-6);
  padding: var(--space-6);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  margin-bottom: var(--space-8);
}

.header-cover {
  width: 200px;
  height: 140px;
  flex-shrink: 0;
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: var(--bg-secondary);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  
  svg {
    width: 48px;
    height: 48px;
    color: var(--text-muted);
  }
}

.header-content {
  flex: 1;
}

.header-meta {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-3);
}

.meta-item {
  font-size: var(--text-sm);
  color: var(--text-muted);
}

.header-title {
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  margin-bottom: var(--space-3);
}

.header-desc {
  font-size: var(--text-base);
  color: var(--text-secondary);
  line-height: var(--leading-relaxed);
}

.articles-section {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  padding: var(--space-6);
}

.section-title {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  margin-bottom: var(--space-4);
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--border-light);
}

.articles-list {
  display: flex;
  flex-direction: column;
}

.article-item {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4);
  border-radius: var(--radius-lg);
  text-decoration: none;
  transition: all var(--transition-fast);

  &:hover {
    background: var(--bg-hover);

    .article-title {
      color: var(--color-primary);
    }

    .article-arrow {
      transform: translateX(4px);
      opacity: 1;
    }
  }
}

.chapter-num {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--color-primary);
  background: var(--color-primary-light);
  border-radius: var(--radius-full);
  flex-shrink: 0;
}

.article-content {
  flex: 1;
  min-width: 0;
}

.article-title {
  font-size: var(--text-base);
  font-weight: var(--font-medium);
  margin-bottom: var(--space-1);
  transition: color var(--transition-fast);
}

.article-summary {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin-bottom: var(--space-2);
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-meta {
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.article-arrow {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  opacity: 0.4;
  transition: all var(--transition-fast);

  svg {
    width: 100%;
    height: 100%;
    color: var(--text-muted);
  }
}

@media (max-width: 768px) {
  .series-detail-page {
    padding: var(--space-4);
  }

  .series-header {
    flex-direction: column;
  }

  .header-cover {
    width: 100%;
    height: 180px;
  }
}
</style>
```

- [ ] **Step 2: 提交系列详情页**

```bash
git add blog-web/src/views/portal/SeriesDetail.vue
git commit -m "feat(series): add SeriesDetail portal page"
```

---

## Task 13: 首页侧边栏 - 热门系列

**Files:**
- Modify: `blog-web/src/views/portal/Home.vue`

- [ ] **Step 1: 在 Home.vue 中添加热门系列模块**

1. 在 `<script setup>` 部分添加导入和数据：

```javascript
import { getSeries, getHotSeries } from '@/api/series'

const hotSeries = ref([])

const fetchHotSeries = async () => {
  try {
    const res = await getHotSeries(5)
    hotSeries.value = res.data || []
  } catch (error) {
    console.error('获取热门系列失败', error)
  }
}
```

2. 在 `onMounted` 的 `Promise.all` 中添加 `fetchHotSeries()` 调用

3. 在侧边栏（`<aside class="sidebar">`）中，在热门文章 section 之前添加：

```vue
        <!-- Hot Series -->
        <section v-if="hotSeries.length > 0" class="sidebar-section">
          <div class="section-title-row">
            <svg class="section-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 12.75V12A2.25 2.25 0 014.5 9.75h15A2.25 2.25 0 0121.75 12v.75m-8.69-6.44l-2.12-2.12a1.5 1.5 0 00-1.061-.44H4.5A2.25 2.25 0 002.25 6v12a2.25 2.25 0 002.25 2.25h15A2.25 2.25 0 0021.75 18V9a2.25 2.25 0 00-2.25-2.25h-5.379a1.5 1.5 0 01-1.06-.44z" />
            </svg>
            <h4 class="sidebar-title">热门系列</h4>
          </div>
          <div class="series-list">
            <router-link
              v-for="series in hotSeries"
              :key="series.id"
              :to="'/series/' + series.id"
              class="series-item"
            >
              <span class="series-name">{{ series.name }}</span>
              <span class="series-count">{{ series.articleCount }}篇</span>
            </router-link>
          </div>
        </section>
```

4. 在 `<style>` 部分添加样式：

```scss
// Hot Series
.series-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.series-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-3);
  border-radius: var(--radius-lg);
  text-decoration: none;
  transition: all var(--transition-fast);

  &:hover {
    background: var(--bg-hover);
    transform: translateX(4px);

    .series-name {
      color: var(--color-primary);
    }
  }
}

.series-name {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  transition: color var(--transition-fast);
}

.series-count {
  font-size: var(--text-xs);
  color: var(--text-muted);
}
```

- [ ] **Step 2: 提交首页修改**

```bash
git add blog-web/src/views/portal/Home.vue
git commit -m "feat(series): add hot series section to Home sidebar"
```

---

## Task 14: 文章详情页 - 显示所属系列

**Files:**
- Modify: `blog-web/src/views/portal/ArticleDetail.vue`
- Modify: `blog-server/src/main/java/com/blog/domain/vo/ArticleVO.java`
- Modify: `blog-server/src/main/java/com/blog/service/impl/ArticleServiceImpl.java`

- [ ] **Step 1: 在 ArticleVO 中添加系列字段**

在 `ArticleVO.java` 中添加：

```java
    private List<SeriesSimpleVO> series;

    @Data
    public static class SeriesSimpleVO {
        private Long id;
        private String name;
    }
```

- [ ] **Step 2: 在 ArticleServiceImpl 中填充系列信息**

在 `getArticleById` 方法中，在设置标签之后添加：

```java
        // 设置所属系列
        List<Long> seriesIds = seriesArticleMapper.selectSeriesIdsByArticleId(id);
        if (!seriesIds.isEmpty()) {
            List<Series> seriesList = seriesMapper.selectBatchIds(seriesIds);
            vo.setSeries(seriesList.stream()
                    .filter(s -> s.getDeleted() == 0 && s.getStatus() == 1)
                    .map(s -> {
                        ArticleVO.SeriesSimpleVO svo = new ArticleVO.SeriesSimpleVO();
                        svo.setId(s.getId());
                        svo.setName(s.getName());
                        return svo;
                    })
                    .collect(Collectors.toList()));
        }
```

需要在 `ArticleServiceImpl` 中注入 `SeriesMapper` 和 `SeriesArticleMapper`：

```java
    private final SeriesMapper seriesMapper;
    private final SeriesArticleMapper seriesArticleMapper;
```

- [ ] **Step 3: 在前端 ArticleDetail.vue 中显示系列**

在文章标题下方添加系列信息显示：

```vue
<div v-if="article.series?.length" class="article-series">
  <span class="series-label">所属系列：</span>
  <router-link
    v-for="s in article.series"
    :key="s.id"
    :to="'/series/' + s.id"
    class="series-link"
  >
    {{ s.name }}
  </router-link>
</div>
```

添加样式：

```scss
.article-series {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
}

.series-label {
  font-size: var(--text-sm);
  color: var(--text-muted);
}

.series-link {
  font-size: var(--text-sm);
  color: var(--color-primary);
  text-decoration: none;
  
  &:hover {
    text-decoration: underline;
  }
}
```

- [ ] **Step 4: 提交文章详情页修改**

```bash
git add blog-server/src/main/java/com/blog/domain/vo/ArticleVO.java
git add blog-server/src/main/java/com/blog/service/impl/ArticleServiceImpl.java
git add blog-web/src/views/portal/ArticleDetail.vue
git commit -m "feat(series): show series info in article detail page"
```

---

## Task 15: 管理后台菜单 - 添加系列管理入口

**Files:**
- Modify: `blog-web/src/views/admin/Layout.vue`

- [ ] **Step 1: 在管理后台侧边栏菜单中添加系列管理**

在 `Layout.vue` 的菜单列表中，在「评论管理」之后添加：

```vue
      {
        name: 'SeriesManage',
        title: '系列管理',
        icon: 'apps-o'
      },
```

（具体实现方式取决于 Layout.vue 的菜单结构）

- [ ] **Step 2: 提交菜单修改**

```bash
git add blog-web/src/views/admin/Layout.vue
git commit -m "feat(series): add series menu item to admin layout"
```

---

## Task 16: 最终验证与提交

- [ ] **Step 1: 验证后端编译**

```bash
cd blog-server && mvn clean compile -DskipTests
```

- [ ] **Step 2: 验证前端编译**

```bash
cd blog-web && pnpm build
```

- [ ] **Step 3: 最终提交（如有遗漏文件）**

```bash
git status
git add -A
git commit -m "feat(series): complete article series feature implementation"
```

---

## Spec Coverage Check

| Spec 需求 | 对应任务 |
|-----------|----------|
| series 表 | Task 1 |
| series_article 表 | Task 1 |
| Series/SeriesArticle 实体 | Task 2 |
| DTO/VO | Task 3 |
| Mapper | Task 4 |
| Service | Task 5 |
| Admin API | Task 6 |
| Portal API | Task 6 |
| 前端 API 模块 | Task 7 |
| 路由配置 | Task 8 |
| 系列管理页面 | Task 9 |
| 系列编辑页面 | Task 10 |
| 门户系列列表页 | Task 11 |
| 门户系列详情页 | Task 12 |
| 首页热门系列 | Task 13 |
| 文章详情显示系列 | Task 14 |
| 管理后台菜单 | Task 15 |

所有需求已覆盖。
