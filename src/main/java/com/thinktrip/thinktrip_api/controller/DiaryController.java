package com.thinktrip.thinktrip_api.controller;

import com.thinktrip.thinktrip_api.dto.diary.DiaryRequest;
import com.thinktrip.thinktrip_api.dto.diary.DiaryResponse;
import com.thinktrip.thinktrip_api.service.diary.DiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

// DiaryController.java (JWT 이메일 기반)

@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> createDiary(@RequestPart("request") DiaryRequest request,
                                         @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        String email = getEmailFromToken();
        System.out.println("images is null? " + (images == null));
        diaryService.createDiary(request.getTravelPlanId(), request, email, images);
        return ResponseEntity.ok(Map.of("message", "다이어리 저장 완료"+ images));
    }

    @PutMapping(value = "/{diaryId}", consumes = "multipart/form-data")
    public ResponseEntity<?> updateDiary(@PathVariable Long diaryId,
                                         @RequestPart("request") DiaryRequest request,
                                         @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        String email = getEmailFromToken();
        diaryService.updateDiary(diaryId, request, email);
        return ResponseEntity.ok(Map.of("message", "다이어리 수정 완료"));
    }

    @DeleteMapping("/{diaryId}")
    public ResponseEntity<?> deleteDiary(@PathVariable Long diaryId) {
        String email = getEmailFromToken();
        diaryService.deleteDiary(diaryId, email);
        return ResponseEntity.ok(Map.of("message", "다이어리 삭제 완료"));
    }

    @GetMapping("/{diaryId}")
    public ResponseEntity<DiaryResponse> getDiary(@PathVariable Long diaryId) {
        String email = getEmailFromToken();
        return ResponseEntity.ok(diaryService.getDiaryById(diaryId, email));
    }

    @GetMapping
    public ResponseEntity<List<DiaryResponse>> getUserDiaries() {
        String email = getEmailFromToken();
        return ResponseEntity.ok(diaryService.getAllDiariesForUser(email));
    }

    @GetMapping("/plan/{planId}")
    public ResponseEntity<List<DiaryResponse>> getDiariesByPlan(@PathVariable Long planId) {
        String email = getEmailFromToken();
        return ResponseEntity.ok(diaryService.getAllDiaries(planId, email));
    }

    private String getEmailFromToken() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
