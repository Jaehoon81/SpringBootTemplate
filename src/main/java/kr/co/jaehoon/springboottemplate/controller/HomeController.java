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
        return "auth/secure_page";
    }

    @GetMapping("/system-page")  // SYSTEM 권한만 접근 가능한 페이지
    public String systemPage(@AuthenticationPrincipal CustomUserDetails customUserDetails, Model model) {
        if (customUserDetails != null) {
            log.debug("System-Page_UserDetails: {}", customUserDetails.getAuthorities().toString());

            model.addAttribute("displayName", customUserDetails.getDisplayname());
            model.addAttribute("userRole", customUserDetails.getUser().getRole());
            // 추가 데이터 로드는 JavaScript에서 AJAX로 처리
        }
        return "auth/system_page";
    }

    @GetMapping("/admin-page")  // ADMIN 권한만 접근 가능한 페이지
    public String adminPage(@AuthenticationPrincipal CustomUserDetails customUserDetails, Model model) {
        if (customUserDetails != null) {
            log.debug("Admin-Page_UserDetails: {}", customUserDetails.getAuthorities().toString());

            model.addAttribute("displayName", customUserDetails.getDisplayname());
            model.addAttribute("userRole", customUserDetails.getUser().getRole());
            // 추가 데이터 로드는 JavaScript에서 AJAX로 처리
        }
        return "auth/admin_page";
    }
}
