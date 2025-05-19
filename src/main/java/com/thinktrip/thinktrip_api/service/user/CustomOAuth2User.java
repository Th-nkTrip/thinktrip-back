package com.thinktrip.thinktrip_api.service.user;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private final OAuth2User oauth2User;
    private final String nickname;
    @Getter
    private final String kakaoId;

    public CustomOAuth2User(OAuth2User oauth2User) {
        this.oauth2User = oauth2User;

        @SuppressWarnings("unchecked")
        Map<String, Object> kakaoAccount = (Map<String, Object>) oauth2User.getAttribute("kakao_account");
        if (kakaoAccount == null) {
            throw new OAuth2AuthenticationException("Kakao account 정보가 없습니다.");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        if (profile == null) {
            throw new OAuth2AuthenticationException("Kakao profile 정보가 없습니다.");
        }

        String nickname = (String) profile.get("nickname");


        this.nickname = (String) profile.get("nickname");

        Object rawId = oauth2User.getAttribute("id");
        this.kakaoId = rawId != null ? String.valueOf(rawId) : "unknown";
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oauth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return oauth2User.getAuthorities();
    }

    @Override
    public String getName() {
        return nickname;
    }

    public String getEmail() {
        @SuppressWarnings("unchecked")
        Map<String, Object> kakaoAccount = (Map<String, Object>) oauth2User.getAttribute("kakao_account");
        return (String) kakaoAccount.get("email");
    }

}
