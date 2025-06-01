package com.thinktrip.thinktrip_api.dto.travel;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TravelPlanRequest {

    private LocalDate date;

    private String title;

    private String content;
}
