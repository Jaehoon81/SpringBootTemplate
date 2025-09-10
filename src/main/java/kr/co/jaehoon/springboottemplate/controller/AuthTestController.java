package kr.co.jaehoon.springboottemplate.controller;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class AuthTestController {

//    private final Logger log = LoggerFactory.getLogger(this.getClass());

    // 의도적으로 에러를 발생시키는 페이지 (500 Internal_Server_Error 유발)
    @GetMapping("/trigger-error-page")
    public String triggerErrorPage() {
        throw new RuntimeException("의도적으로 발생시킨 에러입니다!");
    }

    // ADMIN 권한만 접근 가능한 페이지 (403 Forbidden 테스트용)
    @GetMapping("/admin-page")
    public String adminPage() {
        return "admin_page";
    }

    // 에러 테스트용 API 예시 (500 Internal_Server_Error 유발)
    @GetMapping("/api/auth/test-error")
    public ResponseEntity<String> testError() {
        // 의도적으로 NullPointerException을 발생시켜 500 에러를 유발
        String test = null;
        test.length();
        return ResponseEntity.ok("NullPointerException에 의한 500 Internal_Server_Error");
    }

    // ADMIN 권한만 접근 가능한 API 예시 (403 Forbidden 테스트용)
    @GetMapping("/api/auth/admin-only")
    public ResponseEntity<String> adminOnlyEndpoint() {
        return ResponseEntity.ok("관리자만 접근 가능한 정보입니다!");
    }
}
