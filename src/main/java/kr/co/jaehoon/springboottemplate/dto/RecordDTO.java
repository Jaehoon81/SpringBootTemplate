package kr.co.jaehoon.springboottemplate.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@ToString
public class RecordDTO {

    private Long recordId;
    private Long participantId;
//    private Byte recordSequence;  // Swagger UI에서 오류 발생
    private Short recordSequence;
    private String filePath;
    private String mimeType;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
