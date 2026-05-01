package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.result.PageResult;
import com.blog.domain.entity.Article;
import com.blog.domain.entity.ReadingHistory;
import com.blog.domain.vo.ReadingHistoryVO;
import com.blog.repository.mapper.ArticleMapper;
import com.blog.repository.mapper.ReadingHistoryMapper;
import com.blog.security.LoginUser;
import com.blog.service.ReadingHistoryService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReadingHistoryServiceImpl implements ReadingHistoryService {

    private final ReadingHistoryMapper readingHistoryMapper;
    private final ArticleMapper articleMapper;

    @Override
    public void recordHistory(Long articleId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return;
        }
        readingHistoryMapper.upsert(userId, articleId);
    }

    @Override
    public PageResult<ReadingHistoryVO> getHistoryList(int pageNum, int pageSize) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return PageResult.of(List.of(), 0L, (long) pageSize, (long) pageNum);
        }

        // 查询用户的阅读历史
        LambdaQueryWrapper<ReadingHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReadingHistory::getUserId, userId).orderByDesc(ReadingHistory::getUpdateTime);

        List<ReadingHistory> allHistory = readingHistoryMapper.selectList(wrapper);

        // 过滤已删除的文章并构建VO
        List<ReadingHistoryVO> allVOs = allHistory.stream()
                .map(history -> {
                    Article article = articleMapper.selectById(history.getArticleId());
                    if (article == null || article.getDeleted() == 1 || article.getStatus() != 1) {
                        return null;
                    }
                    return buildHistoryVO(history, article);
                })
                .filter(vo -> vo != null)
                .collect(Collectors.toList());

        // 分页
        long total = allVOs.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, allVOs.size());
        List<ReadingHistoryVO> records = start < allVOs.size() ? allVOs.subList(start, end) : List.of();

        return PageResult.of(records, total, (long) pageSize, (long) pageNum);
    }

    @Override
    @Transactional
    public void deleteHistory(Long articleId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return;
        }
        readingHistoryMapper.delete(new LambdaQueryWrapper<ReadingHistory>()
                .eq(ReadingHistory::getUserId, userId)
                .eq(ReadingHistory::getArticleId, articleId));
    }

    @Override
    @Transactional
    public void clearHistory() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return;
        }
        readingHistoryMapper.delete(new LambdaQueryWrapper<ReadingHistory>().eq(ReadingHistory::getUserId, userId));
    }

    private ReadingHistoryVO buildHistoryVO(ReadingHistory history, Article article) {
        ReadingHistoryVO vo = new ReadingHistoryVO();
        vo.setId(history.getId());
        vo.setArticleId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setCoverImage(article.getCoverImage());
        vo.setLastReadTime(history.getUpdateTime().toString());
        return vo;
    }

    private Long getCurrentUserId() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser.getUserId();
        }
        return null;
    }
}
