package kr.co.jaehoon.springboottemplate.dto.network;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.jaehoon.springboottemplate.dto.ParticipantDTO;
import kr.co.jaehoon.springboottemplate.dto.validation.Gender;
import kr.co.jaehoon.springboottemplate.dto.validation.Grade;
import lombok.Builder;
import lombok.Data;

@Schema(description = "참가자 등록/조회 응답 데이터")
@Data
@Builder
public class ParticipantResponse {

    @Schema(description = "참가자 고유 Index", example = "1")
    private Long participantIndex;
    @Schema(description = "참가자 이름", example = "홍길동")
    private String participantName;
    @Schema(description = "참가자 출생 연도", example = "1981")
    private Short birthYear;
    @Schema(description = "참가자 출생 월", example = "5")
    private Short birthMonth;
    @Schema(description = "참가자 성별(Enum 타입)", example = "MALE", allowableValues = { "MALE", "FEMALE", "OTHER" })
    private Gender gender;
    @Schema(description = "참가자 등급(Enum 타입)", example = "GOLD", allowableValues = { "GOLD", "SILVER", "BRONZE", "NONE" })
    private Grade grade;
    @Schema(description = "응답 메시지", example = "참가자 등록 성공")
    private String resMessage;

    /**
     * Participant 모델(DTO) 객체를 ParticipantResponse로 변환하는 팩토리 메서드
     * @param participant 변환할 ParticipantDTO 객체
     * @param resMessage 응답 메시지
     * @return ParticipantResponse 객체
     */
    public static ParticipantResponse from(ParticipantDTO participant, String resMessage) {
        return ParticipantResponse.builder()
                .participantIndex(participant.getParticipantId())
                .participantName(participant.getParticipantName())
                .birthYear(participant.getBirthYear())
                .birthMonth(participant.getBirthMonth())
                .gender(participant.getGender())
                .grade(participant.getGrade())
                .resMessage(resMessage)
                .build();
    }
}
