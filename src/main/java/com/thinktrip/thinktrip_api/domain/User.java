package com.thinktrip.thinktrip_api.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(length = 100)
    private String email; // 이메일이 PK

    private String password; // 소셜 로그인은 null일 수도 있음

    private String nickname;

    private String role; // USER / ADMIN

    @Column(name = "provider") // 로그인 방식 (local, kakao, google 등)
    private String provider;

    @Column(name = "provider_id") // 소셜에서 제공하는 유저 고유 ID
    private String providerId;

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

