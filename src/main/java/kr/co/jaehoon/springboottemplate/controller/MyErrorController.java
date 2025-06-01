package kr.co.jaehoon.springboottemplate.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Date;

@Controller
public class MyErrorController implements ErrorController {

    @GetMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status != null) {
            HttpStatus httpStatus = HttpStatus.valueOf(Integer.parseInt(status.toString()));
            model.addAttribute("timeStamp", new Date());
            model.addAttribute("statusCode", status.toString());
            model.addAttribute("exceptionType", httpStatus.getReasonPhrase());

            int statusCode = Integer.parseInt(status.toString());
            return switch (statusCode) {
//                case 400 -> "error/400";  // BAD_REQUEST
//                case 403 -> "error/403";  // FORBIDDEN
//                case 404 -> "error/404";  // NOT_FOUND
                case 400, 403, 404 -> "error/4xx";
                case 500 -> "error/500";    // INTERNAL_SERVER_ERROR
                default ->  "error/error";  // UNKNOWN
            };
        }
        return "error/error";
    }
}
