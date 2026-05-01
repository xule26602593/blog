package com.blog.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.Result;
import com.blog.domain.dto.*;
import com.blog.service.TopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "后台话题管理")
@RestController
@RequestMapping("/api/admin/topics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class TopicController {

    private final TopicService topicService;

    @Operation(summary = "分页查询话题")
    @GetMapping
    public Result<Page<TopicVO>> listTopics(TopicQueryRequest request) {
        return Result.success(topicService.listTopics(request));
    }

    @Operation(summary = "获取话题详情")
    @GetMapping("/{id}")
    public Result<TopicVO> getTopic(@PathVariable Long id) {
        return Result.success(topicService.getTopicDetail(id));
    }

    @Operation(summary = "创建话题")
    @PostMapping
    public Result<Long> createTopic(@Valid @RequestBody TopicCreateRequest request) {
        Long id = topicService.createTopic(request);
        return Result.success(id);
    }

    @Operation(summary = "更新话题")
    @PutMapping("/{id}")
    public Result<Void> updateTopic(@PathVariable Long id, @RequestBody TopicUpdateRequest request) {
        topicService.updateTopic(id, request);
        return Result.success();
    }

    @Operation(summary = "删除话题")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTopic(@PathVariable Long id) {
        topicService.deleteTopic(id);
        return Result.success();
    }

    @Operation(summary = "触发AI分析")
    @PostMapping("/{id}/analyze")
    public Result<Void> analyzeTopic(@PathVariable Long id) {
        topicService.analyzeTopic(id);
        return Result.success();
    }

    @Operation(summary = "更新话题状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody String status) {
        // 去掉可能的引号
        String statusValue = status;
        if (status.startsWith("\"") && status.endsWith("\"")) {
            statusValue = status.substring(1, status.length() - 1);
        }
        topicService.updateStatusWithString(id, statusValue);
        return Result.success();
    }

    @Operation(summary = "关联文章")
    @PostMapping("/{id}/link")
    public Result<Void> linkArticle(@PathVariable Long id, @RequestParam Long articleId) {
        topicService.linkArticle(id, articleId);
        return Result.success();
    }
}
