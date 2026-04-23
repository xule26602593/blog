package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.SeriesArticle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SeriesArticleMapper extends BaseMapper<SeriesArticle> {

    @Select("SELECT series_id FROM series_article WHERE article_id = #{articleId}")
    List<Long> selectSeriesIdsByArticleId(@Param("articleId") Long articleId);

    @Select("SELECT MAX(chapter_order) FROM series_article WHERE series_id = #{seriesId}")
    Integer selectMaxChapterOrder(@Param("seriesId") Long seriesId);
}
