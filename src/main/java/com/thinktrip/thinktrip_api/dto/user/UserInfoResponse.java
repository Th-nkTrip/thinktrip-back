package com.thinktrip.thinktrip_api.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserInfoResponse {
    private String email;
    private String name;
    private String nickname;
    private String address;
    private String travelStyle;
    private String role;
    private String provider;
    private boolean isPremium;
    private int gptCallCount;
    private LocalDate lastGptCallDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
