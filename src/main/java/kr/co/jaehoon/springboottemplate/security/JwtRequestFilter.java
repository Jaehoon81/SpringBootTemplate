package kr.co.jaehoon.springboottemplate.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.jaehoon.springboottemplate.service.impl.UserDetailsServiceImpl;
import kr.co.jaehoon.springboottemplate.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String jwt = null;
        String username = null;

        // 1. Authorization 헤더에서 JWT 추출 (기존 방식)
        final String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
//            try {
//                username = jwtUtil.extractUsername(jwt);
//            } catch (Exception e) {
//                logger.error("JWT Token extraction failed: " + e.getMessage());
//                // 토큰 추출 실패 시 다음 필터로 진행하지 않거나 401 응답 처리
//                // response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT Token");
//                // return;
//            }
        }
        // 2. Authorization 헤더에 JWT가 없으면 쿠키에서 JWT 추출
        if (jwt == null && request.getCookies() != null) {
            jwt = Arrays.stream(request.getCookies())
                    .filter(cookie -> "jwtToken".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }
        // JWT가 존재하면 검증 시작
        if (jwt != null) {
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                logger.warn("JWT token extraction failed or token is invalid: " + e.getMessage());
                // 유효하지 않은 토큰 처리 (예: 401 에러로 넘기거나, 다음 필터로 진행하지 않음)
                // 이 단계에서 401을 직접 발생시키지 않으면 SecurityConfig의 JwtAuthenticationEntryPoint로 이동
            }
        }
        // username이 있고, 아직 SecurityContext에 인증 정보가 없는 경우에만 인증 진행
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            if (jwtUtil.validateToken(jwt, (CustomUserDetails) userDetails)) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        chain.doFilter(request, response);
    }
}
