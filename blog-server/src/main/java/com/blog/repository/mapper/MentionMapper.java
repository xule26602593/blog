package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.Mention;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MentionMapper extends BaseMapper<Mention> {}
