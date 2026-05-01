package com.blog.service;

import com.blog.domain.vo.StatisticsOverviewVO;
import java.util.List;
import java.util.Map;

public interface StatisticsService {

    StatisticsOverviewVO getOverview();

    List<Map<String, Object>> getTrend(String type, Integer days);

    List<Map<String, Object>> getHotArticles(Integer limit);
}
