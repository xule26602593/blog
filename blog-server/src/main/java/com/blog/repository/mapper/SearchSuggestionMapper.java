package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.SearchSuggestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SearchSuggestionMapper extends BaseMapper<SearchSuggestion> {

    @Select("SELECT keyword FROM search_suggestion WHERE keyword LIKE CONCAT(#{prefix}, '%') ORDER BY search_count DESC, last_search_time DESC LIMIT #{limit}")
    List<String> selectByPrefix(@Param("prefix") String prefix, @Param("limit") int limit);

    @Select("SELECT keyword FROM search_suggestion ORDER BY search_count DESC LIMIT #{limit}")
    List<String> selectHotKeywords(@Param("limit") int limit);

    @Update("INSERT INTO search_suggestion (keyword, search_count, last_search_time) VALUES (#{keyword}, 1, NOW()) ON DUPLICATE KEY UPDATE search_count = search_count + 1, last_search_time = NOW()")
    int upsertKeyword(@Param("keyword") String keyword);
}
