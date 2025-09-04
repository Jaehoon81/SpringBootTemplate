package kr.co.jaehoon.springboottemplate.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kr.co.jaehoon.springboottemplate.dto.RegistrationRequest;
import kr.co.jaehoon.springboottemplate.dto.UserDTO;
import kr.co.jaehoon.springboottemplate.repository.UserRepository;
import kr.co.jaehoon.springboottemplate.repository.dao.UserDAO;
import kr.co.jaehoon.springboottemplate.service.UserService;
import kr.co.jaehoon.springboottemplate.service.impl.UserDetailsServiceImpl;
import kr.co.jaehoon.springboottemplate.security.JwtUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class UserRestController {

//    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

//    private final UserDAO userDAO;
//    private final UserRepository userRepository;
    private final UserService userService;

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

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest, HttpServletResponse response) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authenticationRequest.getUsername(), authenticationRequest.getPassword()
            ));
        } catch (BadCredentialsException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디 또는 비밀번호가 일치하지 않습니다.");
        }
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails.getUsername());

        // JWT를 HttpOnly 쿠키로 추가
        Cookie jwtCookie = new Cookie("jwtToken", jwt);  // 쿠키 이름은 'jwtToken'
        jwtCookie.setHttpOnly(true);  // JavaScript에서 접근 불가
        jwtCookie.setPath("/");  // 모든 경로에서 유효하도록 설정
        jwtCookie.setMaxAge((int) (jwtUtil.getExpirationTime() / 1000));  // JWT 만료 시간과 동일하게 설정 (초 단위)
//        jwtCookie.setSecure(true);  // HTTPS 환경에서 추가
        response.addCookie(jwtCookie);

        // 프론트엔드에서 jwtToken을 body로 받을 필요 없으므로 빈 응답 반환 또는 성공 메시지만 반환
//        return ResponseEntity.ok(new AuthenticationResponse(jwt));
        return ResponseEntity.ok().body("로그인 성공");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) throws Exception {
        // 서버 측 로그아웃 엔드포인트 구현 (JWT 쿠키를 만료시켜 제거)
        Cookie jwtCookie = new Cookie("jwtToken", "");  // 빈 값으로 설정
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);  // 유효기간을 0으로 설정하여 즉시 만료
//        jwtCookie.setSecure(true);
        response.addCookie(jwtCookie);

        return ResponseEntity.ok().body("로그아웃 성공");
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
    static class AuthenticationRequest {  // 로그인 요청 DTO
        private String username;
        private String password;
    }

    // AuthenticationResponse 클래스 필요 없음 (JWT를 body로 보내지 않으므로)
    @Data
    static class AuthenticationResponse {  // 로그인 응답 DTO
        private final String jwt;
    }
}
