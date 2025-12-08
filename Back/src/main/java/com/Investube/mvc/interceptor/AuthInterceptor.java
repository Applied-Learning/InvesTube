package com.Investube.mvc.interceptor;

import com.Investube.mvc.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // CORS preflight OPTIONS 요청은 통과
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 1) 헤더에서 Authorization 가져오기
        String auth = request.getHeader("Authorization");

        // 2) 헤더 자체가 없거나 형식이 틀리면 → 401
        if (auth == null || !auth.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // 3) "Bearer " 이후 토큰 문자열 추출
        String token = auth.substring(7);

        // 4) 토큰 검증
        if (!jwtUtil.validate(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // 5) 토큰에서 userId 추출
        int userId = jwtUtil.getUserIdByToken(token);

        // 6) request 객체에 userId 주입 → 컨트롤러에서 그대로 사용 가능
        request.setAttribute("userId", userId);

        return true; // 정상 → 컨트롤러까지 진행
    }
}
