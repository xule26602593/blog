package com.blog.domain.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckinStatusVO {
    private Boolean isCheckedToday;
    private Integer consecutiveDays;
    private Integer totalDays;
    private Integer totalPoints;
    private Integer maxConsecutiveDays;
    private Integer todayPoints;
}
