package kr.co.jaehoon.springboottemplate.dto.network;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.jaehoon.springboottemplate.dto.ParticipantDTO;
import kr.co.jaehoon.springboottemplate.dto.validation.Gender;
import kr.co.jaehoon.springboottemplate.dto.validation.Grade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Schema(description = "참가자 및 음성녹음 목록 응답 데이터 (웹 브라우저용)")
@Data
@Builder
@NoArgsConstructor  // 매개변수가 없는 기본 생성자를 자동으로 생성
@AllArgsConstructor  // 모든 필드를 인자로 받는 생성자를 자동으로 생성
public class ParticipantListResponse {

    @Schema(description = "리스트 번호(인덱스)", example = "10")
    private Long listNumber;  // 페이지 내에서 번호 매김
    @Schema(description = "참가자 고유 ID", example = "1")
    private Long participantId;
    @Schema(description = "참가자 이름", example = "홍길동")
    private String participantName;
    @Schema(description = "참가자 출생연월 (yyyy/MM)", example = "1981/05")
    private String birthYearMonth;
    @Schema(description = "참가자 성별(한글 표시)", example = "남자")
    private String genderKor;
    @Schema(description = "참가자 등급(Enum 타입)", example = "GOLD", allowableValues = { "GOLD", "SILVER", "BRONZE", "NONE" })
    private Grade grade;
    @Schema(description = "담당 관리자 이름", example = "관리자01")
    private String assignedAdminName;
    @Schema(description = "음성녹음 재생용 정보 목록",
            example = "[{ recordId: 101, recordSequence: 1, mimeType: 'audio/mp4', updatedAt: '2025/10/15' }, " +
                    "{ recordId: 102, recordSequence: 2, ... }, " +
                    "{ recordId: 103, recordSequence: 3, ... }]")
    private List<RecordInfo> audioRecordList;
    @Schema(description = "첫 번째 녹음일자 (yyyy/MM/dd)", example = "2025/10/15")
    private String firstRecordDate;

    // 내부 클래스: 음성녹음 재생에 필요한 최소한의 정보
    @Schema(description = "단일 음성녹음 정보 (재생용)")
    @Data
    @Builder
    public static class RecordInfo {

        @Schema(description = "음성녹음 고유 ID", example = "101")
        private Long recordId;
        @Schema(description = "녹음순서 (1, 2, 3 또는 그 이상)", example = "1")
        private Short recordSequence;
        @Schema(description = "음성 파일의 MIME 타입", example = "audio/mp4")
        private String mimeType;
        @Schema(description = "녹음 수정일자 (yyyy/MM/dd)", example = "2025/10/15")
        private String updatedAt;
    }

    /**
     * Participant 모델(DTO) 객체를 ParticipantListResponse로 변환하는 팩토리 메서드
     * (초기 객체 생성용이며, 실제 값들은 Mapper에서 주입됨)
     * @param participant 변환할 ParticipantDTO 객체
     * @param audioRecords 참가자가 녹음한 음성 데이터 List
     * @return ParticipantListResponse 객체
     */
    public static ParticipantListResponse from(ParticipantDTO participant, List<RecordInfo> audioRecords) {
        String birthYearMonth = participant.getBirthYear() + "/" + String.format("%02d", participant.getBirthMonth());

        String genderKor = "";
        if (participant.getGender() != null) {
            genderKor = switch (participant.getGender()) {
                case MALE   -> "남자";
                case FEMALE -> "여자";
                case OTHER  -> "기타";
                default     -> "알 수 없음";
            };
        }
        String firstRecordDate = "-";
        if (audioRecords != null && !audioRecords.isEmpty()) {
            audioRecords.stream()
                    .filter(rec -> (rec.getRecordSequence() != null && rec.getRecordSequence() == 1))
                    .findFirst()
                    .ifPresent(rec -> {
                        // Mapper에서 select한 created_at을 받아서 처리
                    });
        }
        return ParticipantListResponse.builder()
                .participantId(participant.getParticipantId())
                .participantName(participant.getParticipantName())
                .birthYearMonth(birthYearMonth)
                .genderKor(genderKor)
                .grade(participant.getGrade())
                .assignedAdminName(participant.getAssignedAdminName())  // Mapper에서 조인으로 채워짐
                .audioRecordList(audioRecords)  // Mapper에서 resultMap의 collection 태그로 채워짐
                .firstRecordDate(firstRecordDate)  // Mapper에서 서브 쿼리로 채워짐
                .build();
    }
}
