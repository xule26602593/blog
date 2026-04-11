package com.blog.service.impl;

import com.blog.common.exception.BusinessException;
import com.blog.common.result.ErrorCode;
import com.blog.service.FileUploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Value("${file.upload-path}")
    private String uploadPath;

    @Value("${file.allowed-types}")
    private String allowedTypes;

    @Value("${file.max-size}")
    private Long maxSize;

    @Override
    public String uploadImage(MultipartFile file) {
        // 校验文件
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
        }

        // 校验文件大小
        if (file.getSize() > maxSize) {
            throw new BusinessException(ErrorCode.FILE_SIZE_ERROR);
        }

        // 校验文件类型
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase() 
                : "";
        
        List<String> allowedList = Arrays.asList(allowedTypes.split(","));
        if (!allowedList.contains(suffix)) {
            throw new BusinessException(ErrorCode.FILE_TYPE_ERROR);
        }

        // 生成文件名
        String newFilename = UUID.randomUUID().toString().replace("-", "") + "." + suffix;
        
        // 按日期分目录存储
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String dirPath = uploadPath + datePath + "/";
        
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 保存文件
        File destFile = new File(dirPath + newFilename);
        try {
            file.transferTo(destFile);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
        }

        // 返回访问URL
        return "/uploads/" + datePath + "/" + newFilename;
    }
}
