package com.blog.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.utils.HighlightUtil;
import com.blog.domain.dto.SearchDTO;
import com.blog.domain.entity.SearchHistory;
import com.blog.domain.vo.SearchVO;
import com.blog.repository.mapper.ArticleMapper;
import com.blog.repository.mapper.SearchHistoryMapper;
import com.blog.repository.mapper.SearchSuggestionMapper;
import com.blog.security.LoginUser;
import com.blog.service.SearchService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ArticleMapper articleMapper;
    private final SearchHistoryMapper searchHistoryMapper;
    private final SearchSuggestionMapper searchSuggestionMapper;

    @Value("${search.highlight.content-length:200}")
    private int highlightContentLength;

    @Value("${search.suggestion.max-size:10}")
    private int suggestionMaxSize;

    @Override
    @Transactional
    public Page<SearchVO> search(SearchDTO dto, String ipAddress) {
        String keyword = dto.getKeyword();
        if (!StringUtils.hasText(keyword)) {
            return new Page<>(dto.getPage(), dto.getSize(), 0);
        }

        // 计算分页偏移
        int offset = (dto.getPage() - 1) * dto.getSize();

        // 执行全文检索
        List<SearchVO> results = articleMapper.searchFulltext(keyword, dto.getSortBy(), offset, dto.getSize());
        Long total = articleMapper.searchFulltextCount(keyword);

        // 高亮处理
        for (SearchVO vo : results) {
            vo.setTitle(HighlightUtil.highlightOnly(vo.getTitle(), keyword));
            vo.setContentHighlight(HighlightUtil.highlight(vo.getContentHighlight(), keyword, highlightContentLength));
        }

        // 记录搜索历史和更新建议统计
        saveSearchHistory(keyword, results.size(), ipAddress);
        searchSuggestionMapper.upsertKeyword(keyword);

        // 构建分页结果
        Page<SearchVO> page = new Page<>(dto.getPage(), dto.getSize(), total);
        page.setRecords(results);
        return page;
    }

    @Override
    public List<String> getSuggestions(String prefix) {
        if (!StringUtils.hasText(prefix)) {
            return getHotKeywords();
        }
        return searchSuggestionMapper.selectByPrefix(prefix, suggestionMaxSize);
    }

    @Override
    public List<String> getHistory() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return List.of();
        }
        return searchHistoryMapper.selectUserHistory(userId, suggestionMaxSize);
    }

    @Override
    public List<String> getHotKeywords() {
        return searchSuggestionMapper.selectHotKeywords(suggestionMaxSize);
    }

    private void saveSearchHistory(String keyword, int resultCount, String ipAddress) {
        Long userId = getCurrentUserId();

        SearchHistory history = new SearchHistory();
        history.setUserId(userId);
        history.setKeyword(keyword);
        history.setResultCount(resultCount);
        history.setIpAddress(ipAddress);

        searchHistoryMapper.insert(history);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return ((LoginUser) authentication.getPrincipal()).getUserId();
        }
        return null;
    }
}
