package kr.co.jaehoon.springboottemplate.dto.network;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.jaehoon.springboottemplate.dto.RecordDTO;
import lombok.Builder;
import lombok.Data;

@Schema(description = "음성녹음 업로드/업데이트 응답 데이터")
@Data
@Builder
public class RecordResponse {

    @Schema(description = "음성녹음 고유 Index", example = "101")
    private Long recordIndex;
    @Schema(description = "녹음순서 (1, 2, 3 또는 그 이상)", example = "1")
    private Short recordSequence;
    @Schema(description = "음성 파일의 저장 경로", example = "./uploads/audios/2025/10/15/...")
    private String filePath;
    @Schema(description = "음성 파일의 MIME 타입", example = "audio/mp4")
    private String mimeType;
    @Schema(description = "응답 메시지", example = "음성녹음 업로드/업데이트 성공")
    private String resMessage;

    /**
     * Record 모델(DTO) 객체를 RecordResponse로 변환하는 팩토리 메서드
     * @param record 변환할 RecordDTO 객체
     * @param resMessage 응답 메시지
     * @return RecordResponse 객체
     */
    public static RecordResponse from(RecordDTO record, String resMessage) {
        return RecordResponse.builder()
                .recordIndex(record.getRecordId())
                .recordSequence(record.getRecordSequence())
                .filePath(record.getFilePath())
                .mimeType(record.getMimeType())
                .resMessage(resMessage)
                .build();
    }
}
