package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.domain.entity.Article;
import com.blog.domain.entity.Comment;
import com.blog.domain.entity.DailyStatistics;
import com.blog.domain.entity.Message;
import com.blog.domain.vo.StatisticsOverviewVO;
import com.blog.repository.mapper.*;
import com.blog.service.StatisticsService;
import java.time.LocalDate;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final DailyStatisticsMapper dailyStatisticsMapper;
    private final ArticleMapper articleMapper;
    private final CommentMapper commentMapper;
    private final MessageMapper messageMapper;
    private final UserMapper userMapper;

    @Override
    public StatisticsOverviewVO getOverview() {
        StatisticsOverviewVO vo = new StatisticsOverviewVO();

        // 今日统计
        LocalDate today = LocalDate.now();
        DailyStatistics todayStats = dailyStatisticsMapper.selectOne(
                new LambdaQueryWrapper<DailyStatistics>().eq(DailyStatistics::getDate, today));

        vo.setTodayPv(todayStats != null ? todayStats.getPv() : 0);
        vo.setTodayUv(todayStats != null ? todayStats.getUv() : 0);

        // 文章总数
        Long totalArticles = articleMapper.selectCount(
                new LambdaQueryWrapper<Article>().eq(Article::getStatus, 1).eq(Article::getDeleted, 0));
        vo.setTotalArticles(totalArticles.intValue());

        // 评论总数
        Long totalComments = commentMapper.selectCount(new LambdaQueryWrapper<Comment>().eq(Comment::getStatus, 1));
        vo.setTotalComments(totalComments.intValue());

        // 用户总数
        Long totalUsers = userMapper.selectCount(
                new LambdaQueryWrapper<com.blog.domain.entity.User>().eq(com.blog.domain.entity.User::getDeleted, 0));
        vo.setTotalUsers(totalUsers.intValue());

        // 待审核评论
        Long pendingComments = commentMapper.selectCount(new LambdaQueryWrapper<Comment>().eq(Comment::getStatus, 0));
        vo.setPendingComments(pendingComments.intValue());

        // 待审核留言
        Long pendingMessages = messageMapper.selectCount(new LambdaQueryWrapper<Message>().eq(Message::getStatus, 0));
        vo.setPendingMessages(pendingMessages.intValue());

        return vo;
    }

    @Override
    public List<Map<String, Object>> getTrend(String type, Integer days) {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<DailyStatistics> stats = dailyStatisticsMapper.selectList(new LambdaQueryWrapper<DailyStatistics>()
                .ge(DailyStatistics::getDate, startDate)
                .le(DailyStatistics::getDate, endDate)
                .orderByAsc(DailyStatistics::getDate));

        Map<LocalDate, DailyStatistics> statsMap = new HashMap<>();
        for (DailyStatistics stat : stats) {
            statsMap.put(stat.getDate(), stat);
        }

        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            DailyStatistics stat = statsMap.get(date);

            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            item.put(type, stat != null ? getValueByType(stat, type) : 0);
            result.add(item);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getHotArticles(Integer limit) {
        List<Article> articles = articleMapper.selectList(new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, 1)
                .eq(Article::getDeleted, 0)
                .orderByDesc(Article::getViewCount)
                .last("LIMIT " + limit));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Article article : articles) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", article.getId());
            item.put("title", article.getTitle());
            item.put("viewCount", article.getViewCount());
            item.put("likeCount", article.getLikeCount());
            item.put("commentCount", article.getCommentCount());
            result.add(item);
        }

        return result;
    }

    private Integer getValueByType(DailyStatistics stat, String type) {
        switch (type) {
            case "pv":
                return stat.getPv();
            case "uv":
                return stat.getUv();
            case "ip":
                return stat.getIpCount();
            default:
                return stat.getPv();
        }
    }
}
