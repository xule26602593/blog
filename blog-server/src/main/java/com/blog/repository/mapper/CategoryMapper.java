package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
    
    @Select("SELECT COUNT(*) FROM article WHERE category_id = #{categoryId} AND deleted = 0 AND status = 1")
    Integer countArticles(@Param("categoryId") Long categoryId);
}
