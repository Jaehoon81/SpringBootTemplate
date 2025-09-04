package kr.co.jaehoon.springboottemplate.config;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.jaehoon.springboottemplate.service.impl.UserDetailsServiceImpl;
import kr.co.jaehoon.springboottemplate.security.JwtAuthenticationEntryPoint;
import kr.co.jaehoon.springboottemplate.security.JwtRequestFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtRequestFilter jwtRequestFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {  // 403 Forbidden 에러를 처리할 핸들러 정의
        return (request, response, accessDeniedException) -> {
            String requestedWith = request.getHeader("X-Requested-With");
            if ("XMLHttpRequest".equals(requestedWith)) {
                // AJAX 요청인 경우 403 응답 반환
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "접근 권한이 없습니다: " + accessDeniedException.getMessage());
            } else {
                // 웹 브라우저에서 직접 접근(페이지 요청)인 경우:
                // 1. 응답 상태 코드를 403으로 설정하고 /error 경로로 포워드
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                // 2. MyErrorController에서 사용할 에러 속성들을 수동으로 설정
                request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpServletResponse.SC_FORBIDDEN);
                request.setAttribute(RequestDispatcher.ERROR_MESSAGE, "접근 권한이 없습니다.");       // 사용자에게 보여줄 메시지
                request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, accessDeniedException);      // 발생한 예외 객체
                request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, request.getRequestURI());  // 원래 요청 URI
                // 3. Spring Boot의 기본 에러 핸들러(BasicErrorController)가 /error 요청을 처리하고 error.jsp를 렌더링
                request.getRequestDispatcher("/error").forward(request, response);
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)  // CSRF 비활성화 (JWT 사용 시 일반적으로 필요 없음)
                .authorizeHttpRequests(authorize -> authorize
                        // 인증 없이 접근 허용할 경로
                        .requestMatchers(
                                "/api/auth/register", "/api/auth/login", "/", "/error",

                                // Spring Boot는 src/main/resources/static 경로를 / 로컬 루트로 매핑
//                                "/js/**", "/css/**", "/WEB-INF/views/**"
                                // JSP 설정을 통해 src/main/webapp 경로를 / 로컬 루트로 매핑
                                "/static/js/**", "/static/css/**", "/WEB-INF/views/**"
                        ).permitAll()
                        // secure-page는 authenticated()로 유지하여 인증된 사용자만 접근하도록 함 (기본 적용)
                        .requestMatchers("/secure-page").authenticated()

                        // 에러 테스트용 경로는 permitAll()
                        .requestMatchers("/api/auth/test-error", "/trigger-error-page").permitAll()
                        // ADMIN 권한 테스트용 경로
                        .requestMatchers("/api/auth/admin-only", "/admin-page").hasRole("ADMIN")

                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)  // 401 Unauthorized 처리
                        .accessDeniedHandler(accessDeniedHandler())             // 403 Forbidden 처리
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // JWT 사용 시 세션 사용 안함
                );
        // JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 추가
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
