package com.blog.service;

import com.blog.domain.dto.LoginDTO;
import com.blog.domain.dto.RegisterDTO;
import com.blog.domain.vo.LoginVO;
import com.blog.domain.vo.UserVO;

public interface AuthService {

    LoginVO login(LoginDTO dto);

    void register(RegisterDTO dto);

    void logout();

    UserVO getCurrentUser();

    void updateCurrentUser(UserVO userVO);

    void updatePassword(String oldPassword, String newPassword);
}
