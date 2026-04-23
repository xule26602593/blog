package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.exception.BusinessException;
import com.blog.common.result.ErrorCode;
import com.blog.common.utils.BeanCopyUtils;
import com.blog.domain.dto.TagDTO;
import com.blog.domain.entity.ArticleTag;
import com.blog.domain.entity.Tag;
import com.blog.domain.vo.TagVO;
import com.blog.repository.mapper.ArticleTagMapper;
import com.blog.repository.mapper.TagMapper;
import com.blog.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;

    @Override
    @Cacheable(value = "tag", key = "'list'")
    public List<TagVO> listAll() {
        List<Tag> tags = tagMapper.selectList(null);
        return tags.stream()
                .map(tag -> BeanCopyUtils.copy(tag, TagVO.class))
                .collect(Collectors.toList());
    }

    @Override
    public Page<TagVO> pageTag(int pageNum, int pageSize) {
        Page<Tag> page = new Page<>(pageNum, pageSize);
        Page<Tag> tagPage = tagMapper.selectPage(page, null);

        Page<TagVO> voPage = new Page<>(pageNum, pageSize, tagPage.getTotal());
        voPage.setRecords(tagPage.getRecords().stream()
                .map(tag -> BeanCopyUtils.copy(tag, TagVO.class))
                .collect(Collectors.toList()));

        return voPage;
    }

    @Override
    @Transactional
    @CacheEvict(value = "tag", key = "'list'")
    public void saveOrUpdate(TagDTO dto) {
        Tag tag;
        if (dto.getId() == null) {
            tag = new Tag();
            // 检查标签名是否已存在
            Long count = tagMapper.selectCount(new LambdaQueryWrapper<Tag>()
                    .eq(Tag::getName, dto.getName()));
            if (count > 0) {
                throw new BusinessException("标签名称已存在");
            }
        } else {
            tag = tagMapper.selectById(dto.getId());
            if (tag == null) {
                throw new BusinessException(ErrorCode.TAG_NOT_FOUND);
            }
        }

        tag.setName(dto.getName());
        tag.setColor(dto.getColor() != null ? dto.getColor() : "#409EFF");

        if (dto.getId() == null) {
            tagMapper.insert(tag);
        } else {
            tagMapper.updateById(tag);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "tag", key = "'list'")
    public void delete(Long id) {
        // 删除标签与文章的关联
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>()
                .eq(ArticleTag::getTagId, id));
        tagMapper.deleteById(id);
    }
}
