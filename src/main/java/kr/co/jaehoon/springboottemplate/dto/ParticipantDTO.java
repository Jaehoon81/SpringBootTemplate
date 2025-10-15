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
}
