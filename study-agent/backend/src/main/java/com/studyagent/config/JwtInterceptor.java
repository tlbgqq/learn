package com.studyagent.config;

import com.studyagent.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    public static final String USER_ID_HEADER = "X-User-Id";

    private final JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 登录和注册接口不需要验证
        String path = request.getRequestURI();
        if (path.contains("/api/student/login") || path.contains("/api/student/register")
                || path.contains("/api/auth/login")) {
            return true;
        }

        // 公开接口不需要验证（年级）
        if (path.startsWith("/api/grade")) {
            return true;
        }

        // 题目生成接口不需要验证（管理功能）
        if (path.startsWith("/api/question/generate")) {
            return true;
        }

        // 测试接口不需要验证
        if (path.startsWith("/api/test/")) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtils.validateToken(token)) {
                Long userId = jwtUtils.getUserIdFromToken(token);
                request.setAttribute(USER_ID_HEADER, userId);
                return true;
            }
        }

        // 对于需要认证的接口，返回 401
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }
}
