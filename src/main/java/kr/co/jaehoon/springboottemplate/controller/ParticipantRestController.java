package kr.co.jaehoon.springboottemplate.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.jaehoon.springboottemplate.dto.CustomUserDetails;
import kr.co.jaehoon.springboottemplate.dto.ParticipantDTO;
import kr.co.jaehoon.springboottemplate.dto.network.BasicResponse;
import kr.co.jaehoon.springboottemplate.dto.network.ErrorResponse;
import kr.co.jaehoon.springboottemplate.dto.network.ParticipantResponse;
import kr.co.jaehoon.springboottemplate.dto.validation.Grade;
import kr.co.jaehoon.springboottemplate.dto.validation.GradeUpdateRequest;
import kr.co.jaehoon.springboottemplate.dto.validation.ParticipantRequest;
import kr.co.jaehoon.springboottemplate.service.ParticipantCrudService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Participants API", description = "참가자 정보 관리 API")
@RestController
@RequestMapping("/api/participants")
@RequiredArgsConstructor
@Slf4j
public class ParticipantRestController {

//    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ParticipantCrudService participantCrudService;

    /**
     * 모바일 앱으로부터 참가자 데이터를 수신하여 저장
     * @param currentUser 현재 인증된(=로그인한) 사용자 정보 (JWT로부터 얻음)
     * @param request 모바일 앱으로부터 수신받은 참가자 데이터 (JSON)
     * @param bindingResult 유효성 검사 결과
     * @return 저장된 참가자 정보 또는 유효성 검사 오류
     */
    @Operation(summary = "새 참가자 등록",
            description = "모바일 앱을 통해 새로운 참가자 정보를 등록합니다.",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT 토큰 (Bearer)", required = true, example = "Bearer <token>") })
    @ApiResponse(responseCode = "201",
            description = "참가자 등록 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ParticipantResponse.class)))
    @ApiResponse(responseCode = "400",
            description = "잘못된 요청 또는 유효성 검사 실패",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패 (JWT 토큰 누락 또는 유효하지 않음)")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    @PostMapping("/register")
    public ResponseEntity<?> registerParticipant(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody ParticipantRequest request,
            BindingResult bindingResult
    ) throws Exception {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream().collect(Collectors.toMap(
                    fieldError -> fieldError.getField(), fieldError -> fieldError.getDefaultMessage()
            ));
            log.warn("Participants-Register_BindingResult_InvalidException: {}", errors.toString());
//            return ResponseEntity.badRequest().body(errors);

            List<String> errorList = (errors.values().stream().map(Object::toString)).toList();
            final String exceptionMessage = (!errorList.isEmpty()) ? errorList.get(errorList.size() - 1) : "잘못된 요청 또는 유효성 검사에 실패했습니다.";
            return ResponseEntity.badRequest().body(BasicResponse.failure(
                    ErrorResponse.from(400, "Bad_Request", "잘못된 요청입니다.", exceptionMessage)
            ));
        }
        if (currentUser == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            throw new BadCredentialsException("인증된 사용자 정보가 없습니다.");
        }
        try {
            // 성공적으로 등록되면 201 Created 상태 코드와 함께 저장된 ParticipantResponse 객체 정보를 반환
            ParticipantDTO newParticipant = participantCrudService.registerParticipant(currentUser.getUser().getId(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(BasicResponse.success(
                    ParticipantResponse.from(newParticipant, "참가자 등록 성공")
            ));
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("general", "참가자 등록 중 오류가 발생했습니다: " + e.getMessage());
            log.error("Participants-Register_General_InvalidException: {}", errors.toString());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errors);

            final String exceptionMessage = errors.get("general");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BasicResponse.failure(
                    ErrorResponse.from(500, "Internal_Server_Error", "서버 오류가 발생했습니다.", exceptionMessage)
            ));
        }
    }

    /**
     * 참가자 및 음성녹음 목록을 조회 (웹 브라우저용)
     * @param currentUser 현재 로그인한 사용자 정보
     * @param page 요청 페이지 번호 (1부터 시작)
     * @param size 한 페이지당 항목(셀) 수
     * @param grade 등급별 필터 (선택 사항)
     * @param startDate yyyy-MM-dd 형식의 시작일 (선택 사항)
     * @param endDate yyyy-MM-dd 형식의 종료일 (선택 사항)
     * @param searchKeyword 이름, 성별, 담당 관리자 대상의 검색어 (선택 사항)
     * @return 웹 브라우저에 응답할 현재 페이지 정보와 참가자 및 음성녹음 목록
     */
    @GetMapping("/paginated-list")
    public ResponseEntity<?> getPaginatedParticipantList(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,  // pageSize
            @RequestParam(required = false) Grade grade,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String searchKeyword
    ) throws AccessDeniedException, Exception {
        if (currentUser == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            throw new BadCredentialsException("인증된 사용자 정보가 없습니다.");
        }
        try {
            Long currentUserId = currentUser.getUser().getId();
            String rolename = currentUser.getUser().getRolename();

            // 권한(역할) 등에 따른 데이터 필터링은 ParticipantCrudService를 통해서 수행
            Map<String, Object> resultMap = participantCrudService.getPaginatedParticipantList(
                    page, size, grade, startDate, endDate, searchKeyword,
                    currentUserId, rolename
            );
            return ResponseEntity.ok(resultMap);
        } catch (AccessDeniedException e) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("해당 참가자 정보에 접근할 권한이 없습니다.");
            throw new AccessDeniedException("해당 참가자 정보에 접근할 권한이 없습니다.");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("참가자 목록 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 필터링된 모든 참가자 목록을 Excel로 다운로드 (웹 브라우저용)
     * @param currentUser 현재 로그인한 사용자 정보
     * @param grade 등급별 필터 (선택 사항)
     * @param startDate yyyy-MM-dd 형식의 시작일 (선택 사항)
     * @param endDate yyyy-MM-dd 형식의 종료일 (선택 사항)
     * @param searchKeyword 이름, 성별, 담당 관리자 대상의 검색어 (선택 사항)
     * @return 웹 브라우저에 응답할 다운로드된 Excel 파일 리소스(ByteArrayResource)
     */
    @GetMapping("/export-excel")
    public ResponseEntity<Resource> exportParticipantsToExcel(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(required = false) Grade grade,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String searchKeyword
    ) throws AccessDeniedException, IOException, Exception {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            Long currentUserId = currentUser.getUser().getId();
            String rolename = currentUser.getUser().getRolename();

            // 권한(역할) 등에 따른 데이터 필터링은 ParticipantCrudService를 통해서 수행
            ByteArrayOutputStream outputStream = participantCrudService.exportParticipantsToExcel(
                    grade, startDate, endDate, searchKeyword, currentUserId, rolename
            );
            // 파일명 생성
            String filename = String.format("%s_data-statistics-list_%s.xlsx",
                    currentUser.getUser().getUsername(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
            );
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);  // Generic Binary Stream
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");

            // application/vnd.openxmlformats-officedocument.spreadsheetml.sheet: Excel MIME Type
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(outputStream.size())
                    .body(new ByteArrayResource(outputStream.toByteArray()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                    .body(new ByteArrayResource(("필터링된 모든 참가자 정보에 접근할 권한이 없습니다: " + e.getMessage()).getBytes()));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                    .body(new ByteArrayResource(("Excel 파일 생성 중 오류가 발생했습니다: " + e.getMessage()).getBytes()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                    .body(new ByteArrayResource(("Excel 파일 다운로드 중 오류가 발생했습니다: " + e.getMessage()).getBytes()));
        }
    }

    /**
     * 특정 참가자의 음성녹음 상세 정보를 조회 (웹 브라우저용)
     * @param currentUser 현재 로그인한 사용자 정보
     * @param participantId 특정 참가자 고유 ID
     * @return 웹 브라우저에 응답할 특정 참가자 정보와 3개 음성녹음 데이터
     */
    @GetMapping("/{participantId}/record-details")
    public ResponseEntity<?> getParticipantRecordDetails(
            @AuthenticationPrincipal CustomUserDetails currentUser, @PathVariable Long participantId
    ) throws AccessDeniedException, IllegalArgumentException, Exception {
        if (currentUser == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            throw new BadCredentialsException("인증된 사용자 정보가 없습니다.");
        }
        try {
            Long currentUserId = currentUser.getUser().getId();
            String rolename = currentUser.getUser().getRolename();

            // 권한(역할)에 따른 데이터 필터링은 ParticipantCrudService를 통해서 수행
            Map<String, Object> resultMap =
                    participantCrudService.getParticipantRecordDetails(participantId, currentUserId, rolename);
            return ResponseEntity.ok(resultMap);
        } catch (AccessDeniedException e) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("해당 참가자 정보에 접근할 권한이 없습니다.");
            throw new AccessDeniedException("해당 참가자 정보에 접근할 권한이 없습니다.");
        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 참가자 정보를 찾을 수 없습니다.");
            throw new IllegalArgumentException("해당 참가자 정보를 찾을 수 없습니다.");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("참가자 음성녹음 상세 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 특정 참가자의 등급을 업데이트 (웹 브라우저용)
     * SYSTEM or ADMIN 권한만 등급 변경이 가능하도록 설정 (USER 권한은 등급 변경 불가능)
     * @param currentUser 현재 로그인한 사용자 정보
     * @param request 특정 참가자 정보 및 등급 데이터 (JSON)
     * @param bindingResult 유효성 검사 결과
     * @return 웹 브라우저에 응답할 성공 메시지 또는 유효성 검사 오류
     */
    @PutMapping("/grade")
    public ResponseEntity<?> updateParticipantGrade(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody GradeUpdateRequest request,
            BindingResult bindingResult
    ) throws AccessDeniedException, IllegalArgumentException, Exception {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream().collect(Collectors.toMap(
                    fieldError -> fieldError.getField(), fieldError -> fieldError.getDefaultMessage()
            ));
            log.warn("Participants-Grade_BindingResult_InvalidException: {}", errors.toString());
            return ResponseEntity.badRequest().body(errors);
        }
        if (currentUser == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            throw new BadCredentialsException("인증된 사용자 정보가 없습니다.");
        }
        String rolename = currentUser.getUser().getRolename();
        if (rolename.equals("USER")) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("USER 계정은 자신의 참가자 등급을 변경할 수 없습니다.");
            throw new AccessDeniedException("USER 계정은 자신의 참가자 등급을 변경할 수 없습니다.");
        }
        try {
            // 해당 참가자의 등급을 업데이트한 후 성공 메시지를 반환
            Long currentUserId = currentUser.getUser().getId();
            participantCrudService.updateParticipantGrade(request, currentUserId, rolename);
            return ResponseEntity.ok("참가자 등급이 성공적으로 변경되었습니다.");
        } catch (AccessDeniedException e) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("해당 참가자는 등급을 변경할 권한이 없습니다.");
            throw new AccessDeniedException("해당 참가자는 등급을 변경할 권한이 없습니다.");
        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 참가자 정보를 찾을 수 없습니다.");
            throw new IllegalArgumentException("해당 참가자 정보를 찾을 수 없습니다.");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("참가자 등급 변경 중 오류가 발생했습니다.");
        }
    }
}
