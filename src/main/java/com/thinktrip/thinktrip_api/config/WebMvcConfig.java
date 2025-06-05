package com.thinktrip.thinktrip_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload-path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 정적 이미지 자원 서빙
        registry.addResourceHandler("/uploads/profile/**")
                .addResourceLocations("file:" + uploadPath + "/profile/");

        registry.addResourceHandler("/uploads/diary/**")
                .addResourceLocations("file:" + uploadPath + "/diary/");
    }
}

