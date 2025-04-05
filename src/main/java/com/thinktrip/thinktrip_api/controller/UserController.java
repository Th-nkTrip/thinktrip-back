package com.thinktrip.thinktrip_api.controller;

import com.thinktrip.thinktrip_api.dto.user.UserSignupRequest;
import com.thinktrip.thinktrip_api.dto.user.UserLoginRequest;
import com.thinktrip.thinktrip_api.domain.user.User;
import com.thinktrip.thinktrip_api.domain.user.UserRepository;
import com.thinktrip.thinktrip_api.jwt.JwtUtil;
import com.thinktrip.thinktrip_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<String> login(@RequestBody UserLoginRequest request) {
        Optional<User> userOpt = userRepository.findById(request.getEmail());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        return ResponseEntity.ok(token); // 토큰 반환
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
