package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.exception.BusinessException;
import com.blog.common.result.ErrorCode;
import com.blog.domain.entity.User;
import com.blog.domain.entity.UserFollow;
import com.blog.domain.vo.FollowVO;
import com.blog.repository.mapper.UserFollowMapper;
import com.blog.repository.mapper.UserMapper;
import com.blog.service.AchievementTriggerService;
import com.blog.service.UserFollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserFollowServiceImpl implements UserFollowService {

    private final UserFollowMapper userFollowMapper;
    private final UserMapper userMapper;
    private final AchievementTriggerService achievementTriggerService;

    @Override
    @Transactional
    public void follow(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能关注自己");
        }

        User targetUser = userMapper.selectById(targetUserId);
        if (targetUser == null || targetUser.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 检查是否已关注
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowerId, userId)
               .eq(UserFollow::getFollowingId, targetUserId);
        if (userFollowMapper.selectCount(wrapper) > 0) {
            return; // 已关注，幂等处理
        }

        // 创建关注关系
        UserFollow userFollow = new UserFollow();
        userFollow.setFollowerId(userId);
        userFollow.setFollowingId(targetUserId);
        userFollowMapper.insert(userFollow);

        // 更新计数
        userMapper.updateFollowingCount(userId, 1);
        userMapper.updateFollowerCount(targetUserId, 1);

        // 触发成就检查
        achievementTriggerService.triggerFollowAchievements(userId);
        achievementTriggerService.triggerFollowAchievements(targetUserId);
    }

    @Override
    @Transactional
    public void unfollow(Long userId, Long targetUserId) {
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowerId, userId)
               .eq(UserFollow::getFollowingId, targetUserId);

        UserFollow userFollow = userFollowMapper.selectOne(wrapper);
        if (userFollow == null) {
            return; // 未关注，幂等处理
        }

        userFollowMapper.deleteById(userFollow.getId());

        // 更新计数
        userMapper.updateFollowingCount(userId, -1);
        userMapper.updateFollowerCount(targetUserId, -1);
    }

    @Override
    public boolean isFollowing(Long userId, Long targetUserId) {
        if (userId == null || targetUserId == null) {
            return false;
        }
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowerId, userId)
               .eq(UserFollow::getFollowingId, targetUserId);
        return userFollowMapper.selectCount(wrapper) > 0;
    }

    @Override
    public List<FollowVO> getFollowingList(Long userId) {
        return userFollowMapper.selectFollowingList(userId);
    }

    @Override
    public List<FollowVO> getFollowerList(Long userId) {
        return userFollowMapper.selectFollowerList(userId);
    }

    @Override
    public Integer getFollowingCount(Long userId) {
        return userFollowMapper.countFollowing(userId);
    }

    @Override
    public Integer getFollowerCount(Long userId) {
        return userFollowMapper.countFollowers(userId);
    }
}
