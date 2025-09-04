package kr.co.jaehoon.springboottemplate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
@Getter
@Setter
@ToString
public class RegistrationRequest {

    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "아이디는 영문 대소문자, 숫자만 가능합니다.")
    @Size(min = 4, max = 20, message = "아이디는 4자 이상 20자 이하로 입력해야 합니다.")
    private String username;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    // 최소 10자, 영문/숫자/특수문자 포함 정규식
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?~])[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?~]{10,}$",
            message = "비밀번호는 10자 이상의 영문, 숫자, 특수문자를 모두 포함해야 합니다.")
    private String password;

    private String displayname;  // 이름은 NotBlank가 아님 (선택 사항)

    @NotBlank(message = "권한은 필수 입력 값입니다.")
    private String role;
}
