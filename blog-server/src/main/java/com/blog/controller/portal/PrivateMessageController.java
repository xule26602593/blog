package com.blog.controller.portal;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.Result;
import com.blog.domain.dto.PrivateMessageDTO;
import com.blog.domain.vo.ConversationVO;
import com.blog.domain.vo.PrivateMessageVO;
import com.blog.service.PrivateMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "私信接口")
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class PrivateMessageController {

    private final PrivateMessageService privateMessageService;

    @Operation(summary = "获取会话列表")
    @GetMapping("/conversations")
    public Result<Page<ConversationVO>> getConversations(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(privateMessageService.getConversations(pageNum, pageSize));
    }

    @Operation(summary = "获取会话消息")
    @GetMapping("/{conversationId}")
    public Result<Page<PrivateMessageVO>> getMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(privateMessageService.getMessages(conversationId, pageNum, pageSize));
    }

    @Operation(summary = "发送私信")
    @PostMapping
    public Result<Void> send(@Valid @RequestBody PrivateMessageDTO dto) {
        privateMessageService.send(dto);
        return Result.success();
    }

    @Operation(summary = "标记消息已读")
    @PutMapping("/{conversationId}/read")
    public Result<Void> markAsRead(@PathVariable Long conversationId) {
        privateMessageService.markAsRead(conversationId);
        return Result.success();
    }

    @Operation(summary = "获取未读消息数")
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        return Result.success(privateMessageService.getUnreadCount());
    }
}
