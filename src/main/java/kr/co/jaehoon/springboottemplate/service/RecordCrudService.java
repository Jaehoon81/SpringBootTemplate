package kr.co.jaehoon.springboottemplate.service;

import kr.co.jaehoon.springboottemplate.dto.RecordDTO;
import kr.co.jaehoon.springboottemplate.dto.validation.RecordRequest;
import kr.co.jaehoon.springboottemplate.repository.RecordRepository;
import kr.co.jaehoon.springboottemplate.repository.dao.RecordDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
//@Slf4j
public class RecordCrudService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${file.upload-dir}")  // 파일 업로드 디렉토리
    private String uploadDir;

//    private final RecordDAO recordDAO;
//    private final RecordRepository recordRepository;
    private final RecordService recordService;
    private final ParticipantCrudService participantCrudService;

    @Transactional
    public RecordDTO uploadAudioRecord(RecordRequest request, Long currentUserId) throws IOException, Exception {
        // 1. 참가자 소유권 확인 로직
        if (!participantCrudService.isOwner(request.getParticipantId(), currentUserId)) {
            throw new AccessDeniedException("해당 참가자에 대한 접근 권한이 없습니다.");
        }
        // 2. 녹음 순서(recordSequence)의 유효성 검증 로직
        Long participantId = request.getParticipantId();
        Short requestedSequence = request.getRecordSequence();
        // 1번 순서를 건너뛰고 다른 순서부터 시작하는지 확인
        if (requestedSequence > 1) {
            Optional<RecordDTO> prevSequenceRecord =
                    recordService.findByParticipantIdAndSequence(participantId, (short) (requestedSequence - 1));
            if (prevSequenceRecord.isEmpty()) {
                throw new IllegalArgumentException(
//                        "음성 녹음의 순서가 " + (requestedSequence - 1) + "번부터 순차적으로 업로드되어야 합니다.\n" +
                        "음성 녹음의 순서가 " + ((requestedSequence == 2) ? "1" : "1, 2") + "번부터 순차적으로 업로드되어야 합니다.\n" +
                        "현재 " + requestedSequence + "번 음성을 업로드할 수 없습니다."
                );
            }
        }
        // 3. Base64 데이터에서 MIME 타입과 실제 Base64 문자열 분리 (data:audio/mp4;base64,AAAAHGZ0eX... 형식 처리)
        String base64Data = request.getBase64Audio();
        String mimeType = request.getMimeType();
        // data URI 형식으로 전송될 경우 접두사를 제거
        if (base64Data.startsWith("data:") && base64Data.contains(";base64,")) {
            mimeType = base64Data.substring(5, base64Data.indexOf(";base64,"));  // 실제 MIME 타입 추출
            base64Data = base64Data.substring(base64Data.indexOf(";base64,") + 8);
        }
        // 4. 파일 저장 경로 생성 (년/월/일 구조)
        LocalDate today = LocalDate.now();
        String year = today.format(DateTimeFormatter.ofPattern("yyyy"));
        String month = today.format(DateTimeFormatter.ofPattern("MM"));
        String day = today.format(DateTimeFormatter.ofPattern("dd"));
        // ./uploads/audios/2025/10/15
        Path uploadPath = Paths.get(uploadDir, "audios", year, month, day).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);  // 디렉토리가 없으면 생성
        // 5. 파일명 생성 (UUID 사용으로 중복 방지)
        String fileExtension = getFileExtension(mimeType);
        String fileName = UUID.randomUUID().toString() + fileExtension;
        Path filePath = uploadPath.resolve(fileName);  // 실제 저장될 파일 경로
        // 6. 파일 저장
        byte[] audioBytes = Base64.getDecoder().decode(base64Data);
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            fos.write(audioBytes);
        }
        // 7. DB records 테이블에 저장 또는 업데이트
        RecordDTO record = new RecordDTO();
        record.setParticipantId(participantId);
        record.setRecordSequence(requestedSequence);
        record.setFilePath(filePath.toString());  // 저장된 파일의 절대 경로
        record.setMimeType(mimeType);

        // 이미 해당 순서의 녹음이 존재하는지 확인 (덮어쓰기는 허용)
        Optional<RecordDTO> existingRecordForSequence =
                recordService.findByParticipantIdAndSequence(participantId, requestedSequence);
        if (existingRecordForSequence.isPresent()) {
            // 기존 파일이 있다면 업데이트 (기존 파일은 삭제하고, 새로운 파일을 저장)
            Path oldFilePath = Paths.get(existingRecordForSequence.get().getFilePath());
            Files.deleteIfExists(oldFilePath);  // 기존 파일 삭제

            recordService.updateRecord(record);
            record.setRecordId(existingRecordForSequence.get().getRecordId());  // 업데이트 시에는 기존 ID 유지
        } else {
            // 새 레코드 추가
            recordService.saveRecord(record);
        }
        return record;
    }

    // 파일 확장자를 MIME 타입으로부터 추론
    private String getFileExtension(String mimeType) {
        if (mimeType == null) {
            return ".mp4";  // 기본값
        }
        return switch (mimeType) {
            case "audio/mpeg" -> ".mp3";
            case "audio/wav"  -> ".wav";
            case "audio/ogg"  -> ".ogg";
            case "audio/mp4"  -> ".mp4";  // MP4 오디오
            case "video/mp4"  -> ".mp4";  // MP4 비디오 (음성 포함)
            default           -> ".mp4";  // 기본값
        };
    }

    // 파일 시스템에서 특정 레코드의 파일을 읽어 바이트 배열로 반환하는 함수
    // (프론트엔드에 직접 전달 시 사용)
    public byte[] getAudioFile(Long recordId) throws IOException, Exception {
        Optional<RecordDTO> recordOptional = recordService.findByRecordId(recordId);
        if (recordOptional.isEmpty()) {
            return null;  // 또는 throw new FileNotFoundException("Record not found");
        }
        Path filePath = Paths.get(recordOptional.get().getFilePath());
        return Files.readAllBytes(filePath);
    }
}
