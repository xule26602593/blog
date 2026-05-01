package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.domain.dto.CheckinResultDTO;
import com.blog.domain.entity.UserCheckin;
import com.blog.domain.vo.CheckinCalendarVO;
import com.blog.domain.vo.CheckinStatusVO;
import java.util.List;

public interface CheckinService {

    CheckinResultDTO checkin(Long userId);

    CheckinStatusVO getCheckinStatus(Long userId);

    List<CheckinCalendarVO> getCheckinCalendar(Long userId, String month);

    Page<UserCheckin> getCheckinHistory(Long userId, int page, int size);
}
