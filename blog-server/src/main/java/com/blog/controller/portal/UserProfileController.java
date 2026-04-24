package com.blog.controller.portal;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.Result;
import com.blog.common.utils.BeanCopyUtils;
import com.blog.domain.entity.User;
import com.blog.domain.entity.Comment;
import com.blog.domain.entity.UserFollow;
import com.blog.domain.vo.UserPublicVO;
import com.blog.domain.vo.CommentVO;
import com.blog.repository.mapper.UserMapper;
import com.blog.repository.mapper.CommentMapper;
import com.blog.repository.mapper.UserFollowMapper;
import com.blog.security.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户公开主页接口")
@RestController
@RequestMapping("/api/portal/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserMapper userMapper;
    private final CommentMapper commentMapper;
    private final UserFollowMapper userFollowMapper;

    @Operation(summary = "获取用户公开信息")
    @GetMapping("/{id}")
    public Result<UserPublicVO> getUserPublicInfo(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            return Result.error(404, "用户不存在");
        }

        UserPublicVO vo = BeanCopyUtils.copy(user, UserPublicVO.class);

        // 获取关注数和粉丝数
        vo.setFollowingCount(userFollowMapper.countFollowing(id));
        vo.setFollowerCount(userFollowMapper.countFollowers(id));

        // 检查当前用户是否关注了该用户
        Long currentUserId = getCurrentUserId();
        if (currentUserId != null && !currentUserId.equals(id)) {
            Long count = userFollowMapper.selectCount(
                    new LambdaQueryWrapper<UserFollow>()
                            .eq(UserFollow::getFollowerId, currentUserId)
                            .eq(UserFollow::getFollowingId, id));
            vo.setIsFollowing(count > 0);
        } else {
            vo.setIsFollowing(false);
        }

        return Result.success(vo);
    }

    @Operation(summary = "获取用户最近评论")
    @GetMapping("/{id}/comments")
    public Result<Page<CommentVO>> getUserComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "5") int pageSize) {

        Page<Comment> page = new Page<>(pageNum, pageSize);
        Page<Comment> commentPage = commentMapper.selectPage(page,
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getUserId, id)
                        .eq(Comment::getStatus, 1)
                        .orderByDesc(Comment::getCreateTime));

        Page<CommentVO> voPage = new Page<>(pageNum, pageSize, commentPage.getTotal());
        voPage.setRecords(commentPage.getRecords().stream()
                .map(comment -> {
                    CommentVO vo = BeanCopyUtils.copy(comment, CommentVO.class);
                    if (comment.getArticleId() != null) {
                        // 可选：获取文章标题
                    }
                    return vo;
                })
                .toList());

        return Result.success(voPage);
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof LoginUser) {
            return ((LoginUser) principal).getUserId();
        }
        return null;
    }
}
