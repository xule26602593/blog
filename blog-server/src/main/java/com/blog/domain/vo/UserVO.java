package com.blog.domain.vo;

import lombok.Data;

@Data
public class UserVO {
    
    private Long id;
    
    private String username;
    
    private String nickname;
    
    private String email;
    
    private String avatar;
    
    private String roleCode;
    
    private String createTime;
}
