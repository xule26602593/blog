package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.Notification;
import com.blog.domain.vo.NotificationVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    @Select(
            """
        SELECT n.id, n.type AS typeCode, n.title, n.content, n.related_id, n.sender_id,
               u.nickname AS senderName, u.avatar AS senderAvatar, n.is_read, n.create_time
        FROM notification n
        LEFT JOIN sys_user u ON n.sender_id = u.id
        WHERE n.user_id = #{userId}
        ORDER BY n.create_time DESC
        """)
    List<NotificationVO> selectNotificationList(@Param("userId") Long userId);

    @Select(
            """
        SELECT n.id, n.type AS typeCode, n.title, n.content, n.related_id, n.sender_id,
               u.nickname AS senderName, u.avatar AS senderAvatar, n.is_read, n.create_time
        FROM notification n
        LEFT JOIN sys_user u ON n.sender_id = u.id
        WHERE n.user_id = #{userId} AND n.type = #{type}
        ORDER BY n.create_time DESC
        """)
    List<NotificationVO> selectNotificationListByType(@Param("userId") Long userId, @Param("type") Integer type);

    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND is_read = 0")
    Integer countUnread(@Param("userId") Long userId);

    @Update("UPDATE notification SET is_read = 1 WHERE id = #{id} AND user_id = #{userId}")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);

    @Update("UPDATE notification SET is_read = 1 WHERE user_id = #{userId} AND is_read = 0")
    int markAllAsRead(@Param("userId") Long userId);
}
