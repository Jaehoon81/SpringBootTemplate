package kr.co.jaehoon.springboottemplate.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.jaehoon.springboottemplate.dto.network.BasicResponse;
import kr.co.jaehoon.springboottemplate.dto.network.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component  // Spring @Bean으로 등록
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(  // 403 Forbidden 에러를 처리할 핸들러 정의
            HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException
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

        if (shouldSendJson == true) {
            // JSON 응답을 기대하는 클라이언트 (모바일 앱, API 클라이언트 등)
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);       // 403 Forbidden
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);  // Content-Type 설정
            response.setCharacterEncoding("UTF-8");                     // 한글 깨짐 방지

            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("status", HttpServletResponse.SC_FORBIDDEN);
            errorDetails.put("error", "Forbidden");
//            errorDetails.put("message", "접근 권한이 없습니다.\n다른 계정으로 로그인해주세요.");  // 상세 메시지
//            errorDetails.put("exceptionMessage", accessDeniedException.getMessage());  // 예외 메시지 (개발 및 디버그 용도로 사용)

            String errorMessage = "접근 권한이 없습니다.";
            String exceptionMessage = accessDeniedException.getMessage();
            if (expectsJsonExplicitly == false && isAjaxRequest == true) {
//                if (exceptionMessage != null && accessDeniedException.getCause() instanceof AccessDeniedException) {
                if (exceptionMessage != null && accessDeniedException instanceof AccessDeniedException) {
                    // 전달받은 예외가 AccessDeniedException이고,
                    // 특정 메시지인 경우 해당 메시지를 사용 (특정 메시지를 그대로 반환)
                    if (exceptionMessage.contains("등급")) {
                        errorMessage = exceptionMessage;
                    } else {  // 그 외 모든 권한 예외는 일반적인 메시지로 처리 (로그인한 사용자가 해당 권한이 없는 경우 등)
                        errorMessage = "접근 권한이 없습니다.";
                    }
                }
                errorDetails.put("message", errorMessage);  // 상세 메시지
                errorDetails.put("exceptionMessage", exceptionMessage);  // 예외 메시지 (개발 및 디버그 용도로 사용)
                // 웹 브라우저용 JSON 응답 작성
                objectMapper.writeValue(response.getWriter(), errorDetails);
            } else {  // expectsJsonExplicitly == true && isAjaxRequest == false
                errorDetails.put("message", errorMessage);  // 상세 메시지
                errorDetails.put("exceptionMessage", exceptionMessage);  // 예외 메시지 (개발 및 디버그 용도로 사용)
                // 모바일 앱용 BasicResponse 및 ErrorResponse로 JSON 응답 작성
                objectMapper.writeValue(response.getWriter(), BasicResponse.failure(
                        ErrorResponse.from(
                                (int) errorDetails.get("status"),
                                (String) errorDetails.get("error"),
                                (String) errorDetails.get("message"),
                                (String) errorDetails.get("exceptionMessage"))
                ));
            }
        } else {
            // 웹 브라우저에서 직접 접근(페이지 요청)인 경우:
            // 1. 403 HTML 응답(response.sendError()) 대신, 응답 상태 코드를 403으로 설정하고 /error 경로로 포워드
//            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden: " + accessDeniedException.getMessage());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            // 2. CustomErrorController에서 사용할 에러 속성들을 수동으로 설정
            request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute(RequestDispatcher.ERROR_MESSAGE, "Forbidden");  // 발생한 예외 객체
//            request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, accessDeniedException);  // 사용자에게 보여줄 메시지
            request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, new AccessDeniedException("접근 권한이 없습니다: " + accessDeniedException.getMessage()));
            request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, request.getRequestURI());  // 원래 요청 URI
            // 3. Spring Boot의 기본 에러 핸들러(BasicErrorController)가 /error 요청을 처리하고 error.jsp를 렌더링
            request.getRequestDispatcher("/error").forward(request, response);
        }
    }
}
