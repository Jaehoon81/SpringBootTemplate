package kr.co.jaehoon.springboottemplate.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.jaehoon.springboottemplate.dto.validation.Gender;
import kr.co.jaehoon.springboottemplate.dto.validation.Grade;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@ToString
public class ParticipantDTO {

    private Long participantId;
    private Long userId;
    private String participantName;
    private Short birthYear;
//    private Byte birthMonth;  // Swagger UI에서 오류 발생
    private Short birthMonth;
    private Gender gender;
    private Grade grade;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // USER 권한의 계정인 경우, 담당 관리자(ADMIN)의 displayname을 가져오기 위한 필드 (DB 컬럼은 아니지만 조인해서 매핑)
    private String assignedAdminName;
}
