package com.blog.controller.portal;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.Result;
import com.blog.common.utils.IpUtils;
import com.blog.domain.dto.SearchDTO;
import com.blog.domain.vo.SearchVO;
import com.blog.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "搜索接口")
@RestController
@RequestMapping("/api/portal/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "全文检索文章")
    @GetMapping
    public Result<Page<SearchVO>> search(SearchDTO dto, HttpServletRequest request) {
        String ipAddress = IpUtils.getIpAddress(request);
        return Result.success(searchService.search(dto, ipAddress));
    }

    @Operation(summary = "获取搜索建议")
    @GetMapping("/suggestions")
    public Result<List<String>> getSuggestions(@RequestParam(required = false) String prefix) {
        return Result.success(searchService.getSuggestions(prefix));
    }

    @Operation(summary = "获取搜索历史")
    @GetMapping("/history")
    public Result<List<String>> getHistory() {
        return Result.success(searchService.getHistory());
    }

    @Operation(summary = "获取热门搜索词")
    @GetMapping("/hot")
    public Result<List<String>> getHotKeywords() {
        return Result.success(searchService.getHotKeywords());
    }
}
