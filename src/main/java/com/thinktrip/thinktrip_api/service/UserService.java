package com.thinktrip.thinktrip_api.service;

import com.thinktrip.thinktrip_api.domain.user.User;
import com.thinktrip.thinktrip_api.dto.user.UserInfoResponse;
import com.thinktrip.thinktrip_api.dto.user.UserSignupRequest;
import com.thinktrip.thinktrip_api.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${app.upload-path}") // 사용자 프로필 업로드 경로
    private String uploadPath;

    public void signup(UserSignupRequest request) {
        // 중복 이메일 체크
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new RuntimeException("이미 가입된 이메일입니다.");
        }

        // 비밀번호 암호화
        String encryptedPassword = passwordEncoder.encode(request.getPassword());

        // User 엔티티 생성
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(encryptedPassword);
        user.setName(request.getName());
        user.setNickname(request.getNickname());
        user.setAddress(request.getAddress());
        user.setTravelStyle(request.getTravelStyle());
        user.setRole("USER");
        user.setProvider("local");

        // 저장
        userRepository.save(user);
    }

    public UserInfoResponse getUserInfo(String email) {
        User user = userRepository.findById(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 이미지 존재 여부 확인
        Path imagePath = Paths.get(uploadPath, email + ".jpg");
        boolean hasCustomImage = Files.exists(imagePath);
        String profileImageUrl = "/image/" + (hasCustomImage ? email : "default.jpg");


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

    public String uploadProfileImage(String email, MultipartFile image) throws IOException {
        // 확장자 체크 (선택)
        if (!image.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
        }

        // 저장 경로 설정
        String filename = email + ".jpg";
        Path savePath = Paths.get(uploadPath, filename);

        Files.createDirectories(savePath.getParent());
        Files.write(savePath, image.getBytes());

        return "/image/" + email;
    }

    public void deleteProfileImage(String email) throws IOException {
        Path imagePath = Paths.get(uploadPath, email + ".jpg");

        if (Files.exists(imagePath)) {
            Files.delete(imagePath);
        }
    }

}
