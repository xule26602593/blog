package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.UserCheckin;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserCheckinMapper extends BaseMapper<UserCheckin> {

    @Select("SELECT COUNT(*) > 0 FROM user_checkin WHERE user_id = #{userId} AND checkin_date = #{date}")
    boolean existsByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Select("SELECT * FROM user_checkin WHERE user_id = #{userId} AND checkin_date = #{date}")
    UserCheckin findByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Select(
            "SELECT * FROM user_checkin WHERE user_id = #{userId} AND checkin_date BETWEEN #{start} AND #{end} ORDER BY checkin_date")
    List<UserCheckin> selectByUserAndDateRange(
            @Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);
}
