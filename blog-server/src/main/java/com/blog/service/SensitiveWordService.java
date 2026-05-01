package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.domain.dto.SensitiveWordDTO;
import com.blog.domain.vo.SensitiveWordVO;
import java.util.List;

public interface SensitiveWordService {

    Page<SensitiveWordVO> pageList(String word, String category, int pageNum, int pageSize);

    void add(SensitiveWordDTO dto);

    void batchAdd(List<SensitiveWordDTO> list);

    void update(Long id, SensitiveWordDTO dto);

    void delete(Long id);

    String filter(String text);

    boolean contains(String text);

    void refreshCache();
}
