package com.blog.service;

import com.blog.domain.vo.FollowVO;

import java.util.List;

public interface UserFollowService {

    void follow(Long userId, Long targetUserId);

    void unfollow(Long userId, Long targetUserId);

    boolean isFollowing(Long userId, Long targetUserId);

    List<FollowVO> getFollowingList(Long userId);

    List<FollowVO> getFollowerList(Long userId);

    Integer getFollowingCount(Long userId);

    Integer getFollowerCount(Long userId);
}
