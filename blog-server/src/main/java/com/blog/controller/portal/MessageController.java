package com.blog.controller.portal;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.Result;
import com.blog.domain.dto.MessageDTO;
import com.blog.domain.vo.MessageVO;
import com.blog.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "前台留言接口")
@RestController
@RequestMapping("/api/portal/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @Operation(summary = "分页查询留言列表")
    @GetMapping
    public Result<Page<MessageVO>> pageList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(messageService.pagePublicList(pageNum, pageSize));
    }

    @Operation(summary = "提交留言")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody MessageDTO dto) {
        messageService.add(dto);
        return Result.success();
    }
}
