package com.thinktrip.thinktrip_api.service.user;

import com.thinktrip.thinktrip_api.jwt.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${app.frontend.redirect-url}")
    private String frontendRedirectUrl; // 환경 변수에서 읽음

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User user = (CustomOAuth2User) authentication.getPrincipal();
        String email = user.getEmail();
        String token = jwtUtil.generateToken(email);

        // 프론트엔드로 토큰 리다이렉트 (프론트에 따라 수정 가능)
        response.sendRedirect(frontendRedirectUrl + "?token=" + token);
    }
}
