package com.thinktrip.thinktrip_api.domain.travel;

import com.thinktrip.thinktrip_api.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

    private LocalDate startDate;

    private LocalDate endDate;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_generated")
    private boolean isGenerated;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt; // 수정일

}
