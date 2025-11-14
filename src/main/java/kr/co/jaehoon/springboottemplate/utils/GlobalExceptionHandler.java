package kr.co.jaehoon.springboottemplate.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.jaehoon.springboottemplate.dto.network.BasicResponse;
import kr.co.jaehoon.springboottemplate.dto.network.ErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
@RequiredArgsConstructor
//@Slf4j
public class GlobalExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ObjectMapper objectMapper;

    // @Valid 어노테이션에 의한 유효성 검사 실패 시 발생
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleValidationException(
            HttpServletRequest request, HttpServletResponse response, MethodArgumentNotValidException ex
    ) throws ServletException, IOException {
        // 1. 요청이 API 엔드포인트인지 판단 (/api/ 로 시작하는 URI)
        boolean isApiRequest = request.getRequestURI().startsWith("/api/");
        // 2. 클라이언트의 Accept 헤더를 명시적으로 검사하여 JSON을 원하는지 확인
        String acceptHeader = request.getHeader("Accept");
        boolean expectsJsonExplicitly = (acceptHeader != null && acceptHeader.contains(MediaType.APPLICATION_JSON_VALUE));
        // 3. AJAX 요청인지 확인 (웹에서 AJAX 호출 시 사용하는 X-Requested-With 헤더를 통한 판단)
        boolean isAjaxRequest = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
        // JSON 응답을 보내야 하는 조건:
        // - 명백하게 API 요청인 경우 (경로 기준)
        // - or 클라이언트가 명시적으로 JSON을 요청한 경우
        // - or 일반적인 AJAX 요청인 경우 (일반적으로 JSON 응답을 기대)
        boolean shouldSendJson = isApiRequest || expectsJsonExplicitly || isAjaxRequest;

        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
        log.warn("MethodArgumentNotValidException: {}", errors.toString());
//        return ResponseEntity.badRequest().body(errors);

        List<String> errorList = (errors.values().stream().map(Object::toString)).toList();
        final String exceptionMessage = (!errorList.isEmpty()) ? errorList.get(errorList.size() - 1) : "잘못된 요청 또는 유효성 검사에 실패했습니다.";
        if (shouldSendJson == true) {
            // JSON 응답을 기대하는 클라이언트 (모바일 앱, API 클라이언트 등)
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);     // 400 Bad_Request
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);  // Content-Type 설정
            response.setCharacterEncoding("UTF-8");                     // 한글 깨짐 방지

            if (expectsJsonExplicitly == false && isAjaxRequest == true) {
                // 웹 브라우저용 JSON 응답 작성
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionMessage);
            } else {  // expectsJsonExplicitly == true && isAjaxRequest == false
                // 모바일 앱용 BasicResponse 및 ErrorResponse로 JSON 응답 작성
                return ResponseEntity.badRequest().body(BasicResponse.failure(
                        ErrorResponse.from(400, "Bad_Request", "잘못된 요청입니다.", exceptionMessage)
                ));
            }
        } else {
            // 웹 브라우저에서 직접 접근(페이지 요청)인 경우:
            // 1. 400 HTML 응답(response.sendError()) 대신, 응답 상태 코드를 400으로 설정하고 /error 경로로 포워드
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            // 2. CustomErrorController에서 사용할 에러 속성들을 수동으로 설정
            request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute(RequestDispatcher.ERROR_MESSAGE, "Bad_Request");  // 발생한 예외 객체
            request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, new AccessDeniedException(exceptionMessage));  // 사용자에게 보여줄 메시지
            request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, request.getRequestURI());  // 원래 요청 URI
            // 3. Spring Boot의 기본 에러 핸들러(BasicErrorController)가 /error 요청을 처리하고 error.jsp를 렌더링
            request.getRequestDispatcher("/error").forward(request, response);

//            return (ResponseEntity<?>) errors;
            return null;
        }
    }

    // JSON 파싱 또는 타입변환 오류 시 발생
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleHttpMessageNotReadableException(
            HttpServletRequest request, HttpServletResponse response, HttpMessageNotReadableException ex
    ) throws ServletException, IOException {
        boolean isApiRequest = request.getRequestURI().startsWith("/api/");
        String acceptHeader = request.getHeader("Accept");
        boolean expectsJsonExplicitly = (acceptHeader != null && acceptHeader.contains(MediaType.APPLICATION_JSON_VALUE));
        boolean isAjaxRequest = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
        boolean shouldSendJson = isApiRequest || expectsJsonExplicitly || isAjaxRequest;

        Map<String, String> errors = new HashMap<>();
        String fieldName = "";

        Throwable mostSpecificCause = ex.getMostSpecificCause();
        if (mostSpecificCause != null && mostSpecificCause instanceof InvalidFormatException) {
            InvalidFormatException invalidFormatEx = (InvalidFormatException) mostSpecificCause;
            if (invalidFormatEx.getTargetType() != null && invalidFormatEx.getTargetType().isEnum()) {
                fieldName = invalidFormatEx.getPath().stream()
                        .map(p -> (p.getFieldName() != null) ? p.getFieldName() : "")
                        .collect(Collectors.joining(""));
                String errorMessage = "'" + invalidFormatEx.getValue() + "'은(는) 유효한 " +
                        invalidFormatEx.getTargetType().getSimpleName() + " 값이 아닙니다.\n" +
                        "(유효한 값: " + Arrays.toString(invalidFormatEx.getTargetType().getEnumConstants()) + ")";

                // 타입변환 오류 시 (예: String을 Enum 타입으로 변환 실패)
                errors.put((fieldName.isEmpty()) ? "general" : fieldName, (fieldName.isEmpty()) ? mostSpecificCause.getMessage() : errorMessage);
                log.warn("HttpMessageNotReadableException(1): {}", errors.toString());
//                return ResponseEntity.badRequest().body(errors);
            }
        } else if (ex.getMessage() != null && ex.getMessage().contains("JSON parse error")) {
            // 일반적인 JSON 파싱 에러
            errors.put("general", mostSpecificCause.getMessage());
            log.warn("HttpMessageNotReadableException(2): {}", errors.toString());
//            return ResponseEntity.badRequest().body(errors);
        } else {
            // 그 외 HttpMessageNotReadableException
            errors.put("general", (mostSpecificCause != null) ? mostSpecificCause.getMessage() : ex.getMessage());
            log.warn("HttpMessageNotReadableException(3): {}", errors.toString());
//            return ResponseEntity.badRequest().body(errors);
        }
        final String exceptionMessage = errors.get((fieldName.isEmpty()) ? "general" : fieldName);
        if (shouldSendJson == true) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);     // 400 Bad_Request
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);  // Content-Type 설정
            response.setCharacterEncoding("UTF-8");                     // 한글 깨짐 방지

            if (expectsJsonExplicitly == false && isAjaxRequest == true) {
                // 웹 브라우저용 JSON 응답 작성
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionMessage);
            } else {  // expectsJsonExplicitly == true && isAjaxRequest == false
                // 모바일 앱용 BasicResponse 및 ErrorResponse로 JSON 응답 작성
                return ResponseEntity.badRequest().body(BasicResponse.failure(
                        ErrorResponse.from(400, "Bad_Request", "JSON 파싱 또는 타입변환 오류가 발생했습니다.", exceptionMessage)
                ));
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute(RequestDispatcher.ERROR_MESSAGE, "Bad_Request");  // 발생한 예외 객체
            request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, new AccessDeniedException(exceptionMessage));  // 사용자에게 보여줄 메시지
            request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, request.getRequestURI());  // 원래 요청 URI
            request.getRequestDispatcher("/error").forward(request, response);

//            return (ResponseEntity<?>) errors;
            return null;
        }
    }

//    // 그 외 모든 예외 처리
//    @ExceptionHandler(Exception.class)
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    public ResponseEntity<?> handleAllExceptions(
//            HttpServletRequest request, HttpServletResponse response, Exception ex
//    ) throws ServletException, IOException {
//        boolean isApiRequest = request.getRequestURI().startsWith("/api/");
//        String acceptHeader = request.getHeader("Accept");
//        boolean expectsJsonExplicitly = (acceptHeader != null && acceptHeader.contains(MediaType.APPLICATION_JSON_VALUE));
//        boolean isAjaxRequest = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
//        boolean shouldSendJson = isApiRequest || expectsJsonExplicitly || isAjaxRequest;
//
//        Map<String, String> errors = new HashMap<>();
//        errors.put("general", "서버 내부에서 문제가 발생하여 요청을 처리할 수 없습니다: " + ex.getMessage());
//        log.error(ex.fillInStackTrace().getLocalizedMessage(), ex);  // 개발 중 디버깅을 위해 StackTrace 출력
////        return ResponseEntity.internalServerError().body(errors);
//
//        final String exceptionMessage = errors.get("general");
//        if (shouldSendJson == true) {
//            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);  // 500 Internal_Server_Error
//            response.setContentType(MediaType.APPLICATION_JSON_VALUE);         // Content-Type 설정
//            response.setCharacterEncoding("UTF-8");                            // 한글 깨짐 방지
//
//            if (expectsJsonExplicitly == false && isAjaxRequest == true) {
//                // 웹 브라우저용 JSON 응답 작성
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionMessage);
//            } else {  // expectsJsonExplicitly == true && isAjaxRequest == false
//                // 모바일 앱용 BasicResponse 및 ErrorResponse로 JSON 응답 작성
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BasicResponse.failure(
//                        ErrorResponse.from(500, "Internal_Server_Error", "서버 오류가 발생했습니다.", exceptionMessage)
//                ));
//            }
//        } else {
//            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//            request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//            request.setAttribute(RequestDispatcher.ERROR_MESSAGE, "Internal_Server_Error");  // 발생한 예외 객체
//            request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, new AccessDeniedException(exceptionMessage));  // 사용자에게 보여줄 메시지
//            request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, request.getRequestURI());  // 원래 요청 URI
//            request.getRequestDispatcher("/error").forward(request, response);
//
////            return (ResponseEntity<?>) errors;
//            return null;
//        }
//    }
}
