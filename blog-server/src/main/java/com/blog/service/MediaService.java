package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.domain.vo.MediaVO;
import org.springframework.web.multipart.MultipartFile;

public interface MediaService {

    MediaVO upload(MultipartFile file);

    Page<MediaVO> pageList(String fileType, int pageNum, int pageSize);

    void delete(Long id);

    MediaVO getById(Long id);
}
