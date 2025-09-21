package kr.co.jaehoon.springboottemplate.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.jaehoon.springboottemplate.service.impl.UserDetailsServiceImpl;
import kr.co.jaehoon.springboottemplate.security.JwtAuthenticationEntryPoint;
import kr.co.jaehoon.springboottemplate.security.JwtRequestFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtRequestFilter jwtRequestFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private final ObjectMapper objectMapper;

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
            // 1. 요청이 API 엔드포인트인지 판단 (/api/ 로 시작하는 URI)
            boolean isApiRequest = request.getRequestURI().startsWith("/api/");
            // 2. 클라이언트의 'Accept' 헤더를 명시적으로 검사하여 JSON을 원하는지 확인
            String acceptHeader = request.getHeader("Accept");
            boolean expectsJsonExplicitly = (acceptHeader != null && acceptHeader.contains(MediaType.APPLICATION_JSON_VALUE));
            // 3. AJAX 요청인지 확인 (웹에서 AJAX 호출 시 사용하는 X-Requested-With 헤더를 통한 판단)
            boolean isAjaxRequest = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
            // JSON 응답을 보내야 하는 조건:
            // - 명백하게 API 요청인 경우 (경로 기준)
            // - or 클라이언트가 명시적으로 JSON을 요청한 경우
            // - or 일반적인 AJAX 요청인 경우 (일반적으로 JSON 응답을 기대)
            boolean shouldSendJson = isApiRequest || expectsJsonExplicitly || isAjaxRequest;

            if (shouldSendJson == true) {
                // JSON 응답을 기대하는 클라이언트 (모바일 앱, API 클라이언트 등)
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);       // 403 Forbidden
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);  // Content-Type 설정
                response.setCharacterEncoding("UTF-8");                     // 한글 깨짐 방지

                Map<String, Object> errorDetails = new HashMap<>();
                errorDetails.put("status", HttpServletResponse.SC_FORBIDDEN);
                errorDetails.put("error", "Forbidden");
                errorDetails.put("message", "접근 권한이 없습니다.");  // 상세 메시지
                errorDetails.put("exceptionMessage", accessDeniedException.getMessage());  // 예외 메시지 (개발 및 디버그 용도로 사용)

                objectMapper.writeValue(response.getWriter(), errorDetails);  // JSON 응답 작성
            } else {
                // 웹 브라우저에서 직접 접근(페이지 요청)인 경우:
                // 1. 403 HTML 응답(response.sendError()) 대신, 응답 상태 코드를 403으로 설정하고 /error 경로로 포워드
//                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden: " + accessDeniedException.getMessage());
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
                                "/api/auth/register", "/api/auth/admins",
                                "/api/auth/web-login", "/api/auth/mobile-login", "/api/auth/web-logout", "/api/auth/mobile-logout",
                                "/", "/error", "/favicon.ico",
                                // favicon.ico 파일: 16x16 또는 32x32 픽셀의 .ico 형식
                                // - 경로: 'src/main/webapp/favicon.ico' or 'src/main/resources/static/favicon.ico'

                                // Spring Boot는 src/main/resources/static 경로를 / 로컬 루트로 매핑
//                                "/js/**", "/css/**", "/include/**", "/WEB-INF/views/**"
                                // JSP 설정을 통해 src/main/webapp 경로를 / 로컬 루트로 매핑
                                "/static/js/**", "/static/css/**", "/static/include/**", "/WEB-INF/views/**"
                        ).permitAll()
                        // /secure-page는 authenticated()로 유지하여 인증된 사용자만 접근하도록 함 (기본 적용)
//                        .requestMatchers("/secure-page").authenticated()
                        .requestMatchers("/dashboard", "/contents/**").authenticated()
                        // /api/auth/check-token과 /api/app/version은 여전히 authenticated() 대상임 (기본 적용)
                        .requestMatchers("/api/auth/check-token", "/api/app/version").authenticated()

                        // 역할별 접근 권한 설정 (콘텐츠 URL 기준)
                        .requestMatchers("/contents/system-approval").hasRole("SYSTEM")
                        .requestMatchers("/contents/admin-approval").hasRole("ADMIN")
                        .requestMatchers("/contents/secure").hasRole("USER")
                        // 모든 인증된 사용자는 접근 가능
                        .requestMatchers("/contents/statistics", "/contents/profile").hasAnyRole("USER", "ADMIN", "SYSTEM")

                        // SYSTEM, ADMIN 권한 전용 페이지 설정
//                        .requestMatchers("/system-page").hasRole("SYSTEM")
//                        .requestMatchers("/admin-page").hasRole("ADMIN")
                        // SYSTEM, ADMIN 권한 전용 API 엔드포인트 보호
                        .requestMatchers("/api/system/**").hasRole("SYSTEM")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/auth/admin-only").hasRole("ADMIN")

                        // 만약 특정 API는 권한 없이 접근하게 하려면 여기에 .permitAll() 추가
//                        .requestMatchers("/api/public/**").permitAll()
                        // 에러 테스트용 경로는 permitAll()
                        .requestMatchers("/api/auth/test-error", "/trigger-error-page").permitAll()
                        
                        // 그 외 모든 요청은 인증 필요
                        // (/api/auth/web-logout, /api/auth/mobile-logout도 포함
                        // -> 포함 X: 토큰 만료 시 로그아웃 버튼을 누르면 401 에러가 발생하므로 permitAll()로 수정)
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
