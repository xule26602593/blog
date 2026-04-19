# 用户交互功能增强实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为博客系统添加我的收藏列表、阅读历史记录、文章分享三项用户交互功能。

**Architecture:** 后端使用 Spring Boot 服务层模式，前端使用 Vue 3 组件化开发。收藏列表复用现有 `user_action` 表，阅读历史新建专用表，分享功能纯前端实现。

**Tech Stack:** Spring Boot 3 + MyBatis Plus + Vue 3 + Vant 4 + qrcode-generator

---

## 文件结构

### 后端新建文件

```
blog-server/src/main/java/com/blog/
├── controller/portal/
│   ├── FavoriteController.java      # 收藏列表接口
│   └── ReadingHistoryController.java # 阅读历史接口
├── domain/
│   ├── entity/
│   │   └── ReadingHistory.java      # 阅读历史实体
│   ├── vo/
│   │   ├── FavoriteVO.java          # 收藏列表VO
│   │   └── ReadingHistoryVO.java    # 阅读历史VO
│   └── dto/
│       └── ReadingHistoryQueryDTO.java # 阅读历史查询DTO
├── repository/mapper/
│   └── ReadingHistoryMapper.java    # 阅读历史Mapper
├── service/
│   ├── FavoriteService.java         # 收藏服务接口
│   ├── ReadingHistoryService.java   # 阅读历史服务接口
│   └── impl/
│       ├── FavoriteServiceImpl.java # 收藏服务实现
│       └── ReadingHistoryServiceImpl.java # 阅读历史服务实现
└── resources/db/
    └── migration/
        └── V001__create_reading_history.sql # 阅读历史表迁移脚本
```

### 前端新建/修改文件

```
blog-web/src/
├── api/
│   ├── favorite.js                  # 收藏API（新建）
│   └── history.js                   # 阅读历史API（新建）
├── views/portal/
│   ├── UserCenter.vue               # 改为标签页布局（修改）
│   ├── UserProfile.vue              # 个人资料组件（新建）
│   ├── UserFavorites.vue            # 收藏列表组件（新建）
│   └── UserHistory.vue              # 阅读历史组件（新建）
├── components/
│   └── SharePanel.vue               # 分享面板组件（新建）
└── router/index.js                  # 添加子路由（修改）
```

---

## 第一阶段：我的收藏列表

### Task 1: 创建收藏列表 VO

**Files:**
- Create: `blog-server/src/main/java/com/blog/domain/vo/FavoriteVO.java`

- [ ] **Step 1: 创建 FavoriteVO 类**

```java
package com.blog.domain.vo;

import lombok.Data;

@Data
public class FavoriteVO {

    private Long id;

    private Long articleId;

    private String title;

    private String summary;

    private String coverImage;

    private String authorName;

    private String categoryName;

    private Long viewCount;

    private Long likeCount;

    private String favoriteTime;
}
```

- [ ] **Step 2: 验证文件创建**

确认文件 `blog-server/src/main/java/com/blog/domain/vo/FavoriteVO.java` 已创建。

---

### Task 2: 创建收藏服务接口

**Files:**
- Create: `blog-server/src/main/java/com/blog/service/FavoriteService.java`

- [ ] **Step 1: 创建 FavoriteService 接口**

```java
package com.blog.service;

import com.blog.common.result.PageResult;
import com.blog.domain.vo.FavoriteVO;

public interface FavoriteService {

    /**
     * 获取当前用户的收藏列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param keyword 关键词（可选）
     * @return 收藏列表
     */
    PageResult<FavoriteVO> getFavorites(int pageNum, int pageSize, String keyword);
}
```

- [ ] **Step 2: 验证文件创建**

确认文件 `blog-server/src/main/java/com/blog/service/FavoriteService.java` 已创建。

---

### Task 3: 创建收藏服务实现类

**Files:**
- Create: `blog-server/src/main/java/com/blog/service/impl/FavoriteServiceImpl.java`

- [ ] **Step 1: 创建 FavoriteServiceImpl 类**

```java
package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.result.PageResult;
import com.blog.domain.entity.Article;
import com.blog.domain.entity.UserAction;
import com.blog.domain.vo.FavoriteVO;
import com.blog.repository.mapper.ArticleMapper;
import com.blog.repository.mapper.UserActionMapper;
import com.blog.security.LoginUser;
import com.blog.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final UserActionMapper userActionMapper;
    private final ArticleMapper articleMapper;

    @Override
    public PageResult<FavoriteVO> getFavorites(int pageNum, int pageSize, String keyword) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return PageResult.of(List.of(), 0L, (long) pageSize, (long) pageNum);
        }

        // 查询用户的收藏记录（action_type = 2）
        LambdaQueryWrapper<UserAction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAction::getUserId, userId)
               .eq(UserAction::getActionType, 2)
               .orderByDesc(UserAction::getCreateTime);

        List<UserAction> allFavorites = userActionMapper.selectList(wrapper);

        // 过滤并构建VO
        List<FavoriteVO> allVOs = allFavorites.stream()
                .map(action -> {
                    Article article = articleMapper.selectById(action.getArticleId());
                    if (article == null || article.getDeleted() == 1 || article.getStatus() != 1) {
                        return null;
                    }
                    // 关键词过滤
                    if (StringUtils.hasText(keyword) && !article.getTitle().contains(keyword)) {
                        return null;
                    }
                    return buildFavoriteVO(action, article);
                })
                .filter(vo -> vo != null)
                .collect(Collectors.toList());

        // 分页
        long total = allVOs.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, allVOs.size());
        List<FavoriteVO> records = start < allVOs.size() ? allVOs.subList(start, end) : List.of();

        return PageResult.of(records, total, (long) pageSize, (long) pageNum);
    }

    private FavoriteVO buildFavoriteVO(UserAction action, Article article) {
        FavoriteVO vo = new FavoriteVO();
        vo.setId(action.getId());
        vo.setArticleId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setSummary(article.getSummary());
        vo.setCoverImage(article.getCoverImage());
        vo.setViewCount(article.getViewCount());
        vo.setLikeCount(article.getLikeCount());
        vo.setFavoriteTime(action.getCreateTime().toString());
        return vo;
    }

    private Long getCurrentUserId() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser.getUserId();
        }
        return null;
    }
}
```

- [ ] **Step 2: 验证文件创建**

确认文件 `blog-server/src/main/java/com/blog/service/impl/FavoriteServiceImpl.java` 已创建。

---

### Task 4: 创建收藏控制器

**Files:**
- Create: `blog-server/src/main/java/com/blog/controller/portal/FavoriteController.java`

- [ ] **Step 1: 创建 FavoriteController 类**

```java
package com.blog.controller.portal;

import com.blog.common.result.PageResult;
import com.blog.common.result.Result;
import com.blog.domain.vo.FavoriteVO;
import com.blog.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "前台收藏接口")
@RestController
@RequestMapping("/api/portal")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(summary = "获取收藏列表")
    @GetMapping("/favorites")
    public Result<PageResult<FavoriteVO>> getFavorites(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.success(favoriteService.getFavorites(pageNum, pageSize, keyword));
    }
}
```

- [ ] **Step 2: 验证文件创建**

确认文件 `blog-server/src/main/java/com/blog/controller/portal/FavoriteController.java` 已创建。

---

### Task 5: 创建前端收藏 API

**Files:**
- Create: `blog-web/src/api/favorite.js`

- [ ] **Step 1: 创建收藏 API 模块**

```javascript
import request from '@/utils/request'

// 获取收藏列表
export function getFavorites(params) {
  return request.get('/api/portal/favorites', { params })
}

// 收藏/取消收藏文章（复用现有API）
export function favoriteArticle(id) {
  return request.post(`/api/portal/article/${id}/favorite`)
}
```

- [ ] **Step 2: 验证文件创建**

确认文件 `blog-web/src/api/favorite.js` 已创建。

---

### Task 6: 创建个人资料组件

**Files:**
- Create: `blog-web/src/views/portal/UserProfile.vue`

- [ ] **Step 1: 创建 UserProfile 组件**

从现有 `UserCenter.vue` 提取个人资料和修改密码部分：

```vue
<template>
  <div class="panels">
    <section class="panel">
      <h2 class="panel-title">个人资料</h2>
      <div class="form">
        <div class="form-group">
          <label class="form-label">用户名</label>
          <input :value="userStore.userInfo?.username" disabled class="form-input disabled" />
        </div>
        <div class="form-group">
          <label class="form-label">昵称</label>
          <input v-model="form.nickname" class="form-input" />
        </div>
        <div class="form-group">
          <label class="form-label">邮箱</label>
          <input v-model="form.email" type="email" class="form-input" />
        </div>
        <div class="form-group">
          <label class="form-label">头像</label>
          <div class="avatar-upload">
            <input
              type="file"
              ref="avatarInput"
              accept="image/*"
              @change="handleFileChange"
              hidden
            />
            <div class="avatar-preview" @click="avatarInput?.click()">
              <div v-if="form.avatar" class="avatar-image" :style="{ backgroundImage: `url(${form.avatar})` }"></div>
              <div v-else class="avatar-placeholder">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
                </svg>
              </div>
            </div>
            <div class="upload-info">
              <p class="upload-title">上传头像</p>
              <p class="upload-desc">支持常见图片格式</p>
            </div>
          </div>
        </div>
        <button class="save-btn" @click="handleUpdate">保存修改</button>
      </div>
    </section>

    <section class="panel">
      <h2 class="panel-title">修改密码</h2>
      <div class="form">
        <div class="form-group">
          <label class="form-label">原密码</label>
          <div class="password-input">
            <input
              v-model="passwordForm.oldPassword"
              :type="showOldPassword ? 'text' : 'password'"
              class="form-input"
            />
            <button type="button" class="toggle-password" @click="showOldPassword = !showOldPassword">
              <svg v-if="showOldPassword" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M3.98 8.223A10.477 10.477 0 001.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.45 10.45 0 0112 4.5c4.756 0 8.773 3.162 10.065 7.498a10.523 10.523 0 01-4.293 5.774M6.228 6.228L3 3m3.228 3.228l3.65 3.65m7.894 7.894L21 21m-3.228-3.228l-3.65-3.65m0 0a3 3 0 10-4.243-4.243m4.242 4.242L9.88 9.88" />
              </svg>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z" />
                <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
            </button>
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">新密码</label>
          <div class="password-input">
            <input
              v-model="passwordForm.newPassword"
              :type="showNewPassword ? 'text' : 'password'"
              class="form-input"
            />
            <button type="button" class="toggle-password" @click="showNewPassword = !showNewPassword">
              <svg v-if="showNewPassword" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M3.98 8.223A10.477 10.477 0 001.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.45 10.45 0 0112 4.5c4.756 0 8.773 3.162 10.065 7.498a10.523 10.523 0 01-4.293 5.774M6.228 6.228L3 3m3.228 3.228l3.65 3.65m7.894 7.894L21 21m-3.228-3.228l-3.65-3.65m0 0a3 3 0 10-4.243-4.243m4.242 4.242L9.88 9.88" />
              </svg>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z" />
                <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
            </button>
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">确认新密码</label>
          <div class="password-input">
            <input
              v-model="passwordForm.confirmPassword"
              :type="showConfirmPassword ? 'text' : 'password'"
              class="form-input"
            />
            <button type="button" class="toggle-password" @click="showConfirmPassword = !showConfirmPassword">
              <svg v-if="showConfirmPassword" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M3.98 8.223A10.477 10.477 0 001.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.45 10.45 0 0112 4.5c4.756 0 8.773 3.162 10.065 7.498a10.523 10.523 0 01-4.293 5.774M6.228 6.228L3 3m3.228 3.228l3.65 3.65m7.894 7.894L21 21m-3.228-3.228l-3.65-3.65m0 0a3 3 0 10-4.243-4.243m4.242 4.242L9.88 9.88" />
              </svg>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z" />
                <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
            </button>
          </div>
        </div>
        <button class="save-btn secondary" @click="handlePasswordUpdate">更新密码</button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { showToast } from 'vant'
import { useUserStore } from '@/stores/user'
import { updateCurrentUser, updatePassword } from '@/api/auth'
import { uploadImage } from '@/api/admin'

const userStore = useUserStore()
const avatarInput = ref()

const form = reactive({
  nickname: '',
  email: '',
  avatar: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const showOldPassword = ref(false)
const showNewPassword = ref(false)
const showConfirmPassword = ref(false)

const handleFileChange = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return

  try {
    const res = await uploadImage(file)
    form.avatar = res.data.url
    showToast({ type: 'success', message: '头像上传成功' })
  } catch (error) {
    console.error('上传失败', error)
  }
}

const handleUpdate = async () => {
  try {
    await updateCurrentUser(form)
    userStore.updateUserInfo(form)
    showToast({ type: 'success', message: '保存成功' })
  } catch (error) {
    console.error('保存失败', error)
  }
}

const handlePasswordUpdate = async () => {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    showToast('请填写完整信息')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    showToast('两次密码输入不一致')
    return
  }
  try {
    await updatePassword(passwordForm.oldPassword, passwordForm.newPassword)
    showToast({ type: 'success', message: '密码修改成功' })
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (error) {
    console.error('修改密码失败', error)
  }
}

onMounted(() => {
  form.nickname = userStore.userInfo?.nickname || ''
  form.email = userStore.userInfo?.email || ''
  form.avatar = userStore.userInfo?.avatar || ''
})
</script>

<style lang="scss" scoped>
.panels {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-6);
}

.panel {
  padding: var(--space-8);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-2xl);
}

.panel-title {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  margin-bottom: var(--space-6);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--border-light);
}

.form {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.form-label {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
}

.form-input {
  height: 44px;
  padding: 0 var(--space-4);
  font-size: var(--text-sm);
  font-family: var(--font-sans);
  color: var(--text-primary);
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  outline: none;
  transition: all var(--transition-fast);
  width: 100%;

  &::placeholder {
    color: var(--text-muted);
  }

  &:focus {
    border-color: var(--color-primary);
    box-shadow: 0 0 0 3px rgba(180, 83, 9, 0.15);
  }

  &.disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.password-input {
  position: relative;

  .form-input {
    padding-right: 44px;
  }

  .toggle-password {
    position: absolute;
    right: 12px;
    top: 50%;
    transform: translateY(-50%);
    padding: var(--space-1);
    background: none;
    border: none;
    color: var(--text-muted);
    cursor: pointer;
    transition: color var(--transition-fast);

    &:hover {
      color: var(--text-primary);
    }

    svg {
      width: 18px;
      height: 18px;
    }
  }
}

.avatar-upload {
  display: flex;
  align-items: center;
  gap: var(--space-5);
}

.avatar-preview {
  cursor: pointer;
}

.avatar-image {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-full);
  background-size: cover;
  background-position: center;
  border: 2px solid var(--border-color);
  transition: all var(--transition-fast);

  &:hover {
    border-color: var(--color-primary);
    box-shadow: 0 0 0 4px rgba(180, 83, 9, 0.15);
  }
}

.avatar-placeholder {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-secondary);
  border: 1px dashed var(--border-color);
  border-radius: var(--radius-full);
  color: var(--text-muted);
  transition: all var(--transition-fast);

  svg {
    width: 24px;
    height: 24px;
  }

  &:hover {
    color: var(--text-primary);
    border-color: var(--text-muted);
  }
}

.upload-info {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.upload-title {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-primary);
}

.upload-desc {
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.save-btn {
  height: 44px;
  padding: 0 var(--space-6);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: white;
  background: var(--gradient-primary);
  border: none;
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);
  }

  &:active {
    transform: translateY(0);
  }

  &.secondary {
    color: var(--color-primary);
    background: transparent;
    border: 1px solid var(--color-primary);

    &:hover {
      color: white;
      background: var(--gradient-primary);
    }
  }
}

@media (max-width: 768px) {
  .panels {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .panel {
    padding: var(--space-5);
  }

  .avatar-upload {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
```

- [ ] **Step 2: 验证文件创建**

确认文件 `blog-web/src/views/portal/UserProfile.vue` 已创建。

---

### Task 7: 创建收藏列表组件

**Files:**
- Create: `blog-web/src/views/portal/UserFavorites.vue`

- [ ] **Step 1: 创建 UserFavorites 组件**

```vue
<template>
  <div class="favorites-page">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <input
        v-model="keyword"
        type="text"
        placeholder="搜索收藏的文章..."
        class="search-input"
        @keyup.enter="handleSearch"
      />
      <button class="search-btn" @click="handleSearch">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
        </svg>
      </button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <van-loading type="spinner" size="24px" color="var(--color-primary)" vertical>加载中...</van-loading>
    </div>

    <!-- 空状态 -->
    <div v-else-if="favorites.length === 0" class="empty-state">
      <div class="empty-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M17.593 3.322c1.1.128 1.907 1.077 1.907 2.185V21L12 17.25 4.5 21V5.507c0-1.108.806-2.057 1.907-2.185a48.507 48.507 0 0111.186 0z" />
        </svg>
      </div>
      <p class="empty-text">{{ keyword ? '未找到匹配的文章' : '暂无收藏的文章' }}</p>
      <p class="empty-hint">{{ keyword ? '试试其他关键词' : '浏览文章时点击收藏按钮即可添加' }}</p>
    </div>

    <!-- 收藏列表 -->
    <div v-else class="favorites-list">
      <div
        v-for="item in favorites"
        :key="item.id"
        class="favorite-card"
        @click="goToArticle(item.articleId)"
      >
        <div v-if="item.coverImage" class="card-cover">
          <img :src="item.coverImage" :alt="item.title" loading="lazy" />
        </div>
        <div class="card-content">
          <h3 class="card-title">{{ item.title }}</h3>
          <p class="card-summary">{{ item.summary }}</p>
          <div class="card-meta">
            <span class="meta-item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                <path stroke-linecap="round" stroke-linejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
              </svg>
              {{ item.viewCount }}
            </span>
            <span class="meta-item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M11.48 3.499a.562.562 0 011.04 0l2.125 5.111a.563.563 0 00.475.345l5.518.442c.499.04.701.663.321.988l-4.204 3.602a.563.563 0 00-.182.557l1.285 5.385a.562.562 0 01-.84.61l-4.725-2.885a.563.563 0 00-.586 0L6.982 20.54a.562.562 0 01-.84-.61l1.285-5.386a.562.562 0 00-.182-.557l-4.204-3.602a.563.563 0 01.321-.988l5.518-.442a.563.563 0 00.475-.345L11.48 3.5z" />
              </svg>
              {{ item.likeCount }}
            </span>
            <span class="meta-time">收藏于 {{ formatDate(item.favoriteTime) }}</span>
          </div>
        </div>
        <button class="remove-btn" @click.stop="handleRemove(item)">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="total > pageSize" class="pagination">
      <button
        class="page-btn"
        :disabled="pageNum === 1"
        @click="changePage(pageNum - 1)"
      >
        上一页
      </button>
      <span class="page-info">{{ pageNum }} / {{ totalPages }}</span>
      <button
        class="page-btn"
        :disabled="pageNum >= totalPages"
        @click="changePage(pageNum + 1)"
      >
        下一页
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { getFavorites, favoriteArticle } from '@/api/favorite'
import dayjs from 'dayjs'

const router = useRouter()

const loading = ref(false)
const favorites = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const fetchFavorites = async () => {
  loading.value = true
  try {
    const res = await getFavorites({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined
    })
    favorites.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('获取收藏列表失败', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  fetchFavorites()
}

const changePage = (page) => {
  pageNum.value = page
  fetchFavorites()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const goToArticle = (articleId) => {
  router.push(`/article/${articleId}`)
}

const handleRemove = async (item) => {
  try {
    await showConfirmDialog({
      title: '取消收藏',
      message: '确定要取消收藏这篇文章吗？'
    })
    await favoriteArticle(item.articleId)
    showToast({ type: 'success', message: '已取消收藏' })
    fetchFavorites()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消收藏失败', error)
    }
  }
}

const formatDate = (date) => dayjs(date).format('YYYY-MM-DD')

onMounted(() => {
  fetchFavorites()
})
</script>

<style lang="scss" scoped>
.favorites-page {
  max-width: 900px;
  margin: 0 auto;
}

.search-bar {
  display: flex;
  gap: var(--space-2);
  margin-bottom: var(--space-6);
}

.search-input {
  flex: 1;
  height: 44px;
  padding: 0 var(--space-4);
  font-size: var(--text-sm);
  color: var(--text-primary);
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  outline: none;
  transition: all var(--transition-fast);

  &:focus {
    border-color: var(--color-primary);
    box-shadow: 0 0 0 3px rgba(180, 83, 9, 0.15);
  }

  &::placeholder {
    color: var(--text-muted);
  }
}

.search-btn {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-primary);
  border: none;
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);

  svg {
    width: 20px;
    height: 20px;
    color: white;
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);
  }
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 40vh;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--space-16) var(--space-6);
  color: var(--text-muted);
}

.empty-icon {
  margin-bottom: var(--space-6);

  svg {
    width: 64px;
    height: 64px;
    opacity: 0.5;
  }
}

.empty-text {
  font-size: var(--text-lg);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
  margin-bottom: var(--space-2);
}

.empty-hint {
  font-size: var(--text-sm);
}

.favorites-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.favorite-card {
  display: flex;
  gap: var(--space-4);
  padding: var(--space-4);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  cursor: pointer;
  transition: all var(--transition-fast);
  position: relative;

  &:hover {
    border-color: var(--color-primary);
    box-shadow: var(--shadow-md);
    transform: translateY(-2px);
  }
}

.card-cover {
  flex-shrink: 0;
  width: 120px;
  height: 80px;
  border-radius: var(--radius-md);
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.card-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.card-title {
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-summary {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: var(--space-1);

  svg {
    width: 14px;
    height: 14px;
  }
}

.meta-time {
  margin-left: auto;
}

.remove-btn {
  position: absolute;
  top: var(--space-2);
  right: var(--space-2);
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: var(--radius-full);
  color: var(--text-muted);
  cursor: pointer;
  opacity: 0;
  transition: all var(--transition-fast);

  svg {
    width: 18px;
    height: 18px;
  }

  &:hover {
    background: var(--bg-hover);
    color: var(--color-danger, #ef4444);
  }
}

.favorite-card:hover .remove-btn {
  opacity: 1;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-4);
  margin-top: var(--space-8);
}

.page-btn {
  height: 36px;
  padding: 0 var(--space-4);
  font-size: var(--text-sm);
  color: var(--text-secondary);
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);

  &:hover:not(:disabled) {
    color: var(--color-primary);
    border-color: var(--color-primary);
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.page-info {
  font-size: var(--text-sm);
  color: var(--text-muted);
}

@media (max-width: 640px) {
  .card-cover {
    width: 80px;
    height: 60px;
  }

  .card-summary {
    display: none;
  }

  .remove-btn {
    opacity: 1;
  }

  .meta-time {
    display: none;
  }
}
</style>
```

- [ ] **Step 2: 验证文件创建**

确认文件 `blog-web/src/views/portal/UserFavorites.vue` 已创建。

---

### Task 8: 重构 UserCenter 为标签页布局

**Files:**
- Modify: `blog-web/src/views/portal/UserCenter.vue`

- [ ] **Step 1: 重写 UserCenter 组件**

将 UserCenter 改为标签页容器：

```vue
<template>
  <div class="user-center">
    <header class="page-header">
      <h1 class="page-title">个人中心</h1>
      <p class="page-desc">管理您的账号和收藏</p>
    </header>

    <!-- 标签导航 -->
    <nav class="tabs">
      <router-link
        v-for="tab in tabs"
        :key="tab.path"
        :to="tab.path"
        class="tab-item"
        :class="{ active: isActive(tab.path) }"
      >
        <component :is="tab.icon" class="tab-icon" />
        <span>{{ tab.label }}</span>
      </router-link>
    </nav>

    <!-- 子路由内容 -->
    <router-view v-slot="{ Component }">
      <transition name="fade" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
  </div>
</template>

<script setup>
import { computed, h } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const tabs = [
  {
    path: '/user/profile',
    label: '个人资料',
    icon: h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.5' }, [
      h('path', { 'stroke-linecap': 'round', 'stroke-linejoin': 'round', d: 'M15.75 6a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0zM4.501 20.118a7.5 7.5 0 0114.998 0A17.933 17.933 0 0112 21.75c-2.676 0-5.216-.584-7.499-1.632z' })
    ])
  },
  {
    path: '/user/favorites',
    label: '我的收藏',
    icon: h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.5' }, [
      h('path', { 'stroke-linecap': 'round', 'stroke-linejoin': 'round', d: 'M17.593 3.322c1.1.128 1.907 1.077 1.907 2.185V21L12 17.25 4.5 21V5.507c0-1.108.806-2.057 1.907-2.185a48.507 48.507 0 0111.186 0z' })
    ])
  },
  {
    path: '/user/history',
    label: '阅读历史',
    icon: h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.5' }, [
      h('path', { 'stroke-linecap': 'round', 'stroke-linejoin': 'round', d: 'M12 6v6h4.5m4.5 0a9 9 0 11-18 0 9 9 0 0118 0z' })
    ])
  }
]

const isActive = (path) => {
  return route.path === path || route.path.startsWith(path + '/')
}
</script>

<style lang="scss" scoped>
.user-center {
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: var(--space-6);
}

.page-title {
  font-size: var(--text-4xl);
  font-weight: var(--font-bold);
  letter-spacing: -0.02em;
  margin-bottom: var(--space-2);
}

.page-desc {
  font-size: var(--text-lg);
  color: var(--text-secondary);
}

.tabs {
  display: flex;
  gap: var(--space-2);
  margin-bottom: var(--space-6);
  padding: var(--space-1);
  background: var(--bg-secondary);
  border-radius: var(--radius-xl);
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
  text-decoration: none;
  border-radius: var(--radius-lg);
  transition: all var(--transition-fast);

  &:hover {
    color: var(--text-primary);
  }

  &.active {
    color: white;
    background: var(--gradient-primary);
    box-shadow: var(--shadow-sm);
  }
}

.tab-icon {
  width: 18px;
  height: 18px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 640px) {
  .page-title {
    font-size: var(--text-3xl);
  }

  .tabs {
    flex-wrap: wrap;
  }

  .tab-item {
    flex: 1 1 calc(50% - var(--space-1));
  }
}
</style>
```

- [ ] **Step 2: 验证修改**

确认 `blog-web/src/views/portal/UserCenter.vue` 已更新为标签页布局。

---

### Task 9: 更新路由配置

**Files:**
- Modify: `blog-web/src/router/index.js`

- [ ] **Step 1: 更新路由配置**

找到 `/user` 路由，添加子路由：

```javascript
// 找到这部分代码
{
  path: 'user',
  name: 'UserCenter',
  component: () => import('@/views/portal/UserCenter.vue'),
  meta: { title: '个人中心', requiresAuth: true }
}

// 替换为
{
  path: 'user',
  name: 'UserCenter',
  component: () => import('@/views/portal/UserCenter.vue'),
  meta: { title: '个人中心', requiresAuth: true },
  children: [
    {
      path: '',
      redirect: 'profile'
    },
    {
      path: 'profile',
      name: 'UserProfile',
      component: () => import('@/views/portal/UserProfile.vue'),
      meta: { title: '个人资料' }
    },
    {
      path: 'favorites',
      name: 'UserFavorites',
      component: () => import('@/views/portal/UserFavorites.vue'),
      meta: { title: '我的收藏' }
    },
    {
      path: 'history',
      name: 'UserHistory',
      component: () => import('@/views/portal/UserHistory.vue'),
      meta: { title: '阅读历史' }
    }
  ]
}
```

- [ ] **Step 2: 验证路由配置**

确认路由文件已正确更新。

---

### Task 10: 提交第一阶段代码

- [ ] **Step 1: 添加文件到暂存区**

```bash
cd D:/project/test1
git add blog-server/src/main/java/com/blog/domain/vo/FavoriteVO.java
git add blog-server/src/main/java/com/blog/service/FavoriteService.java
git add blog-server/src/main/java/com/blog/service/impl/FavoriteServiceImpl.java
git add blog-server/src/main/java/com/blog/controller/portal/FavoriteController.java
git add blog-web/src/api/favorite.js
git add blog-web/src/views/portal/UserProfile.vue
git add blog-web/src/views/portal/UserFavorites.vue
git add blog-web/src/views/portal/UserCenter.vue
git add blog-web/src/router/index.js
```

- [ ] **Step 2: 提交代码**

```bash
git commit -m "feat: add favorites list feature

- Add FavoriteVO, FavoriteService and FavoriteController
- Refactor UserCenter to tab-based layout
- Add UserFavorites and UserProfile components
- Update router configuration for user center sub-routes"
```

---

## 第二阶段：阅读历史记录

### Task 11: 创建数据库迁移脚本

**Files:**
- Create: `blog-server/src/main/resources/db/migration/V001__create_reading_history.sql`

- [ ] **Step 1: 创建迁移脚本**

```sql
-- 创建阅读历史表
CREATE TABLE IF NOT EXISTS `reading_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `article_id` BIGINT NOT NULL COMMENT '文章ID',
    `read_duration` INT DEFAULT 0 COMMENT '阅读时长(秒)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '首次阅读时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后阅读时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_article` (`user_id`, `article_id`),
    KEY `idx_user_time` (`user_id`, `update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='阅读历史表';
```

- [ ] **Step 2: 执行迁移**

```bash
cd D:/project/test1
mysql -u root -p blog_db < blog-server/src/main/resources/db/migration/V001__create_reading_history.sql
```

- [ ] **Step 3: 验证表创建**

```bash
mysql -u root -p -e "USE blog_db; SHOW TABLES LIKE 'reading_history';"
```

---

### Task 12: 创建阅读历史实体类

**Files:**
- Create: `blog-server/src/main/java/com/blog/domain/entity/ReadingHistory.java`

- [ ] **Step 1: 创建 ReadingHistory 实体**

```java
package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("reading_history")
public class ReadingHistory implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long articleId;

    private Integer readDuration;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

- [ ] **Step 2: 验证文件创建**

确认文件已创建。

---

### Task 13: 创建阅读历史 VO

**Files:**
- Create: `blog-server/src/main/java/com/blog/domain/vo/ReadingHistoryVO.java`

- [ ] **Step 1: 创建 ReadingHistoryVO**

```java
package com.blog.domain.vo;

import lombok.Data;

@Data
public class ReadingHistoryVO {

    private Long id;

    private Long articleId;

    private String title;

    private String coverImage;

    private String lastReadTime;
}
```

- [ ] **Step 2: 验证文件创建**

确认文件已创建。

---

### Task 14: 创建阅读历史 Mapper

**Files:**
- Create: `blog-server/src/main/java/com/blog/repository/mapper/ReadingHistoryMapper.java`

- [ ] **Step 1: 创建 ReadingHistoryMapper**

```java
package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.ReadingHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ReadingHistoryMapper extends BaseMapper<ReadingHistory> {

    @Update("INSERT INTO reading_history (user_id, article_id) VALUES (#{userId}, #{articleId}) " +
            "ON DUPLICATE KEY UPDATE update_time = NOW()")
    int upsert(@Param("userId") Long userId, @Param("articleId") Long articleId);
}
```

- [ ] **Step 2: 验证文件创建**

确认文件已创建。

---

### Task 15: 创建阅读历史服务接口

**Files:**
- Create: `blog-server/src/main/java/com/blog/service/ReadingHistoryService.java`

- [ ] **Step 1: 创建 ReadingHistoryService 接口**

```java
package com.blog.service;

import com.blog.common.result.PageResult;
import com.blog.domain.vo.ReadingHistoryVO;

public interface ReadingHistoryService {

    /**
     * 记录阅读历史
     * @param articleId 文章ID
     */
    void recordHistory(Long articleId);

    /**
     * 获取阅读历史列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 阅读历史列表
     */
    PageResult<ReadingHistoryVO> getHistoryList(int pageNum, int pageSize);

    /**
     * 删除单条阅读历史
     * @param articleId 文章ID
     */
    void deleteHistory(Long articleId);

    /**
     * 清空所有阅读历史
     */
    void clearHistory();
}
```

- [ ] **Step 2: 验证文件创建**

确认文件已创建。

---

### Task 16: 创建阅读历史服务实现类

**Files:**
- Create: `blog-server/src/main/java/com/blog/service/impl/ReadingHistoryServiceImpl.java`

- [ ] **Step 1: 创建 ReadingHistoryServiceImpl**

```java
package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.result.PageResult;
import com.blog.domain.entity.Article;
import com.blog.domain.entity.ReadingHistory;
import com.blog.domain.vo.ReadingHistoryVO;
import com.blog.repository.mapper.ArticleMapper;
import com.blog.repository.mapper.ReadingHistoryMapper;
import com.blog.security.LoginUser;
import com.blog.service.ReadingHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReadingHistoryServiceImpl implements ReadingHistoryService {

    private final ReadingHistoryMapper readingHistoryMapper;
    private final ArticleMapper articleMapper;

    @Override
    public void recordHistory(Long articleId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return;
        }
        readingHistoryMapper.upsert(userId, articleId);
    }

    @Override
    public PageResult<ReadingHistoryVO> getHistoryList(int pageNum, int pageSize) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return PageResult.of(List.of(), 0L, (long) pageSize, (long) pageNum);
        }

        // 查询用户的阅读历史
        LambdaQueryWrapper<ReadingHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReadingHistory::getUserId, userId)
               .orderByDesc(ReadingHistory::getUpdateTime);

        List<ReadingHistory> allHistory = readingHistoryMapper.selectList(wrapper);

        // 过滤已删除的文章并构建VO
        List<ReadingHistoryVO> allVOs = allHistory.stream()
                .map(history -> {
                    Article article = articleMapper.selectById(history.getArticleId());
                    if (article == null || article.getDeleted() == 1 || article.getStatus() != 1) {
                        return null;
                    }
                    return buildHistoryVO(history, article);
                })
                .filter(vo -> vo != null)
                .collect(Collectors.toList());

        // 分页
        long total = allVOs.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, allVOs.size());
        List<ReadingHistoryVO> records = start < allVOs.size() ? allVOs.subList(start, end) : List.of();

        return PageResult.of(records, total, (long) pageSize, (long) pageNum);
    }

    @Override
    @Transactional
    public void deleteHistory(Long articleId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return;
        }
        readingHistoryMapper.delete(new LambdaQueryWrapper<ReadingHistory>()
                .eq(ReadingHistory::getUserId, userId)
                .eq(ReadingHistory::getArticleId, articleId));
    }

    @Override
    @Transactional
    public void clearHistory() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return;
        }
        readingHistoryMapper.delete(new LambdaQueryWrapper<ReadingHistory>()
                .eq(ReadingHistory::getUserId, userId));
    }

    private ReadingHistoryVO buildHistoryVO(ReadingHistory history, Article article) {
        ReadingHistoryVO vo = new ReadingHistoryVO();
        vo.setId(history.getId());
        vo.setArticleId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setCoverImage(article.getCoverImage());
        vo.setLastReadTime(history.getUpdateTime().toString());
        return vo;
    }

    private Long getCurrentUserId() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser.getUserId();
        }
        return null;
    }
}
```

- [ ] **Step 2: 验证文件创建**

确认文件已创建。

---

### Task 17: 创建阅读历史控制器

**Files:**
- Create: `blog-server/src/main/java/com/blog/controller/portal/ReadingHistoryController.java`

- [ ] **Step 1: 创建 ReadingHistoryController**

```java
package com.blog.controller.portal;

import com.blog.common.result.PageResult;
import com.blog.common.result.Result;
import com.blog.domain.vo.ReadingHistoryVO;
import com.blog.service.ReadingHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "前台阅读历史接口")
@RestController
@RequestMapping("/api/portal/history")
@RequiredArgsConstructor
public class ReadingHistoryController {

    private final ReadingHistoryService readingHistoryService;

    @Operation(summary = "记录阅读历史")
    @PostMapping("/{articleId}")
    public Result<Void> recordHistory(@PathVariable Long articleId) {
        readingHistoryService.recordHistory(articleId);
        return Result.success();
    }

    @Operation(summary = "获取阅读历史列表")
    @GetMapping
    public Result<PageResult<ReadingHistoryVO>> getHistoryList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(readingHistoryService.getHistoryList(pageNum, pageSize));
    }

    @Operation(summary = "删除单条阅读历史")
    @DeleteMapping("/{articleId}")
    public Result<Void> deleteHistory(@PathVariable Long articleId) {
        readingHistoryService.deleteHistory(articleId);
        return Result.success();
    }

    @Operation(summary = "清空阅读历史")
    @DeleteMapping
    public Result<Void> clearHistory() {
        readingHistoryService.clearHistory();
        return Result.success();
    }
}
```

- [ ] **Step 2: 验证文件创建**

确认文件已创建。

---

### Task 18: 创建前端阅读历史 API

**Files:**
- Create: `blog-web/src/api/history.js`

- [ ] **Step 1: 创建阅读历史 API 模块**

```javascript
import request from '@/utils/request'

// 记录阅读历史
export function recordHistory(articleId) {
  return request.post(`/api/portal/history/${articleId}`)
}

// 获取阅读历史列表
export function getHistory(params) {
  return request.get('/api/portal/history', { params })
}

// 删除单条阅读历史
export function deleteHistory(articleId) {
  return request.delete(`/api/portal/history/${articleId}`)
}

// 清空阅读历史
export function clearHistory() {
  return request.delete('/api/portal/history')
}
```

- [ ] **Step 2: 验证文件创建**

确认文件已创建。

---

### Task 19: 创建阅读历史页面组件

**Files:**
- Create: `blog-web/src/views/portal/UserHistory.vue`

- [ ] **Step 1: 创建 UserHistory 组件**

```vue
<template>
  <div class="history-page">
    <!-- 操作栏 -->
    <div class="action-bar">
      <span class="total-count">共 {{ total }} 条记录</span>
      <button v-if="total > 0" class="clear-btn" @click="handleClear">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M14.74 9l-.346 9m-4.788 0L9.26 9m9.968-3.21c.342.052.682.107 1.022.166m-1.022-.165L18.16 19.673a2.25 2.25 0 01-2.244 2.077H8.084a2.25 2.25 0 01-2.244-2.077L4.772 5.79m14.456 0a48.108 48.108 0 00-3.478-.397m-12 .562c.34-.059.68-.114 1.022-.165m0 0a48.11 48.11 0 013.478-.397m7.5 0v-.916c0-1.18-.91-2.164-2.09-2.201a51.964 51.964 0 00-3.32 0c-1.18.037-2.09 1.022-2.09 2.201v.916m7.5 0a48.667 48.667 0 00-7.5 0" />
        </svg>
        清空历史
      </button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <van-loading type="spinner" size="24px" color="var(--color-primary)" vertical>加载中...</van-loading>
    </div>

    <!-- 空状态 -->
    <div v-else-if="historyList.length === 0" class="empty-state">
      <div class="empty-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 6v6h4.5m4.5 0a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      </div>
      <p class="empty-text">暂无阅读历史</p>
      <p class="empty-hint">浏览文章时会自动记录</p>
    </div>

    <!-- 历史列表 -->
    <div v-else class="history-list">
      <div
        v-for="item in historyList"
        :key="item.id"
        class="history-card"
        @click="goToArticle(item.articleId)"
      >
        <div v-if="item.coverImage" class="card-cover">
          <img :src="item.coverImage" :alt="item.title" loading="lazy" />
        </div>
        <div class="card-content">
          <h3 class="card-title">{{ item.title }}</h3>
          <div class="card-meta">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 6v6h4.5m4.5 0a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <span>{{ formatDate(item.lastReadTime) }}</span>
          </div>
        </div>
        <button class="remove-btn" @click.stop="handleRemove(item.articleId)">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="total > pageSize" class="pagination">
      <button
        class="page-btn"
        :disabled="pageNum === 1"
        @click="changePage(pageNum - 1)"
      >
        上一页
      </button>
      <span class="page-info">{{ pageNum }} / {{ totalPages }}</span>
      <button
        class="page-btn"
        :disabled="pageNum >= totalPages"
        @click="changePage(pageNum + 1)"
      >
        下一页
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { getHistory, deleteHistory, clearHistory } from '@/api/history'
import dayjs from 'dayjs'

const router = useRouter()

const loading = ref(false)
const historyList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const fetchHistory = async () => {
  loading.value = true
  try {
    const res = await getHistory({
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    historyList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('获取阅读历史失败', error)
  } finally {
    loading.value = false
  }
}

const changePage = (page) => {
  pageNum.value = page
  fetchHistory()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const goToArticle = (articleId) => {
  router.push(`/article/${articleId}`)
}

const handleRemove = async (articleId) => {
  try {
    await showConfirmDialog({
      title: '删除记录',
      message: '确定要删除这条阅读记录吗？'
    })
    await deleteHistory(articleId)
    showToast({ type: 'success', message: '已删除' })
    fetchHistory()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
    }
  }
}

const handleClear = async () => {
  try {
    await showConfirmDialog({
      title: '清空历史',
      message: '确定要清空所有阅读历史吗？此操作不可恢复。'
    })
    await clearHistory()
    showToast({ type: 'success', message: '已清空' })
    historyList.value = []
    total.value = 0
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清空失败', error)
    }
  }
}

const formatDate = (date) => dayjs(date).format('MM-DD HH:mm')

onMounted(() => {
  fetchHistory()
})
</script>

<style lang="scss" scoped>
.history-page {
  max-width: 900px;
  margin: 0 auto;
}

.action-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-4);
}

.total-count {
  font-size: var(--text-sm);
  color: var(--text-muted);
}

.clear-btn {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-3);
  font-size: var(--text-sm);
  color: var(--color-danger, #ef4444);
  background: transparent;
  border: 1px solid currentColor;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);

  svg {
    width: 16px;
    height: 16px;
  }

  &:hover {
    background: var(--color-danger, #ef4444);
    color: white;
  }
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 40vh;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--space-16) var(--space-6);
  color: var(--text-muted);
}

.empty-icon {
  margin-bottom: var(--space-6);

  svg {
    width: 64px;
    height: 64px;
    opacity: 0.5;
  }
}

.empty-text {
  font-size: var(--text-lg);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
  margin-bottom: var(--space-2);
}

.empty-hint {
  font-size: var(--text-sm);
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.history-card {
  display: flex;
  gap: var(--space-4);
  padding: var(--space-3);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  cursor: pointer;
  transition: all var(--transition-fast);
  position: relative;

  &:hover {
    border-color: var(--color-primary);
    box-shadow: var(--shadow-sm);
  }
}

.card-cover {
  flex-shrink: 0;
  width: 80px;
  height: 60px;
  border-radius: var(--radius-md);
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.card-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: var(--space-2);
}

.card-title {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--text-xs);
  color: var(--text-muted);

  svg {
    width: 14px;
    height: 14px;
  }
}

.remove-btn {
  position: absolute;
  top: 50%;
  right: var(--space-3);
  transform: translateY(-50%);
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: var(--radius-full);
  color: var(--text-muted);
  cursor: pointer;
  opacity: 0;
  transition: all var(--transition-fast);

  svg {
    width: 16px;
    height: 16px;
  }

  &:hover {
    background: var(--bg-hover);
    color: var(--color-danger, #ef4444);
  }
}

.history-card:hover .remove-btn {
  opacity: 1;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-4);
  margin-top: var(--space-6);
}

.page-btn {
  height: 36px;
  padding: 0 var(--space-4);
  font-size: var(--text-sm);
  color: var(--text-secondary);
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);

  &:hover:not(:disabled) {
    color: var(--color-primary);
    border-color: var(--color-primary);
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.page-info {
  font-size: var(--text-sm);
  color: var(--text-muted);
}

@media (max-width: 640px) {
  .card-cover {
    width: 60px;
    height: 45px;
  }

  .remove-btn {
    opacity: 1;
  }
}
</style>
```

- [ ] **Step 2: 验证文件创建**

确认文件已创建。

---

### Task 20: 在文章详情页添加阅读历史记录

**Files:**
- Modify: `blog-web/src/views/portal/ArticleDetail.vue`

- [ ] **Step 1: 导入并调用记录历史 API**

在 `ArticleDetail.vue` 的 `<script setup>` 部分添加：

```javascript
// 在文件顶部的导入部分添加
import { recordHistory } from '@/api/history'

// 在 fetchArticle 函数中添加记录历史调用
const fetchArticle = async () => {
  loading.value = true
  try {
    const res = await getArticle(route.params.id)
    article.value = res.data || {}
    document.title = `${article.value.title} - 随笔`

    // 记录阅读历史（仅登录用户）
    if (userStore.isLoggedIn) {
      recordHistory(article.value.id).catch(() => {})
    }

    // 文章加载后提取目录
    nextTick(() => {
      extractHeadings()
    })
  } catch (error) {
    console.error('获取文章失败', error)
    showToast('获取文章失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
```

- [ ] **Step 2: 验证修改**

确认文章详情页已添加阅读历史记录功能。

---

### Task 21: 提交第二阶段代码

- [ ] **Step 1: 添加文件到暂存区**

```bash
cd D:/project/test1
git add blog-server/src/main/resources/db/migration/V001__create_reading_history.sql
git add blog-server/src/main/java/com/blog/domain/entity/ReadingHistory.java
git add blog-server/src/main/java/com/blog/domain/vo/ReadingHistoryVO.java
git add blog-server/src/main/java/com/blog/repository/mapper/ReadingHistoryMapper.java
git add blog-server/src/main/java/com/blog/service/ReadingHistoryService.java
git add blog-server/src/main/java/com/blog/service/impl/ReadingHistoryServiceImpl.java
git add blog-server/src/main/java/com/blog/controller/portal/ReadingHistoryController.java
git add blog-web/src/api/history.js
git add blog-web/src/views/portal/UserHistory.vue
git add blog-web/src/views/portal/ArticleDetail.vue
```

- [ ] **Step 2: 提交代码**

```bash
git commit -m "feat: add reading history feature

- Create reading_history table
- Add ReadingHistory entity, mapper, service and controller
- Add UserHistory component for user center
- Record reading history when viewing article details"
```

---

## 第三阶段：文章分享功能

### Task 22: 安装二维码生成库

**Files:**
- Modify: `blog-web/package.json`

- [ ] **Step 1: 安装 qrcode-generator**

```bash
cd D:/project/test1/blog-web
pnpm add qrcode-generator
```

- [ ] **Step 2: 验证安装**

确认 `package.json` 中已添加 `qrcode-generator` 依赖。

---

### Task 23: 创建分享面板组件

**Files:**
- Create: `blog-web/src/components/SharePanel.vue`

- [ ] **Step 1: 创建 SharePanel 组件**

```vue
<template>
  <van-popup
    v-model:show="visible"
    position="bottom"
    round
    :style="{ padding: '20px' }"
  >
    <div class="share-panel">
      <h3 class="panel-title">分享文章</h3>

      <div class="share-options">
        <button class="share-option" @click="copyLink">
          <div class="option-icon copy">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M13.19 8.688a4.5 4.5 0 011.242 7.244l-4.5 4.5a4.5 4.5 0 01-6.364-6.364l1.757-1.757m13.35-.622l1.757-1.757a4.5 4.5 0 00-6.364-6.364l-4.5 4.5a4.5 4.5 0 001.242 7.244" />
            </svg>
          </div>
          <span>复制链接</span>
        </button>

        <button class="share-option" @click="showQRCode">
          <div class="option-icon qrcode">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M3.75 4.875c0-.621.504-1.125 1.125-1.125h4.5c.621 0 1.125.504 1.125 1.125v4.5c0 .621-.504 1.125-1.125 1.125h-4.5A1.125 1.125 0 013.75 9.375v-4.5zM3.75 14.625c0-.621.504-1.125 1.125-1.125h4.5c.621 0 1.125.504 1.125 1.125v4.5c0 .621-.504 1.125-1.125 1.125h-4.5a1.125 1.125 0 01-1.125-1.125v-4.5zM13.5 4.875c0-.621.504-1.125 1.125-1.125h4.5c.621 0 1.125.504 1.125 1.125v4.5c0 .621-.504 1.125-1.125 1.125h-4.5A1.125 1.125 0 0113.5 9.375v-4.5z" />
              <path stroke-linecap="round" stroke-linejoin="round" d="M6.75 6.75h.75v.75h-.75v-.75zM6.75 16.5h.75v.75h-.75v-.75zM16.5 6.75h.75v.75h-.75v-.75zM13.5 13.5h.75v.75h-.75v-.75zM13.5 19.5h.75v.75h-.75v-.75zM19.5 13.5h.75v.75h-.75v-.75zM19.5 19.5h.75v.75h-.75v-.75zM16.5 16.5h.75v.75h-.75v-.75z" />
            </svg>
          </div>
          <span>二维码</span>
        </button>

        <button class="share-option" @click="shareToWeibo">
          <div class="option-icon weibo">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M10.098 20c-4.612 0-8.348-2.725-8.348-6.084 0-1.755.88-3.686 2.398-5.427 2.023-2.322 4.976-3.697 7.113-3.697.88 0 1.624.244 2.159.707l.066.058.048.043c.294.26.474.427.71.688.39.429.693.895.927 1.45.657-.196 1.407-.297 2.237-.297 2.397 0 4.091.97 4.091 2.334 0 .465-.177.897-.49 1.252l-.035.04-.037.04c-.1.109-.21.208-.33.307.14.092.27.19.39.295.52.444.82 1.023.82 1.665 0 1.78-2.01 3.098-4.908 3.098-.95 0-1.82-.149-2.594-.43C13.172 18.69 11.742 20 10.098 20zm1.172-12.68c-1.552 0-3.922 1.143-5.565 3.03-1.234 1.416-1.935 2.96-1.935 4.252 0 2.206 2.842 4.084 6.328 4.084 1.206 0 2.287-.474 3.124-1.376l.027-.03.028-.028c.25-.247.465-.527.655-.852.51.12 1.054.18 1.628.18 2.242 0 3.908-.88 3.908-2.07 0-.328-.166-.64-.47-.896-.306-.26-.73-.467-1.246-.617l-.785-.228.638-.512c.28-.224.478-.436.61-.655.106-.177.158-.346.158-.512 0-.71-1.148-1.334-2.591-1.334-.7 0-1.338.093-1.886.27l-.534.173-.164-.535c-.18-.587-.448-1.085-.82-1.493-.184-.198-.34-.34-.603-.574l-.066-.058c-.277-.24-.678-.36-1.141-.36zm6.594-.865c-.548-.137-.95-.348-1.186-.626-.237-.28-.323-.63-.256-1.038.14-.855.98-1.593 2.06-2.006 1.08-.412 2.26-.412 3.076.017.39.205.67.49.82.835.15.345.18.75.09 1.188-.18.87-.93 1.573-1.94 1.875-.5.15-1.03.2-1.58.15-.39-.03-.77-.13-1.08-.395h-.004zm1.675-2.61c-.37-.092-.81-.058-1.22.09-.52.19-.9.52-.97.85-.03.14-.01.26.05.36.07.11.19.19.35.24.37.1.82.02 1.26-.21.43-.23.73-.55.8-.87.02-.11.01-.22-.02-.32-.03-.1-.11-.18-.25-.14z"/>
            </svg>
          </div>
          <span>微博</span>
        </button>
      </div>

      <button class="cancel-btn" @click="visible = false">取消</button>
    </div>
  </van-popup>

  <!-- 二维码弹窗 -->
  <van-popup
    v-model:show="showQRPopup"
    round
    :style="{ padding: '24px' }"
  >
    <div class="qrcode-popup">
      <h4 class="qrcode-title">{{ title }}</h4>
      <div ref="qrcodeRef" class="qrcode-container"></div>
      <p class="qrcode-hint">微信扫码阅读文章</p>
    </div>
  </van-popup>
</template>

<script setup>
import { ref, watch } from 'vue'
import { showToast } from 'vant'
import qrcode from 'qrcode-generator'

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  url: {
    type: String,
    required: true
  },
  title: {
    type: String,
    default: '分享文章'
  }
})

const emit = defineEmits(['update:show'])

const visible = ref(false)
const showQRPopup = ref(false)
const qrcodeRef = ref(null)

watch(() => props.show, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:show', val)
})

const copyLink = async () => {
  try {
    await navigator.clipboard.writeText(props.url)
    showToast({ type: 'success', message: '链接已复制' })
    visible.value = false
  } catch (error) {
    showToast('复制失败，请手动复制')
  }
}

const showQRCode = () => {
  visible.value = false
  showQRPopup.value = true

  setTimeout(() => {
    generateQRCode()
  }, 100)
}

const generateQRCode = () => {
  if (!qrcodeRef.value) return

  qrcodeRef.value.innerHTML = ''

  const qr = qrcode(0, 'M')
  qr.addData(props.url)
  qr.make()

  const size = 180
  const cellSize = size / qr.getModuleCount()

  const canvas = document.createElement('canvas')
  canvas.width = size
  canvas.height = size
  const ctx = canvas.getContext('2d')

  for (let row = 0; row < qr.getModuleCount(); row++) {
    for (let col = 0; col < qr.getModuleCount(); col++) {
      ctx.fillStyle = qr.isDark(row, col) ? '#1a1a1a' : '#ffffff'
      ctx.fillRect(col * cellSize, row * cellSize, cellSize, cellSize)
    }
  }

  qrcodeRef.value.appendChild(canvas)
}

const shareToWeibo = () => {
  const shareUrl = `https://service.weibo.com/share/share.php?url=${encodeURIComponent(props.url)}&title=${encodeURIComponent(props.title)}`
  window.open(shareUrl, '_blank')
  visible.value = false
}
</script>

<style lang="scss" scoped>
.share-panel {
  text-align: center;
}

.panel-title {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  margin-bottom: var(--space-6);
}

.share-options {
  display: flex;
  justify-content: space-around;
  margin-bottom: var(--space-6);
}

.share-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-4);
  background: transparent;
  border: none;
  cursor: pointer;
  transition: transform var(--transition-fast);

  &:hover {
    transform: scale(1.05);
  }

  span {
    font-size: var(--text-sm);
    color: var(--text-secondary);
  }
}

.option-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-xl);
  transition: all var(--transition-fast);

  svg {
    width: 28px;
    height: 28px;
  }

  &.copy {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
  }

  &.qrcode {
    background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
    color: white;
  }

  &.weibo {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    color: white;
  }
}

.cancel-btn {
  width: 100%;
  height: 48px;
  font-size: var(--text-base);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
  background: var(--bg-secondary);
  border: none;
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);

  &:hover {
    background: var(--bg-hover);
  }
}

.qrcode-popup {
  text-align: center;
}

.qrcode-title {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
  margin-bottom: var(--space-4);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200px;
}

.qrcode-container {
  display: flex;
  justify-content: center;
  margin-bottom: var(--space-3);

  canvas {
    border-radius: var(--radius-md);
  }
}

.qrcode-hint {
  font-size: var(--text-xs);
  color: var(--text-muted);
}
</style>
```

- [ ] **Step 2: 验证文件创建**

确认文件已创建。

---

### Task 24: 在文章详情页添加分享按钮

**Files:**
- Modify: `blog-web/src/views/portal/ArticleDetail.vue`

- [ ] **Step 1: 添加分享按钮到操作栏**

在模板的 `footer-actions` 部分添加分享按钮：

```vue
<!-- 在 footer-actions div 中，点赞和收藏按钮后添加 -->
<button class="action-btn" @click="showSharePanel = true">
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
    <path stroke-linecap="round" stroke-linejoin="round" d="M7.217 10.907a2.25 2.25 0 100 2.186m0-2.186c.18.324.283.696.283 1.093s-.103.77-.283 1.093m0-2.186l9.566-5.314m-9.566 7.5l9.566 5.314m0 0a2.25 2.25 0 103.935 2.186 2.25 2.25 0 00-3.935-2.186zm0-12.814a2.25 2.25 0 103.933-2.185 2.25 2.25 0 00-3.933 2.185z" />
  </svg>
  <span>分享</span>
</button>
```

- [ ] **Step 2: 导入并使用 SharePanel 组件**

在 `<script setup>` 部分添加：

```javascript
// 导入
import SharePanel from '@/components/SharePanel.vue'

// 添加响应式变量
const showSharePanel = ref(false)

// 计算当前文章URL
const articleUrl = computed(() => {
  return `${window.location.origin}/#/article/${article.value.id}`
})
```

- [ ] **Step 3: 添加 SharePanel 组件到模板**

在模板末尾 `</template>` 前添加：

```vue
<!-- 分享面板 -->
<SharePanel
  v-model:show="showSharePanel"
  :url="articleUrl"
  :title="article.title"
/>
```

- [ ] **Step 4: 验证修改**

确认文章详情页已添加分享功能。

---

### Task 25: 提交第三阶段代码

- [ ] **Step 1: 添加文件到暂存区**

```bash
cd D:/project/test1
git add blog-web/package.json
git add blog-web/pnpm-lock.yaml
git add blog-web/src/components/SharePanel.vue
git add blog-web/src/views/portal/ArticleDetail.vue
```

- [ ] **Step 2: 提交代码**

```bash
git commit -m "feat: add article share feature

- Add qrcode-generator dependency
- Create SharePanel component with copy link, QR code and Weibo share
- Add share button to article detail page"
```

---

## 自检清单

### 规格覆盖检查

| 规格要求 | 对应任务 |
|---------|---------|
| 收藏列表 API | Task 1-4 |
| 收藏列表前端页面 | Task 5-9 |
| 阅读历史数据库表 | Task 11 |
| 阅读历史 API | Task 12-17 |
| 阅读历史前端页面 | Task 18-19 |
| 文章详情页记录阅读历史 | Task 20 |
| 分享按钮和面板 | Task 22-24 |

### 占位符扫描

✅ 无 "TBD"、"TODO"、"implement later" 等占位符

### 类型一致性检查

✅ `FavoriteVO`、`ReadingHistoryVO` 字段与 API 响应一致
✅ 前端 API 调用与后端接口参数匹配
