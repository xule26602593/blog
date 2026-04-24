package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.domain.dto.TemplateDTO;
import com.blog.domain.vo.TemplateVO;

import java.util.List;

public interface TemplateService {

    Page<TemplateVO> pageList(String name, int pageNum, int pageSize);

    List<TemplateVO> listAll();

    TemplateVO getById(Long id);

    void add(TemplateDTO dto);

    void update(Long id, TemplateDTO dto);

    void delete(Long id);
}
