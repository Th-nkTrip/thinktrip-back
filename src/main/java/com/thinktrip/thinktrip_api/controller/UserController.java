package com.thinktrip.thinktrip_api.controller;

import com.thinktrip.thinktrip_api.dto.user.*;
import com.thinktrip.thinktrip_api.domain.user.User;
import com.thinktrip.thinktrip_api.domain.user.UserRepository;
import com.thinktrip.thinktrip_api.jwt.JwtUtil;
import com.thinktrip.thinktrip_api.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody UserSignupRequest request) {
        userService.signup(request);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isEmpty() || !passwordEncoder.matches(request.getPassword(), userOpt.get().getPassword())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new UserLoginErrorResponse("이메일 또는 비밀번호가 일치하지 않습니다."));
        }

        String token = jwtUtil.generateToken(request.getEmail());

        return ResponseEntity.ok(new UserLoginResponse(token));
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getMyInfo(Authentication auth) {
        String email = auth.getName(); // JWT에서 추출된 사용자 이메일
        UserInfoResponse userInfo = userService.getUserInfo(email);
        return ResponseEntity.ok(userInfo);
    }

    @DeleteMapping("/users/me")
    public ResponseEntity<?> deleteUser(@AuthenticationPrincipal String email) {
        userService.deleteByEmail(email);
        return ResponseEntity.ok().body(Map.of("message", "회원 탈퇴가 완료되었습니다."));
    }


    // 프로필 사진 업로드
    @PostMapping("/profile-image")
    public ResponseEntity<?> uploadProfileImage(@RequestParam("image") MultipartFile image,
                                                @AuthenticationPrincipal String email) {
        try {
            String imageUrl = userService.uploadProfileImage(email, image);
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "파일 저장 중 오류가 발생했습니다."));
        }
    }

    // 본인 프로필 사진 조회 및 바이너리로 보내기
    @GetMapping("/profile-image")
    public ResponseEntity<Resource> getProfileImage(@AuthenticationPrincipal String email) throws IOException {
        ResourceWithType result = userService.getProfileImage(email);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType(result.getContentType() != null ? result.getContentType() : "application/octet-stream")
        );

        return new ResponseEntity<>(result.getResource(), headers, HttpStatus.OK);
    }

    // 다른 사용자 id 기반 프로필 사진 조회 및 바이너리로 보내기
    @GetMapping("/profile-image/{id}")
    public ResponseEntity<Resource> getProfileImageById(@PathVariable Long id) throws IOException {
        ResourceWithType result = userService.getProfileImageById(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                result.getContentType() != null ? result.getContentType() : "application/octet-stream"
        ));

        return new ResponseEntity<>(result.getResource(), headers, HttpStatus.OK);
    }



    // 프로필 사진 삭제
    @DeleteMapping("/profile-image")
    public ResponseEntity<?> deleteProfileImage(@AuthenticationPrincipal String email) {
        try {
            userService.deleteProfileImage(email);
            return ResponseEntity.ok(Map.of("message", "프로필 이미지가 기본 이미지로 초기화되었습니다."));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "이미지 삭제 중 오류가 발생했습니다."));
        }
    }

    // gpt 사용횟수 증가 및 성공여부 반환
    @PostMapping("/gpt/usage")
    public ResponseEntity<?> recordGptUsage(@AuthenticationPrincipal String email) {
        try {
            int remaining = userService.useGptOrThrow(email);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "remainingCalls", remaining == -1 ? "무제한" : remaining
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // gpt 사용 횟수 조회
    @GetMapping("/gpt/usage")
    public ResponseEntity<?> getGptUsage(@AuthenticationPrincipal String email) {
        int remaining = userService.getRemainingGptCalls(email);

        return ResponseEntity.ok(Map.of(
                "remainingCalls", remaining == -1 ? "무제한" : remaining
        ));
    }


    // TestController.java
    @RestController
    @RequestMapping("/api/test")
    public class TestController {

        @GetMapping("/secure")
        public ResponseEntity<String> secureEndpoint(Authentication auth) {
            return ResponseEntity.ok("인증된 사용자: " + auth.getName());
        }
    }


}
