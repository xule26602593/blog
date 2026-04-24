package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.exception.BusinessException;
import com.blog.common.result.ErrorCode;
import com.blog.common.utils.BeanCopyUtils;
import com.blog.domain.dto.PrivateMessageDTO;
import com.blog.domain.entity.Conversation;
import com.blog.domain.entity.PrivateMessage;
import com.blog.domain.entity.User;
import com.blog.domain.vo.ConversationVO;
import com.blog.domain.vo.PrivateMessageVO;
import com.blog.repository.mapper.ConversationMapper;
import com.blog.repository.mapper.PrivateMessageMapper;
import com.blog.repository.mapper.UserMapper;
import com.blog.security.LoginUser;
import com.blog.service.PrivateMessageService;
import com.blog.service.SensitiveWordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrivateMessageServiceImpl implements PrivateMessageService {

    private final ConversationMapper conversationMapper;
    private final PrivateMessageMapper privateMessageMapper;
    private final UserMapper userMapper;
    private final SensitiveWordService sensitiveWordService;

    @Override
    public Page<ConversationVO> getConversations(int pageNum, int pageSize) {
        Long userId = getCurrentUserId();

        Page<Conversation> page = new Page<>(pageNum, pageSize);
        Page<Conversation> result = conversationMapper.selectPage(page,
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getUser1Id, userId)
                        .or()
                        .eq(Conversation::getUser2Id, userId)
                        .orderByDesc(Conversation::getLastMessageTime));

        Page<ConversationVO> voPage = new Page<>(pageNum, pageSize, result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(c -> convertToConversationVO(c, userId))
                .collect(Collectors.toList()));

        return voPage;
    }

    @Override
    public Page<PrivateMessageVO> getMessages(Long conversationId, int pageNum, int pageSize) {
        Long userId = getCurrentUserId();

        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "会话不存在");
        }

        if (!conversation.getUser1Id().equals(userId) && !conversation.getUser2Id().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该会话");
        }

        Page<PrivateMessage> page = new Page<>(pageNum, pageSize);
        Page<PrivateMessage> result = privateMessageMapper.selectPage(page,
                new LambdaQueryWrapper<PrivateMessage>()
                        .eq(PrivateMessage::getConversationId, conversationId)
                        .orderByAsc(PrivateMessage::getCreateTime));

        Page<PrivateMessageVO> voPage = new Page<>(pageNum, pageSize, result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(this::convertToPrivateMessageVO)
                .collect(Collectors.toList()));

        return voPage;
    }

    @Override
    @Transactional
    public void send(PrivateMessageDTO dto) {
        Long senderId = getCurrentUserId();
        Long receiverId = dto.getReceiverId();

        if (senderId.equals(receiverId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能给自己发私信");
        }

        User receiver = userMapper.selectById(receiverId);
        if (receiver == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接收者不存在");
        }

        Long user1Id = Math.min(senderId, receiverId);
        Long user2Id = Math.max(senderId, receiverId);

        Conversation conversation = conversationMapper.selectOne(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getUser1Id, user1Id)
                        .eq(Conversation::getUser2Id, user2Id));

        if (conversation == null) {
            conversation = new Conversation();
            conversation.setUser1Id(user1Id);
            conversation.setUser2Id(user2Id);
            conversationMapper.insert(conversation);
        }

        String filteredContent = sensitiveWordService.filter(dto.getContent());

        PrivateMessage message = new PrivateMessage();
        message.setConversationId(conversation.getId());
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(filteredContent);
        message.setMessageType(dto.getMessageType());
        message.setIsRead(0);

        privateMessageMapper.insert(message);

        conversation.setLastMessageId(message.getId());
        conversation.setLastMessageTime(LocalDateTime.now());
        conversationMapper.updateById(conversation);
    }

    @Override
    @Transactional
    public void markAsRead(Long conversationId) {
        Long userId = getCurrentUserId();
        privateMessageMapper.markAsRead(conversationId, userId);
    }

    @Override
    public Long getUnreadCount() {
        Long userId = getCurrentUserId();
        return privateMessageMapper.selectCount(
                new LambdaQueryWrapper<PrivateMessage>()
                        .eq(PrivateMessage::getReceiverId, userId)
                        .eq(PrivateMessage::getIsRead, 0));
    }

    private ConversationVO convertToConversationVO(Conversation conversation, Long currentUserId) {
        ConversationVO vo = new ConversationVO();
        vo.setId(conversation.getId());

        Long peerId = conversation.getUser1Id().equals(currentUserId)
                ? conversation.getUser2Id()
                : conversation.getUser1Id();
        vo.setPeerId(peerId);

        User peer = userMapper.selectById(peerId);
        if (peer != null) {
            vo.setPeerNickname(peer.getNickname());
            vo.setPeerAvatar(peer.getAvatar());
        }

        if (conversation.getLastMessageId() != null) {
            PrivateMessage lastMessage = privateMessageMapper.selectById(conversation.getLastMessageId());
            if (lastMessage != null) {
                String content = lastMessage.getContent();
                vo.setLastMessage(content.length() > 50 ? content.substring(0, 50) + "..." : content);
            }
        }

        vo.setLastMessageTime(conversation.getLastMessageTime());
        vo.setCreateTime(conversation.getCreateTime());

        Long unreadCount = privateMessageMapper.selectCount(
                new LambdaQueryWrapper<PrivateMessage>()
                        .eq(PrivateMessage::getConversationId, conversation.getId())
                        .eq(PrivateMessage::getReceiverId, currentUserId)
                        .eq(PrivateMessage::getIsRead, 0));
        vo.setUnreadCount(unreadCount.intValue());

        return vo;
    }

    private PrivateMessageVO convertToPrivateMessageVO(PrivateMessage message) {
        PrivateMessageVO vo = BeanCopyUtils.copy(message, PrivateMessageVO.class);

        User sender = userMapper.selectById(message.getSenderId());
        if (sender != null) {
            vo.setSenderNickname(sender.getNickname());
            vo.setSenderAvatar(sender.getAvatar());
        }

        return vo;
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof LoginUser) {
            return ((LoginUser) principal).getUserId();
        }
        throw new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录");
    }
}
