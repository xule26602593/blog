package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.CommentLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CommentLikeMapper extends BaseMapper<CommentLike> {

    @Update("UPDATE comment SET like_count = like_count + 1 WHERE id = #{commentId}")
    void incrementLikeCount(@Param("commentId") Long commentId);

    @Update("UPDATE comment SET like_count = GREATEST(0, like_count - 1) WHERE id = #{commentId}")
    void decrementLikeCount(@Param("commentId") Long commentId);
}
