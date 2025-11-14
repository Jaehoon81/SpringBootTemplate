package kr.co.jaehoon.springboottemplate.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import kr.co.jaehoon.springboottemplate.dto.CustomUserDetails;
import kr.co.jaehoon.springboottemplate.dto.LoginApprovalDTO;
import kr.co.jaehoon.springboottemplate.repository.UserRepository;
import kr.co.jaehoon.springboottemplate.repository.dao.UserDAO;
import kr.co.jaehoon.springboottemplate.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminRestController {

//    private final Logger log = LoggerFactory.getLogger(this.getClass());

//    private final UserDAO userDAO;
//    private final UserRepository userRepository;
    private final UserService userService;

    /**
     * 로그인한 ADMIN 계정이 담당하는 승인 대기 중인 USER 계정의 목록을 반환
     * ADMIN 권한을 가진 사용자만 접근 가능함 (SecurityConfig에서 경로 레벨로 보호)
     * 또한, @PreAuthorize 어노테이션으로 메서드 레벨의 추가적인 보안 계층을 제공
     */
    @GetMapping("/pending-users")
    @PreAuthorize("hasRole('ADMIN')")  // ADMIN 권한 필요
    public ResponseEntity<List<LoginApprovalDTO>> getPendingUsers(
            @AuthenticationPrincipal CustomUserDetails adminUserDetails
    ) throws Exception {
        if (adminUserDetails == null || adminUserDetails.getUser() == null
                || !"ADMIN".equalsIgnoreCase(adminUserDetails.getUser().getRolename())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);  // ADMIN 권한이 아닌 경우 403 Forbidden 반환
        }
        // 로그인한 ADMIN 계정의 id를 사용하여 담당하는 승인 대기 중인 USER 계정의 목록을 조회
        Long adminId = adminUserDetails.getUser().getId();
        List<LoginApprovalDTO> pendingUsers = userService.findPendingUsersByAdminName(adminId);
        log.debug("Admin-PendingUsers_PendingUserList: {}", pendingUsers.toString());

        return ResponseEntity.ok(pendingUsers);
    }

    /**
     * 특정 USER 계정을 승인 처리
     * ADMIN 권한을 가진 사용자만 접근 가능함 (SecurityConfig에서 경로 레벨로 보호)
     * 또한, @PreAuthorize 어노테이션으로 메서드 레벨의 추가적인 보안 계층을 제공
     */
    @PostMapping("/approve-user")
    @PreAuthorize("hasRole('ADMIN')")  // ADMIN 권한 필요
    public ResponseEntity<String> approveUser(
            @RequestBody UserApprovalRequest request, @AuthenticationPrincipal CustomUserDetails adminUserDetails
    ) throws Exception {
        if (adminUserDetails == null || adminUserDetails.getUser() == null
                || !"ADMIN".equalsIgnoreCase(adminUserDetails.getUser().getRolename())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("접근 권한이 없습니다.");  // ADMIN 권한이 아닌 경우 403 Forbidden 반환
        }
        if (request.getUserId() == null) {
            return ResponseEntity.badRequest().body("USER ID가 필요합니다.");
        }
        try {
            userService.updateApprovalStatus(request.getUserId(), true);  // true로 승인
            return ResponseEntity.ok("USER 계정이 성공적으로 승인되었습니다.");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("계정승인 중 오류 발생: " + e.getMessage());
        }
    }

    @Data
    static class UserApprovalRequest {
        @JsonProperty("userId") private Long userId;
    }
}
