package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.exception.BusinessException;
import com.blog.domain.dto.*;
import com.blog.domain.entity.Topic;
import com.blog.domain.enums.AnalysisStatus;
import com.blog.domain.enums.TopicPriority;
import com.blog.domain.enums.TopicStatus;
import com.blog.repository.mapper.TopicMapper;
import com.blog.service.TopicService;
import com.blog.service.ai.AiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TopicServiceImpl implements TopicService {

    private final TopicMapper topicMapper;
    private final AiService aiService;
    private final ObjectMapper objectMapper;

    @Override
    public Page<TopicVO> listTopics(TopicQueryRequest request) {
        Page<Topic> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<Topic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(
                        TopicStatus.codeFromName(request.getStatus()) != null,
                        Topic::getStatus,
                        TopicStatus.codeFromName(request.getStatus()))
                .eq(
                        TopicPriority.codeFromName(request.getPriority()) != null,
                        Topic::getPriority,
                        TopicPriority.codeFromName(request.getPriority()))
                .eq(request.getAnalysisStatus() != null, Topic::getAnalysisStatus, request.getAnalysisStatus())
                .like(
                        request.getKeyword() != null && !request.getKeyword().isBlank(),
                        Topic::getTitle,
                        request.getKeyword())
                .orderByDesc(Topic::getPriority)
                .orderByDesc(Topic::getCreateTime);

        Page<Topic> result = topicMapper.selectPage(page, wrapper);

        Page<TopicVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    @Override
    public TopicVO getTopicDetail(Long id) {
        Topic topic = topicMapper.selectById(id);
        if (topic == null) {
            throw new BusinessException("话题不存在");
        }
        return toVO(topic);
    }

    @Override
    public Long createTopic(TopicCreateRequest request) {
        Topic topic = new Topic();
        BeanUtils.copyProperties(request, topic);
        topic.setStatus(0);
        topic.setAnalysisStatus(0);
        topic.setPriority(request.getPriority() != null ? request.getPriority() : 2);

        topicMapper.insert(topic);

        // 如果选择了自动分析，异步触发AI分析
        if (Boolean.TRUE.equals(request.getAutoAnalyze())) {
            triggerAnalysis(topic.getId());
        }

        return topic.getId();
    }

    @Override
    public void updateTopic(Long id, TopicUpdateRequest request) {
        Topic topic = topicMapper.selectById(id);
        if (topic == null) {
            throw new BusinessException("话题不存在");
        }

        if (request.getTitle() != null) topic.setTitle(request.getTitle());
        if (request.getDescription() != null) topic.setDescription(request.getDescription());
        if (request.getSource() != null) topic.setSource(request.getSource());
        if (request.getSourceUrl() != null) topic.setSourceUrl(request.getSourceUrl());
        if (request.getPriority() != null) topic.setPriority(request.getPriority());

        topicMapper.updateById(topic);
    }

    @Override
    public void deleteTopic(Long id) {
        // 使用软删除：MyBatis Plus 的 deleteById 会自动处理 @TableLogic 标记的字段
        // 将 deleted 字段从 0 更新为 1
        topicMapper.deleteById(id);
    }

    @Override
    public void analyzeTopic(Long id) {
        triggerAnalysis(id);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Topic topic = topicMapper.selectById(id);
        if (topic == null) {
            throw new BusinessException("话题不存在");
        }
        topic.setStatus(status);
        topicMapper.updateById(topic);
    }

    @Override
    public void updateStatusWithString(Long id, String statusStr) {
        Integer statusCode = TopicStatus.codeFromName(statusStr);
        if (statusCode == null) {
            throw new BusinessException("无效的状态: " + statusStr);
        }
        updateStatus(id, statusCode);
    }

    @Override
    public void linkArticle(Long id, Long articleId) {
        Topic topic = topicMapper.selectById(id);
        if (topic == null) {
            throw new BusinessException("话题不存在");
        }
        topic.setArticleId(articleId);
        topic.setStatus(2); // 已发布
        topicMapper.updateById(topic);
    }

    @Async
    public void triggerAnalysis(Long topicId) {
        Topic topic = topicMapper.selectById(topicId);
        if (topic == null) {
            log.error("话题不存在: {}", topicId);
            return;
        }

        // 更新状态为分析中
        topic.setAnalysisStatus(1);
        topicMapper.updateById(topic);

        try {
            // 构建AI请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("title", topic.getTitle() != null ? topic.getTitle() : "");
            params.put("description", topic.getDescription() != null ? topic.getDescription() : "");

            // 调用AI服务（使用自定义prompt）
            String analysis = aiService.generate("topic_analysis", params);

            // 存储分析结果
            topic.setAnalysis(analysis);
            topic.setAnalysisStatus(2);
            topicMapper.updateById(topic);

            log.info("话题分析完成: {}", topicId);
        } catch (Exception e) {
            log.error("话题分析失败: {}", topicId, e);
            topic.setAnalysisStatus(3);
            topicMapper.updateById(topic);
        }
    }

    private TopicVO toVO(Topic topic) {
        TopicVO vo = new TopicVO();
        BeanUtils.copyProperties(topic, vo);

        // 设置 sourceLink 别名
        vo.setSourceLink(topic.getSourceUrl());

        // 使用枚举转换状态
        vo.setStatus(TopicStatus.nameFromCode(topic.getStatus()));
        vo.setPriority(TopicPriority.nameFromCode(topic.getPriority()));
        vo.setAiStatus(AnalysisStatus.nameFromCode(topic.getAnalysisStatus()));

        // 设置布尔状态
        vo.setAiAnalyzed(
                topic.getAnalysisStatus() != null && topic.getAnalysisStatus() == AnalysisStatus.COMPLETED.getCode());
        vo.setAiAnalyzing(
                topic.getAnalysisStatus() != null && topic.getAnalysisStatus() == AnalysisStatus.ANALYZING.getCode());

        // 解析 analysis JSON 为对象
        if (topic.getAnalysis() != null && !topic.getAnalysis().isBlank()) {
            try {
                String json = topic.getAnalysis().trim();
                // 去掉 markdown 代码块包裹
                if (json.startsWith("```json")) {
                    json = json.substring(7);
                } else if (json.startsWith("```")) {
                    json = json.substring(3);
                }
                if (json.endsWith("```")) {
                    json = json.substring(0, json.length() - 3);
                }
                json = json.trim();
                vo.setAiAnalysisResult(objectMapper.readValue(json, Object.class));
            } catch (Exception e) {
                log.warn("解析AI分析结果失败: {}", e.getMessage());
                vo.setAiAnalysisResult(null);
            }
        }

        return vo;
    }
}
