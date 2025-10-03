package kr.co.jaehoon.springboottemplate.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;
import java.util.Objects;

@Controller
@Slf4j
public class CustomErrorController implements ErrorController {

//    private final Logger log = LoggerFactory.getLogger(this.getClass());

//    @GetMapping("/error")
//    public String handleError(HttpServletRequest request, Model model) {
//        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
//        if (status != null) {
//            HttpStatus httpStatus = HttpStatus.valueOf(Integer.parseInt(status.toString()));
//            model.addAttribute("timeStamp", new Date());
//            model.addAttribute("statusCode", status.toString());
//            model.addAttribute("exceptionType", httpStatus.getReasonPhrase());
//
//            int statusCode = Integer.parseInt(status.toString());
//            return switch (statusCode) {
////                case 400, 403, 404 -> "error/4xx";  // BAD_REQUEST(400), FORBIDDEN(403), NOT_FOUND(404)
////                case 500 -> "error/500";            // INTERNAL_SERVER_ERROR(500)
//                case 400, 403, 404, 500 -> "error/error";  // 통합된 모든 에러
//                default -> "error/error";  // UNKNOWN
//            };
//        }
//        return "error/error";
//    }

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object error = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);      // error.jsp의 ${error}
        Object message = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);  // error.jsp의 ${message}
        Object timestamp = request.getAttribute("timestamp");  // 에러발생 시간

        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            model.addAttribute("status", statusCode);

            if (statusCode == HttpStatus.FORBIDDEN.value()) {
                model.addAttribute("error", "접근 권한이 없습니다.");
            } else if (statusCode == HttpStatus.NOT_FOUND.value()) {
                model.addAttribute("error", "페이지를 찾을 수 없습니다.");
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                model.addAttribute("error", "서버 오류가 발생했습니다.");
            } else {
                model.addAttribute("error", (error != null) ? error.toString() : "알 수 없는 오류");
            }
        }
        Throwable exception = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        if (exception != null) {
            // 모델에 상세 메시지 추가 (실제 운영 환경에서는 보안상 stacktrace를 클라이언트에 직접 노출하지 않도록 수정)
            model.addAttribute("message", exception.getMessage());
        } else if (error != null) {
            // ERROR_MESSAGE를 message로 활용
            model.addAttribute("message", error.toString());
        } else if (message != null) {
            model.addAttribute("message", message.toString());
        }
        // timestamp는 BasicErrorController에 의해 자동으로 추가
        model.addAttribute("timestamp", Objects.requireNonNullElseGet(timestamp, Date::new));
        return "error/error";
    }

    // 의도적으로 에러를 발생시키는 페이지 (500 Internal_Server_Error 유발)
    @GetMapping("/trigger-error-page")
    public String triggerErrorPage() {
        throw new RuntimeException("의도적으로 발생시킨 에러입니다!");
    }

    // 에러 테스트용 API 예시 (500 Internal_Server_Error 유발)
    @GetMapping("/api/auth/test-error")
    public ResponseEntity<String> testError() {
        // 의도적으로 NullPointerException을 발생시켜 500 에러를 유발
        String test = null;
        test.length();
        return ResponseEntity.ok("NullPointerException에 의한 500 Internal_Server_Error");
    }

    // ADMIN 권한만 접근 가능한 API 예시 (403 Forbidden 테스트용)
    @GetMapping("/api/auth/admin-only")
    public ResponseEntity<String> adminOnlyEndpoint() {
        return ResponseEntity.ok("관리자만 접근 가능한 정보입니다!");
    }
}
