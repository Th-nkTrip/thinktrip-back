package com.thinktrip.thinktrip_api.service.user;

import com.thinktrip.thinktrip_api.domain.user.User;
import com.thinktrip.thinktrip_api.dto.user.ResourceWithType;
import com.thinktrip.thinktrip_api.dto.user.UserInfoResponse;
import com.thinktrip.thinktrip_api.dto.user.UserSignupRequest;
import com.thinktrip.thinktrip_api.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${app.upload-path}") // 사용자 프로필 업로드 경로
    private String uploadPath;

    public void signup(UserSignupRequest request) {
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new RuntimeException("이미 가입된 이메일입니다.");
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 필수 입력 항목입니다.");
        }

        String encryptedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(encryptedPassword);
        user.setName(request.getName());
        user.setNickname(request.getNickname());
        user.setAddress(request.getAddress());
        user.setTravelStyle(request.getTravelStyle());
        user.setRole("USER");
        user.setProvider("local");

        userRepository.save(user);
    }

    public UserInfoResponse getUserInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Path imagePath = Paths.get(uploadPath+"/profile", user.getId() + ".jpg");
        boolean hasCustomImage = Files.exists(imagePath);
        String profileImageUrl = "/api/users/profile-image/" + (hasCustomImage ? user.getId() : "default.jpg");

        return new UserInfoResponse(
                user.getEmail(),
                user.getName(),
                user.getNickname(),
                user.getAddress(),
                user.getTravelStyle(),
                user.getRole(),
                user.getProvider(),
                user.isPremium(),
                user.getGptCallCount(),
                user.getLastGptCallDate(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                profileImageUrl
        );
    }

    public void deleteByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        userRepository.delete(user);
    }


    public String uploadProfileImage(String email, MultipartFile image) throws IOException {
        if (!image.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String filename = user.getId() + ".jpg";
        Path savePath = Paths.get(uploadPath+"/profile", filename);

        Files.createDirectories(savePath.getParent());
        Files.write(savePath, image.getBytes());

        return "/api/users/profile-image/" + user.getId();
    }

    public void deleteProfileImage(String email) throws IOException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Path imagePath = Paths.get(uploadPath+"/profile", user.getId() + ".jpg");

        if (Files.exists(imagePath)) {
            Files.delete(imagePath);
        }
    }

    public ResourceWithType getProfileImage(String email) throws IOException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return resolveImage(user.getId());
    }

    public ResourceWithType getProfileImageById(Long id) throws IOException {
        return resolveImage(id);
    }

    private ResourceWithType resolveImage(Long id) throws IOException {
        Path imagePath = Paths.get(uploadPath+"/profile", id + ".jpg");

        if (!Files.exists(imagePath)) {
            imagePath = Paths.get(uploadPath+"/profile", "default.jpg");
        }

        Resource resource = new UrlResource(imagePath.toUri());
        String contentType = Files.probeContentType(imagePath);

        return new ResourceWithType(resource, contentType);
    }

    // 1. 날짜가 바뀌었으면 호출 수 초기화
    private void resetGptCallIfNewDay(User user) {
        LocalDate today = LocalDate.now();

        if (user.getLastGptCallDate() == null || !user.getLastGptCallDate().isEqual(today)) {
            user.setGptCallCount(0);
            user.setLastGptCallDate(today);
        }
    }

    // 2. 오늘 사용 가능한지 확인
    private boolean canUseGpt(User user) {
        return user.isPremium() || user.getGptCallCount() < 5;
    }

    // 3. 호출 수 증가 처리
    private void useGpt(User user) {
        if (!user.isPremium()) {
            user.setGptCallCount(user.getGptCallCount() + 1);
        }
        userRepository.save(user);
    }

    // gpt 사용횟수 증가 기능
    @Transactional
    public int useGptOrThrow(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        resetGptCallIfNewDay(user);

        if (!canUseGpt(user)) {
            throw new IllegalStateException("오늘 GPT 사용 한도(5회)를 초과했습니다.");
        }

        useGpt(user);

        return user.isPremium() ? -1 : Math.max(0, 5 - user.getGptCallCount());
    }

    // gpt 사용횟수 조회
    public int getRemainingGptCalls(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        resetGptCallIfNewDay(user);

        if (user.isPremium()) return -1;

        return Math.max(0, 5 - user.getGptCallCount());
    }

    public void updateUserInfo(String currentEmail, UserSignupRequest request) {
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // 이메일 변경 허용
        if (request.getEmail() != null && !request.getEmail().equals(currentEmail)) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
            }
            user.setEmail(request.getEmail());
        }

        user.setName(request.getName());

        if (request.getNickname() != null) user.setNickname(request.getNickname());
        if (request.getAddress() != null) user.setAddress(request.getAddress());
        if (request.getTravelStyle() != null) user.setTravelStyle(request.getTravelStyle());

        if (user.getProvider() == null && request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userRepository.save(user);
    }


}

