package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.ReadingHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ReadingHistoryMapper extends BaseMapper<ReadingHistory> {

    @Update("INSERT INTO reading_history (user_id, article_id) VALUES (#{userId}, #{articleId}) " +
            "ON DUPLICATE KEY UPDATE update_time = NOW()")
    int upsert(@Param("userId") Long userId, @Param("articleId") Long articleId);
}
