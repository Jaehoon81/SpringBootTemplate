package kr.co.jaehoon.springboottemplate.dto.network;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(description = "JSON으로 반환 시 에러 응답 데이터")
@Data
@Builder
public class ErrorResponse {

    @Schema(description = "에러 코드", example = "401")
    private Integer statusCode;
    @Schema(description = "에러 종류", example = "Unauthorized")
    private String errorType;
    @Schema(description = "상세 메시지", example = "인증 정보가 유효하지 않습니다.")
    private String errorMessage;
    @Schema(description = "예외 메시지 (개발 및 디버그 용도로 사용)", example = "탈퇴 처리된 계정입니다.\n담당 관리자에게 문의해주세요.")
    private String exceptionMessage;

    /**
     * Error 상황의 정보 데이터를 ErrorResponse로 변환하는 팩토리 메서드
     * @param status 에러 코드
     * @param error 에러 종류
     * @param message 상세 메시지
     * @param exceptionMessage 예외 메시지 (개발 및 디버그 용도로 사용)
     * @return ErrorResponse 객체
     */
    public static ErrorResponse from(int status, String error, String message, String exceptionMessage) {
        return ErrorResponse.builder()
                .statusCode(status)
                .errorType(error)
                .errorMessage(message)
                .exceptionMessage(exceptionMessage)
                .build();
    }
}
