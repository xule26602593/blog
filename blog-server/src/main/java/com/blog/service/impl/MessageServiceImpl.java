package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.exception.BusinessException;
import com.blog.common.result.ErrorCode;
import com.blog.common.utils.BeanCopyUtils;
import com.blog.domain.dto.MessageDTO;
import com.blog.domain.entity.Message;
import com.blog.domain.entity.User;
import com.blog.domain.vo.MessageVO;
import com.blog.repository.mapper.MessageMapper;
import com.blog.repository.mapper.UserMapper;
import com.blog.security.LoginUser;
import com.blog.service.MessageService;
import com.blog.service.SensitiveWordService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;
    private final UserMapper userMapper;
    private final SensitiveWordService sensitiveWordService;
    private final HttpServletRequest request;

    @Override
    public Page<MessageVO> pagePublicList(int pageNum, int pageSize) {
        Page<Message> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getStatus, 1) // 只查询已通过的留言
                .orderByDesc(Message::getCreateTime);

        Page<Message> result = messageMapper.selectPage(page, wrapper);

        return convertToVOPage(result, pageNum, pageSize);
    }

    @Override
    public Page<MessageVO> pageAdminList(int status, int pageNum, int pageSize) {
        Page<Message> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        if (status != -1) {
            wrapper.eq(Message::getStatus, status);
        }
        wrapper.orderByDesc(Message::getCreateTime);

        Page<Message> result = messageMapper.selectPage(page, wrapper);

        return convertToVOPage(result, pageNum, pageSize);
    }

    @Override
    public void add(MessageDTO dto) {
        // 敏感词过滤
        String filteredContent = sensitiveWordService.filter(dto.getContent());

        Message message = new Message();
        message.setContent(filteredContent);

        Long currentUserId = getCurrentUserId();
        if (currentUserId != null) {
            // 登录用户
            message.setUserId(currentUserId);
        } else {
            // 游客
            message.setNickname(dto.getNickname());
            message.setEmail(dto.getEmail());
        }

        // 获取IP地址
        String ipAddress = getClientIpAddress();
        message.setIpAddress(ipAddress);

        // 默认状态为待审核
        message.setStatus(0);

        messageMapper.insert(message);
    }

    @Override
    public void audit(Long id, Integer status) {
        Message message = messageMapper.selectById(id);
        if (message == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "留言不存在");
        }

        message.setStatus(status);
        messageMapper.updateById(message);
    }

    @Override
    public void delete(Long id) {
        Message message = messageMapper.selectById(id);
        if (message == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "留言不存在");
        }

        messageMapper.deleteById(id);
    }

    private Page<MessageVO> convertToVOPage(Page<Message> result, int pageNum, int pageSize) {
        // 获取所有用户ID
        Set<Long> userIds = result.getRecords().stream()
                .map(Message::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        // 批量查询用户信息
        Map<Long, User> userMap = Map.of();
        if (!userIds.isEmpty()) {
            userMap = userMapper.selectBatchIds(userIds).stream()
                    .collect(Collectors.toMap(User::getId, Function.identity()));
        }

        // 转换为VO
        Map<Long, User> finalUserMap = userMap;
        Page<MessageVO> voPage = new Page<>(pageNum, pageSize, result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(message -> {
                    MessageVO vo = BeanCopyUtils.copy(message, MessageVO.class);
                    if (message.getUserId() != null) {
                        User user = finalUserMap.get(message.getUserId());
                        if (user != null) {
                            vo.setNickname(user.getNickname());
                            vo.setEmail(user.getEmail());
                            vo.setAvatar(user.getAvatar());
                        }
                    }
                    return vo;
                })
                .toList());

        return voPage;
    }

    private String getClientIpAddress() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 取第一个IP（如果有多个）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof LoginUser) {
                return ((LoginUser) principal).getUserId();
            }
        }
        return null;
    }
}
