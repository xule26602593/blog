package com.blog.service;

import com.blog.common.result.PageResult;
import com.blog.domain.vo.ReadingHistoryVO;

public interface ReadingHistoryService {

    /**
     * 记录阅读历史
     * @param articleId 文章ID
     */
    void recordHistory(Long articleId);

    /**
     * 获取阅读历史列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 阅读历史列表
     */
    PageResult<ReadingHistoryVO> getHistoryList(int pageNum, int pageSize);

    /**
     * 删除单条阅读历史
     * @param articleId 文章ID
     */
    void deleteHistory(Long articleId);

    /**
     * 清空所有阅读历史
     */
    void clearHistory();
}
