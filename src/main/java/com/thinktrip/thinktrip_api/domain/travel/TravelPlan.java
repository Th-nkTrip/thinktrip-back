package com.thinktrip.thinktrip_api.domain.travel;

import com.thinktrip.thinktrip_api.domain.user.User;
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
@Table(name = "travel_plans")
public class TravelPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // 연관관계 주인: TravelPlan
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDate date;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_generated")
    private boolean isGenerated;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
