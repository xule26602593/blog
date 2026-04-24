package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.DailyStatistics;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DailyStatisticsMapper extends BaseMapper<DailyStatistics> {
}
