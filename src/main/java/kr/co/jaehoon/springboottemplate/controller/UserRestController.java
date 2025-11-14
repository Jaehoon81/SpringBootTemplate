package kr.co.jaehoon.springboottemplate.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kr.co.jaehoon.springboottemplate.dto.CustomUserDetails;
import kr.co.jaehoon.springboottemplate.dto.UserDTO;
import kr.co.jaehoon.springboottemplate.dto.validation.PasswordChangeGroup;
import kr.co.jaehoon.springboottemplate.dto.validation.UserUpdateRequest;
import kr.co.jaehoon.springboottemplate.repository.UserRepository;
import kr.co.jaehoon.springboottemplate.repository.dao.UserDAO;
import kr.co.jaehoon.springboottemplate.security.JwtBlacklistService;
import kr.co.jaehoon.springboottemplate.security.JwtUtil;
import kr.co.jaehoon.springboottemplate.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserRestController {

//    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${file.upload-dir}")  // 파일 업로드 디렉토리
    private String uploadDir;

    private final JwtUtil jwtUtil;
    private final JwtBlacklistService jwtBlacklistService;

//    private final UserDAO userDAO;
//    private final UserRepository userRepository;
    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    // 프로필 사진(이미지) 업로드
    @PostMapping("/profile-picture")
    public ResponseEntity<String> uploadProfilePicture(
            @AuthenticationPrincipal CustomUserDetails currentUser, @RequestParam("file") MultipartFile file
    ) throws Exception {
        if (currentUser == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증된 사용자 정보가 없습니다.");
        }
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("업로드할 파일이 없습니다.");
        }
        try {
            Long userId = currentUser.getUser().getId();
            String username = currentUser.getUser().getUsername();
            String originalFilename = file.getOriginalFilename();
            String fileExtension = Optional.ofNullable(originalFilename)
                    .filter(f -> f.contains("."))
                    .map(f -> f.substring(f.lastIndexOf(".") + 1))
                    .orElse("");

            // 사용자별 디렉토리 생성 (예: ./uploads/profiles/{userId}/)
//            Path userUploadPath = Paths.get(uploadDir, "profiles", String.valueOf(userId));
            // 사용자별 디렉토리 생성 (예: ./uploads/profiles/{username}/)
            Path userUploadPath = Paths.get(uploadDir, "profiles", String.valueOf(username));
            Files.createDirectories(userUploadPath);  // 디렉토리가 없으면 생성
            // 고유한 파일명 생성 (UUID)
            String fileName = UUID.randomUUID().toString() + "." + fileExtension;
            Path filePath = userUploadPath.resolve(fileName);
            // 파일 저장
            file.transferTo(filePath.toFile());
            // DB users 테이블의 profile_picture_path 컬럼에 파일 경로를 저장
            // (예: '/profiles/{userId}/{fileName}' 형태로 저장하여 나중에 GET 요청 시 사용)
//            String storedPath = "/profiles/" + userId + "/" + fileName;
            // DB users 테이블의 profile_picture_path 컬럼에 프로필 사진(이미지)을 조회하는 API 경로를 저장
            // (예: '/api/user/profile-picture/{username}/{fileName}' 형태로 저장하여 나중에 GET 요청 시 사용)
            String storedPath = "/api/user/profile-picture/" + username + "/" + fileName;
            userService.updateProfilePicturePath(userId, storedPath);

            return ResponseEntity.ok(storedPath);  // 저장된 경로 반환
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 업로드 중 오류 발생: " + e.getMessage());
        }
    }

    // 프로필 사진(이미지) 조회
    // (클라이언트에서 <img src="/profiles/{userId}/{filename}"/> 요청 시 처리)
//    @GetMapping("/profile-picture/{userId}/{filename:.+}")
//    public ResponseEntity<Resource> serveProfilePicture(
//            @PathVariable Long userId, @PathVariable String filename
//    ) throws Exception {
    // (클라이언트에서 <img src="/api/user/profile-picture/{username}/{filename}"/> 요청 시 처리)
    @GetMapping("/profile-picture/{username}/{filename:.+}")
    public ResponseEntity<Resource> serveProfilePicture(
            @PathVariable String username, @PathVariable String filename
    ) throws Exception {
        try {
            // 실제 파일이 저장된 물리적 경로를 구성
//            Path filePath = Paths.get(uploadDir, "profiles", String.valueOf(userId), filename);
            Path filePath = Paths.get(uploadDir, "profiles", String.valueOf(username), filename);
            Resource resource = new FileSystemResource(filePath.toFile());
            if (resource.exists() && resource.isReadable()) {
                // 이미지 파일의 Content-Type 설정
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;  // Content-Type 기본값
                }
                return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(resource);
            } else {
                // 파일이 없거나 읽을 수 없으면 404 Not_Found 에러 발생
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {  // FileNotFoundException 포함
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 현재 로그인한 사용자의 정보를 조회하는 API
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(@AuthenticationPrincipal CustomUserDetails currentUser) throws Exception {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserDTO user = userService.findUserById(currentUser.getUser().getId());
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        Map<String, Object> profile = new HashMap<>();
        profile.put("username", user.getUsername());
        profile.put("displayname", user.getDisplayname());
        profile.put("email", user.getEmail());
        // 비밀번호는 직접 노출하지 않음

        return ResponseEntity.ok(profile);
    }

    // 현재 로그인한 사용자의 정보를 업데이트하는 API
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Validated({ PasswordChangeGroup.class }) @RequestBody UserUpdateRequest userUpdateRequest,
            BindingResult bindingResult
    ) throws Exception {
        // 유효성 검사 그룹을 동적으로 적용하기 위해 @Validated 어노테이션과 함께 BindingResult를 사용
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream().collect(Collectors.toMap(
                    fieldError -> fieldError.getField(), fieldError -> fieldError.getDefaultMessage()
            ));
            log.warn("User-Profile_BindingResult_InvalidException: {}", errors.toString());
            return ResponseEntity.badRequest().body(errors);
        }
        if (currentUser == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증된 사용자 정보가 없습니다.");
        }
        UserDTO user = userService.findUserById(currentUser.getUser().getId());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자 정보를 찾을 수 없습니다.");
        }
        // 현재 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(userUpdateRequest.getCurrentPassword(), user.getPassword())) {
            Map<String, String> errors = new HashMap<>();
            errors.put("currentPassword", "현재 비밀번호가 일치하지 않습니다.");

            log.warn("User-Profile_CurrentPassword_InvalidException: {}", errors.toString());
            return ResponseEntity.badRequest().body(errors);
        }
        // 새 비밀번호가 입력된 경우에만 비밀번호 관련 유효성 검사 적용
        boolean isPasswordChangeRequested =
                (userUpdateRequest.getNewPassword() != null && !userUpdateRequest.getNewPassword().isEmpty());
        if (isPasswordChangeRequested == true) {
            // newPassword와 confirmPassword의 일치 여부를 클라이언트에서 확인하지만 서버에서도 다시 확인
            if (!userUpdateRequest.getNewPassword().equals(userUpdateRequest.getConfirmPassword())) {
                Map<String, String> errors = new HashMap<>();
                errors.put("confirmPassword", "새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");

                log.warn("User-Profile_ConfirmPassword_InvalidException: {}", errors.toString());
                return ResponseEntity.badRequest().body(errors);
            }
        }
        if (isPasswordChangeRequested == true) {
            // 새 비밀번호가 유효성 검사를 통과하고, 비밀번호 확인과 일치하면 인코딩 수행
            userUpdateRequest.setNewPassword(passwordEncoder.encode(userUpdateRequest.getNewPassword()));
        } else {
            // 비밀번호 변경 요청이 없으면 기존 비밀번호를 유지
            // (newPassword를 null로 설정하여 MyBatis(userRepoMapper.xml)에서 업데이트하지 않도록 함)
            userUpdateRequest.setNewPassword(null);
        }
        userUpdateRequest.setId(currentUser.getUser().getId());  // 현재 로그인된 사용자의 ID 설정
        userService.updateUser(userUpdateRequest);

        return ResponseEntity.ok("프로필 정보가 성공적으로 업데이트되었습니다.");
    }

    // 사용자의 탈퇴 상태를 업데이트하는 API (USER 계정만 가능)
    @PostMapping("/deactivate")
    public ResponseEntity<String> deactivateUser(
            @AuthenticationPrincipal CustomUserDetails currentUser, HttpServletRequest request, HttpServletResponse response
    ) throws Exception {
        if (currentUser == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증된 사용자 정보가 없습니다.");
        }
        // DB users 테이블의 is_deleted 컬럼을 true로 업데이트
        Long userId = currentUser.getUser().getId();
        userService.updateUserDeleteStatus(userId, true);

        String jwt = null;
        // 쿠키에서 JWT 추출 (웹 브라우저의 자동 쿠키 전송)
        if (jwt == null && request.getCookies() != null) {
            jwt = Arrays.stream(request.getCookies())
                    .filter(cookie -> "jwtToken".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }
        // JWT가 존재하면 검증 시작
        if (jwt != null) {
            try {
                Date expiration = jwtUtil.extractExpiration(jwt);
                // JWT를 블랙리스트에 추가하여 즉시 무효화 (여기서는 웹 토큰에 해당)
//                jwtBlacklistService.addTokenToBlacklist(jwt, expiration.getTime());  // 간단한 인메모리 캐시를 사용
                jwtBlacklistService.addTokenToBlacklist(jwt, expiration);  // MySQL DB 기반 영속적인 방식
            } catch (Exception e) {
                // 토큰 추출 실패 또는 이미 만료된 토큰인 경우에도 로그아웃 처리 진행 (블랙리스트에 추가는 불필요)
//                System.out.println("로그아웃 처리 중 토큰 오류: " + e.getMessage());
                log.warn("JWT token error during logout processing: {}", e.getMessage());
            }
        }
        // JWT 쿠키 삭제
        Cookie jwtCookie = new Cookie("jwtToken", "");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        jwtCookie.setSecure(true);
        response.addCookie(jwtCookie);

        // Spring Security의 Context에서 현재 인증 정보를 클리어
        SecurityContextHolder.clearContext();
        // 웹 클라이언트를 위해 텍스트 응답
        return ResponseEntity.ok("회원 탈퇴가 성공적으로 처리되었습니다.");
    }
}
