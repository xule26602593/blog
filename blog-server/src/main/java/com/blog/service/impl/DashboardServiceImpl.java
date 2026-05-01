package com.blog.service.impl;

import com.blog.domain.vo.DashboardVO;
import com.blog.repository.mapper.ArticleMapper;
import com.blog.repository.mapper.CommentMapper;
import com.blog.repository.mapper.UserMapper;
import com.blog.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ArticleMapper articleMapper;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;

    @Override
    public DashboardVO getDashboardData() {
        DashboardVO vo = new DashboardVO();

        // 文章统计
        vo.setArticleCount(articleMapper.countPublished());
        vo.setTodayArticleCount(articleMapper.countTodayPublished());

        // 评论统计
        vo.setCommentCount(commentMapper.countApproved());
        vo.setTodayCommentCount(commentMapper.countTodayApproved());

        // 用户统计
        vo.setUserCount(userMapper.selectCount(null));

        // 浏览量统计
        vo.setViewCount(articleMapper.sumViewCount());

        return vo;
    }
}
