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
}
