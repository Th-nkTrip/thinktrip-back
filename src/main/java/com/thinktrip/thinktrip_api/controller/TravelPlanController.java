package com.thinktrip.thinktrip_api.controller;

import com.thinktrip.thinktrip_api.dto.travel.TravelPlanRequest;
import com.thinktrip.thinktrip_api.service.travel.TravelPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/travel-plans")
@RequiredArgsConstructor
public class TravelPlanController {

    private final TravelPlanService travelPlanService;

    // GPT가 생성한 여행 계획 저장
    @PostMapping("/gpt")
    public ResponseEntity<?> saveGeneratedPlan(@RequestBody TravelPlanRequest request,
                                               @AuthenticationPrincipal String email) {
        travelPlanService.savePlan(request, email, true);
        return ResponseEntity.ok(Map.of("message", "GPT 여행 계획 저장 완료"));
    }

    // 사용자가 직접 작성한 여행 계획 저장
    @PostMapping("/user")
    public ResponseEntity<?> saveUserPlan(@RequestBody TravelPlanRequest request,
                                          @AuthenticationPrincipal String email) {
        travelPlanService.savePlan(request, email, false);
        return ResponseEntity.ok(Map.of("message", "사용자 여행 계획 저장 완료"));
    }

    // 여행 계획 수정
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePlan(@PathVariable Long id,
                                        @RequestBody TravelPlanRequest request,
                                        @AuthenticationPrincipal String email) {
        travelPlanService.updatePlan(id, request, email);
        return ResponseEntity.ok(Map.of("message", "여행 계획 수정 완료"));
    }

    // 여행 계획 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePlan(@PathVariable Long id,
                                        @AuthenticationPrincipal String email) {
        travelPlanService.deletePlan(id, email);
        return ResponseEntity.ok(Map.of("message", "여행 계획 삭제 완료"));
    }

    // 사용자 직접 작성한 여행 계획 조회
    @GetMapping("/user")
    public ResponseEntity<?> getUserWrittenPlans(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(travelPlanService.getPlansByUser(email, false));
    }

    // GPT가 생성한 여행 계획 조회
    @GetMapping("/gpt")
    public ResponseEntity<?> getGptGeneratedPlans(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(travelPlanService.getPlansByUser(email, true));
    }

    // 단일 계획 조회
    @GetMapping("/{id}")
    public ResponseEntity<?> getPlanById(@PathVariable Long id,
                                         @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(travelPlanService.getPlanById(id, email));
    }

}
