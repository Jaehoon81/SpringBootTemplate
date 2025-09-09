package kr.co.jaehoon.springboottemplate.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kr.co.jaehoon.springboottemplate.dto.RegistrationRequest;
import kr.co.jaehoon.springboottemplate.dto.UserDTO;
import kr.co.jaehoon.springboottemplate.repository.UserRepository;
import kr.co.jaehoon.springboottemplate.repository.dao.UserDAO;
import kr.co.jaehoon.springboottemplate.security.JwtBlacklistService;
import kr.co.jaehoon.springboottemplate.service.UserService;
import kr.co.jaehoon.springboottemplate.service.impl.UserDetailsServiceImpl;
import kr.co.jaehoon.springboottemplate.security.JwtUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class UserRestController {

//    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;
    private final JwtBlacklistService jwtBlacklistService;
    private final PasswordEncoder passwordEncoder;

//    private final UserDAO userDAO;
//    private final UserRepository userRepository;
    private final UserService userService;

    /**
     * 회원가입 API (웹/모바일 공용)
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegistrationRequest registrationRequest) throws UsernameNotFoundException, Exception {
        UserDTO user = userService.findByUsername(registrationRequest.getUsername());
        if (user != null) {
            log.debug("Register_UserDTO: {}", user);
            return ResponseEntity.badRequest().body("이미 사용중인 아이디입니다.");
        }
//        UserDTO newUser = new UserDTO();
//        newUser.setUsername(registrationRequest.getUsername());
//        newUser.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
//        newUser.setDisplayname((registrationRequest.getDisplayname() != null) ? registrationRequest.getDisplayname() : "");
//        newUser.setRole(registrationRequest.getRole());

        // 권한에 대한 유효성 검사(ADMIN, USER만 허용) 추가
        if (!registrationRequest.getRole().equals("USER") && !registrationRequest.getRole().equals("ADMIN")) {
            return ResponseEntity.badRequest().body("Invalid role specified.");
        }
//        userService.save(newUser);
        userService.save(registrationRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 성공적으로 완료되었습니다!");
    }

    /**
     * 웹 로그인 API (쿠키 기반 인증)
     * 웹 브라우저는 로그인 성공 시 HttpOnly 쿠키를 전달받음
     */
    @PostMapping("/web-login")
    public ResponseEntity<?> webLogin(@RequestBody AuthenticationRequest authenticationRequest, HttpServletResponse response) throws Exception {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authenticationRequest.getUsername(), authenticationRequest.getPassword()
            ));
            SecurityContextHolder.getContext().setAuthentication(authentication);  // 인증 정보를 SecurityContext에 저장
        } catch (BadCredentialsException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디 또는 비밀번호가 일치하지 않습니다.");
        }
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails.getUsername(), jwtUtil.getWebExpirationTime());

        // JWT를 HttpOnly 쿠키로 추가
        Cookie jwtCookie = new Cookie("jwtToken", jwt);  // 쿠키 이름은 'jwtToken'
        jwtCookie.setHttpOnly(true);  // JavaScript에서 접근 불가
        jwtCookie.setPath("/");  // 모든 경로에서 유효하도록 설정
        jwtCookie.setMaxAge((int) (jwtUtil.getWebExpirationTime() / 1000));  // JWT 만료 시간과 동일하게 설정 (초 단위)
//        jwtCookie.setSecure(true);  // HTTPS 환경에서 필수
        response.addCookie(jwtCookie);

        // 프론트엔드에서 jwtToken을 body로 받을 필요 없으므로 빈 응답 반환 또는 성공 메시지만 반환
//        return ResponseEntity.ok(new WebAuthResponse(jwt));
        return ResponseEntity.ok().body("로그인(웹) 성공");
    }

    /**
     * 모바일 로그인 API (JWT 응답 본문 전달)
     * 모바일 앱은 로그인 성공 시 Access Token을 응답 본문에서 파싱하여 저장함
     */
    @PostMapping("/mobile-login")
    public ResponseEntity<?> mobileLogin(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {
        try {
            // Spring Security 인증 과정 (stateless이므로 SecurityContextHolder에 저장하지 않음)
            // AuthenticationManager를 통해 사용자가 유효한지 검증
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authenticationRequest.getUsername(), authenticationRequest.getPassword()
            ));
        } catch (BadCredentialsException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디 또는 비밀번호가 일치하지 않습니다.");
        }
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails.getUsername(), jwtUtil.getMobileExpirationTime());

        // 모바일 클라이언트에게 JWT(Access Token)를 응답 본문에 포함하여 반환
        return ResponseEntity.ok(new MobileAuthResponse(
                jwt, userDetails.getUsername(), "로그인(모바일) 성공"
        ));
    }

    /**
     * 웹 로그아웃 API (쿠키 기반 요청 및 문자열 응답)
     * 웹에서 쿠키 기반의 요청에 대해 문자열 응답을 보내고, 쿠키를 제거
     * 클라이언트는 요청 시 Access Token을 쿠키에 담아 전송
     */
    @PostMapping("/web-logout")
    public ResponseEntity<?> webLogout(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String jwt = null;

        // 쿠키에서 JWT 추출 (웹 브라우저의 자동 쿠키 전송)
        if (jwt == null && request.getCookies() != null) {
            jwt = Arrays.stream(request.getCookies())
                    .filter(cookie -> "jwtToken".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }
        performLogout(jwt, response);  // 공통 로그아웃 로직 호출

        // Spring Security의 Context에서 현재 인증 정보를 클리어
        SecurityContextHolder.clearContext();
        // 웹 클라이언트를 위해 텍스트 응답
        return ResponseEntity.ok().body("로그아웃(웹) 성공");
    }

    /**
     * 모바일 로그아웃 API (Authorization 헤더 기반 요청 및 JSON 응답)
     * 모바일에서 Authorization 헤더 기반의 요청에 대해 JSON 응답을 보내고, Access Token을 블랙리스트 처리
     * 클라이언트는 요청 시 Access Token을 Authorization 헤더에 담아 전송
     */
    @PostMapping(value = "/mobile-logout", produces = MediaType.APPLICATION_JSON_VALUE)  // JSON 응답 강제
    public ResponseEntity<Map<String, String>> mobileLogout(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String jwt = null;

        // Authorization 헤더에서 JWT 추출 (모바일에서 명시적으로 보낸 경우)
        final String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
        }
        performLogout(jwt, response);  // 공통 로그아웃 로직 호출

        // 모바일 클라이언트를 위해 JSON 응답
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("username", SecurityContextHolder.getContext().getAuthentication().getName());
        responseMap.put("message", "로그아웃(모바일) 성공");

        // Spring Security의 Context에서 현재 인증 정보를 클리어
        SecurityContextHolder.clearContext();
//        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(responseMap);
        return ResponseEntity.ok(responseMap);
    }

    /**
     * 로그아웃 시 JWT 쿠키 제거 및 블랙리스트 처리 공통 로직
     */
    private void performLogout(String jwt, HttpServletResponse response) throws Exception {
        // JWT가 존재하면 검증 시작
        if (jwt != null) {
            try {
                Date expiration = jwtUtil.extractExpiration(jwt);
                // JWT를 블랙리스트에 추가하여 즉시 무효화 (모바일/웹 토큰 모두 해당)
//                jwtBlacklistService.addTokenToBlacklist(jwt, expiration.getTime());  // 간단한 인메모리 캐시를 사용
                jwtBlacklistService.addTokenToBlacklist(jwt, expiration);  // MySQL DB 기반 영속적인 방식
            } catch (Exception e) {
                // 토큰 추출 실패 또는 이미 만료된 토큰인 경우에도 로그아웃 처리 진행 (블랙리스트에 추가는 불필요)
//                System.out.println("로그아웃 처리 중 토큰 오류: " + e.getMessage());
                log.warn("JWT token error during logout processing: {}", e.getMessage());
            }
        }
        // 웹 클라이언트를 위해 JWT 쿠키 제거 (브라우저에서 해당 쿠키를 더 이상 전송하지 않음)
        Cookie jwtCookie = new Cookie("jwtToken", "");  // 빈 값으로 설정
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);  // 유효기간을 0으로 설정하여 즉시 만료
//        jwtCookie.setSecure(true);
        response.addCookie(jwtCookie);
    }

    /**
     * JWT Access Token 유효성 검사 (모바일 앱에서 세션만료 여부 확인 용도)
     * /api/auth/check-token 엔드포인트에 성공적으로 접근 -> Access Token이 유효함
     */
    @GetMapping("/check-token")
    public ResponseEntity<Map<String, String>> checkTokenValidity() {
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("username", SecurityContextHolder.getContext().getAuthentication().getName());
        responseMap.put("message", "Access Token이 유효합니다.");

//        return ResponseEntity.ok(responseMap);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(responseMap);
    }

    // 에러 테스트용 API 예시 (500 Internal_Server_Error 유발)
    @GetMapping("/test-error")
    public ResponseEntity<String> testError() {
        // 의도적으로 NullPointerException을 발생시켜 500 에러를 유발
        String test = null;
        test.length();
        return ResponseEntity.ok("NullPointerException에 의한 500 Internal_Server_Error");
    }

    // ADMIN 권한만 접근 가능한 API 예시 (403 Forbidden 테스트용)
    @GetMapping("/admin-only")
    public ResponseEntity<String> adminOnlyEndpoint() {
        return ResponseEntity.ok("관리자만 접근 가능한 정보입니다!");
    }

    @Data
    static class AuthenticationRequest {  // 로그인 요청 DTO (웹/모바일 공용)
        private String username;
        private String password;
    }

    // WebAuthResponse 클래스 필요 없음 (JWT를 body로 보내지 않으므로)
    @Data
    static class WebAuthResponse {  // 웹용 로그인 응답 DTO
        private final String jwt;
    }

    @Data
    static class MobileAuthResponse {  // 모바일용 로그인 응답 DTO
        private final String accessToken;
        private final String username;
        private final String message;
    }
}
