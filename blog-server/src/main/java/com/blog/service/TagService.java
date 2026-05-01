package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.domain.dto.TagDTO;
import com.blog.domain.entity.Tag;
import com.blog.domain.vo.TagVO;
import java.util.List;

public interface TagService {

    List<TagVO> listAll();

    Page<TagVO> pageTag(int pageNum, int pageSize);

    void saveOrUpdate(TagDTO dto);

    void delete(Long id);

    List<Tag> findByNames(List<String> names);
}
