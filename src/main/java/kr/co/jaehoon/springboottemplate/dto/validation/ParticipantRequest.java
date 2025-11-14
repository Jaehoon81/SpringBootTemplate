package kr.co.jaehoon.springboottemplate.dto.validation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Schema(description = "참가자 등록 요청 데이터")
@Data
public class ParticipantRequest {

    @Schema(description = "참가자 이름", example = "홍길동")
    @NotBlank(message = "참가자 이름은 필수 항목입니다.")
    @Size(max = 50, message = "참가자 이름은 50자를 초과할 수 없습니다.")
    private String participantName;

    @Schema(description = "참가자 출생 연도", example = "1981")
    @NotNull(message = "출생 연도는 필수 항목입니다.")
    @Min(value = 1900, message = "출생 연도는 1900년 이상이어야 합니다.")
    @Max(value = 2099, message = "출생 연도는 2099년 이하여야 합니다.")
    private Short birthYear;

    @Schema(description = "참가자 출생 월", example = "5")
    @NotNull(message = "출생 월은 필수 항목입니다.")
    @Min(value = 1, message = "출생 월은 1월 이상이어야 합니다.")
    @Max(value = 12, message = "출생 월은 12월 이하여야 합니다.")
    private Short birthMonth;

    @Schema(description = "참가자 성별(Enum 타입)", example = "MALE", allowableValues = { "MALE", "FEMALE", "OTHER" })
    @NotNull(message = "성별은 필수 항목입니다.")
    // Enum 값에 대한 유효성 검사는 @Enumerated 또는 커스텀 어노테이션으로 처리 가능하지만,
    // 여기서는 문자열로 받아 Service 계층에서 변환 및 검증
    private Gender gender;  // Enum 타입으로 직접 수신받음

    @Schema(description = "참가자 등급(Enum 타입)", example = "GOLD", allowableValues = { "GOLD", "SILVER", "BRONZE", "NONE" })
    @NotNull(message = "등급은 필수 항목입니다.")
    // Enum 값에 대한 유효성 검사는 @Enumerated 또는 커스텀 어노테이션으로 처리 가능하지만,
    // 여기서는 문자열로 받아 Service 계층에서 변환 및 검증
    private Grade grade;  // Enum 타입으로 직접 수신받음
}
