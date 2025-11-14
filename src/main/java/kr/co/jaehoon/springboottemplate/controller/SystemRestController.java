package kr.co.jaehoon.springboottemplate.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
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
import java.util.Map;

@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
@Slf4j
public class SystemRestController {

//    private final Logger log = LoggerFactory.getLogger(this.getClass());

//    private final UserDAO userDAO;
//    private final UserRepository userRepository;
    private final UserService userService;

    private final ObjectMapper objectMapper;

    /**
     * 승인 대기 중인 ADMIN 계정의 목록을 반환
     * SYSTEM 권한을 가진 사용자만 접근 가능함 (SecurityConfig에서 경로 레벨로 보호)
     * 또한, @PreAuthorize 어노테이션으로 메서드 레벨의 추가적인 보안 계층을 제공
     */
//    @GetMapping("/pending-admins")
//    @PreAuthorize("hasRole('SYSTEM')")  // SYSTEM 권한 필요
//    public ResponseEntity<List<LoginApprovalDTO>> getPendingAdmins() throws Exception {
//        List<Map<String, Object>> pendingAdmins = userService.findPendingAdmins();
//        log.debug("System-PendingAdmins_PendingAdminList: {}", pendingAdmins.toString());
//
//        // DTO에 없는 필드는 무시하도록 설정 (선택 사항)
//        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//        // PropertyNamingStrategy 설정: 스네이크 케이스(snake_case) 키(key)를 카멜 케이스(camelCase) 필드명에 매핑
//        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
//
//        List<LoginApprovalDTO> loginApprovals = List.of();
//        try {
//            // List<Map<String, Object>>을 List<LoginApprovalDTO>로 변환
//            // TypeReference<List<LoginApprovalDTO>>() {}: 익명 클래스를 사용하여 타입 정보를 전달
//            loginApprovals = objectMapper.convertValue(pendingAdmins, new TypeReference<List<LoginApprovalDTO>>() {});
//            log.debug("System-PendingAdmins_LoginApprovalList: {}", loginApprovals.toString());
//        } catch (IllegalArgumentException e) {
//            // List<Map>에서 List<DTO>로 변환하는데 실패하더라도 빈 리스트(List.of())를 반환
////            System.out.println("List<Map>에서 List<DTO>로 변환 중 오류 발생: " + e.getMessage());
//            log.warn("Error converting from List<Map> to List<DTO>: {}", e.getMessage());
//        }
//        return ResponseEntity.ok(loginApprovals);
//    }
    @GetMapping("/pending-admins")
    @PreAuthorize("hasRole('SYSTEM')")  // SYSTEM 권한 필요
    public ResponseEntity<List<LoginApprovalDTO>> getPendingAdmins(
            @AuthenticationPrincipal CustomUserDetails adminUserDetails
    ) throws Exception {
        if (adminUserDetails == null || adminUserDetails.getUser() == null
                || !"SYSTEM".equalsIgnoreCase(adminUserDetails.getUser().getRolename())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);  // SYSTEM 권한이 아닌 경우 403 Forbidden 반환
        }
        // 로그인한 SYSTEM 계정이 담당하는 승인 대기 중인 ADMIN 계정의 목록을 조회
        List<LoginApprovalDTO> pendingAdmins = userService.findPendingAdmins();
        log.debug("System-PendingAdmins_PendingAdminList: {}", pendingAdmins.toString());

        return ResponseEntity.ok(pendingAdmins);
    }

    /**
     * 특정 ADMIN 계정을 승인 처리
     * SYSTEM 권한을 가진 사용자만 접근 가능함 (SecurityConfig에서 경로 레벨로 보호)
     * 또한, @PreAuthorize 어노테이션으로 메서드 레벨의 추가적인 보안 계층을 제공
     */
    @PostMapping("/approve-admin")
    @PreAuthorize("hasRole('SYSTEM')")  // SYSTEM 권한 필요
    public ResponseEntity<String> approveAdmin(
            @RequestBody AdminApprovalRequest request, @AuthenticationPrincipal CustomUserDetails adminUserDetails
    ) throws Exception {
        if (adminUserDetails == null || adminUserDetails.getUser() == null
                || !"SYSTEM".equalsIgnoreCase(adminUserDetails.getUser().getRolename())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("접근 권한이 없습니다.");  // SYSTEM 권한이 아닌 경우 403 Forbidden 반환
        }
        if (request.getAdminId() == null) {
            return ResponseEntity.badRequest().body("ADMIN ID가 필요합니다.");
        }
        try {
            userService.updateApprovalStatus(request.getAdminId(), true);  // true로 승인
            return ResponseEntity.ok("ADMIN 계정이 성공적으로 승인되었습니다.");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("계정승인 중 오류 발생: " + e.getMessage());
        }
    }

    @Data
    static class AdminApprovalRequest {
        @JsonProperty("adminId") private Long adminId;
    }
}
