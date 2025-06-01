package com.thinktrip.thinktrip_api.dto.travel;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class TravelPlanResponse {
    private Long id;
    private Long userId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String title;
    private String content;
    private boolean isGenerated;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
