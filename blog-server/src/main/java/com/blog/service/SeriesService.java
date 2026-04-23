package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.domain.dto.SeriesDTO;
import com.blog.domain.dto.SeriesQueryDTO;
import com.blog.domain.vo.SeriesListVO;
import com.blog.domain.vo.SeriesVO;

import java.util.List;

public interface SeriesService {

    Page<SeriesListVO> pageSeries(SeriesQueryDTO query);

    SeriesVO getSeriesById(Long id);

    List<SeriesListVO> getHotSeries(int limit);

    void saveOrUpdateSeries(SeriesDTO dto);

    void deleteSeries(Long id);

    void addArticlesToSeries(Long seriesId, List<Long> articleIds);

    void removeArticleFromSeries(Long seriesId, Long articleId);

    void updateArticlesOrder(Long seriesId, List<Long> articleIds);

    List<SeriesListVO> getSeriesByArticleId(Long articleId);
}
