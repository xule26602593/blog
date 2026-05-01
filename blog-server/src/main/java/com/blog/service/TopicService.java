package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.domain.dto.*;

public interface TopicService {

    Page<TopicVO> listTopics(TopicQueryRequest request);

    TopicVO getTopicDetail(Long id);

    Long createTopic(TopicCreateRequest request);

    void updateTopic(Long id, TopicUpdateRequest request);

    void deleteTopic(Long id);

    void analyzeTopic(Long id);

    void updateStatus(Long id, Integer status);

    void updateStatusWithString(Long id, String status);

    void linkArticle(Long id, Long articleId);
}
