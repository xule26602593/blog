package com.blog.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.Result;
import com.blog.domain.vo.MediaVO;
import com.blog.service.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "媒体管理接口")
@RestController
@RequestMapping("/api/admin/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @Operation(summary = "上传图片")
    @PostMapping("/upload")
    public Result<MediaVO> upload(@RequestParam("file") MultipartFile file) {
        return Result.success(mediaService.upload(file));
    }

    @Operation(summary = "获取媒体列表")
    @GetMapping
    public Result<Page<MediaVO>> pageList(
            @RequestParam(required = false) String fileType,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(mediaService.pageList(fileType, pageNum, pageSize));
    }

    @Operation(summary = "获取媒体详情")
    @GetMapping("/{id}")
    public Result<MediaVO> getById(@PathVariable Long id) {
        return Result.success(mediaService.getById(id));
    }

    @Operation(summary = "删除媒体")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        mediaService.delete(id);
        return Result.success();
    }
}
