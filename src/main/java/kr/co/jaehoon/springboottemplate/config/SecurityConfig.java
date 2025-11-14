package kr.co.jaehoon.springboottemplate.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.jaehoon.springboottemplate.dto.network.BasicResponse;
import kr.co.jaehoon.springboottemplate.dto.network.ErrorResponse;
import kr.co.jaehoon.springboottemplate.security.CustomAccessDeniedHandler;
import kr.co.jaehoon.springboottemplate.security.CustomAuthenticationEntryPoint;
import kr.co.jaehoon.springboottemplate.service.impl.UserDetailsServiceImpl;
import kr.co.jaehoon.springboottemplate.security.JwtRequestFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
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
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)  // CSRF 비활성화 (JWT 사용 시 일반적으로 필요 없음)
                .authorizeHttpRequests(authorize -> authorize
                        // 인증 없이 접근을 허용할 경로 설정 ---------------------------------------------------------------------------------
                        .requestMatchers(
                                // Spring Boot는 src/main/resources/static 경로를 / 로컬 루트로 매핑
//                                "/js/**", "/css/**", "/include/**", "/libs/**", "/images/**", "/WEB-INF/views/**",
                                // JSP 설정을 통해 src/main/webapp 경로를 / 로컬 루트로 매핑
                                "/static/**", "/WEB-INF/views/**",
                                // favicon.ico 파일: 최소 48x48 픽셀 이상의 .ico 형식
                                // - 경로: 'src/main/webapp/favicon.ico' or 'src/main/resources/static/favicon.ico'
                                "/favicon_01.ico", "/favicon_02.ico", "/favicon_03.ico", "/favicon_04.ico", "/favicon_05.ico",

                                "/", "/error"  // 웹 사이트 홈(로그인), 에러 페이지 경로
                        ).permitAll()
                        .requestMatchers(
                                "/api/auth/register", "/api/auth/admins", "/api/auth/find-account",
                                "/api/auth/web-login", "/api/auth/web-logout",
                                "/api/auth/mobile-login", "/api/auth/mobile-logout",

//                                "/profiles/**",  // UserRestController에 이미 파일서빙 로직이 있기 때문에 WebConfig 설정과 중복되므로 주석처리
                                "/api/user/profile-picture/**",  // 프로필 사진(이미지) 조회
                                "/api/records/play/{recordId}"   // 음성녹음 플레이(재생)
                        ).permitAll()
                        .requestMatchers(
                                // Swagger UI 및 OpenAPI 문서 경로
                                // Swagger UI 접속 HTML, Swagger UI 리소스(CSS/JS 등), OpenAPI 3.0 API 문서(JSON/YAML)
                                "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**"
                        ).permitAll()
                        // 인증 없이 접근을 허용할 경로 설정 ---------------------------------------------------------------------------------

                        // /secure-page는 authenticated()로 유지하여 인증된 사용자만 접근하도록 함 (기본 적용)
//                        .requestMatchers("/secure-page").authenticated()
                        .requestMatchers("/dashboard", "/contents/**").authenticated()
                        // 모바일 앱에서 세션만료 여부 확인 및 애플리케이션 버전 정보 요청은 인증된 사용자만 허용
                        .requestMatchers("/api/auth/check-token", "/api/app-info/version").authenticated()
                        // 프로필 정보 조회 및 업데이트, 사진(이미지) 업로드, 회원 탈퇴는 인증된 사용자만 허용
                        .requestMatchers("/api/user/profile", "/api/user/profile-picture", "/api/user/deactivate").authenticated()
                        // 참가자 관련(등록 및 목록 조회) API는 인증된 사용자만 허용
//                        .requestMatchers("/api/participants/register").authenticated()
//                        .requestMatchers("/api/participants/paginated-list").hasAnyRole("USER", "ADMIN", "SYSTEM")
//                        .requestMatchers("/api/participants/export-excel").hasAnyRole("USER", "ADMIN", "SYSTEM")
//                        .requestMatchers("/api/participants/{participantId}/record-details").hasAnyRole("USER", "ADMIN", "SYSTEM")
//                        .requestMatchers("/api/participants/grade").hasAnyRole("USER", "ADMIN", "SYSTEM")
                        .requestMatchers("/api/participants/**").authenticated()
                        // 음성녹음 관련(업로드) API는 인증된 사용자만 허용
                        .requestMatchers("/api/records/**").authenticated()

                        // 권한(역할)별 접근 권한 설정 (콘텐츠 URL 기준)
                        .requestMatchers("/contents/system-approval").hasRole("SYSTEM")
                        .requestMatchers("/contents/admin-approval").hasRole("ADMIN")
                        .requestMatchers("/contents/secure").hasRole("USER")
                        // 모든 인증된 사용자는 접근 가능
                        .requestMatchers("/contents/statistics", "/contents/notice", "/contents/profile").hasAnyRole("USER", "ADMIN", "SYSTEM")

                        // SYSTEM, ADMIN 권한 전용 페이지 설정
//                        .requestMatchers("/system-page").hasRole("SYSTEM")
//                        .requestMatchers("/admin-page").hasRole("ADMIN")
                        // SYSTEM, ADMIN 권한 전용 API 엔드포인트 보호
                        .requestMatchers("/api/system/**").hasRole("SYSTEM")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/auth/admin-only").hasRole("ADMIN")

                        // 만약 특정 API는 권한 없이 접근하게 하려면 여기에 permitAll() 추가
//                        .requestMatchers("/api/public/**").permitAll()
                        // 에러 테스트용 경로는 permitAll()
                        .requestMatchers("/api/auth/test-error", "/trigger-error-page").permitAll()
                        
                        // 그 외 모든 요청은 인증 필요
                        // (/api/auth/web-logout, /api/auth/mobile-logout도 포함
                        // -> 포함 X: 토큰 만료 시 로그아웃 버튼을 누르면 401 에러가 발생하므로 permitAll()로 수정)
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(customAuthenticationEntryPoint)  // 401 Unauthorized 처리
                        .accessDeniedHandler(customAccessDeniedHandler)            // 403 Forbidden 처리
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // JWT 사용 시 세션 사용 안함
                );
        // JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 추가
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
