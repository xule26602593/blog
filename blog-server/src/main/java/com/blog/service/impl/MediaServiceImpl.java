package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.exception.BusinessException;
import com.blog.common.result.ErrorCode;
import com.blog.common.utils.BeanCopyUtils;
import com.blog.domain.entity.Media;
import com.blog.domain.vo.MediaVO;
import com.blog.repository.mapper.MediaMapper;
import com.blog.security.LoginUser;
import com.blog.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final MediaMapper mediaMapper;

    @Value("${file.upload-path:./uploads}")
    private String uploadPath;

    @Value("${file.base-url:http://localhost:8080/uploads}")
    private String baseUrl;

    @Override
    @Transactional
    public MediaVO upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String filename = UUID.randomUUID().toString() + extension;

        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String relativePath = datePath + "/" + filename;
        String fullPath = uploadPath + "/" + relativePath;

        File dest = new File(fullPath);
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }

        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "文件上传失败");
        }

        Media media = new Media();
        media.setFilename(filename);
        media.setOriginalName(originalFilename);
        media.setFilePath(fullPath);
        media.setFileUrl(baseUrl + "/" + relativePath);
        media.setFileSize(file.getSize());
        media.setFileType(file.getContentType());
        media.setUploaderId(getCurrentUserId());
        media.setUseCount(0);

        mediaMapper.insert(media);

        return BeanCopyUtils.copy(media, MediaVO.class);
    }

    @Override
    public Page<MediaVO> pageList(String fileType, int pageNum, int pageSize) {
        Page<Media> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Media> wrapper = new LambdaQueryWrapper<>();

        if (fileType != null && !fileType.isEmpty()) {
            wrapper.like(Media::getFileType, fileType);
        }
        wrapper.orderByDesc(Media::getCreateTime);

        Page<Media> result = mediaMapper.selectPage(page, wrapper);

        Page<MediaVO> voPage = new Page<>(pageNum, pageSize, result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(m -> BeanCopyUtils.copy(m, MediaVO.class))
                .collect(Collectors.toList()));

        return voPage;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Media media = mediaMapper.selectById(id);
        if (media != null) {
            File file = new File(media.getFilePath());
            if (file.exists()) {
                file.delete();
            }
            mediaMapper.deleteById(id);
        }
    }

    @Override
    public MediaVO getById(Long id) {
        Media media = mediaMapper.selectById(id);
        return media != null ? BeanCopyUtils.copy(media, MediaVO.class) : null;
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof LoginUser) {
            return ((LoginUser) principal).getUserId();
        }
        return null;
    }
}
