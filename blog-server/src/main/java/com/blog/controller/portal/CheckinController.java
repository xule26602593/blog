package com.blog.controller.portal;

import com.blog.common.result.Result;
import com.blog.domain.dto.CheckinResultDTO;
import com.blog.domain.vo.CheckinCalendarVO;
import com.blog.domain.vo.CheckinStatusVO;
import com.blog.security.LoginUser;
import com.blog.service.CheckinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "签到接口", description = "用户签到相关接口")
@RestController
@RequestMapping("/api/portal/checkin")
@RequiredArgsConstructor
public class CheckinController {

    private final CheckinService checkinService;

    @Operation(summary = "签到")
    @PostMapping
    public Result<CheckinResultDTO> checkin(@AuthenticationPrincipal LoginUser loginUser) {
        CheckinResultDTO result = checkinService.checkin(loginUser.getUserId());
        return Result.success(result);
    }

    @Operation(summary = "获取签到状态")
    @GetMapping("/status")
    public Result<CheckinStatusVO> getStatus(@AuthenticationPrincipal LoginUser loginUser) {
        CheckinStatusVO status = checkinService.getCheckinStatus(loginUser.getUserId());
        return Result.success(status);
    }

    @Operation(summary = "获取签到日历")
    @GetMapping("/calendar")
    public Result<List<CheckinCalendarVO>> getCalendar(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam(required = false, defaultValue = "") String month) {
        if (month.isEmpty()) {
            month = LocalDate.now().toString().substring(0, 7);
        }
        List<CheckinCalendarVO> calendar = checkinService.getCheckinCalendar(loginUser.getUserId(), month);
        return Result.success(calendar);
    }
}
