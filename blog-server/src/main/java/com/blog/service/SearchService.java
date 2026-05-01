package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.domain.dto.SearchDTO;
import com.blog.domain.vo.SearchVO;
import java.util.List;

public interface SearchService {

    /**
     * 全文检索文章
     */
    Page<SearchVO> search(SearchDTO dto, String ipAddress);

    /**
     * 获取搜索建议
     */
    List<String> getSuggestions(String prefix);

    /**
     * 获取搜索历史
     */
    List<String> getHistory();

    /**
     * 获取热门搜索词
     */
    List<String> getHotKeywords();
}
