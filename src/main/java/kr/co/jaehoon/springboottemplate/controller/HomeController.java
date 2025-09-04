package kr.co.jaehoon.springboottemplate.controller;

import kr.co.jaehoon.springboottemplate.dto.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class HomeController {

//    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/secure-page")  // 보호된 페이지
    public String securePage(@AuthenticationPrincipal CustomUserDetails customUserDetails, Model model) {
        if (customUserDetails != null) {
            log.debug("Secure-Page_UserDetails: {}", customUserDetails.getAuthorities().toString());

            model.addAttribute("displayName", customUserDetails.getDisplayname());
            model.addAttribute("userRole", customUserDetails.getUser().getRole());
        }
        return "secure_page";
    }

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
}
