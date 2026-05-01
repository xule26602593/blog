package com.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.exception.BusinessException;
import com.blog.common.result.ErrorCode;
import com.blog.common.result.Result;
import com.blog.common.utils.BeanCopyUtils;
import com.blog.domain.entity.User;
import com.blog.domain.vo.UserManageVO;
import com.blog.repository.mapper.UserMapper;
import com.blog.security.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理接口")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserManageController {

    private final UserMapper userMapper;

    @Operation(summary = "分页查询用户列表")
    @GetMapping
    public Result<Page<UserManageVO>> pageList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or()
                    .like(User::getNickname, keyword)
                    .or()
                    .like(User::getEmail, keyword));
        }
        if (roleCode != null && !roleCode.isEmpty()) {
            wrapper.eq(User::getRoleCode, roleCode);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.eq(User::getDeleted, 0);
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> result = userMapper.selectPage(page, wrapper);

        Page<UserManageVO> voPage = new Page<>(pageNum, pageSize, result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(user -> BeanCopyUtils.copy(user, UserManageVO.class))
                .toList());

        return Result.success(voPage);
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    public Result<UserManageVO> getDetail(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return Result.success(BeanCopyUtils.copy(user, UserManageVO.class));
    }

    @Operation(summary = "更新用户状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Long currentUserId = getCurrentUserId();
        if (id.equals(currentUserId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能修改自己的状态");
        }

        User user = new User();
        user.setId(id);
        user.setStatus(status);
        userMapper.updateById(user);

        return Result.success();
    }

    @Operation(summary = "修改用户角色")
    @PutMapping("/{id}/role")
    public Result<Void> updateRole(@PathVariable Long id, @RequestParam String roleCode) {
        Long currentUserId = getCurrentUserId();
        if (id.equals(currentUserId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能修改自己的角色");
        }

        User user = new User();
        user.setId(id);
        user.setRoleCode(roleCode);
        userMapper.updateById(user);

        return Result.success();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        if (id.equals(currentUserId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能删除自己");
        }

        User user = new User();
        user.setId(id);
        user.setDeleted(1);
        userMapper.updateById(user);

        return Result.success();
    }

    private Long getCurrentUserId() {
        Object principal =
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof LoginUser) {
            return ((LoginUser) principal).getUserId();
        }
        return null;
    }
}
