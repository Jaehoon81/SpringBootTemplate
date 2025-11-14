package kr.co.jaehoon.springboottemplate.dto.validation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "음성녹음 파일 업로드 요청 데이터")
@Data
public class RecordRequest {

    @Schema(description = "참가자 고유 ID", example = "1")
    @NotNull(message = "참가자 ID는 필수 항목입니다.")
    private Long participantId;

    @Schema(description = "녹음순서 (1, 2, 3 또는 그 이상)", example = "1")
    @NotNull(message = "녹음 순서는 필수 항목입니다.")
    @Min(value = 1, message = "녹음 순서는 1 이상이어야 합니다.")
    @Max(value = 3, message = "녹음 순서는 3 이하여야 합니다.")  // 최대 3개의 녹음을 가정
    private Short recordSequence;

    @Schema(description = "Base64 인코딩된 음성 데이터", example = "data:audio/mp4;base64,AAAAHGZ0eX...")  // 데이터 URI 형식으로 받을 수 있음
    @NotBlank(message = "Base64 인코딩된 음성 데이터는 필수 항목입니다.")
    private String base64Audio;

    @Schema(description = "음성 파일의 MIME 타입", example = "audio/mp4")
    @NotBlank(message = "MIME 타입은 필수 항목입니다.")
    private String mimeType;
}
