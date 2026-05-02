package com.blog.service;

import com.blog.common.result.PageResult;
import com.blog.domain.enums.MentionSourceType;
import com.blog.domain.vo.MentionVO;
import java.util.List;

public interface MentionService {

    /**
     * 解析内容中的@用户昵称
     * @return 被@的用户ID列表
     */
    List<Long> parseMentions(String content);

    /**
     * 创建@提及记录并发送通知
     */
    void createMentions(MentionSourceType sourceType, Long sourceId, Long mentionerId, String content);

    /**
     * 获取用户的@提及通知列表
     */
    PageResult<MentionVO> listUserMentions(Long userId, int page, int size);
}
