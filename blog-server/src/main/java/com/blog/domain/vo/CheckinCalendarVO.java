package com.blog.domain.vo;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class CheckinCalendarVO {
    private LocalDate date;
    private Boolean checked;
    private Integer points;
}
