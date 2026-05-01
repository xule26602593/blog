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
import com.blog.service.AchievementTriggerService;
import com.blog.service.CheckinService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckinServiceImpl implements CheckinService {

    private final UserCheckinMapper checkinMapper;
    private final CheckinConfigMapper configMapper;
    private final UserMapper userMapper;
    private final AchievementTriggerService achievementTriggerService;

    private static final int BASE_POINTS = 5;
    private static final int EARLY_BIRD_START = 6;
    private static final int EARLY_BIRD_END = 8;
    private static final int EARLY_BIRD_BONUS = 5;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CheckinResultDTO checkin(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        if (checkinMapper.existsByUserAndDate(userId, today)) {
            throw new BusinessException("今日已签到，明天再来吧~");
        }

        LocalDate yesterday = today.minusDays(1);
        UserCheckin yesterdayCheckin = checkinMapper.findByUserAndDate(userId, yesterday);

        int consecutiveDays;
        if (yesterdayCheckin != null) {
            consecutiveDays = yesterdayCheckin.getConsecutiveDays() + 1;
        } else {
            consecutiveDays = 1;
        }

        int basePoints = BASE_POINTS;
        int bonusPoints = calculateBonusPoints(consecutiveDays);
        int specialBonus = calculateSpecialBonus(now);
        int totalPoints = basePoints + bonusPoints + specialBonus;

        UserCheckin checkin = new UserCheckin();
        checkin.setUserId(userId);
        checkin.setCheckinDate(today);
        checkin.setConsecutiveDays(consecutiveDays);
        checkin.setPointsEarned(totalPoints);
        checkin.setCheckinTime(now);
        checkinMapper.insert(checkin);

        userMapper.updateCheckinInfo(userId, totalPoints, consecutiveDays);

        if (achievementTriggerService != null) {
            try {
                achievementTriggerService.triggerCheckinAchievements(userId, consecutiveDays);
                if (specialBonus > 0) {
                    achievementTriggerService.triggerSpecialAchievement(userId, "early_bird");
                }
            } catch (Exception e) {
                log.error("Failed to trigger achievement", e);
            }
        }

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

        boolean isCheckedToday = checkinMapper.existsByUserAndDate(userId, today);

        var user = userMapper.selectById(userId);

        int currentConsecutiveDays = 0;
        if (isCheckedToday) {
            UserCheckin todayCheckin = checkinMapper.findByUserAndDate(userId, today);
            currentConsecutiveDays = todayCheckin.getConsecutiveDays();
        } else if (checkinMapper.existsByUserAndDate(userId, yesterday)) {
            UserCheckin yesterdayCheckin = checkinMapper.findByUserAndDate(userId, yesterday);
            currentConsecutiveDays = yesterdayCheckin.getConsecutiveDays();
        }

        int todayPoints = 0;
        if (isCheckedToday) {
            UserCheckin todayCheckin = checkinMapper.findByUserAndDate(userId, today);
            todayPoints = todayCheckin.getPointsEarned();
        }

        return CheckinStatusVO.builder()
                .isCheckedToday(isCheckedToday)
                .consecutiveDays(currentConsecutiveDays)
                .totalDays(user.getCheckinDays())
                .totalPoints(user.getTotalPoints())
                .maxConsecutiveDays(user.getMaxConsecutiveDays())
                .todayPoints(todayPoints)
                .build();
    }

    @Override
    public List<CheckinCalendarVO> getCheckinCalendar(Long userId, String month) {
        LocalDate startDate = LocalDate.parse(month + "-01");
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<UserCheckin> checkins = checkinMapper.selectByUserAndDateRange(userId, startDate, endDate);

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

    @Override
    public Page<UserCheckin> getCheckinHistory(Long userId, int page, int size) {
        Page<UserCheckin> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<UserCheckin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCheckin::getUserId, userId).orderByDesc(UserCheckin::getCheckinDate);
        return checkinMapper.selectPage(pageObj, wrapper);
    }

    private int calculateBonusPoints(int consecutiveDays) {
        LambdaQueryWrapper<CheckinConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckinConfig::getStatus, 1)
                .le(CheckinConfig::getConsecutiveDays, consecutiveDays)
                .orderByDesc(CheckinConfig::getConsecutiveDays)
                .last("LIMIT 1");

        CheckinConfig config = configMapper.selectOne(wrapper);
        return config != null ? config.getRewardPoints() : 0;
    }

    private int calculateSpecialBonus(LocalDateTime time) {
        int hour = time.getHour();
        if (hour >= EARLY_BIRD_START && hour < EARLY_BIRD_END) {
            return EARLY_BIRD_BONUS;
        }
        return 0;
    }

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
