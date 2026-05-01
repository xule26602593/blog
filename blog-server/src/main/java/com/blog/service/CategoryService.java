package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.domain.dto.CategoryDTO;
import com.blog.domain.vo.CategoryVO;
import java.util.List;

public interface CategoryService {

    List<CategoryVO> listAll();

    Page<CategoryVO> pageCategory(int pageNum, int pageSize);

    CategoryVO getById(Long id);

    void saveOrUpdate(CategoryDTO dto);

    void delete(Long id);
}
