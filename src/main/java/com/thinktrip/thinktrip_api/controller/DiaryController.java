package com.thinktrip.thinktrip_api.controller;

import com.thinktrip.thinktrip_api.dto.diary.DiaryRequest;
import com.thinktrip.thinktrip_api.service.diary.DiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/plan{planId}/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    @PostMapping
    public ResponseEntity<?> create(@PathVariable Long planId,
                                    @RequestBody DiaryRequest request) {
        String email = getEmailFromToken();
        diaryService.createDiary(planId, request, email);
        return ResponseEntity.ok(Map.of("message", "다이어리 저장 완료"));
    }

    @PutMapping("/{diaryId}")
    public ResponseEntity<?> update(@PathVariable Long planId,
                                    @PathVariable Long diaryId,
                                    @RequestBody DiaryRequest request) {
        String email = getEmailFromToken();
        diaryService.updateDiary(diaryId, request, email);
        return ResponseEntity.ok(Map.of("message", "다이어리 수정 완료"));
    }

    @DeleteMapping("/{diaryId}")
    public ResponseEntity<?> delete(@PathVariable Long planId,
                                    @PathVariable Long diaryId) {
        String email = getEmailFromToken();
        diaryService.deleteDiary(diaryId, email);
        return ResponseEntity.ok(Map.of("message", "다이어리 삭제 완료"));
    }

    @GetMapping
    public ResponseEntity<?> getAll(@PathVariable Long planId) {
        String email = getEmailFromToken();
        return ResponseEntity.ok(diaryService.getAllDiaries(planId, email));
    }

    @GetMapping("/{diaryId}")
    public ResponseEntity<?> getById(@PathVariable Long planId,
                                     @PathVariable Long diaryId) {
        String email = getEmailFromToken();
        return ResponseEntity.ok(diaryService.getDiaryById(diaryId, email));
    }

    private String getEmailFromToken() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
