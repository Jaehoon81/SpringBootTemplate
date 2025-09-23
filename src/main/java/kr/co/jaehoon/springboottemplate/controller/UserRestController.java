package kr.co.jaehoon.springboottemplate.controller;

import kr.co.jaehoon.springboottemplate.dto.CustomUserDetails;
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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserRestController {

//    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${file.upload-dir}")  // 파일 업로드 디렉토리
    private String uploadDir;

//    private final UserRepository userRepository;
    private final UserService userService;

    // 프로필 사진(이미지) 업로드
    @PostMapping("/profile-picture")
    public ResponseEntity<String> uploadProfilePicture(
            @AuthenticationPrincipal CustomUserDetails currentUser, @RequestParam("file") MultipartFile file
    ) throws Exception {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
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

            // 사용자별 디렉토리 생성 (예: upload/profiles/{userId}/)
//            Path userUploadPath = Paths.get(uploadDir, "profiles", String.valueOf(userId));
            // 사용자별 디렉토리 생성 (예: upload/profiles/{username}/)
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 업로드에 실패했습니다.");
        }
    }

    // 프로필 사진(이미지) 조회
    // (클라이언트에서 <img src="/profiles/{userId}/{filename}"/> 요청 시 처리)
//    @GetMapping("/profile-picture/{userId}/{filename:.+}")
//    public ResponseEntity<Resource> serveProfilePicture(@PathVariable Long userId, @PathVariable String filename) throws Exception {
    // (클라이언트에서 <img src="/api/user/profile-picture/{username}/{filename}"/> 요청 시 처리)
    @GetMapping("/profile-picture/{username}/{filename:.+}")
    public ResponseEntity<Resource> serveProfilePicture(@PathVariable String username, @PathVariable String filename) throws Exception {
        try {
            // 실제 파일이 저장된 물리적 경로를 구성
//            Path filePath = Paths.get(uploadDir, "profiles", String.valueOf(userId), filename);
            Path filePath = Paths.get(uploadDir, "profiles", String.valueOf(username), filename);
            Resource resource = new FileSystemResource(filePath.toFile());
            if (resource.exists() && resource.isReadable()) {
                // 이미지 파일의 Content-Type 설정
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;  // 기본값
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
}
