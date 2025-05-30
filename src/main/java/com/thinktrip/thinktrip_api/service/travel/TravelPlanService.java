package com.thinktrip.thinktrip_api.service.travel;

import com.thinktrip.thinktrip_api.domain.travel.TravelPlan;
import com.thinktrip.thinktrip_api.domain.user.User;
import com.thinktrip.thinktrip_api.dto.travel.TravelPlanRequest;
import com.thinktrip.thinktrip_api.dto.travel.TravelPlanResponse;
import com.thinktrip.thinktrip_api.domain.travel.TravelPlanRepository;
import com.thinktrip.thinktrip_api.domain.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TravelPlanService {

    private final TravelPlanRepository travelPlanRepository;
    private final UserRepository userRepository;

    public void savePlan(TravelPlanRequest request, String email, boolean isGenerated) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        TravelPlan plan = new TravelPlan();
        plan.setUser(user);
        plan.setDate(request.getDate());
        plan.setTitle(request.getTitle());
        plan.setContent(request.getContent());
        plan.setGenerated(isGenerated);

        travelPlanRepository.save(plan);
    }

    @Transactional
    public void updatePlan(Long planId, TravelPlanRequest request, String email) {
        TravelPlan plan = getPlanOwnedByUser(planId, email);
        plan.setDate(request.getDate());
        plan.setTitle(request.getTitle());
        plan.setContent(request.getContent());
    }

    public void deletePlan(Long planId, String email) {
        TravelPlan plan = getPlanOwnedByUser(planId, email);
        travelPlanRepository.delete(plan);
    }

    public TravelPlanResponse getPlanById(Long id, String email) {
        TravelPlan plan = getPlanOwnedByUser(id, email);
        return toResponseDto(plan);
    }

    public List<TravelPlanResponse> getPlansByUser(String email, boolean isGenerated) {
        Long userId = userRepository.findIdByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        return travelPlanRepository.findAllByUserIdAndIsGenerated(userId, isGenerated)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    private TravelPlan getPlanOwnedByUser(Long planId, String email) {
        TravelPlan plan = travelPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("여행 계획을 찾을 수 없습니다."));

        if (!plan.getUser().getEmail().equals(email)) {
            throw new IllegalArgumentException("해당 여행 계획을 수정/삭제할 권한이 없습니다.");
        }

        return plan;
    }

    private TravelPlanResponse toResponseDto(TravelPlan plan) {
        return TravelPlanResponse.builder()
                .id(plan.getId())
                .userId(plan.getUser().getId())
                .date(plan.getDate())
                .title(plan.getTitle())
                .content(plan.getContent())
                .isGenerated(plan.isGenerated())
                .createdAt(plan.getCreatedAt())
                .build();
    }
}
