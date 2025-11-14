package kr.co.jaehoon.springboottemplate.controller;

import kr.co.jaehoon.springboottemplate.dto.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@Slf4j
public class HomeController {

//    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @GetMapping("/")
    public String home() {
        return "login";
    }

    // 대시보드 페이지 (메인 레이아웃)
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails customUserDetails, Model model) {
        if (customUserDetails != null) {
            log.debug("Dashboard_UserDetails: {}", customUserDetails.getAuthorities().toString());

            model.addAttribute("displayName", customUserDetails.getDisplayname());
            model.addAttribute("userRole", customUserDetails.getUser().getRolename());
            model.addAttribute("profilePicturePath", customUserDetails.getUser().getProfilePicturePath());
        }
        return "dashboard";
    }

    // 동적 콘텐츠 로드를 위한 엔드포인트
    @GetMapping("/contents/{contentName}")
    public String getContent(
            @PathVariable String contentName, @AuthenticationPrincipal CustomUserDetails customUserDetails, Model model
    ) {
        if (customUserDetails != null) {
            log.debug("Contents/{}_UserDetails: {}", contentName.toUpperCase(), customUserDetails.getAuthorities().toString());

            model.addAttribute("displayName", customUserDetails.getDisplayname());
            model.addAttribute("userRole", customUserDetails.getUser().getRolename());
        }
        // 요청된 콘텐츠 이름에 따라 해당하는 JSP 파일의 경로를 반환
        // (예: /contents/approval 요청 시 contents/secure_content.jsp 등을 반환)
        switch (contentName) {
            case "approval":
                // '사용자 관리' 메뉴 선택 시 권한(역할)에 따라 다른 콘텐츠를 반환
                if (customUserDetails != null) {
                    if ("SYSTEM".equalsIgnoreCase(customUserDetails.getUser().getRolename())) {
                        return "contents/system_content";
                    } else if ("ADMIN".equalsIgnoreCase(customUserDetails.getUser().getRolename())) {
                        return "contents/admin_content";
                    }
                }
                // USER 계정 또는 기타 권한 시
                return "contents/secure_content";
            case "system-approval":  // SYSTEM 권한 전용의 계정승인 콘텐츠
                return "contents/system_content";
            case "admin-approval":   // ADMIN 권한 전용의 계정승인 콘텐츠
                return "contents/admin_content";
            case "secure":           // 일반 사용자 보안 페이지 콘텐츠
                return "contents/secure_content";
            case "statistics":       // 데이터 통계 페이지 콘텐츠
                return "contents/statistics_content";
            case "notice":           // 데이터 통계 - 공지사항 페이지 콘텐츠
                return "contents/notice_content";
            case "profile":          // 마이 페이지 콘텐츠
                return "contents/profile_content";
            default:                 // 해당하는 콘텐츠가 없을 경우 대시보드로 리다이렉트
                return "redirect:/dashboard";
        }
    }

    @GetMapping("/secure-page")  // 보호된 페이지 (더이상 사용하지 않음)
    public String securePage(@AuthenticationPrincipal CustomUserDetails customUserDetails, Model model) {
        if (customUserDetails != null) {
            log.debug("SecurePage_UserDetails: {}", customUserDetails.getAuthorities().toString());

            model.addAttribute("displayName", customUserDetails.getDisplayname());
            model.addAttribute("userRole", customUserDetails.getUser().getRolename());
        }
        return "contents/secure_page";
    }

    @GetMapping("/system-page")  // SYSTEM 권한만 접근 가능한 페이지 (사용하지 않음)
    public String systemPage(@AuthenticationPrincipal CustomUserDetails customUserDetails, Model model) {
        if (customUserDetails != null) {
            log.debug("SystemPage_UserDetails: {}", customUserDetails.getAuthorities().toString());

            model.addAttribute("displayName", customUserDetails.getDisplayname());
            model.addAttribute("userRole", customUserDetails.getUser().getRolename());
            // 추가 데이터 로드는 JavaScript에서 AJAX로 처리
        }
        return "contents/system_page";
    }

    @GetMapping("/admin-page")  // ADMIN 권한만 접근 가능한 페이지 (사용하지 않음)
    public String adminPage(@AuthenticationPrincipal CustomUserDetails customUserDetails, Model model) {
        if (customUserDetails != null) {
            log.debug("AdminPage_UserDetails: {}", customUserDetails.getAuthorities().toString());

            model.addAttribute("displayName", customUserDetails.getDisplayname());
            model.addAttribute("userRole", customUserDetails.getUser().getRolename());
            // 추가 데이터 로드는 JavaScript에서 AJAX로 처리
        }
        return "contents/admin_page";
    }
}
