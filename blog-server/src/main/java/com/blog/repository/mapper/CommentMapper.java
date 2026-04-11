package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    
    @Update("UPDATE comment SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    
    @Select("SELECT COUNT(*) FROM comment WHERE status = 1")
    Long countApproved();
    
    @Select("SELECT COUNT(*) FROM comment WHERE status = 1 AND DATE(create_time) = CURDATE()")
    Long countTodayApproved();
}
