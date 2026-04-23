package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.SearchHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SearchHistoryMapper extends BaseMapper<SearchHistory> {

    @Select("SELECT DISTINCT keyword FROM search_history WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{limit}")
    List<String> selectUserHistory(@Param("userId") Long userId, @Param("limit") int limit);
}
