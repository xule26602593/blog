package com.blog.service;

import com.blog.domain.vo.AchievementVO;
import com.blog.domain.vo.UserAchievementVO;
import java.util.List;

public interface AchievementService {

    List<AchievementVO> listAll();

    List<AchievementVO> listByCategory(String category);

    List<UserAchievementVO> getUserAchievements(Long userId);

    List<UserAchievementVO> getUserUnlockedAchievements(Long userId);

    UserAchievementVO getUserAchievementProgress(Long userId, Long achievementId);

    boolean checkAndUnlock(Long userId, String achievementCode);

    void checkCategory(Long userId, String category);
}
