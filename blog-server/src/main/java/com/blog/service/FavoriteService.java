package com.blog.service;

import com.blog.common.result.PageResult;
import com.blog.domain.vo.FavoriteVO;

public interface FavoriteService {

    /**
     * 获取当前用户的收藏列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param keyword 关键词（可选）
     * @return 收藏列表
     */
    PageResult<FavoriteVO> getFavorites(int pageNum, int pageSize, String keyword);
}
