package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.exception.BusinessException;
import com.blog.domain.entity.WritingTemplate;
import com.blog.repository.mapper.WritingTemplateMapper;
import com.blog.service.WritingTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WritingTemplateServiceImpl implements WritingTemplateService {

    private final WritingTemplateMapper templateMapper;

    @Override
    public Page<WritingTemplate> page(int pageNum, int pageSize, Long authorId) {
        Page<WritingTemplate> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<WritingTemplate> wrapper = new LambdaQueryWrapper<>();
        if (authorId != null) {
            wrapper.eq(WritingTemplate::getAuthorId, authorId);
        }
        wrapper.orderByDesc(WritingTemplate::getUsageCount);
        return templateMapper.selectPage(page, wrapper);
    }

    @Override
    public List<WritingTemplate> listAvailable(Long currentUserId) {
        LambdaQueryWrapper<WritingTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
            .eq(WritingTemplate::getIsBuiltin, 1)
            .or()
            .eq(WritingTemplate::getAuthorId, currentUserId)
        );
        wrapper.eq(WritingTemplate::getStatus, 1);
        wrapper.orderByDesc(WritingTemplate::getUsageCount);
        return templateMapper.selectList(wrapper);
    }

    @Override
    public WritingTemplate getById(Long id) {
        return templateMapper.selectById(id);
    }

    @Override
    public void create(WritingTemplate template) {
        template.setIsBuiltin(0);
        template.setUsageCount(0);
        template.setStatus(1);
        templateMapper.insert(template);
    }

    @Override
    public void update(WritingTemplate template) {
        WritingTemplate existing = templateMapper.selectById(template.getId());
        if (existing != null && existing.getIsBuiltin() == 1) {
            throw new BusinessException("内置模板不允许修改");
        }
        templateMapper.updateById(template);
    }

    @Override
    public void delete(Long id) {
        WritingTemplate existing = templateMapper.selectById(id);
        if (existing != null && existing.getIsBuiltin() == 1) {
            throw new BusinessException("内置模板不允许删除");
        }
        templateMapper.deleteById(id);
    }

    @Override
    public void useTemplate(Long id) {
        templateMapper.incrementUsageCount(id);
    }
}
