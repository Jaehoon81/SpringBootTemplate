package kr.co.jaehoon.springboottemplate.service;

import kr.co.jaehoon.springboottemplate.dto.RecordDTO;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface RecordService {

    public Optional<RecordDTO> findByRecordId(Long recordId) throws IOException;

    public Optional<RecordDTO> findByParticipantIdAndSequence(Long participantId, Short recordSequence) throws IOException;

    public List<RecordDTO> findRecordsByParticipantId(Long participantId) throws Exception;

    public void saveRecord(RecordDTO record) throws Exception;

    public void updateRecord(RecordDTO record) throws Exception;
}
