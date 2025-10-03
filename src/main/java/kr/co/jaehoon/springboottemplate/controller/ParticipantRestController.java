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
import kr.co.jaehoon.springboottemplate.dto.validation.ParticipantRequest;
import kr.co.jaehoon.springboottemplate.service.ParticipantCrudService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            description = "잘못된 요청",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패 (JWT 토큰 누락 또는 유효하지 않음)")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    @PostMapping
    public ResponseEntity<?> registerParticipant(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody ParticipantRequest request,
            BindingResult bindingResult
    ) throws Exception {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream().collect(Collectors.toMap(
                    fieldError -> fieldError.getField(), fieldError -> fieldError.getDefaultMessage()
            ));
            log.warn("Participants_BindingResult_InvalidException: {}", errors.toString());
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
            log.error("Participants_General_InvalidException: {}", errors.toString());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errors);

            final String exceptionMessage = errors.get("general");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BasicResponse.failure(
                    ErrorResponse.from(500, "Internal_Server_Error", "서버 오류가 발생했습니다.", exceptionMessage)
            ));
        }
    }
}
