package com.blog.controller.admin;

import com.blog.common.result.Result;
import com.blog.domain.dto.FormatPreviewResult;
import com.blog.domain.dto.LinkCheckResult;
import com.blog.domain.entity.FormatRule;
import com.blog.service.FormatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/format")
@RequiredArgsConstructor
public class FormatController {

    private final FormatService formatService;

    @PostMapping("/preview")
    public Result<FormatPreviewResult> preview(@RequestBody Map<String, Object> request) {
        String content = (String) request.get("content");
        @SuppressWarnings("unchecked")
        List<String> rules = (List<String>) request.get("rules");

        FormatPreviewResult result = formatService.preview(content, rules);
        return Result.success(result);
    }

    @PostMapping("/apply")
    public Result<String> apply(@RequestBody Map<String, Object> request) {
        String content = (String) request.get("content");
        @SuppressWarnings("unchecked")
        List<String> rules = (List<String>) request.get("rules");

        String result = formatService.apply(content, rules);
        return Result.success(result);
    }

    @GetMapping("/rules")
    public Result<List<FormatRule>> getRules() {
        List<FormatRule> rules = formatService.getRules();
        return Result.success(rules);
    }

    @PutMapping("/rules/{id}")
    public Result<Void> updateRuleStatus(
        @PathVariable Long id,
        @RequestBody Map<String, Integer> request
    ) {
        formatService.updateRuleStatus(id, request.get("status"));
        return Result.success();
    }

    @PostMapping("/check-links")
    public Result<LinkCheckResult> checkLinks(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        LinkCheckResult result = formatService.checkLinks(content);
        return Result.success(result);
    }
}
