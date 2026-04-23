package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.exception.BusinessException;
import com.blog.common.result.ErrorCode;
import com.blog.common.utils.BeanCopyUtils;
import com.blog.domain.dto.CategoryDTO;
import com.blog.domain.entity.Category;
import com.blog.domain.vo.CategoryVO;
import com.blog.repository.mapper.CategoryMapper;
import com.blog.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    @Cacheable(value = "category", key = "'list'")
    public List<CategoryVO> listAll() {
        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getStatus, 1)
                        .orderByAsc(Category::getSort));

        return categories.stream().map(category -> {
            CategoryVO vo = BeanCopyUtils.copy(category, CategoryVO.class);
            vo.setArticleCount(categoryMapper.countArticles(category.getId()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public Page<CategoryVO> pageCategory(int pageNum, int pageSize) {
        Page<Category> page = new Page<>(pageNum, pageSize);
        Page<Category> categoryPage = categoryMapper.selectPage(page,
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSort));

        Page<CategoryVO> voPage = new Page<>(pageNum, pageSize, categoryPage.getTotal());
        voPage.setRecords(categoryPage.getRecords().stream().map(category -> {
            CategoryVO vo = BeanCopyUtils.copy(category, CategoryVO.class);
            vo.setArticleCount(categoryMapper.countArticles(category.getId()));
            return vo;
        }).collect(Collectors.toList()));

        return voPage;
    }

    @Override
    public CategoryVO getById(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        CategoryVO vo = BeanCopyUtils.copy(category, CategoryVO.class);
        vo.setArticleCount(categoryMapper.countArticles(id));
        return vo;
    }

    @Override
    @Transactional
    @CacheEvict(value = "category", key = "'list'")
    public void saveOrUpdate(CategoryDTO dto) {
        Category category;
        if (dto.getId() == null) {
            category = new Category();
        } else {
            category = categoryMapper.selectById(dto.getId());
            if (category == null) {
                throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
            }
        }

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setSort(dto.getSort() != null ? dto.getSort() : 0);
        category.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);

        if (dto.getId() == null) {
            categoryMapper.insert(category);
        } else {
            categoryMapper.updateById(category);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "category", key = "'list'")
    public void delete(Long id) {
        Integer count = categoryMapper.countArticles(id);
        if (count > 0) {
            throw new BusinessException("该分类下存在文章，无法删除");
        }
        categoryMapper.deleteById(id);
    }
}
