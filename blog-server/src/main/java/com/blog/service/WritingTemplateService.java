package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.domain.entity.WritingTemplate;
import java.util.List;

public interface WritingTemplateService {

    Page<WritingTemplate> page(int pageNum, int pageSize, Long authorId);

    List<WritingTemplate> listAvailable(Long currentUserId);

    WritingTemplate getById(Long id);

    void create(WritingTemplate template);

    void update(WritingTemplate template);

    void delete(Long id);

    void useTemplate(Long id);
}
