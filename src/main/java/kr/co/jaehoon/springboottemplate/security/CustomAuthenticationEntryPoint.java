package kr.co.jaehoon.springboottemplate.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.jaehoon.springboottemplate.dto.network.BasicResponse;
import kr.co.jaehoon.springboottemplate.dto.network.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component  // Spring @Bean으로 등록
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(  // 401 Unauthorized 에러를 처리할 핸들러 정의
            HttpServletRequest request, HttpServletResponse response, AuthenticationException authException
    ) throws IOException {
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
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);    // 401 Unauthorized
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);  // Content-Type 설정
            response.setCharacterEncoding("UTF-8");                     // 한글 깨짐 방지

            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("status", HttpServletResponse.SC_UNAUTHORIZED);
            errorDetails.put("error", "Unauthorized");
//            errorDetails.put("message", "인증 정보가 유효하지 않습니다.\n다시 로그인해주세요.");  // 상세 메시지
//            errorDetails.put("exceptionMessage", authException.getMessage());  // 예외 메시지 (개발 및 디버그 용도로 사용)

            String errorMessage = "인증 정보가 유효하지 않습니다.";
            String exceptionMessage = authException.getMessage();
            if (expectsJsonExplicitly == false && isAjaxRequest == true) {
                if (exceptionMessage != null && authException.getCause() instanceof AuthenticationException) {
                    // 전달받은 예외가 AuthenticationException(BadCredentialsException 포함)이고,
                    // 특정 메시지인 경우 해당 메시지를 사용 (특정 메시지를 그대로 반환)
                    if (exceptionMessage.contains("승인")) {
                        errorMessage = exceptionMessage;
                    } else if (exceptionMessage.startsWith("탈퇴")) {
                        errorMessage = exceptionMessage;
                    } else {  // 그 외 모든 인증 예외는 일반적인 메시지로 처리 (아이디 or 비밀번호 불일치 등)
                        errorMessage = "인증 정보가 유효하지 않습니다.";
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
            // 401 HTML 응답(response.sendError()) 대신, 로그인 페이지(login.jsp)로 리다이렉트
//            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: " + authException.getMessage());
            response.sendRedirect("/");
        }
    }
}
