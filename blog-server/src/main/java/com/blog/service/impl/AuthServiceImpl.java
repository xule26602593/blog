package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.exception.BusinessException;
import com.blog.common.result.ErrorCode;
import com.blog.common.utils.BeanCopyUtils;
import com.blog.common.utils.JwtUtils;
import com.blog.domain.dto.LoginDTO;
import com.blog.domain.dto.RegisterDTO;
import com.blog.domain.entity.User;
import com.blog.domain.vo.LoginVO;
import com.blog.domain.vo.UserVO;
import com.blog.repository.mapper.UserMapper;
import com.blog.security.LoginUser;
import com.blog.service.AuthService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        if (user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }

        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 生成Token
        String token = jwtUtils.generateToken(user.getUsername());

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setRoleCode(user.getRoleCode());

        return vo;
    }

    @Override
    @Transactional
    public void register(RegisterDTO dto) {
        // 检查用户名是否存在
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        // 检查邮箱是否存在
        if (dto.getEmail() != null) {
            count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, dto.getEmail()));
            if (count > 0) {
                throw new BusinessException(ErrorCode.EMAIL_EXISTS);
            }
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setStatus(1);
        user.setRoleId(2L);
        user.setRoleCode("visitor");

        userMapper.insert(user);
    }

    @Override
    public void logout() {
        SecurityContextHolder.clearContext();
    }

    @Override
    public UserVO getCurrentUser() {
        LoginUser loginUser = getLoginUser();
        User user = userMapper.selectById(loginUser.getUserId());
        return BeanCopyUtils.copy(user, UserVO.class);
    }

    @Override
    @Transactional
    public void updateCurrentUser(UserVO userVO) {
        LoginUser loginUser = getLoginUser();
        User user = new User();
        user.setId(loginUser.getUserId());
        user.setNickname(userVO.getNickname());
        user.setEmail(userVO.getEmail());
        user.setAvatar(userVO.getAvatar());
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void updatePassword(String oldPassword, String newPassword) {
        LoginUser loginUser = getLoginUser();
        User user = userMapper.selectById(loginUser.getUserId());

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    private LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return (LoginUser) authentication.getPrincipal();
    }
}
