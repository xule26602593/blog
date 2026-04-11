package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.domain.dto.ArticleDTO;
import com.blog.domain.dto.ArticleQueryDTO;
import com.blog.domain.vo.ArticleListVO;
import com.blog.domain.vo.ArticleVO;

import java.util.List;

public interface ArticleService {

    Page<ArticleListVO> pageArticle(ArticleQueryDTO query);

    ArticleVO getArticleById(Long id);

    List<ArticleListVO> getHotArticles(int limit);

    List<ArticleListVO> getTopArticles();

    void saveOrUpdateArticle(ArticleDTO dto);

    void deleteArticle(Long id);

    void updateStatus(Long id, Integer status);

    void toggleTop(Long id);

    Page<ArticleListVO> searchArticle(String keyword, int pageNum, int pageSize);

    List<ArticleListVO> getArticlesByCategory(Long categoryId, int pageNum, int pageSize);

    List<ArticleListVO> getArticlesByTag(Long tagId, int pageNum, int pageSize);

    List<ArticleListVO> getArchiveList();
}
