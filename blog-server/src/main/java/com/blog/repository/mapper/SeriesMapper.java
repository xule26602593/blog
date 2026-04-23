package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.Series;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SeriesMapper extends BaseMapper<Series> {

    @Update("UPDATE series SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(@Param("id") Long id);

    @Update("UPDATE series SET article_count = article_count + 1 WHERE id = #{id}")
    void incrementArticleCount(@Param("id") Long id);

    @Update("UPDATE series SET article_count = article_count - 1 WHERE id = #{id} AND article_count > 0")
    void decrementArticleCount(@Param("id") Long id);
}
