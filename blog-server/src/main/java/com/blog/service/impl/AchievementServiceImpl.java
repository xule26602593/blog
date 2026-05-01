package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.domain.entity.Achievement;
import com.blog.domain.entity.UserAchievement;
import com.blog.domain.vo.AchievementVO;
import com.blog.domain.vo.UserAchievementVO;
import com.blog.repository.mapper.AchievementMapper;
import com.blog.repository.mapper.UserAchievementMapper;
import com.blog.repository.mapper.UserMapper;
import com.blog.service.AchievementService;
import com.blog.service.NotificationService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementServiceImpl implements AchievementService {

    private final AchievementMapper achievementMapper;
    private final UserAchievementMapper userAchievementMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    @Override
    @Cacheable(value = "achievements", key = "'all'")
    public List<AchievementVO> listAll() {
        return achievementMapper
                .selectList(new LambdaQueryWrapper<Achievement>()
                        .eq(Achievement::getStatus, 1)
                        .orderByAsc(Achievement::getCategory)
                        .orderByAsc(Achievement::getSort))
                .stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AchievementVO> listByCategory(String category) {
        return achievementMapper
                .selectList(new LambdaQueryWrapper<Achievement>()
                        .eq(Achievement::getStatus, 1)
                        .eq(Achievement::getCategory, category)
                        .orderByAsc(Achievement::getSort))
                .stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserAchievementVO> getUserAchievements(Long userId) {
        List<Achievement> achievements = achievementMapper.selectList(new LambdaQueryWrapper<Achievement>()
                .eq(Achievement::getStatus, 1)
                .orderByAsc(Achievement::getSort));

        List<UserAchievement> userAchievements = userAchievementMapper.selectList(
                new LambdaQueryWrapper<UserAchievement>().eq(UserAchievement::getUserId, userId));

        return achievements.stream()
                .map(achievement -> {
                    UserAchievement ua = userAchievements.stream()
                            .filter(u -> u.getAchievementId().equals(achievement.getId()))
                            .findFirst()
                            .orElse(null);
                    return convertToUserVO(achievement, ua, userId);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<UserAchievementVO> getUserUnlockedAchievements(Long userId) {
        return getUserAchievements(userId).stream()
                .filter(UserAchievementVO::getUnlocked)
                .collect(Collectors.toList());
    }

    @Override
    public UserAchievementVO getUserAchievementProgress(Long userId, Long achievementId) {
        Achievement achievement = achievementMapper.selectById(achievementId);
        if (achievement == null) return null;

        UserAchievement ua = userAchievementMapper.selectOne(new LambdaQueryWrapper<UserAchievement>()
                .eq(UserAchievement::getUserId, userId)
                .eq(UserAchievement::getAchievementId, achievementId));

        return convertToUserVO(achievement, ua, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "userAchievements", key = "#userId")
    public boolean checkAndUnlock(Long userId, String achievementCode) {
        Achievement achievement = achievementMapper.selectOne(new LambdaQueryWrapper<Achievement>()
                .eq(Achievement::getCode, achievementCode)
                .eq(Achievement::getStatus, 1));

        if (achievement == null) {
            log.warn("Achievement not found: {}", achievementCode);
            return false;
        }

        UserAchievement ua = userAchievementMapper.selectOne(new LambdaQueryWrapper<UserAchievement>()
                .eq(UserAchievement::getUserId, userId)
                .eq(UserAchievement::getAchievementId, achievement.getId()));

        if (ua != null && ua.getUnlocked() == 1) {
            return false;
        }

        int currentProgress = calculateProgress(userId, achievement);

        if (ua == null) {
            ua = new UserAchievement();
            ua.setUserId(userId);
            ua.setAchievementId(achievement.getId());
            ua.setProgress(currentProgress);
        } else {
            ua.setProgress(currentProgress);
        }

        boolean unlocked = false;
        if ("special".equals(achievement.getType()) || currentProgress >= achievement.getConditionValue()) {
            ua.setUnlocked(1);
            ua.setUnlockTime(LocalDateTime.now());
            unlocked = true;

            if (achievement.getPoints() > 0) {
                userMapper.addPoints(userId, achievement.getPoints());
            }

            userMapper.incrementAchievementCount(userId, achievement.getPoints());
            sendUnlockNotification(userId, achievement);
        }

        if (ua.getId() == null) {
            userAchievementMapper.insert(ua);
        } else {
            userAchievementMapper.updateById(ua);
        }

        return unlocked;
    }

    @Override
    public void checkCategory(Long userId, String category) {
        List<Achievement> achievements = achievementMapper.selectList(new LambdaQueryWrapper<Achievement>()
                .eq(Achievement::getStatus, 1)
                .eq(Achievement::getCategory, category));

        for (Achievement achievement : achievements) {
            try {
                checkAndUnlock(userId, achievement.getCode());
            } catch (Exception e) {
                log.error("Achievement check failed: code={}, userId={}", achievement.getCode(), userId, e);
            }
        }
    }

    private int calculateProgress(Long userId, Achievement achievement) {
        String code = achievement.getCode();

        if (code.startsWith("article_")) {
            return userMapper.countArticles(userId);
        }
        if (code.startsWith("like_")) {
            return userMapper.countTotalLikes(userId);
        }
        if (code.startsWith("view_")) {
            return userMapper.countTotalViews(userId);
        }
        if (code.startsWith("comment_")) {
            return userMapper.countComments(userId);
        }
        if (code.startsWith("follower_")) {
            return userMapper.getFollowerCount(userId);
        }
        if (code.startsWith("following_")) {
            return userMapper.getFollowingCount(userId);
        }
        if (code.startsWith("favorite_")) {
            return userMapper.countFavorites(userId);
        }
        if (code.startsWith("checkin_")) {
            if ("streak".equals(achievement.getType())) {
                return userMapper.getConsecutiveCheckinDays(userId);
            }
            return userMapper.getTotalCheckinDays(userId);
        }
        if ("special".equals(achievement.getType())) {
            return achievement.getConditionValue();
        }

        return 0;
    }

    private void sendUnlockNotification(Long userId, Achievement achievement) {
        try {
            notificationService.createAnnouncementNotification(
                    achievement.getId(),
                    String.format("恭喜解锁成就【%s】！获得%d积分奖励", achievement.getName(), achievement.getPoints()));
        } catch (Exception e) {
            log.error("Failed to send achievement notification", e);
        }
    }

    private AchievementVO convertToVO(Achievement achievement) {
        AchievementVO vo = new AchievementVO();
        vo.setId(achievement.getId());
        vo.setCode(achievement.getCode());
        vo.setName(achievement.getName());
        vo.setDescription(achievement.getDescription());
        vo.setIcon(achievement.getIcon());
        vo.setCategory(achievement.getCategory());
        vo.setType(achievement.getType());
        vo.setConditionValue(achievement.getConditionValue());
        vo.setPoints(achievement.getPoints());
        vo.setLevel(achievement.getLevel());
        return vo;
    }

    private UserAchievementVO convertToUserVO(Achievement achievement, UserAchievement ua, Long userId) {
        UserAchievementVO vo = new UserAchievementVO();
        vo.setId(achievement.getId());
        vo.setCode(achievement.getCode());
        vo.setName(achievement.getName());
        vo.setDescription(achievement.getDescription());
        vo.setIcon(achievement.getIcon());
        vo.setCategory(achievement.getCategory());
        vo.setType(achievement.getType());
        vo.setConditionValue(achievement.getConditionValue());
        vo.setPoints(achievement.getPoints());
        vo.setLevel(achievement.getLevel());

        int progress = ua != null ? ua.getProgress() : calculateProgress(userId, achievement);

        if (ua != null) {
            vo.setProgress(ua.getProgress());
            vo.setUnlocked(ua.getUnlocked() == 1);
            vo.setUnlockTime(ua.getUnlockTime());
        } else {
            vo.setProgress(progress);
            vo.setUnlocked(false);
        }

        if ("special".equals(achievement.getType())) {
            vo.setProgressPercent(vo.getUnlocked() ? 100 : 0);
        } else {
            vo.setProgressPercent(Math.min(100, (int) (progress * 100.0 / achievement.getConditionValue())));
        }

        return vo;
    }
}
