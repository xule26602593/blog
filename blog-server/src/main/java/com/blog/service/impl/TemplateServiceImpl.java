package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.exception.BusinessException;
import com.blog.common.result.ErrorCode;
import com.blog.common.utils.BeanCopyUtils;
import com.blog.domain.dto.TemplateDTO;
import com.blog.domain.entity.ArticleTemplate;
import com.blog.domain.entity.Category;
import com.blog.domain.vo.TemplateVO;
import com.blog.repository.mapper.ArticleTemplateMapper;
import com.blog.repository.mapper.CategoryMapper;
import com.blog.service.TemplateService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

    private final ArticleTemplateMapper templateMapper;
    private final CategoryMapper categoryMapper;

    @Override
    public Page<TemplateVO> pageList(String name, int pageNum, int pageSize) {
        Page<ArticleTemplate> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ArticleTemplate> wrapper = new LambdaQueryWrapper<>();

        if (name != null && !name.isEmpty()) {
            wrapper.like(ArticleTemplate::getName, name);
        }
        wrapper.orderByDesc(ArticleTemplate::getIsDefault).orderByDesc(ArticleTemplate::getCreateTime);

        Page<ArticleTemplate> result = templateMapper.selectPage(page, wrapper);

        Page<TemplateVO> voPage = new Page<>(pageNum, pageSize, result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::convertToVO).collect(Collectors.toList()));

        return voPage;
    }

    @Override
    public List<TemplateVO> listAll() {
        return templateMapper
                .selectList(new LambdaQueryWrapper<ArticleTemplate>()
                        .eq(ArticleTemplate::getStatus, 1)
                        .orderByDesc(ArticleTemplate::getIsDefault)
                        .orderByDesc(ArticleTemplate::getCreateTime))
                .stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public TemplateVO getById(Long id) {
        ArticleTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "模板不存在");
        }
        return convertToVO(template);
    }

    @Override
    @Transactional
    public void add(TemplateDTO dto) {
        ArticleTemplate template = BeanCopyUtils.copy(dto, ArticleTemplate.class);
        templateMapper.insert(template);
    }

    @Override
    @Transactional
    public void update(Long id, TemplateDTO dto) {
        ArticleTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "模板不存在");
        }

        template.setName(dto.getName());
        template.setDescription(dto.getDescription());
        template.setContent(dto.getContent());
        template.setCategoryId(dto.getCategoryId());
        template.setTags(dto.getTags());
        template.setIsDefault(dto.getIsDefault());
        template.setStatus(dto.getStatus());

        templateMapper.updateById(template);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        templateMapper.deleteById(id);
    }

    private TemplateVO convertToVO(ArticleTemplate template) {
        TemplateVO vo = BeanCopyUtils.copy(template, TemplateVO.class);

        if (template.getCategoryId() != null) {
            Category category = categoryMapper.selectById(template.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }

        return vo;
    }
}
