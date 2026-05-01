package com.blog.service.impl;

import com.blog.service.AchievementService;
import com.blog.service.AchievementTriggerService;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementTriggerServiceImpl implements AchievementTriggerService {

    private final AchievementService achievementService;

    private static final List<String> ARTICLE_ACHIEVEMENTS = Arrays.asList(
            "first_article",
            "article_10",
            "article_50",
            "article_100",
            "article_500",
            "like_100",
            "like_500",
            "like_1000",
            "like_5000",
            "view_1000",
            "view_10000",
            "view_100000");

    private static final List<String> COMMENT_ACHIEVEMENTS =
            Arrays.asList("first_comment", "comment_10", "comment_50", "comment_100");

    private static final List<String> FOLLOW_ACHIEVEMENTS = Arrays.asList(
            "follower_10",
            "follower_50",
            "follower_100",
            "follower_500",
            "follower_1000",
            "following_10",
            "following_50");

    private static final List<String> CHECKIN_STREAK_ACHIEVEMENTS =
            Arrays.asList("checkin_7", "checkin_30", "checkin_100", "checkin_365");

    private static final List<String> CHECKIN_TOTAL_ACHIEVEMENTS =
            Arrays.asList("checkin_1", "checkin_total_30", "checkin_total_100", "checkin_total_365");

    @Override
    @Async
    public void triggerArticleAchievements(Long userId) {
        log.debug("Triggering article achievements for user: {}", userId);
        ARTICLE_ACHIEVEMENTS.forEach(code -> {
            try {
                achievementService.checkAndUnlock(userId, code);
            } catch (Exception e) {
                log.error("Achievement check failed: code={}, userId={}", code, userId, e);
            }
        });
    }

    @Override
    @Async
    public void triggerCommentAchievements(Long userId) {
        log.debug("Triggering comment achievements for user: {}", userId);
        COMMENT_ACHIEVEMENTS.forEach(code -> {
            try {
                achievementService.checkAndUnlock(userId, code);
            } catch (Exception e) {
                log.error("Achievement check failed: code={}, userId={}", code, userId, e);
            }
        });
    }

    @Override
    @Async
    public void triggerFollowAchievements(Long userId) {
        log.debug("Triggering follow achievements for user: {}", userId);
        FOLLOW_ACHIEVEMENTS.forEach(code -> {
            try {
                achievementService.checkAndUnlock(userId, code);
            } catch (Exception e) {
                log.error("Achievement check failed: code={}, userId={}", code, userId, e);
            }
        });
    }

    @Override
    @Async
    public void triggerCheckinAchievements(Long userId, int consecutiveDays) {
        log.debug("Triggering checkin achievements for user: {}, consecutiveDays: {}", userId, consecutiveDays);

        CHECKIN_STREAK_ACHIEVEMENTS.forEach(code -> {
            try {
                achievementService.checkAndUnlock(userId, code);
            } catch (Exception e) {
                log.error("Achievement check failed: code={}, userId={}", code, userId, e);
            }
        });

        CHECKIN_TOTAL_ACHIEVEMENTS.forEach(code -> {
            try {
                achievementService.checkAndUnlock(userId, code);
            } catch (Exception e) {
                log.error("Achievement check failed: code={}, userId={}", code, userId, e);
            }
        });
    }

    @Override
    public void triggerSpecialAchievement(Long userId, String achievementCode) {
        log.debug("Triggering special achievement for user: {}, code: {}", userId, achievementCode);
        achievementService.checkAndUnlock(userId, achievementCode);
    }
}
