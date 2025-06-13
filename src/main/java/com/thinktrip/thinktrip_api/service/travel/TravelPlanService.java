package com.thinktrip.thinktrip_api.service.travel;

import com.thinktrip.thinktrip_api.domain.travel.TravelPlan;
import com.thinktrip.thinktrip_api.domain.travel.TravelPlanRepository;
import com.thinktrip.thinktrip_api.domain.user.User;
import com.thinktrip.thinktrip_api.domain.user.UserRepository;
import com.thinktrip.thinktrip_api.dto.travel.TravelPlanRequest;
import com.thinktrip.thinktrip_api.dto.travel.TravelPlanResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TravelPlanService {

    private final TravelPlanRepository travelPlanRepository;
    private final UserRepository userRepository;

    public void savePlan(TravelPlanRequest request, String email, boolean isGenerated) {
        User user = getUserByEmail(email);

        if (request.getStartDate() == null) {
            throw new IllegalArgumentException("시작일은 필수입니다.");
        }
        if (request.getEndDate() == null) {
            throw new IllegalArgumentException("마지막일은 필수입니다.");
        }

        TravelPlan plan = new TravelPlan();
        plan.setUser(user);
        plan.setStartDate(request.getStartDate());
        plan.setEndDate(request.getEndDate());
        plan.setTitle(request.getTitle());
        plan.setContent(request.getContent());
        plan.setGenerated(isGenerated);

        travelPlanRepository.save(plan);
    }

    @Transactional
    public void updatePlan(Long planId, TravelPlanRequest request, String email) {
        TravelPlan plan = getOwnedPlan(planId, email);
        plan.setStartDate(request.getStartDate());
        plan.setEndDate(request.getEndDate());
        plan.setTitle(request.getTitle());
        plan.setContent(request.getContent());
    }

    public void deletePlan(Long planId, String email) {
        TravelPlan plan = getOwnedPlan(planId, email);
        travelPlanRepository.delete(plan);
    }

    public TravelPlanResponse getPlanById(Long id, String email) {
        return toDto(getOwnedPlan(id, email));
    }

    public List<TravelPlanResponse> getPlansByUser(String email, boolean isGenerated) {
        Long userId = userRepository.findIdByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        return travelPlanRepository.findAllByUserIdAndIsGenerated(userId, isGenerated)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public String getDdayForUser(String email) {
        Long userId = userRepository.findIdByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        return travelPlanRepository.findFirstByUserIdOrderByStartDateAsc(userId)
                .map(plan -> {
                    LocalDate today = LocalDate.now();
                    LocalDate start = plan.getStartDate();
                    if (start == null) return "시작일 미정";

                    long days = ChronoUnit.DAYS.between(today, start);
                    if (days > 0) return "D-" + days;
                    else if (days == 0) return "D-Day";
                    else return "D+" + Math.abs(days);
                })
                .orElse("계획 없음");
    }


    // ==========================
    // 🔒 내부 유틸 메서드
    // ==========================

    private TravelPlan getOwnedPlan(Long planId, String email) {
        TravelPlan plan = travelPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("여행 계획을 찾을 수 없습니다."));

        if (!plan.getUser().getEmail().equals(email)) {
            throw new IllegalArgumentException("해당 여행 계획에 대한 권한이 없습니다.");
        }

        return plan;
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
    }

    private TravelPlanResponse toDto(TravelPlan plan) {
        return TravelPlanResponse.builder()
                .id(plan.getId())
                .userId(plan.getUser().getId())
                .startDate(plan.getStartDate())
                .endDate(plan.getEndDate())
                .title(plan.getTitle())
                .content(plan.getContent())
                .isGenerated(plan.isGenerated())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }
}
