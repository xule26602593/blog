package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.PageResult;
import com.blog.domain.entity.Article;
import com.blog.domain.entity.Mention;
import com.blog.domain.entity.Notification;
import com.blog.domain.entity.User;
import com.blog.domain.enums.MentionSourceType;
import com.blog.domain.enums.NotificationType;
import com.blog.domain.vo.MentionVO;
import com.blog.repository.mapper.ArticleMapper;
import com.blog.repository.mapper.MentionMapper;
import com.blog.repository.mapper.NotificationMapper;
import com.blog.repository.mapper.UserMapper;
import com.blog.service.MentionService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MentionServiceImpl implements MentionService {

    private final MentionMapper mentionMapper;
    private final UserMapper userMapper;
    private final ArticleMapper articleMapper;
    private final NotificationMapper notificationMapper;

    // 匹配 @昵称 格式，昵称可以是中文、英文、数字、下划线
    private static final Pattern MENTION_PATTERN = Pattern.compile("@([\\u4e00-\\u9fa5a-zA-Z0-9_]+)");

    @Override
    public List<Long> parseMentions(String content) {
        if (content == null || content.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> mentionedUserIds = new HashSet<>();
        Matcher matcher = MENTION_PATTERN.matcher(content);

        while (matcher.find()) {
            String nickname = matcher.group(1);
            // 根据昵称查找用户
            User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getNickname, nickname)
                    .eq(User::getStatus, 1));
            if (user != null) {
                mentionedUserIds.add(user.getId());
            }
        }

        return new ArrayList<>(mentionedUserIds);
    }

    @Override
    @Async
    @Transactional
    public void createMentions(MentionSourceType sourceType, Long sourceId, Long mentionerId, String content) {
        List<Long> mentionedUserIds = parseMentions(content);
        User mentioner = userMapper.selectById(mentionerId);
        if (mentioner == null) return;

        for (Long userId : mentionedUserIds) {
            // 跳过@自己
            if (userId.equals(mentionerId)) continue;

            // 创建提及记录
            Mention mention = new Mention();
            mention.setSourceType(sourceType.getCode());
            mention.setSourceId(sourceId);
            mention.setMentionedUserId(userId);
            mention.setMentionerId(mentionerId);
            mention.setContent(extractContext(content, userId));
            mention.setIsNotified(0);
            mentionMapper.insert(mention);

            // 发送通知
            sendMentionNotification(userId, mentioner, sourceType, sourceId);
        }
    }

    @Override
    public PageResult<MentionVO> listUserMentions(Long userId, int page, int size) {
        Page<Mention> mentionPage = new Page<>(page, size);
        Page<Mention> result = mentionMapper.selectPage(
                mentionPage,
                new LambdaQueryWrapper<Mention>()
                        .eq(Mention::getMentionedUserId, userId)
                        .orderByDesc(Mention::getCreateTime));

        List<MentionVO> vos =
                result.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());

        return PageResult.of(vos, result.getTotal(), (long) size, (long) page);
    }

    private void sendMentionNotification(Long userId, User mentioner, MentionSourceType sourceType, Long sourceId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(NotificationType.MENTION.getCode());
        notification.setTitle("有人@了你");
        notification.setContent(mentioner.getNickname() + "在" + sourceType.getDesc() + "中提到了你");
        notification.setRelatedId(sourceId);
        notification.setSenderId(mentioner.getId());
        notification.setIsRead(0);
        notificationMapper.insert(notification);
    }

    private String extractContext(String content, Long userId) {
        // 提取@相关的内容片段（前后各取20个字符）
        User user = userMapper.selectById(userId);
        if (user == null) return null;

        String nickname = user.getNickname();
        int index = content.indexOf("@" + nickname);
        if (index < 0) return null;

        int start = Math.max(0, index - 20);
        int end = Math.min(content.length(), index + nickname.length() + 1 + 20);
        return content.substring(start, end);
    }

    private MentionVO convertToVO(Mention mention) {
        MentionVO vo = new MentionVO();
        vo.setId(mention.getId());
        vo.setSourceType(mention.getSourceType());
        vo.setSourceId(mention.getSourceId());
        vo.setMentionerId(mention.getMentionerId());
        vo.setContent(mention.getContent());
        vo.setCreateTime(mention.getCreateTime());

        User mentioner = userMapper.selectById(mention.getMentionerId());
        if (mentioner != null) {
            vo.setMentionerNickname(mentioner.getNickname());
            vo.setMentionerAvatar(mentioner.getAvatar());
        }

        // 如果是文章中的@，获取文章标题
        if (MentionSourceType.ARTICLE.getCode().equals(mention.getSourceType())) {
            Article article = articleMapper.selectById(mention.getSourceId());
            if (article != null) {
                vo.setArticleTitle(article.getTitle());
            }
        }

        return vo;
    }
}
