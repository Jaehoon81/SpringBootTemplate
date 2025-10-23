package kr.co.jaehoon.springboottemplate.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.jaehoon.springboottemplate.dto.CustomUserDetails;
import kr.co.jaehoon.springboottemplate.dto.RecordDTO;
import kr.co.jaehoon.springboottemplate.dto.network.BasicResponse;
import kr.co.jaehoon.springboottemplate.dto.network.ErrorResponse;
import kr.co.jaehoon.springboottemplate.dto.network.RecordResponse;
import kr.co.jaehoon.springboottemplate.dto.validation.RecordRequest;
import kr.co.jaehoon.springboottemplate.service.RecordCrudService;
import kr.co.jaehoon.springboottemplate.service.RecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Records API", description = "음성녹음 파일 관리 API")
@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
@Slf4j
public class RecordRestController {

//    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${file.upload-dir}")  // 파일 업로드 디렉토리
    private String uploadDir;

    private final RecordCrudService recordCrudService;
    private final RecordService recordService;

    /**
     * 모바일 앱으로부터 음성녹음 데이터를 수신하여 저장
     * @param currentUser   현재 인증된(=로그인한) 사용자 정보 (JWT로부터 얻음)
     * @param request       모바일 앱으로부터 수신받은 음성녹음 데이터 (JSON)
     * @param bindingResult 유효성 검사 결과
     * @return 저장된 음성녹음 정보 또는 유효성 검사 오류
     */
    @Operation(summary = "음성녹음 파일 업로드",
            description = "모바일 앱에서 Base64 인코딩된 음성녹음 파일을 전송하여 저장합니다. 순서는 1번부터 순차적으로 업로드되어야 합니다.",
            parameters = {
                    @Parameter(name = "Authorization", description = "JWT 토큰 (Bearer)", required = true, example = "Bearer <token>") })
    @ApiResponse(responseCode = "200",
            description = "음성녹음 업로드/업데이트 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RecordResponse.class)))
    @ApiResponse(responseCode = "400",
            description = "잘못된 요청, 유효성 검사 실패 또는 순서 오류",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패 (JWT 토큰 누락 또는 유효하지 않음)")
    @ApiResponse(responseCode = "403", description = "접근 거부")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    @PostMapping("/upload")
    public ResponseEntity<?> uploadAudioRecord(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody RecordRequest request,
            BindingResult bindingResult
    ) throws AccessDeniedException, IllegalArgumentException, IOException, Exception {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream().collect(Collectors.toMap(
                    fieldError -> fieldError.getField(), fieldError -> fieldError.getDefaultMessage()
            ));
            log.warn("Records-Upload_BindingResult_InvalidException: {}", errors.toString());
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
            // 성공적으로 업로드되면 200 OK 상태 코드와 함께 저장된 RecordResponse 객체 정보를 반환
            RecordDTO savedRecord = recordCrudService.uploadAudioRecord(request, currentUser.getUser().getId());
            return ResponseEntity.status(HttpStatus.OK).body(BasicResponse.success(
                    RecordResponse.from(savedRecord, "음성녹음 업로드/업데이트 성공")
            ));
        } catch (AccessDeniedException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("general", "참가자 소유권 확인 중 오류가 발생했습니다: " + e.getMessage());
            log.warn("Records-Upload_General_InvalidException(1): {}", errors.toString());
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errors);

            final String exceptionMessage = errors.get("general");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(BasicResponse.failure(
                    ErrorResponse.from(403, "Forbidden", "접근 권한이 없습니다.", exceptionMessage)
            ));
        } catch (IllegalArgumentException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("general", "녹음순서 유효성 검증 중 오류가 발생했습니다: " + e.getMessage());
            log.warn("Records-Upload_General_InvalidException(2): {}", errors.toString());
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);

            final String exceptionMessage = errors.get("general");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(BasicResponse.failure(
                    ErrorResponse.from(400, "Bad_Request", "잘못된 요청입니다.", exceptionMessage)
            ));
        } catch (IOException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("general", "파일 저장 중 오류가 발생했습니다: " + e.getMessage());
            log.error("Records-Upload_General_InvalidException(3): {}", errors.toString());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errors);

            final String exceptionMessage = errors.get("general");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BasicResponse.failure(
                    ErrorResponse.from(500, "Internal_Server_Error", "서버 오류가 발생했습니다.", exceptionMessage)
            ));
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("general", "음성녹음 업로드 중 오류가 발생했습니다: " + e.getMessage());
            log.error("Records-Upload_General_InvalidException(4): {}", errors.toString());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errors);

            final String exceptionMessage = errors.get("general");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BasicResponse.failure(
                    ErrorResponse.from(500, "Internal_Server_Error", "서버 오류가 발생했습니다.", exceptionMessage)
            ));
        }
    }

    /**
     * 음성녹음 파일을 스트리밍 방식으로 제공하는 API (웹 브라우저용)
     * ID를 통해 저장된 음성녹음 파일을 스트리밍 형태로 반환 (직접 재생 가능)
     * @param recordId 저장된 음성녹음 식별자(=record_id)
     * @return 웹 브라우저에 응답할 파일 스트림
     */
    @GetMapping("/play/{recordId}")
    public ResponseEntity<Resource> streamAudioRecord(@PathVariable Long recordId) throws MalformedURLException, IOException, Exception {
        try {
            RecordDTO record = recordService.findByRecordId(recordId).orElseThrow(
                    () -> new FileNotFoundException("Record not found for ID: " + recordId)
            );
            Path filePath = Paths.get(record.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(record.getMimeType()));
                headers.setContentDispositionFormData("attachment", resource.getFilename());  // attachment 대신 inline도 사용 가능
                // Content-Disposition을 inline으로 설정하면 웹 브라우저가 다운로드 대신 바로 재생을 시도
                headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"");

                return ResponseEntity.ok().headers(headers).body(resource);
            } else {
                throw new FileNotFoundException("File not found: " + filePath.toString());
            }
        } catch (FileNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
