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

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${app.upload-path}")
    private String uploadPath;

    public void signup(UserSignupRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
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
        User user = getUserByEmail(email);

        Path imagePath = getProfileImagePath(user.getId());
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
        User user = getUserByEmail(email);
        userRepository.delete(user);
    }

    public String uploadProfileImage(String email, MultipartFile image) throws IOException {
        if (image.getContentType() == null || !image.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
        }


        User user = getUserByEmail(email);
        Path savePath = getProfileImagePath(user.getId());

        Files.createDirectories(savePath.getParent());
        Files.write(savePath, image.getBytes());

        return "/api/users/profile-image/" + user.getId();
    }

    public void deleteProfileImage(String email) throws IOException {
        User user = getUserByEmail(email);
        Path imagePath = getProfileImagePath(user.getId());

        if (Files.exists(imagePath)) {
            Files.delete(imagePath);
        }
    }

    public ResourceWithType getProfileImage(String email) throws IOException {
        User user = getUserByEmail(email);
        return resolveImage(user.getId());
    }

    public ResourceWithType getProfileImageById(Long id) throws IOException {
        return resolveImage(id);
    }

    private ResourceWithType resolveImage(Long id) throws IOException {
        Path imagePath = getProfileImagePath(id);

        if (!Files.exists(imagePath)) {
            imagePath = Paths.get(uploadPath, "profile", "default.jpg");
        }

        Resource resource = new UrlResource(imagePath.toUri());
        String contentType = Files.probeContentType(imagePath);

        return new ResourceWithType(resource, contentType);
    }

    private Path getProfileImagePath(Long userId) {
        return Paths.get(uploadPath, "profile", userId + ".jpg");
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private void resetGptCallIfNewDay(User user) {
        LocalDate today = LocalDate.now();

        if (user.getLastGptCallDate() == null || !user.getLastGptCallDate().isEqual(today)) {
            user.setGptCallCount(0);
            user.setLastGptCallDate(today);
        }
    }

    private boolean canUseGpt(User user) {
        return user.isPremium() || user.getGptCallCount() < 5;
    }

    private void useGpt(User user) {
        if (!user.isPremium()) {
            user.setGptCallCount(user.getGptCallCount() + 1);
        }
        userRepository.save(user);
    }

    @Transactional
    public int useGptOrThrow(String email) {
        User user = getUserByEmail(email);

        resetGptCallIfNewDay(user);

        if (!canUseGpt(user)) {
            throw new IllegalStateException("오늘 GPT 사용 한도(5회)를 초과했습니다.");
        }

        useGpt(user);

        return user.isPremium() ? -1 : Math.max(0, 5 - user.getGptCallCount());
    }

    public int getRemainingGptCalls(String email) {
        User user = getUserByEmail(email);

        resetGptCallIfNewDay(user);

        return user.isPremium() ? -1 : Math.max(0, 5 - user.getGptCallCount());
    }

    public void updateUserInfo(String currentEmail, UserSignupRequest request) {
        User user = getUserByEmail(currentEmail);

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
