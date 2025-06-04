package com.thinktrip.thinktrip_api.dto.diary;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DiaryRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private String title;
    private String content;
}
