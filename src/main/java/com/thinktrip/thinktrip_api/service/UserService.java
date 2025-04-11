package com.thinktrip.thinktrip_api.service;

import com.thinktrip.thinktrip_api.domain.user.User;
import com.thinktrip.thinktrip_api.dto.user.UserInfoResponse;
import com.thinktrip.thinktrip_api.dto.user.UserSignupRequest;
import com.thinktrip.thinktrip_api.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

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
                user.getUpdatedAt()
        );
    }

}
