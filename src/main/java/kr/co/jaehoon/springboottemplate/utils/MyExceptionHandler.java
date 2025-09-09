package kr.co.jaehoon.springboottemplate.utils;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
//@Slf4j
public class MyExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)  // 500 Internal_Server_Error 반환
//    @ExceptionHandler
//    public String handleError500(Exception e, Model model) {
//        log.error(e.getMessage(), e);
//
//        model.addAttribute("errorMsg", "500_INTERNAL_SERVER_ERROR: " + e.getMessage());
//        return "error/500";
//    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<String> handleException(Exception e) {
//        log.error(e.getMessage(), e);
//
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("400_BAD_REQUEST");
//    }

    // Bean Validation 실패 시 예외 처리 핸들러
    // (프론트엔드 유효성 검사를 우회하여 백엔드에 Postman 등으로 직접 API 호출을 통한 유효하지 않은 아이디를 보내면 여기서 에러 메시지를 반환)
    @ResponseStatus(HttpStatus.BAD_REQUEST)  // 400 Bad_Request 반환
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
}
