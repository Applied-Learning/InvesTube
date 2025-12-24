package com.Investube.mvc.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.Investube.mvc.interceptor.AuthInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // allowedOrigins → allowedOriginPatterns 로 변경
                .allowedOriginPatterns("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    private final AuthInterceptor authInterceptor;

    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns(
                        "/users/me/**",
                        "/videos/**",
                        "/reviews/**",
                        "/boards/**",
                        "/board/**",
                        "/comments/**",
                        "/financial/**",
                        "/stocks/wished",
                        "/stocks/*/wished",
                        "/stocks/*/wish",
                        "/profiles/**", // 프로필 관련 API 인증 필요
                        "/chat/**" // 챗봇 API 인증 필요 (투자 성향 반영)
                )
                // ⭐ 인증 불필요 + CORS 문제 방지
                .excludePathPatterns(
                        "/auth/**",
                        "/reviews/video/*/",
                        "/profiles/survey/questions", // 설문 질문 조회는 인증 불필요
                        "/financial/sync-all", // 배치 동기화 API (관리자용)
                        "/financial/sync-status", // 동기화 상태 확인
                        "/financial/export-csv", // CSV 내보내기
                        "/financial/load-csv" // CSV 로드
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
