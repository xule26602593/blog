# 签到系统开发文档

## 1. 概述

### 1.1 功能定位

签到系统是博客平台的用户激励核心模块，通过每日签到机制提升用户活跃度和留存率。

### 1.2 核心特性

- 每日签到获取积分奖励
- 连续签到额外奖励加成
- 签到日历可视化展示
- 与成就系统联动

### 1.3 技术栈

| 层级 | 技术选型 |
|------|----------|
| 后端 | Spring Boot 3.2 + MyBatis Plus |
| 前端 | Vue 3.5 + Vant 4 |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis |

---

## 2. 数据库设计

### 2.1 表结构

#### 2.1.1 签到记录表 `user_checkin`

```sql
CREATE TABLE `user_checkin` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `checkin_date` DATE NOT NULL COMMENT '签到日期',
    `consecutive_days` INT DEFAULT 1 COMMENT '连续签到天数',
    `points_earned` INT DEFAULT 0 COMMENT '获得积分',
    `checkin_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '签到时间',
    `ip_address` VARCHAR(50) DEFAULT NULL COMMENT '签到IP',
    `device_info` VARCHAR(255) DEFAULT NULL COMMENT '设备信息',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `checkin_date`),
    KEY `idx_user_consecutive` (`user_id`, `consecutive_days`),
    KEY `idx_checkin_date` (`checkin_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户签到记录表';
```

**字段说明：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | BIGINT | 是 | 自增主键 |
| user_id | BIGINT | 是 | 关联 sys_user.id |
| checkin_date | DATE | 是 | 签到日期（YYYY-MM-DD） |
| consecutive_days | INT | 否 | 当次签到时的连续天数 |
| points_earned | INT | 否 | 本次签到获得的积分 |
| checkin_time | DATETIME | 否 | 精确签到时间 |
| ip_address | VARCHAR(50) | 否 | 签到IP地址 |
| device_info | VARCHAR(255) | 否 | 设备信息（预留） |

#### 2.1.2 签到奖励配置表 `checkin_config`

```sql
CREATE TABLE `checkin_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `consecutive_days` INT NOT NULL COMMENT '连续签到天数阈值',
    `reward_points` INT NOT NULL COMMENT '额外奖励积分',
    `reward_type` VARCHAR(50) DEFAULT 'points' COMMENT '奖励类型',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '奖励描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0:禁用 1:启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_days` (`consecutive_days`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到奖励配置表';
```

**初始数据：**

```sql
INSERT INTO `checkin_config` (`consecutive_days`, `reward_points`, `description`) VALUES
(1, 0, '每日签到基础奖励'),
(7, 20, '连续签到7天额外奖励'),
(14, 50, '连续签到14天额外奖励'),
(30, 100, '连续签到30天额外奖励'),
(60, 200, '连续签到60天额外奖励'),
(100, 500, '连续签到100天额外奖励'),
(180, 1000, '连续签到180天额外奖励'),
(365, 3000, '连续签到365天额外奖励');
```

#### 2.1.3 用户表扩展

```sql
-- 为用户表添加签到相关字段
ALTER TABLE `sys_user` 
ADD COLUMN `points` INT DEFAULT 0 COMMENT '当前积分',
ADD COLUMN `total_points` INT DEFAULT 0 COMMENT '累计积分',
ADD COLUMN `level` INT DEFAULT 1 COMMENT '用户等级',
ADD COLUMN `checkin_days` INT DEFAULT 0 COMMENT '累计签到天数',
ADD COLUMN `max_consecutive_days` INT DEFAULT 0 COMMENT '最大连续签到天数',
ADD COLUMN `last_checkin_date` DATE DEFAULT NULL COMMENT '最后签到日期';
```

---

## 3. 后端实现

### 3.1 目录结构

```
blog-server/src/main/java/com/blog/
├── domain/
│   ├── entity/
│   │   ├── UserCheckin.java
│   │   └── CheckinConfig.java
│   ├── dto/
│   │   └── CheckinResultDTO.java
│   └── vo/
│       ├── CheckinStatusVO.java
│       └── CheckinCalendarVO.java
├── repository/
│   └── mapper/
│       ├── UserCheckinMapper.java
│       └── CheckinConfigMapper.java
├── service/
│   ├── CheckinService.java
│   └── impl/
│       └── CheckinServiceImpl.java
└── controller/
    └── portal/
        └── CheckinController.java
```

### 3.2 实体类

#### UserCheckin.java

```java
package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user_checkin")
public class UserCheckin implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private LocalDate checkinDate;

    private Integer consecutiveDays;

    private Integer pointsEarned;

    private LocalDateTime checkinTime;

    private String ipAddress;

    private String deviceInfo;
}
```

### 3.3 服务层

#### CheckinService.java

```java
package com.blog.service;

import com.blog.domain.vo.CheckinCalendarVO;
import com.blog.domain.vo.CheckinStatusVO;
import com.blog.domain.dto.CheckinResultDTO;

import java.time.LocalDate;
import java.util.List;

public interface CheckinService {

    /**
     * 用户签到
     * @param userId 用户ID
     * @return 签到结果
     */
    CheckinResultDTO checkin(Long userId);

    /**
     * 获取签到状态
     * @param userId 用户ID
     * @return 签到状态信息
     */
    CheckinStatusVO getCheckinStatus(Long userId);

    /**
     * 获取签到日历数据
     * @param userId 用户ID
     * @param month 月份 (格式: YYYY-MM)
     * @return 日历数据
     */
    List<CheckinCalendarVO> getCheckinCalendar(Long userId, String month);

    /**
     * 获取签到记录列表
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 签到记录分页
     */
    IPage<UserCheckin> getCheckinHistory(Long userId, int page, int size);
}
```

#### CheckinServiceImpl.java

```java
package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.exception.BusinessException;
import com.blog.domain.dto.CheckinResultDTO;
import com.blog.domain.entity.CheckinConfig;
import com.blog.domain.entity.UserCheckin;
import com.blog.domain.vo.CheckinCalendarVO;
import com.blog.domain.vo.CheckinStatusVO;
import com.blog.repository.mapper.CheckinConfigMapper;
import com.blog.repository.mapper.UserCheckinMapper;
import com.blog.repository.mapper.UserMapper;
import com.blog.service.CheckinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckinServiceImpl implements CheckinService {

    private final UserCheckinMapper checkinMapper;
    private final CheckinConfigMapper configMapper;
    private final UserMapper userMapper;

    // 基础签到积分
    private static final int BASE_POINTS = 5;
    // 早起奖励时间段 (6:00-8:00)
    private static final int EARLY_BIRD_START = 6;
    private static final int EARLY_BIRD_END = 8;
    // 早起额外奖励积分
    private static final int EARLY_BIRD_BONUS = 5;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CheckinResultDTO checkin(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // 1. 检查今日是否已签到
        if (checkinMapper.existsByUserAndDate(userId, today)) {
            throw new BusinessException("今日已签到，明天再来吧~");
        }

        // 2. 计算连续签到天数
        LocalDate yesterday = today.minusDays(1);
        UserCheckin yesterdayCheckin = checkinMapper.findByUserAndDate(userId, yesterday);
        
        int consecutiveDays;
        if (yesterdayCheckin != null) {
            consecutiveDays = yesterdayCheckin.getConsecutiveDays() + 1;
        } else {
            consecutiveDays = 1;
        }

        // 3. 计算奖励积分
        int basePoints = BASE_POINTS;
        int bonusPoints = calculateBonusPoints(consecutiveDays);
        int specialBonus = calculateSpecialBonus(now);
        int totalPoints = basePoints + bonusPoints + specialBonus;

        // 4. 保存签到记录
        UserCheckin checkin = new UserCheckin();
        checkin.setUserId(userId);
        checkin.setCheckinDate(today);
        checkin.setConsecutiveDays(consecutiveDays);
        checkin.setPointsEarned(totalPoints);
        checkin.setCheckinTime(now);
        checkinMapper.insert(checkin);

        // 5. 更新用户积分和签到信息
        userMapper.updateCheckinInfo(userId, totalPoints, consecutiveDays);

        // 6. 构建返回结果
        return CheckinResultDTO.builder()
                .success(true)
                .consecutiveDays(consecutiveDays)
                .pointsEarned(totalPoints)
                .basePoints(basePoints)
                .bonusPoints(bonusPoints)
                .specialBonus(specialBonus)
                .message(generateCheckinMessage(consecutiveDays, specialBonus > 0))
                .build();
    }

    @Override
    public CheckinStatusVO getCheckinStatus(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // 查询今日是否已签到
        boolean isCheckedToday = checkinMapper.existsByUserAndDate(userId, today);
        
        // 获取用户签到统计
        User user = userMapper.selectById(userId);
        
        // 计算当前连续天数
        int currentConsecutiveDays = 0;
        if (isCheckedToday) {
            UserCheckin todayCheckin = checkinMapper.findByUserAndDate(userId, today);
            currentConsecutiveDays = todayCheckin.getConsecutiveDays();
        } else if (checkinMapper.existsByUserAndDate(userId, yesterday)) {
            UserCheckin yesterdayCheckin = checkinMapper.findByUserAndDate(userId, yesterday);
            currentConsecutiveDays = yesterdayCheckin.getConsecutiveDays();
        }

        return CheckinStatusVO.builder()
                .isCheckedToday(isCheckedToday)
                .consecutiveDays(currentConsecutiveDays)
                .totalDays(user.getCheckinDays())
                .totalPoints(user.getTotalPoints())
                .maxConsecutiveDays(user.getMaxConsecutiveDays())
                .todayPoints(isCheckedToday ? 
                    checkinMapper.findByUserAndDate(userId, today).getPointsEarned() : 0)
                .build();
    }

    @Override
    public List<CheckinCalendarVO> getCheckinCalendar(Long userId, String month) {
        LocalDate startDate = LocalDate.parse(month + "-01");
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        
        // 获取该月签到记录
        List<UserCheckin> checkins = checkinMapper.selectByUserAndDateRange(
            userId, startDate, endDate
        );
        
        // 转换为日历格式
        List<CheckinCalendarVO> calendar = new ArrayList<>();
        for (UserCheckin checkin : checkins) {
            calendar.add(CheckinCalendarVO.builder()
                .date(checkin.getCheckinDate())
                .checked(true)
                .points(checkin.getPointsEarned())
                .build());
        }
        
        return calendar;
    }

    /**
     * 计算连续签到额外奖励
     */
    private int calculateBonusPoints(int consecutiveDays) {
        // 查找匹配的奖励配置
        LambdaQueryWrapper<CheckinConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckinConfig::getStatus, 1)
               .le(CheckinConfig::getConsecutiveDays, consecutiveDays)
               .orderByDesc(CheckinConfig::getConsecutiveDays)
               .last("LIMIT 1");
        
        CheckinConfig config = configMapper.selectOne(wrapper);
        return config != null ? config.getRewardPoints() : 0;
    }

    /**
     * 计算特殊时段奖励（早起奖励）
     */
    private int calculateSpecialBonus(LocalDateTime time) {
        int hour = time.getHour();
        if (hour >= EARLY_BIRD_START && hour < EARLY_BIRD_END) {
            return EARLY_BIRD_BONUS;
        }
        return 0;
    }

    /**
     * 生成签到成功消息
     */
    private String generateCheckinMessage(int days, boolean isEarlyBird) {
        StringBuilder msg = new StringBuilder();
        
        if (isEarlyBird) {
            msg.append("早起鸟儿有虫吃！早起签到额外+").append(EARLY_BIRD_BONUS).append("积分\n");
        }
        
        if (days == 1) {
            msg.append("开始你的签到之旅！");
        } else if (days < 7) {
            msg.append("已坚持").append(days).append("天，继续加油！");
        } else if (days < 30) {
            msg.append("连续").append(days).append("天，真棒！");
        } else if (days < 100) {
            msg.append("太厉害了，").append(days).append("天！");
        } else if (days < 365) {
            msg.append("传奇！").append(days).append("天不间断！");
        } else {
            msg.append("封神！").append(days).append("天！你是签到之王！");
        }
        
        return msg.toString();
    }
}
```

### 3.4 控制器

#### CheckinController.java

```java
package com.blog.controller.portal;

import com.blog.common.result.Result;
import com.blog.domain.dto.CheckinResultDTO;
import com.blog.domain.vo.CheckinCalendarVO;
import com.blog.domain.vo.CheckinStatusVO;
import com.blog.security.UserDetailsImpl;
import com.blog.service.CheckinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "签到接口", description = "用户签到相关接口")
@RestController
@RequestMapping("/api/portal/checkin")
@RequiredArgsConstructor
public class CheckinController {

    private final CheckinService checkinService;

    @Operation(summary = "签到")
    @PostMapping
    public Result<CheckinResultDTO> checkin(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        CheckinResultDTO result = checkinService.checkin(userDetails.getUser().getId());
        return Result.success(result);
    }

    @Operation(summary = "获取签到状态")
    @GetMapping("/status")
    public Result<CheckinStatusVO> getStatus(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        CheckinStatusVO status = checkinService.getCheckinStatus(userDetails.getUser().getId());
        return Result.success(status);
    }

    @Operation(summary = "获取签到日历")
    @GetMapping("/calendar")
    public Result<List<CheckinCalendarVO>> getCalendar(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false, defaultValue = "") String month) {
        // 默认当前月份
        if (month.isEmpty()) {
            month = java.time.LocalDate.now().toString().substring(0, 7);
        }
        List<CheckinCalendarVO> calendar = checkinService.getCheckinCalendar(
            userDetails.getUser().getId(), month
        );
        return Result.success(calendar);
    }
}
```

### 3.5 Mapper 接口

#### UserCheckinMapper.java

```java
package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.UserCheckin;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface UserCheckinMapper extends BaseMapper<UserCheckin> {

    @Select("SELECT COUNT(*) > 0 FROM user_checkin WHERE user_id = #{userId} AND checkin_date = #{date}")
    boolean existsByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Select("SELECT * FROM user_checkin WHERE user_id = #{userId} AND checkin_date = #{date}")
    UserCheckin findByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Select("SELECT * FROM user_checkin WHERE user_id = #{userId} AND checkin_date BETWEEN #{start} AND #{end} ORDER BY checkin_date")
    List<UserCheckin> selectByUserAndDateRange(
        @Param("userId") Long userId, 
        @Param("start") LocalDate start, 
        @Param("end") LocalDate end
    );
}
```

#### UserMapper 扩展

```java
// 在 UserMapper.java 中添加以下方法

@Update("UPDATE sys_user SET " +
        "points = points + #{points}, " +
        "total_points = total_points + #{points}, " +
        "checkin_days = checkin_days + 1, " +
        "max_consecutive_days = GREATEST(max_consecutive_days, #{consecutive}), " +
        "last_checkin_date = CURRENT_DATE " +
        "WHERE id = #{userId}")
void updateCheckinInfo(@Param("userId") Long userId, 
                       @Param("points") int points, 
                       @Param("consecutive") int consecutive);
```

---

## 4. 前端实现

### 4.1 目录结构

```
blog-web/src/
├── api/
│   └── checkin.js
├── components/
│   └── checkin/
│       ├── CheckinPanel.vue
│       ├── CheckinCalendar.vue
│       └── CheckinResultDialog.vue
├── stores/
│   └── checkin.js
└── views/
    └── portal/
        └── UserCenter.vue (修改)
```

### 4.2 API 接口

#### api/checkin.js

```javascript
import request from '@/utils/request'

/**
 * 签到
 */
export function checkin() {
  return request.post('/api/portal/checkin')
}

/**
 * 获取签到状态
 */
export function getCheckinStatus() {
  return request.get('/api/portal/checkin/status')
}

/**
 * 获取签到日历
 */
export function getCheckinCalendar(month) {
  return request.get('/api/portal/checkin/calendar', { params: { month } })
}
```

### 4.3 状态管理

#### stores/checkin.js

```javascript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCheckinStatus, checkin as checkinApi } from '@/api/checkin'

export const useCheckinStore = defineStore('checkin', () => {
  // 状态
  const status = ref({
    isCheckedToday: false,
    consecutiveDays: 0,
    totalDays: 0,
    totalPoints: 0,
    maxConsecutiveDays: 0,
    todayPoints: 0
  })
  
  const loading = ref(false)
  const lastCheckinResult = ref(null)

  // 计算属性
  const canCheckin = computed(() => !status.value.isCheckedToday)

  // 方法
  async function fetchStatus() {
    try {
      const { data } = await getCheckinStatus()
      status.value = data
    } catch (error) {
      console.error('获取签到状态失败:', error)
    }
  }

  async function doCheckin() {
    if (loading.value) return null
    
    loading.value = true
    try {
      const { data } = await checkinApi()
      lastCheckinResult.value = data
      await fetchStatus()
      return data
    } catch (error) {
      throw error
    } finally {
      loading.value = false
    }
  }

  return {
    status,
    loading,
    lastCheckinResult,
    canCheckin,
    fetchStatus,
    doCheckin
  }
})
```

### 4.4 组件实现

#### components/checkin/CheckinPanel.vue

```vue
<template>
  <AppCard class="checkin-panel">
    <!-- 头部 -->
    <div class="checkin-header">
      <div class="header-left">
        <h3>每日签到</h3>
        <span class="streak-badge" v-if="status.consecutiveDays > 0">
          🔥 连续 {{ status.consecutiveDays }} 天
        </span>
      </div>
      <van-button 
        :type="canCheckin ? 'primary' : 'default'"
        size="small"
        :loading="loading"
        :disabled="!canCheckin"
        @click="handleCheckin"
      >
        {{ canCheckin ? '签到' : '已签到' }}
      </van-button>
    </div>

    <!-- 统计数据 -->
    <div class="checkin-stats">
      <div class="stat-item">
        <span class="stat-value">{{ status.totalDays }}</span>
        <span class="stat-label">累计签到</span>
      </div>
      <div class="stat-item">
        <span class="stat-value">{{ status.totalPoints }}</span>
        <span class="stat-label">获得积分</span>
      </div>
      <div class="stat-item">
        <span class="stat-value">{{ status.maxConsecutiveDays }}</span>
        <span class="stat-label">最长连续</span>
      </div>
    </div>

    <!-- 签到结果弹窗 -->
    <CheckinResultDialog 
      v-model:show="showResult" 
      :result="checkinResult"
    />
  </AppCard>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { showToast } from 'vant'
import { useCheckinStore } from '@/stores/checkin'
import CheckinResultDialog from './CheckinResultDialog.vue'

const checkinStore = useCheckinStore()
const { status, loading, canCheckin, fetchStatus, doCheckin } = checkinStore

const showResult = ref(false)
const checkinResult = ref(null)

onMounted(() => {
  fetchStatus()
})

const handleCheckin = async () => {
  try {
    const result = await doCheckin()
    checkinResult.value = result
    showResult.value = true
  } catch (error) {
    showToast(error.message || '签到失败')
  }
}
</script>

<style lang="scss" scoped>
.checkin-panel {
  margin-bottom: var(--space-4);
}

.checkin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--space-3);

  h3 {
    margin: 0;
    font-size: var(--text-lg);
  }
}

.streak-badge {
  padding: var(--space-1) var(--space-2);
  background: linear-gradient(135deg, #FF6B6B, #FF8E53);
  color: white;
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
}

.checkin-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-4);
  text-align: center;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.stat-value {
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
}

.stat-label {
  font-size: var(--text-xs);
  color: var(--text-secondary);
}
</style>
```

#### components/checkin/CheckinResultDialog.vue

```vue
<template>
  <van-dialog
    :show="show"
    @update:show="$emit('update:show', $event)"
    title="签到成功"
    :show-confirm-button="false"
    class="checkin-result-dialog"
  >
    <div class="result-content">
      <!-- 动画效果 -->
      <div class="result-animation">
        <span class="celebration">🎉</span>
      </div>
      
      <!-- 积分展示 -->
      <div class="result-points">
        <span class="points-label">获得积分</span>
        <span class="points-value">+{{ result?.pointsEarned || 0 }}</span>
      </div>
      
      <!-- 积分明细 -->
      <div class="result-detail">
        <div class="detail-item" v-if="result?.basePoints">
          <span>基础奖励</span>
          <span>+{{ result.basePoints }}</span>
        </div>
        <div class="detail-item" v-if="result?.bonusPoints">
          <span>连续签到奖励</span>
          <span>+{{ result.bonusPoints }}</span>
        </div>
        <div class="detail-item special" v-if="result?.specialBonus">
          <span>早起奖励</span>
          <span>+{{ result.specialBonus }}</span>
        </div>
      </div>
      
      <!-- 连续天数 -->
      <div class="result-streak">
        已连续签到 <strong>{{ result?.consecutiveDays || 1 }}</strong> 天
      </div>
      
      <!-- 消息 -->
      <p class="result-message">{{ result?.message }}</p>
      
      <!-- 关闭按钮 -->
      <van-button type="primary" block @click="handleClose">
        太棒了
      </van-button>
    </div>
  </van-dialog>
</template>

<script setup>
defineProps({
  show: Boolean,
  result: Object
})

const emit = defineEmits(['update:show'])

const handleClose = () => {
  emit('update:show', false)
}
</script>

<style lang="scss" scoped>
.result-content {
  padding: var(--space-6);
  text-align: center;
}

.result-animation {
  margin-bottom: var(--space-4);
  
  .celebration {
    font-size: 48px;
    animation: bounce 0.6s ease infinite;
  }
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.result-points {
  display: flex;
  flex-direction: column;
  margin-bottom: var(--space-4);
  
  .points-label {
    font-size: var(--text-sm);
    color: var(--text-secondary);
  }
  
  .points-value {
    font-size: var(--text-4xl);
    font-weight: var(--font-bold);
    color: #FF9500;
  }
}

.result-detail {
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  padding: var(--space-3);
  margin-bottom: var(--space-4);
  
  .detail-item {
    display: flex;
    justify-content: space-between;
    padding: var(--space-2) 0;
    font-size: var(--text-sm);
    
    &.special {
      color: #FF9500;
    }
  }
}

.result-streak {
  font-size: var(--text-base);
  margin-bottom: var(--space-2);
  
  strong {
    color: var(--color-primary);
    font-size: var(--text-xl);
  }
}

.result-message {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin-bottom: var(--space-4);
}
</style>
```

---

## 5. 测试要点

### 5.1 单元测试

```java
@SpringBootTest
class CheckinServiceTest {

    @Autowired
    private CheckinService checkinService;

    @Test
    void testCheckin_Success() {
        // 测试正常签到
        CheckinResultDTO result = checkinService.checkin(1L);
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.getPointsEarned() >= 5);
    }

    @Test
    void testCheckin_DuplicateCheckin() {
        // 测试重复签到
        checkinService.checkin(1L);
        assertThrows(BusinessException.class, () -> {
            checkinService.checkin(1L);
        });
    }

    @Test
    void testConsecutiveDays() {
        // 测试连续天数计算
        // 1. 连续签到
        // 2. 断签后重置
    }
}
```

### 5.2 集成测试要点

1. **签到流程**
   - 首次签到
   - 连续签到
   - 断签后重新签到

2. **积分计算**
   - 基础积分
   - 连续签到奖励
   - 早起奖励

3. **边界情况**
   - 跨天签到
   - 时区处理
   - 并发签到

---

## 6. 部署注意事项

### 6.1 数据库迁移

```bash
# 执行迁移脚本
mysql -u root -p blog_db < docs/migrations/V1.2.0__checkin_system.sql
```

### 6.2 缓存配置

签到状态建议缓存，过期时间设置为当日结束：

```java
@Cacheable(value = "checkin", key = "#userId + ':' + #date", unless = "#result == null")
public boolean existsByUserAndDate(Long userId, LocalDate date) {
    // ...
}
```

### 6.3 定时任务

建议配置定时任务清理过期缓存：

```java
@Scheduled(cron = "0 0 0 * * ?")
public void clearDailyCache() {
    // 清理昨日签到缓存
}
```

---

## 7. 扩展计划

### 7.1 短期扩展

- [ ] 签到提醒通知
- [ ] 补签卡功能
- [ ] 签到排行榜

### 7.2 长期扩展

- [ ] 积分兑换商城
- [ ] 签到任务系统
- [ ] 签到成就联动
