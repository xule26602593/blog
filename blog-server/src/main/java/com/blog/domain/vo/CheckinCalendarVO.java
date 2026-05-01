package com.blog.domain.vo;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckinCalendarVO {
    private LocalDate date;
    private Boolean checked;
    private Integer points;
}
