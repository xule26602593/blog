package com.blog.domain.dto;

import lombok.Data;

@Data
public class UserDTO {

    private Long id;

    private String username;

    private String nickname;

    private String email;

    private String avatar;

    private Integer status;

    private Long roleId;

    private String roleCode;
}
