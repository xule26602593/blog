package com.blog.service;

public interface AchievementTriggerService {

    void triggerArticleAchievements(Long userId);

    void triggerCommentAchievements(Long userId);

    void triggerFollowAchievements(Long userId);

    void triggerCheckinAchievements(Long userId, int consecutiveDays);

    void triggerSpecialAchievement(Long userId, String achievementCode);
}
