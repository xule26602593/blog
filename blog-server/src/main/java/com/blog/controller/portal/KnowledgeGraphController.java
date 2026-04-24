package com.blog.controller.portal;

import com.blog.common.result.Result;
import com.blog.domain.vo.KnowledgeGraphVO;
import com.blog.service.KnowledgeGraphService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "知识图谱接口")
@RestController
@RequestMapping("/api/portal/knowledge-graph")
@RequiredArgsConstructor
public class KnowledgeGraphController {

    private final KnowledgeGraphService knowledgeGraphService;

    @Operation(summary = "获取知识图谱数据")
    @GetMapping
    public Result<KnowledgeGraphVO> getGraph() {
        return Result.success(knowledgeGraphService.getGraph());
    }
}
