package kr.co.jaehoon.springboottemplate.dto.validation;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GradeUpdateRequest {

    @NotNull(message = "참가자 ID는 필수 항목입니다.")
    private Long participantId;

    @NotNull(message = "변경할 등급은 필수 항목입니다.")
    private Grade newGrade;  // Enum 타입으로 직접 수신받음
}
