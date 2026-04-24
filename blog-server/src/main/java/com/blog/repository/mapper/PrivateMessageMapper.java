package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.PrivateMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PrivateMessageMapper extends BaseMapper<PrivateMessage> {

    @Update("UPDATE private_message SET is_read = 1 WHERE conversation_id = #{conversationId} AND receiver_id = #{receiverId} AND is_read = 0")
    int markAsRead(@Param("conversationId") Long conversationId, @Param("receiverId") Long receiverId);
}
