package com.thinktrip.thinktrip_api.service.diary;

import com.thinktrip.thinktrip_api.domain.diary.Diary;
import com.thinktrip.thinktrip_api.domain.diary.DiaryRepository;
import com.thinktrip.thinktrip_api.domain.travel.TravelPlan;
import com.thinktrip.thinktrip_api.domain.travel.TravelPlanRepository;
import com.thinktrip.thinktrip_api.dto.diary.DiaryRequest;
import com.thinktrip.thinktrip_api.dto.diary.DiaryResponse;
import com.thinktrip.thinktrip_api.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final UserRepository userRepository;

    public void createDiary(Long travelPlanId, DiaryRequest request, String email) {
        TravelPlan plan = getOwnedPlan(travelPlanId, email);

        Diary diary = new Diary();
        diary.setDate(request.getStartDate());
        diary.setDate(request.getEndDate());
        diary.setTitle(request.getTitle());
        diary.setContent(request.getContent());
        diary.setTravelPlan(plan);

        diaryRepository.save(diary);
    }

    public void updateDiary(Long diaryId, DiaryRequest request, String email) {
        Diary diary = getOwnedDiary(diaryId, email);
        diary.setDate(request.getStartDate());
        diary.setDate(request.getEndDate());
        diary.setTitle(request.getTitle());
        diary.setContent(request.getContent());
    }

    public void deleteDiary(Long diaryId, String email) {
        Diary diary = getOwnedDiary(diaryId, email);
        diaryRepository.delete(diary);
    }

    public List<DiaryResponse> getAllDiaries(Long travelPlanId, String email) {
        TravelPlan plan = getOwnedPlan(travelPlanId, email);
        return diaryRepository.findAllByTravelPlan(plan)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public DiaryResponse getDiaryById(Long diaryId, String email) {
        Diary diary = getOwnedDiary(diaryId, email);
        return toDto(diary);
    }

    private Diary getOwnedDiary(Long diaryId, String email) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("다이어리를 찾을 수 없습니다."));
        String planOwner = diary.getTravelPlan().getUser().getEmail();
        if (!planOwner.equals(email)) {
            throw new IllegalArgumentException("접근 권한이 없습니다.");
        }
        return diary;
    }

    private TravelPlan getOwnedPlan(Long planId, String email) {
        TravelPlan plan = travelPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("여행 계획이 존재하지 않습니다."));
        if (!plan.getUser().getEmail().equals(email)) {
            throw new IllegalArgumentException("해당 여행 계획에 접근할 수 없습니다.");
        }
        return plan;
    }

    private DiaryResponse toDto(Diary diary) {
        return DiaryResponse.builder()
                .id(diary.getId())
                .startDate(diary.getTravelPlan().getStartDate())
                .endDate(diary.getTravelPlan().getEndDate())
                .title(diary.getTitle())
                .content(diary.getContent())
                .travelPlanId(diary.getTravelPlan().getId())
                .createdAt(diary.getCreatedAt())
                .updatedAt(diary.getUpdatedAt())
                .build();
    }
}
