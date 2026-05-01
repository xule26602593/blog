package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.ArticleTag;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ArticleTagMapper extends BaseMapper<ArticleTag> {

    @Select("SELECT tag_id FROM article_tag WHERE article_id = #{articleId}")
    List<Long> selectTagIdsByArticleId(@Param("articleId") Long articleId);

    void deleteByArticleId(@Param("articleId") Long articleId);

    /**
     * 批量查询多篇文章的标签ID
     * @param articleIds 文章ID列表
     * @return 文章标签关联列表
     */
    @Select("<script>" + "SELECT article_id, tag_id FROM article_tag "
            + "WHERE article_id IN "
            + "<foreach collection='articleIds' item='id' open='(' separator=',' close=')'>"
            + "#{id}"
            + "</foreach>"
            + "</script>")
    List<ArticleTag> selectByArticleIds(@Param("articleIds") List<Long> articleIds);
}
