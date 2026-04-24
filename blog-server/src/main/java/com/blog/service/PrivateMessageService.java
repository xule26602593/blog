package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.domain.dto.PrivateMessageDTO;
import com.blog.domain.vo.ConversationVO;
import com.blog.domain.vo.PrivateMessageVO;

public interface PrivateMessageService {

    Page<ConversationVO> getConversations(int pageNum, int pageSize);

    Page<PrivateMessageVO> getMessages(Long conversationId, int pageNum, int pageSize);

    void send(PrivateMessageDTO dto);

    void markAsRead(Long conversationId);

    Long getUnreadCount();
}
