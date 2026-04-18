package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.exception.BusinessException;
import com.blog.common.result.ErrorCode;
import com.blog.common.utils.BeanCopyUtils;
import com.blog.domain.dto.ArticleDTO;
import com.blog.domain.dto.ArticleQueryDTO;
import com.blog.domain.entity.Article;
import com.blog.domain.entity.ArticleTag;
import com.blog.domain.entity.Category;
import com.blog.domain.entity.Tag;
import com.blog.domain.entity.User;
import com.blog.domain.entity.UserAction;
import com.blog.domain.vo.ArticleListVO;
import com.blog.domain.vo.ArticleVO;
import com.blog.domain.vo.TagVO;
import com.blog.repository.mapper.ArticleMapper;
import com.blog.repository.mapper.ArticleTagMapper;
import com.blog.repository.mapper.CategoryMapper;
import com.blog.repository.mapper.TagMapper;
import com.blog.repository.mapper.UserActionMapper;
import com.blog.repository.mapper.UserMapper;
import com.blog.security.LoginUser;
import com.blog.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;
    private final UserMapper userMapper;
    private final UserActionMapper userActionMapper;

    @Override
    public Page<ArticleListVO> pageArticle(ArticleQueryDTO query) {
        Page<Article> page = new Page<>(query.getPageNum(), query.getPageSize());
        
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getDeleted, 0);
        
        if (StringUtils.hasText(query.getTitle())) {
            wrapper.like(Article::getTitle, query.getTitle());
        }
        if (query.getCategoryId() != null) {
            wrapper.eq(Article::getCategoryId, query.getCategoryId());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Article::getStatus, query.getStatus());
        }
        
        wrapper.orderByDesc(Article::getIsTop)
               .orderByDesc(Article::getCreateTime);
        
        Page<Article> articlePage = articleMapper.selectPage(page, wrapper);
        
        return convertToVOPage(articlePage);
    }

    @Override
    public ArticleVO getArticleById(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null || article.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
        
        // 增加浏览量
        articleMapper.incrementViewCount(id);
        article.setViewCount(article.getViewCount() + 1);
        
        ArticleVO vo = BeanCopyUtils.copy(article, ArticleVO.class);
        
        // 设置分类名称
        if (article.getCategoryId() != null) {
            Category category = categoryMapper.selectById(article.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }
        
        // 设置作者信息
        if (article.getAuthorId() != null) {
            User author = userMapper.selectById(article.getAuthorId());
            if (author != null) {
                vo.setAuthorName(author.getNickname());
                vo.setAuthorAvatar(author.getAvatar());
            }
        }
        
        // 设置标签
        List<Long> tagIds = articleTagMapper.selectTagIdsByArticleId(id);
        if (!tagIds.isEmpty()) {
            List<Tag> tags = tagMapper.selectBatchIds(tagIds);
            vo.setTags(tags.stream()
                    .map(tag -> BeanCopyUtils.copy(tag, TagVO.class))
                    .collect(Collectors.toList()));
        }
        
        // 检查当前用户是否点赞/收藏
        Long userId = getCurrentUserId();
        if (userId != null) {
            vo.setIsLiked(checkUserAction(userId, id, 1));
            vo.setIsFavorited(checkUserAction(userId, id, 2));
        }

        // 设置上一篇和下一篇
        vo.setPrevArticle(getPrevArticle(id, article.getCategoryId(), tagIds));
        vo.setNextArticle(getNextArticle(id, article.getCategoryId(), tagIds));

        return vo;
    }

    @Override
    public List<ArticleListVO> getHotArticles(int limit) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getDeleted, 0)
               .eq(Article::getStatus, 1)
               .orderByDesc(Article::getViewCount)
               .last("LIMIT " + limit);
        
        List<Article> articles = articleMapper.selectList(wrapper);
        return convertToVOList(articles);
    }

    @Override
    public List<ArticleListVO> getTopArticles() {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getDeleted, 0)
               .eq(Article::getStatus, 1)
               .eq(Article::getIsTop, 1)
               .orderByDesc(Article::getPublishTime);
        
        List<Article> articles = articleMapper.selectList(wrapper);
        return convertToVOList(articles);
    }

    @Override
    @Transactional
    public void saveOrUpdateArticle(ArticleDTO dto) {
        Article article;
        
        if (dto.getId() == null) {
            article = new Article();
            article.setViewCount(0L);
            article.setLikeCount(0L);
            article.setCommentCount(0);
            article.setAuthorId(getCurrentUserId());
        } else {
            article = articleMapper.selectById(dto.getId());
            if (article == null) {
                throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
            }
        }
        
        article.setTitle(dto.getTitle());
        article.setSummary(dto.getSummary());
        article.setContent(dto.getContent());
        article.setCoverImage(dto.getCoverImage());
        article.setCategoryId(dto.getCategoryId());
        article.setIsTop(dto.getIsTop() != null ? dto.getIsTop() : 0);
        article.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);
        
        // 发布时设置发布时间
        if (article.getStatus() == 1 && article.getPublishTime() == null) {
            article.setPublishTime(LocalDateTime.now());
        }
        
        if (dto.getId() == null) {
            articleMapper.insert(article);
        } else {
            articleMapper.updateById(article);
            // 删除旧的标签关联
            articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>()
                    .eq(ArticleTag::getArticleId, article.getId()));
        }
        
        // 保存标签关联
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            for (Long tagId : dto.getTagIds()) {
                ArticleTag articleTag = new ArticleTag();
                articleTag.setArticleId(article.getId());
                articleTag.setTagId(tagId);
                articleTagMapper.insert(articleTag);
            }
        }
    }

    @Override
    @Transactional
    public void deleteArticle(Long id) {
        // 使用 deleteById 会自动执行逻辑删除（UPDATE SET deleted = 1）
        int rows = articleMapper.deleteById(id);
        if (rows == 0) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
        article.setStatus(status);
        if (status == 1 && article.getPublishTime() == null) {
            article.setPublishTime(LocalDateTime.now());
        }
        articleMapper.updateById(article);
    }

    @Override
    @Transactional
    public void toggleTop(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
        article.setIsTop(article.getIsTop() == 1 ? 0 : 1);
        articleMapper.updateById(article);
    }

    @Override
    public Page<ArticleListVO> searchArticle(String keyword, int pageNum, int pageSize) {
        Page<Article> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getDeleted, 0)
               .eq(Article::getStatus, 1)
               .and(w -> w.like(Article::getTitle, keyword)
                       .or()
                       .like(Article::getSummary, keyword)
                       .or()
                       .like(Article::getContent, keyword))
               .orderByDesc(Article::getPublishTime);
        
        Page<Article> articlePage = articleMapper.selectPage(page, wrapper);
        return convertToVOPage(articlePage);
    }

    @Override
    public Page<ArticleListVO> getArticlesByCategory(Long categoryId, int pageNum, int pageSize) {
        Page<Article> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getDeleted, 0)
               .eq(Article::getStatus, 1)
               .eq(Article::getCategoryId, categoryId)
               .orderByDesc(Article::getPublishTime);

        Page<Article> articlePage = articleMapper.selectPage(page, wrapper);
        return convertToVOPage(articlePage);
    }

    @Override
    public Page<ArticleListVO> getArticlesByTag(Long tagId, int pageNum, int pageSize) {
        List<Long> articleIds = articleTagMapper.selectList(
                new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getTagId, tagId))
                .stream()
                .map(ArticleTag::getArticleId)
                .collect(Collectors.toList());

        if (articleIds.isEmpty()) {
            Page<ArticleListVO> emptyPage = new Page<>(pageNum, pageSize, 0);
            emptyPage.setRecords(new ArrayList<>());
            return emptyPage;
        }

        Page<Article> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getDeleted, 0)
               .eq(Article::getStatus, 1)
               .in(Article::getId, articleIds)
               .orderByDesc(Article::getPublishTime);

        Page<Article> articlePage = articleMapper.selectPage(page, wrapper);
        return convertToVOPage(articlePage);
    }

    @Override
    public List<ArticleListVO> getArchiveList() {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getDeleted, 0)
               .eq(Article::getStatus, 1)
               .orderByDesc(Article::getPublishTime);

        List<Article> articles = articleMapper.selectList(wrapper);
        return convertToVOList(articles);
    }

    @Override
    public ArticleVO.ArticleNavVO getPrevArticle(Long currentId, Long categoryId, List<Long> tagIds) {
        // 1. 查找同分类上一篇
        if (categoryId != null) {
            ArticleVO.ArticleNavVO article = articleMapper.selectPrevByCategory(currentId, categoryId);
            if (article != null) {
                return article;
            }
        }

        // 2. 查找同标签上一篇
        if (tagIds != null && !tagIds.isEmpty()) {
            String tagIdsStr = String.join(",", tagIds.stream().map(String::valueOf).collect(Collectors.toList()));
            ArticleVO.ArticleNavVO article = articleMapper.selectPrevByTags(currentId, tagIdsStr);
            if (article != null) {
                return article;
            }
        }

        // 3. 全局上一篇
        return articleMapper.selectPrevGlobal(currentId);
    }

    @Override
    public ArticleVO.ArticleNavVO getNextArticle(Long currentId, Long categoryId, List<Long> tagIds) {
        // 1. 查找同分类下一篇
        if (categoryId != null) {
            ArticleVO.ArticleNavVO article = articleMapper.selectNextByCategory(currentId, categoryId);
            if (article != null) {
                return article;
            }
        }

        // 2. 查找同标签下一篇
        if (tagIds != null && !tagIds.isEmpty()) {
            String tagIdsStr = String.join(",", tagIds.stream().map(String::valueOf).collect(Collectors.toList()));
            ArticleVO.ArticleNavVO article = articleMapper.selectNextByTags(currentId, tagIdsStr);
            if (article != null) {
                return article;
            }
        }

        // 3. 全局下一篇
        return articleMapper.selectNextGlobal(currentId);
    }

    private Page<ArticleListVO> convertToVOPage(Page<Article> articlePage) {
        Page<ArticleListVO> voPage = new Page<>(articlePage.getCurrent(), articlePage.getSize(), articlePage.getTotal());
        voPage.setRecords(convertToVOList(articlePage.getRecords()));
        return voPage;
    }
    
    private List<ArticleListVO> convertToVOList(List<Article> articles) {
        return articles.stream().map(article -> {
            ArticleListVO vo = BeanCopyUtils.copy(article, ArticleListVO.class);
            
            if (article.getCategoryId() != null) {
                Category category = categoryMapper.selectById(article.getCategoryId());
                if (category != null) {
                    vo.setCategoryName(category.getName());
                }
            }
            
            List<Long> tagIds = articleTagMapper.selectTagIdsByArticleId(article.getId());
            if (!tagIds.isEmpty()) {
                List<Tag> tags = tagMapper.selectBatchIds(tagIds);
                vo.setTags(tags.stream()
                        .map(tag -> BeanCopyUtils.copy(tag, TagVO.class))
                        .collect(Collectors.toList()));
            }
            
            if (article.getPublishTime() != null) {
                vo.setPublishTime(article.getPublishTime().toString());
            }
            
            return vo;
        }).collect(Collectors.toList());
    }
    
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return ((LoginUser) authentication.getPrincipal()).getUserId();
        }
        return null;
    }
    
    private Boolean checkUserAction(Long userId, Long articleId, Integer actionType) {
        Long count = userActionMapper.selectCount(new LambdaQueryWrapper<UserAction>()
                .eq(UserAction::getUserId, userId)
                .eq(UserAction::getArticleId, articleId)
                .eq(UserAction::getActionType, actionType));
        return count > 0;
    }
}
