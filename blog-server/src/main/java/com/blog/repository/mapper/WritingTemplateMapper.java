package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.WritingTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface WritingTemplateMapper extends BaseMapper<WritingTemplate> {

    @Update("UPDATE writing_template SET usage_count = usage_count + 1 WHERE id = #{id}")
    int incrementUsageCount(Long id);
}
