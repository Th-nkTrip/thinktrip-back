package com.thinktrip.thinktrip_api.domain.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(length = 100)
    private String email; // 이메일이 PK

    @Column(nullable = false)
    private String password; // 비밀번호 (소셜 로그인은 null 가능)

    @Column(nullable = false)
    private String name;

    private String nickname;

    private String address; // 거주지 (카카오 주소 API 활용)

    private String travelStyle; // 여행 선호도

    private String role; // USER / ADMIN

    @Column(name = "provider")
    private String provider; // 로그인 방식 (local, kakao 등)

    @Column(name = "provider_id")
    private String providerId; // 소셜에서 제공하는 유저 고유 ID

    private boolean isPremium = false; // 프리미엄 구독 여부

    private int gptCallCount = 0; // GPT 호출 횟수

    private LocalDate lastGptCallDate; // 하루 호출 제한 체크용

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
