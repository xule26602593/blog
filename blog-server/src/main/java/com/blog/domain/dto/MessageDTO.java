package com.blog.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MessageDTO {

    @NotBlank(message = "留言内容不能为空")
    @Size(max = 1000, message = "留言内容不能超过1000字")
    private String content;

    // 游客信息（登录用户无需填写）
    @Size(max = 50, message = "昵称不能超过50字")
    private String nickname;

    @Size(max = 100, message = "邮箱不能超过100字")
    private String email;
}
