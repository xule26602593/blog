package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Update("UPDATE sys_user SET following_count = following_count + #{delta} WHERE id = #{userId}")
    int updateFollowingCount(@Param("userId") Long userId, @Param("delta") int delta);

    @Update("UPDATE sys_user SET follower_count = follower_count + #{delta} WHERE id = #{userId}")
    int updateFollowerCount(@Param("userId") Long userId, @Param("delta") int delta);
}
