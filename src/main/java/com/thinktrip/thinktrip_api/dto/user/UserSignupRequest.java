package com.thinktrip.thinktrip_api.dto.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserSignupRequest {

    private String email;
    private String password;
    private String name;
    private String nickname;
    private String address;       // 거주지 (카카오 주소 API 활용)
    private String travelStyle;   // 사용자 여행 선호도

}
