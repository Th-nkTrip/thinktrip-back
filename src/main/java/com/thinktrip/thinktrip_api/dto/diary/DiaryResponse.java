package com.thinktrip.thinktrip_api.dto.diary;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
public class DiaryResponse {
    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private String title;
    private String content;
    private Long travelPlanId;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
