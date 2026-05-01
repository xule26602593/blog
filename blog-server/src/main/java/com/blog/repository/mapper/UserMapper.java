package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Update("UPDATE sys_user SET following_count = following_count + #{delta} WHERE id = #{userId}")
    int updateFollowingCount(@Param("userId") Long userId, @Param("delta") int delta);

    @Update("UPDATE sys_user SET follower_count = follower_count + #{delta} WHERE id = #{userId}")
    int updateFollowerCount(@Param("userId") Long userId, @Param("delta") int delta);

    @Update("UPDATE sys_user SET points = points + #{points}, total_points = total_points + #{points}, " +
            "checkin_days = checkin_days + 1, max_consecutive_days = GREATEST(max_consecutive_days, #{consecutive}), " +
            "last_checkin_date = CURRENT_DATE WHERE id = #{userId}")
    void updateCheckinInfo(@Param("userId") Long userId, @Param("points") int points, @Param("consecutive") int consecutive);

    @Update("UPDATE sys_user SET points = points + #{points}, total_points = total_points + #{points} WHERE id = #{userId}")
    void addPoints(@Param("userId") Long userId, @Param("points") int points);

    @Update("UPDATE sys_user SET achievement_count = achievement_count + 1, " +
            "total_achievement_points = total_achievement_points + #{points} WHERE id = #{userId}")
    void incrementAchievementCount(@Param("userId") Long userId, @Param("points") int points);

    @Select("SELECT COUNT(*) FROM article WHERE author_id = #{userId} AND deleted = 0 AND status = 1")
    int countArticles(@Param("userId") Long userId);

    @Select("SELECT COALESCE(SUM(like_count), 0) FROM article WHERE author_id = #{userId} AND deleted = 0")
    int countTotalLikes(@Param("userId") Long userId);

    @Select("SELECT COALESCE(SUM(view_count), 0) FROM article WHERE author_id = #{userId} AND deleted = 0")
    int countTotalViews(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM comment WHERE user_id = #{userId}")
    int countComments(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM user_action WHERE user_id = #{userId} AND action_type = 2")
    int countFavorites(@Param("userId") Long userId);

    @Select("SELECT follower_count FROM sys_user WHERE id = #{userId}")
    int getFollowerCount(@Param("userId") Long userId);

    @Select("SELECT following_count FROM sys_user WHERE id = #{userId}")
    int getFollowingCount(@Param("userId") Long userId);

    @Select("SELECT consecutive_days FROM user_checkin WHERE user_id = #{userId} ORDER BY checkin_date DESC LIMIT 1")
    int getConsecutiveCheckinDays(@Param("userId") Long userId);

    @Select("SELECT checkin_days FROM sys_user WHERE id = #{userId}")
    int getTotalCheckinDays(@Param("userId") Long userId);
}
