package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.UserFollow;
import com.blog.domain.vo.FollowVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {

    @Select("""
        SELECT u.id, u.username, u.nickname, u.avatar, uf.create_time AS followTime
        FROM user_follow uf
        INNER JOIN sys_user u ON uf.following_id = u.id
        WHERE uf.follower_id = #{userId} AND u.deleted = 0
        ORDER BY uf.create_time DESC
        """)
    List<FollowVO> selectFollowingList(@Param("userId") Long userId);

    @Select("""
        SELECT u.id, u.username, u.nickname, u.avatar, uf.create_time AS followTime
        FROM user_follow uf
        INNER JOIN sys_user u ON uf.follower_id = u.id
        WHERE uf.following_id = #{userId} AND u.deleted = 0
        ORDER BY uf.create_time DESC
        """)
    List<FollowVO> selectFollowerList(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM user_follow WHERE follower_id = #{userId}")
    Integer countFollowing(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM user_follow WHERE following_id = #{userId}")
    Integer countFollowers(@Param("userId") Long userId);
}
