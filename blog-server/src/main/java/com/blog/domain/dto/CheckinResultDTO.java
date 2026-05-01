package com.blog.domain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckinResultDTO {
    private Boolean success;
    private Integer consecutiveDays;
    private Integer pointsEarned;
    private Integer basePoints;
    private Integer bonusPoints;
    private Integer specialBonus;
    private String message;
}
