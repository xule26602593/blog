package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.exception.BusinessException;
import com.blog.common.result.ErrorCode;
import com.blog.common.utils.BeanCopyUtils;
import com.blog.domain.dto.SeriesDTO;
import com.blog.domain.dto.SeriesQueryDTO;
import com.blog.domain.entity.Article;
import com.blog.domain.entity.Series;
import com.blog.domain.entity.SeriesArticle;
import com.blog.domain.vo.SeriesListVO;
import com.blog.domain.vo.SeriesVO;
import com.blog.repository.mapper.ArticleMapper;
import com.blog.repository.mapper.SeriesArticleMapper;
import com.blog.repository.mapper.SeriesMapper;
import com.blog.security.LoginUser;
import com.blog.service.SeriesService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SeriesServiceImpl implements SeriesService {

    private final SeriesMapper seriesMapper;
    private final SeriesArticleMapper seriesArticleMapper;
    private final ArticleMapper articleMapper;

    @Override
    public Page<SeriesListVO> pageSeries(SeriesQueryDTO query) {
        Page<Series> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<Series> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Series::getDeleted, 0);

        if (StringUtils.hasText(query.getName())) {
            wrapper.like(Series::getName, query.getName());
        }
        if (query.getMode() != null) {
            wrapper.eq(Series::getMode, query.getMode());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Series::getStatus, query.getStatus());
        }

        wrapper.orderByDesc(Series::getSort).orderByDesc(Series::getCreateTime);

        Page<Series> seriesPage = seriesMapper.selectPage(page, wrapper);

        Page<SeriesListVO> voPage = new Page<>(seriesPage.getCurrent(), seriesPage.getSize(), seriesPage.getTotal());
        voPage.setRecords(seriesPage.getRecords().stream()
                .map(series -> BeanCopyUtils.copy(series, SeriesListVO.class))
                .collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public SeriesVO getSeriesById(Long id) {
        Series series = seriesMapper.selectById(id);
        if (series == null || series.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        seriesMapper.incrementViewCount(id);
        series.setViewCount(series.getViewCount() + 1);

        SeriesVO vo = BeanCopyUtils.copy(series, SeriesVO.class);

        LambdaQueryWrapper<SeriesArticle> saWrapper = new LambdaQueryWrapper<>();
        saWrapper.eq(SeriesArticle::getSeriesId, id);
        if (series.getMode() == 0) {
            saWrapper.orderByAsc(SeriesArticle::getChapterOrder);
        } else {
            saWrapper.orderByDesc(SeriesArticle::getCreateTime);
        }

        List<SeriesArticle> seriesArticles = seriesArticleMapper.selectList(saWrapper);
        if (!seriesArticles.isEmpty()) {
            List<Long> articleIds =
                    seriesArticles.stream().map(SeriesArticle::getArticleId).collect(Collectors.toList());

            List<Article> articles = articleMapper.selectBatchIds(articleIds);

            List<SeriesVO.SeriesArticleVO> articleVOs = new ArrayList<>();
            for (SeriesArticle sa : seriesArticles) {
                Article article = articles.stream()
                        .filter(a -> a.getId().equals(sa.getArticleId()))
                        .findFirst()
                        .orElse(null);
                if (article != null && article.getDeleted() == 0 && article.getStatus() == 1) {
                    SeriesVO.SeriesArticleVO articleVO = new SeriesVO.SeriesArticleVO();
                    articleVO.setId(article.getId());
                    articleVO.setTitle(article.getTitle());
                    articleVO.setSummary(article.getSummary());
                    articleVO.setCoverImage(article.getCoverImage());
                    articleVO.setViewCount(article.getViewCount());
                    articleVO.setChapterOrder(sa.getChapterOrder());
                    articleVOs.add(articleVO);
                }
            }
            vo.setArticles(articleVOs);
        }

        return vo;
    }

    @Override
    public List<SeriesListVO> getHotSeries(int limit) {
        LambdaQueryWrapper<Series> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Series::getDeleted, 0)
                .eq(Series::getStatus, 1)
                .gt(Series::getArticleCount, 0)
                .orderByDesc(Series::getViewCount)
                .last("LIMIT " + limit);

        List<Series> seriesList = seriesMapper.selectList(wrapper);
        return seriesList.stream()
                .map(series -> BeanCopyUtils.copy(series, SeriesListVO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void saveOrUpdateSeries(SeriesDTO dto) {
        Series series;

        if (dto.getId() == null) {
            series = new Series();
            series.setViewCount(0L);
            series.setArticleCount(0);
            series.setAuthorId(getCurrentUserId());
        } else {
            series = seriesMapper.selectById(dto.getId());
            if (series == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND);
            }
        }

        series.setName(dto.getName());
        series.setDescription(dto.getDescription());
        series.setCoverImage(dto.getCoverImage());
        series.setMode(dto.getMode() != null ? dto.getMode() : 0);
        series.setSort(dto.getSort() != null ? dto.getSort() : 0);
        series.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);

        if (dto.getId() == null) {
            seriesMapper.insert(series);
        } else {
            seriesMapper.updateById(series);
        }

        if (dto.getArticleIds() != null && !dto.getArticleIds().isEmpty()) {
            addArticlesToSeriesInternal(series.getId(), dto.getArticleIds(), series.getMode() == 0);
        }
    }

    @Override
    @Transactional
    public void deleteSeries(Long id) {
        Series series = seriesMapper.selectById(id);
        if (series == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        seriesArticleMapper.delete(new LambdaQueryWrapper<SeriesArticle>().eq(SeriesArticle::getSeriesId, id));

        seriesMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void addArticlesToSeries(Long seriesId, List<Long> articleIds) {
        Series series = seriesMapper.selectById(seriesId);
        if (series == null || series.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        addArticlesToSeriesInternal(seriesId, articleIds, series.getMode() == 0);
    }

    private void addArticlesToSeriesInternal(Long seriesId, List<Long> articleIds, boolean ordered) {
        int maxOrder = 0;
        if (ordered) {
            Integer currentMax = seriesArticleMapper.selectMaxChapterOrder(seriesId);
            maxOrder = currentMax != null ? currentMax : 0;
        }

        for (Long articleId : articleIds) {
            Long exists = seriesArticleMapper.selectCount(new LambdaQueryWrapper<SeriesArticle>()
                    .eq(SeriesArticle::getSeriesId, seriesId)
                    .eq(SeriesArticle::getArticleId, articleId));

            if (exists == 0) {
                SeriesArticle sa = new SeriesArticle();
                sa.setSeriesId(seriesId);
                sa.setArticleId(articleId);
                if (ordered) {
                    sa.setChapterOrder(++maxOrder);
                }
                seriesArticleMapper.insert(sa);
                seriesMapper.incrementArticleCount(seriesId);
            }
        }
    }

    @Override
    @Transactional
    public void removeArticleFromSeries(Long seriesId, Long articleId) {
        int deleted = seriesArticleMapper.delete(new LambdaQueryWrapper<SeriesArticle>()
                .eq(SeriesArticle::getSeriesId, seriesId)
                .eq(SeriesArticle::getArticleId, articleId));

        if (deleted > 0) {
            seriesMapper.decrementArticleCount(seriesId);
        }
    }

    @Override
    @Transactional
    public void updateArticlesOrder(Long seriesId, List<Long> articleIds) {
        for (int i = 0; i < articleIds.size(); i++) {
            SeriesArticle sa = seriesArticleMapper.selectOne(new LambdaQueryWrapper<SeriesArticle>()
                    .eq(SeriesArticle::getSeriesId, seriesId)
                    .eq(SeriesArticle::getArticleId, articleIds.get(i)));

            if (sa != null) {
                sa.setChapterOrder(i + 1);
                seriesArticleMapper.updateById(sa);
            }
        }
    }

    @Override
    public List<SeriesListVO> getSeriesByArticleId(Long articleId) {
        List<Long> seriesIds = seriesArticleMapper.selectSeriesIdsByArticleId(articleId);
        if (seriesIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Series> seriesList = seriesMapper.selectBatchIds(seriesIds);
        return seriesList.stream()
                .filter(s -> s.getDeleted() == 0 && s.getStatus() == 1)
                .map(series -> BeanCopyUtils.copy(series, SeriesListVO.class))
                .collect(Collectors.toList());
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return ((LoginUser) authentication.getPrincipal()).getUserId();
        }
        return null;
    }
}
