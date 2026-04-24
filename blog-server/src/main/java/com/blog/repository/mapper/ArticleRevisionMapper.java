package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.ArticleRevision;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ArticleRevisionMapper extends BaseMapper<ArticleRevision> {

    @Select("SELECT MAX(version) FROM article_revision WHERE article_id = #{articleId}")
    Integer selectMaxVersion(@Param("articleId") Long articleId);
}
