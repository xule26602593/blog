package com.blog.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SensitiveWordDTO {

    @NotBlank(message = "敏感词不能为空")
    @Size(max = 50, message = "敏感词长度不能超过50")
    private String word;

    @Size(max = 50, message = "分类长度不能超过50")
    private String category;

    @Size(max = 50, message = "替换字符长度不能超过50")
    private String replaceWord;

    private Integer status = 1;
}
