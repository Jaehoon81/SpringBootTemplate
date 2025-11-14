package kr.co.jaehoon.springboottemplate.dto.validation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {

    // 사용자 계정의 ID값
    private Long id;  // @AuthenticationPrincipal을 통해 백엔드에서 설정

    @NotBlank(message = "이름은 필수 항목입니다.")
    @Size(max = 10, message = "이름은 10자 이하로 입력해주세요.")
    // 10자 이하, 한글/영문 대소문자/숫자 포함 정규식
    @Pattern(regexp = "^[a-zA-Z가-힣0-9\\s]+$", message = "이름은 한글, 영문 대소문자, 숫자만 가능합니다.")
    private String displayname;

    @NotBlank(message = "이메일은 필수 항목입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "현재 비밀번호는 필수 항목입니다.")
    private String currentPassword;

    // 비밀번호를 변경하지 않을 경우 null이나 empty 값이 입력되므로 @NotBlank 제거
//    @NotBlank(message = "새 비밀번호는 필수 항목입니다.")
    // 값이 있을 경우에만 유효성 검사 (@Pattern.regexp의 (?=...)는 빈 문자열 허용을 의미)
    @Size(min = 10, message = "새 비밀번호는 10자 이상이어야 합니다.", groups = PasswordChangeGroup.class)
    // 영문, 숫자, 특수문자를 모두 포함하는 정규식 (빈 문자열은 검사하지 않음)
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "새 비밀번호는 영문, 숫자, 특수문자를 모두 포함해야 합니다.",
            groups = PasswordChangeGroup.class)
    private String newPassword;

    // 비밀번호(신규) 확인 입력 필드
    private String confirmPassword;  // 프론트엔드 및 백엔드에서 newPassword와 일치하는지 확인
}
