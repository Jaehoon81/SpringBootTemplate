package kr.co.jaehoon.springboottemplate.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        // 요청이 AJAX 요청인지 확인 (일반적으로 'X-Requested-With' 헤더 사용)
        String requestedWith = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(requestedWith)) {
            // AJAX 요청인 경우 401 응답 반환
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: " + authException.getMessage());
        } else {
            // 웹 브라우저에서 직접 접근(페이지 요청)인 경우 로그인 페이지(index.jsp)로 리다이렉트
            response.sendRedirect("/");
        }
    }
}
