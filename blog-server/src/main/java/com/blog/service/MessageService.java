package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.domain.dto.MessageDTO;
import com.blog.domain.vo.MessageVO;

public interface MessageService {

    /**
     * 分页查询公开留言列表
     */
    Page<MessageVO> pagePublicList(int pageNum, int pageSize);

    /**
     * 分页查询管理端留言列表
     */
    Page<MessageVO> pageAdminList(int status, int pageNum, int pageSize);

    /**
     * 添加留言
     */
    void add(MessageDTO dto);

    /**
     * 审核留言
     */
    void audit(Long id, Integer status);

    /**
     * 删除留言
     */
    void delete(Long id);
}
