package com.thinktrip.thinktrip_api.service.diary;

import com.thinktrip.thinktrip_api.domain.diary.Diary;
import com.thinktrip.thinktrip_api.domain.diary.DiaryImage;
import com.thinktrip.thinktrip_api.domain.diary.DiaryRepository;
import com.thinktrip.thinktrip_api.domain.travel.TravelPlan;
import com.thinktrip.thinktrip_api.domain.travel.TravelPlanRepository;
import com.thinktrip.thinktrip_api.domain.user.User;
import com.thinktrip.thinktrip_api.domain.user.UserRepository;
import com.thinktrip.thinktrip_api.dto.diary.DiaryRequest;
import com.thinktrip.thinktrip_api.dto.diary.DiaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final UserRepository userRepository;

    @Value("${app.upload-path}")
    private String uploadPath;

    @Transactional
    public void createDiary(Long travelPlanId, DiaryRequest request, String email, List<MultipartFile> images) {
        User user = getUserByEmail(email);
        TravelPlan plan = (travelPlanId != null) ? getOwnedPlan(travelPlanId, email) : null;

        Diary diary = new Diary();
        diary.setStartDate(request.getStartDate());
        diary.setEndDate(request.getEndDate());
        diary.setTitle(request.getTitle());
        diary.setContent(request.getContent());
        diary.setTravelPlan(plan);
        diary.setUser(user);

        if (images != null && !images.isEmpty()) {
            images.stream()
                    .map(this::convertToDiaryImage)
                    .forEach(image -> {
                        image.setDiary(diary);
                        diary.getImages().add(image);
                    });
        }

        diaryRepository.save(diary);
    }

    @Transactional
    public void updateDiary(Long diaryId, DiaryRequest request, String email, List<MultipartFile> images) {
        Diary diary = getOwnedDiary(diaryId, email);

        diary.setStartDate(request.getStartDate());
        diary.setEndDate(request.getEndDate());
        diary.setTitle(request.getTitle());
        diary.setContent(request.getContent());

        // 기존 이미지 삭제
        diary.getImages().forEach(image -> deleteImageFile(image.getImageUrl()));
        diary.getImages().clear();

        if (images != null && !images.isEmpty()) {
            images.stream()
                    .map(this::convertToDiaryImage)
                    .forEach(image -> {
                        image.setDiary(diary);
                        diary.getImages().add(image);
                    });
        }

        diaryRepository.save(diary);
    }

    @Transactional
    public void uploadImagesToDiary(Long diaryId, String email, List<MultipartFile> images) {
        Diary diary = getOwnedDiary(diaryId, email);

        if (images != null && !images.isEmpty()) {
            images.stream()
                    .map(this::convertToDiaryImage)
                    .forEach(image -> {
                        image.setDiary(diary);
                        diary.getImages().add(image);
                    });

            diaryRepository.save(diary);
        }
    }

    @Transactional
    public void deleteDiary(Long diaryId, String email) {
        Diary diary = getOwnedDiary(diaryId, email);
        diary.getImages().forEach(image -> deleteImageFile(image.getImageUrl()));
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
        return toDto(getOwnedDiary(diaryId, email));
    }

    public List<DiaryResponse> getAllDiariesForUser(String email) {
        User user = getUserByEmail(email);
        return diaryRepository.findAllByUser(user)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // ===== 내부 메서드 =====

    private Diary getOwnedDiary(Long diaryId, String email) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("다이어리를 찾을 수 없습니다."));
        if (!diary.getUser().getEmail().equals(email)) {
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

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
    }

    private String saveImage(MultipartFile image) {
        try {
            String filename = UUID.randomUUID() + "_" + image.getOriginalFilename();
            Path savePath = Paths.get(uploadPath + "/diary", filename);
            Files.createDirectories(savePath.getParent());
            Files.write(savePath, image.getBytes());
            return "/uploads/diary/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패", e);
        }
    }

    private void deleteImageFile(String imageUrl) {
        try {
            String relativePath = imageUrl.replace("/uploads", "");
            Path fullPath = Paths.get(uploadPath + relativePath);
            Files.deleteIfExists(fullPath);
        } catch (IOException e) {
            System.err.println("이미지 삭제 실패: " + e.getMessage());
        }
    }

    private DiaryImage convertToDiaryImage(MultipartFile image) {
        String url = saveImage(image);
        DiaryImage diaryImage = new DiaryImage();
        diaryImage.setImageUrl(url);
        return diaryImage;
    }

    private DiaryResponse toDto(Diary diary) {
        List<String> imageUrls = diary.getImages().stream()
                .map(DiaryImage::getImageUrl)
                .toList();

        return DiaryResponse.builder()
                .id(diary.getId())
                .startDate(diary.getStartDate())
                .endDate(diary.getEndDate())
                .title(diary.getTitle())
                .content(diary.getContent())
                .travelPlanId(diary.getTravelPlan() != null ? diary.getTravelPlan().getId() : null)
                .userId(diary.getUser().getId())
                .imageUrls(imageUrls)
                .createdAt(diary.getCreatedAt())
                .updatedAt(diary.getUpdatedAt())
                .build();
    }
}

