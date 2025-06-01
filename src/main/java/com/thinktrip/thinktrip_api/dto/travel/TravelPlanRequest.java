package com.thinktrip.thinktrip_api.dto.travel;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TravelPlanRequest {

    private LocalDate startDate;  // 여행 시작일

    private LocalDate endDate;    // 여행 종료일

    private String title;

    private String content;
}
