package kr.co.jaehoon.springboottemplate.dto.validation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistrationRequest {

    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    // 4자 이상 20자 이하, 영문 대소문자/숫자 포함 정규식
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "아이디는 영문 대소문자, 숫자만 가능합니다.")
    @Size(min = 4, max = 20, message = "아이디는 4자 이상 20자 이하로 입력해야 합니다.")
    private String username;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    // 10자 이상, 영문/숫자/특수문자 포함 정규식
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?~])[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?~]{10,}$",
            message = "비밀번호는 10자 이상의 영문, 숫자, 특수문자를 모두 포함해야 합니다.")
    private String password;

    @NotBlank(message = "이름은 필수 입력 값입니다.")
    // 10자 이하, 한글/영문 대소문자/숫자 포함 정규식
    @Pattern(regexp = "^[a-zA-Z가-힣0-9\\s]+$", message = "이름은 한글, 영문 대소문자, 숫자만 가능합니다.")
    @Size(max = 10, message = "이름은 10자 이하로 입력해야 합니다.")
    private String displayname;

    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;

    // 요청 메시지는 NotBlank가 아님 (유효성 검사는 클라이언트나 서비스 레이어에서 필요 시 추가)
    private String reqMessage;

    @NotBlank(message = "권한은 필수 입력 값입니다.")
    @Pattern(regexp = "^(USER|ADMIN)$", message = "권한은 '일반 사용자' 또는 '관리자'만 선택 가능합니다.")
    private String role;  // UI에서 오는 값 (USER or ADMIN)
    private Long roleId;  // rolename으로 role_id를 조회한 후 저장

    // USER 권한일 때만 필수, 백엔드 컨트롤러에서 role에 따른 유효성 검사 추가 필요
    @Pattern(regexp = "^(?!USER$).*|(.+)", message = "일반 사용자 선택 시 담당 관리자를 선택해야 합니다.")
    // 선택된 관리자 이름은 조건부 NotBlank 적용
    private String adminname;  // 담당 관리자(ADMIN)의 displayname
}
