package kr.co.jaehoon.springboottemplate.dto.validation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FindAccountRequest {

    @NotBlank(message = "이름은 필수 항목입니다.")
    private String displayname;

    @NotBlank(message = "이메일은 필수 항목입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;
}
