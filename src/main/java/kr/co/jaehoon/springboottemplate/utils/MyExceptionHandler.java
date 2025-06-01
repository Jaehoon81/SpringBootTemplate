package kr.co.jaehoon.springboottemplate.utils;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@Slf4j
public class MyExceptionHandler {

//    private final Logger log = LoggerFactory.getLogger(this.getClass());

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<String> handleException(Exception e) {
//        log.error(e.getMessage(), e);
//
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("400_BAD_REQUEST");  // 400 Error
//    }

//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)  // 500 Error
//    @ExceptionHandler
//    public String handleError500(Exception e, Model model) {
//        log.error(e.getMessage(), e);
//
//        model.addAttribute("errorMsg", "500_INTERNAL_SERVER_ERROR: " + e.getMessage());
//        return "error/500";
//    }
}
