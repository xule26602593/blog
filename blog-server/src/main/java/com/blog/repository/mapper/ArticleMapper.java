package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.Article;
import com.blog.domain.vo.ArticleVO.ArticleNavVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
    
    @Update("UPDATE article SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(@Param("id") Long id);
    
    @Update("UPDATE article SET like_count = like_count + #{delta} WHERE id = #{id}")
    int updateLikeCount(@Param("id") Long id, @Param("delta") int delta);
    
    @Update("UPDATE article SET comment_count = comment_count + #{delta} WHERE id = #{id}")
    int updateCommentCount(@Param("id") Long id, @Param("delta") int delta);
    
    @Select("SELECT COUNT(*) FROM article WHERE deleted = 0 AND status = 1")
    Long countPublished();
    
    @Select("SELECT COUNT(*) FROM article WHERE deleted = 0 AND status = 1 AND DATE(publish_time) = CURDATE()")
    Long countTodayPublished();
    
    @Select("SELECT IFNULL(SUM(view_count), 0) FROM article WHERE deleted = 0")
    Long sumViewCount();

    // ==================== 相邻文章查询 ====================

    /**
     * 查找同分类的上一篇文章（发布时间早于当前文章，按时间倒序取第一条）
     */
    @Select("""
        SELECT a.id, a.title, c.name AS categoryName
        FROM article a
        LEFT JOIN category c ON a.category_id = c.id
        WHERE a.deleted = 0 AND a.status = 1
          AND a.category_id = #{categoryId}
          AND a.publish_time < (SELECT publish_time FROM article WHERE id = #{currentId})
        ORDER BY a.publish_time DESC
        LIMIT 1
        """)
    ArticleNavVO selectPrevByCategory(@Param("currentId") Long currentId, @Param("categoryId") Long categoryId);

    /**
     * 查找同分类的下一篇文章（发布时间晚于当前文章，按时间正序取第一条）
     */
    @Select("""
        SELECT a.id, a.title, c.name AS categoryName
        FROM article a
        LEFT JOIN category c ON a.category_id = c.id
        WHERE a.deleted = 0 AND a.status = 1
          AND a.category_id = #{categoryId}
          AND a.publish_time > (SELECT publish_time FROM article WHERE id = #{currentId})
        ORDER BY a.publish_time ASC
        LIMIT 1
        """)
    ArticleNavVO selectNextByCategory(@Param("currentId") Long currentId, @Param("categoryId") Long categoryId);

    /**
     * 查找同标签的上一篇文章
     */
    @SelectProvider(type = ArticleMapperProvider.class, method = "selectPrevByTags")
    ArticleNavVO selectPrevByTags(@Param("currentId") Long currentId, @Param("tagIdsStr") String tagIdsStr);

    /**
     * 查找同标签的下一篇文章
     */
    @SelectProvider(type = ArticleMapperProvider.class, method = "selectNextByTags")
    ArticleNavVO selectNextByTags(@Param("currentId") Long currentId, @Param("tagIdsStr") String tagIdsStr);

    /**
     * 查找全局上一篇文章（无分类和标签过滤）
     */
    @Select("""
        SELECT a.id, a.title, c.name AS categoryName
        FROM article a
        LEFT JOIN category c ON a.category_id = c.id
        WHERE a.deleted = 0 AND a.status = 1
          AND a.publish_time < (SELECT publish_time FROM article WHERE id = #{currentId})
        ORDER BY a.publish_time DESC
        LIMIT 1
        """)
    ArticleNavVO selectPrevGlobal(@Param("currentId") Long currentId);

    /**
     * 查找全局下一篇文章（无分类和标签过滤）
     */
    @Select("""
        SELECT a.id, a.title, c.name AS categoryName
        FROM article a
        LEFT JOIN category c ON a.category_id = c.id
        WHERE a.deleted = 0 AND a.status = 1
          AND a.publish_time > (SELECT publish_time FROM article WHERE id = #{currentId})
        ORDER BY a.publish_time ASC
        LIMIT 1
        """)
    ArticleNavVO selectNextGlobal(@Param("currentId") Long currentId);
}
